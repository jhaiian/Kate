package com.jhaiian.kate.ui.library

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.pressIndication
import com.jhaiian.kate.ui.components.katePressable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.jhaiian.kate.ui.components.KateIconButton
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.JumpToPlayingButton
import com.jhaiian.kate.ui.components.isItemInView
import androidx.compose.foundation.gestures.stopScroll
import android.content.Context
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import com.jhaiian.kate.data.LibraryPreferences
import com.jhaiian.kate.data.LibraryViewMode
import kotlin.math.roundToInt
import com.jhaiian.kate.data.ArtistImage
import com.jhaiian.kate.data.ArtistImageSource
import com.jhaiian.kate.data.ArtistImageStore
import com.jhaiian.kate.data.ArtistNames
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.PlaylistFiles
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.data.SmartPlaylist
import com.jhaiian.kate.data.SmartPlaylistStore
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.LocalNavAnimatedScope
import com.jhaiian.kate.ui.components.ReorderableTrackList
import com.jhaiian.kate.ui.components.ShufflePickRequest
import com.jhaiian.kate.ui.components.RadioRequest
import com.jhaiian.kate.playback.RadioSeed
import com.jhaiian.kate.playback.RadioSeedType
import androidx.compose.material.icons.filled.Radio
import com.jhaiian.kate.ui.components.VerticalScrollbar
import com.jhaiian.kate.ui.components.sharedBoundsIn
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.theme.LibraryStylePreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AlbumDetailScreen(
    albumKey: String,
    bottomBarConnection: NestedScrollConnection?,
    onBack: () -> Unit,
    onTrackClick: (List<Track>, Track) -> Unit,
    onArtistClick: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val album = LibraryStore.index.album(albumKey)
    var addToPlaylistIds by remember { mutableStateOf<List<String>?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>?>(null) }
    var showViewModes by remember { mutableStateOf(false) }
    val viewMode = rememberDetailViewMode(DetailPageAlbum, LibraryViewMode.LIST_NO_IMAGE)
    val haptics = LocalKateHaptics.current
    val tracks = album?.tracks.orEmpty()
    val tiles = rememberNumberedSongTiles(tracks)
    MorphDetailScaffold(
        onBack = onBack,
        bottomBarConnection = bottomBarConnection,
        title = album?.let { albumTitle(it) },
        tiles = tiles,
        viewMode = viewMode,
        pinchEnabled = !showViewModes && addToPlaylistIds == null,
        isSelected = { id -> selectedIds?.contains(id) == true },
        onTileClick = { position, id ->
            if (selectedIds != null) {
                haptics.tick()
                selectedIds = selectedIds.toggled(id)
            } else {
                tracks.getOrNull(position)?.let { onTrackClick(tracks, it) }
            }
        },
        onTileLongClick = { id ->
            haptics.longPress()
            selectedIds = (selectedIds ?: emptySet()) + id
        },
        actions = {
            if (album != null) {
                DetailViewModeButton(mode = viewMode.mode, onClick = { showViewModes = true })
                val label = albumTitle(album)
                StartRadioButton { RadioRequest.open(RadioSeed(RadioSeedType.ALBUM, album.key, label)) }
                AddAllToPlaylistButton { addToPlaylistIds = album.tracks.map { it.id } }
            }
        },
        overlay = { morph ->
            AddToPlaylistDialog(trackIds = addToPlaylistIds, onDismiss = { addToPlaylistIds = null })
            ViewModeOptionsDialog(
                visible = showViewModes,
                currentMode = viewMode.mode,
                onDismiss = { showViewModes = false },
                onSelect = { mode ->
                    showViewModes = false
                    if (!morph.morphTo(mode)) viewMode.select(mode)
                }
            )
            SelectionOverlay(
                selectedCount = selectedIds?.size,
                resolveTracks = { tracks.filter { it.id in selectedIds.orEmpty() } },
                onSelectAll = { selectedIds = tracks.mapTo(HashSet()) { it.id } },
                onClose = { selectedIds = null }
            )
        },
        hero = {
            if (album != null) {
                val artist = album.artist
                DetailHeader(
                    sharedKey = albumArtKey(album.key),
                    artTrack = album.artTrack,
                    icon = Icons.Filled.Album,
                    title = albumTitle(album),
                    subtitle = albumArtistLabel(album),
                    onSubtitleClick = if (artist.isNullOrBlank()) null else { { onArtistClick(ArtistNames.key(artist), true) } },
                    meta = joinMeta(
                        album.year?.toString().orEmpty(),
                        pluralStringResource(R.plurals.library_song_count, album.tracks.size, album.tracks.size),
                        durationText(album.durationMs)
                    ),
                    onPlay = { PlayerController.playAll(context, album.tracks) },
                    onShuffle = { PlayerController.playShuffled(context, album.tracks) },
                    shuffleTracks = { album.tracks }
                )
            }
        }
    )
}

