package com.jhaiian.kate.data

import android.content.Context

object TrackPositionStore {
    private const val PREFS_NAME = "track_positions"
    private const val MAX_ENTRIES = 200
    private const val SEPARATOR = ','

    fun get(context: Context, trackId: String): Long? {
        val raw = prefs(context).getString(trackId, null) ?: return null
        return raw.substringBefore(SEPARATOR).toLongOrNull()?.takeIf { it > 0L }
    }

    fun save(context: Context, trackId: String, positionMs: Long) {
        val prefs = prefs(context)
        val editor = prefs.edit().putString(trackId, "$positionMs$SEPARATOR${System.currentTimeMillis()}")
        val all = prefs.all
        if (all.size >= MAX_ENTRIES && !all.containsKey(trackId)) {
            all.entries
                .sortedBy { (it.value as? String)?.substringAfter(SEPARATOR)?.toLongOrNull() ?: 0L }
                .take(all.size - MAX_ENTRIES + 1)
                .forEach { editor.remove(it.key) }
        }
        editor.apply()
    }

    fun remove(context: Context, trackId: String) {
        val prefs = prefs(context)
        if (prefs.contains(trackId)) prefs.edit().remove(trackId).apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
