package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.preference.PreferenceManager

object LyricsPreferences {
    private const val KEY_TEXT_SIZE = "lyrics_text_size"
    private const val KEY_ALIGNMENT = "lyrics_alignment"
    private const val KEY_SOFTEN_LINES = "lyrics_soften_lines"
    private const val KEY_IMMERSIVE = "lyrics_immersive"
    private const val KEY_KEEP_SCREEN_ON = "lyrics_keep_screen_on"
    private const val KEY_WORD_BY_WORD = "lyrics_word_by_word"
    private const val KEY_LINE_SWEEP = "lyrics_line_sweep"
    private const val KEY_WORD_STYLE = "lyrics_word_style"
    private const val KEY_BACKGROUND = "lyrics_background"
    private const val KEY_FONT = "lyrics_font"
    private const val KEY_ACTIVE_COLOR = "lyrics_active_color"
    private const val KEY_INACTIVE_COLOR = "lyrics_inactive_color"
    private const val KEY_LINE_SPACING = "lyrics_line_spacing"
    const val MIN_LINE_SPACING = 80
    const val MAX_LINE_SPACING = 180
    const val MIN_TEXT_SCALE = 0.6f
    const val MAX_TEXT_SCALE = 1.6f
    const val FONT_APP = "app"

    const val TEXT_SIZE_SMALL = "small"
    const val TEXT_SIZE_MEDIUM = "medium"
    const val TEXT_SIZE_LARGE = "large"
    const val ALIGN_START = "start"
    const val ALIGN_CENTER = "center"

    const val WORD_STYLE_SWEEP = "sweep"
    const val WORD_STYLE_POP = "pop"
    const val WORD_STYLE_FADE = "fade"
    const val WORD_STYLE_GLOW = "glow"
    const val WORD_STYLE_HIGHLIGHTER = "highlighter"
    const val WORD_STYLE_PILL = "pill"
    const val WORD_STYLE_REVEAL = "reveal"
    const val WORD_STYLE_WAVE = "wave"
    const val WORD_STYLE_STRETCH = "stretch"
    const val WORD_STYLE_FOCUS = "focus"

    const val DEFAULT_TEXT_SIZE = TEXT_SIZE_MEDIUM
    const val DEFAULT_WORD_STYLE = WORD_STYLE_SWEEP
    const val DEFAULT_ALIGNMENT = ALIGN_CENTER
    const val DEFAULT_SOFTEN_LINES = true
    const val DEFAULT_IMMERSIVE = true
    const val DEFAULT_KEEP_SCREEN_ON = true
    const val DEFAULT_WORD_BY_WORD = true
    const val DEFAULT_LINE_SWEEP = false
    const val DEFAULT_BACKGROUND = PlayerStylePreferences.BACKGROUND_PLAIN

    fun getTextSize(context: Context): String {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE) ?: DEFAULT_TEXT_SIZE
        val custom = FineTune.value(raw)
        if (custom != null) return FineTune.key(custom.coerceIn(MIN_TEXT_SCALE, MAX_TEXT_SCALE))
        return getChoice(context, KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE, LyricsTextSizeOptions.map { it.key })
    }

    fun textScale(size: String): Float = FineTune.value(size) ?: when (size) {
        TEXT_SIZE_SMALL -> 22f / 28f
        TEXT_SIZE_LARGE -> 34f / 28f
        else -> 1f
    }

    fun getActiveColor(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_ACTIVE_COLOR, ThemePreferences.NO_COLOR)

    fun getInactiveColor(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_INACTIVE_COLOR, ThemePreferences.NO_COLOR)

    fun getLineSpacing(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_LINE_SPACING, 100).coerceIn(MIN_LINE_SPACING, MAX_LINE_SPACING)

    fun saveExtras(context: Context, activeColor: Int, inactiveColor: Int, lineSpacing: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putInt(KEY_ACTIVE_COLOR, activeColor)
            .putInt(KEY_INACTIVE_COLOR, inactiveColor)
            .putInt(KEY_LINE_SPACING, lineSpacing)
            .apply()
    }

    fun getAlignment(context: Context): String =
        getChoice(context, KEY_ALIGNMENT, DEFAULT_ALIGNMENT, LyricsAlignmentOptions.map { it.key })

    fun getSoftenLines(context: Context): Boolean = getFlag(context, KEY_SOFTEN_LINES, DEFAULT_SOFTEN_LINES)

    fun getImmersive(context: Context): Boolean = getFlag(context, KEY_IMMERSIVE, DEFAULT_IMMERSIVE)

    fun getKeepScreenOn(context: Context): Boolean = getFlag(context, KEY_KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON)

    fun getWordByWord(context: Context): Boolean = getFlag(context, KEY_WORD_BY_WORD, DEFAULT_WORD_BY_WORD)

    fun getLineSweep(context: Context): Boolean = getFlag(context, KEY_LINE_SWEEP, DEFAULT_LINE_SWEEP)

    fun getWordStyle(context: Context): String =
        getChoice(context, KEY_WORD_STYLE, DEFAULT_WORD_STYLE, LyricsWordStyleOptions.map { it.key })

    fun getFont(context: Context): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_FONT, FONT_APP) ?: FONT_APP
        return if (value == FONT_APP || FontStore.isFontKey(value) || FontStyleOptions.any { it.key == value }) value else FONT_APP
    }

    fun setFont(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_FONT, value).apply()
    }

    fun getBackground(context: Context): String =
        getChoice(context, KEY_BACKGROUND, DEFAULT_BACKGROUND, PlayerBackgroundOptions.map { it.key })

    fun save(
        context: Context,
        textSize: String,
        alignment: String,
        softenLines: Boolean,
        immersive: Boolean,
        keepScreenOn: Boolean,
        wordByWord: Boolean,
        lineSweep: Boolean,
        wordStyle: String,
        background: String
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_TEXT_SIZE, textSize)
            .putString(KEY_ALIGNMENT, alignment)
            .putBoolean(KEY_SOFTEN_LINES, softenLines)
            .putBoolean(KEY_IMMERSIVE, immersive)
            .putBoolean(KEY_KEEP_SCREEN_ON, keepScreenOn)
            .putBoolean(KEY_WORD_BY_WORD, wordByWord)
            .putBoolean(KEY_LINE_SWEEP, lineSweep)
            .putString(KEY_WORD_STYLE, wordStyle)
            .putString(KEY_BACKGROUND, background)
            .apply()
    }

    private fun getChoice(context: Context, key: String, default: String, allowed: List<String>): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, default) ?: default
        return if (value in allowed) value else default
    }

    private fun getFlag(context: Context, key: String, default: Boolean): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, default)
}
