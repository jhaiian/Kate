package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.preference.PreferenceManager
import java.util.Locale

object DisplayPreferences {
    private const val KEY_HIDE_STATUS_BAR = "hide_status_bar"
    private const val KEY_HIDE_NAVIGATION_BAR = "hide_navigation_bar"
    private const val KEY_FONT_STYLE = "font_style"
    private const val KEY_TEXT_SIZE = "text_size"
    private const val KEY_APP_IMAGE = "background_image_app"
    private const val KEY_PLAYER_IMAGE = "background_image_player"
    private const val KEY_IMAGE_BLUR = "background_image_blur"
    private const val KEY_IMAGE_DIM = "background_image_dim"
    const val DEFAULT_IMAGE_BLUR = 16
    const val DEFAULT_IMAGE_DIM = 55
    const val MAX_IMAGE_BLUR = 40
    const val MIN_IMAGE_DIM = 10
    const val MAX_IMAGE_DIM = 95
    const val MIN_TEXT_SCALE = 0.8f
    const val MAX_TEXT_SCALE = 1.5f

    const val FONT_DEFAULT = "default"
    const val FONT_CONDENSED = "condensed"
    const val FONT_SERIF = "serif"
    const val FONT_MONO = "mono"
    const val FONT_CASUAL = "casual"
    const val TEXT_SMALL = "small"
    const val TEXT_DEFAULT = "default"
    const val TEXT_LARGE = "large"
    const val TEXT_LARGER = "larger"
    const val DEFAULT_FONT_STYLE = FONT_DEFAULT
    const val DEFAULT_TEXT_SIZE = TEXT_DEFAULT

    const val DEFAULT_HIDE_STATUS_BAR = true
    const val DEFAULT_HIDE_NAVIGATION_BAR = false

    fun getHideStatusBar(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_HIDE_STATUS_BAR, DEFAULT_HIDE_STATUS_BAR)

    fun getHideNavigationBar(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_HIDE_NAVIGATION_BAR, DEFAULT_HIDE_NAVIGATION_BAR)

    fun getFontStyle(context: Context): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_FONT_STYLE, DEFAULT_FONT_STYLE) ?: DEFAULT_FONT_STYLE
        return if (FontStore.isFontKey(value) || FontStyleOptions.any { it.key == value }) value else DEFAULT_FONT_STYLE
    }

    fun getTextSize(context: Context): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE) ?: DEFAULT_TEXT_SIZE
        val custom = FineTune.value(value)
        if (custom != null) return FineTune.key(custom.coerceIn(MIN_TEXT_SCALE, MAX_TEXT_SCALE))
        return if (TextSizeOptions.any { it.key == value }) value else DEFAULT_TEXT_SIZE
    }

    fun setFontStyle(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_FONT_STYLE, value).apply()
    }

    fun setTextSize(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_TEXT_SIZE, value).apply()
    }

    fun textScale(size: String): Float = FineTune.value(size) ?: when (size) {
        TEXT_SMALL -> 0.9f
        TEXT_LARGE -> 1.12f
        TEXT_LARGER -> 1.25f
        else -> 1f
    }

    fun setHideStatusBar(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_HIDE_STATUS_BAR, value).apply()
    }

    fun setHideNavigationBar(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(KEY_HIDE_NAVIGATION_BAR, value).apply()
    }

    fun getAppImage(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_APP_IMAGE, "").orEmpty()

    fun getPlayerImage(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_PLAYER_IMAGE, "").orEmpty()

    fun getImageBlur(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_IMAGE_BLUR, DEFAULT_IMAGE_BLUR).coerceIn(0, MAX_IMAGE_BLUR)

    fun getImageDim(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_IMAGE_DIM, DEFAULT_IMAGE_DIM).coerceIn(MIN_IMAGE_DIM, MAX_IMAGE_DIM)

    fun saveImages(context: Context, appImage: String, playerImage: String, blur: Int, dim: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_APP_IMAGE, appImage)
            .putString(KEY_PLAYER_IMAGE, playerImage)
            .putInt(KEY_IMAGE_BLUR, blur)
            .putInt(KEY_IMAGE_DIM, dim)
            .apply()
    }
}

object FineTune {
    const val PREFIX = "custom:"

    fun value(key: String): Float? = key.takeIf { it.startsWith(PREFIX) }?.removePrefix(PREFIX)?.toFloatOrNull()?.takeIf { !it.isNaN() }

    fun key(value: Float): String = PREFIX + String.format(Locale.US, "%.2f", value)
}
