package com.jhaiian.kate.ui.library

import com.jhaiian.kate.data.PlayCounts
import com.jhaiian.kate.data.SmartPlaylistEngine
import com.jhaiian.kate.ui.theme.LibraryLayoutPreferences
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.JumpToPlayingButton
import com.jhaiian.kate.ui.components.isItemInView
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.pressIndication
import com.jhaiian.kate.ui.components.katePressable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.jhaiian.kate.ui.components.KateIconButton
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.data.LibraryScanState
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.LibraryViewMode
import com.jhaiian.kate.data.PlaylistFiles
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.data.SmartPlaylist
import com.jhaiian.kate.data.SmartPlaylistStore
import com.jhaiian.kate.data.ScanPhase
import com.jhaiian.kate.data.ScanProgress
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.ShufflePickRequest
import com.jhaiian.kate.ui.components.VerticalScrollbar
import com.jhaiian.kate.ui.components.WithSharedTransitionScope
import com.jhaiian.kate.ui.components.sharedBoundsIn
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.settings.SectionHeader
import com.jhaiian.kate.ui.theme.HomePreferences
import com.jhaiian.kate.ui.theme.LibraryStylePreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val LibraryHomeTitleKey = "library_home_title"
private const val LibraryShuffleKey = "library_action_shuffle"
private const val LibraryPlayKey = "library_action_play"

private fun menuIconKey(category: LibraryCategory) = "library_menu_icon_${category.key}"

private fun menuLabelKey(category: LibraryCategory) = "library_menu_label_${category.key}"

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    bottomBarConnection: NestedScrollConnection? = null,
    onTrackClick: (List<Track>, Track) -> Unit = { _, _ -> },
    onAlbumClick: (String) -> Unit = {},
    onArtistClick: (String, Boolean) -> Unit = { _, _ -> },
    onGroupClick: (LibraryCategory, String) -> Unit = { _, _ -> },
    onPlaylistClick: (String) -> Unit = {},
    onSmartPlaylistClick: (String) -> Unit = {},
    onOpenPlayer: () -> Unit = {}
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    var showFolderPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        LibraryStore.ensureLoaded(context)
        LibraryStore.scanIfNeeded(context)
    }

    val folders = LibraryStore.folders
    val scanState = LibraryStore.scanState

    BackHandler(enabled = LibraryStore.category != null && scanState == LibraryScanState.Loaded) {
        LibraryStore.openCategory(context, null)
    }

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        AnimatedContent(
            targetState = scanState,
            modifier = Modifier.weight(1f),
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
            label = "library_state"
        ) { state ->
            when (state) {
                is LibraryScanState.Scanning -> ScanningState()
                is LibraryScanState.Loaded -> LibraryBrowser(
                    onAddFolders = { showFolderPicker = true },
                    bottomBarConnection = bottomBarConnection,
                    onTrackClick = onTrackClick,
                    onAlbumClick = onAlbumClick,
                    onArtistClick = onArtistClick,
                    onGroupClick = onGroupClick,
                    onPlaylistClick = onPlaylistClick,
                    onSmartPlaylistClick = onSmartPlaylistClick,
                    onOpenPlayer = onOpenPlayer
                )
                is LibraryScanState.Empty -> NoResultsState(onAddFolders = { showFolderPicker = true })
                is LibraryScanState.Idle -> EmptyLibraryState(onAddFolders = { showFolderPicker = true })
            }
        }
    }

    if (showFolderPicker) {
        FolderPickerDialog(
            folders = folders,
            onDismiss = { showFolderPicker = false },
            onFolderAdded = { uri, name, label -> LibraryStore.addFolder(context, uri, name, label) },
            onFolderToggled = { uri, enabled -> LibraryStore.setFolderEnabled(context, uri, enabled) },
            onFolderRemoved = { uri -> LibraryStore.removeFolder(context, uri) },
            onSaveAndScan = {
                showFolderPicker = false
                LibraryStore.startScan(context, restart = true)
            }
        )
    }
}

@Composable
private fun LibraryBrowser(
    onAddFolders: () -> Unit,
    bottomBarConnection: NestedScrollConnection?,
    onTrackClick: (List<Track>, Track) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String, Boolean) -> Unit,
    onGroupClick: (LibraryCategory, String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onSmartPlaylistClick: (String) -> Unit,
    onOpenPlayer: () -> Unit
) {
    val context = LocalContext.current
    val index = LibraryStore.index
    val open: (LibraryCategory?) -> Unit = { LibraryStore.openCategory(context, it) }
    val back = { open(null) }
    AnimatedContent(
        targetState = LibraryStore.category,
        transitionSpec = {
            fadeIn(tween(durationMillis = 260, delayMillis = 60, easing = LinearOutSlowInEasing)) togetherWith
                fadeOut(tween(durationMillis = 140, easing = FastOutLinearInEasing))
        },
        label = "library_category"
    ) { category ->
        val contentScope = this
        if (category == null) {
            LibraryHome(
                contentScope = contentScope,
                bottomBarConnection = bottomBarConnection,
                onOpen = { open(it) },
                onTrackClick = onTrackClick,
                onPlaylistClick = onPlaylistClick,
                onOpenPlayer = onOpenPlayer
            )
        } else {
            CategoryState(
                category = category,
                contentScope = contentScope,
                onAddFolders = onAddFolders,
                onBack = back,
                onSelectCategory = { open(it) },
                bottomBarConnection = bottomBarConnection,
                onTrackClick = onTrackClick,
                onAlbumClick = onAlbumClick,
                onArtistClick = onArtistClick,
                onGroupClick = onGroupClick,
                onPlaylistClick = onPlaylistClick,
                onSmartPlaylistClick = onSmartPlaylistClick
            )
        }
    }
}

