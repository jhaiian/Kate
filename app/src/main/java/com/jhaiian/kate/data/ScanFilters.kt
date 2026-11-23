package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ScanFilters {
    private const val PREFS_NAME = "scan_filters"
    private const val KEY_MIN_SECONDS = "min_seconds"
    private const val KEY_EXCLUDED = "excluded"
    private const val KEY_PREFER_FOLDER_ART = "prefer_folder_art"
    const val MAX_MIN_SECONDS = 120

    private var loaded = false
    var minSeconds by mutableIntStateOf(0)
        private set
    var excluded by mutableStateOf<Set<String>>(emptySet())
        private set
    @Volatile
    var preferFolderArt = false
        private set
    var preferFolderArtState by mutableStateOf(false)
        private set

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = prefs(context)
        minSeconds = prefs.getInt(KEY_MIN_SECONDS, 0).coerceIn(0, MAX_MIN_SECONDS)
        excluded = prefs.getStringSet(KEY_EXCLUDED, null)?.toSet().orEmpty()
        preferFolderArt = prefs.getBoolean(KEY_PREFER_FOLDER_ART, false)
        preferFolderArtState = preferFolderArt
    }

    fun accepts(track: Track): Boolean {
        val min = minSeconds
        if (min > 0 && track.durationMs > 0 && track.durationMs < min * 1000L) return false
        val path = track.path
        return excluded.none { prefix -> path == prefix || path.startsWith(prefix.trimEnd('/') + "/") }
    }

    fun apply(tracks: List<Track>): List<Track> =
        if (minSeconds == 0 && excluded.isEmpty()) tracks else tracks.filter(::accepts)

    fun setMinSeconds(context: Context, value: Int) {
        ensureLoaded(context)
        minSeconds = value.coerceIn(0, MAX_MIN_SECONDS)
        prefs(context).edit().putInt(KEY_MIN_SECONDS, minSeconds).apply()
    }

    fun addExcluded(context: Context, documentId: String) {
        ensureLoaded(context)
        excluded = excluded + documentId
        prefs(context).edit().putStringSet(KEY_EXCLUDED, HashSet(excluded)).apply()
    }

    fun removeExcluded(context: Context, documentId: String) {
        ensureLoaded(context)
        excluded = excluded - documentId
        prefs(context).edit().putStringSet(KEY_EXCLUDED, HashSet(excluded)).apply()
    }

    fun setPreferFolderArt(context: Context, value: Boolean) {
        ensureLoaded(context)
        preferFolderArt = value
        preferFolderArtState = value
        prefs(context).edit().putBoolean(KEY_PREFER_FOLDER_ART, value).apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
