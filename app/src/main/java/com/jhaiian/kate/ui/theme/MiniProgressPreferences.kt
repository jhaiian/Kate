package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.preference.PreferenceManager

object MiniProgressPreferences {
    private const val KEY_STYLE = "mini_progress_style"
    private const val KEY_MATCH_PLAYER = "mini_progress_match_player"
    private const val KEY_THICKNESS = "mini_progress_thickness"
    private const val KEY_COLOR = "mini_progress_color"
    private const val KEY_TIME_LEFT = "mini_progress_time_left"

    const val STYLE_BOTTOM = "bottom"
    const val STYLE_TOP = "top"
    const val STYLE_PLAY_RING = "play_ring"
    const val STYLE_ART_RING = "art_ring"
    const val STYLE_FILL = "fill"
    const val STYLE_HIDDEN = "hidden"
    const val THICKNESS_HAIRLINE = "hairline"
    const val THICKNESS_THIN = "thin"
    const val THICKNESS_THICK = "thick"

    const val DEFAULT_STYLE = STYLE_FILL
    const val DEFAULT_MATCH_PLAYER = false
    const val DEFAULT_THICKNESS = THICKNESS_THIN
    const val DEFAULT_COLOR = SeekBarPreferences.COLOR_GRADIENT
    const val DEFAULT_TIME_LEFT = false

    fun getStyle(context: Context): String =
        getChoice(context, KEY_STYLE, DEFAULT_STYLE, MiniProgressStyleOptions.map { it.key })

    fun getMatchPlayer(context: Context): Boolean = getFlag(context, KEY_MATCH_PLAYER, DEFAULT_MATCH_PLAYER)

    fun getThickness(context: Context): String =
        getChoice(context, KEY_THICKNESS, DEFAULT_THICKNESS, MiniProgressThicknessOptions.map { it.key })

    fun getColor(context: Context): String =
        getChoice(context, KEY_COLOR, DEFAULT_COLOR, SeekColorOptions.map { it.key })

    fun getTimeLeft(context: Context): Boolean = getFlag(context, KEY_TIME_LEFT, DEFAULT_TIME_LEFT)

    fun save(
        context: Context,
        style: String,
        matchPlayer: Boolean,
        thickness: String,
        color: String,
        timeLeft: Boolean
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_STYLE, style)
            .putBoolean(KEY_MATCH_PLAYER, matchPlayer)
            .putString(KEY_THICKNESS, thickness)
            .putString(KEY_COLOR, color)
            .putBoolean(KEY_TIME_LEFT, timeLeft)
            .apply()
    }

    private fun getChoice(context: Context, key: String, default: String, allowed: List<String>): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, default) ?: default
        return if (value in allowed) value else default
    }

    private fun getFlag(context: Context, key: String, default: Boolean): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, default)
}
