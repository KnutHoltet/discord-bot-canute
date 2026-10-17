package io.github.knutholtet.music

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpotifyPlaylistTest {
    private val id = "37i9dQZF1DXcBWIGoYBM5M"
    private val canonical = "https://open.spotify.com/playlist/$id"

    @Test fun `normalizes share links locale links and Spotify URIs`() {
        listOf(canonical, "$canonical?si=abc", "$canonical/", "<$canonical>",
            "https://open.spotify.com/intl-no/playlist/$id?si=abc", "spotify:playlist:$id")
            .forEach { assertEquals(canonical, SpotifyPlaylist.normalize(it)) }
    }

    @Test fun `rejects arbitrary URLs credentials tracks malformed ids and short links`() {
        listOf("", "http://open.spotify.com/playlist/$id", "https://open.spotify.com.evil.test/playlist/$id",
            "https://open.spotify.com@evil.test/playlist/$id", "https://user@open.spotify.com/playlist/$id",
            "https://open.spotify.com:443/playlist/$id", "https://open.spotify.com/track/$id",
            "https://spotify.link/abc", "file:///etc/passwd", "spotify:playlist:abc",
            "https://open.spotify.com/playlist/abc", "$canonical/extra")
            .forEach { assertNull(SpotifyPlaylist.normalize(it), it) }
    }
}
