package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.preference.PreferenceManager

object NavigationPreferences {
    private const val KEY_TAB_ORDER = "nav_tab_order"
    private const val KEY_HIDDEN_TABS = "nav_hidden_tabs"
    private const val KEY_LABELS = "nav_labels"
    private const val KEY_START_SCREEN = "start_screen"
    private const val KEY_MINI_PLAYER = "mini_player_style"
    private const val KEY_TWO_PANE = "nav_two_pane"
    private const val KEY_TWO_PANE_SPLIT = "nav_two_pane_split"
    private const val LIST_SEPARATOR = ","

    const val TAB_LIBRARY = "library"
    const val TAB_SEARCH = "search"
    const val TAB_STATS = "stats"
    const val TAB_SETTINGS = "settings"
    const val LABELS_ALWAYS = "always"
    const val LABELS_SELECTED = "selected"
    const val LABELS_NEVER = "never"
    const val MINI_PLAYER_FLOATING = "floating"
    const val MINI_PLAYER_DOCKED = "docked"

    val DEFAULT_TAB_ORDER = listOf(TAB_LIBRARY, TAB_SEARCH, TAB_STATS, TAB_SETTINGS)
    val LOCKED_TABS = setOf(TAB_LIBRARY, TAB_SETTINGS)
    val DEFAULT_HIDDEN_TABS = emptySet<String>()
    const val DEFAULT_LABELS = LABELS_ALWAYS
    const val DEFAULT_START_SCREEN = TAB_LIBRARY
    const val DEFAULT_MINI_PLAYER = MINI_PLAYER_FLOATING
    const val DEFAULT_TWO_PANE = true
    const val DEFAULT_TWO_PANE_SPLIT = 0.4f

    fun sanitizeOrder(order: List<String>): List<String> {
        val known = order.filter { it in DEFAULT_TAB_ORDER }.distinct()
        return known + DEFAULT_TAB_ORDER.filter { it !in known }
    }

    fun getTabOrder(context: Context): List<String> {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_TAB_ORDER, null)
        return sanitizeOrder(raw?.split(LIST_SEPARATOR).orEmpty())
    }

    fun getHiddenTabs(context: Context): Set<String> {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_HIDDEN_TABS, null)
        return raw?.split(LIST_SEPARATOR).orEmpty().filter { it in DEFAULT_TAB_ORDER && it !in LOCKED_TABS }.toSet()
    }

    fun getLabels(context: Context): String = getChoice(context, KEY_LABELS, DEFAULT_LABELS, NavLabelOptions.map { it.key })

    fun getStartScreen(context: Context): String =
        getChoice(context, KEY_START_SCREEN, DEFAULT_START_SCREEN, listOf(TAB_LIBRARY, TAB_SEARCH, TAB_STATS))

    fun getMiniPlayer(context: Context): String =
        getChoice(context, KEY_MINI_PLAYER, DEFAULT_MINI_PLAYER, MiniPlayerStyleOptions.map { it.key })

    fun getTwoPane(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_TWO_PANE, DEFAULT_TWO_PANE)

    fun getTwoPaneSplit(context: Context): Float =
        PreferenceManager.getDefaultSharedPreferences(context).getFloat(KEY_TWO_PANE_SPLIT, DEFAULT_TWO_PANE_SPLIT)
            .takeIf { it in 0.2f..0.7f } ?: DEFAULT_TWO_PANE_SPLIT

    fun setTwoPaneSplit(context: Context, split: Float) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putFloat(KEY_TWO_PANE_SPLIT, split.coerceIn(0.2f, 0.7f))
            .apply()
    }

    fun save(
        context: Context,
        tabOrder: List<String>,
        hiddenTabs: Set<String>,
        labels: String,
        startScreen: String,
        miniPlayer: String,
        twoPane: Boolean
    ) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(KEY_TAB_ORDER, tabOrder.joinToString(LIST_SEPARATOR))
            .putString(KEY_HIDDEN_TABS, hiddenTabs.joinToString(LIST_SEPARATOR))
            .putString(KEY_LABELS, labels)
            .putString(KEY_START_SCREEN, startScreen)
            .putString(KEY_MINI_PLAYER, miniPlayer)
            .putBoolean(KEY_TWO_PANE, twoPane)
            .apply()
    }

    private fun getChoice(context: Context, key: String, default: String, allowed: List<String>): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, default) ?: default
        return if (value in allowed) value else default
    }
}
