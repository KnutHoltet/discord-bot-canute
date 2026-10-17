package io.github.knutholtet.music

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlin.test.*

class SpotifyClientTest {
    private val url = "https://open.spotify.com/playlist/0123456789012345678901"
    private val song = """{"item":{"type":"track","name":"Song","artists":[{"name":"Artist"}]}}"""
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test fun `reads current and legacy items and skips unplayable entries`() {
        val items = Json.parseToJsonElement("""[$song,${song.replace("item", "track")},
            {"item":null},{"item":{"type":"episode"}},
            {"is_local":true,"item":{"type":"track","name":"Local","artists":[{"name":"A"}]}},
            {"item":{"type":"track","is_playable":false,"name":"Missing","artists":[{"name":"A"}]}}]""").jsonArray
        assertEquals(listOf(SpotifySong("Song", "Artist"), SpotifySong("Song", "Artist")), SpotifyClient.parseSongs(items))
    }

    @Test fun `paginates preserves order and caches access token`() = runBlocking {
        var tokens = 0
        val offsets = mutableListOf<String?>()
        val http = HttpClient(MockEngine { request ->
            if (request.url.host == "accounts.spotify.com") {
                tokens++
                respond("""{"access_token":"access","expires_in":3600}""", headers = jsonHeaders)
            } else {
                assertEquals("api.spotify.com", request.url.host)
                assertEquals("Bearer access", request.headers[HttpHeaders.Authorization])
                val offset = request.url.parameters["offset"]
                offsets += offset
                respond("""{"items":[$song],"next":${if (offset == "0") "\"https://untrusted.invalid/ignored\"" else "null"}}""", headers = jsonHeaders)
            }
        })
        try {
            val spotify = SpotifyClient(http, "id", "secret", "refresh")
            assertEquals(2, spotify.playlist(url).size)
            assertEquals(2, spotify.playlist(url).size)
            assertEquals(listOf("0", "1", "0", "1"), offsets)
            assertEquals(1, tokens)
        } finally { http.close() }
    }

    @Test fun `refreshes once on 401 and explains forbidden playlists`() = runBlocking {
        var tokens = 0
        var pages = 0
        val http = HttpClient(MockEngine { request ->
            if (request.url.host == "accounts.spotify.com") {
                tokens++
                respond("""{"access_token":"access$tokens","expires_in":3600}""", headers = jsonHeaders)
            } else {
                pages++
                respond("{}", if (pages == 1) HttpStatusCode.Unauthorized else HttpStatusCode.Forbidden, jsonHeaders)
            }
        })
        try {
            val spotify = SpotifyClient(http, "id", "secret", "refresh")
            val error = assertFailsWith<IllegalArgumentException> { spotify.playlist(url) }
            assertTrue(error.message!!.contains("own or collaborate"))
            assertEquals(2, tokens)
            assertEquals(2, pages)
        } finally { http.close() }
    }
}