@Composable
fun ArtistDetailScreen(
    artistKey: String,
    albumArtist: Boolean,
    bottomBarConnection: NestedScrollConnection?,
    onBack: () -> Unit,
    onTrackClick: (List<Track>, Track) -> Unit,
    onAlbumClick: (String) -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val artist = LibraryStore.index.artist(artistKey, albumArtist)
    val artistImage = ArtistImageStore.images[artistKey]
    val imageSource = ArtistImageStore.sourceOf(artistKey)
    var showImageMenu by remember { mutableStateOf(false) }
    var addToPlaylistIds by remember { mutableStateOf<List<String>?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>?>(null) }
    var showViewModes by remember { mutableStateOf(false) }
    val viewMode = rememberDetailViewMode(
        if (albumArtist) DetailPageAlbumArtist else DetailPageArtist,
        LibraryViewMode.LIST_SMALL
    )
    val haptics = LocalKateHaptics.current
    val tracks = artist?.tracks.orEmpty()
    val tiles = rememberSongTiles(tracks)
    val failedMessage = stringResource(R.string.artist_image_failed)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                if (!ArtistImageStore.setCustomImage(context, artistKey, uri)) {
                    Toast.makeText(context, failedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    MorphDetailScaffold(
        onBack = onBack,
        bottomBarConnection = bottomBarConnection,
        title = artist?.let { artistName(it) },
        tiles = tiles,
        viewMode = viewMode,
        pinchEnabled = !showViewModes && !showImageMenu && addToPlaylistIds == null,
        isSelected = { id -> selectedIds?.contains(id) == true },
        onTileClick = { position, id ->
            if (selectedIds != null) {
                haptics.tick()
                selectedIds = selectedIds.toggled(id)
            } else {
                tracks.getOrNull(position)?.let { onTrackClick(tracks, it) }
            }
        },
        onTileLongClick = { id ->
            haptics.longPress()
            selectedIds = (selectedIds ?: emptySet()) + id
        },
        actions = {
            if (artist != null) {
                DetailViewModeButton(mode = viewMode.mode, onClick = { showViewModes = true })
                val label = artistName(artist)
                StartRadioButton { RadioRequest.open(RadioSeed(RadioSeedType.ARTIST, artistKey, label)) }
                AddAllToPlaylistButton { addToPlaylistIds = artist.tracks.map { it.id } }
            }
            if (artist != null && artist.name.isNotBlank()) {
                KateIconButton(onClick = { showImageMenu = true }) {
                    KateIcon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.library_menu),
                        tint = colors.primary
                    )
                }
            }
        },
        overlay = { morph ->
            ArtistImageMenu(
                visible = showImageMenu,
                hasImage = artistImage != null,
                canReset = imageSource == ArtistImageSource.CUSTOM || imageSource == ArtistImageSource.REMOVED,
                onDismiss = { showImageMenu = false },
                onChoose = {
                    showImageMenu = false
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onRemove = {
                    showImageMenu = false
                    ArtistImageStore.removeImage(context, artistKey)
                },
                onReset = {
                    showImageMenu = false
                    ArtistImageStore.restoreImage(context, artistKey)
                }
            )
            AddToPlaylistDialog(trackIds = addToPlaylistIds, onDismiss = { addToPlaylistIds = null })
            ViewModeOptionsDialog(
                visible = showViewModes,
                currentMode = viewMode.mode,
                onDismiss = { showViewModes = false },
                onSelect = { mode ->
                    showViewModes = false
                    if (!morph.morphTo(mode)) viewMode.select(mode)
                }
            )
            SelectionOverlay(
                selectedCount = selectedIds?.size,
                resolveTracks = { tracks.filter { it.id in selectedIds.orEmpty() } },
                onSelectAll = { selectedIds = tracks.mapTo(HashSet()) { it.id } },
                onClose = { selectedIds = null }
            )
        },
        hero = {
            if (artist != null) {
                DetailHeader(
                    sharedKey = artistArtKey(artist.key, albumArtist),
                    artTrack = artist.artTrack,
                    artistImage = artistImage,
                    icon = if (albumArtist) LibraryCategory.ALBUM_ARTISTS.icon else LibraryCategory.ARTISTS.icon,
                    title = artistName(artist),
                    subtitle = null,
                    onSubtitleClick = null,
                    meta = joinMeta(
                        pluralStringResource(R.plurals.library_album_count, artist.albums.size, artist.albums.size),
                        pluralStringResource(R.plurals.library_song_count, artist.tracks.size, artist.tracks.size)
                    ),
                    onPlay = { PlayerController.playAll(context, artist.tracks) },
                    onShuffle = { PlayerController.playShuffled(context, artist.tracks) },
                    shuffleTracks = { artist.tracks }
                )
            }
        },
        extra = {
            if (artist != null) {
                if (artist.albums.isNotEmpty()) {
                    DetailSectionTitle(text = stringResource(R.string.library_category_albums))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(artist.albums, key = { it.key }) { album ->
                            AlbumCard(
                                album = album,
                                onClick = { onAlbumClick(album.key) },
                                modifier = Modifier.width(152.dp)
                            )
                        }
                    }
                }
                DetailSectionTitle(text = stringResource(R.string.library_category_songs))
            }
        }
    )
}

@Composable
fun GroupDetailScreen(
    category: LibraryCategory,
    groupKey: String,
    bottomBarConnection: NestedScrollConnection?,
    onBack: () -> Unit,
    onTrackClick: (List<Track>, Track) -> Unit
) {
    val context = LocalContext.current
    val group = LibraryStore.index.group(category, groupKey)
    var addToPlaylistIds by remember { mutableStateOf<List<String>?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>?>(null) }
    val haptics = LocalKateHaptics.current
    val currentId = PlayerController.currentTrack?.id
    val groupPlayingIndex = group?.tracks?.indexOfFirst { it.id == currentId } ?: -1
    DetailScaffold(
        onBack = onBack,
        bottomBarConnection = bottomBarConnection,
        title = group?.let { groupName(category, it) },
        jumpIndex = if (groupPlayingIndex >= 0) groupPlayingIndex + 2 else -1,
        actions = {
            if (group != null) {
                if (category == LibraryCategory.GENRES) {
                    val label = groupName(category, group)
                    StartRadioButton { RadioRequest.open(RadioSeed(RadioSeedType.GENRE, group.key, label)) }
                }
                AddAllToPlaylistButton { addToPlaylistIds = group.tracks.map { it.id } }
            }
        },
        overlay = {
            AddToPlaylistDialog(trackIds = addToPlaylistIds, onDismiss = { addToPlaylistIds = null })
            SelectionOverlay(
                selectedCount = selectedIds?.size,
                resolveTracks = { group?.tracks.orEmpty().filter { it.id in selectedIds.orEmpty() } },
                onSelectAll = { selectedIds = group?.tracks?.mapTo(HashSet()) { it.id } },
                onClose = { selectedIds = null }
            )
        }
    ) {
        if (group == null) return@DetailScaffold
        item(key = "group_header") {
            DetailHeader(
                sharedKey = groupArtKey(category, group.key),
                artTrack = if (category == LibraryCategory.GENRES) group.artTrack else null,
                icon = category.icon,
                title = groupName(category, group),
                subtitle = if (category == LibraryCategory.FOLDERS) group.detail else null,
                onSubtitleClick = null,
                meta = joinMeta(
                    pluralStringResource(R.plurals.library_song_count, group.tracks.size, group.tracks.size),
                    durationText(group.tracks.sumOf { it.durationMs })
                ),
                onPlay = { PlayerController.playAll(context, group.tracks) },
                onShuffle = { PlayerController.playShuffled(context, group.tracks) },
                shuffleTracks = { group.tracks }
            )
        }
        items(group.tracks, key = { "track_" + it.id }) { track ->
            DetailTrackRow(
                track = track,
                leading = null,
                onClick = {
                    if (selectedIds != null) {
                        haptics.tick()
                        selectedIds = selectedIds.toggled(track.id)
                    } else {
                        onTrackClick(group.tracks, track)
                    }
                },
                modifier = Modifier.animateItem(),
                onLongClick = {
                    haptics.longPress()
                    selectedIds = (selectedIds ?: emptySet()) + track.id
                },
                selected = selectedIds?.contains(track.id) == true
            )
        }
    }
}

