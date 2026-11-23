package com.jhaiian.kate.ui.search

import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AlbumGroup
import com.jhaiian.kate.data.ArtistGroup
import com.jhaiian.kate.data.ArtistImage
import com.jhaiian.kate.data.ArtistImageStore
import com.jhaiian.kate.data.LibraryIndex
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.Playlist
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.data.SearchHistory
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.ui.library.SelectionOverlay
import com.jhaiian.kate.ui.library.selectionHighlight
import com.jhaiian.kate.ui.library.toggled
import com.jhaiian.kate.data.TrackGroup
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.AlbumArt
import com.jhaiian.kate.ui.components.ArtistArt
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.VerticalScrollbar
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.Normalizer

private const val ARTIST_PREVIEW = 4
private const val ALBUM_PREVIEW = 4
private const val SONG_PREVIEW = 12
private const val PLAYLIST_PREVIEW = 4
private const val GENRE_PREVIEW = 4
private const val SEARCH_DEBOUNCE_MS = 120L

private enum class SearchFilter(val key: String, val labelRes: Int) {
    ALL("all", R.string.search_filter_all),
    SONGS("songs", R.string.library_category_songs),
    ALBUMS("albums", R.string.library_category_albums),
    ARTISTS("artists", R.string.library_category_artists),
    PLAYLISTS("playlists", R.string.library_category_playlists),
    GENRES("genres", R.string.library_category_genres);

    companion object {
        fun fromKey(key: String): SearchFilter = entries.firstOrNull { it.key == key } ?: ALL
    }
}

private class PlaylistHit(val playlist: Playlist, val tracks: List<Track>) {
    val artTrack: Track? get() = tracks.firstOrNull { it.artKey != null } ?: tracks.firstOrNull()
}

private class SearchResults(
    val query: String,
    val terms: List<String>,
    val artists: List<ArtistGroup>,
    val albums: List<AlbumGroup>,
    val songs: List<Track>,
    val playlists: List<PlaylistHit>,
    val genres: List<TrackGroup>
) {
    val isEmpty: Boolean
        get() = artists.isEmpty() && albums.isEmpty() && songs.isEmpty() && playlists.isEmpty() && genres.isEmpty()

    fun count(filter: SearchFilter): Int = when (filter) {
        SearchFilter.ALL -> artists.size + albums.size + songs.size + playlists.size + genres.size
        SearchFilter.SONGS -> songs.size
        SearchFilter.ALBUMS -> albums.size
        SearchFilter.ARTISTS -> artists.size
        SearchFilter.PLAYLISTS -> playlists.size
        SearchFilter.GENRES -> genres.size
    }
}

private val MarksPattern = Regex("\\p{Mn}+")
private val SpacePattern = Regex("\\s+")

private fun normalizeForSearch(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD).replace(MarksPattern, "").lowercase()

private fun searchTerms(query: String): List<String> =
    normalizeForSearch(query).split(SpacePattern).filter { it.isNotEmpty() }

