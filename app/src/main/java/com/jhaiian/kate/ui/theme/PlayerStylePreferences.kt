package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.compose.ui.text.font.FontWeight
import androidx.preference.PreferenceManager
import com.jhaiian.kate.ui.player.PlayerActions

object PlayerStylePreferences {
    private const val KEY_ART_SHAPE = "player_art_shape"
    private const val KEY_ART_SIZE = "player_art_size"
    private const val KEY_BACKGROUND = "player_background"
    private const val KEY_GLOW = "player_glow"
    private const val KEY_SHOW_SHUFFLE = "player_show_shuffle"
    private const val KEY_SHOW_REPEAT = "player_show_repeat"
    private const val KEY_SHOW_FAVORITE = "player_show_favorite"
    private const val KEY_SWIPE_ARTWORK = "player_swipe_artwork"
    private const val KEY_DOUBLE_TAP = "player_double_tap"
    private const val KEY_MINI_SWIPE_SKIP = "mini_player_swipe_skip"
    private const val KEY_MINI_SWIPE_DISMISS = "mini_player_swipe_dismiss"
    private const val KEY_LAYOUT = "player_layout"
    private const val KEY_COVER_PREVIEW = "player_cover_preview"
    private const val KEY_VINYL_SPEED = "player_vinyl_speed"
    private const val KEY_VINYL_TONEARM = "player_vinyl_tonearm"
    private const val KEY_CASSETTE_COLOR = "player_cassette_color"
    private const val KEY_HEADER = "player_header"
    private const val KEY_SHOW_HANDLE = "player_show_handle"
    private const val KEY_SHOW_COLLAPSE = "player_show_collapse"
    private const val KEY_SHOW_QUEUE_BUTTON = "player_show_queue_button"
    private const val KEY_SHOW_MENU_BUTTON = "player_show_menu_button"
    private const val KEY_SHOW_AUDIO_CHIP = "player_show_audio_chip"
    private const val KEY_INFO_ALIGN = "player_info_align"
    private const val KEY_TITLE_SIZE = "player_title_size"
    private const val KEY_SHOW_ALBUM = "player_show_album"
    private const val KEY_MARQUEE = "player_marquee"
    private const val KEY_LYRIC_LINE = "player_lyric_line"
    private const val KEY_QUICK_ACTIONS = "player_quick_actions"
    private const val KEY_SKIP_BUTTONS = "player_skip_buttons"
    private const val KEY_ART_SHADOW = "player_art_shadow"
    private const val KEY_BACKDROP_STRENGTH = "player_backdrop_strength"
    private const val KEY_TITLE_WEIGHT = "player_title_weight"
    private const val KEY_SLOT_LEFT = "player_slot_left"
    private const val KEY_SLOT_INNER_LEFT = "player_slot_inner_left"
    private const val KEY_SLOT_INNER_RIGHT = "player_slot_inner_right"
    private const val KEY_SLOT_RIGHT = "player_slot_right"
    private const val KEY_QUICK_LIST = "player_quick_list"
    private const val KEY_SEEK_STEP = "player_seek_step"
    private const val KEY_LONG_PRESS = "player_long_press"
    private const val KEY_DOUBLE_TAP_SIDES = "player_double_tap_sides"
    private const val KEY_MINI_LONG_PRESS = "mini_player_long_press"
    private const val KEY_INFO_FIELDS = "player_info_fields"
    private const val KEY_LAYOUT_LANDSCAPE = "player_layout_landscape"
    private const val LIST_SEPARATOR = ","
    const val LAYOUT_SAME = "same"
    const val INFO_YEAR = "year"
    const val INFO_GENRE = "genre"
    const val INFO_FORMAT = "format"
    const val INFO_QUALITY = "quality"
    const val INFO_PLAYS = "plays"
    const val INFO_TRACK = "track"
    val InfoFields = listOf(INFO_TRACK, INFO_YEAR, INFO_GENRE, INFO_FORMAT, INFO_QUALITY, INFO_PLAYS)
    val SeekSteps = listOf(5, 10, 15, 30)
    const val DEFAULT_SEEK_STEP = 10
    const val MIN_ART_SCALE = 0.6f
    const val MAX_ART_SCALE = 1.2f
    const val BACKGROUND_IMAGE = "image"
    const val WEIGHT_DEFAULT = "default"
    const val WEIGHT_LIGHT = "light"
    const val WEIGHT_REGULAR = "regular"
    const val WEIGHT_MEDIUM = "medium"
    const val WEIGHT_BOLD = "bold"
    const val WEIGHT_BLACK = "black"
    const val DEFAULT_TITLE_WEIGHT = WEIGHT_DEFAULT

