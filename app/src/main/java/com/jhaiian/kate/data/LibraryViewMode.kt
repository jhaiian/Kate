package com.jhaiian.kate.data

enum class LibraryViewMode(val key: String) {
    GRID("grid"),
    GRID_SMALL("grid_small"),
    GRID_EXTRA_SMALL("grid_extra_small"),
    LIST_BIG("list_big"),
    LIST_LARGE("list_large"),
    LIST("list"),
    LIST_SMALL("list_small"),
    LIST_EXTRA_SMALL("list_extra_small"),
    LIST_COMPACT("list_compact"),
    LIST_NO_IMAGE("list_no_image");

    companion object {
        val Default = LIST_SMALL

        fun fromKey(key: String?): LibraryViewMode = entries.firstOrNull { it.key == key } ?: Default
    }
}