@Composable
private fun CategoryState(
    category: LibraryCategory,
    contentScope: AnimatedVisibilityScope,
    onAddFolders: () -> Unit,
    onBack: () -> Unit,
    onSelectCategory: (LibraryCategory) -> Unit,
    bottomBarConnection: NestedScrollConnection?,
    onTrackClick: (List<Track>, Track) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String, Boolean) -> Unit,
    onGroupClick: (LibraryCategory, String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onSmartPlaylistClick: (String) -> Unit
) {
    val index = LibraryStore.index
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var namingPlaylist by remember { mutableStateOf(false) }
    var editingSmart by remember { mutableStateOf<SmartPlaylist?>(null) }
    val importFailed = stringResource(R.string.playlist_import_failed)
    val importedDefaultName = stringResource(R.string.playlist_imported_default_name)
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val library = LibraryStore.tracks.toList()
        scope.launch {
            val imported = withContext(Dispatchers.IO) { PlaylistFiles.read(context, uri, library) }
            if (imported == null) {
                Toast.makeText(context, importFailed, Toast.LENGTH_SHORT).show()
                return@launch
            }
            val playlist = PlaylistStore.create(context, imported.name ?: importedDefaultName, imported.trackIds)
            val message = context.resources.getQuantityString(
                R.plurals.playlist_imported,
                imported.entryCount,
                playlist.trackIds.size,
                imported.entryCount,
                playlist.name
            )
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        when (category) {
            LibraryCategory.SONGS, LibraryCategory.RECENTLY_ADDED, LibraryCategory.FAVORITES -> {
                val source = when (category) {
                    LibraryCategory.SONGS -> LibraryStore.tracks.toList()
                    LibraryCategory.RECENTLY_ADDED -> index.recentlyAdded
                    else -> FavoritesStore.favoriteTracks(LibraryStore.tracks)
                }
                TileListState(
                    category = category,
                    tiles = rememberSongTiles(source),
                    playTracks = { source },
                    sortable = category != LibraryCategory.RECENTLY_ADDED,
                    onAddFolders = onAddFolders,
                    onBack = onBack,
                    onSelectCategory = onSelectCategory,
                    contentScope = contentScope,
                    bottomBarConnection = bottomBarConnection,
                    onTileClick = { position -> source.getOrNull(position)?.let { onTrackClick(source, it) } },
                    resolveSelection = { ids -> source.filter { it.id in ids } }
                )
            }
            LibraryCategory.ALBUMS -> {
                val albums = index.albums
                TileListState(
                    category = category,
                    tiles = rememberAlbumTiles(albums),
                    playTracks = { albums.flatMap { it.tracks } },
                    sortable = false,
                    onAddFolders = onAddFolders,
                    onBack = onBack,
                    onSelectCategory = onSelectCategory,
                    contentScope = contentScope,
                    bottomBarConnection = bottomBarConnection,
                    onTileClick = { position -> albums.getOrNull(position)?.let { onAlbumClick(it.key) } },
                    resolveSelection = { ids -> albums.filter { it.key in ids }.flatMap { it.tracks } }
                )
            }
            LibraryCategory.ARTISTS, LibraryCategory.ALBUM_ARTISTS -> {
                val albumArtist = category == LibraryCategory.ALBUM_ARTISTS
                val artists = if (albumArtist) index.albumArtists else index.artists
                TileListState(
                    category = category,
                    tiles = rememberArtistTiles(artists, albumArtist, category.icon),
                    playTracks = { artists.flatMap { it.tracks }.distinctBy { it.id } },
                    sortable = false,
                    onAddFolders = onAddFolders,
                    onBack = onBack,
                    onSelectCategory = onSelectCategory,
                    contentScope = contentScope,
                    bottomBarConnection = bottomBarConnection,
                    onTileClick = { position -> artists.getOrNull(position)?.let { onArtistClick(it.key, albumArtist) } },
                    resolveSelection = { ids -> artists.filter { it.key in ids }.flatMap { it.tracks }.distinctBy { it.id } }
                )
            }
            LibraryCategory.GENRES, LibraryCategory.FOLDERS, LibraryCategory.YEARS -> {
                val groups = index.groups(category)
                TileListState(
                    category = category,
                    tiles = rememberGroupTiles(category, groups),
                    playTracks = { groups.flatMap { it.tracks }.distinctBy { it.id } },
                    sortable = false,
                    onAddFolders = onAddFolders,
                    onBack = onBack,
                    onSelectCategory = onSelectCategory,
                    contentScope = contentScope,
                    bottomBarConnection = bottomBarConnection,
                    onTileClick = { position -> groups.getOrNull(position)?.let { onGroupClick(category, it.key) } },
                    resolveSelection = { ids -> groups.filter { it.key in ids }.flatMap { it.tracks }.distinctBy { it.id } }
                )
            }
            LibraryCategory.PLAYLISTS -> {
                val playlists = PlaylistStore.playlists
                TileListState(
                    category = category,
                    tiles = rememberPlaylistTiles(),
                    playTracks = {
                        val library = LibraryStore.tracks.toList()
                        playlists.flatMap { PlaylistStore.tracksOf(it, library) }.distinctBy { it.id }
                    },
                    sortable = false,
                    onAddFolders = onAddFolders,
                    onBack = onBack,
                    onSelectCategory = onSelectCategory,
                    contentScope = contentScope,
                    bottomBarConnection = bottomBarConnection,
                    onTileClick = { position -> playlists.getOrNull(position)?.let { onPlaylistClick(it.id) } },
                    resolveSelection = { ids ->
                        val library = LibraryStore.tracks.toList()
                        playlists.filter { it.id in ids }.flatMap { PlaylistStore.tracksOf(it, library) }
                    },
                    extraMenuActions = listOf(
                        LibraryMenuAction(Icons.AutoMirrored.Outlined.PlaylistAdd, stringResource(R.string.playlist_new)) { namingPlaylist = true },
                        LibraryMenuAction(Icons.Outlined.FileOpen, stringResource(R.string.playlist_import)) {
                            importLauncher.launch(arrayOf("*/*"))
                        }
                    ),
                    emptyMessage = stringResource(R.string.playlist_empty_library),
                    headerExtra = {
                        SmartPlaylistShelf(
                            onOpen = onSmartPlaylistClick,
                            onCreate = { editingSmart = SmartPlaylistStore.newDraft() }
                        )
                    }
                )
            }
        }
        PlaylistNameDialog(
            visible = namingPlaylist,
            title = stringResource(R.string.playlist_new),
            initialName = "",
            confirmLabel = stringResource(R.string.playlist_create),
            onConfirm = { name ->
                val playlist = PlaylistStore.create(context, name)
                namingPlaylist = false
                onPlaylistClick(playlist.id)
            },
            onDismiss = { namingPlaylist = false }
        )
        SmartPlaylistEditor(
            initial = editingSmart,
            onDismiss = { editingSmart = null },
            onSaved = { saved ->
                editingSmart = null
                onSmartPlaylistClick(saved.id)
            }
        )
    }
}