private val DetailRowCorner = 14.dp

@Composable
private fun StartRadioButton(onClick: () -> Unit) {
    val colors = LocalClintColors.current
    KateIconButton(onClick = onClick) {
        KateIcon(
            imageVector = Icons.Filled.Radio,
            contentDescription = stringResource(R.string.radio_start),
            tint = colors.primary
        )
    }
}

@Composable
private fun AddAllToPlaylistButton(onClick: () -> Unit) {
    val colors = LocalClintColors.current
    KateIconButton(onClick = onClick) {
        KateIcon(
            imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
            contentDescription = stringResource(R.string.playlist_add_to),
            tint = colors.primary
        )
    }
}

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    bottomBarConnection: NestedScrollConnection?,
    onBack: () -> Unit,
    onTrackClick: (List<Track>, Track) -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    val bottomInset = LocalBottomContentPadding.current
    val playlist = PlaylistStore.playlist(playlistId)
    val entries by remember(playlistId) {
        derivedStateOf<List<Pair<Int, Track>>> {
            val current = PlaylistStore.playlist(playlistId) ?: return@derivedStateOf emptyList()
            val byId = LibraryStore.tracks.associateBy { it.id }
            current.trackIds.mapIndexedNotNull { index, id -> byId[id]?.let { index to it } }
        }
    }
    val tracks = remember(entries) { entries.map { it.second } }
    var showMenu by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var topBarHeight by remember { mutableIntStateOf(0) }
    val parallax = rememberHeaderParallax(listState) { topBarHeight }
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex >= CollapsedBarItemIndex } }
    val exportedMessage = stringResource(R.string.playlist_exported)
    val exportFailedMessage = stringResource(R.string.playlist_export_failed)
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(PlaylistFiles.EXPORT_MIME_TYPE)
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val snapshot = tracks
        scope.launch {
            val written = withContext(Dispatchers.IO) { PlaylistFiles.write(context, uri, snapshot) }
            Toast.makeText(context, if (written) exportedMessage else exportFailedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    if (playlist == null) {
        LaunchedEffect(Unit) { onBack() }
        Box(modifier = Modifier.fillMaxSize().background(colors.background))
        return
    }

    val currentId = PlayerController.currentTrack?.id
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        CompositionLocalProvider(LocalHeaderParallax provides parallax) {
            ReorderableTrackList(
                tracks = tracks,
                currentIndex = tracks.indexOfFirst { it.id == currentId },
                currentLabel = null,
                onPlay = { index ->
                    tracks.getOrNull(index)?.let { track ->
                        haptics.click()
                        onTrackClick(tracks, track)
                    }
                },
                onMove = { from, to ->
                    val fromIndex = entries.getOrNull(from)?.first
                    val toIndex = entries.getOrNull(to)?.first
                    if (fromIndex != null && toIndex != null) PlaylistStore.moveTrack(playlistId, fromIndex, toIndex)
                },
                onRemove = { index ->
                    entries.getOrNull(index)?.first?.let { PlaylistStore.removeTrackAt(playlistId, it) }
                },
                canRemove = { true },
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it },
                listState = listState,
                contentPadding = PaddingValues(bottom = bottomInset),
                header = {
                    item(key = "playlist_top_bar") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .widthIn(max = 900.dp)
                                .fillMaxWidth()
                                .onSizeChanged { topBarHeight = it.height }
                                .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                        ) {
                            KateIconButton(onClick = onBack) {
                                KateIcon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.library_back),
                                    tint = colors.primary
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            KateIconButton(onClick = { showMenu = true }) {
                                KateIcon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.library_menu),
                                    tint = colors.primary
                                )
                            }
                        }
                    }
                    item(key = "playlist_header") {
                        DetailHeader(
                            sharedKey = playlistArtKey(playlist.id),
                            artTrack = tracks.firstOrNull { it.artKey != null } ?: tracks.firstOrNull(),
                            icon = LibraryCategory.PLAYLISTS.icon,
                            title = playlist.name,
                            subtitle = null,
                            onSubtitleClick = null,
                            meta = joinMeta(
                                pluralStringResource(R.plurals.library_song_count, tracks.size, tracks.size),
                                if (tracks.isEmpty()) "" else durationText(tracks.sumOf { it.durationMs })
                            ),
                            onPlay = { if (tracks.isNotEmpty()) PlayerController.playAll(context, tracks) },
                            onShuffle = { if (tracks.isNotEmpty()) PlayerController.playShuffled(context, tracks) },
                            shuffleTracks = { tracks }
                        )
                    }
                    if (tracks.isEmpty()) {
                        item(key = "playlist_empty") {
                            Text(
                                text = stringResource(R.string.playlist_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.secondaryText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .widthIn(max = 520.dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 32.dp)
                            )
                        }
                    }
                },
                footer = {
                    item(key = "playlist_bottom_space") { Spacer(modifier = Modifier.height(24.dp)) }
                }
            )
        }
        VerticalScrollbar(
            state = listState,
            thumbColor = colors.primary
        )
        val playlistPlayingIndex = tracks.indexOfFirst { it.id == currentId }
        val playlistJumpIndex = if (playlistPlayingIndex >= 0) playlistPlayingIndex + 2 else -1
        val playlistBottomPx = with(LocalDensity.current) { bottomInset.roundToPx() }
        val playlistJumpVisible by remember(playlistJumpIndex, playlistBottomPx) {
            derivedStateOf { playlistJumpIndex >= 0 && !listState.isItemInView(playlistJumpIndex, playlistBottomPx) }
        }
        JumpToPlayingButton(
            visible = playlistJumpVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = bottomInset + 16.dp),
            onClick = { scope.launch { listState.animateScrollToItem(maxOf(0, playlistJumpIndex - 2)) } }
        )
        CollapsedDetailBar(visible = collapsed, title = playlist.name, onBack = onBack) {
            KateIconButton(onClick = { showMenu = true }) {
                KateIcon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.library_menu),
                    tint = colors.primary
                )
            }
        }
        PlaylistMenu(
            visible = showMenu,
            name = playlist.name,
            onDismiss = { showMenu = false },
            onRename = {
                showMenu = false
                renaming = true
            },
            onExport = {
                showMenu = false
                exportLauncher.launch("${playlist.name}.${PlaylistFiles.EXPORT_EXTENSION}")
            },
            onDelete = {
                showMenu = false
                deleting = true
            }
        )
        PlaylistNameDialog(
            visible = renaming,
            title = stringResource(R.string.playlist_rename),
            initialName = playlist.name,
            confirmLabel = stringResource(R.string.playlist_save),
            onConfirm = { name ->
                PlaylistStore.rename(playlistId, name)
                renaming = false
            },
            onDismiss = { renaming = false }
        )
        DeletePlaylistDialog(
            visible = deleting,
            name = playlist.name,
            onConfirm = {
                deleting = false
                haptics.confirm()
                onBack()
                PlaylistStore.delete(playlistId)
            },
            onDismiss = { deleting = false }
        )
    }
}