    const val ART_SHAPE_ROUNDED = "rounded"
    const val ART_SHAPE_SQUARE = "square"
    const val ART_SHAPE_CIRCLE = "circle"
    const val ART_SIZE_NORMAL = "normal"
    const val ART_SIZE_LARGE = "large"
    const val BACKGROUND_DRIFT = "drift"
    const val BACKGROUND_ARTWORK = "artwork"
    const val BACKGROUND_PLAIN = "plain"
    const val BACKGROUND_GRADIENT = "gradient"
    const val DOUBLE_TAP_FAVORITE = "favorite"
    const val DOUBLE_TAP_PLAY_PAUSE = "play_pause"
    const val DOUBLE_TAP_NONE = "none"

    const val LAYOUT_CLASSIC = "classic"
    const val LAYOUT_BIG_ART = "big_art"
    const val LAYOUT_MINIMAL = "minimal"
    const val LAYOUT_VINYL = "vinyl"
    const val LAYOUT_CASSETTE = "cassette"
    const val LAYOUT_CARD = "card"
    const val VINYL_SPEED_SLOW = "slow"
    const val VINYL_SPEED_33 = "rpm33"
    const val VINYL_SPEED_45 = "rpm45"
    const val CASSETTE_BLACK = "black"
    const val CASSETTE_CLEAR = "clear"
    const val CASSETTE_WHITE = "white"
    const val HEADER_ALBUM = "album"
    const val HEADER_QUEUE = "queue"
    const val HEADER_HIDDEN = "hidden"
    const val ALIGN_START = "start"
    const val ALIGN_CENTER = "center"
    const val TITLE_SMALL = "small"
    const val TITLE_MEDIUM = "medium"
    const val TITLE_LARGE = "large"
    const val SKIP_TRACKS = "tracks"
    const val SKIP_SEEK = "seek"
    const val STRENGTH_SUBTLE = "subtle"
    const val STRENGTH_NORMAL = "normal"
    const val STRENGTH_VIVID = "vivid"
    const val SEEK_STEP_MS = 10_000L

    const val DEFAULT_LAYOUT = LAYOUT_CLASSIC
    const val DEFAULT_COVER_PREVIEW = true
    const val DEFAULT_VINYL_SPEED = VINYL_SPEED_SLOW
    const val DEFAULT_VINYL_TONEARM = true
    const val DEFAULT_CASSETTE_COLOR = CASSETTE_BLACK
    const val DEFAULT_ART_SHAPE = ART_SHAPE_ROUNDED
    const val DEFAULT_ART_SIZE = ART_SIZE_NORMAL
    const val DEFAULT_BACKGROUND = BACKGROUND_PLAIN
    const val DEFAULT_GLOW = true
    const val DEFAULT_SHOW_SHUFFLE = true
    const val DEFAULT_SHOW_REPEAT = true
    const val DEFAULT_SHOW_FAVORITE = true
    const val DEFAULT_SWIPE_ARTWORK = true
    const val DEFAULT_DOUBLE_TAP = DOUBLE_TAP_PLAY_PAUSE
    const val DEFAULT_DOUBLE_TAP_SIDES = true
    const val DEFAULT_MINI_SWIPE_SKIP = true
    const val DEFAULT_MINI_SWIPE_DISMISS = true
    const val DEFAULT_HEADER = HEADER_ALBUM
    const val DEFAULT_SHOW_HANDLE = true
    const val DEFAULT_SHOW_COLLAPSE = true
    const val DEFAULT_SHOW_QUEUE_BUTTON = true
    const val DEFAULT_SHOW_MENU_BUTTON = true
    const val DEFAULT_SHOW_AUDIO_CHIP = true
    const val DEFAULT_INFO_ALIGN = ALIGN_START
    const val DEFAULT_TITLE_SIZE = TITLE_MEDIUM
    const val DEFAULT_SHOW_ALBUM = false
    const val DEFAULT_MARQUEE = true
    const val DEFAULT_LYRIC_LINE = true
    const val DEFAULT_QUICK_ACTIONS = false
    const val DEFAULT_SKIP_BUTTONS = SKIP_TRACKS
    const val DEFAULT_ART_SHADOW = true
    const val DEFAULT_BACKDROP_STRENGTH = STRENGTH_NORMAL

    fun getArtShape(context: Context): String =
        getChoice(context, KEY_ART_SHAPE, DEFAULT_ART_SHAPE, ArtShapeOptions.map { it.key })

