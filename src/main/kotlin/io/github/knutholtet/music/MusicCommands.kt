package io.github.knutholtet.music

import dev.arbjerg.lavalink.protocol.v4.LoadResult
import dev.arbjerg.lavalink.protocol.v4.Track
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.string
import dev.schlaubi.lavakord.audio.Link
import dev.schlaubi.lavakord.audio.TrackEndEvent
import dev.schlaubi.lavakord.audio.TrackStuckEvent
import dev.schlaubi.lavakord.kord.getLink
import dev.schlaubi.lavakord.kord.lavakord
import dev.schlaubi.lavakord.rest.loadItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

class MusicCommands(private val kord: Kord) {
    private val spotify = SpotifyClient()
    private val sessions = ConcurrentHashMap<Snowflake, Session>()
    private val password = System.getenv("LAVALINK_PASSWORD")?.takeIf { it.isNotBlank() }
    private val lavalink = password?.let {
        kord.lavakord().apply {
            addNode(System.getenv("LAVALINK_URL") ?: "ws://localhost:2333", it)
        }
    }

    suspend fun register() {
        // Individual upserts preserve the bot's existing utility commands.
        kord.createGlobalChatInputCommand("play", "Queue a Spotify playlist in your voice channel") {
            string("playlist", "Spotify playlist URL or spotify:playlist: URI") { required = true }
        }
        kord.createGlobalChatInputCommand("skip", "Skip the current song")
        kord.createGlobalChatInputCommand("stop", "Clear the music queue and leave voice")

        kord.on<ChatInputCommandInteractionCreateEvent> {
            val command = interaction.command.rootName
            if (command !in setOf("play", "skip", "stop")) return@on
            val response = interaction.deferEphemeralResponse()
            val result = try {
                withTimeout(60_000) {
                    val guildId = interaction.data.guildId.value
                    requireNotNull(guildId) { "Use this command in a server." }
                    val node = requireNotNull(lavalink) {
                        "Music is not configured. Ask the bot owner to set LAVALINK_PASSWORD and start Lavalink (see README)."
                    }
                    val channelId = interaction.user.asMember(guildId).getVoiceStateOrNull()?.channelId
                    requireNotNull(channelId) { "Join a voice channel first." }
                    val playlist = if (command == "play") {
                        requireNotNull(SpotifyPlaylist.normalize(interaction.command.strings["playlist"].orEmpty())) {
                            "Provide a full https://open.spotify.com/playlist/... link. Short links, albums and tracks are not supported."
                        }
                    } else null
                    val session = if (command == "play") {
                        sessions.computeIfAbsent(guildId) { Session(node.getLink(guildId), spotify) }
                    } else sessions[guildId]
                    if (session == null) "Nothing is playing." else session.mutex.withLock {
                        val botChannel = kord.getGuild(guildId).getMember(kord.selfId)
                            .getVoiceStateOrNull()?.channelId
                        require(botChannel == null || botChannel == channelId) {
                            "Join the same voice channel as the bot to control its music."
                        }
                        when (command) {
                            "play" -> session.enqueue(playlist!!, channelId)
                            "skip" -> session.skip()
                            else -> {
                                session.stop()
                                "Stopped playback, cleared the queue and left voice."
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                "Music request timed out. Check Lavalink and try /stop before retrying."
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                e.message ?: "Invalid music request."
            } catch (e: Exception) {
                // Don't send remote exceptions (which may contain tokens/URLs) to Discord.
                System.err.println("Music command failed: ${e.javaClass.simpleName}")
                "Could not play music. Check Lavalink, Spotify access, and the bot's View Channel, Connect and Speak permissions."
            }
            response.respond { content = result }
        }
    }

    private class Session(private val link: Link, private val spotify: SpotifyClient) {
        val mutex = Mutex()
        private val queue = TrackQueue<SpotifySong>()
        private var currentAudio: Track? = null

        init {
            // Subscribe before the first track can finish; process events in order.
            link.launch(start = CoroutineStart.UNDISPATCHED) {
                link.player.events.collect { event ->
                    try {
                        mutex.withLock {
                            when (event) {
                                is TrackEndEvent -> {
                                    // STOPPED/REPLACED events are caused by our own skip/stop.
                                    if (event.reason.mayStartNext &&
                                        currentAudio?.encoded == event.track.encoded
                                    ) playNext()
                                }
                                is TrackStuckEvent -> {
                                    if (currentAudio?.encoded == event.track.encoded) playNext()
                                }
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        System.err.println("Music queue timed out; cleared the queue.")
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        System.err.println("Music queue failed: ${e.javaClass.simpleName}")
                    }
                }
            }
        }

        suspend fun enqueue(url: String, channelId: Snowflake): String {
            val tracks = spotify.playlist(url)
            if (tracks.isEmpty()) return "This playlist has no playable tracks."
            require(queue.size + tracks.size <= 1000) { "The queue limit is 1000 tracks. Use /stop before adding this playlist." }
            if (link.state != Link.State.CONNECTED) {
                // A disconnected voice session must not leave a stale current track blocking playback.
                queue.clear()
                link.connectAudio(channelId.value)
                withTimeout(15_000) {
                    while (link.state != Link.State.CONNECTED) delay(100)
                }
            }
            queue.add(tracks)
            if (queue.current == null) playNext()
            return "Queued ${tracks.size} tracks from <$url> in playlist order. Unavailable audio matches are skipped."
        }

        suspend fun skip(): String {
            if (queue.current == null) return "Nothing is playing."
            playNext()
            return "Skipped the current song."
        }

        suspend fun stop() {
            queue.clear()
            currentAudio = null
            try {
                if (link.state == Link.State.CONNECTED) link.player.stopTrack()
            } finally {
                link.disconnectAudio()
            }
        }

        private suspend fun playNext() {
            currentAudio = null
            try {
                withTimeout(30_000) {
                    while (true) {
                        val song = queue.next()
                        if (song == null) {
                            link.player.stopTrack()
                            return@withTimeout
                        }
                        val loaded = link.loadItem(song.query)
                        val audio = when (loaded) {
                            is LoadResult.SearchResult -> loaded.data.tracks.firstOrNull()
                            is LoadResult.TrackLoaded -> loaded.data
                            else -> null
                        } ?: continue
                        currentAudio = audio
                        link.player.playTrack(audio)
                        return@withTimeout
                    }
                }
            } catch (e: Exception) {
                queue.clear()
                currentAudio = null
                throw e
            }
        }
    }
}
