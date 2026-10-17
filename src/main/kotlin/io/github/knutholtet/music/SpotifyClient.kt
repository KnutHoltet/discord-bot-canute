package io.github.knutholtet.music

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

internal data class SpotifySong(val title: String, val artists: String) {
    val query: String get() = "ytsearch:$title $artists"
}

internal class SpotifyClient(
    private val client: HttpClient = HttpClient(CIO) {
        install(HttpTimeout) { requestTimeoutMillis = 15_000 }
        followRedirects = false
    },
    private val clientId: String = System.getenv("SPOTIFY_CLIENT_ID").orEmpty(),
    private val clientSecret: String = System.getenv("SPOTIFY_CLIENT_SECRET").orEmpty(),
    private var refreshToken: String = System.getenv("SPOTIFY_REFRESH_TOKEN").orEmpty()
) {
    private val tokenMutex = Mutex()
    private var accessToken = ""
    private var expiresAt = 0L

    private suspend fun token(force: Boolean = false): String = tokenMutex.withLock {
        require(clientId.isNotBlank() && clientSecret.isNotBlank() && refreshToken.isNotBlank()) {
            "Set SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET and SPOTIFY_REFRESH_TOKEN (see README)."
        }
        if (!force && System.currentTimeMillis() < expiresAt) return@withLock accessToken
        val response = client.submitForm("https://accounts.spotify.com/api/token", Parameters.build {
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
        }) { basicAuth(clientId, clientSecret) }
        require(response.status.value == 200) { "Spotify authorization failed. Re-authorize the Spotify account using the README setup." }
        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        accessToken = json["access_token"]!!.jsonPrimitive.content
        refreshToken = json["refresh_token"]?.jsonPrimitive?.contentOrNull ?: refreshToken
        expiresAt = System.currentTimeMillis() + ((json["expires_in"]?.jsonPrimitive?.longOrNull ?: 3600) - 60) * 1000
        accessToken
    }

    suspend fun playlist(url: String): List<SpotifySong> {
        val id = requireNotNull(SpotifyPlaylist.normalize(url)).substringAfterLast('/')
        val songs = mutableListOf<SpotifySong>()
        var offset = 0
        while (true) {
            // Construct every page ourselves: never send a bearer token to an API-provided URL.
            val endpoint = "https://api.spotify.com/v1/playlists/$id/items?limit=50&offset=$offset"
            var bearer = token()
            var response = client.get(endpoint) { bearerAuth(bearer) }
            if (response.status.value == 401) {
                bearer = token(force = true)
                response = client.get(endpoint) { bearerAuth(bearer) }
            }
            when (response.status.value) {
                200 -> Unit
                403 -> throw IllegalArgumentException("Spotify denied access. The authorized account must own or collaborate on this playlist and have the required scopes.")
                404 -> throw IllegalArgumentException("Spotify playlist not found or inaccessible to the authorized account.")
                429 -> throw IllegalArgumentException("Spotify rate limit reached. Wait ${response.headers["Retry-After"]?.toIntOrNull() ?: 60} seconds and retry.")
                else -> throw IllegalArgumentException("Spotify is unavailable or authorization expired. Try again or re-authorize the account.")
            }
            val page = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val items = page["items"]?.jsonArray ?: error("Missing Spotify playlist items")
            songs += parseSongs(items)
            if (page["next"] == null || page["next"] == JsonNull) break
            require(items.isNotEmpty()) { "Spotify returned an incomplete playlist. Try again." }
            offset += items.size
            require(offset < 1000) { "Playlists longer than 1000 entries are not supported. Split the playlist first." }
        }
        return songs
    }

    companion object {
        internal fun parseSongs(items: JsonArray): List<SpotifySong> = items.mapNotNull { entry ->
            val wrapper = entry as? JsonObject ?: return@mapNotNull null
            val track = (wrapper["item"] ?: wrapper["track"]) as? JsonObject ?: return@mapNotNull null
            if (wrapper["is_local"]?.jsonPrimitive?.booleanOrNull == true ||
                track["is_local"]?.jsonPrimitive?.booleanOrNull == true ||
                track["is_playable"]?.jsonPrimitive?.booleanOrNull == false ||
                track["type"]?.jsonPrimitive?.contentOrNull != "track") return@mapNotNull null
            val title = track["name"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val artists = (track["artists"] as? JsonArray)?.mapNotNull {
                (it as? JsonObject)?.get("name")?.jsonPrimitive?.contentOrNull
            }?.joinToString(" ").orEmpty()
            if (artists.isBlank()) null else SpotifySong(title, artists)
        }
    }
}
