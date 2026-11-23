package com.jhaiian.kate.ui.player

import com.jhaiian.kate.ui.components.KateIcon
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.AlbumArt
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.library.AddToPlaylistDialog
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

val MiniPlayerHeight = 64.dp
val MiniPlayerVerticalMargin = 8.dp
private val MiniPlayerSideMargin = 12.dp
internal val MiniPlayerRowHeight = 62.dp
internal val MiniArtSize = 44.dp
internal val MiniArtStart = 10.dp
private val MiniCorner = 22.dp
private val MiniArtCorner = 12.dp
private val FullArtCorner = 28.dp
private val SquareArtCorner = 4.dp
private val CassetteArtCorner = 3.dp
private val MinimalArtCorner = 18.dp
private val CoverPreviewGap = 24.dp
private val MiniElevation = 10.dp
private val FullArtElevation = 20.dp
private val ArtSwipeThreshold = 72.dp
private val ArtSwipeVelocity = 900.dp
private const val ArtSwipeFade = 0.5f
private const val BurstMillis = 650
private val MiniDismissThreshold = 40.dp
private const val MiniDismissMillis = 180

private fun mix(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction

fun miniPlayerReservedSpace(docked: Boolean): Dp =
    MiniPlayerHeight + if (docked) 0.dp else MiniPlayerVerticalMargin * 2

@Stable
class PlayerSheetState internal constructor(private val scope: CoroutineScope) {
    var progress by mutableFloatStateOf(0f)
        private set
    var targetProgress by mutableFloatStateOf(0f)
        private set
    var isAnimating by mutableStateOf(false)
        private set
    private var animationJob: Job? = null

    fun expand() = animateTo(1f)

    fun collapse() = animateTo(0f)

    fun snapTo(value: Float) {
        stop()
        progress = value.coerceIn(0f, 1f)
    }

    internal fun stop() {
        animationJob?.cancel()
        animationJob = null
        isAnimating = false
    }

    internal fun dragBy(delta: Float) {
        progress = (progress + delta).coerceIn(0f, 1f)
    }

    internal fun settle(velocity: Float) {
        val target = when {
            velocity > 0.6f -> 1f
            velocity < -0.6f -> 0f
            progress > 0.5f -> 1f
            else -> 0f
        }
        animateTo(target, velocity)
    }

    fun animateTo(target: Float, velocity: Float = 0f) {
        animationJob?.cancel()
        targetProgress = target
        isAnimating = true
        val job = scope.launch {
            animate(
                initialValue = progress,
                targetValue = target,
                initialVelocity = velocity,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 420f)
            ) { value, _ ->
                progress = value.coerceIn(0f, 1f)
            }
        }
        animationJob = job
        job.invokeOnCompletion {
            if (animationJob === job) {
                animationJob = null
                isAnimating = false
            }
        }
    }
}

@Composable
fun rememberPlayerSheetState(): PlayerSheetState {
    val scope = rememberCoroutineScope()
    return remember(scope) { PlayerSheetState(scope) }
}

private class SheetGeometry(
    private val widthPx: Float,
    private val heightPx: Float,
    private val sideMarginPx: Float,
    private val bottomMarginPx: Float,
    private val miniHeightPx: Float,
    private val startPx: () -> Float,
    private val bottomPx: () -> Float,
    private val appear: () -> Float
) {
    fun collapsedLeft(): Float = startPx() + sideMarginPx

    fun collapsedWidth(): Float = (widthPx - startPx() - sideMarginPx * 2f).coerceAtLeast(0f)

    fun collapsedTop(): Float {
        val resting = heightPx - bottomPx() - bottomMarginPx - miniHeightPx
        return resting + (1f - appear()) * (heightPx - resting)
    }

    fun frameLeft(progress: Float): Float = mix(collapsedLeft(), 0f, progress)

    fun frameTop(progress: Float): Float = mix(collapsedTop(), 0f, progress)

    fun frameWidth(progress: Float): Float = mix(collapsedWidth(), widthPx, progress)

    fun frameHeight(progress: Float): Float = mix(miniHeightPx, heightPx, progress)

    fun travel(): Float = collapsedTop().coerceAtLeast(1f)
}

