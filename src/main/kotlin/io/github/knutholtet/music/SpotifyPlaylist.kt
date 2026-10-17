package io.github.knutholtet.music

import java.net.URI

object SpotifyPlaylist {
    private val playlistPath = Regex("/(?:intl-[a-zA-Z-]+/)?playlist/([a-zA-Z0-9]{22})/?")
    private val playlistUri = Regex("spotify:playlist:([a-zA-Z0-9]{22})")

    /** Accept only Spotify playlist identifiers; never forward arbitrary URLs to Lavalink. */
    fun normalize(input: String): String? {
        val value = input.trim().removeSurrounding("<", ">")
        playlistUri.matchEntire(value)?.let {
            return "https://open.spotify.com/playlist/${it.groupValues[1]}"
        }
        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        if (uri.scheme != "https" || !uri.host.equals("open.spotify.com", ignoreCase = true) ||
            uri.userInfo != null || uri.port != -1
        ) return null
        val match = playlistPath.matchEntire(uri.path ?: return null) ?: return null
        return "https://open.spotify.com/playlist/${match.groupValues[1]}"
    }
}
