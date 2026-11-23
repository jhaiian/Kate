package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.preference.PreferenceManager

object VisualizerPreferences {
    private const val KEY_STYLE = "visualizer_style"
    private const val KEY_PLACEMENTS = "visualizer_placements"
    private const val KEY_BARS = "visualizer_bars"
    private const val KEY_SENSITIVITY = "visualizer_sensitivity"
    private const val KEY_SMOOTHING = "visualizer_smoothing"
    private const val KEY_COLOR = "visualizer_color"
    private const val KEY_CUSTOM_COLOR = "visualizer_custom_color"
    private const val KEY_FPS = "visualizer_fps"

    const val STYLE_OFF = "off"
    const val STYLE_BARS = "bars"
    const val STYLE_MIRROR = "mirror"
    const val STYLE_LINE = "line"
    const val STYLE_WAVE = "wave"
    const val STYLE_RING = "ring"
    const val STYLE_PULSE = "pulse"
    const val PLACE_BACKGROUND = "background"
    const val PLACE_STRIP = "strip"
    const val PLACE_MINI = "mini"
    const val PLACE_LYRICS = "lyrics"
    const val COLOR_ACCENT = "accent"
    const val COLOR_GRADIENT = "gradient"
    const val COLOR_CUSTOM = "custom"
    const val MIN_BARS = 12
    const val MAX_BARS = 96
    const val MIN_SENSITIVITY = 40
    const val MAX_SENSITIVITY = 250
    const val MAX_SMOOTHING = 90

    val Styles = listOf(STYLE_OFF, STYLE_BARS, STYLE_MIRROR, STYLE_LINE, STYLE_WAVE, STYLE_RING, STYLE_PULSE)
    val Placements = listOf(PLACE_BACKGROUND, PLACE_STRIP, PLACE_MINI, PLACE_LYRICS)
    val ColorSources = listOf(COLOR_ACCENT, COLOR_GRADIENT, COLOR_CUSTOM)
    val FrameRates = listOf(30, 60, 0)

    const val DEFAULT_STYLE = STYLE_OFF
    val DEFAULT_PLACEMENTS = setOf(PLACE_BACKGROUND)
    const val DEFAULT_BARS = 24
    const val DEFAULT_SENSITIVITY = 40
    const val DEFAULT_SMOOTHING = 90
    const val DEFAULT_COLOR = COLOR_ACCENT
    const val DEFAULT_FPS = 60

    private fun prefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    fun getStyle(context: Context): String = prefs(context).getString(KEY_STYLE, DEFAULT_STYLE)?.takeIf { it in Styles } ?: DEFAULT_STYLE

    fun getPlacements(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_PLACEMENTS, null)?.filter { it in Placements }?.toSet() ?: DEFAULT_PLACEMENTS

    fun getBars(context: Context): Int = prefs(context).getInt(KEY_BARS, DEFAULT_BARS).coerceIn(MIN_BARS, MAX_BARS)

    fun getSensitivity(context: Context): Int = prefs(context).getInt(KEY_SENSITIVITY, DEFAULT_SENSITIVITY).coerceIn(MIN_SENSITIVITY, MAX_SENSITIVITY)

    fun getSmoothing(context: Context): Int = prefs(context).getInt(KEY_SMOOTHING, DEFAULT_SMOOTHING).coerceIn(0, MAX_SMOOTHING)

    fun getColor(context: Context): String = prefs(context).getString(KEY_COLOR, DEFAULT_COLOR)?.takeIf { it in ColorSources } ?: DEFAULT_COLOR

    fun getCustomColor(context: Context): Int = prefs(context).getInt(KEY_CUSTOM_COLOR, ThemePreferences.DEFAULT_CUSTOM_ACCENT)

    fun getFps(context: Context): Int = prefs(context).getInt(KEY_FPS, DEFAULT_FPS).takeIf { it in FrameRates } ?: DEFAULT_FPS

    fun save(
        context: Context,
        style: String,
        placements: Set<String>,
        bars: Int,
        sensitivity: Int,
        smoothing: Int,
        color: String,
        customColor: Int,
        fps: Int
    ) {
        prefs(context).edit()
            .putString(KEY_STYLE, style)
            .putStringSet(KEY_PLACEMENTS, HashSet(placements))
            .putInt(KEY_BARS, bars)
            .putInt(KEY_SENSITIVITY, sensitivity)
            .putInt(KEY_SMOOTHING, smoothing)
            .putString(KEY_COLOR, color)
            .putInt(KEY_CUSTOM_COLOR, customColor)
            .putInt(KEY_FPS, fps)
            .apply()
    }
}
