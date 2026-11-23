package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object SystemControls {
    private const val PREFS_NAME = "system_controls"
    private const val KEY_NOTIFICATION_1 = "notification_button_1"
    private const val KEY_NOTIFICATION_2 = "notification_button_2"
    private const val KEY_HEADSET_SINGLE = "headset_single"
    private const val KEY_HEADSET_DOUBLE = "headset_double"
    private const val KEY_HEADSET_TRIPLE = "headset_triple"
    private const val KEY_HEADSET_LONG = "headset_long"
    private const val KEY_AUTO_HIDDEN = "auto_hidden"

    const val NONE = "none"
    const val PLAY_PAUSE = "play_pause"
    const val NEXT = "next"
    const val PREVIOUS = "previous"
    const val REWIND = "rewind"
    const val FORWARD = "forward"
    const val FAVORITE = "favorite"
    const val STOP = "stop"

    const val AUTO_SHUFFLE = "shuffle_all"
    const val AUTO_SONGS = "songs"
    const val AUTO_ALBUMS = "albums"
    const val AUTO_ARTISTS = "artists"
    const val AUTO_PLAYLISTS = "playlists"
    const val AUTO_FAVORITES = "favorites"

    const val COMMAND_PREFIX = "com.jhaiian.kate.command."
    const val SEEK_STEP_MS = 10_000L

    val NotificationActions = listOf(NONE, FAVORITE, REWIND, FORWARD, STOP)
    val HeadsetActions = listOf(NONE, PLAY_PAUSE, NEXT, PREVIOUS, REWIND, FORWARD, FAVORITE)
    val AutoSections = listOf(AUTO_SHUFFLE, AUTO_SONGS, AUTO_ALBUMS, AUTO_ARTISTS, AUTO_PLAYLISTS, AUTO_FAVORITES)
    val CommandActions = listOf(FAVORITE, REWIND, FORWARD, STOP)

    private var loaded = false

    var notificationFirst by mutableStateOf(NONE)
        private set
    var notificationSecond by mutableStateOf(NONE)
        private set
    var headsetSingle by mutableStateOf(PLAY_PAUSE)
        private set
    var headsetDouble by mutableStateOf(NEXT)
        private set
    var headsetTriple by mutableStateOf(PREVIOUS)
        private set
    var headsetLong by mutableStateOf(NONE)
        private set
    var autoHidden by mutableStateOf<Set<String>>(emptySet())
        private set

    val headsetCustomized: Boolean
        get() = headsetSingle != PLAY_PAUSE || headsetDouble != NEXT || headsetTriple != PREVIOUS || headsetLong != NONE

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = prefs(context)
        notificationFirst = prefs.getString(KEY_NOTIFICATION_1, NONE)?.takeIf { it in NotificationActions } ?: NONE
        notificationSecond = prefs.getString(KEY_NOTIFICATION_2, NONE)?.takeIf { it in NotificationActions } ?: NONE
        headsetSingle = prefs.getString(KEY_HEADSET_SINGLE, PLAY_PAUSE)?.takeIf { it in HeadsetActions } ?: PLAY_PAUSE
        headsetDouble = prefs.getString(KEY_HEADSET_DOUBLE, NEXT)?.takeIf { it in HeadsetActions } ?: NEXT
        headsetTriple = prefs.getString(KEY_HEADSET_TRIPLE, PREVIOUS)?.takeIf { it in HeadsetActions } ?: PREVIOUS
        headsetLong = prefs.getString(KEY_HEADSET_LONG, NONE)?.takeIf { it in HeadsetActions } ?: NONE
        autoHidden = prefs.getStringSet(KEY_AUTO_HIDDEN, null)?.filter { it in AutoSections }?.toSet().orEmpty()
    }

    fun setNotificationButtons(context: Context, first: String, second: String) {
        ensureLoaded(context)
        notificationFirst = first.takeIf { it in NotificationActions } ?: NONE
        notificationSecond = second.takeIf { it in NotificationActions } ?: NONE
        prefs(context).edit().putString(KEY_NOTIFICATION_1, notificationFirst).putString(KEY_NOTIFICATION_2, notificationSecond).apply()
    }

    fun setHeadset(context: Context, single: String, double: String, triple: String, long: String) {
        ensureLoaded(context)
        headsetSingle = single.takeIf { it in HeadsetActions } ?: PLAY_PAUSE
        headsetDouble = double.takeIf { it in HeadsetActions } ?: NEXT
        headsetTriple = triple.takeIf { it in HeadsetActions } ?: PREVIOUS
        headsetLong = long.takeIf { it in HeadsetActions } ?: NONE
        prefs(context).edit()
            .putString(KEY_HEADSET_SINGLE, headsetSingle)
            .putString(KEY_HEADSET_DOUBLE, headsetDouble)
            .putString(KEY_HEADSET_TRIPLE, headsetTriple)
            .putString(KEY_HEADSET_LONG, headsetLong)
            .apply()
    }

    fun setAutoHidden(context: Context, value: Set<String>) {
        ensureLoaded(context)
        autoHidden = value.filter { it in AutoSections }.toSet()
        prefs(context).edit().putStringSet(KEY_AUTO_HIDDEN, HashSet(autoHidden)).apply()
    }

    fun autoVisible(section: String): Boolean = section !in autoHidden

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
