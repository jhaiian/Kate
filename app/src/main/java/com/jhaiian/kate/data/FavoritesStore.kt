package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object FavoritesStore {
    private const val PREFS_NAME = "favorites"
    private const val KEY_TRACK_IDS = "track_ids"

    var ids by mutableStateOf<Set<String>>(emptySet())
        private set
    private var loaded = false

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        ids = prefs(context).getStringSet(KEY_TRACK_IDS, null)?.toSet() ?: emptySet()
    }

    fun isFavorite(trackId: String): Boolean = trackId in ids

    fun toggle(context: Context, trackId: String) {
        ensureLoaded(context)
        ids = if (trackId in ids) ids - trackId else ids + trackId
        prefs(context).edit().putStringSet(KEY_TRACK_IDS, HashSet(ids)).apply()
    }

    fun favoriteTracks(tracks: List<Track>): List<Track> {
        val current = ids
        return tracks.filter { it.id in current }
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