private class CoordinatesHolder {
    var coordinates: LayoutCoordinates? = null
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PlayerSheet(
    state: PlayerSheetState,
    track: Track,
    windowWidthSizeClass: WindowWidthSizeClass,
    collapsedStartPx: () -> Float,
    collapsedBottomPx: () -> Float,
    onOpenAlbum: (String) -> Unit,
    onOpenArtist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val density = LocalDensity.current
    val isPlaying = PlayerController.isPlaying
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessLow))
    }
    val playScale = animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 260f),
        label = "player_art_scale"
    )
    val collapsed by remember { derivedStateOf { state.progress == 0f } }
    val expandedForBack by remember { derivedStateOf { state.targetProgress == 1f } }
    val showingFull by remember { derivedStateOf { state.progress > 0f } }
    val showingMini by remember { derivedStateOf { state.progress < 0.4f } }

    PredictiveBackHandler(enabled = expandedForBack) { events ->
        try {
            events.collect { event -> state.snapTo(1f - event.progress * 0.18f) }
            state.collapse()
        } catch (e: CancellationException) {
            state.expand()
            throw e
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthInt = constraints.maxWidth
        val heightInt = constraints.maxHeight
        val miniHeightPx = with(density) { MiniPlayerHeight.toPx() }
        val miniHeightInt = miniHeightPx.roundToInt()
        val docked = LocalLookAndFeel.current.miniPlayerDocked
        val miniCornerPx = with(density) { (if (docked) 0.dp else MiniCorner).toPx() }
        val miniElevationPx = with(density) { MiniElevation.toPx() }
        val miniArtPx = with(density) { MiniArtSize.toPx() }
        val miniArtStartPx = with(density) { MiniArtStart.toPx() }
        val miniArtTopPx = with(density) { ((MiniPlayerRowHeight - MiniArtSize) / 2).toPx() }
        val miniArtCornerPx = with(density) { MiniArtCorner.toPx() }
        val fullArtCornerPx = with(density) { FullArtCorner.toPx() }
        val squareArtCornerPx = with(density) { SquareArtCorner.toPx() }
        val lookAndFeel = LocalLookAndFeel.current
        val artShape = lookAndFeel.playerArtShape
        val fullArtElevationPx = with(density) { FullArtElevation.toPx() }
        val playerLayout = lookAndFeel.playerLayout
        val cardLayout = playerLayout == PlayerStylePreferences.LAYOUT_CARD
        val artShadowEnabled = lookAndFeel.playerArtShadow
        val vinylLayout = playerLayout == PlayerStylePreferences.LAYOUT_VINYL
        val cassetteCornerPx = with(density) { CassetteArtCorner.toPx() }
        val minimalCornerPx = with(density) { MinimalArtCorner.toPx() }
        val vinylSpin = rememberSpinAngle(
            running = vinylLayout && isPlaying && showingFull,
            degreesPerSecond = PlayerStylePreferences.vinylDegreesPerSecond(lookAndFeel.playerVinylSpeed)
        )
        val geometry = SheetGeometry(
            widthPx = widthInt.toFloat(),
            heightPx = heightInt.toFloat(),
            sideMarginPx = with(density) { (if (docked) 0.dp else MiniPlayerSideMargin).toPx() },
            bottomMarginPx = with(density) { (if (docked) 0.dp else MiniPlayerVerticalMargin).toPx() },
            miniHeightPx = miniHeightPx,
            startPx = collapsedStartPx,
            bottomPx = collapsedBottomPx,
            appear = { appear.value }
        )

        var fullArtBounds by remember { mutableStateOf(Rect.Zero) }
        var showMenu by remember { mutableStateOf(false) }
        var showSongInfo by remember { mutableStateOf(false) }
        var showSleepTimer by remember { mutableStateOf(false) }
        var showShuffleModes by remember { mutableStateOf(false) }
        var showLyrics by remember { mutableStateOf(false) }
        var showQueue by remember { mutableStateOf(false) }
        var showEqualizer by remember { mutableStateOf(false) }
        var showAudio by remember { mutableStateOf(false) }
        var addToPlaylistIds by remember { mutableStateOf<List<String>?>(null) }
        LaunchedEffect(collapsed) {
            if (collapsed) {
                showLyrics = false
                showQueue = false
                showEqualizer = false
                showAudio = false
                VisualizerFocus.sheetOpen = false
            }
        }
        val fullRoot = remember { CoordinatesHolder() }
        val frameShadow = colors.primary.copy(alpha = 0.28f)
        val artShadow = colors.primary.copy(alpha = 0.5f)
        var dismissOffset by remember { mutableFloatStateOf(0f) }
        var dismissArmed by remember { mutableStateOf(false) }
        val dragState = rememberDraggableState { delta ->
            if (dismissArmed && (dismissOffset > 0f || (state.progress == 0f && delta > 0f))) {
                dismissOffset = (dismissOffset + delta).coerceAtLeast(0f)
            } else {
                state.dragBy(-delta / geometry.travel())
            }
        }
        val haptics = LocalKateHaptics.current
        val context = LocalContext.current
        val gestureScope = rememberCoroutineScope()
        val artGesturesActive by remember { derivedStateOf { state.progress > 0f } }
        val cardButtonsEnabled by remember { derivedStateOf { state.progress > 0.95f } }
        var artDragging by remember { mutableStateOf(false) }
        val swipeEnabled = lookAndFeel.playerSwipeArtwork
        val doubleTapMode = lookAndFeel.playerDoubleTap
        val swipeThresholdPx = with(density) { ArtSwipeThreshold.toPx() }
        val swipeVelocityPx = with(density) { ArtSwipeVelocity.toPx() }
        var swipeOffset by remember { mutableFloatStateOf(0f) }
        var swipeReset by remember { mutableStateOf<Job?>(null) }
        val swipeState = rememberDraggableState { delta -> swipeOffset += delta }
        val settleSwipe: (Float) -> Unit = { velocity ->
            val offset = swipeOffset
            if (offset <= 0f && (offset < -swipeThresholdPx || velocity < -swipeVelocityPx)) {
                haptics.click()
                PlayerController.skipNext()
            } else if (offset >= 0f && (offset > swipeThresholdPx || velocity > swipeVelocityPx)) {
                haptics.click()
                PlayerController.skipPrevious()
            }
            swipeReset = gestureScope.launch {
                animate(
                    initialValue = offset,
                    targetValue = 0f,
                    animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                ) { value, _ -> swipeOffset = value }
            }
        }
        val dismissThresholdPx = with(density) { MiniDismissThreshold.toPx() }
        val dismissFade: () -> Float = { (dismissOffset / (miniHeightPx * 2f)).coerceIn(0f, 1f) }
        val settleDismiss: (Float) -> Unit = { velocity ->
            val offset = dismissOffset
            if (offset > dismissThresholdPx || velocity > swipeVelocityPx) {
                haptics.confirm()
                gestureScope.launch {
                    animate(
                        initialValue = offset,
                        targetValue = miniHeightPx * 2f,
                        animationSpec = tween(durationMillis = MiniDismissMillis)
                    ) { value, _ -> dismissOffset = value }
                    PlayerController.dismiss()
                }
            } else {
                gestureScope.launch {
                    animate(
                        initialValue = offset,
                        targetValue = 0f,
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                    ) { value, _ -> dismissOffset = value }
                }
            }
        }
        val burst = remember { Animatable(1f) }
        var burstIcon by remember { mutableStateOf<ImageVector?>(null) }
        val sheetActions = remember {
            PlayerQuickActions(
                onLyrics = { showLyrics = true },
                onSleepTimer = { showSleepTimer = true },
                onEqualizer = { showEqualizer = true },
                onAudio = { showAudio = true },
                onAddToPlaylist = { PlayerController.currentTrack?.id?.let { addToPlaylistIds = listOf(it) } },
                onQueue = { showQueue = true },
                onMenu = { showMenu = true },
                onShuffleLongPress = { showShuffleModes = true }
            )
        }
        val performGesture by rememberUpdatedState { action: String ->
            when (action) {
                PlayerActions.NONE -> Unit
                PlayerActions.FAVORITE -> {
                    val nowFavorite = !FavoritesStore.isFavorite(track.id)
                    FavoritesStore.toggle(context, track.id)
                    haptics.toggle(nowFavorite)
                    burstIcon = if (nowFavorite) Icons.Filled.Favorite else Icons.Filled.HeartBroken
                }
                PlayerActions.PLAY_PAUSE -> {
                    val willPlay = !PlayerController.isPlaying
                    haptics.click()
                    PlayerController.togglePlayPause()
                    burstIcon = if (willPlay) Icons.Filled.PlayArrow else Icons.Filled.Pause
                }
                else -> {
                    haptics.click()
                    if (collapsed && action in PanelActions) state.expand()
                    PlayerActions.run(action, sheetActions, context, track.id, lookAndFeel.seekStepSeconds * 1000L)
                    burstIcon = PlayerActions.icon(action, track.id)
                }
            }
            if (action != PlayerActions.NONE && !collapsed) {
                gestureScope.launch {
                    burst.snapTo(0f)
                    burst.animateTo(1f, tween(durationMillis = BurstMillis))
                }
            }
        }
        val focusArtAlpha by animateFloatAsState(
            targetValue = VisualizerFocus.targetUiAlpha,
            animationSpec = tween(durationMillis = 600),
            label = "visualizer_focus_art"
        )
        val doubleTapSides = lookAndFeel.playerDoubleTapSides
        val longPressAction = lookAndFeel.playerLongPress
        val onArtDoubleTap by rememberUpdatedState { x: Float, width: Float ->
            val action = when {
                doubleTapSides && x < width / 3f -> PlayerActions.REWIND
                doubleTapSides && x > width * 2f / 3f -> PlayerActions.FORWARD
                else -> doubleTapMode
            }
            performGesture(action)
        }

        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val p = state.progress
                    val width = geometry.frameWidth(p).roundToInt().coerceAtLeast(0)
                    val height = geometry.frameHeight(p).roundToInt().coerceAtLeast(0)
                    val placeable = measurable.measure(Constraints.fixed(width, height))
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(geometry.frameLeft(p).roundToInt(), geometry.frameTop(p).roundToInt())
                    }
                }
                .graphicsLayer {
                    val p = state.progress
                    translationY = dismissOffset
                    alpha = 1f - dismissFade()
                    shape = RoundedCornerShape(mix(miniCornerPx, 0f, p))
                    clip = true
                    shadowElevation = mix(miniElevationPx, 0f, p) * appear.value
                    ambientShadowColor = frameShadow
                    spotShadowColor = frameShadow
                }
                .drawBehind { drawRect(lerp(colors.cardBackground, colors.background, state.progress)) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Vertical,
                    startDragImmediately = state.isAnimating,
                    onDragStarted = {
                        dismissArmed = lookAndFeel.miniSwipeDismiss && state.progress == 0f
                        state.stop()
                    },
                    onDragStopped = { velocity ->
                        if (dismissArmed && dismissOffset > 0f) {
                            settleDismiss(velocity)
                        } else {
                            state.settle(-velocity / geometry.travel())
                        }
                        dismissArmed = false
                    }
                )
                .draggable(
                    state = swipeState,
                    orientation = Orientation.Horizontal,
                    enabled = collapsed && lookAndFeel.miniSwipeSkip,
                    onDragStarted = { swipeReset?.cancel() },
                    onDragStopped = { velocity -> settleSwipe(velocity) }
                )
                .combinedClickable(
                    enabled = collapsed,
                    onClick = { state.expand() },
                    onLongClick = if (lookAndFeel.miniLongPress != PlayerActions.NONE) {
                        { performGesture(lookAndFeel.miniLongPress) }
                    } else {
                        null
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(Constraints.fixed(widthInt, heightInt))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(0, if (state.progress > 0f) 0 else heightInt)
                        }
                    }
                    .onGloballyPositioned { fullRoot.coordinates = it }
            ) {
                FullPlayerContent(
                    track = track,
                    isPlaying = isPlaying,
                    windowWidthSizeClass = windowWidthSizeClass,
                    active = showingFull,
                    progress = { state.progress },
                    onCollapse = { state.collapse() },
                    onMenuClick = { showMenu = true },
                    onLyricsClick = { showLyrics = true },
                    onQueueClick = { showQueue = true },
                    onShuffleLongPress = { showShuffleModes = true },
                    onSleepTimerClick = { showSleepTimer = true },
                    onEqualizerClick = { showEqualizer = true },
                    onAudioClick = { showAudio = true },
                    onAddToPlaylistClick = { addToPlaylistIds = listOf(track.id) },
                    artSlotModifier = Modifier.onGloballyPositioned { coordinates ->
                        val root = fullRoot.coordinates
                        if (root != null && root.isAttached && coordinates.isAttached) {
                            fullArtBounds = root.localBoundingBoxOf(coordinates, clipBounds = false)
                        }
                    }
                )
            }
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(Constraints.fixed(constraints.maxWidth, miniHeightInt))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            if (state.progress < 0.4f) placeable.place(0, 0)
                        }
                    }
                    .graphicsLayer {
                        val swipeFade = (abs(swipeOffset) / widthInt.coerceAtLeast(1)).coerceIn(0f, 1f) * ArtSwipeFade
                        translationX = swipeOffset * (1f - state.progress)
                        alpha = (1f - state.progress * 3f).coerceIn(0f, 1f) * (1f - swipeFade)
                    }
            ) {
                MiniPlayerContent(track = track, isPlaying = isPlaying, active = showingMini)
            }
        }

        val fullCornerFor: (Float) -> Float = { width ->
            when (playerLayout) {
                PlayerStylePreferences.LAYOUT_VINYL -> width / 2f
                PlayerStylePreferences.LAYOUT_CASSETTE -> cassetteCornerPx
                PlayerStylePreferences.LAYOUT_BIG_ART -> 0f
                PlayerStylePreferences.LAYOUT_MINIMAL -> minimalCornerPx
                PlayerStylePreferences.LAYOUT_CARD -> if (artShape == PlayerStylePreferences.ART_SHAPE_SQUARE) squareArtCornerPx else fullArtCornerPx
                else -> when (artShape) {
                    PlayerStylePreferences.ART_SHAPE_CIRCLE -> width / 2f
                    PlayerStylePreferences.ART_SHAPE_SQUARE -> squareArtCornerPx
                    else -> fullArtCornerPx
                }
            }
        }
        val coverPreview = lookAndFeel.playerCoverPreview && swipeEnabled
        val mainSwipeFade = if (coverPreview) ArtSwipeFade * 0.4f else ArtSwipeFade
        val swipeDirection by remember { derivedStateOf { if (swipeOffset < 0f) 1 else if (swipeOffset > 0f) -1 else 0 } }
        val previewGapPx = with(density) { CoverPreviewGap.toPx() }
        val neighborTrack = if (coverPreview && swipeDirection != 0 && !collapsed) {
            PlayerController.queue.getOrNull(PlayerController.currentQueueIndex() + swipeDirection)
        } else {
            null
        }
        if (neighborTrack != null) {
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val bounds = fullArtBounds
                        val width = if (bounds.isEmpty) 1 else bounds.width.roundToInt()
                        val height = if (bounds.isEmpty) 1 else bounds.height.roundToInt()
                        val placeable = measurable.measure(Constraints.fixed(width.coerceAtLeast(1), height.coerceAtLeast(1)))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(0, 0)
                        }
                    }
                    .graphicsLayer {
                        val bounds = fullArtBounds
                        if (bounds.isEmpty) {
                            alpha = 0f
                            return@graphicsLayer
                        }
                        val offset = swipeOffset
                        val direction = if (offset < 0f) 1f else -1f
                        translationX = bounds.left + offset + direction * (bounds.width + previewGapPx)
                        translationY = bounds.top + dismissOffset
                        alpha = if (state.progress < 0.98f) {
                            0f
                        } else {
                            (abs(offset) / (bounds.width * 0.5f)).coerceIn(0f, 1f) * (1f - dismissFade())
                        }
                        shape = RoundedCornerShape(fullCornerFor(bounds.width))
                        clip = true
                    }
                    .background(colors.surfaceVariant)
            ) {
                AlbumArt(track = neighborTrack, modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.4f), colors.surfaceVariant))),
                        contentAlignment = Alignment.Center
                    ) {
                        KateIcon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = colors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxSize(0.3f)
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val bounds = fullArtBounds
                    val width = if (bounds.isEmpty) miniArtPx.roundToInt() else bounds.width.roundToInt()
                    val height = if (bounds.isEmpty) miniArtPx.roundToInt() else bounds.height.roundToInt()
                    val placeable = measurable.measure(Constraints.fixed(width.coerceAtLeast(1), height.coerceAtLeast(1)))
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(0, 0)
                    }
                }
                .graphicsLayer {
                    val p = state.progress
                    val bounds = fullArtBounds
                    val hasBounds = !bounds.isEmpty
                    val baseWidth = if (hasBounds) bounds.width.roundToInt().toFloat() else miniArtPx.roundToInt().toFloat()
                    val baseHeight = if (hasBounds) bounds.height.roundToInt().toFloat() else miniArtPx.roundToInt().toFloat()
                    val startLeft = geometry.collapsedLeft() + miniArtStartPx
                    val startTop = geometry.collapsedTop() + miniArtTopPx
                    val endLeft = if (hasBounds) bounds.left else startLeft
                    val endTop = if (hasBounds) bounds.top else startTop
                    val endWidth = if (hasBounds) bounds.width else miniArtPx
                    val endHeight = if (hasBounds) bounds.height else miniArtPx
                    val rawWidth = mix(miniArtPx, endWidth, p)
                    val rawHeight = mix(miniArtPx, endHeight, p)
                    val pulse = mix(1f, playScale.value, p)
                    val width = rawWidth * pulse
                    val height = rawHeight * pulse
                    val scale = (width / baseWidth).coerceAtLeast(0.001f)
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = scale
                    scaleY = (height / baseHeight).coerceAtLeast(0.001f)
                    translationX = mix(startLeft, endLeft, p) + (rawWidth - width) / 2f + swipeOffset
                    translationY = mix(startTop, endTop, p) + (rawHeight - height) / 2f + dismissOffset
                    alpha = (1f - (abs(swipeOffset) / baseWidth).coerceIn(0f, 1f) * mainSwipeFade) * (1f - dismissFade()) * focusArtAlpha
                    val fullCorner = fullCornerFor(width)
                    shape = RoundedCornerShape(mix(miniArtCornerPx, fullCorner, p) / scale)
                    clip = true
                    val elevation = when {
                        !artShadowEnabled -> 0f
                        playerLayout == PlayerStylePreferences.LAYOUT_CLASSIC -> fullArtElevationPx
                        playerLayout == PlayerStylePreferences.LAYOUT_CARD -> fullArtElevationPx
                        playerLayout == PlayerStylePreferences.LAYOUT_MINIMAL -> fullArtElevationPx * 0.5f
                        else -> 0f
                    }
                    shadowElevation = mix(0f, elevation, p)
                    ambientShadowColor = artShadow
                    spotShadowColor = artShadow
                }
                .background(colors.surfaceVariant)
                .visualizerFocusTouch()
                .then(
                    if (artGesturesActive || artDragging) {
                        Modifier
                            .draggable(
                                state = dragState,
                                orientation = Orientation.Vertical,
                                startDragImmediately = state.isAnimating,
                                onDragStarted = {
                                    artDragging = true
                                    state.stop()
                                },
                                onDragStopped = { velocity ->
                                    artDragging = false
                                    state.settle(-velocity / geometry.travel())
                                }
                            )
                            .let { modifier ->
                                if (swipeEnabled) {
                                    modifier.draggable(
                                        state = swipeState,
                                        orientation = Orientation.Horizontal,
                                        onDragStarted = { swipeReset?.cancel() },
                                        onDragStopped = { velocity -> settleSwipe(velocity) }
                                    )
                                } else {
                                    modifier
                                }
                            }
                            .let { modifier ->
                                val doubleTapOn = doubleTapMode != PlayerActions.NONE || doubleTapSides
                                val longPressOn = longPressAction != PlayerActions.NONE
                                if (doubleTapOn || longPressOn) {
                                    modifier.pointerInput(doubleTapOn, longPressOn) {
                                        detectTapGestures(
                                            onDoubleTap = if (doubleTapOn) {
                                                { offset -> onArtDoubleTap(offset.x, size.width.toFloat()) }
                                            } else {
                                                null
                                            },
                                            onLongPress = if (longPressOn) {
                                                { performGesture(longPressAction) }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                } else {
                                    modifier
                                }
                            }
                    } else {
                        Modifier
                    }
                )
        ) {
            AnimatedContent(
                targetState = track,
                contentKey = { it.id },
                transitionSpec = { artTrackTransition() },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (vinylLayout) {
                            val p = state.progress
                            val eased = p * p * (3f - 2f * p)
                            rotationZ = vinylSpin.value * eased
                        }
                    },
                label = "player_art"
            ) { animatedTrack ->
                Box(modifier = Modifier.fillMaxSize()) {
                    AlbumArt(track = animatedTrack, modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(colors.primary.copy(alpha = 0.4f), colors.surfaceVariant))),
                            contentAlignment = Alignment.Center
                        ) {
                            KateIcon(
                                imageVector = Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = colors.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxSize(0.3f)
                            )
                        }
                    }
                    if (cardLayout) {
                        CardTrackLabels(
                            track = animatedTrack,
                            marquee = showingFull && lookAndFeel.playerMarquee,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = CardInset, bottom = CardInset, end = CardInset * 2 + CardButtonSize)
                                .graphicsLayer { alpha = ((state.progress - 0.6f) / 0.4f).coerceIn(0f, 1f) }
                        )
                    }
                }
            }
            if (cardLayout) {
                CardArtButtons(
                    isFavorite = FavoritesStore.isFavorite(track.id),
                    showFavorite = lookAndFeel.playerShowFavorite,
                    enabled = cardButtonsEnabled,
                    onFavoriteToggle = {
                        val nowFavorite = !FavoritesStore.isFavorite(track.id)
                        haptics.toggle(nowFavorite)
                        FavoritesStore.toggle(context, track.id)
                    },
                    onMenuClick = { showMenu = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(CardInset)
                        .graphicsLayer { alpha = ((state.progress - 0.6f) / 0.4f).coerceIn(0f, 1f) }
                )
            }
            if (vinylLayout) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(0.1f)
                        .graphicsLayer { alpha = state.progress }
                        .clip(CircleShape)
                        .background(colors.background)
                )
            }
            if (playerLayout == PlayerStylePreferences.LAYOUT_BIG_ART) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = state.progress }
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.55f to Color.Transparent,
                                1f to colors.background
                            )
                        )
                )
            }
            burstIcon?.let { icon ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(0.34f)
                        .graphicsLayer {
                            val t = burst.value
                            val pop = if (t < 0.35f) 0.5f + t / 0.35f * 0.7f else 1.2f - (t - 0.35f) * 0.3f
                            scaleX = pop
                            scaleY = pop
                            alpha = if (t < 0.6f) 1f else (1f - (t - 0.6f) / 0.4f).coerceAtLeast(0f)
                        }
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.28f)),
                    contentAlignment = Alignment.Center
                ) {
                    KateIcon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize(0.6f)
                    )
                }
            }
        }
        LyricsPanel(
            visible = showLyrics,
            track = track,
            isPlaying = isPlaying,
            onClose = { showLyrics = false },
            onMenuClick = { showMenu = true }
        )
        QueuePanel(
            visible = showQueue,
            onClose = { showQueue = false }
        )
        EqualizerPanel(
            visible = showEqualizer,
            onClose = { showEqualizer = false }
        )
        PlayerOptionsMenu(
            visible = showMenu,
            track = track,
            onDismiss = { showMenu = false },
            onOpenAlbum = { key ->
                showMenu = false
                onOpenAlbum(key)
            },
            onOpenArtist = { key ->
                showMenu = false
                onOpenArtist(key)
            },
            onShowInfo = {
                showMenu = false
                showSongInfo = true
            },
            onShowLyrics = {
                showMenu = false
                showLyrics = true
            },
            onShowSleepTimer = {
                showMenu = false
                showSleepTimer = true
            },
            onShowEqualizer = {
                showMenu = false
                showEqualizer = true
            },
            onShowAudio = {
                showMenu = false
                showAudio = true
            },
            onShowShuffleModes = {
                showMenu = false
                showShuffleModes = true
            },
            onAddToPlaylist = {
                showMenu = false
                addToPlaylistIds = listOf(track.id)
            }
        )
        SongInfoDialog(visible = showSongInfo, track = track, onDismiss = { showSongInfo = false })
        AudioQuickDialog(
            visible = showAudio,
            onDismiss = { showAudio = false },
            onOpenEqualizer = {
                showAudio = false
                showEqualizer = true
            }
        )
        SleepTimerDialog(visible = showSleepTimer, onDismiss = { showSleepTimer = false })
        if (VisualizerFocus.sheetOpen) VisualizerQuickSheet(onDismiss = { VisualizerFocus.sheetOpen = false })
        ShuffleModeDialog(visible = showShuffleModes, onDismiss = { showShuffleModes = false })
        AddToPlaylistDialog(trackIds = addToPlaylistIds, onDismiss = { addToPlaylistIds = null })
    }
}

