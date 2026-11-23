package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SearchHistory {
    private const val PREFS_NAME = "search_history"
    private const val KEY_ENTRIES = "entries"
    private const val SEPARATOR = "\n"
    private const val MAX_ENTRIES = 12
    private const val MIN_LENGTH = 2
    private val Whitespace = Regex("\\s+")

    var entries by mutableStateOf<List<String>>(emptyList())
        private set

    private var loaded = false

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        entries = prefs(context).getString(KEY_ENTRIES, null)
            ?.split(SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.take(MAX_ENTRIES)
            .orEmpty()
    }

    fun record(context: Context, query: String) {
        ensureLoaded(context)
        val cleaned = query.trim().replace(Whitespace, " ")
        if (cleaned.length < MIN_LENGTH) return
        val updated = (listOf(cleaned) + entries.filterNot { it.equals(cleaned, ignoreCase = true) }).take(MAX_ENTRIES)
        if (updated == entries) return
        entries = updated
        persist(context)
    }

    fun remove(context: Context, query: String) {
        ensureLoaded(context)
        val updated = entries.filterNot { it == query }
        if (updated.size == entries.size) return
        entries = updated
        persist(context)
    }

    fun clear(context: Context) {
        entries = emptyList()
        persist(context)
    }

    private fun persist(context: Context) {
        prefs(context).edit().putString(KEY_ENTRIES, entries.joinToString(SEPARATOR)).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