private fun runSearch(
    query: String,
    tracks: List<Track>,
    index: LibraryIndex,
    playlists: List<Playlist>
): SearchResults {
    val terms = searchTerms(query)
    if (terms.isEmpty()) {
        return SearchResults(query, terms, emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    }
    val first = terms.first()
    fun matches(vararg fields: String): Boolean {
        val haystack = fields.joinToString(" ") { normalizeForSearch(it) }
        return terms.all { haystack.contains(it) }
    }
    fun rank(name: String): Int = if (normalizeForSearch(name).startsWith(first)) 0 else 1
    val artists = index.artists
        .filter { it.name.isNotBlank() && matches(it.name) }
        .sortedBy { rank(it.name) }
    val albums = index.albums
        .filter { matches(it.title, it.artist.orEmpty()) }
        .sortedBy { rank(it.title) }
    val songs = tracks
        .filter { matches(it.title, it.artist, it.album) }
        .sortedBy { rank(it.title) }
    val playlistHits = playlists
        .filter { it.name.isNotBlank() && matches(it.name) }
        .sortedBy { rank(it.name) }
        .map { PlaylistHit(it, PlaylistStore.tracksOf(it, tracks)) }
    val genres = index.genres
        .filter { it.name.isNotBlank() && matches(it.name) }
        .sortedBy { rank(it.name) }
    return SearchResults(query, terms, artists, albums, songs, playlistHits, genres)
}

private fun highlightMatches(text: String, terms: List<String>, style: SpanStyle): AnnotatedString {
    if (terms.isEmpty() || text.isEmpty()) return AnnotatedString(text)
    val normalized = StringBuilder()
    val starts = ArrayList<Int>()
    val ends = ArrayList<Int>()
    var position = 0
    while (position < text.length) {
        val length = Character.charCount(text.codePointAt(position))
        val piece = normalizeForSearch(text.substring(position, position + length))
        for (character in piece) {
            normalized.append(character)
            starts.add(position)
            ends.add(position + length)
        }
        position += length
    }
    val haystack = normalized.toString()
    val ranges = ArrayList<IntRange>()
    for (term in terms) {
        var from = 0
        while (from <= haystack.length - term.length) {
            val at = haystack.indexOf(term, from)
            if (at < 0) break
            ranges.add(starts[at] until ends[at + term.length - 1])
            from = at + term.length
        }
    }
    if (ranges.isEmpty()) return AnnotatedString(text)
    val merged = ArrayList<IntRange>()
    for (range in ranges.sortedBy { it.first }) {
        val last = merged.lastOrNull()
        if (last != null && range.first <= last.last + 1) {
            merged[merged.size - 1] = last.first..maxOf(last.last, range.last)
        } else {
            merged.add(range)
        }
    }
    return buildAnnotatedString {
        append(text)
        merged.forEach { addStyle(style, it.first, it.last + 1) }
    }
}

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    bottomBarConnection: NestedScrollConnection? = null,
    onTrackClick: (List<Track>, Track) -> Unit = { _, _ -> },
    onAlbumClick: (String) -> Unit = {},
    onArtistClick: (String, Boolean) -> Unit = { _, _ -> },
    onPlaylistClick: (String) -> Unit = {},
    onGenreClick: (String) -> Unit = {}
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val keyboard = LocalSoftwareKeyboardController.current
    val bottomInset = LocalBottomContentPadding.current
    var field by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val query = field.text
    var filterKey by rememberSaveable { mutableStateOf(SearchFilter.ALL.key) }
    val filter = SearchFilter.fromKey(filterKey)
    var showAllArtists by rememberSaveable { mutableStateOf(false) }
    var showAllAlbums by rememberSaveable { mutableStateOf(false) }
    var showAllSongs by rememberSaveable { mutableStateOf(false) }
    var showAllPlaylists by rememberSaveable { mutableStateOf(false) }
    var showAllGenres by rememberSaveable { mutableStateOf(false) }
    val index = LibraryStore.index
    val playlists = PlaylistStore.playlists
    val listState = rememberLazyListState()
    val history = SearchHistory.entries
    var selectedIds by remember { mutableStateOf<Set<String>?>(null) }

    LaunchedEffect(Unit) {
        LibraryStore.ensureLoaded(context)
        PlaylistStore.ensureLoaded(context)
        SearchHistory.ensureLoaded(context)
    }

    val results by produceState<SearchResults?>(
        initialValue = null,
        query,
        System.identityHashCode(index),
        System.identityHashCode(playlists)
    ) {
        if (query.isBlank()) {
            value = null
            return@produceState
        }
        delay(SEARCH_DEBOUNCE_MS)
        val tracks = LibraryStore.tracks.toList()
        val currentQuery = query
        value = withContext(Dispatchers.Default) { runSearch(currentQuery, tracks, index, playlists) }
    }

    LaunchedEffect(query) {
        showAllArtists = false
        showAllAlbums = false
        showAllSongs = false
        showAllPlaylists = false
        showAllGenres = false
    }
    LaunchedEffect(filterKey) {
        if (listState.layoutInfo.totalItemsCount > 0) listState.scrollToItem(0)
    }

    val setQuery: (String) -> Unit = { text -> field = TextFieldValue(text, TextRange(text.length)) }
    val saveQuery: () -> Unit = { SearchHistory.record(context, query) }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 900.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.nav_search),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 16.dp)
                )
                TextField(
                    value = field,
                    onValueChange = { field = it },
                    singleLine = true,
                    placeholder = { Text(text = stringResource(R.string.search_hint), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { KateIcon(imageVector = Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            KateIconButton(onClick = { field = TextFieldValue() }) {
                                KateIcon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.search_clear))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        saveQuery()
                        keyboard?.hide()
                    }),
                    shape = RoundedCornerShape(28.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.cardBackground,
                        unfocusedContainerColor = colors.cardBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        cursorColor = colors.primary,
                        focusedLeadingIconColor = colors.primary,
                        unfocusedLeadingIconColor = colors.secondaryText,
                        focusedTrailingIconColor = colors.secondaryText,
                        unfocusedTrailingIconColor = colors.secondaryText,
                        focusedPlaceholderColor = colors.secondaryText,
                        unfocusedPlaceholderColor = colors.secondaryText
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                if (query.isNotBlank()) {
                    FilterChips(
                        results = results,
                        selected = filter,
                        onSelect = {
                            haptics.click()
                            filterKey = it.key
                        }
                    )
                }
            }

            val current = results
            when {
                query.isBlank() && history.isNotEmpty() -> RecentSearches(
                    entries = history,
                    bottomBarConnection = bottomBarConnection,
                    onPick = { entry ->
                        haptics.click()
                        keyboard?.hide()
                        setQuery(entry)
                        SearchHistory.record(context, entry)
                    },
                    onRemove = { entry ->
                        haptics.click()
                        SearchHistory.remove(context, entry)
                    },
                    onClear = {
                        haptics.confirm()
                        SearchHistory.clear(context)
                    },
                    modifier = Modifier.weight(1f)
                )
                query.isBlank() -> SearchMessage(
                    icon = Icons.Outlined.Search,
                    title = stringResource(R.string.placeholder_search_title),
                    message = stringResource(R.string.placeholder_search_message)
                )
                current == null -> Spacer(modifier = Modifier.weight(1f))
                current.isEmpty -> SearchMessage(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.search_no_results, current.query.trim()),
                    message = stringResource(R.string.search_no_results_hint)
                )
                current.count(filter) == 0 -> SearchMessage(
                    icon = Icons.Outlined.SearchOff,
                    title = stringResource(R.string.search_filter_empty_title),
                    message = stringResource(R.string.search_filter_empty_message)
                )
                else -> Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val currentTrackId = PlayerController.currentTrack?.id
                    val artistImages = ArtistImageStore.images
                    val terms = current.terms
                    val filtered = filter != SearchFilter.ALL
                    fun shows(section: SearchFilter) = filter == SearchFilter.ALL || filter == section
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it },
                        contentPadding = PaddingValues(top = 8.dp, bottom = bottomInset + 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (shows(SearchFilter.ARTISTS)) {
                            resultSection(
                                key = "artists",
                                titleRes = R.string.library_category_artists,
                                entries = current.artists,
                                limit = if (filtered) Int.MAX_VALUE else ARTIST_PREVIEW,
                                expanded = showAllArtists,
                                onToggle = { showAllArtists = !showAllArtists },
                                entryKey = { "artist_" + it.key }
                            ) { artist ->
                                SearchResultRow(
                                    title = artist.name,
                                    subtitle = pluralStringResource(R.plurals.library_song_count, artist.tracks.size, artist.tracks.size),
                                    terms = terms,
                                    highlightSubtitle = false,
                                    art = artist.artTrack,
                                    artistImage = artistImages[artist.key],
                                    icon = Icons.Filled.Mic,
                                    playing = currentTrackId != null && artist.tracks.any { it.id == currentTrackId },
                                    onClick = {
                                        saveQuery()
                                        keyboard?.hide()
                                        onArtistClick(artist.key, false)
                                    }
                                )
                            }
                        }
                        if (shows(SearchFilter.ALBUMS)) {
                            resultSection(
                                key = "albums",
                                titleRes = R.string.library_category_albums,
                                entries = current.albums,
                                limit = if (filtered) Int.MAX_VALUE else ALBUM_PREVIEW,
                                expanded = showAllAlbums,
                                onToggle = { showAllAlbums = !showAllAlbums },
                                entryKey = { "album_" + it.key }
                            ) { album ->
                                SearchResultRow(
                                    title = album.title.ifBlank { stringResource(R.string.player_unknown_album) },
                                    subtitle = album.artist?.ifBlank { stringResource(R.string.player_unknown_artist) }
                                        ?: stringResource(R.string.library_various_artists),
                                    terms = terms,
                                    highlightSubtitle = true,
                                    art = album.artTrack,
                                    artistImage = null,
                                    icon = Icons.Filled.Album,
                                    playing = currentTrackId != null && album.tracks.any { it.id == currentTrackId },
                                    onClick = {
                                        saveQuery()
                                        keyboard?.hide()
                                        onAlbumClick(album.key)
                                    }
                                )
                            }
                        }
                        if (shows(SearchFilter.SONGS)) {
                            resultSection(
                                key = "songs",
                                titleRes = R.string.library_category_songs,
                                entries = current.songs,
                                limit = if (filtered) Int.MAX_VALUE else SONG_PREVIEW,
                                expanded = showAllSongs,
                                onToggle = { showAllSongs = !showAllSongs },
                                entryKey = { "song_" + it.id }
                            ) { track ->
                                SearchResultRow(
                                    title = track.title,
                                    subtitle = listOf(
                                        track.artist.ifBlank { stringResource(R.string.player_unknown_artist) },
                                        track.album.ifBlank { stringResource(R.string.player_unknown_album) }
                                    ).joinToString(" • "),
                                    terms = terms,
                                    highlightSubtitle = true,
                                    art = track,
                                    artistImage = null,
                                    icon = Icons.Filled.MusicNote,
                                    playing = track.id == currentTrackId,
                                    selected = selectedIds?.contains(track.id) == true,
                                    onLongClick = {
                                        haptics.longPress()
                                        keyboard?.hide()
                                        selectedIds = (selectedIds ?: emptySet()) + track.id
                                    },
                                    onClick = {
                                        if (selectedIds != null) {
                                            haptics.tick()
                                            selectedIds = selectedIds.toggled(track.id)
                                        } else {
                                            saveQuery()
                                            keyboard?.hide()
                                            onTrackClick(current.songs, track)
                                        }
                                    }
                                )
                            }
                        }
                        if (shows(SearchFilter.PLAYLISTS)) {
                            resultSection(
                                key = "playlists",
                                titleRes = R.string.library_category_playlists,
                                entries = current.playlists,
                                limit = if (filtered) Int.MAX_VALUE else PLAYLIST_PREVIEW,
                                expanded = showAllPlaylists,
                                onToggle = { showAllPlaylists = !showAllPlaylists },
                                entryKey = { "playlist_" + it.playlist.id }
                            ) { hit ->
                                SearchResultRow(
                                    title = hit.playlist.name,
                                    subtitle = pluralStringResource(R.plurals.library_song_count, hit.tracks.size, hit.tracks.size),
                                    terms = terms,
                                    highlightSubtitle = false,
                                    art = hit.artTrack,
                                    artistImage = null,
                                    icon = Icons.AutoMirrored.Filled.QueueMusic,
                                    playing = currentTrackId != null && hit.tracks.any { it.id == currentTrackId },
                                    onClick = {
                                        saveQuery()
                                        keyboard?.hide()
                                        onPlaylistClick(hit.playlist.id)
                                    }
                                )
                            }
                        }
                        if (shows(SearchFilter.GENRES)) {
                            resultSection(
                                key = "genres",
                                titleRes = R.string.library_category_genres,
                                entries = current.genres,
                                limit = if (filtered) Int.MAX_VALUE else GENRE_PREVIEW,
                                expanded = showAllGenres,
                                onToggle = { showAllGenres = !showAllGenres },
                                entryKey = { "genre_" + it.key }
                            ) { genre ->
                                SearchResultRow(
                                    title = genre.name,
                                    subtitle = pluralStringResource(R.plurals.library_song_count, genre.tracks.size, genre.tracks.size),
                                    terms = terms,
                                    highlightSubtitle = false,
                                    art = genre.artTrack,
                                    artistImage = null,
                                    icon = Icons.Filled.Category,
                                    playing = currentTrackId != null && genre.tracks.any { it.id == currentTrackId },
                                    onClick = {
                                        saveQuery()
                                        keyboard?.hide()
                                        onGenreClick(genre.key)
                                    }
                                )
                            }
                        }
                    }
                    VerticalScrollbar(state = listState, thumbColor = colors.primary)
                }
            }
        }
        SelectionOverlay(
            selectedCount = selectedIds?.size,
            resolveTracks = {
                val ids = selectedIds.orEmpty()
                LibraryStore.tracks.filter { it.id in ids }
            },
            onSelectAll = { selectedIds = selectedIds.orEmpty() + results?.songs.orEmpty().map { it.id } },
            onClose = { selectedIds = null }
        )
    }
}