internal val CardInset = 12.dp
internal val CardButtonSize = 44.dp
private const val CardSurfaceAlpha = 0.82f

@Composable
internal fun CardTrackLabels(track: Track, marquee: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val artist = track.artist.ifBlank { stringResource(R.string.player_unknown_artist) }
    val detail = if (track.album.isBlank()) artist else stringResource(R.string.player_card_artist_album, artist, track.album)
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        CardLabel(
            text = track.title,
            color = colors.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            marquee = marquee
        )
        CardLabel(
            text = detail,
            color = colors.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            marquee = marquee
        )
    }
}

@Composable
private fun CardLabel(text: String, color: Color, style: TextStyle, fontWeight: FontWeight, marquee: Boolean) {
    val colors = LocalClintColors.current
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.cardBackground.copy(alpha = CardSurfaceAlpha))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = style,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = if (marquee) TextOverflow.Clip else TextOverflow.Ellipsis,
            modifier = if (marquee) Modifier.basicMarquee() else Modifier
        )
    }
}

@Composable
internal fun CardArtButtons(
    isFavorite: Boolean,
    showFavorite: Boolean,
    enabled: Boolean,
    onFavoriteToggle: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        if (showFavorite) {
            CardRoundButton(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) R.string.player_remove_from_favorites else R.string.player_add_to_favorites
                ),
                tint = if (isFavorite) colors.primary else buttonTint(colors.onSurface),
                enabled = enabled,
                onClick = onFavoriteToggle
            )
        }
        CardRoundButton(
            icon = Icons.Filled.MoreVert,
            contentDescription = stringResource(R.string.library_menu),
            tint = buttonTint(colors.primary),
            enabled = enabled,
            onClick = onMenuClick
        )
    }
}

@Composable
private fun CardRoundButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalClintColors.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(CardButtonSize)
            .clip(CircleShape)
            .background(colors.cardBackground.copy(alpha = CardSurfaceAlpha))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        KateIcon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

private val PanelActions = setOf(PlayerActions.LYRICS, PlayerActions.QUEUE, PlayerActions.EQUALIZER, PlayerActions.AUDIO)
