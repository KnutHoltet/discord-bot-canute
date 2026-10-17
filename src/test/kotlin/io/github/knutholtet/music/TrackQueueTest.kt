package io.github.knutholtet.music

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TrackQueueTest {
    @Test fun `appending a playlist preserves the current song and order including duplicates`() {
        val queue = TrackQueue<String>()
        queue.add(listOf("a", "b"))
        assertEquals("a", queue.next())
        queue.add(listOf("b", "c"))
        assertEquals("a", queue.current)
        assertEquals(4, queue.size)
        assertEquals(listOf("b", "b", "c"), List(3) { queue.next() })
        assertNull(queue.next())
        assertEquals(0, queue.size)
    }

    @Test fun `stop clears current and pending tracks and allows a new playlist`() {
        val queue = TrackQueue<String>()
        queue.add(listOf("a", "b"))
        queue.next()
        queue.clear()
        assertNull(queue.current)
        assertNull(queue.next())
        queue.add(listOf("c"))
        assertEquals("c", queue.next())
    }

    @Test fun `overflow rejects whole playlist without changing queue`() {
        val queue = TrackQueue<String>(2)
        queue.add(listOf("a"))
        queue.next()
        assertFailsWith<IllegalArgumentException> { queue.add(listOf("b", "c")) }
        assertEquals(1, queue.size)
        assertEquals("a", queue.current)
        assertNull(queue.next())
    }

    @Test fun `queues are isolated between servers`() {
        val first = TrackQueue<String>()
        val second = TrackQueue<String>()
        first.add(listOf("a", "b"))
        second.add(listOf("c"))
        first.clear()
        assertEquals("c", second.next())
    }
}