@Composable
fun SmartPlaylistDetailScreen(
    smartId: String,
    bottomBarConnection: NestedScrollConnection?,
    onBack: () -> Unit,
    onTrackClick: (List<Track>, Track) -> Unit,
    onOpenSmartPlaylist: (String) -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    SmartPlaylistsLoader()
    SmartPlaylistStore.ensureLoaded(context)
    val playlist = SmartPlaylistStore.get(smartId)
    val tracks = rememberSmartTracks(playlist)
    var addToPlaylistIds by remember { mutableStateOf<List<String>?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<SmartPlaylist?>(null) }
    var deleting by remember { mutableStateOf(false) }
    if (playlist == null) {
        LaunchedEffect(Unit) { onBack() }
        Box(modifier = Modifier.fillMaxSize().background(colors.background))
        return
    }
    val name = smartPlaylistName(playlist)
    val frozenMessage = stringResource(R.string.smart_frozen, name)
    val smartPlayingIndex = tracks.indexOfFirst { it.id == PlayerController.currentTrack?.id }
    val smartLeading = if (playlist.usesPlays && !ListeningHistory.enabled) 3 else 2
    DetailScaffold(
        onBack = onBack,
        bottomBarConnection = bottomBarConnection,
        title = name,
        jumpIndex = if (smartPlayingIndex >= 0) smartPlayingIndex + smartLeading else -1,
        actions = {
            AddAllToPlaylistButton { addToPlaylistIds = tracks.map { it.id } }
            KateIconButton(onClick = { showMenu = true }) {
                KateIcon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.library_menu),
                    tint = colors.primary
                )
            }
        },
        overlay = {
            AddToPlaylistDialog(trackIds = addToPlaylistIds, onDismiss = { addToPlaylistIds = null })
            SelectionOverlay(
                selectedCount = selectedIds?.size,
                resolveTracks = { tracks.filter { it.id in selectedIds.orEmpty() } },
                onSelectAll = { selectedIds = tracks.mapTo(HashSet()) { it.id } },
                onClose = { selectedIds = null }
            )
            SmartPlaylistMenu(
                visible = showMenu,
                name = name,
                isDecade = playlist.isDecade,
                onDismiss = { showMenu = false },
                onEdit = {
                    showMenu = false
                    editing = playlist
                },
                onFreeze = {
                    showMenu = false
                    if (tracks.isNotEmpty()) {
                        PlaylistStore.create(context, name, tracks.map { it.id })
                        haptics.confirm()
                        Toast.makeText(context, frozenMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = {
                    showMenu = false
                    deleting = true
                }
            )
            SmartPlaylistEditor(
                initial = editing,
                onDismiss = { editing = null },
                onSaved = { saved ->
                    editing = null
                    if (saved.id != playlist.id) onOpenSmartPlaylist(saved.id)
                }
            )
            DeletePlaylistDialog(
                visible = deleting,
                name = name,
                onConfirm = {
                    deleting = false
                    haptics.confirm()
                    SmartPlaylistStore.delete(playlist.id)
                },
                onDismiss = { deleting = false }
            )
        }
    ) {
        item(key = "smart_header") {
            DetailHeader(
                sharedKey = smartArtKey(playlist.id),
                artTrack = tracks.firstOrNull { it.artKey != null },
                icon = smartPlaylistIcon(playlist),
                title = name,
                subtitle = stringResource(R.string.smart_auto_updates),
                onSubtitleClick = { editing = playlist },
                meta = joinMeta(
                    pluralStringResource(R.plurals.library_song_count, tracks.size, tracks.size),
                    durationText(tracks.sumOf { it.durationMs })
                ),
                onPlay = { if (tracks.isNotEmpty()) PlayerController.playAll(context, tracks) },
                onShuffle = { if (tracks.isNotEmpty()) PlayerController.playShuffled(context, tracks) },
                shuffleTracks = { tracks }
            )
        }
        if (playlist.usesPlays && !ListeningHistory.enabled) {
            item(key = "smart_history_off") {
                Text(
                    text = stringResource(R.string.smart_history_off),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.colorError,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }
        }
        if (tracks.isEmpty()) {
            item(key = "smart_empty") {
                Text(
                    text = stringResource(R.string.smart_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 32.dp)
                )
            }
        }
        items(tracks, key = { "track_" + it.id }) { track ->
            DetailTrackRow(
                track = track,
                leading = null,
                onClick = {
                    if (selectedIds != null) {
                        haptics.tick()
                        selectedIds = selectedIds.toggled(track.id)
                    } else {
                        onTrackClick(tracks, track)
                    }
                },
                modifier = Modifier.animateItem(),
                onLongClick = {
                    haptics.longPress()
                    selectedIds = (selectedIds ?: emptySet()) + track.id
                },
                selected = selectedIds?.contains(track.id) == true
            )
        }
    }
}

@Composable
private fun SmartPlaylistMenu(
    visible: Boolean,
    name: String,
    isDecade: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onFreeze: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalClintColors.current
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = 340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
            RowDivider(color = colors.surfaceVariant)
            PlaylistMenuRow(
                Icons.Outlined.Tune,
                stringResource(if (isDecade) R.string.smart_menu_customize else R.string.smart_menu_edit),
                colors.onSurface,
                onEdit
            )
            PlaylistMenuRow(Icons.AutoMirrored.Outlined.PlaylistAddCheck, stringResource(R.string.smart_menu_freeze), colors.onSurface, onFreeze)
            if (!isDecade) {
                PlaylistMenuRow(Icons.Outlined.Delete, stringResource(R.string.playlist_delete_title), colors.colorError, onDelete)
            }
        }
    }
}

