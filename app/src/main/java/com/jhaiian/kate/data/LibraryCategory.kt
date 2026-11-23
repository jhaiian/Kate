package com.jhaiian.kate.data

enum class LibraryCategory(val key: String) {
    SONGS("songs"),
    ALBUMS("albums"),
    ARTISTS("artists"),
    ALBUM_ARTISTS("album_artists"),
    GENRES("genres"),
    FOLDERS("folders"),
    YEARS("years"),
    RECENTLY_ADDED("recently_added"),
    FAVORITES("favorites"),
    PLAYLISTS("playlists");

    companion object {
        val Browse = listOf(SONGS, ALBUMS, ARTISTS, ALBUM_ARTISTS, GENRES, FOLDERS, YEARS)
        val Collections = listOf(RECENTLY_ADDED, FAVORITES, PLAYLISTS)

        fun fromKey(key: String?): LibraryCategory? = entries.firstOrNull { it.key == key }
    }
}
