package com.jhaiian.kate.data

import android.content.Context
import android.net.Uri
import androidx.preference.PreferenceManager

object LibraryPreferences {
    private const val KEY_FOLDERS = "music_folders"
    private const val KEY_SORT_ORDER = "music_sort_order"
    private const val KEY_SORT_DIRECTION = "music_sort_direction"
    private const val KEY_VIEW_MODE = "music_view_mode"
    private const val KEY_CATEGORY = "music_library_category"
    private const val ENTRY_DELIMITER = "\u001F"
    private const val FIELD_DELIMITER = "\u001E"

    fun getSortOrder(context: Context): LibrarySortOrder {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_SORT_ORDER, null)
        return LibrarySortOrder.fromKey(raw)
    }

    fun setSortOrder(context: Context, order: LibrarySortOrder) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_SORT_ORDER, order.key).apply()
    }

    fun getSortDirection(context: Context): SortDirection {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_SORT_DIRECTION, null)
        return SortDirection.fromKey(raw)
    }

    fun setSortDirection(context: Context, direction: SortDirection) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_SORT_DIRECTION, direction.key).apply()
    }

    fun getCategorySort(context: Context, category: LibraryCategory): Pair<LibrarySortOrder, SortDirection>? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val order = prefs.getString("${KEY_SORT_ORDER}_${category.key}", null) ?: return null
        return LibrarySortOrder.fromKey(order) to SortDirection.fromKey(prefs.getString("${KEY_SORT_DIRECTION}_${category.key}", null))
    }

    fun setCategorySort(context: Context, category: LibraryCategory, order: LibrarySortOrder, direction: SortDirection) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString("${KEY_SORT_ORDER}_${category.key}", order.key)
            .putString("${KEY_SORT_DIRECTION}_${category.key}", direction.key)
            .apply()
    }

    fun getViewMode(context: Context, category: LibraryCategory): LibraryViewMode {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(viewModeKey(category), null)
        return LibraryViewMode.entries.firstOrNull { it.key == raw } ?: defaultViewMode(category)
    }

    fun setViewMode(context: Context, category: LibraryCategory, mode: LibraryViewMode) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(viewModeKey(category), mode.key).apply()
    }

    fun getDetailViewMode(context: Context, page: String, default: LibraryViewMode): LibraryViewMode {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(detailViewModeKey(page), null)
        return LibraryViewMode.entries.firstOrNull { it.key == raw } ?: default
    }

    fun setDetailViewMode(context: Context, page: String, mode: LibraryViewMode) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(detailViewModeKey(page), mode.key).apply()
    }

    private fun detailViewModeKey(page: String): String = "${KEY_VIEW_MODE}_detail_$page"

    private fun viewModeKey(category: LibraryCategory): String =
        if (category == LibraryCategory.SONGS) KEY_VIEW_MODE else "${KEY_VIEW_MODE}_${category.key}"

    private fun defaultViewMode(category: LibraryCategory): LibraryViewMode =
        if (category == LibraryCategory.ALBUMS) LibraryViewMode.GRID else LibraryViewMode.Default

    fun getCategory(context: Context): LibraryCategory? {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_CATEGORY, null)
        return LibraryCategory.fromKey(raw)
    }

    fun setCategory(context: Context, category: LibraryCategory?) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        if (category == null) editor.remove(KEY_CATEGORY) else editor.putString(KEY_CATEGORY, category.key)
        editor.apply()
    }

    fun getFolders(context: Context): List<MusicFolder> {
        val raw = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_FOLDERS, null)
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(ENTRY_DELIMITER).mapNotNull { entry ->
            val parts = entry.split(FIELD_DELIMITER)
            if (parts.size < 4) return@mapNotNull null
            runCatching {
                MusicFolder(
                    uri = Uri.parse(parts[0]),
                    displayName = parts[1],
                    locationLabel = parts[2],
                    enabled = parts[3].toBoolean()
                )
            }.getOrNull()
        }
    }

    fun setFolders(context: Context, folders: List<MusicFolder>) {
        val raw = folders.joinToString(ENTRY_DELIMITER) { folder ->
            listOf(folder.uri.toString(), folder.displayName, folder.locationLabel, folder.enabled.toString())
                .joinToString(FIELD_DELIMITER)
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_FOLDERS, raw).apply()
    }
}