@Composable
private fun PlaylistMenu(
    visible: Boolean,
    name: String,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalClintColors.current
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = 340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
            RowDivider(color = colors.surfaceVariant)
            PlaylistMenuRow(Icons.Outlined.Edit, stringResource(R.string.playlist_rename), colors.onSurface, onRename)
            PlaylistMenuRow(Icons.Outlined.FileUpload, stringResource(R.string.playlist_export), colors.onSurface, onExport)
            PlaylistMenuRow(Icons.Outlined.Delete, stringResource(R.string.playlist_delete_title), colors.colorError, onDelete)
        }
    }
}

@Composable
private fun PlaylistMenuRow(icon: ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        KateIcon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

private val LocalHeaderParallax = staticCompositionLocalOf<() -> Float> { { 0f } }

private const val CollapsedBarItemIndex = 2
private const val ParallaxFactor = 0.45f
private const val ParallaxFade = 0.7f
private const val ParallaxShrink = 0.1f

@Composable
private fun rememberHeaderParallax(listState: LazyListState, topBarHeight: () -> Int): () -> Float {
    val currentTopBarHeight by rememberUpdatedState(topBarHeight)
    return remember(listState) {
        {
            when (listState.firstVisibleItemIndex) {
                0 -> listState.firstVisibleItemScrollOffset.toFloat()
                1 -> currentTopBarHeight() + listState.firstVisibleItemScrollOffset.toFloat()
                else -> 0f
            }
        }
    }
}

@Composable
private fun Modifier.headerParallax(): Modifier {
    val scrolled = LocalHeaderParallax.current
    return graphicsLayer {
        val distance = scrolled().coerceAtLeast(0f)
        val fraction = if (size.height > 0f) (distance / size.height).coerceIn(0f, 1f) else 0f
        translationY = distance * ParallaxFactor
        alpha = 1f - fraction * ParallaxFade
        val scale = 1f - fraction * ParallaxShrink
        scaleX = scale
        scaleY = scale
    }
}

@Composable
private fun BoxScope.CollapsedDetailBar(
    visible: Boolean,
    title: String?,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    val colors = LocalClintColors.current
    AnimatedVisibility(
        visible = visible && title != null,
        enter = fadeIn(tween(durationMillis = 180)) + slideInVertically(tween(durationMillis = 220)) { -it / 3 },
        exit = fadeOut(tween(durationMillis = 140)) + slideOutVertically(tween(durationMillis = 160)) { -it / 3 },
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp)
                .background(colors.cardBackground)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            KateIconButton(onClick = onBack) {
                KateIcon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.library_back),
                    tint = colors.primary
                )
            }
            Text(
                text = title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            )
            actions()
        }
    }
}

@Composable
private fun DetailScaffold(
    onBack: () -> Unit,
    bottomBarConnection: NestedScrollConnection?,
    title: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    overlay: @Composable BoxScope.() -> Unit = {},
    jumpIndex: Int = -1,
    content: LazyListScope.() -> Unit
) {
    val colors = LocalClintColors.current
    val bottomInset = LocalBottomContentPadding.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var topBarHeight by remember { mutableIntStateOf(0) }
    val parallax = rememberHeaderParallax(listState) { topBarHeight }
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex >= CollapsedBarItemIndex } }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        CompositionLocalProvider(LocalHeaderParallax provides parallax) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it },
                contentPadding = PaddingValues(bottom = bottomInset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item(key = "detail_top_bar") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .widthIn(max = 900.dp)
                            .fillMaxWidth()
                            .onSizeChanged { topBarHeight = it.height }
                            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                    ) {
                        KateIconButton(onClick = onBack) {
                            KateIcon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.library_back),
                                tint = colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        actions()
                    }
                }
                content()
                item(key = "detail_bottom_space") { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
        VerticalScrollbar(
            state = listState,
            thumbColor = colors.primary
        )
        val bottomInsetPx = with(density) { bottomInset.roundToPx() }
        val jumpVisible by remember(jumpIndex, bottomInsetPx) {
            derivedStateOf { jumpIndex >= 0 && !listState.isItemInView(jumpIndex, bottomInsetPx) }
        }
        JumpToPlayingButton(
            visible = jumpVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = bottomInset + 16.dp),
            onClick = { scope.launch { listState.animateScrollToItem(maxOf(0, jumpIndex - 2)) } }
        )
        CollapsedDetailBar(visible = collapsed, title = title, onBack = onBack, actions = actions)
        overlay()
    }
}

