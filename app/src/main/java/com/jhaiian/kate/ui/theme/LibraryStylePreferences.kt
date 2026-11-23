package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.compose.ui.unit.Dp
import androidx.preference.PreferenceManager

object LibraryStylePreferences {
    private const val KEY_CORNERS = "library_corners"
    private const val KEY_SHOW_BADGES = "library_show_badges"
    private const val KEY_SHOW_DURATION = "library_show_duration"
    private const val KEY_SHOW_SCROLLBAR = "show_scrollbar"
    private const val KEY_ALPHABET_INDEX = "library_alphabet_index"

    const val CORNERS_SHARP = "sharp"
    const val CORNERS_ROUNDED = "rounded"
    const val CORNERS_ROUND = "round"

    const val DEFAULT_CORNERS = CORNERS_ROUNDED
    const val DEFAULT_SHOW_BADGES = true
    const val DEFAULT_SHOW_DURATION = true
    const val DEFAULT_SHOW_SCROLLBAR = true
    const val DEFAULT_ALPHABET_INDEX = true

    const val MAX_CORNER_SCALE = 2.4f

    fun cornerScale(corners: String): Float = FineTune.value(corners) ?: when (corners) {
        CORNERS_SHARP -> 0.25f
        CORNERS_ROUND -> 1.7f
        else -> 1f
    }

    fun scaledCorner(base: Dp, corners: String): Dp = base * cornerScale(corners)

    fun getCorners(context: Context): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_CORNERS, DEFAULT_CORNERS) ?: DEFAULT_CORNERS
        val custom = FineTune.value(value)
        if (custom != null) return FineTune.key(custom.coerceIn(0f, MAX_CORNER_SCALE))
        return if (CornerStyleOptions.any { it.key == value }) value else DEFAULT_CORNERS
    }

    fun getShowBadges(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_SHOW_BADGES, DEFAULT_SHOW_BADGES)

    fun getShowDuration(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_SHOW_DURATION, DEFAULT_SHOW_DURATION)

    fun getShowScrollbar(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_SHOW_SCROLLBAR, DEFAULT_SHOW_SCROLLBAR)

    fun getAlphabetIndex(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ALPHABET_INDEX, DEFAULT_ALPHABET_INDEX)

    fun save(
        context: Context,
        corners: String,
        showBadges: Boolean,
        showDuration: Boolean,
        showScrollbar: Boolean,
        alphabetIndex: Boolean
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_CORNERS, corners)
            .putBoolean(KEY_SHOW_BADGES, showBadges)
            .putBoolean(KEY_SHOW_DURATION, showDuration)
            .putBoolean(KEY_SHOW_SCROLLBAR, showScrollbar)
            .putBoolean(KEY_ALPHABET_INDEX, alphabetIndex)
            .apply()
    }
}
