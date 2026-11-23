package com.jhaiian.kate.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

object PlaybackStateStore {
    private const val PREFS_NAME = "playback_state"
    private const val KEY_CURRENT_ID = "current_id"
    private const val KEY_INDEX = "index"
    private const val KEY_POSITION = "position_ms"
    private const val KEY_SHUFFLE = "shuffle"
    private const val KEY_REPEAT = "repeat_mode"
    private const val KEY_PLAYER_EXPANDED = "player_expanded"
    private const val QUEUE_FILE = "playback_queue.txt"
    private const val SEPARATOR = "\n"

    data class Saved(
        val trackIds: List<String>,
        val currentId: String?,
        val index: Int,
        val positionMs: Long,
        val shuffle: Boolean,
        val repeatMode: Int
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    fun saveQueue(context: Context, trackIds: List<String>) {
        val appContext = context.applicationContext
        val content = trackIds.joinToString(SEPARATOR)
        scope.launch {
            runCatching {
                val target = File(appContext.filesDir, QUEUE_FILE)
                val temp = File(appContext.filesDir, "$QUEUE_FILE.tmp")
                temp.writeText(content)
                if (!temp.renameTo(target)) {
                    target.writeText(content)
                    temp.delete()
                }
            }
        }
    }

    fun savePosition(context: Context, currentId: String?, index: Int, positionMs: Long) {
        prefs(context).edit()
            .putString(KEY_CURRENT_ID, currentId)
            .putInt(KEY_INDEX, index)
            .putLong(KEY_POSITION, positionMs.coerceAtLeast(0L))
            .apply()
    }

    fun saveModes(context: Context, shuffle: Boolean, repeatMode: Int) {
        prefs(context).edit()
            .putBoolean(KEY_SHUFFLE, shuffle)
            .putInt(KEY_REPEAT, repeatMode)
            .apply()
    }

    fun isPlayerExpanded(context: Context): Boolean = prefs(context).getBoolean(KEY_PLAYER_EXPANDED, false)

    fun setPlayerExpanded(context: Context, expanded: Boolean) {
        prefs(context).edit().putBoolean(KEY_PLAYER_EXPANDED, expanded).apply()
    }

    fun load(context: Context): Saved {
        val prefs = prefs(context)
        val ids = runCatching { File(context.applicationContext.filesDir, QUEUE_FILE).readText() }
            .getOrNull()
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            .orEmpty()
        return Saved(
            trackIds = ids,
            currentId = prefs.getString(KEY_CURRENT_ID, null),
            index = prefs.getInt(KEY_INDEX, 0),
            positionMs = prefs.getLong(KEY_POSITION, 0L),
            shuffle = prefs.getBoolean(KEY_SHUFFLE, false),
            repeatMode = prefs.getInt(KEY_REPEAT, 0).coerceIn(0, 2)
        )
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