@Composable
private fun DetailHeader(
    sharedKey: String,
    artTrack: Track?,
    artistImage: ArtistImage? = null,
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onSubtitleClick: (() -> Unit)?,
    meta: String,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    shuffleTracks: () -> List<Track> = { emptyList() }
) {
    BoxWithConstraints(
        modifier = Modifier
            .widthIn(max = 900.dp)
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp)
    ) {
        val shape = RoundedCornerShape(LibraryStylePreferences.scaledCorner(24.dp, LocalLookAndFeel.current.libraryCorners))
        if (maxWidth >= 560.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                ArtThumb(
                    track = artTrack,
                    icon = icon,
                    shape = shape,
                    iconSize = 64.dp,
                    artistImage = artistImage,
                    modifier = Modifier
                        .sharedBoundsIn(sharedKey, LocalNavAnimatedScope.current)
                        .size(200.dp)
                        .headerParallax()
                )
                Column(modifier = Modifier.weight(1f).padding(start = 24.dp)) {
                    HeaderTexts(
                        title = title,
                        subtitle = subtitle,
                        onSubtitleClick = onSubtitleClick,
                        meta = meta,
                        textAlign = TextAlign.Start,
                        horizontalAlignment = Alignment.Start
                    )
                    HeaderButtons(onPlay = onPlay, onShuffle = onShuffle, shuffleTracks = shuffleTracks)
                }
            }
        } else {
            val artSize: Dp = min(maxWidth * 0.68f, 280.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                ArtThumb(
                    track = artTrack,
                    icon = icon,
                    shape = shape,
                    iconSize = 64.dp,
                    artistImage = artistImage,
                    modifier = Modifier
                        .sharedBoundsIn(sharedKey, LocalNavAnimatedScope.current)
                        .size(artSize)
                        .headerParallax()
                )
                Spacer(modifier = Modifier.height(20.dp))
                HeaderTexts(
                    title = title,
                    subtitle = subtitle,
                    onSubtitleClick = onSubtitleClick,
                    meta = meta,
                    textAlign = TextAlign.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                HeaderButtons(onPlay = onPlay, onShuffle = onShuffle, shuffleTracks = shuffleTracks)
            }
        }
    }
}