    fun getArtSize(context: Context): String {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_ART_SIZE, DEFAULT_ART_SIZE) ?: DEFAULT_ART_SIZE
        val custom = FineTune.value(raw)
        if (custom != null) return FineTune.key(custom.coerceIn(MIN_ART_SCALE, MAX_ART_SCALE))
        return getChoice(context, KEY_ART_SIZE, DEFAULT_ART_SIZE, ArtSizeOptions.map { it.key })
    }

    fun artScale(size: String): Float = FineTune.value(size) ?: 1f

    fun getTitleWeight(context: Context): String =
        getChoice(context, KEY_TITLE_WEIGHT, DEFAULT_TITLE_WEIGHT, TitleWeightOptions.map { it.key })

    fun setTitleWeight(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_TITLE_WEIGHT, value).apply()
    }

    fun titleWeight(key: String, fallback: FontWeight): FontWeight = when (key) {
        WEIGHT_LIGHT -> FontWeight.Light
        WEIGHT_REGULAR -> FontWeight.Normal
        WEIGHT_MEDIUM -> FontWeight.Medium
        WEIGHT_BOLD -> FontWeight.Bold
        WEIGHT_BLACK -> FontWeight.Black
        else -> fallback
    }

    fun getBackground(context: Context): String =
        getChoice(context, KEY_BACKGROUND, DEFAULT_BACKGROUND, PlayerScreenBackgroundOptions.map { it.key })

    fun getGlow(context: Context): Boolean = getFlag(context, KEY_GLOW, DEFAULT_GLOW)

    fun getShowShuffle(context: Context): Boolean = getFlag(context, KEY_SHOW_SHUFFLE, DEFAULT_SHOW_SHUFFLE)

    fun getShowRepeat(context: Context): Boolean = getFlag(context, KEY_SHOW_REPEAT, DEFAULT_SHOW_REPEAT)

    fun getShowFavorite(context: Context): Boolean = getFlag(context, KEY_SHOW_FAVORITE, DEFAULT_SHOW_FAVORITE)

    fun getSwipeArtwork(context: Context): Boolean = getFlag(context, KEY_SWIPE_ARTWORK, DEFAULT_SWIPE_ARTWORK)

    fun getMiniSwipeSkip(context: Context): Boolean = getFlag(context, KEY_MINI_SWIPE_SKIP, DEFAULT_MINI_SWIPE_SKIP)

    fun getMiniSwipeDismiss(context: Context): Boolean = getFlag(context, KEY_MINI_SWIPE_DISMISS, DEFAULT_MINI_SWIPE_DISMISS)

    fun getLayout(context: Context): String =
        getChoice(context, KEY_LAYOUT, DEFAULT_LAYOUT, PlayerLayoutOptions.map { it.key })

    fun getVinylSpeed(context: Context): String =
        getChoice(context, KEY_VINYL_SPEED, DEFAULT_VINYL_SPEED, VinylSpeedOptions.map { it.key })

    fun getVinylTonearm(context: Context): Boolean = getFlag(context, KEY_VINYL_TONEARM, DEFAULT_VINYL_TONEARM)

    fun getCoverPreview(context: Context): Boolean = getFlag(context, KEY_COVER_PREVIEW, DEFAULT_COVER_PREVIEW)

    fun getCassetteColor(context: Context): String =
        getChoice(context, KEY_CASSETTE_COLOR, DEFAULT_CASSETTE_COLOR, CassetteColorOptions.map { it.key })

    fun vinylDegreesPerSecond(speed: String): Float = when (speed) {
        VINYL_SPEED_33 -> 200f
        VINYL_SPEED_45 -> 270f
        else -> 60f
    }

    fun getDoubleTap(context: Context): String =
        getChoice(context, KEY_DOUBLE_TAP, DEFAULT_DOUBLE_TAP, PlayerActions.Gestures)

    fun getSlotLeft(context: Context): String =
        getSlot(context, KEY_SLOT_LEFT, if (getShowShuffle(context)) PlayerActions.SHUFFLE else PlayerActions.NONE, PlayerActions.SideSlots)

    fun getSlotRight(context: Context): String =
        getSlot(context, KEY_SLOT_RIGHT, if (getShowRepeat(context)) PlayerActions.REPEAT else PlayerActions.NONE, PlayerActions.SideSlots)

    fun getSlotInnerLeft(context: Context): String =
        getSlot(context, KEY_SLOT_INNER_LEFT, if (getSkipButtons(context) == SKIP_SEEK) PlayerActions.REWIND else PlayerActions.PREVIOUS, PlayerActions.InnerSlots)

    fun getSlotInnerRight(context: Context): String =
        getSlot(context, KEY_SLOT_INNER_RIGHT, if (getSkipButtons(context) == SKIP_SEEK) PlayerActions.FORWARD else PlayerActions.NEXT, PlayerActions.InnerSlots)

    private fun getSlot(context: Context, key: String, legacy: String, allowed: List<String>): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null) ?: return legacy
        return if (value in allowed) value else legacy
    }

    fun getQuickList(context: Context): List<String> {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_QUICK_LIST, null)
            ?: return PlayerActions.DefaultQuickRow
        return raw.split(LIST_SEPARATOR).filter { it in PlayerActions.QuickRow }.distinct().take(PlayerActions.MAX_QUICK)
    }

    fun getSeekStep(context: Context): Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_SEEK_STEP, DEFAULT_SEEK_STEP).takeIf { it in SeekSteps } ?: DEFAULT_SEEK_STEP

    fun getLongPress(context: Context): String = getChoice(context, KEY_LONG_PRESS, PlayerActions.NONE, PlayerActions.Gestures)

    fun getDoubleTapSides(context: Context): Boolean = getFlag(context, KEY_DOUBLE_TAP_SIDES, DEFAULT_DOUBLE_TAP_SIDES)

    fun getMiniLongPress(context: Context): String = getChoice(context, KEY_MINI_LONG_PRESS, PlayerActions.NONE, PlayerActions.Gestures)

    fun getInfoFields(context: Context): Set<String> =
        PreferenceManager.getDefaultSharedPreferences(context).getStringSet(KEY_INFO_FIELDS, null)?.filter { it in InfoFields }?.toSet().orEmpty()

    fun getLayoutLandscape(context: Context): String =
        getChoice(context, KEY_LAYOUT_LANDSCAPE, LAYOUT_SAME, listOf(LAYOUT_SAME) + PlayerLayoutOptions.map { it.key })

    fun saveBuilder(
        context: Context,
        slotLeft: String,
        slotInnerLeft: String,
        slotInnerRight: String,
        slotRight: String,
        quickList: List<String>,
        seekStep: Int,
        longPress: String,
        doubleTapSides: Boolean,
        miniLongPress: String,
        infoFields: Set<String>,
        layoutLandscape: String
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_SLOT_LEFT, slotLeft)
            .putString(KEY_SLOT_INNER_LEFT, slotInnerLeft)
            .putString(KEY_SLOT_INNER_RIGHT, slotInnerRight)
            .putString(KEY_SLOT_RIGHT, slotRight)
            .putString(KEY_QUICK_LIST, quickList.joinToString(LIST_SEPARATOR))
            .putInt(KEY_SEEK_STEP, seekStep)
            .putString(KEY_LONG_PRESS, longPress)
            .putBoolean(KEY_DOUBLE_TAP_SIDES, doubleTapSides)
            .putString(KEY_MINI_LONG_PRESS, miniLongPress)
            .putStringSet(KEY_INFO_FIELDS, HashSet(infoFields))
            .putString(KEY_LAYOUT_LANDSCAPE, layoutLandscape)
            .apply()
    }

    fun getHeader(context: Context): String =
        getChoice(context, KEY_HEADER, DEFAULT_HEADER, PlayerHeaderOptions.map { it.key })

    fun getShowHandle(context: Context): Boolean = getFlag(context, KEY_SHOW_HANDLE, DEFAULT_SHOW_HANDLE)

    fun getShowCollapse(context: Context): Boolean = getFlag(context, KEY_SHOW_COLLAPSE, DEFAULT_SHOW_COLLAPSE)

    fun getShowQueueButton(context: Context): Boolean = getFlag(context, KEY_SHOW_QUEUE_BUTTON, DEFAULT_SHOW_QUEUE_BUTTON)

    fun getShowMenuButton(context: Context): Boolean = getFlag(context, KEY_SHOW_MENU_BUTTON, DEFAULT_SHOW_MENU_BUTTON)

    fun getShowAudioChip(context: Context): Boolean = getFlag(context, KEY_SHOW_AUDIO_CHIP, DEFAULT_SHOW_AUDIO_CHIP)

    fun getInfoAlign(context: Context): String =
        getChoice(context, KEY_INFO_ALIGN, DEFAULT_INFO_ALIGN, PlayerInfoAlignOptions.map { it.key })

    fun getTitleSize(context: Context): String =
        getChoice(context, KEY_TITLE_SIZE, DEFAULT_TITLE_SIZE, PlayerTitleSizeOptions.map { it.key })

    fun getShowAlbum(context: Context): Boolean = getFlag(context, KEY_SHOW_ALBUM, DEFAULT_SHOW_ALBUM)

    fun getMarquee(context: Context): Boolean = getFlag(context, KEY_MARQUEE, DEFAULT_MARQUEE)

    fun getLyricLine(context: Context): Boolean = getFlag(context, KEY_LYRIC_LINE, DEFAULT_LYRIC_LINE)

    fun getQuickActions(context: Context): Boolean = getFlag(context, KEY_QUICK_ACTIONS, DEFAULT_QUICK_ACTIONS)

    fun getSkipButtons(context: Context): String =
        getChoice(context, KEY_SKIP_BUTTONS, DEFAULT_SKIP_BUTTONS, PlayerSkipButtonOptions.map { it.key })

    fun getArtShadow(context: Context): Boolean = getFlag(context, KEY_ART_SHADOW, DEFAULT_ART_SHADOW)

    fun getBackdropStrength(context: Context): String =
        getChoice(context, KEY_BACKDROP_STRENGTH, DEFAULT_BACKDROP_STRENGTH, PlayerBackdropStrengthOptions.map { it.key })

    fun backdropFactor(strength: String): Float = when (strength) {
        STRENGTH_SUBTLE -> 0.55f
        STRENGTH_VIVID -> 1.6f
        else -> 1f
    }

    fun save(
        context: Context,
        artShape: String,
        artSize: String,
        background: String,
        glow: Boolean,
        showShuffle: Boolean,
        showRepeat: Boolean,
        showFavorite: Boolean,
        swipeArtwork: Boolean,
        doubleTap: String,
        miniSwipeSkip: Boolean,
        miniSwipeDismiss: Boolean,
        layout: String,
        vinylSpeed: String,
        vinylTonearm: Boolean,
        cassetteColor: String,
        coverPreview: Boolean,
        header: String,
        showHandle: Boolean,
        showCollapse: Boolean,
        showQueueButton: Boolean,
        showMenuButton: Boolean,
        showAudioChip: Boolean,
        infoAlign: String,
        titleSize: String,
        showAlbum: Boolean,
        marquee: Boolean,
        lyricLine: Boolean,
        quickActions: Boolean,
        skipButtons: String,
        artShadow: Boolean,
        backdropStrength: String
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_ART_SHAPE, artShape)
            .putString(KEY_ART_SIZE, artSize)
            .putString(KEY_BACKGROUND, background)
            .putBoolean(KEY_GLOW, glow)
            .putBoolean(KEY_SHOW_SHUFFLE, showShuffle)
            .putBoolean(KEY_SHOW_REPEAT, showRepeat)
            .putBoolean(KEY_SHOW_FAVORITE, showFavorite)
            .putBoolean(KEY_SWIPE_ARTWORK, swipeArtwork)
            .putString(KEY_DOUBLE_TAP, doubleTap)
            .putBoolean(KEY_MINI_SWIPE_SKIP, miniSwipeSkip)
            .putBoolean(KEY_MINI_SWIPE_DISMISS, miniSwipeDismiss)
            .putString(KEY_LAYOUT, layout)
            .putString(KEY_VINYL_SPEED, vinylSpeed)
            .putBoolean(KEY_VINYL_TONEARM, vinylTonearm)
            .putString(KEY_CASSETTE_COLOR, cassetteColor)
            .putBoolean(KEY_COVER_PREVIEW, coverPreview)
            .putString(KEY_HEADER, header)
            .putBoolean(KEY_SHOW_HANDLE, showHandle)
            .putBoolean(KEY_SHOW_COLLAPSE, showCollapse)
            .putBoolean(KEY_SHOW_QUEUE_BUTTON, showQueueButton)
            .putBoolean(KEY_SHOW_MENU_BUTTON, showMenuButton)
            .putBoolean(KEY_SHOW_AUDIO_CHIP, showAudioChip)
            .putString(KEY_INFO_ALIGN, infoAlign)
            .putString(KEY_TITLE_SIZE, titleSize)
            .putBoolean(KEY_SHOW_ALBUM, showAlbum)
            .putBoolean(KEY_MARQUEE, marquee)
            .putBoolean(KEY_LYRIC_LINE, lyricLine)
            .putBoolean(KEY_QUICK_ACTIONS, quickActions)
            .putString(KEY_SKIP_BUTTONS, skipButtons)
            .putBoolean(KEY_ART_SHADOW, artShadow)
            .putString(KEY_BACKDROP_STRENGTH, backdropStrength)
            .apply()
    }

    private fun getChoice(context: Context, key: String, default: String, allowed: List<String>): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, default) ?: default
        return if (value in allowed) value else default
    }

    private fun getFlag(context: Context, key: String, default: Boolean): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, default)
}
