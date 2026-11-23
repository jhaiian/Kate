package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.preference.PreferenceManager

object SeekBarPreferences {
    private const val KEY_STYLE = "seek_style"
    private const val KEY_THUMB = "seek_thumb"
    private const val KEY_THICKNESS = "seek_thickness"
    private const val KEY_COLOR = "seek_color"
    private const val KEY_BUBBLE = "seek_scrub_bubble"
    private const val KEY_LYRIC_MARKERS = "seek_lyric_markers"
    private const val KEY_WAVE_SHAPE = "seek_wave_shape"
    private const val KEY_WAVE_DENSITY = "seek_wave_density"
    private const val KEY_SQUIGGLE_HEIGHT = "seek_squiggle_height"
    private const val KEY_SQUIGGLE_LENGTH = "seek_squiggle_length"
    private const val KEY_SQUIGGLE_SPEED = "seek_squiggle_speed"
    private const val KEY_SQUIGGLE_REACTIVE = "seek_squiggle_reactive"
    private const val KEY_SHOW_REMAINING = "seek_show_remaining"

    const val STYLE_CLASSIC = "classic"
    const val STYLE_WAVEFORM = "waveform"
    const val STYLE_SQUIGGLE = "squiggle"
    const val STYLE_PILL = "pill"
    const val STYLE_DOTS = "dots"
    const val THUMB_CIRCLE = "circle"
    const val THUMB_LINE = "line"
    const val THUMB_HIDDEN = "hidden"
    const val SIZE_SMALL = "small"
    const val SIZE_MEDIUM = "medium"
    const val SIZE_LARGE = "large"
    const val COLOR_ACCENT = "accent"
    const val COLOR_GRADIENT = "gradient"
    const val WAVE_MIRRORED = "mirrored"
    const val WAVE_BOTTOM = "bottom"
    const val DENSITY_FINE = "fine"
    const val DENSITY_CHUNKY = "chunky"

    const val DEFAULT_STYLE = STYLE_SQUIGGLE
    const val DEFAULT_THUMB = THUMB_LINE
    const val DEFAULT_THICKNESS = SIZE_MEDIUM
    const val DEFAULT_COLOR = COLOR_GRADIENT
    const val DEFAULT_BUBBLE = true
    const val DEFAULT_LYRIC_MARKERS = false
    const val DEFAULT_WAVE_SHAPE = WAVE_MIRRORED
    const val DEFAULT_WAVE_DENSITY = DENSITY_FINE
    const val DEFAULT_SQUIGGLE_HEIGHT = SIZE_MEDIUM
    const val DEFAULT_SQUIGGLE_LENGTH = SIZE_MEDIUM
    const val DEFAULT_SQUIGGLE_SPEED = SIZE_MEDIUM
    const val DEFAULT_SQUIGGLE_REACTIVE = false

    private val Sizes = listOf(SIZE_SMALL, SIZE_MEDIUM, SIZE_LARGE)

    var showRemaining by mutableStateOf(false)
        private set
    private var remainingLoaded = false

    fun getStyle(context: Context): String =
        getChoice(context, KEY_STYLE, DEFAULT_STYLE, SeekStyleOptions.map { it.key })

    fun getThumb(context: Context): String =
        getChoice(context, KEY_THUMB, DEFAULT_THUMB, SeekThumbOptions.map { it.key })

    fun getThickness(context: Context): String = getChoice(context, KEY_THICKNESS, DEFAULT_THICKNESS, Sizes)

    fun getColor(context: Context): String =
        getChoice(context, KEY_COLOR, DEFAULT_COLOR, SeekColorOptions.map { it.key })

    fun getBubble(context: Context): Boolean = getFlag(context, KEY_BUBBLE, DEFAULT_BUBBLE)

    fun getLyricMarkers(context: Context): Boolean = getFlag(context, KEY_LYRIC_MARKERS, DEFAULT_LYRIC_MARKERS)

    fun getWaveShape(context: Context): String =
        getChoice(context, KEY_WAVE_SHAPE, DEFAULT_WAVE_SHAPE, SeekWaveShapeOptions.map { it.key })

    fun getWaveDensity(context: Context): String =
        getChoice(context, KEY_WAVE_DENSITY, DEFAULT_WAVE_DENSITY, SeekWaveDensityOptions.map { it.key })

    fun getSquiggleHeight(context: Context): String = getChoice(context, KEY_SQUIGGLE_HEIGHT, DEFAULT_SQUIGGLE_HEIGHT, Sizes)

    fun getSquiggleLength(context: Context): String = getChoice(context, KEY_SQUIGGLE_LENGTH, DEFAULT_SQUIGGLE_LENGTH, Sizes)

    fun getSquiggleSpeed(context: Context): String = getChoice(context, KEY_SQUIGGLE_SPEED, DEFAULT_SQUIGGLE_SPEED, Sizes)

    fun getSquiggleReactive(context: Context): Boolean = getFlag(context, KEY_SQUIGGLE_REACTIVE, DEFAULT_SQUIGGLE_REACTIVE)

    fun ensureRemainingLoaded(context: Context) {
        if (remainingLoaded) return
        remainingLoaded = true
        showRemaining = getFlag(context, KEY_SHOW_REMAINING, false)
    }

    fun toggleRemaining(context: Context) {
        ensureRemainingLoaded(context)
        showRemaining = !showRemaining
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean(KEY_SHOW_REMAINING, showRemaining)
            .apply()
    }

    fun save(
        context: Context,
        style: String,
        thumb: String,
        thickness: String,
        color: String,
        bubble: Boolean,
        lyricMarkers: Boolean,
        waveShape: String,
        waveDensity: String,
        squiggleHeight: String,
        squiggleLength: String,
        squiggleSpeed: String,
        squiggleReactive: Boolean
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_STYLE, style)
            .putString(KEY_THUMB, thumb)
            .putString(KEY_THICKNESS, thickness)
            .putString(KEY_COLOR, color)
            .putBoolean(KEY_BUBBLE, bubble)
            .putBoolean(KEY_LYRIC_MARKERS, lyricMarkers)
            .putString(KEY_WAVE_SHAPE, waveShape)
            .putString(KEY_WAVE_DENSITY, waveDensity)
            .putString(KEY_SQUIGGLE_HEIGHT, squiggleHeight)
            .putString(KEY_SQUIGGLE_LENGTH, squiggleLength)
            .putString(KEY_SQUIGGLE_SPEED, squiggleSpeed)
            .putBoolean(KEY_SQUIGGLE_REACTIVE, squiggleReactive)
            .apply()
    }

    private fun getChoice(context: Context, key: String, default: String, allowed: List<String>): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, default) ?: default
        return if (value in allowed) value else default
    }

    private fun getFlag(context: Context, key: String, default: Boolean): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, default)
}
