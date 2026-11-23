package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jhaiian.kate.playback.PlayerController

object LyricsOffsetStore {
    private const val PREFS_NAME = "lyrics_offsets"
    const val MAX_OFFSET_MS = 10_000L

    private var loaded = false
    var offsets by mutableStateOf<Map<String, Long>>(emptyMap())
        private set

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        offsets = prefs(context).all.mapNotNull { (key, value) -> (value as? Long)?.let { key to it } }.toMap()
    }

    fun offsetFor(trackId: String?): Long = trackId?.let { offsets[it] } ?: 0L

    fun currentPositionMs(): Long = PlayerController.estimatedPositionMs() + offsetFor(PlayerController.currentTrack?.id)

    fun set(context: Context, trackId: String, offsetMs: Long) {
        ensureLoaded(context)
        val value = offsetMs.coerceIn(-MAX_OFFSET_MS, MAX_OFFSET_MS)
        offsets = if (value == 0L) offsets - trackId else offsets + (trackId to value)
        val editor = prefs(context).edit()
        if (value == 0L) editor.remove(trackId) else editor.putLong(trackId, value)
        editor.apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
