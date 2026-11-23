package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class TagConfig(
    val artistSeparators: Set<String>,
    val genreSeparators: Set<String>,
    val unsplitArtists: List<String>,
    val groupAlbumsByMainArtist: Boolean
) {
    val unsplitByLength: List<String> = unsplitArtists.sortedByDescending { it.length }
    val artistPattern: Regex = separatorPattern(artistSeparators)
    val genrePattern: Regex = separatorPattern(genreSeparators)

    private companion object {
        fun separatorPattern(tokens: Set<String>): Regex {
            val symbols = tokens.filter { token -> token.none { it.isLetterOrDigit() } }
            val words = tokens.filter { token -> token.any { it.isLetterOrDigit() } }
            val symbolPart = (symbols.map { Regex.escape(it) } + "\u0000").joinToString("|")
            val wordPart = words.joinToString("|") { word ->
                if (word.endsWith('.')) Regex.escape(word.dropLast(1)) + "\\.?" else Regex.escape(word)
            }
            val pattern = if (wordPart.isEmpty()) {
                """\s*(?:$symbolPart)\s*"""
            } else {
                """\s*(?:$symbolPart|\s[(\[]?(?:$wordPart)\s)\s*"""
            }
            return Regex(pattern, RegexOption.IGNORE_CASE)
        }
    }
}

object TagSettings {
    val ArtistSeparatorOptions = listOf(",", ";", "/", "&", "+", "feat.", "ft.", "featuring", "x", "vs.", "with")
    val GenreSeparatorOptions = listOf(",", ";", "/", "&", "|", "+")
    val DefaultArtistSeparators = setOf(",", ";", "/", "feat.", "ft.", "featuring")
    val DefaultGenreSeparators = setOf(",", ";", "/")
    val DefaultUnsplitArtists = listOf(
        "Tyler, The Creator",
        "Earth, Wind & Fire",
        "Crosby, Stills, Nash & Young",
        "Crosby, Stills & Nash",
        "Emerson, Lake & Palmer",
        "Peter, Paul and Mary",
        "Blood, Sweat & Tears",
        "Black Country, New Road",
        "Now, Now",
        "AC/DC",
        "Simon & Garfunkel",
        "Hall & Oates",
        "Florence + the Machine",
        "Lil Nas X",
        "Panic! at the Disco"
    )
    const val MAX_SEPARATOR_LENGTH = 16
    private const val PREFS_NAME = "tag_settings"
    private const val KEY_ARTIST_SEPARATORS = "artist_separators"
    private const val KEY_GENRE_SEPARATORS = "genre_separators"
    private const val KEY_UNSPLIT_ARTISTS = "unsplit_artists"
    private const val KEY_GROUP_ALBUMS = "group_albums_by_main_artist"
    private const val LIST_SEPARATOR = "\n"

    val Defaults = TagConfig(DefaultArtistSeparators, DefaultGenreSeparators, DefaultUnsplitArtists, true)

    var config by mutableStateOf(Defaults)
        private set
    private var loaded = false

    val isDefault: Boolean get() = config == Defaults

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = prefs(context)
        config = TagConfig(
            artistSeparators = prefs.getString(KEY_ARTIST_SEPARATORS, null)?.let(::decodeSet) ?: DefaultArtistSeparators,
            genreSeparators = prefs.getString(KEY_GENRE_SEPARATORS, null)?.let(::decodeSet) ?: DefaultGenreSeparators,
            unsplitArtists = prefs.getString(KEY_UNSPLIT_ARTISTS, null)?.let(::decodeList) ?: DefaultUnsplitArtists,
            groupAlbumsByMainArtist = prefs.getBoolean(KEY_GROUP_ALBUMS, true)
        )
    }

    fun toggleArtistSeparator(context: Context, token: String) {
        ensureLoaded(context)
        update(context, config.copy(artistSeparators = config.artistSeparators.toggled(token)))
    }

    fun toggleGenreSeparator(context: Context, token: String) {
        ensureLoaded(context)
        update(context, config.copy(genreSeparators = config.genreSeparators.toggled(token)))
    }

    fun addArtistSeparator(context: Context, token: String) {
        ensureLoaded(context)
        val clean = cleanSeparator(token) ?: return
        update(context, config.copy(artistSeparators = config.artistSeparators + clean))
    }

    fun addGenreSeparator(context: Context, token: String) {
        ensureLoaded(context)
        val clean = cleanSeparator(token) ?: return
        update(context, config.copy(genreSeparators = config.genreSeparators + clean))
    }

    fun addUnsplitArtist(context: Context, name: String) {
        ensureLoaded(context)
        val clean = name.trim().replace(LIST_SEPARATOR, " ")
        if (clean.isEmpty() || config.unsplitArtists.any { it.equals(clean, ignoreCase = true) }) return
        update(context, config.copy(unsplitArtists = config.unsplitArtists + clean))
    }

    fun removeUnsplitArtist(context: Context, name: String) {
        ensureLoaded(context)
        update(context, config.copy(unsplitArtists = config.unsplitArtists - name))
    }

    fun setGroupAlbumsByMainArtist(context: Context, enabled: Boolean) {
        ensureLoaded(context)
        update(context, config.copy(groupAlbumsByMainArtist = enabled))
    }

    fun resetToDefaults(context: Context) {
        ensureLoaded(context)
        update(context, Defaults)
    }

    fun cleanSeparator(token: String): String? {
        val clean = token.trim().lowercase()
        if (clean.isEmpty() || clean.length > MAX_SEPARATOR_LENGTH || clean.contains(LIST_SEPARATOR)) return null
        return clean
    }

    private fun update(context: Context, next: TagConfig) {
        if (next == config) return
        config = next
        prefs(context).edit()
            .putString(KEY_ARTIST_SEPARATORS, next.artistSeparators.joinToString(LIST_SEPARATOR))
            .putString(KEY_GENRE_SEPARATORS, next.genreSeparators.joinToString(LIST_SEPARATOR))
            .putString(KEY_UNSPLIT_ARTISTS, next.unsplitArtists.joinToString(LIST_SEPARATOR))
            .putBoolean(KEY_GROUP_ALBUMS, next.groupAlbumsByMainArtist)
            .apply()
    }

    private fun Set<String>.toggled(token: String): Set<String> = if (token in this) this - token else this + token

    private fun decodeSet(raw: String): Set<String> =
        raw.split(LIST_SEPARATOR).filter { it.isNotEmpty() }.toCollection(LinkedHashSet())

    private fun decodeList(raw: String): List<String> =
        raw.split(LIST_SEPARATOR).filter { it.isNotBlank() }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
