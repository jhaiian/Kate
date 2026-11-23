package com.jhaiian.kate.ui

import com.jhaiian.kate.ui.theme.LibraryLayoutPreferences
import android.widget.Toast
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import com.jhaiian.kate.ui.tags.TagEditorHost
import com.jhaiian.kate.ui.player.LrcLibSearchHost
import com.jhaiian.kate.ui.player.LyricsMakerHost
import com.jhaiian.kate.ui.theme.kateNavBarColor
import com.jhaiian.kate.ui.components.KateNavIcon
import android.net.Uri
import androidx.activity.BackEventCompat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.ListeningStats
import com.jhaiian.kate.data.PlaybackStateStore
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.LocalNavAnimatedScope
import com.jhaiian.kate.ui.components.LocalSharedTransitionScope
import com.jhaiian.kate.ui.components.PanePlaceholder
import com.jhaiian.kate.ui.components.TwoPaneLayout
import com.jhaiian.kate.ui.components.rememberKateHaptics
import com.jhaiian.kate.ui.components.rememberVerticalFold
import com.jhaiian.kate.ui.components.KateRadioHost
import com.jhaiian.kate.ui.stats.AchievementToastHost
import com.jhaiian.kate.ui.stats.AchievementsScreen
import com.jhaiian.kate.ui.components.LibraryShuffleModeHost
import com.jhaiian.kate.ui.library.AlbumDetailScreen
import com.jhaiian.kate.ui.library.ArtistDetailScreen
import com.jhaiian.kate.ui.library.GroupDetailScreen
import com.jhaiian.kate.ui.library.LibraryScreen
import com.jhaiian.kate.ui.library.PlaylistDetailScreen
import com.jhaiian.kate.ui.library.SmartPlaylistDetailScreen
import com.jhaiian.kate.ui.player.PlayerSheet
import com.jhaiian.kate.ui.player.miniPlayerReservedSpace
import com.jhaiian.kate.ui.player.rememberPlayerSheetState
import com.jhaiian.kate.ui.search.SearchScreen
import com.jhaiian.kate.ui.settings.AudioAdvancedScreen
import com.jhaiian.kate.ui.settings.AudioHeadphonesScreen
import com.jhaiian.kate.ui.settings.AudioPages
import com.jhaiian.kate.ui.settings.LocalSettingsPage
import com.jhaiian.kate.ui.settings.rememberSpotlightBringIntoViewSpec
import com.jhaiian.kate.ui.settings.AudioPlaybackScreen
import com.jhaiian.kate.ui.settings.AudioSettingsScreen
import com.jhaiian.kate.ui.settings.AudioSoundScreen
import com.jhaiian.kate.ui.settings.AudioVolumeScreen
import com.jhaiian.kate.ui.settings.AboutScreen
import com.jhaiian.kate.ui.settings.BackupScreen
import com.jhaiian.kate.ui.settings.SupportScreen
import com.jhaiian.kate.ui.settings.UpdateSettingsScreen
import com.jhaiian.kate.update.UpdateFlowHost
import com.jhaiian.kate.ui.components.DocumentViewerHost
import com.jhaiian.kate.ui.settings.CrashLogScreen
import com.jhaiian.kate.ui.settings.DisplayLookScreen
import com.jhaiian.kate.ui.settings.EqualizerScreen
import com.jhaiian.kate.ui.settings.ParametricEqScreen
import com.jhaiian.kate.ui.settings.ControlsScreen
import com.jhaiian.kate.ui.settings.LibraryLookScreen
import com.jhaiian.kate.ui.settings.ButtonsLookScreen
import com.jhaiian.kate.ui.settings.LookAndFeelHubScreen
import com.jhaiian.kate.ui.settings.MotionLookScreen
import com.jhaiian.kate.ui.settings.NavigationLookScreen
import com.jhaiian.kate.ui.settings.PlayerLookScreen
import com.jhaiian.kate.ui.settings.SettingsScreen
import com.jhaiian.kate.ui.settings.TagSettingsScreen
import com.jhaiian.kate.ui.settings.OnlineSettingsScreen
import com.jhaiian.kate.ui.settings.CustomThemesScreen
import com.jhaiian.kate.ui.settings.ThemeColorsScreen
import com.jhaiian.kate.ui.settings.StylesScreen
import com.jhaiian.kate.ui.settings.VisualizerScreen
import com.jhaiian.kate.ui.components.CustomImageBackdrop
import com.jhaiian.kate.ui.settings.StyleAutoSwitch
import com.jhaiian.kate.ui.stats.StatsScreen
import com.jhaiian.kate.ui.stats.WrappedOverlay
import com.jhaiian.kate.ui.theme.ArtworkColorScope
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.LocalLookAndFeelUpdate
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.NavigationPreferences
import com.jhaiian.kate.ui.theme.clintNavigationBarItemColors
import com.jhaiian.kate.ui.theme.clintNavigationRailItemColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull

