package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.preference.PreferenceManager
import com.jhaiian.kate.data.LibraryCategory

object LibraryLayoutPreferences {
    private const val KEY_CATEGORY_ORDER = "library_category_order"
    private const val KEY_HIDDEN_CATEGORIES = "library_hidden_categories"
    private const val KEY_HOME_ORDER = "home_section_order"
    private const val KEY_HOME_PINS = "home_pins"
    private const val KEY_SONG_TITLE = "song_row_title"
    private const val KEY_SONG_FIELDS = "song_row_fields"
    private const val KEY_SONG_TRACK_NUMBER = "song_row_track_number"
    private const val KEY_SONG_TAP = "song_tap_action"
    private const val KEY_TAP_OPENS_PLAYER = "song_tap_opens_player"
    private const val SEPARATOR = ","

    const val TITLE_TAG = "title"
    const val TITLE_FILENAME = "filename"
    const val FIELD_ARTIST = "artist"
    const val FIELD_ALBUM = "album"
    const val FIELD_ALBUM_ARTIST = "album_artist"
    const val FIELD_YEAR = "year"
    const val FIELD_GENRE = "genre"
    const val FIELD_FORMAT = "format"
    const val FIELD_FOLDER = "folder"
    const val TAP_PLAY_LIST = "play_list"
    const val TAP_PLAY_ONLY = "play_only"
    const val TAP_PLAY_NEXT = "play_next"
    const val TAP_ADD_TO_QUEUE = "add_to_queue"
    const val PIN_PLAYLIST = "playlist:"
    const val PIN_SMART = "smart:"

    val Fields = listOf(FIELD_ARTIST, FIELD_ALBUM, FIELD_ALBUM_ARTIST, FIELD_YEAR, FIELD_GENRE, FIELD_FORMAT, FIELD_FOLDER)
    val TapActions = listOf(TAP_PLAY_LIST, TAP_PLAY_ONLY, TAP_PLAY_NEXT, TAP_ADD_TO_QUEUE)
    val DefaultCategoryOrder = LibraryCategory.entries.map { it.key }
    val DefaultHomeOrder = listOf(
        HomePreferences.SECTION_CONTINUE,
        HomePreferences.SECTION_RECENTLY_PLAYED,
        HomePreferences.SECTION_MOST_PLAYED,
        HomePreferences.SECTION_RECENTLY_ADDED,
        HomePreferences.SECTION_PLAYLISTS
    )
    val DefaultFields = listOf(FIELD_ARTIST, FIELD_ALBUM)

    private fun prefs(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    private fun list(context: Context, key: String): List<String>? =
        prefs(context).getString(key, null)?.split(SEPARATOR)?.filter { it.isNotEmpty() }

    private fun ordered(stored: List<String>?, defaults: List<String>): List<String> {
        val known = stored.orEmpty().filter { it in defaults }.distinct()
        return known + defaults.filter { it !in known }
    }

    fun getCategoryOrder(context: Context): List<String> = ordered(list(context, KEY_CATEGORY_ORDER), DefaultCategoryOrder)

    fun getHiddenCategories(context: Context): Set<String> =
        list(context, KEY_HIDDEN_CATEGORIES).orEmpty().filter { it in DefaultCategoryOrder }.toSet()

    fun getHomeOrder(context: Context): List<String> = ordered(list(context, KEY_HOME_ORDER), DefaultHomeOrder)

    fun getHomePins(context: Context): List<String> =
        list(context, KEY_HOME_PINS).orEmpty().filter { it.startsWith(PIN_PLAYLIST) || it.startsWith(PIN_SMART) }.distinct()

    fun getSongTitle(context: Context): String =
        prefs(context).getString(KEY_SONG_TITLE, TITLE_TAG)?.takeIf { it == TITLE_TAG || it == TITLE_FILENAME } ?: TITLE_TAG

    fun getSongFields(context: Context): List<String> =
        list(context, KEY_SONG_FIELDS)?.filter { it in Fields }?.distinct() ?: DefaultFields

    fun getSongTrackNumber(context: Context): Boolean = prefs(context).getBoolean(KEY_SONG_TRACK_NUMBER, false)

    fun getSongTap(context: Context): String = prefs(context).getString(KEY_SONG_TAP, TAP_PLAY_LIST)?.takeIf { it in TapActions } ?: TAP_PLAY_LIST

    fun getTapOpensPlayer(context: Context): Boolean = prefs(context).getBoolean(KEY_TAP_OPENS_PLAYER, true)

    fun save(
        context: Context,
        categoryOrder: List<String>,
        hiddenCategories: Set<String>,
        homeOrder: List<String>,
        homePins: List<String>,
        songTitle: String,
        songFields: List<String>,
        songTrackNumber: Boolean,
        songTap: String,
        tapOpensPlayer: Boolean
    ) {
        prefs(context).edit()
            .putString(KEY_CATEGORY_ORDER, categoryOrder.joinToString(SEPARATOR))
            .putString(KEY_HIDDEN_CATEGORIES, hiddenCategories.joinToString(SEPARATOR))
            .putString(KEY_HOME_ORDER, homeOrder.joinToString(SEPARATOR))
            .putString(KEY_HOME_PINS, homePins.joinToString(SEPARATOR))
            .putString(KEY_SONG_TITLE, songTitle)
            .putString(KEY_SONG_FIELDS, songFields.joinToString(SEPARATOR))
            .putBoolean(KEY_SONG_TRACK_NUMBER, songTrackNumber)
            .putString(KEY_SONG_TAP, songTap)
            .putBoolean(KEY_TAP_OPENS_PLAYER, tapOpensPlayer)
            .apply()
    }
}
