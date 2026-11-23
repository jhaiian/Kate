package com.jhaiian.kate.widget

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.jhaiian.kate.ui.theme.PlayerStylePreferences

enum class WidgetKind { NOW_PLAYING, ALBUM_ART, UP_NEXT, QUICK_PLAY }

data class WidgetSettings(
    val background: String = BACKGROUND_SYSTEM,
    val opacity: Int = 100,
    val artShape: String = PlayerStylePreferences.ART_SHAPE_ROUNDED,
    val showProgress: Boolean = true,
    val showArtist: Boolean = true,
    val showAlbum: Boolean = false,
    val showShuffle: Boolean = false,
    val showRepeat: Boolean = false,
    val showFavorite: Boolean = false,
    val showPrevious: Boolean = true,
    val tapAction: String = TAP_OPEN_PLAYER,
    val quickShuffle: Boolean = true,
    val quickFavorites: Boolean = true,
    val quickRecent: Boolean = true,
    val quickResume: Boolean = true,
    val corners: String = CORNERS_SYSTEM
) {
    companion object {
        const val BACKGROUND_SYSTEM = "system"
        const val BACKGROUND_ARTWORK = "artwork"
        const val BACKGROUND_DARK = "dark"
        const val BACKGROUND_LIGHT = "light"
        const val TAP_OPEN_PLAYER = "open_player"
        const val TAP_OPEN_APP = "open_app"
        const val TAP_PLAY_PAUSE = "play_pause"
        const val CORNERS_SYSTEM = "system"
        const val CORNERS_SQUARE = "square"
        const val CORNERS_ROUND = "round"
        val Corners = listOf(CORNERS_SYSTEM, CORNERS_SQUARE, CORNERS_ROUND)
        val Backgrounds = listOf(BACKGROUND_SYSTEM, BACKGROUND_ARTWORK, BACKGROUND_DARK, BACKGROUND_LIGHT)
        val TapActions = listOf(TAP_OPEN_PLAYER, TAP_OPEN_APP, TAP_PLAY_PAUSE)
        val ArtShapes = listOf(
            PlayerStylePreferences.ART_SHAPE_ROUNDED,
            PlayerStylePreferences.ART_SHAPE_CIRCLE,
            PlayerStylePreferences.ART_SHAPE_SQUARE
        )
    }
}

object WidgetSettingsStore {
    private const val PREFS_NAME = "widget_settings"

    var revision by mutableIntStateOf(0)
        private set

    fun load(context: Context, appWidgetId: Int): WidgetSettings {
        val prefs = prefs(context)
        val defaults = WidgetSettings()
        fun key(name: String) = "${appWidgetId}_$name"
        fun choice(name: String, default: String, allowed: List<String>): String =
            prefs.getString(key(name), default)?.takeIf { it in allowed } ?: default
        return WidgetSettings(
            background = choice("background", defaults.background, WidgetSettings.Backgrounds),
            opacity = prefs.getInt(key("opacity"), defaults.opacity).coerceIn(0, 100),
            artShape = choice("art_shape", defaults.artShape, WidgetSettings.ArtShapes),
            showProgress = prefs.getBoolean(key("show_progress"), defaults.showProgress),
            showArtist = prefs.getBoolean(key("show_artist"), defaults.showArtist),
            showAlbum = prefs.getBoolean(key("show_album"), defaults.showAlbum),
            showShuffle = prefs.getBoolean(key("show_shuffle"), defaults.showShuffle),
            showRepeat = prefs.getBoolean(key("show_repeat"), defaults.showRepeat),
            showFavorite = prefs.getBoolean(key("show_favorite"), defaults.showFavorite),
            showPrevious = prefs.getBoolean(key("show_previous"), defaults.showPrevious),
            tapAction = choice("tap_action", defaults.tapAction, WidgetSettings.TapActions),
            quickShuffle = prefs.getBoolean(key("quick_shuffle"), defaults.quickShuffle),
            quickFavorites = prefs.getBoolean(key("quick_favorites"), defaults.quickFavorites),
            quickRecent = prefs.getBoolean(key("quick_recent"), defaults.quickRecent),
            quickResume = prefs.getBoolean(key("quick_resume"), defaults.quickResume),
            corners = choice("corners", defaults.corners, WidgetSettings.Corners)
        )
    }

    fun save(context: Context, appWidgetId: Int, settings: WidgetSettings) {
        fun key(name: String) = "${appWidgetId}_$name"
        prefs(context).edit()
            .putString(key("background"), settings.background)
            .putInt(key("opacity"), settings.opacity)
            .putString(key("art_shape"), settings.artShape)
            .putBoolean(key("show_progress"), settings.showProgress)
            .putBoolean(key("show_artist"), settings.showArtist)
            .putBoolean(key("show_album"), settings.showAlbum)
            .putBoolean(key("show_shuffle"), settings.showShuffle)
            .putBoolean(key("show_repeat"), settings.showRepeat)
            .putBoolean(key("show_favorite"), settings.showFavorite)
            .putBoolean(key("show_previous"), settings.showPrevious)
            .putString(key("tap_action"), settings.tapAction)
            .putBoolean(key("quick_shuffle"), settings.quickShuffle)
            .putBoolean(key("quick_favorites"), settings.quickFavorites)
            .putBoolean(key("quick_recent"), settings.quickRecent)
            .putBoolean(key("quick_resume"), settings.quickResume)
            .putString(key("corners"), settings.corners)
            .apply()
        revision += 1
    }

    fun delete(context: Context, appWidgetIds: IntArray) {
        val prefs = prefs(context)
        val editor = prefs.edit()
        appWidgetIds.forEach { id ->
            prefs.all.keys.filter { it.startsWith("${id}_") }.forEach { editor.remove(it) }
        }
        editor.apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