@Composable
private fun FilterChips(results: SearchResults?, selected: SearchFilter, onSelect: (SearchFilter) -> Unit) {
    val visible = SearchFilter.entries.filter { filter ->
        filter == SearchFilter.ALL || filter == selected || (results?.count(filter) ?: 0) > 0
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        items(visible, key = { it.key }) { filter ->
            SearchChip(
                label = stringResource(filter.labelRes),
                count = if (filter == SearchFilter.ALL) null else results?.count(filter),
                selected = filter == selected,
                onClick = { if (filter != selected) onSelect(filter) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun SearchChip(label: String, count: Int?, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val content = kateChoiceText(selected)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .kateChoice(selected, chip = true, unselected = colors.cardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = content,
            maxLines = 1
        )
        if (count != null) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = content.copy(alpha = 0.7f),
                maxLines = 1,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
private fun RecentSearches(
    entries: List<String>,
    bottomBarConnection: NestedScrollConnection?,
    onPick: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val bottomInset = LocalBottomContentPadding.current
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it },
        contentPadding = PaddingValues(top = 12.dp, bottom = bottomInset + 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "recent_header") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .widthIn(max = 900.dp)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 12.dp, bottom = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.search_recent_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                KateTextButton(
                    label = stringResource(R.string.search_recent_clear),
                    role = DialogButtonRole.LINK,
                    onClick = onClear
                )
            }
        }
        items(entries, key = { "recent_$it" }) { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .animateItem()
                    .widthIn(max = 900.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 1.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onPick(entry) }
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
            ) {
                KateIcon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = colors.secondaryText,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = entry,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )
                KateIconButton(onClick = { onRemove(entry) }) {
                    KateIcon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.search_recent_remove, entry),
                        tint = buttonTint(colors.secondaryText),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun <T> LazyListScope.resultSection(
    key: String,
    titleRes: Int,
    entries: List<T>,
    limit: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    entryKey: (T) -> String,
    row: @Composable (T) -> Unit
) {
    if (entries.isEmpty()) return
    item(key = "header_$key") {
        SectionTitle(
            title = stringResource(titleRes),
            count = entries.size,
            showToggle = entries.size > limit,
            expanded = expanded,
            onToggle = onToggle
        )
    }
    items(if (expanded) entries else entries.take(limit), key = entryKey) { entry ->
        Box(modifier = Modifier.animateItem()) { row(entry) }
    }
}

@Composable
private fun SectionTitle(title: String, count: Int, showToggle: Boolean, expanded: Boolean, onToggle: () -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .widthIn(max = 900.dp)
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)
            .defaultMinSize(minHeight = 40.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (showToggle) {
            KateTextButton(
                label = if (expanded) stringResource(R.string.search_show_less) else stringResource(R.string.search_show_all, count),
                role = DialogButtonRole.LINK,
                onClick = onToggle
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    title: String,
    subtitle: String,
    terms: List<String>,
    highlightSubtitle: Boolean,
    art: Track?,
    artistImage: ArtistImage?,
    icon: ImageVector,
    playing: Boolean,
    onClick: () -> Unit,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val colors = LocalClintColors.current
    val matchStyle = SpanStyle(
        color = colors.primary,
        fontWeight = FontWeight.Bold,
        background = colors.primary.copy(alpha = 0.14f)
    )
    val highlightedTitle = remember(title, terms, matchStyle) { highlightMatches(title, terms, matchStyle) }
    val highlightedSubtitle = remember(subtitle, terms, matchStyle, highlightSubtitle) {
        if (highlightSubtitle) highlightMatches(subtitle, terms, matchStyle) else AnnotatedString(subtitle)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .widthIn(max = 900.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (playing) colors.primary.copy(alpha = 0.14f) else Color.Transparent)
            .selectionHighlight(selected, colors.primary, 14.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val artModifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
        val placeholder: @Composable BoxScope.() -> Unit = {
            KateIcon(imageVector = icon, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(22.dp))
        }
        if (artistImage != null) {
            ArtistArt(image = artistImage, fallback = art, modifier = artModifier, placeholder = placeholder)
        } else if (art != null) {
            AlbumArt(track = art, modifier = artModifier, placeholder = placeholder)
        } else {
            Box(modifier = artModifier, contentAlignment = Alignment.Center, content = placeholder)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = highlightedTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (playing) colors.primary else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = highlightedSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun SearchMessage(icon: ImageVector, title: String, message: String) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = LocalBottomContentPadding.current)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KateIcon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
