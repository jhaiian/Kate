package com.jhaiian.kate.data

import com.jhaiian.kate.playback.PlayerController

enum class LibrarySortOrder(val key: String) {
    TITLE("title"),
    TRACK_NUMBER("track_number"),
    DISC_TRACK("disc_track"),
    FILENAME("filename"),
    PATH("path"),
    ARTIST("artist"),
    ALBUM("album"),
    YEAR("year"),
    YEAR_ALBUM("year_album"),
    DATE_ADDED("date_added"),
    QUEUE("queue");

    companion object {
        val Default = TITLE

        fun fromKey(key: String?): LibrarySortOrder = entries.firstOrNull { it.key == key } ?: Default
    }
}

enum class SortDirection(val key: String) {
    ASCENDING("ascending"),
    DESCENDING("descending");

    companion object {
        val Default = ASCENDING

        fun fromKey(key: String?): SortDirection = entries.firstOrNull { it.key == key } ?: Default
    }
}

private fun compareText(a: String, b: String): Int = a.lowercase().compareTo(b.lowercase())

private fun compareKnownText(a: String, b: String): Int = when {
    a.isBlank() && b.isBlank() -> 0
    a.isBlank() -> 1
    b.isBlank() -> -1
    else -> compareText(a, b)
}

private fun compareNullableInt(a: Int?, b: Int?): Int = when {
    a == null && b == null -> 0
    a == null -> 1
    b == null -> -1
    else -> a.compareTo(b)
}

fun sortTracks(
    tracks: List<Track>,
    order: LibrarySortOrder,
    direction: SortDirection = SortDirection.Default
): List<Track> {
    val albumYears: Map<String, Int?> = if (order == LibrarySortOrder.YEAR_ALBUM) {
        tracks.groupBy { it.album.lowercase() }
            .mapValues { (_, albumTracks) -> albumTracks.mapNotNull { it.year }.minOrNull() }
    } else {
        emptyMap()
    }
    // Tracks not present in the current playback queue (e.g. nothing is playing yet, or a
    // track was added after the queue snapshot was taken) sort to the end, after everything
    // that is actually queued.
    val queuePositions: Map<String, Int> = if (order == LibrarySortOrder.QUEUE) {
        PlayerController.queue.withIndex().associate { (index, track) -> track.id to index }
    } else {
        emptyMap()
    }
    val comparator = Comparator<Track> { a, b ->
        var r = 0
        when (order) {
            LibrarySortOrder.TITLE -> {
                r = compareText(a.title, b.title)
            }
            LibrarySortOrder.TRACK_NUMBER -> {
                r = compareNullableInt(a.trackNumber, b.trackNumber)
                if (r == 0) r = compareText(a.title, b.title)
            }
            LibrarySortOrder.DISC_TRACK -> {
                r = compareNullableInt(a.discNumber, b.discNumber)
                if (r == 0) r = compareNullableInt(a.trackNumber, b.trackNumber)
                if (r == 0) r = compareText(a.title, b.title)
            }
            LibrarySortOrder.FILENAME -> {
                r = compareText(a.filename, b.filename)
            }
            LibrarySortOrder.PATH -> {
                r = compareText(a.path, b.path)
            }
            LibrarySortOrder.ARTIST -> {
                r = compareKnownText(a.artist, b.artist)
                if (r == 0) r = compareKnownText(a.album, b.album)
                if (r == 0) r = compareNullableInt(a.discNumber, b.discNumber)
                if (r == 0) r = compareNullableInt(a.trackNumber, b.trackNumber)
            }
            LibrarySortOrder.ALBUM -> {
                r = compareKnownText(a.album, b.album)
                if (r == 0) r = compareNullableInt(a.discNumber, b.discNumber)
                if (r == 0) r = compareNullableInt(a.trackNumber, b.trackNumber)
            }
            LibrarySortOrder.YEAR -> {
                r = compareNullableInt(a.year, b.year)
                if (r == 0) r = compareText(a.title, b.title)
            }
            LibrarySortOrder.YEAR_ALBUM -> {
                r = compareNullableInt(albumYears[a.album.lowercase()], albumYears[b.album.lowercase()])
                if (r == 0) r = compareKnownText(a.album, b.album)
                if (r == 0) r = compareNullableInt(a.discNumber, b.discNumber)
                if (r == 0) r = compareNullableInt(a.trackNumber, b.trackNumber)
            }
            LibrarySortOrder.DATE_ADDED -> {
                r = b.dateAddedMs.compareTo(a.dateAddedMs)
            }
            LibrarySortOrder.QUEUE -> {
                r = compareNullableInt(queuePositions[a.id], queuePositions[b.id])
                if (r == 0) r = compareText(a.title, b.title)
            }
        }
        if (r == 0) r = a.id.compareTo(b.id)
        r
    }
    val directedComparator = if (direction == SortDirection.DESCENDING) comparator.reversed() else comparator
    return tracks.sortedWith(directedComparator)
}