private data class KateDestination(val route: String, val labelRes: Int, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

private val kateDestinations = listOf(
    KateDestination("library", R.string.nav_library, Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    KateDestination("search", R.string.nav_search, Icons.Filled.Search, Icons.Outlined.Search),
    KateDestination("stats", R.string.nav_stats, Icons.Filled.Insights, Icons.Outlined.Insights),
    KateDestination("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
)

private const val NavSlideMillis = 320
private const val NavFadeInMillis = 220
private const val NavFadeInDelayMillis = 80
private const val NavFadeOutMillis = 100
private const val PaneSlideMillis = 260
private const val PaneCollapseDelayMillis = 400L

private val LibraryPaneHosts = setOf("library", "search", "stats")
private const val SettingsPaneHost = "settings"

private val PaneStackSaver = listSaver<List<String>, String>(save = { it }, restore = { it })

private fun albumRoute(key: String) = "library/album?key=${Uri.encode(key)}"

private fun artistRoute(key: String, albumArtist: Boolean) =
    "library/artist?key=${Uri.encode(key)}&albumArtist=$albumArtist"

private fun playlistRoute(id: String) = "library/playlist?id=${Uri.encode(id)}"

private fun smartPlaylistRoute(id: String) = "library/smart?id=${Uri.encode(id)}"

private fun groupRoute(category: LibraryCategory, key: String) =
    "library/group?category=${category.key}&key=${Uri.encode(key)}"

private fun AnimatedContentTransitionScope<NavBackStackEntry>.isDrillDown(): Boolean {
    val from = initialState.destination.route.orEmpty()
    val to = targetState.destination.route.orEmpty()
    return to.startsWith("$from/") || from.startsWith("$to/") ||
        (from.contains('/') && to.contains('/') && from.substringBefore('/') == to.substringBefore('/'))
}

private fun isRouteSelected(currentRoute: String?, route: String): Boolean =
    currentRoute == route || currentRoute?.startsWith("$route/") == true

private fun AnimatedContentTransitionScope<NavBackStackEntry>.kateEnter(forward: Boolean): EnterTransition {
    val fade = fadeIn(tween(durationMillis = NavFadeInMillis, delayMillis = NavFadeInDelayMillis, easing = LinearOutSlowInEasing))
    return if (isDrillDown()) {
        slideInHorizontally(tween(durationMillis = NavSlideMillis, easing = FastOutSlowInEasing)) { width ->
            (if (forward) width else -width) / 6
        } + fade
    } else {
        fade + scaleIn(
            animationSpec = tween(durationMillis = NavSlideMillis, easing = FastOutSlowInEasing),
            initialScale = 0.96f
        )
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.kateExit(forward: Boolean): ExitTransition {
    val fade = fadeOut(tween(durationMillis = NavFadeOutMillis, easing = FastOutLinearInEasing))
    return if (isDrillDown()) {
        slideOutHorizontally(tween(durationMillis = NavSlideMillis, easing = FastOutSlowInEasing)) { width ->
            (if (forward) -width else width) / 6
        } + fade
    } else {
        fade
    }
}

private fun predictiveSlideSign(swipeEdge: Int): Int =
    if (swipeEdge == BackEventCompat.EDGE_RIGHT) -1 else 1

private fun AnimatedContentTransitionScope<NavBackStackEntry>.katePredictiveEnter(swipeEdge: Int): EnterTransition {
    val spec = tween<IntOffset>(durationMillis = NavSlideMillis, easing = LinearEasing)
    return if (isDrillDown()) {
        slideInHorizontally(spec) { width -> -predictiveSlideSign(swipeEdge) * width / 6 }
    } else {
        scaleIn(
            animationSpec = tween(durationMillis = NavSlideMillis, easing = LinearEasing),
            initialScale = 0.96f
        )
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.katePredictiveExit(swipeEdge: Int): ExitTransition {
    val fade = fadeOut(tween(durationMillis = NavSlideMillis, easing = LinearEasing))
    return if (isDrillDown()) {
        slideOutHorizontally(tween(durationMillis = NavSlideMillis, easing = LinearEasing)) { width ->
            predictiveSlideSign(swipeEdge) * width / 6
        } + fade
    } else {
        fade
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun KateApp(
    windowWidthSizeClass: WindowWidthSizeClass,
    navController: NavHostController,
    lookAndFeel: LookAndFeelSettings,
    onLookAndFeelChange: (LookAndFeelSettings) -> Unit,
    startRoute: String? = null
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination
    val currentRouteName = currentRoute?.route

    val useRail = windowWidthSizeClass != WindowWidthSizeClass.Compact
    val fold = rememberVerticalFold()
    val twoPane = lookAndFeel.twoPane && useRail &&
        (windowWidthSizeClass == WindowWidthSizeClass.Expanded || fold != null)
    var paneStack by rememberSaveable(stateSaver = PaneStackSaver) { mutableStateOf(emptyList<String>()) }
    var paneSplit by remember { mutableFloatStateOf(NavigationPreferences.getTwoPaneSplit(context)) }
    val pushPane: (String) -> Unit = { route -> paneStack = paneStack + route }
    val popPane: () -> Unit = { paneStack = paneStack.dropLast(1) }
    val playerSheet = rememberPlayerSheetState()
    val haptics = rememberKateHaptics(lookAndFeel.haptics)
    val visibleDestinations = lookAndFeel.visibleTabs.mapNotNull { key -> kateDestinations.firstOrNull { it.route == key } }
    val labelMode = lookAndFeel.navLabels
    val currentTrack = PlayerController.currentTrack

    LaunchedEffect(playerSheet) {
        if (PlaybackStateStore.isPlayerExpanded(context)) {
            val restored = withTimeoutOrNull(5_000L) {
                snapshotFlow { PlayerController.currentTrack }.filterNotNull().first()
            }
            if (restored != null) playerSheet.snapTo(1f)
        }
        snapshotFlow { playerSheet.isAnimating to playerSheet.progress }
            .filter { (animating, _) -> !animating }
            .map { (_, progress) -> progress > 0.5f }
            .distinctUntilChanged()
            .collect { expanded -> PlaybackStateStore.setPlayerExpanded(context, expanded) }
    }

    fun navigate(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun onDestinationClick(route: String) {
        haptics.click()
        paneStack = emptyList()
        if (route == "library" && isRouteSelected(currentRouteName, route)) {
            navController.popBackStack("library", inclusive = false)
            LibraryStore.openCategory(context, null)
        } else {
            navigate(route)
        }
    }

    val onTrackClick: (List<Track>, Track) -> Unit = { list, track ->
        when (lookAndFeel.songTapAction) {
            LibraryLayoutPreferences.TAP_PLAY_NEXT -> {
                PlayerController.playNext(context, listOf(track))
                Toast.makeText(context, context.resources.getQuantityString(R.plurals.selection_play_next_done, 1, 1), Toast.LENGTH_SHORT).show()
            }
            LibraryLayoutPreferences.TAP_ADD_TO_QUEUE -> {
                PlayerController.addToQueue(context, listOf(track))
                Toast.makeText(context, context.resources.getQuantityString(R.plurals.selection_add_to_queue_done, 1, 1), Toast.LENGTH_SHORT).show()
            }
            else -> {
                if (PlayerController.currentTrack?.id != track.id) {
                    val queue = if (lookAndFeel.songTapAction == LibraryLayoutPreferences.TAP_PLAY_ONLY) listOf(track) else list
                    PlayerController.playTrack(context, queue, track)
                }
                if (lookAndFeel.songTapOpensPlayer) playerSheet.expand()
            }
        }
    }
    val openLibraryRoute: (String) -> Unit = { route ->
        if (twoPane && currentRouteName in LibraryPaneHosts) paneStack = listOf(route) else navController.navigate(route)
    }
    val openSettingsRoute: (String) -> Unit = { route ->
        if (twoPane && currentRouteName == SettingsPaneHost) paneStack = listOf(route) else navController.navigate(route)
    }
    val openAlbum: (String) -> Unit = { key -> openLibraryRoute(albumRoute(key)) }
    val openArtist: (String, Boolean) -> Unit = { key, albumArtist -> openLibraryRoute(artistRoute(key, albumArtist)) }
    val openPlaylist: (String) -> Unit = { id -> openLibraryRoute(playlistRoute(id)) }
    val openSmartPlaylist: (String) -> Unit = { id -> openLibraryRoute(smartPlaylistRoute(id)) }
    val openGroup: (LibraryCategory, String) -> Unit = { category, key -> openLibraryRoute(groupRoute(category, key)) }

    var wrappedStats by remember { mutableStateOf<ListeningStats?>(null) }
    val playFromStats: (List<Track>, Track) -> Unit = { list, track ->
        PlayerController.playTrack(context, list, track)
    }
    var navBarHeightPx by remember { mutableIntStateOf(0) }
    var railWidthPx by remember { mutableIntStateOf(0) }
    var bottomBarHidden by remember { mutableStateOf(false) }

    LaunchedEffect(currentRouteName) { bottomBarHidden = false }

    var startRouteApplied by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!startRouteApplied) {
            startRouteApplied = true
            if (startRoute != null && startRoute != "library" && startRoute in lookAndFeel.visibleTabs) {
                navigate(startRoute)
            }
        }
    }

    val openPlayerRequest = PlayerController.openPlayerRequest
    LaunchedEffect(openPlayerRequest) {
        if (openPlayerRequest > 0) {
            val track = withTimeoutOrNull(5_000L) {
                snapshotFlow { PlayerController.currentTrack }.filterNotNull().first()
            }
            if (track != null) playerSheet.expand()
        }
    }

    val libraryTabRequest = LibraryStore.libraryTabRequest
    LaunchedEffect(libraryTabRequest) {
        if (libraryTabRequest > 0) {
            playerSheet.collapse()
            paneStack = emptyList()
            navController.popBackStack(navController.graph.findStartDestination().id, inclusive = false)
        }
    }

    val bottomBarHide = animateFloatAsState(
        targetValue = if (bottomBarHidden && !useRail) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "bottom_bar_hide"
    )

    val libraryNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -1f) bottomBarHidden = true
                else if (available.y > 1f) bottomBarHidden = false
                return Offset.Zero
            }
        }
    }

    val systemBottomInsetPx = WindowInsets.navigationBars.getBottom(density)
    val navBarHeight = if (useRail) 0.dp else with(density) { navBarHeightPx.toDp() }
    val systemBottomInset = with(density) { systemBottomInsetPx.toDp() }
    val miniPlayerSpace = if (currentTrack != null) miniPlayerReservedSpace(lookAndFeel.miniPlayerDocked) else 0.dp
    val bottomContentPadding = max(navBarHeight, systemBottomInset) + miniPlayerSpace

    LaunchedEffect(twoPane) {
        if (!twoPane && paneStack.isNotEmpty()) {
            delay(PaneCollapseDelayMillis)
            val routes = paneStack
            paneStack = emptyList()
            routes.forEach { navController.navigate(it) }
        }
    }

    val paneStateHolder = rememberSaveableStateHolder()
    val paneRoute: @Composable (String) -> Unit = { route ->
        val uri = Uri.parse("kate://app/$route")
        when (uri.path.orEmpty().removePrefix("/")) {
            "library/album" -> AlbumDetailScreen(
                albumKey = uri.getQueryParameter("key").orEmpty(),
                bottomBarConnection = null,
                onBack = popPane,
                onTrackClick = onTrackClick,
                onArtistClick = { key, albumArtist -> pushPane(artistRoute(key, albumArtist)) }
            )
            "library/artist" -> ArtistDetailScreen(
                artistKey = uri.getQueryParameter("key").orEmpty(),
                albumArtist = uri.getBooleanQueryParameter("albumArtist", false),
                bottomBarConnection = null,
                onBack = popPane,
                onTrackClick = onTrackClick,
                onAlbumClick = { key -> pushPane(albumRoute(key)) }
            )
            "library/playlist" -> PlaylistDetailScreen(
                playlistId = uri.getQueryParameter("id").orEmpty(),
                bottomBarConnection = null,
                onBack = popPane,
                onTrackClick = onTrackClick
            )
            "library/smart" -> SmartPlaylistDetailScreen(
                smartId = uri.getQueryParameter("id").orEmpty(),
                bottomBarConnection = null,
                onBack = popPane,
                onTrackClick = onTrackClick,
                onOpenSmartPlaylist = { id -> pushPane(smartPlaylistRoute(id)) }
            )
            "library/group" -> GroupDetailScreen(
                category = LibraryCategory.fromKey(uri.getQueryParameter("category")) ?: LibraryCategory.GENRES,
                groupKey = uri.getQueryParameter("key").orEmpty(),
                bottomBarConnection = null,
                onBack = popPane,
                onTrackClick = onTrackClick
            )
            "stats/achievements" -> AchievementsScreen(
                bottomBarConnection = null,
                onBack = popPane
            )
            "settings/audio" -> AudioSettingsScreen(
                onBack = popPane,
                onEqualizerClick = { pushPane("settings/equalizer") },
                onPageClick = { pushPane(it) }
            )
            AudioPages.SOUND -> AudioSoundScreen(onBack = popPane, onEqualizerClick = { pushPane("settings/equalizer") })
            AudioPages.VOLUME -> AudioVolumeScreen(onBack = popPane)
            AudioPages.PLAYBACK -> AudioPlaybackScreen(onBack = popPane)
            AudioPages.HEADPHONES -> AudioHeadphonesScreen(onBack = popPane)
            AudioPages.ADVANCED -> AudioAdvancedScreen(onBack = popPane)
            "settings/equalizer" -> EqualizerScreen(onBack = popPane, onParametricClick = { pushPane("settings/equalizer/parametric") })
            "settings/equalizer/parametric" -> ParametricEqScreen(onBack = popPane)
            "settings/tags" -> TagSettingsScreen(onBack = popPane)
            "settings/online" -> OnlineSettingsScreen(onBack = popPane)
            "settings/controls" -> ControlsScreen(onBack = popPane)
            "settings/crash_logs" -> CrashLogScreen(onBack = popPane)
            "settings/backup" -> BackupScreen(onBack = popPane)
            "settings/about" -> AboutScreen(onBack = popPane)
            "settings/updates" -> UpdateSettingsScreen(onBack = popPane)
            "settings/support" -> SupportScreen(onBack = popPane)
            "settings/look_and_feel" -> LookAndFeelHubScreen(
                settings = lookAndFeel,
                onSettingsChange = onLookAndFeelChange,
                onThemeColorsClick = { pushPane("settings/look_and_feel/theme") },
                onPlayerClick = { pushPane("settings/look_and_feel/player") },
                onButtonsClick = { pushPane("settings/look_and_feel/buttons") },
                onLibraryClick = { pushPane("settings/look_and_feel/library") },
                onNavigationClick = { pushPane("settings/look_and_feel/navigation") },
                onDisplayClick = { pushPane("settings/look_and_feel/display") },
                onMotionClick = { pushPane("settings/look_and_feel/motion") },
                onBack = popPane,
                onStylesClick = { pushPane("settings/look_and_feel/styles") },
                onVisualizerClick = { pushPane("settings/look_and_feel/visualizer") }
            )
            "settings/look_and_feel/visualizer" -> VisualizerScreen(lookAndFeel, onLookAndFeelChange, popPane)
            "settings/look_and_feel/styles" -> StylesScreen(lookAndFeel, onLookAndFeelChange, popPane)
            "settings/look_and_feel/theme" -> ThemeColorsScreen(
                lookAndFeel,
                onLookAndFeelChange,
                popPane,
                onCustomThemesClick = { pushPane("settings/look_and_feel/custom_themes") }
            )
            "settings/look_and_feel/custom_themes" -> CustomThemesScreen(lookAndFeel, onLookAndFeelChange, popPane)
            "settings/look_and_feel/player" -> PlayerLookScreen(lookAndFeel, onLookAndFeelChange, popPane)
            "settings/look_and_feel/buttons" -> ButtonsLookScreen(lookAndFeel, onLookAndFeelChange, popPane)
            "settings/look_and_feel/library" -> LibraryLookScreen(lookAndFeel, onLookAndFeelChange, popPane)
            "settings/look_and_feel/navigation" -> NavigationLookScreen(lookAndFeel, onLookAndFeelChange, popPane)
            "settings/look_and_feel/motion" -> MotionLookScreen(lookAndFeel, onLookAndFeelChange, popPane)
            "settings/look_and_feel/display" -> DisplayLookScreen(lookAndFeel, onLookAndFeelChange, popPane)
            else -> LaunchedEffect(route) { popPane() }
        }
    }
    val paneHost: @Composable (Boolean, @Composable () -> Unit) -> Unit = { settingsHost, list ->
        if (twoPane) {
            BackHandler(enabled = paneStack.isNotEmpty()) { popPane() }
            TwoPaneLayout(
                fold = fold,
                split = paneSplit,
                onSplitChange = { paneSplit = it },
                onSplitSettled = { NavigationPreferences.setTwoPaneSplit(context, paneSplit) },
                modifier = Modifier.fillMaxSize(),
                first = list,
                second = {
                    CompositionLocalProvider(LocalNavAnimatedScope provides null) {
                        AnimatedContent(
                            targetState = paneStack,
                            contentKey = { stack -> "${stack.size}:${stack.lastOrNull()}" },
                            transitionSpec = {
                                val forward = targetState.size >= initialState.size
                                val spec = tween<IntOffset>(durationMillis = PaneSlideMillis, easing = FastOutSlowInEasing)
                                (slideInHorizontally(spec) { width -> (if (forward) width else -width) / 8 } +
                                    fadeIn(tween(durationMillis = NavFadeInMillis, delayMillis = NavFadeInDelayMillis))) togetherWith
                                    (slideOutHorizontally(spec) { width -> (if (forward) -width else width) / 8 } +
                                        fadeOut(tween(durationMillis = NavFadeOutMillis)))
                            },
                            label = "detail_pane"
                        ) { stack ->
                            val route = stack.lastOrNull()
                            if (route == null) {
                                PanePlaceholder(
                                    icon = if (settingsHost) Icons.Outlined.Settings else Icons.Outlined.LibraryMusic,
                                    title = stringResource(
                                        if (settingsHost) R.string.pane_settings_empty_title else R.string.pane_library_empty_title
                                    ),
                                    message = stringResource(
                                        if (settingsHost) R.string.pane_settings_empty_message else R.string.pane_library_empty_message
                                    )
                                )
                            } else {
                                paneStateHolder.SaveableStateProvider(key = "${stack.size}:$route") {
                                    CompositionLocalProvider(LocalSettingsPage provides route) {
                                        paneRoute(route)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        } else {
            list()
        }
    }

    val appImage = lookAndFeel.appBackgroundImage
    val contentColors = if (appImage.isEmpty()) colors else colors.copy(background = colors.background.copy(alpha = lookAndFeel.imageDim / 100f))
    val spotlightSpec = rememberSpotlightBringIntoViewSpec()
    val content: @Composable (Modifier) -> Unit = { contentModifier ->
        CompositionLocalProvider(LocalClintColors provides contentColors) {
        SharedTransitionLayout(modifier = contentModifier) {
            CompositionLocalProvider(LocalSharedTransitionScope provides this, LocalBringIntoViewSpec provides spotlightSpec) {
                NavHost(
                    navController = navController,
                    startDestination = "library",
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { kateEnter(forward = true) },
                    exitTransition = { kateExit(forward = true) },
                    popEnterTransition = { kateEnter(forward = false) },
                    popExitTransition = { kateExit(forward = false) },
                    predictivePopEnterTransition = { swipeEdge -> katePredictiveEnter(swipeEdge) },
                    predictivePopExitTransition = { swipeEdge -> katePredictiveExit(swipeEdge) }
                ) {
                    composable("library") {
                        val animatedScope = this
                        paneHost(false) {
                            CompositionLocalProvider(LocalNavAnimatedScope provides animatedScope) {
                                LibraryScreen(
                                    bottomBarConnection = if (useRail) null else libraryNestedScrollConnection,
                                    onTrackClick = onTrackClick,
                                    onAlbumClick = openAlbum,
                                    onArtistClick = openArtist,
                                    onGroupClick = openGroup,
                                    onPlaylistClick = openPlaylist,
                                    onSmartPlaylistClick = openSmartPlaylist,
                                    onOpenPlayer = { playerSheet.expand() }
                                )
                            }
                        }
                    }
                    composable(
                        route = "library/album?key={key}",
                        arguments = listOf(navArgument("key") { type = NavType.StringType; defaultValue = "" })
                    ) { entry ->
                        CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                            AlbumDetailScreen(
                                albumKey = entry.arguments?.getString("key").orEmpty(),
                                bottomBarConnection = if (useRail) null else libraryNestedScrollConnection,
                                onBack = { navController.popBackStack() },
                                onTrackClick = onTrackClick,
                                onArtistClick = openArtist
                            )
                        }
                    }
                    composable(
                        route = "library/artist?key={key}&albumArtist={albumArtist}",
                        arguments = listOf(
                            navArgument("key") { type = NavType.StringType; defaultValue = "" },
                            navArgument("albumArtist") { type = NavType.BoolType; defaultValue = false }
                        )
                    ) { entry ->
                        CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                            ArtistDetailScreen(
                                artistKey = entry.arguments?.getString("key").orEmpty(),
                                albumArtist = entry.arguments?.getBoolean("albumArtist") ?: false,
                                bottomBarConnection = if (useRail) null else libraryNestedScrollConnection,
                                onBack = { navController.popBackStack() },
                                onTrackClick = onTrackClick,
                                onAlbumClick = openAlbum
                            )
                        }
                    }
                    composable(
                        route = "library/playlist?id={id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType; defaultValue = "" })
                    ) { entry ->
                        CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                            PlaylistDetailScreen(
                                playlistId = entry.arguments?.getString("id").orEmpty(),
                                bottomBarConnection = if (useRail) null else libraryNestedScrollConnection,
                                onBack = { navController.popBackStack() },
                                onTrackClick = onTrackClick
                            )
                        }
                    }
                    composable(
                        route = "library/smart?id={id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType; defaultValue = "" })
                    ) { entry ->
                        CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                            SmartPlaylistDetailScreen(
                                smartId = entry.arguments?.getString("id").orEmpty(),
                                bottomBarConnection = if (useRail) null else libraryNestedScrollConnection,
                                onBack = { navController.popBackStack() },
                                onTrackClick = onTrackClick,
                                onOpenSmartPlaylist = { id -> navController.navigate(smartPlaylistRoute(id)) }
                            )
                        }
                    }
                    composable(
                        route = "library/group?category={category}&key={key}",
                        arguments = listOf(
                            navArgument("category") { type = NavType.StringType; defaultValue = LibraryCategory.GENRES.key },
                            navArgument("key") { type = NavType.StringType; defaultValue = "" }
                        )
                    ) { entry ->
                        CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                            GroupDetailScreen(
                                category = LibraryCategory.fromKey(entry.arguments?.getString("category")) ?: LibraryCategory.GENRES,
                                groupKey = entry.arguments?.getString("key").orEmpty(),
                                bottomBarConnection = if (useRail) null else libraryNestedScrollConnection,
                                onBack = { navController.popBackStack() },
                                onTrackClick = onTrackClick
                            )
                        }
                    }
                    composable("search") {
                        val animatedScope = this
                        paneHost(false) {
                            CompositionLocalProvider(LocalNavAnimatedScope provides animatedScope) {
                                SearchScreen(
                                    bottomBarConnection = if (useRail) null else libraryNestedScrollConnection,
                                    onTrackClick = onTrackClick,
                                    onAlbumClick = openAlbum,
                                    onArtistClick = openArtist,
                                    onPlaylistClick = openPlaylist,
                                    onGenreClick = { key -> openGroup(LibraryCategory.GENRES, key) }
                                )
                            }
                        }
                    }
                    composable("stats") {
                        paneHost(false) {
                            StatsScreen(
                                bottomBarConnection = if (useRail) null else libraryNestedScrollConnection,
                                onOpenWrapped = { wrappedStats = it },
                                onPlayTracks = onTrackClick,
                                onAlbumClick = openAlbum,
                                onArtistClick = openArtist,
                                onGenreClick = { key -> openGroup(LibraryCategory.GENRES, key) },
                                onOpenAchievements = { openLibraryRoute("stats/achievements") }
                            )
                        }
                    }
                    composable("stats/achievements") {
                        AchievementsScreen(
                            bottomBarConnection = if (useRail) null else libraryNestedScrollConnection,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings") {
                        paneHost(true) {
                            SettingsScreen(
                                onLookAndFeelClick = { openSettingsRoute("settings/look_and_feel") },
                                onAudioClick = { openSettingsRoute("settings/audio") },
                                onTagsClick = { openSettingsRoute("settings/tags") },
                                onOnlineClick = { openSettingsRoute("settings/online") },
                                onCrashLogsClick = { openSettingsRoute("settings/crash_logs") },
                                onBackupClick = { openSettingsRoute("settings/backup") },
                                onSupportClick = { openSettingsRoute("settings/support") },
                                onUpdatesClick = { openSettingsRoute("settings/updates") },
                                onAboutClick = { openSettingsRoute("settings/about") },
                                onOpenRoute = { openSettingsRoute(it) },
                                onControlsClick = { openSettingsRoute("settings/controls") }
                            )
                        }
                    }
                    composable("settings/audio") {
                        AudioSettingsScreen(
                            onBack = { navController.popBackStack() },
                            onEqualizerClick = { navController.navigate("settings/equalizer") },
                            onPageClick = { navController.navigate(it) }
                        )
                    }
                    composable(AudioPages.SOUND) {
                        AudioSoundScreen(
                            onBack = { navController.popBackStack() },
                            onEqualizerClick = { navController.navigate("settings/equalizer") }
                        )
                    }
                    composable(AudioPages.VOLUME) {
                        AudioVolumeScreen(onBack = { navController.popBackStack() })
                    }
                    composable(AudioPages.PLAYBACK) {
                        AudioPlaybackScreen(onBack = { navController.popBackStack() })
                    }
                    composable(AudioPages.HEADPHONES) {
                        AudioHeadphonesScreen(onBack = { navController.popBackStack() })
                    }
                    composable(AudioPages.ADVANCED) {
                        AudioAdvancedScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/equalizer") {
                        EqualizerScreen(
                            onBack = { navController.popBackStack() },
                            onParametricClick = { navController.navigate("settings/equalizer/parametric") }
                        )
                    }
                    composable("settings/equalizer/parametric") {
                        ParametricEqScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/controls") {
                        ControlsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/tags") {
                        TagSettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/online") {
                        OnlineSettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/backup") {
                        BackupScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/crash_logs") {
                        CrashLogScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/about") {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/updates") {
                        UpdateSettingsScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/support") {
                        SupportScreen(onBack = { navController.popBackStack() })
                    }
                    composable("settings/look_and_feel") {
                        LookAndFeelHubScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onThemeColorsClick = { navController.navigate("settings/look_and_feel/theme") },
                            onPlayerClick = { navController.navigate("settings/look_and_feel/player") },
                            onButtonsClick = { navController.navigate("settings/look_and_feel/buttons") },
                            onLibraryClick = { navController.navigate("settings/look_and_feel/library") },
                            onNavigationClick = { navController.navigate("settings/look_and_feel/navigation") },
                            onDisplayClick = { navController.navigate("settings/look_and_feel/display") },
                            onMotionClick = { navController.navigate("settings/look_and_feel/motion") },
                            onBack = { navController.popBackStack() },
                            onStylesClick = { navController.navigate("settings/look_and_feel/styles") },
                            onVisualizerClick = { navController.navigate("settings/look_and_feel/visualizer") }
                        )
                    }
                    composable("settings/look_and_feel/visualizer") {
                        VisualizerScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings/look_and_feel/styles") {
                        StylesScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings/look_and_feel/theme") {
                        ThemeColorsScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() },
                            onCustomThemesClick = { navController.navigate("settings/look_and_feel/custom_themes") }
                        )
                    }
                    composable("settings/look_and_feel/custom_themes") {
                        CustomThemesScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings/look_and_feel/player") {
                        PlayerLookScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings/look_and_feel/buttons") {
                        ButtonsLookScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings/look_and_feel/library") {
                        LibraryLookScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings/look_and_feel/navigation") {
                        NavigationLookScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings/look_and_feel/motion") {
                        MotionLookScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("settings/look_and_feel/display") {
                        DisplayLookScreen(
                            settings = lookAndFeel,
                            onSettingsChange = onLookAndFeelChange,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
        }
    }

    val latestLook by rememberUpdatedState(lookAndFeel)
    val latestLookChange by rememberUpdatedState(onLookAndFeelChange)
    val lookUpdate = remember { { transform: (LookAndFeelSettings) -> LookAndFeelSettings -> latestLookChange(transform(latestLook)) } }
    CompositionLocalProvider(
        LocalBottomContentPadding provides bottomContentPadding,
        LocalKateHaptics provides haptics,
        LocalLookAndFeel provides lookAndFeel,
        LocalLookAndFeelUpdate provides lookUpdate
    ) {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
            if (appImage.isNotEmpty()) CustomImageBackdrop(appImage, lookAndFeel.imageBlur, 0)
            if (useRail) {
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        containerColor = kateNavBarColor(),
                        modifier = Modifier.onSizeChanged { railWidthPx = it.width }
                    ) {
                        visibleDestinations.forEach { destination ->
                            val selected = isRouteSelected(currentRouteName, destination.route)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { onDestinationClick(destination.route) },
                                colors = clintNavigationRailItemColors(),
                                icon = {
                                    KateNavIcon(
                                        selected = selected,
                                        icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = stringResource(destination.labelRes)
                                    )
                                },
                                label = if (labelMode == NavigationPreferences.LABELS_NEVER) {
                                    null
                                } else {
                                    { Text(stringResource(destination.labelRes)) }
                                },
                                alwaysShowLabel = labelMode == NavigationPreferences.LABELS_ALWAYS
                            )
                        }
                    }
                    content(Modifier.weight(1f).fillMaxHeight())
                }
            } else {
                content(Modifier.fillMaxSize())
                NavigationBar(
                    containerColor = kateNavBarColor(),
                    windowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { navBarHeightPx = it.height }
                        .graphicsLayer {
                            translationY = navBarHeightPx * maxOf(bottomBarHide.value, playerSheet.progress)
                        }
                ) {
                    visibleDestinations.forEach { destination ->
                        val selected = isRouteSelected(currentRouteName, destination.route)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onDestinationClick(destination.route) },
                            colors = clintNavigationBarItemColors(),
                            icon = {
                                KateNavIcon(
                                    selected = selected,
                                    icon = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = stringResource(destination.labelRes)
                                )
                            },
                            label = if (labelMode == NavigationPreferences.LABELS_NEVER) {
                                null
                            } else {
                                { Text(stringResource(destination.labelRes)) }
                            },
                            alwaysShowLabel = labelMode == NavigationPreferences.LABELS_ALWAYS
                        )
                    }
                }
            }
            currentTrack?.let { track ->
                val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
                val playerLook = if (landscape && lookAndFeel.playerLayoutLandscape != PlayerStylePreferences.LAYOUT_SAME) {
                    lookAndFeel.copy(playerLayout = lookAndFeel.playerLayoutLandscape)
                } else {
                    lookAndFeel
                }
                CompositionLocalProvider(LocalLookAndFeel provides playerLook) {
                ArtworkColorScope(enabled = lookAndFeel.playerFollowsArtwork) {
                    PlayerSheet(
                        state = playerSheet,
                        track = track,
                        windowWidthSizeClass = windowWidthSizeClass,
                        collapsedStartPx = { if (useRail) railWidthPx.toFloat() else 0f },
                        onOpenAlbum = { key ->
                            playerSheet.collapse()
                            openAlbum(key)
                        },
                        onOpenArtist = { key ->
                            playerSheet.collapse()
                            openArtist(key, false)
                        },
                        collapsedBottomPx = {
                            if (useRail) {
                                systemBottomInsetPx.toFloat()
                            } else {
                                maxOf(navBarHeightPx * (1f - bottomBarHide.value), systemBottomInsetPx.toFloat())
                            }
                        }
                    )
                }
                }
            }
            StyleAutoSwitch(lookAndFeel, onLookAndFeelChange)
            LibraryShuffleModeHost()
            KateRadioHost()
            LyricsMakerHost()
            LrcLibSearchHost()
            TagEditorHost()
            UpdateFlowHost()
            DocumentViewerHost()
            AchievementToastHost(onOpen = {
                playerSheet.collapse()
                navController.navigate("stats/achievements")
            })
            WrappedOverlay(
                stats = wrappedStats,
                onClose = { wrappedStats = null },
                onPlayTracks = playFromStats
            )
        }
    }
}