@Composable
private fun LibraryHome(
    contentScope: AnimatedVisibilityScope,
    bottomBarConnection: NestedScrollConnection?,
    onOpen: (LibraryCategory) -> Unit,
    onTrackClick: (List<Track>, Track) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onOpenPlayer: () -> Unit
) {
    val lookAndFeel = LocalLookAndFeel.current
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val bottomInset = LocalBottomContentPadding.current
    val index = LibraryStore.index
    val songCount = LibraryStore.tracks.size
    val favoriteCount = FavoritesStore.favoriteTracks(LibraryStore.tracks).size
    val listState = rememberLazyListState()
    val pullState = rememberPullToRefreshState()
    val refreshing = LibraryStore.scanProgress?.phase == ScanPhase.FINDING
    val scanProgress = LibraryStore.scanProgress

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { LibraryStore.startScan(context) },
        state = pullState,
        modifier = Modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = refreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = colors.cardBackground,
                color = colors.primary
            )
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it },
            contentPadding = PaddingValues(bottom = bottomInset),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 4.dp)
                    ) {
                        KateIcon(
                            imageVector = Icons.Outlined.LibraryMusic,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Box(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                            Text(
                                text = stringResource(R.string.nav_library),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.sharedBoundsIn(LibraryHomeTitleKey, contentScope)
                            )
                        }
                        LibraryActionButton(
                            icon = Icons.Filled.Shuffle,
                            label = stringResource(R.string.library_shuffle),
                            onClick = { PlayerController.playShuffled(context, LibraryStore.tracks.toList()) },
                            modifier = Modifier.sharedBoundsIn(LibraryShuffleKey, contentScope),
                            onLongClick = { ShufflePickRequest.open(LibraryStore.tracks.toList()) },
                            onLongClickLabel = stringResource(R.string.shuffle_mode_choose)
                        )
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                        LibraryActionButton(
                            icon = Icons.Filled.PlayArrow,
                            label = stringResource(R.string.library_play),
                            primary = true,
                            onClick = { PlayerController.playAll(context, LibraryStore.tracks.toList()) },
                            modifier = Modifier.sharedBoundsIn(LibraryPlayKey, contentScope)
                        )
                    }
                    Text(
                        text = if (scanProgress != null) {
                            scanStatusText(scanProgress)
                        } else {
                            pluralStringResource(R.plurals.library_song_count, songCount, songCount)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 64.dp, end = 24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .padding(horizontal = 24.dp)
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = scanProgress != null,
                            enter = fadeIn(tween(220)),
                            exit = fadeOut(tween(320))
                        ) {
                            ScanProgressBar(progress = scanProgress, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            lookAndFeel.homeSectionOrder.filter { lookAndFeel.showsHomeSection(it) }.forEach { section ->
                when (section) {
                    HomePreferences.SECTION_CONTINUE -> item(key = "home_continue") { ContinueListeningCard(onOpenPlayer = onOpenPlayer) }
                    HomePreferences.SECTION_RECENTLY_PLAYED -> item(key = "home_recently_played") {
                        TrackCarousel(stringResource(R.string.home_recently_played), rememberRecentlyPlayed(), onTrackClick)
                    }
                    HomePreferences.SECTION_MOST_PLAYED -> item(key = "home_most_played") {
                        TrackCarousel(stringResource(R.string.home_most_played), rememberMostPlayed(), onTrackClick)
                    }
                    HomePreferences.SECTION_RECENTLY_ADDED -> item(key = "home_recently_added") {
                        TrackCarousel(
                            stringResource(R.string.library_category_recently_added),
                            index.recentlyAdded.take(HomeCarouselLimit),
                            onTrackClick
                        )
                    }
                    HomePreferences.SECTION_PLAYLISTS -> item(key = "home_playlists") { PlaylistCarousel(onPlaylistClick = onPlaylistClick) }
                }
            }
            lookAndFeel.homePins.forEach { pin ->
                item(key = "home_pin_$pin") {
                    val shelf = rememberPinnedShelf(pin)
                    if (shelf != null && shelf.second.isNotEmpty()) {
                        TrackCarousel(shelf.first, shelf.second.take(HomeCarouselLimit), onTrackClick)
                    }
                }
            }
            item {
                HomeSection(
                    title = stringResource(R.string.library_section_browse),
                    categories = orderedCategories(lookAndFeel, LibraryCategory.Browse),
                    count = { index.count(it, songCount, favoriteCount) },
                    contentScope = contentScope,
                    onOpen = onOpen
                )
            }
            item {
                HomeSection(
                    title = stringResource(R.string.library_section_collections),
                    categories = orderedCategories(lookAndFeel, LibraryCategory.Collections),
                    count = { index.count(it, songCount, favoriteCount) },
                    contentScope = contentScope,
                    onOpen = onOpen
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
        VerticalScrollbar(
            state = listState,
            thumbColor = colors.primary
        )
    }
}

@Composable
private fun HomeSection(
    title: String,
    categories: List<LibraryCategory>,
    count: (LibraryCategory) -> Int,
    contentScope: AnimatedVisibilityScope,
    onOpen: (LibraryCategory) -> Unit
) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        SectionHeader(text = title, tint = colors.primary)
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
        ) {
            categories.forEachIndexed { position, category ->
                if (position > 0) RowDivider(color = colors.divider)
                CategoryRow(
                    category = category,
                    count = count(category),
                    contentScope = contentScope,
                    onClick = { onOpen(category) }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.LibraryTopBar(
    category: LibraryCategory,
    countText: String,
    showMenu: Boolean,
    categoryScope: AnimatedVisibilityScope?,
    onBack: () -> Unit,
    onSwitchCategory: () -> Unit,
    onMenuToggle: () -> Unit,
    onShuffleClick: () -> Unit,
    onPlayClick: () -> Unit,
    onShuffleLongClick: (() -> Unit)? = null
) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.fillMaxWidth()) {
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
            Text(
                text = stringResource(R.string.nav_library),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .sharedBoundsIn(LibraryHomeTitleKey, categoryScope)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 12.dp)
        ) {
            Box(
                modifier = Modifier.height(48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !showMenu,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(120))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClickLabel = stringResource(R.string.library_switch_category)) { onSwitchCategory() }
                            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = menuIconKey(category)),
                                animatedVisibilityScope = this@AnimatedVisibility
                            )
                        ) {
                            KateIcon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier
                                    .sharedBoundsIn(categoryIconKey(category), categoryScope)
                                    .size(28.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(start = 12.dp)
                                .sharedBounds(
                                    rememberSharedContentState(key = menuLabelKey(category)),
                                    animatedVisibilityScope = this@AnimatedVisibility
                                )
                        ) {
                            Text(
                                text = stringResource(category.labelRes),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.sharedBoundsIn(categoryLabelKey(category), categoryScope)
                            )
                        }
                        KateIcon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.padding(start = 2.dp).size(28.dp)
                        )
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
        ) {
            val scanProgress = LibraryStore.scanProgress
            Text(
                text = if (scanProgress != null) scanStatusText(scanProgress) else countText,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            LibraryActionButton(
                icon = Icons.Filled.Shuffle,
                label = stringResource(R.string.library_shuffle),
                onClick = onShuffleClick,
                modifier = Modifier.sharedBoundsIn(LibraryShuffleKey, categoryScope),
                onLongClick = onShuffleLongClick,
                onLongClickLabel = stringResource(R.string.shuffle_mode_choose)
            )
            Spacer(modifier = Modifier.padding(start = 8.dp))
            LibraryActionButton(
                icon = Icons.Filled.PlayArrow,
                label = stringResource(R.string.library_play),
                primary = true,
                onClick = onPlayClick,
                modifier = Modifier.sharedBoundsIn(LibraryPlayKey, categoryScope)
            )
            KateIconButton(onClick = onMenuToggle) {
                KateIcon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.library_menu),
                    tint = colors.primary
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .padding(horizontal = 24.dp)
        ) {
            val scanProgress = LibraryStore.scanProgress
            androidx.compose.animation.AnimatedVisibility(
                visible = scanProgress != null,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(320))
            ) {
                ScanProgressBar(progress = scanProgress, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun scanStatusText(progress: ScanProgress): String = when {
    progress.phase == ScanPhase.SAVING -> stringResource(R.string.library_scan_finishing)
    progress.phase == ScanPhase.READING && progress.total > 0 ->
        pluralStringResource(R.plurals.library_scan_reading, progress.total, progress.processed, progress.total)
    progress.found > 0 -> pluralStringResource(R.plurals.library_scan_found, progress.found, progress.found)
    else -> stringResource(R.string.library_scan_status_finding)
}

@Composable
private fun ScanProgressBar(progress: ScanProgress?, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val target = if (progress != null && progress.phase == ScanPhase.READING && progress.total > 0) {
        progress.processed.toFloat() / progress.total.toFloat()
    } else {
        -1f
    }
    if (target < 0f) {
        LinearProgressIndicator(
            modifier = modifier.clip(RoundedCornerShape(2.dp)),
            color = colors.primary,
            trackColor = colors.primary.copy(alpha = 0.16f)
        )
    } else {
        val fraction by animateFloatAsState(
            targetValue = target.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 260),
            label = "scan_progress"
        )
        LinearProgressIndicator(
            progress = { fraction },
            modifier = modifier.clip(RoundedCornerShape(2.dp)),
            color = colors.primary,
            trackColor = colors.primary.copy(alpha = 0.16f)
        )
    }
}

@Composable
internal fun LibraryActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null
) {
    val haptics = LocalKateHaptics.current
    KateActionButton(
        label = label,
        icon = icon,
        primary = primary,
        compact = true,
        minHeight = 36.dp,
        onClick = onClick,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick?.let { longClick ->
            {
                haptics.longPress()
                longClick()
            }
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.LibraryOverflowMenu(
    category: LibraryCategory,
    visible: Boolean,
    onDismiss: () -> Unit,
    onSelectFolders: () -> Unit,
    onRescan: () -> Unit,
    onListOptions: (() -> Unit)?,
    onViewMode: (() -> Unit)?,
    extraActions: List<LibraryMenuAction> = emptyList()
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val categoryLabel = stringResource(category.labelRes)
    val unsupportedMessage = stringResource(R.string.library_shortcut_unsupported)
    KateDialog(visible = visible, onDismiss = onDismiss) {
        val dialogScope = this
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = 340.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                KateIcon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier
                        .size(28.dp)
                        .sharedBounds(
                            rememberSharedContentState(key = menuIconKey(category)),
                            animatedVisibilityScope = dialogScope
                        )
                )
                Text(
                    text = stringResource(category.labelRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .sharedBounds(
                            rememberSharedContentState(key = menuLabelKey(category)),
                            animatedVisibilityScope = dialogScope
                        )
                )
            }
            RowDivider(color = colors.surfaceVariant)
            extraActions.forEach { action ->
                MenuItemRow(
                    icon = action.icon,
                    label = action.label,
                    onClick = {
                        onDismiss()
                        action.onClick()
                    }
                )
            }
            MenuItemRow(
                icon = Icons.Outlined.Folder,
                label = stringResource(R.string.library_menu_select_folders),
                onClick = onSelectFolders
            )
            MenuItemRow(
                icon = Icons.Outlined.Refresh,
                label = stringResource(R.string.library_menu_rescan),
                onClick = onRescan
            )
            MenuItemRow(
                icon = Icons.AutoMirrored.Outlined.OpenInNew,
                label = stringResource(R.string.library_menu_add_to_launcher),
                onClick = {
                    onDismiss()
                    if (!pinCategoryShortcut(context, category, categoryLabel)) {
                        Toast.makeText(context, unsupportedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            )
            if (onListOptions != null) {
                MenuItemRow(
                    icon = Icons.Outlined.Tune,
                    label = stringResource(R.string.library_menu_list_options),
                    onClick = onListOptions
                )
            }
            if (onViewMode != null) {
                MenuItemRow(
                    icon = Icons.Outlined.ViewAgenda,
                    label = stringResource(R.string.library_menu_view_mode),
                    onClick = onViewMode
                )
            }
        }
    }
}

private val SelectedRowCorner = 12.dp
private const val HomeCarouselLimit = 20

internal class LibraryMenuAction(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
private fun TileListEmptyMessage(message: String) {
    val colors = LocalClintColors.current
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = colors.secondaryText,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp)
    )
}

@Composable
private fun MenuItemRow(icon: ImageVector, label: String, onClick: () -> Unit) {
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

@Composable
private fun EmptyLibraryState(onAddFolders: () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KateIcon(
            imageVector = Icons.Outlined.LibraryMusic,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = stringResource(R.string.placeholder_library_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = stringResource(R.string.placeholder_library_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        KateActionButton(
            label = stringResource(R.string.library_add_folder_button),
            icon = Icons.Outlined.CreateNewFolder,
            allowIconOnly = false,
            minHeight = 52.dp,
            onClick = onAddFolders,
            modifier = Modifier.padding(top = 28.dp)
        )
    }
}

@Composable
private fun ScanningState() {
    val colors = LocalClintColors.current
    val progress = LibraryStore.scanProgress
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = colors.primary, strokeWidth = 3.dp, modifier = Modifier.size(44.dp))
        Text(
            text = stringResource(R.string.library_scanning_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = if (progress == null || (progress.phase == ScanPhase.FINDING && progress.found == 0)) {
                stringResource(R.string.library_scanning_message)
            } else {
                scanStatusText(progress)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        ScanProgressBar(
            progress = progress,
            modifier = Modifier
                .padding(top = 20.dp)
                .widthIn(max = 260.dp)
                .fillMaxWidth()
                .height(4.dp)
        )
    }
}

@Composable
private fun NoResultsState(onAddFolders: () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KateIcon(
            imageVector = Icons.Outlined.MusicOff,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = stringResource(R.string.library_no_results_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = stringResource(R.string.library_no_results_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        KateActionButton(
            label = stringResource(R.string.library_add_folder_button),
            minHeight = 52.dp,
            onClick = onAddFolders,
            modifier = Modifier.padding(top = 28.dp)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TileListState(
    category: LibraryCategory,
    tiles: List<LibraryTile>,
    playTracks: () -> List<Track>,
    sortable: Boolean,
    onAddFolders: () -> Unit,
    onBack: () -> Unit,
    onSelectCategory: (LibraryCategory) -> Unit,
    contentScope: AnimatedVisibilityScope,
    bottomBarConnection: NestedScrollConnection?,
    onTileClick: (Int) -> Unit,
    resolveSelection: ((Set<String>) -> List<Track>)? = null,
    extraMenuActions: List<LibraryMenuAction> = emptyList(),
    emptyMessage: String? = null,
    headerExtra: (@Composable () -> Unit)? = null
) {
    val colors = LocalClintColors.current
    val bottomInset = LocalBottomContentPadding.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptics = LocalKateHaptics.current
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    val morph = remember(scope) { ViewModeMorphState(scope) }
    var showMenu by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var showViewModeOptions by remember { mutableStateOf(false) }
    var showSwitcher by remember { mutableStateOf(false) }
    var selectedIds by remember(category) { mutableStateOf<Set<String>?>(null) }
    val tileClick: (Int, String) -> Unit = { position, id ->
        if (selectedIds != null) {
            haptics.tick()
            selectedIds = selectedIds.toggled(id)
        } else {
            onTileClick(position)
        }
    }
    val tileLongClick: ((String) -> Unit)? = if (resolveSelection == null) {
        null
    } else {
        { id ->
            haptics.longPress()
            selectedIds = (selectedIds ?: emptySet()) + id
        }
    }
    val countText = categoryCountText(category, tiles.size)
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val viewMode = LibraryStore.viewModeFor(category)
    val currentTrackId = PlayerController.currentTrack?.id
    val pullState = rememberPullToRefreshState()
    val refreshing = LibraryStore.scanProgress?.phase == ScanPhase.FINDING
    val morphing = morph.session != null
    val placementSpec: FiniteAnimationSpec<IntOffset>? = if (morphing) {
        null
    } else {
        spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = IntOffset.VisibilityThreshold)
    }
    val header: @Composable () -> Unit = {
        Column {
            SharedTransitionLayout {
                LibraryTopBar(
                    category = category,
                    countText = countText,
                    showMenu = false,
                    categoryScope = null,
                    onBack = {},
                    onSwitchCategory = {},
                    onMenuToggle = {},
                    onShuffleClick = {},
                    onPlayClick = {}
                )
            }
            headerExtra?.invoke()
        }
    }

    WithSharedTransitionScope {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .viewModePinch(morph)
        ) {
            val viewportWidth = constraints.maxWidth
            val viewportHeight = constraints.maxHeight
            val bottomInsetPx = with(density) { bottomInset.roundToPx() }
            val trackCount = tiles.size
            val cornerScale = LibraryStylePreferences.cornerScale(LocalLookAndFeel.current.libraryCorners)
            val geometries = remember(viewportWidth, viewportHeight, headerHeightPx, bottomInsetPx, trackCount, density, cornerScale) {
                LibraryViewMode.entries.associateWith { mode ->
                    computeModeGeometry(mode, viewportWidth, viewportHeight, headerHeightPx, bottomInsetPx, trackCount, density, cornerScale)
                }
            }
            val geometry = geometries.getValue(viewMode)
            val style = geometry.style
            val staticFrame = remember(style) { TileFrame(style, 0, 0) }
            val alphabetEnabled = LocalLookAndFeel.current.libraryAlphabetIndex
            val sections = remember(tiles, alphabetEnabled) {
                if (alphabetEnabled) alphabetSections(tiles.map { it.title }) else null
            }
            val firstVisibleTile by remember(geometry.isGrid) {
                derivedStateOf {
                    (if (geometry.isGrid) gridState.firstVisibleItemIndex else listState.firstVisibleItemIndex) - 1
                }
            }
            val listScrolling = if (geometry.isGrid) gridState.isScrollInProgress else listState.isScrollInProgress
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

            SideEffect {
                morph.pinchEnabled = !showMenu && !showSortOptions && !showViewModeOptions && !showSwitcher && trackCount > 0
                morph.currentMode = { LibraryStore.viewModeFor(category) }
                morph.onStep = { haptics.tick() }
                morph.captureSession = capture@{
                    val current = geometries.getValue(LibraryStore.viewModeFor(category))
                    if (headerHeightPx <= 0 || trackCount == 0) return@capture null
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
                morph.commit = { mode ->
                    val target = geometries.getValue(mode)
                    val session = morph.session
                    val scroll = if (session != null) target.scrollFor(session).roundToInt() else 0
                    val (index, offset) = target.lazyPosition(scroll)
                    if (target.isGrid) {
                        gridState.requestScrollToItem(index, offset)
                    } else {
                        listState.requestScrollToItem(index, offset)
                    }
                    LibraryStore.setViewMode(context, category, mode)
                }
            }

            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = { LibraryStore.startScan(context) },
                state = pullState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (morph.session != null) 0f else 1f },
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullState,
                        isRefreshing = refreshing,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = colors.cardBackground,
                        color = colors.primary
                    )
                }
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
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(modifier = Modifier.onSizeChanged { headerHeightPx = it.height }) {
                                LibraryTopBar(
                                    category = category,
                                    countText = countText,
                                    showMenu = showMenu,
                                    categoryScope = contentScope,
                                    onBack = onBack,
                                    onSwitchCategory = { showSwitcher = true },
                                    onMenuToggle = { showMenu = !showMenu },
                                    onShuffleClick = { PlayerController.playShuffled(context, playTracks()) },
                                    onPlayClick = { PlayerController.playAll(context, playTracks()) },
                                    onShuffleLongClick = { ShufflePickRequest.open(playTracks()) }
                                )
                                headerExtra?.invoke()
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
                                    .selectionHighlight(selectedIds?.contains(tile.id) == true, colors.primary, style.artCorner)
                                    .combinedClickable(
                                        onClick = { tileClick(position, tile.id) },
                                        onLongClick = tileLongClick?.let { longClick -> { longClick(tile.id) } }
                                    )
                            )
                        }
                        if (tiles.isEmpty() && emptyMessage != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) { TileListEmptyMessage(emptyMessage) }
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                    if (sections == null) {
                        VerticalScrollbar(
                            state = gridState,
                            thumbColor = colors.primary
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it },
                        contentPadding = PaddingValues(bottom = bottomInset),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 900.dp)
                                    .fillMaxWidth()
                                    .onSizeChanged { headerHeightPx = it.height }
                            ) {
                                LibraryTopBar(
                                    category = category,
                                    countText = countText,
                                    showMenu = showMenu,
                                    categoryScope = contentScope,
                                    onBack = onBack,
                                    onSwitchCategory = { showSwitcher = true },
                                    onMenuToggle = { showMenu = !showMenu },
                                    onShuffleClick = { PlayerController.playShuffled(context, playTracks()) },
                                    onPlayClick = { PlayerController.playAll(context, playTracks()) },
                                    onShuffleLongClick = { ShufflePickRequest.open(playTracks()) }
                                )
                                headerExtra?.invoke()
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
                                    .exactSize(geometry.rowWidth, geometry.rowHeight)
                                    .selectionHighlight(selectedIds?.contains(tile.id) == true, colors.primary, SelectedRowCorner)
                                    .combinedClickable(
                                        onClick = { tileClick(position, tile.id) },
                                        onLongClick = tileLongClick?.let { longClick -> { longClick(tile.id) } }
                                    )
                            )
                        }
                        if (tiles.isEmpty() && emptyMessage != null) {
                            item { TileListEmptyMessage(emptyMessage) }
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                    if (sections == null) {
                        VerticalScrollbar(
                            state = listState,
                            thumbColor = colors.primary
                        )
                    }
                }
            }
            if (sections != null && morph.session == null) {
                SectionPill(
                    letter = tiles.getOrNull(firstVisibleTile)?.let { sectionLetter(it.title) },
                    scrolling = listScrolling
                )
                AlphabetRail(
                    sections = sections,
                    topPadding = with(density) { headerHeightPx.toDp() },
                    bottomPadding = bottomInset + 8.dp
                ) { tileIndex ->
                    scope.launch {
                        if (geometry.isGrid) gridState.scrollToItem(tileIndex + 1) else listState.scrollToItem(tileIndex + 1)
                    }
                }
            }
            JumpToPlayingButton(
                visible = playingIndex >= 0 && !playingVisible && morph.session == null && selectedIds == null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = if (sections != null) 44.dp else 20.dp, bottom = bottomInset + 16.dp),
                onClick = {
                    val target = playingIndex + 1
                    scope.launch {
                        if (geometry.isGrid) {
                            val columns = geometry.columns.coerceAtLeast(1)
                            val row = playingIndex / columns
                            gridState.animateScrollToItem(maxOf(1, (row - 1) * columns + 1))
                        } else {
                            listState.animateScrollToItem(maxOf(1, target - 2))
                        }
                    }
                }
            )
            morph.session?.let { session ->
                ViewModeMorphOverlay(
                    morph = morph,
                    session = session,
                    geometries = geometries,
                    header = header
                )
            }
            if (resolveSelection != null) {
                SelectionOverlay(
                    selectedCount = selectedIds?.size,
                    resolveTracks = { selectedIds?.let(resolveSelection).orEmpty() },
                    onSelectAll = { selectedIds = tiles.mapTo(HashSet()) { it.id } },
                    onClose = { selectedIds = null }
                )
            }
            LibraryOverflowMenu(
                category = category,
                visible = showMenu,
                onDismiss = { showMenu = false },
                onSelectFolders = {
                    showMenu = false
                    onAddFolders()
                },
                onRescan = {
                    showMenu = false
                    LibraryStore.startScan(context)
                },
                onListOptions = if (sortable) {
                    {
                        showMenu = false
                        showSortOptions = true
                    }
                } else {
                    null
                },
                onViewMode = {
                    showMenu = false
                    showViewModeOptions = true
                },
                extraActions = extraMenuActions
            )
            CategorySwitcherDialog(
                visible = showSwitcher,
                current = category,
                onDismiss = { showSwitcher = false },
                onSelect = {
                    showSwitcher = false
                    onSelectCategory(it)
                }
            )
            SortOptionsDialog(
                visible = showSortOptions,
                currentOrder = LibraryStore.sortOrder,
                currentDirection = LibraryStore.sortDirection,
                onDismiss = { showSortOptions = false },
                onSelect = { order ->
                    LibraryStore.setSortOrder(context, order)
                    showSortOptions = false
                },
                onSelectDirection = { direction ->
                    LibraryStore.setSortDirection(context, direction)
                }
            )
            ViewModeOptionsDialog(
                visible = showViewModeOptions,
                currentMode = viewMode,
                onDismiss = { showViewModeOptions = false },
                onSelect = { mode ->
                    showViewModeOptions = false
                    if (!morph.morphTo(mode)) {
                        LibraryStore.setViewMode(context, category, mode)
                    }
                }
            )
        }
    }
}

private fun Modifier.exactSize(width: Int, height: Int): Modifier = layout { measurable, _ ->
    val placeable = measurable.measure(Constraints.fixed(width, height))
    layout(width, height) {
        placeable.placeRelative(0, 0)
    }
}

internal fun orderedCategories(look: LookAndFeelSettings, group: List<LibraryCategory>): List<LibraryCategory> =
    look.libraryCategoryOrder
        .mapNotNull { key -> group.firstOrNull { it.key == key } }
        .filter { it.key !in look.libraryHiddenCategories }

@Composable
private fun rememberPinnedShelf(pin: String): Pair<String, List<Track>>? {
    val library = LibraryStore.tracks
    return when {
        pin.startsWith(LibraryLayoutPreferences.PIN_PLAYLIST) -> {
            val playlist = PlaylistStore.playlists.firstOrNull { it.id == pin.removePrefix(LibraryLayoutPreferences.PIN_PLAYLIST) } ?: return null
            val tracks = remember(playlist, library.size) {
                val byId = library.associateBy { it.id }
                playlist.trackIds.mapNotNull { byId[it] }
            }
            playlist.name to tracks
        }
        pin.startsWith(LibraryLayoutPreferences.PIN_SMART) -> {
            val playlist = SmartPlaylistStore.get(pin.removePrefix(LibraryLayoutPreferences.PIN_SMART)) ?: return null
            val plays = PlayCounts.stats
            val favorites = FavoritesStore.ids
            val tracks = remember(playlist, library.size, plays, favorites) {
                SmartPlaylistEngine.evaluate(playlist, library.toList(), plays, favorites.toSet())
            }
            playlist.name to tracks
        }
        else -> null
    }
}
