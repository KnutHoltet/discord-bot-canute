package io.github.knutholtet.music

/** Access under the guild session's mutex, including player callbacks. */
internal class TrackQueue<T>(private val limit: Int = 1000) {
    private val pending = ArrayDeque<T>()
    var current: T? = null
        private set
    val size: Int get() = pending.size + if (current == null) 0 else 1

    fun add(tracks: List<T>) {
        require(size + tracks.size <= limit) { "The queue limit is $limit tracks. Use /stop before adding this playlist." }
        pending.addAll(tracks)
    }

    fun next(): T? {
        current = pending.removeFirstOrNull()
        return current
    }

    fun clear() {
        pending.clear()
        current = null
    }
}
