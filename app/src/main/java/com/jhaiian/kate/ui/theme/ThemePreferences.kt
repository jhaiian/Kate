package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.preference.PreferenceManager

object ThemePreferences {
    private const val KEY_THEME = "app_theme"
    private const val KEY_ACCENT = "accent_color"
    private const val KEY_INTENSITY = "surface_intensity"
    private const val KEY_PLAYER_ARTWORK_COLORS = "player_artwork_colors"
    private const val KEY_APP_ARTWORK_COLORS = "app_artwork_colors"
    private const val KEY_PALETTE_STYLE = "palette_style"
    private const val KEY_CONTRAST = "theme_contrast"
    private const val KEY_CUSTOM_ACCENT = "custom_accent_argb"
    private const val KEY_COLOR_BACKGROUND = "color_override_background"
    private const val KEY_COLOR_CARD = "color_override_card"
    private const val KEY_COLOR_TEXT = "color_override_text"
    private const val KEY_COLOR_SECONDARY_TEXT = "color_override_secondary_text"

    const val ACCENT_CUSTOM = "custom"
    const val STYLE_AUTO = "auto"
    const val STYLE_TONAL_SPOT = "tonal_spot"
    const val STYLE_VIBRANT = "vibrant"
    const val STYLE_EXPRESSIVE = "expressive"
    const val STYLE_FIDELITY = "fidelity"
    const val STYLE_CONTENT = "content"
    const val STYLE_RAINBOW = "rainbow"
    const val STYLE_FRUIT_SALAD = "fruit_salad"
    const val STYLE_NEUTRAL = "neutral"
    const val STYLE_MONOCHROME = "monochrome"
    const val CONTRAST_STANDARD = "standard"
    const val CONTRAST_MEDIUM = "medium"
    const val CONTRAST_HIGH = "high"
    const val NO_COLOR = 0
    const val DEFAULT_PALETTE_STYLE = STYLE_AUTO
    const val DEFAULT_CONTRAST = CONTRAST_STANDARD
    const val DEFAULT_CUSTOM_ACCENT = -0x98af5c

    const val DEFAULT_THEME = "system"
    const val DEFAULT_ACCENT = "material_you"
    const val DEFAULT_INTENSITY = "soft_tint"
    const val DEFAULT_PLAYER_ARTWORK_COLORS = false
    const val DEFAULT_APP_ARTWORK_COLORS = false
    private const val LEGACY_ARTWORK_ACCENT = "artwork"

    fun getTheme(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_THEME, DEFAULT_THEME)
            ?.takeIf { stored -> ThemeOptions.any { it.key == stored } } ?: DEFAULT_THEME

    fun getAccent(context: Context): String {
        val accent = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_ACCENT, DEFAULT_ACCENT) ?: DEFAULT_ACCENT
        return when {
            AccentOptions.any { it.key == accent } -> accent
            isCustomAccent(accent) && CustomThemeRepository.find(context, customThemeId(accent)) != null -> accent
            else -> DEFAULT_ACCENT
        }
    }

    fun getIntensity(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_INTENSITY, DEFAULT_INTENSITY)
            ?.takeIf { stored -> IntensityOptions.any { it.key == stored } } ?: DEFAULT_INTENSITY

    fun getPlayerArtworkColors(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_PLAYER_ARTWORK_COLORS, DEFAULT_PLAYER_ARTWORK_COLORS)

    fun setPlayerArtworkColors(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_PLAYER_ARTWORK_COLORS, value).apply()
    }

    fun getAppArtworkColors(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(
            KEY_APP_ARTWORK_COLORS,
            DEFAULT_APP_ARTWORK_COLORS || prefs.getString(KEY_ACCENT, null) == LEGACY_ARTWORK_ACCENT
        )
    }

    fun setAppArtworkColors(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_APP_ARTWORK_COLORS, value).apply()
    }

    fun setTheme(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_THEME, value).apply()
    }

    fun setAccent(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_ACCENT, value).apply()
    }

    fun setIntensity(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_INTENSITY, value).apply()
    }

    fun getPaletteStyle(context: Context): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PALETTE_STYLE, DEFAULT_PALETTE_STYLE)
        return value?.takeIf { stored -> PaletteStyleOptions.any { it.key == stored } } ?: DEFAULT_PALETTE_STYLE
    }

    fun getContrast(context: Context): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_CONTRAST, DEFAULT_CONTRAST)
        return value?.takeIf { stored -> ContrastOptions.any { it.key == stored } } ?: DEFAULT_CONTRAST
    }

    fun getCustomAccent(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_CUSTOM_ACCENT, DEFAULT_CUSTOM_ACCENT)

    fun getColorOverrides(context: Context): ColorOverrides {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return ColorOverrides(
            background = prefs.getInt(KEY_COLOR_BACKGROUND, NO_COLOR),
            card = prefs.getInt(KEY_COLOR_CARD, NO_COLOR),
            text = prefs.getInt(KEY_COLOR_TEXT, NO_COLOR),
            secondaryText = prefs.getInt(KEY_COLOR_SECONDARY_TEXT, NO_COLOR)
        )
    }

    fun saveColorEngine(
        context: Context,
        paletteStyle: String,
        contrast: String,
        customAccent: Int,
        overrides: ColorOverrides
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_PALETTE_STYLE, paletteStyle)
            .putString(KEY_CONTRAST, contrast)
            .putInt(KEY_CUSTOM_ACCENT, customAccent)
            .putInt(KEY_COLOR_BACKGROUND, overrides.background)
            .putInt(KEY_COLOR_CARD, overrides.card)
            .putInt(KEY_COLOR_TEXT, overrides.text)
            .putInt(KEY_COLOR_SECONDARY_TEXT, overrides.secondaryText)
            .apply()
    }

    fun contrastLevel(contrast: String): Double = when (contrast) {
        CONTRAST_MEDIUM -> 0.5
        CONTRAST_HIGH -> 1.0
        else -> 0.0
    }
}

data class ColorOverrides(
    val background: Int = ThemePreferences.NO_COLOR,
    val card: Int = ThemePreferences.NO_COLOR,
    val text: Int = ThemePreferences.NO_COLOR,
    val secondaryText: Int = ThemePreferences.NO_COLOR
)

data class ThemeTuning(
    val paletteStyle: String = ThemePreferences.DEFAULT_PALETTE_STYLE,
    val contrast: String = ThemePreferences.DEFAULT_CONTRAST,
    val customAccent: Int = ThemePreferences.DEFAULT_CUSTOM_ACCENT
)