@Composable
private fun HeaderTexts(
    title: String,
    subtitle: String?,
    onSubtitleClick: (() -> Unit)?,
    meta: String,
    textAlign: TextAlign,
    horizontalAlignment: Alignment.Horizontal
) {
    val colors = LocalClintColors.current
    Column(horizontalAlignment = horizontalAlignment, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = textAlign,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = if (onSubtitleClick != null) colors.primary else colors.secondaryText,
                textAlign = textAlign,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .let { if (onSubtitleClick != null) it.clickable { onSubtitleClick() } else it }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
        if (meta.isNotBlank()) {
            Text(
                text = meta,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                textAlign = textAlign,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun HeaderButtons(onPlay: () -> Unit, onShuffle: () -> Unit, shuffleTracks: () -> List<Track>) {
    val haptics = LocalKateHaptics.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        KateActionButton(
            label = stringResource(R.string.library_play),
            icon = Icons.Filled.PlayArrow,
            primary = true,
            onClick = onPlay,
            modifier = Modifier.weight(1f).height(48.dp)
        )
        KateActionButton(
            label = stringResource(R.string.library_shuffle),
            icon = Icons.Filled.Shuffle,
            primary = false,
            onClick = onShuffle,
            onLongClickLabel = stringResource(R.string.shuffle_mode_choose),
            onLongClick = {
                val tracks = shuffleTracks()
                if (tracks.isNotEmpty()) {
                    haptics.longPress()
                    ShufflePickRequest.open(tracks)
                }
            },
            modifier = Modifier.weight(1f).height(48.dp)
        )
    }
}

@Composable
private fun DetailSectionTitle(text: String) {
    val colors = LocalClintColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = colors.onSurface,
        modifier = Modifier
            .widthIn(max = 900.dp)
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun DetailTrackRow(
    track: Track,
    leading: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false
) {
    val colors = LocalClintColors.current
    val playing = PlayerController.currentTrack?.id == track.id
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .widthIn(max = 900.dp)
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (playing) colors.primary.copy(alpha = 0.14f) else Color.Transparent)
            .selectionHighlight(selected, colors.primary, DetailRowCorner)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (leading != null) {
            Text(
                text = leading,
                style = MaterialTheme.typography.bodyMedium,
                color = if (playing) colors.primary else colors.secondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.width(32.dp)
            )
        } else {
            ArtThumb(
                track = track,
                icon = Icons.Filled.MusicNote,
                shape = RoundedCornerShape(10.dp),
                iconSize = 20.dp,
                modifier = Modifier.size(48.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (playing) colors.primary else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = trackArtist(track),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = formatTrackDuration(track.durationMs),
            style = MaterialTheme.typography.labelMedium,
            color = colors.secondaryText,
            maxLines = 1
        )
    }
}

private const val DetailPageAlbum = "album"
private const val DetailPageArtist = "artist"
private const val DetailPageAlbumArtist = "album_artist"
private val DetailRowSelectionCorner = 14.dp

@Stable
private class DetailViewModeState(
    private val context: Context,
    private val page: String,
    initial: LibraryViewMode
) {
    var mode by mutableStateOf(initial)
        private set

    fun select(next: LibraryViewMode) {
        if (next == mode) return
        mode = next
        LibraryPreferences.setDetailViewMode(context, page, next)
    }
}

@Composable
private fun rememberDetailViewMode(page: String, default: LibraryViewMode): DetailViewModeState {
    val context = LocalContext.current.applicationContext
    return remember(page) {
        DetailViewModeState(context, page, LibraryPreferences.getDetailViewMode(context, page, default))
    }
}

@Composable
private fun rememberNumberedSongTiles(tracks: List<Track>): List<LibraryTile> {
    val base = rememberSongTiles(tracks)
    return remember(base, tracks) {
        base.mapIndexed { position, tile ->
            val number = tracks.getOrNull(position)?.trackNumber ?: (position + 1)
            LibraryTile(
                id = tile.id,
                title = tile.title,
                gridSubtitle = tile.gridSubtitle,
                listSubtitle = "$number • ${tile.gridSubtitle}",
                art = tile.art,
                artistImage = tile.artistImage,
                icon = tile.icon,
                meta = tile.meta,
                chips = tile.chips,
                trackIds = tile.trackIds,
                sharedKey = tile.sharedKey
            )
        }
    }
}

@Composable
private fun DetailViewModeButton(mode: LibraryViewMode, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    KateIconButton(onClick = onClick) {
        KateIcon(
            imageVector = if (mode.isGrid) Icons.Outlined.GridView else Icons.AutoMirrored.Outlined.ViewList,
            contentDescription = stringResource(R.string.library_menu_view_mode),
            tint = colors.primary
        )
    }
}

@Composable
private fun DetailTopBarRow(onBack: () -> Unit, actions: @Composable RowScope.() -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
    ) {
        KateIconButton(onClick = onBack) {
            KateIcon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.library_back),
                tint = colors.primary
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        actions()
    }
}

@Composable
private fun MorphDetailScaffold(
    onBack: () -> Unit,
    bottomBarConnection: NestedScrollConnection?,
    title: String?,
    tiles: List<LibraryTile>,
    viewMode: DetailViewModeState,
    pinchEnabled: Boolean,
    isSelected: (String) -> Boolean,
    onTileClick: (Int, String) -> Unit,
    onTileLongClick: (String) -> Unit,
    actions: @Composable RowScope.() -> Unit,
    overlay: @Composable BoxScope.(ViewModeMorphState) -> Unit,
    hero: @Composable () -> Unit,
    extra: @Composable () -> Unit = {}
) {
    val colors = LocalClintColors.current
    val bottomInset = LocalBottomContentPadding.current
    val density = LocalDensity.current
    val haptics = LocalKateHaptics.current
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val morph = remember(scope) { ViewModeMorphState(scope) }
    var headerHeightPx by remember { mutableIntStateOf(0) }
    var collapseAtPx by remember { mutableIntStateOf(0) }
    val mode = viewMode.mode
    val currentTrackId = PlayerController.currentTrack?.id
    val morphing = morph.session != null
    val placementSpec: FiniteAnimationSpec<IntOffset>? = if (morphing) {
        null
    } else {
        spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset.VisibilityThreshold)
    }
    val parallax: () -> Float = remember(listState, gridState, viewMode) {
        {
            val grid = viewMode.mode.isGrid
            val index = if (grid) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex
            val offset = if (grid) gridState.firstVisibleItemScrollOffset else listState.firstVisibleItemScrollOffset
            if (index == 0) offset.toFloat() else 0f
        }
    }
    val collapsed by remember(listState, gridState, viewMode) {
        derivedStateOf {
            val grid = viewMode.mode.isGrid
            val index = if (grid) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex
            val offset = if (grid) gridState.firstVisibleItemScrollOffset else listState.firstVisibleItemScrollOffset
            index > 0 || (collapseAtPx > 0 && offset >= collapseAtPx)
        }
    }
    val headerContent: @Composable (Boolean) -> Unit = { interactive ->
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .let { if (interactive) it.onSizeChanged { size -> collapseAtPx = size.height } else it }
            ) {
                DetailTopBarRow(onBack = if (interactive) onBack else ({}), actions = actions)
                hero()
            }
            extra()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .viewModePinch(morph)
    ) {
        val viewportWidth = constraints.maxWidth
        val viewportHeight = constraints.maxHeight
        val bottomInsetPx = with(density) { bottomInset.roundToPx() }
        val count = tiles.size
        val cornerScale = LibraryStylePreferences.cornerScale(LocalLookAndFeel.current.libraryCorners)
        val geometries = remember(viewportWidth, viewportHeight, headerHeightPx, bottomInsetPx, count, density, cornerScale) {
            LibraryViewMode.entries.associateWith { entry ->
                computeModeGeometry(entry, viewportWidth, viewportHeight, headerHeightPx, bottomInsetPx, count, density, cornerScale)
            }
        }
        val geometry = geometries.getValue(mode)
        val style = geometry.style
        val staticFrame = remember(style) { TileFrame(style, 0, 0) }

        SideEffect {
            morph.pinchEnabled = pinchEnabled && count > 0
            morph.currentMode = { viewMode.mode }
            morph.onStep = { haptics.tick() }
            morph.captureSession = capture@{
                val current = geometries.getValue(viewMode.mode)
                if (headerHeightPx <= 0 || count == 0) return@capture null
                val scroll = if (current.isGrid) {
                    current.scrollFromLazy(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset)
                } else {
                    current.scrollFromLazy(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
                }
                scope.launch {
                    if (current.isGrid) gridState.stopScroll() else listState.stopScroll()
                }
                val anchor = current.anchorAt(scroll)
                MorphSession(
                    tiles = tiles,
                    anchorIndex = anchor,
                    anchorViewportY = (current.contentTopOf(anchor) - scroll).toFloat()
                )
            }
            morph.commit = { target ->
                val targetGeometry = geometries.getValue(target)
                val session = morph.session
                val scroll = if (session != null) targetGeometry.scrollFor(session).roundToInt() else 0
                val (index, offset) = targetGeometry.lazyPosition(scroll)
                if (targetGeometry.isGrid) {
                    gridState.requestScrollToItem(index, offset)
                } else {
                    listState.requestScrollToItem(index, offset)
                }
                viewMode.select(target)
            }
        }

        CompositionLocalProvider(LocalHeaderParallax provides parallax) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (morph.session != null) 0f else 1f }
            ) {
                if (geometry.isGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(geometry.columns),
                        state = gridState,
                        modifier = Modifier
                            .fillMaxSize()
                            .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it },
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomInset),
                        horizontalArrangement = Arrangement.spacedBy(style.gridSpacing),
                        verticalArrangement = Arrangement.spacedBy(style.gridSpacing)
                    ) {
                        item(key = "detail_header", span = { GridItemSpan(maxLineSpan) }) {
                            Box(modifier = Modifier.onSizeChanged { headerHeightPx = it.height }) {
                                headerContent(true)
                            }
                        }
                        gridItemsIndexed(tiles, key = { _, tile -> tile.id }) { position, tile ->
                            LibraryTileView(
                                tile = tile,
                                isPlaying = tile.isPlaying(currentTrackId),
                                from = staticFrame,
                                to = null,
                                fraction = { 0f },
                                modifier = Modifier
                                    .animateItem(placementSpec = placementSpec)
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(style.artCorner))
                                    .selectionHighlight(isSelected(tile.id), colors.primary, style.artCorner)
                                    .combinedClickable(
                                        onClick = { onTileClick(position, tile.id) },
                                        onLongClick = { onTileLongClick(tile.id) }
                                    )
                            )
                        }
                        item(key = "detail_bottom_space", span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    VerticalScrollbar(state = gridState, thumbColor = colors.primary)
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it },
                        contentPadding = PaddingValues(bottom = bottomInset),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item(key = "detail_header") {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 900.dp)
                                    .fillMaxWidth()
                                    .onSizeChanged { headerHeightPx = it.height }
                            ) {
                                headerContent(true)
                            }
                        }
                        itemsIndexed(tiles, key = { _, tile -> tile.id }) { position, tile ->
                            LibraryTileView(
                                tile = tile,
                                isPlaying = tile.isPlaying(currentTrackId),
                                from = staticFrame,
                                to = null,
                                fraction = { 0f },
                                modifier = Modifier
                                    .animateItem(placementSpec = placementSpec)
                                    .detailExactSize(geometry.rowWidth, geometry.rowHeight)
                                    .selectionHighlight(isSelected(tile.id), colors.primary, DetailRowSelectionCorner)
                                    .combinedClickable(
                                        onClick = { onTileClick(position, tile.id) },
                                        onLongClick = { onTileLongClick(tile.id) }
                                    )
                            )
                        }
                        item(key = "detail_bottom_space") { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                    VerticalScrollbar(state = listState, thumbColor = colors.primary)
                }
            }
            morph.session?.let { session ->
                ViewModeMorphOverlay(
                    morph = morph,
                    session = session,
                    geometries = geometries,
                    header = {
                        CompositionLocalProvider(LocalNavAnimatedScope provides null) {
                            headerContent(false)
                        }
                    }
                )
            }
        }
        val playingIndex = remember(tiles, currentTrackId) {
            if (currentTrackId == null) -1 else tiles.indexOfFirst { it.isPlaying(currentTrackId) }
        }
        val playingVisible by remember(geometry.isGrid, playingIndex, bottomInsetPx) {
            derivedStateOf {
                when {
                    playingIndex < 0 -> true
                    geometry.isGrid -> gridState.isItemInView(playingIndex + 1, bottomInsetPx)
                    else -> listState.isItemInView(playingIndex + 1, bottomInsetPx)
                }
            }
        }
        JumpToPlayingButton(
            visible = playingIndex >= 0 && !playingVisible && !morphing,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = bottomInset + 16.dp),
            onClick = {
                scope.launch {
                    if (geometry.isGrid) {
                        val columns = geometry.columns.coerceAtLeast(1)
                        val row = playingIndex / columns
                        gridState.animateScrollToItem(maxOf(1, (row - 1) * columns + 1))
                    } else {
                        listState.animateScrollToItem(maxOf(1, playingIndex - 1))
                    }
                }
            }
        )
        CollapsedDetailBar(visible = collapsed && !morphing, title = title, onBack = onBack, actions = actions)
        overlay(morph)
    }
}

