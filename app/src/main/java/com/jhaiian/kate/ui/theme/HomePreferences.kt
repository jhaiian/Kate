package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.preference.PreferenceManager

object HomePreferences {
    private const val KEY_HIDDEN_SECTIONS = "home_hidden_sections"

    const val SECTION_CONTINUE = "continue"
    const val SECTION_RECENTLY_PLAYED = "recently_played"
    const val SECTION_MOST_PLAYED = "most_played"
    const val SECTION_RECENTLY_ADDED = "recently_added"
    const val SECTION_PLAYLISTS = "playlists"

    val DEFAULT_HIDDEN_SECTIONS = emptySet<String>()

    fun getHiddenSections(context: Context): Set<String> {
        val stored = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(KEY_HIDDEN_SECTIONS, null)
            ?: return DEFAULT_HIDDEN_SECTIONS
        val known = HomeSectionOptions.map { it.key }.toSet()
        return stored.filter { it in known }.toSet()
    }

    fun setHiddenSections(context: Context, value: Set<String>) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(KEY_HIDDEN_SECTIONS, HashSet(value)).apply()
    }
}