private fun Modifier.detailExactSize(width: Int, height: Int): Modifier = layout { measurable, _ ->
    val placeable = measurable.measure(Constraints.fixed(width, height))
    layout(width, height) {
        placeable.placeRelative(0, 0)
    }
}

@Composable
private fun durationText(durationMs: Long): String {
    if (durationMs <= 0L) return ""
    val minutes = ((durationMs + 30_000L) / 60_000L).toInt().coerceAtLeast(1)
    return pluralStringResource(R.plurals.library_duration_minutes, minutes, minutes)
}

private fun formatTrackDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun ArtistImageMenu(
    visible: Boolean,
    hasImage: Boolean,
    canReset: Boolean,
    onDismiss: () -> Unit,
    onChoose: () -> Unit,
    onRemove: () -> Unit,
    onReset: () -> Unit
) {
    val colors = LocalClintColors.current
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = 340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.artist_image_menu_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
            RowDivider(color = colors.surfaceVariant)
            ArtistImageMenuRow(
                icon = Icons.Outlined.AddPhotoAlternate,
                label = stringResource(R.string.artist_image_choose),
                onClick = onChoose
            )
            if (hasImage) {
                ArtistImageMenuRow(
                    icon = Icons.Outlined.HideImage,
                    label = stringResource(R.string.artist_image_remove),
                    onClick = onRemove
                )
            }
            if (canReset) {
                ArtistImageMenuRow(
                    icon = Icons.Outlined.Restore,
                    label = stringResource(R.string.artist_image_reset),
                    onClick = onReset
                )
            }
        }
    }
}

@Composable
private fun ArtistImageMenuRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        KateIcon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

