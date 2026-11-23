package com.jhaiian.kate.ui.player

import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import com.jhaiian.kate.ui.components.CustomImageBackdrop
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.EqualizerSettings
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.LibraryTrackCache
import com.jhaiian.kate.data.Lyrics
import com.jhaiian.kate.data.LyricsRepository
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.KateRadio
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.playback.ShuffleMode
import com.jhaiian.kate.playback.ShufflePreferences
import com.jhaiian.kate.playback.SkipDirection
import com.jhaiian.kate.ui.components.KateControlButton
import com.jhaiian.kate.ui.components.KatePlayButton
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.controlsSpacing
import com.jhaiian.kate.ui.components.RadioRequest
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.Replay30
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.ImageVector
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import com.jhaiian.kate.ui.theme.VisualizerPreferences
import com.jhaiian.kate.ui.theme.UiVisibility

private fun skipSign(): Int = if (PlayerController.lastSkipDirection == SkipDirection.NEXT) 1 else -1

private fun snapTransition(): ContentTransform = EnterTransition.None togetherWith ExitTransition.None

internal fun skipTrackAnimation(): Boolean = !UiVisibility.visible || PlayerController.trackChangeIsStale()

internal fun artTrackTransition(): ContentTransform {
    if (skipTrackAnimation()) return snapTransition()
    val crossfadeMs = PlayerController.crossfadeTransitionMs()
    if (crossfadeMs > 0) {
        return (fadeIn(tween(durationMillis = crossfadeMs, easing = LinearEasing)) +
            scaleIn(tween(durationMillis = crossfadeMs, easing = FastOutSlowInEasing), initialScale = 0.94f)) togetherWith
            (fadeOut(tween(durationMillis = crossfadeMs, easing = LinearEasing)) +
                scaleOut(tween(durationMillis = crossfadeMs, easing = FastOutSlowInEasing), targetScale = 1.04f))
    }
    val sign = skipSign()
    val offsetSpec = spring(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    )
    return slideInHorizontally(offsetSpec) { sign * it } togetherWith slideOutHorizontally(offsetSpec) { -sign * it }
}

class PlayerQuickActions(
    val onLyrics: () -> Unit,
    val onSleepTimer: () -> Unit,
    val onEqualizer: () -> Unit,
    val onAudio: () -> Unit,
    val onAddToPlaylist: () -> Unit,
    val onQueue: () -> Unit = {},
    val onMenu: () -> Unit = {},
    val onShuffleLongPress: () -> Unit = {}
)

internal fun infoTrackTransition(): ContentTransform {
    if (skipTrackAnimation()) return snapTransition()
    val crossfadeMs = PlayerController.crossfadeTransitionMs()
    if (crossfadeMs > 0) {
        val half = crossfadeMs / 2
        return fadeIn(tween(durationMillis = half, delayMillis = half, easing = LinearEasing)) togetherWith
            fadeOut(tween(durationMillis = half, easing = LinearEasing))
    }
    val sign = skipSign()
    val offsetSpec = spring(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    )
    return (slideInHorizontally(offsetSpec) { sign * it / 4 } + fadeIn(tween(durationMillis = 220, delayMillis = 60))) togetherWith
        (slideOutHorizontally(offsetSpec) { -sign * it / 4 } + fadeOut(tween(durationMillis = 140)))
}

private fun Modifier.revealOnExpand(progress: () -> Float, start: Float): Modifier = graphicsLayer {
    val fraction = ((progress() - start) / (1f - start)).coerceIn(0f, 1f)
    alpha = fraction
    translationY = (1f - fraction) * 28.dp.toPx()
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun FullPlayerContent(
    track: Track,
    isPlaying: Boolean,
    windowWidthSizeClass: WindowWidthSizeClass,
    active: Boolean,
    progress: () -> Float,
    onCollapse: () -> Unit,
    onMenuClick: () -> Unit,
    onLyricsClick: () -> Unit,
    onQueueClick: () -> Unit,
    onShuffleLongPress: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onEqualizerClick: () -> Unit,
    onAudioClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    artSlotModifier: Modifier
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val look = LocalLookAndFeel.current
    val isFavorite = FavoritesStore.isFavorite(track.id)
    val loadedLyrics by produceState<Pair<String, Lyrics?>?>(null, track.id, LyricsRepository.version) {
        value = track.id to LyricsRepository.load(context, track)
    }
    val lyricsLoaded = loadedLyrics?.first == track.id
    val lyrics = if (lyricsLoaded) loadedLyrics?.second else null
    val syncedLyrics = if (lyricsLoaded) lyrics?.synced == true else loadedLyrics?.second?.synced == true
    val showLyricLine = look.playerLyricLine && syncedLyrics
    val toggleFavorite = { FavoritesStore.toggle(context, track.id) }
    val background = look.playerBackground
    val backdropFactor = PlayerStylePreferences.backdropFactor(look.playerBackdropStrength)
    val actionHandler = remember(onLyricsClick, onSleepTimerClick, onEqualizerClick, onAudioClick, onAddToPlaylistClick, onQueueClick, onMenuClick, onShuffleLongPress) {
        PlayerQuickActions(
            onLyrics = onLyricsClick,
            onSleepTimer = onSleepTimerClick,
            onEqualizer = onEqualizerClick,
            onAudio = onAudioClick,
            onAddToPlaylist = onAddToPlaylistClick,
            onQueue = onQueueClick,
            onMenu = onMenuClick,
            onShuffleLongPress = onShuffleLongPress
        )
    }
    val quickActions = actionHandler.takeIf { look.playerQuickActions }
    val focusMode = if (look.visualizerStyle == VisualizerPreferences.STYLE_OFF) VisualizerFocus.OFF else VisualizerFocus.mode
    LaunchedEffect(focusMode, isPlaying, active, VisualizerFocus.touches) {
        VisualizerFocus.idle = false
        if (focusMode != VisualizerFocus.OFF && isPlaying && active) {
            delay(VisualizerFocus.IDLE_DELAY_MS)
            VisualizerFocus.idle = true
        }
    }
    DisposableEffect(Unit) { onDispose { VisualizerFocus.idle = false } }
    val focusAlpha by animateFloatAsState(
        targetValue = VisualizerFocus.targetUiAlpha,
        animationSpec = tween(durationMillis = 600),
        label = "visualizer_focus"
    )
    CompositionLocalProvider(LocalPlayerActions provides actionHandler) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .visualizerFocusTouch()
    ) {
        if (active) {
            when (background) {
                PlayerStylePreferences.BACKGROUND_DRIFT -> PlayerAmbientBackdrop(backdropFactor)
                PlayerStylePreferences.BACKGROUND_ARTWORK -> PlayerArtworkBackdrop(track, backdropFactor)
                PlayerStylePreferences.BACKGROUND_GRADIENT -> PlayerGradientBackdrop(backdropFactor)
                PlayerStylePreferences.BACKGROUND_IMAGE -> CustomImageBackdrop(look.playerBackgroundImage, look.imageBlur, look.imageDim)
            }
            if (focusMode != VisualizerFocus.OFF || VisualizerPreferences.PLACE_BACKGROUND in look.visualizerPlacements) {
                AudioVisualizer(
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.32f + (1f - focusAlpha) * 0.6f
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = focusAlpha }
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .revealOnExpand(progress, 0.55f)
            ) {
                if (look.playerShowHandle) {
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(colors.secondaryText.copy(alpha = 0.35f))
                    )
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }
                val radio = KateRadio.session
                val queueSize = PlayerController.queue.size
                val queueMode = look.playerHeader == PlayerStylePreferences.HEADER_QUEUE && radio == null && queueSize > 0
                PlayerTopBar(
                    header = look.playerHeader,
                    label = stringResource(if (queueMode) R.string.player_header_queue_label else R.string.player_playing_from),
                    albumName = when {
                        radio != null -> stringResource(R.string.radio_playing_label, radio.label)
                        queueMode -> stringResource(
                            R.string.player_queue_position,
                            (PlayerController.currentQueueIndex() + 1).coerceIn(1, queueSize),
                            queueSize
                        )
                        else -> track.album.ifBlank { stringResource(R.string.player_unknown_album) }
                    },
                    radioActive = radio != null,
                    showCollapse = look.playerShowCollapse,
                    showQueue = look.playerShowQueueButton,
                    showMenu = look.playerShowMenuButton,
                    onCollapse = onCollapse,
                    onQueueClick = onQueueClick,
                    onMenuClick = onMenuClick
                )
                if (look.playerShowAudioChip) {
                    AudioStatusChip(onClick = onAudioClick, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if (windowWidthSizeClass == WindowWidthSizeClass.Compact) {
                CompactPlayerBody(
                    track = track,
                    isPlaying = isPlaying,
                    isFavorite = isFavorite,
                    active = active,
                    progress = progress,
                    artSlotModifier = artSlotModifier,
                    onFavoriteToggle = toggleFavorite,
                    onLyricsClick = onLyricsClick,
                    onShuffleLongPress = onShuffleLongPress,
                    lyrics = lyrics,
                    showLyricLine = showLyricLine,
                    quickActions = quickActions,
                    modifier = Modifier.weight(1f)
                )
            } else {
                ExpandedPlayerBody(
                    track = track,
                    isPlaying = isPlaying,
                    isFavorite = isFavorite,
                    active = active,
                    progress = progress,
                    artSlotModifier = artSlotModifier,
                    onFavoriteToggle = toggleFavorite,
                    onLyricsClick = onLyricsClick,
                    onShuffleLongPress = onShuffleLongPress,
                    lyrics = lyrics,
                    showLyricLine = showLyricLine,
                    quickActions = quickActions,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (VisualizerFocus.idle) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { VisualizerFocus.touch() } }
            )
        }
    }
    }
}

@Composable
private fun PlayerAmbientBackdrop(strength: Float) {
    val colors = LocalClintColors.current
    val infinite = rememberInfiniteTransition(label = "player_backdrop")
    val drift = infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "player_backdrop_drift"
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-150).dp)
                .graphicsLayer { translationX = drift.value * 60.dp.toPx() }
                .background(
                    Brush.radialGradient(listOf(colors.primary.copy(alpha = (0.30f * strength).coerceAtMost(0.6f)), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 170.dp)
                .graphicsLayer { translationX = -drift.value * 50.dp.toPx() }
                .background(
                    Brush.radialGradient(listOf(colors.buttonBackground.copy(alpha = (0.24f * strength).coerceAtMost(0.5f)), Color.Transparent)),
                    CircleShape
                )
        )
    }
}

@Composable
private fun PlayerGradientBackdrop(strength: Float) {
    val colors = LocalClintColors.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to colors.primary.copy(alpha = (0.34f * strength).coerceAtMost(0.7f)),
                    0.45f to colors.buttonBackground.copy(alpha = (0.14f * strength).coerceAtMost(0.35f)),
                    1f to Color.Transparent
                )
            )
    )
}

@Composable
private fun PlayerArtworkBackdrop(track: Track, strength: Float) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize()) {
        val fadeMs = remember(track.artKey) {
            if (skipTrackAnimation()) 0 else PlayerController.crossfadeTransitionMs().takeIf { it > 0 } ?: 600
        }
        Crossfade(
            targetState = track.artKey,
            animationSpec = tween(durationMillis = fadeMs),
            label = "player_artwork_backdrop"
        ) { artKey ->
            if (artKey != null) {
                val request = remember(artKey) {
                    ImageRequest.Builder(context)
                        .data(LibraryTrackCache.artFile(context, artKey))
                        .size(BackdropArtSizePx)
                        .memoryCacheKey("$artKey:backdrop")
                        .build()
                }
                Image(
                    painter = rememberAsyncImagePainter(
                        model = request,
                        contentScale = ContentScale.Crop,
                        filterQuality = FilterQuality.High
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alpha = (0.6f * strength).coerceIn(0.2f, 0.9f),
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(BackdropBlur)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.background.copy(alpha = (0.3f / strength).coerceIn(0.1f, 0.6f)),
                            colors.background.copy(alpha = (0.7f / strength).coerceIn(0.4f, 0.9f)),
                            colors.background
                        )
                    )
                )
        )
    }
}

@Composable
private fun PlayerTopBar(
    header: String,
    label: String,
    albumName: String,
    radioActive: Boolean,
    showCollapse: Boolean,
    showQueue: Boolean,
    showMenu: Boolean,
    onCollapse: () -> Unit,
    onQueueClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val colors = LocalClintColors.current
    val showTitle = header != PlayerStylePreferences.HEADER_HIDDEN || radioActive
    if (!showTitle && !showCollapse && !showQueue && !showMenu) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 12.dp, top = 12.dp)
    ) {
        if (showCollapse) {
            KateIconButton(onClick = onCollapse) {
                KateIcon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.player_collapse),
                    tint = buttonTint(colors.onSurface),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        if (!showTitle) {
            Spacer(modifier = Modifier.weight(1f))
        } else {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondaryText,
                    letterSpacing = 1.2.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .let { if (radioActive) it.clickable { RadioRequest.manage() } else it }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    if (radioActive) {
                        KateIcon(
                            imageVector = Icons.Filled.Radio,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.padding(end = 6.dp).size(14.dp)
                        )
                    }
                    Text(
                        text = albumName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (radioActive) colors.primary else colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (showQueue) {
            KateIconButton(onClick = onQueueClick) {
                KateIcon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = stringResource(R.string.queue_title),
                    tint = buttonTint(colors.onSurface)
                )
            }
        }
        if (showMenu) {
            KateIconButton(onClick = onMenuClick) {
                KateIcon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.library_menu),
                    tint = buttonTint(colors.onSurface)
                )
            }
        }
    }
}

@Composable
private fun CompactPlayerBody(
    track: Track,
    isPlaying: Boolean,
    isFavorite: Boolean,
    active: Boolean,
    progress: () -> Float,
    artSlotModifier: Modifier,
    onFavoriteToggle: () -> Unit,
    onLyricsClick: () -> Unit,
    onShuffleLongPress: () -> Unit,
    lyrics: Lyrics?,
    showLyricLine: Boolean,
    quickActions: PlayerQuickActions?,
    modifier: Modifier = Modifier
) {
    val style = LocalLookAndFeel.current
    val layout = style.playerLayout
    if (layout == PlayerStylePreferences.LAYOUT_MINIMAL) {
        MinimalPlayerBody(
            track = track,
            isPlaying = isPlaying,
            isFavorite = isFavorite,
            active = active,
            progress = progress,
            artSlotModifier = artSlotModifier,
            onFavoriteToggle = onFavoriteToggle,
            onLyricsClick = onLyricsClick,
            onShuffleLongPress = onShuffleLongPress,
            lyrics = lyrics,
            showLyricLine = showLyricLine,
            quickActions = quickActions,
            thumbSize = 88.dp,
            modifier = modifier
        )
        return
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val room = if (quickActions != null) 0.88f else 1f
        val gap by animateDpAsState(
            targetValue = if (showLyricLine) 18.dp else 36.dp,
            animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
            label = "player_info_gap"
        )
        val bigArtHeight by animateDpAsState(
            targetValue = minOf(maxWidth, maxHeight * (if (showLyricLine) 0.46f else 0.54f) * room),
            animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
            label = "player_big_art_height"
        )
        val vinylSize by animateDpAsState(
            targetValue = minOf(maxWidth - 24.dp, maxHeight * (if (showLyricLine) 0.46f else 0.52f) * room).coerceIn(180.dp, 520.dp),
            animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
            label = "player_vinyl_size"
        )
        val cassetteWidth = minOf(maxWidth - 32.dp, 560.dp, maxHeight * 0.42f * room * CassetteAspect).coerceAtLeast(220.dp)
        val cardSize by animateDpAsState(
            targetValue = minOf(maxWidth - 32.dp, maxHeight * (if (showLyricLine) 0.52f else 0.6f) * room).coerceIn(200.dp, 560.dp),
            animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow),
            label = "player_card_size"
        )
        val card = layout == PlayerStylePreferences.LAYOUT_CARD
        val bigArt = layout == PlayerStylePreferences.LAYOUT_BIG_ART
        val fullWidth = maxWidth
        Column(modifier = Modifier.fillMaxSize()) {
            if (!bigArt) Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                LayoutArt(
                    layout = layout,
                    track = track,
                    isPlaying = isPlaying,
                    active = active,
                    classicSize = cardSize,
                    bigArtWidth = fullWidth,
                    bigArtHeight = bigArtHeight,
                    vinylSize = vinylSize,
                    cassetteWidth = cassetteWidth,
                    cardSize = cardSize,
                    artSlotModifier = artSlotModifier
                )
            }
            if (bigArt) Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(gap))
            AnimatedVisibility(
                visible = showLyricLine,
                enter = fadeIn(tween(durationMillis = 260)) + expandVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(tween(durationMillis = 160)) + shrinkVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow))
            ) {
                CurrentLyricLine(
                    lyrics = lyrics,
                    active = active,
                    onClick = onLyricsClick,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .revealOnExpand(progress, 0.45f)
                )
            }
            if (!card) {
                AnimatedContent(
                    targetState = track,
                    contentKey = { it.id },
                    transitionSpec = { infoTrackTransition() },
                    modifier = Modifier.revealOnExpand(progress, 0.45f),
                    label = "player_info"
                ) { animatedTrack ->
                    TrackInfoSection(
                        track = animatedTrack,
                        isFavorite = isFavorite,
                        marquee = active,
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
            }
            Spacer(modifier = Modifier.height(if (card) 12.dp else 28.dp))
            PlayerSeekBar(track = track, active = active, modifier = Modifier.revealOnExpand(progress, 0.5f))
            Spacer(modifier = Modifier.height(20.dp))
            PlaybackControlsRow(
                isPlaying = isPlaying,
                onShuffleLongPress = onShuffleLongPress,
                modifier = Modifier.revealOnExpand(progress, 0.55f)
            )
            if (quickActions != null) {
                Spacer(modifier = Modifier.height(14.dp))
                PlayerQuickActionsRow(actions = quickActions, modifier = Modifier.revealOnExpand(progress, 0.6f))
            }
            if (!bigArt) Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExpandedPlayerBody(
    track: Track,
    isPlaying: Boolean,
    isFavorite: Boolean,
    active: Boolean,
    progress: () -> Float,
    artSlotModifier: Modifier,
    onFavoriteToggle: () -> Unit,
    onLyricsClick: () -> Unit,
    onShuffleLongPress: () -> Unit,
    lyrics: Lyrics?,
    showLyricLine: Boolean,
    quickActions: PlayerQuickActions?,
    modifier: Modifier = Modifier
) {
    val style = LocalLookAndFeel.current
    val layout = style.playerLayout
    if (layout == PlayerStylePreferences.LAYOUT_MINIMAL) {
        MinimalPlayerBody(
            track = track,
            isPlaying = isPlaying,
            isFavorite = isFavorite,
            active = active,
            progress = progress,
            artSlotModifier = artSlotModifier,
            onFavoriteToggle = onFavoriteToggle,
            onLyricsClick = onLyricsClick,
            onShuffleLongPress = onShuffleLongPress,
            lyrics = lyrics,
            showLyricLine = showLyricLine,
            quickActions = quickActions,
            thumbSize = 112.dp,
            modifier = modifier
        )
        return
    }
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp)) {
        BoxWithConstraints(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            val square = minOf(maxWidth, maxHeight)
            val cardSize = minOf(maxWidth * 0.96f, maxHeight * 0.96f).coerceIn(240.dp, 600.dp)
            LayoutArt(
                layout = layout,
                track = track,
                isPlaying = isPlaying,
                active = active,
                classicSize = cardSize,
                bigArtWidth = square,
                bigArtHeight = square,
                vinylSize = square * 0.96f,
                cassetteWidth = minOf(maxWidth * 0.98f, maxHeight * 0.9f * CassetteAspect),
                cardSize = cardSize,
                artSlotModifier = artSlotModifier
            )
        }
        Spacer(modifier = Modifier.width(40.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = showLyricLine,
                enter = fadeIn(tween(durationMillis = 260)) + expandVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(tween(durationMillis = 160)) + shrinkVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow))
            ) {
                CurrentLyricLine(
                    lyrics = lyrics,
                    active = active,
                    onClick = onLyricsClick,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .revealOnExpand(progress, 0.45f)
                )
            }
            if (layout != PlayerStylePreferences.LAYOUT_CARD) {
                AnimatedContent(
                    targetState = track,
                    contentKey = { it.id },
                    transitionSpec = { infoTrackTransition() },
                    modifier = Modifier.revealOnExpand(progress, 0.45f),
                    label = "player_info"
                ) { animatedTrack ->
                    TrackInfoSection(
                        track = animatedTrack,
                        isFavorite = isFavorite,
                        marquee = active,
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            PlayerSeekBar(track = track, active = active, modifier = Modifier.revealOnExpand(progress, 0.5f))
            Spacer(modifier = Modifier.height(24.dp))
            PlaybackControlsRow(
                isPlaying = isPlaying,
                onShuffleLongPress = onShuffleLongPress,
                modifier = Modifier.revealOnExpand(progress, 0.55f)
            )
            if (quickActions != null) {
                Spacer(modifier = Modifier.height(18.dp))
                PlayerQuickActionsRow(actions = quickActions, modifier = Modifier.revealOnExpand(progress, 0.6f))
            }
        }
    }
}

@Composable
private fun LayoutArt(
    layout: String,
    track: Track,
    isPlaying: Boolean,
    active: Boolean,
    classicSize: Dp,
    bigArtWidth: Dp,
    bigArtHeight: Dp,
    vinylSize: Dp,
    cassetteWidth: Dp,
    cardSize: Dp,
    artSlotModifier: Modifier
) {
    val style = LocalLookAndFeel.current
    when (layout) {
        PlayerStylePreferences.LAYOUT_CARD -> Spacer(
            modifier = Modifier
                .size(cardSize)
                .then(artSlotModifier)
        )
        PlayerStylePreferences.LAYOUT_BIG_ART -> Spacer(
            modifier = Modifier
                .size(bigArtWidth, bigArtHeight)
                .then(artSlotModifier)
        )
        PlayerStylePreferences.LAYOUT_VINYL -> VinylPanel(
            size = vinylSize,
            isPlaying = isPlaying,
            active = active,
            tonearm = style.playerVinylTonearm,
            artSlotModifier = artSlotModifier
        )
        PlayerStylePreferences.LAYOUT_CASSETTE -> CassettePanel(
            width = cassetteWidth,
            track = track,
            isPlaying = isPlaying,
            active = active,
            tapeColor = style.playerCassetteColor,
            artSlotModifier = artSlotModifier
        )
        else -> ArtSlot(size = classicSize, glowing = active && isPlaying && style.playerGlow, artSlotModifier = artSlotModifier)
    }
}

@Composable
private fun MinimalPlayerBody(
    track: Track,
    isPlaying: Boolean,
    isFavorite: Boolean,
    active: Boolean,
    progress: () -> Float,
    artSlotModifier: Modifier,
    onFavoriteToggle: () -> Unit,
    onLyricsClick: () -> Unit,
    onShuffleLongPress: () -> Unit,
    lyrics: Lyrics?,
    showLyricLine: Boolean,
    quickActions: PlayerQuickActions?,
    thumbSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(thumbSize).then(artSlotModifier))
            Spacer(modifier = Modifier.height(28.dp))
            AnimatedContent(
                targetState = track,
                contentKey = { it.id },
                transitionSpec = { infoTrackTransition() },
                modifier = Modifier.revealOnExpand(progress, 0.45f),
                label = "player_minimal_info"
            ) { animatedTrack ->
                MinimalTrackInfo(
                    track = animatedTrack,
                    isFavorite = isFavorite,
                    onFavoriteToggle = onFavoriteToggle
                )
            }
            AnimatedVisibility(
                visible = showLyricLine,
                enter = fadeIn(tween(durationMillis = 260)) + expandVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(tween(durationMillis = 160)) + shrinkVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow))
            ) {
                CurrentLyricLine(
                    lyrics = lyrics,
                    active = active,
                    onClick = onLyricsClick,
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .revealOnExpand(progress, 0.45f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            PlayerSeekBar(track = track, active = active, modifier = Modifier.revealOnExpand(progress, 0.5f))
            Spacer(modifier = Modifier.height(20.dp))
            PlaybackControlsRow(
                isPlaying = isPlaying,
                onShuffleLongPress = onShuffleLongPress,
                modifier = Modifier.revealOnExpand(progress, 0.55f)
            )
            if (quickActions != null) {
                Spacer(modifier = Modifier.height(16.dp))
                PlayerQuickActionsRow(actions = quickActions, modifier = Modifier.revealOnExpand(progress, 0.6f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MinimalTrackInfo(
    track: Track,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val typography = MaterialTheme.typography
    val titleStyle = when (LocalLookAndFeel.current.playerTitleSize) {
        PlayerStylePreferences.TITLE_SMALL -> typography.headlineSmall
        PlayerStylePreferences.TITLE_LARGE -> typography.displaySmall
        else -> typography.headlineLarge
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
    ) {
        Text(
            text = track.title,
            style = titleStyle,
            fontWeight = PlayerStylePreferences.titleWeight(LocalLookAndFeel.current.playerTitleWeight, FontWeight.Black),
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist.ifBlank { stringResource(R.string.player_unknown_artist) },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = colors.primary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (track.album.isNotBlank()) {
            Text(
                text = track.album,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        val infoLine = trackInfoLine(track, LocalLookAndFeel.current.playerInfoFields)
        if (infoLine.isNotEmpty()) {
            Text(
                text = infoLine,
                style = MaterialTheme.typography.labelMedium,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (LocalLookAndFeel.current.playerShowFavorite) {
            KateIconButton(
                onClick = {
                    haptics.toggle(!isFavorite)
                    onFavoriteToggle()
                },
                modifier = Modifier.padding(top = 6.dp)
            ) {
                KateIcon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.player_remove_from_favorites else R.string.player_add_to_favorites
                    ),
                    tint = if (isFavorite) colors.primary else buttonTint(colors.secondaryText),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ArtSlot(size: Dp, glowing: Boolean, artSlotModifier: Modifier) {
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = glowing,
            enter = fadeIn(tween(durationMillis = 500)),
            exit = fadeOut(tween(durationMillis = 400))
        ) {
            ArtGlow(modifier = Modifier.size(size * 0.92f))
        }
        Spacer(modifier = Modifier.size(size).then(artSlotModifier))
    }
}

@Composable
private fun ArtGlow(modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val transition = rememberInfiniteTransition(label = "art_glow")
    val glowAlpha = transition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "art_glow_alpha"
    )
    Box(
        modifier = modifier.drawBehind {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.primary.copy(alpha = glowAlpha.value), Color.Transparent),
                    center = center,
                    radius = size.minDimension / 2f
                ),
                radius = size.minDimension / 2f
            )
        }
    )
}

@Composable
private fun TrackInfoSection(
    track: Track,
    isFavorite: Boolean,
    marquee: Boolean,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val look = LocalLookAndFeel.current
    val centered = look.playerInfoAlign == PlayerStylePreferences.ALIGN_CENTER
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start
    val typography = MaterialTheme.typography
    val titleStyle: TextStyle = when (look.playerTitleSize) {
        PlayerStylePreferences.TITLE_SMALL -> typography.titleLarge
        PlayerStylePreferences.TITLE_LARGE -> typography.headlineMedium
        else -> typography.headlineSmall
    }
    val artistStyle: TextStyle = when (look.playerTitleSize) {
        PlayerStylePreferences.TITLE_SMALL -> typography.bodyMedium
        PlayerStylePreferences.TITLE_LARGE -> typography.titleMedium
        else -> typography.bodyLarge
    }
    val favScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "favorite_scale"
    )
    val showFavorite = look.playerShowFavorite
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp)
    ) {
        if (centered && showFavorite) {
            Spacer(modifier = Modifier.width(12.dp + SideControlSlot))
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(
                text = track.title,
                style = titleStyle,
                fontWeight = PlayerStylePreferences.titleWeight(look.playerTitleWeight, FontWeight.Bold),
                color = colors.onSurface,
                textAlign = textAlign,
                maxLines = if (look.playerMarquee) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = if (marquee && look.playerMarquee) Modifier.basicMarquee() else Modifier
            )
            Text(
                text = track.artist.ifBlank { stringResource(R.string.player_unknown_artist) },
                style = artistStyle,
                color = colors.secondaryText,
                textAlign = textAlign,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (look.playerShowAlbum && track.album.isNotBlank()) {
                Text(
                    text = track.album,
                    style = typography.bodyMedium,
                    color = colors.secondaryText.copy(alpha = 0.8f),
                    textAlign = textAlign,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            val infoLine = trackInfoLine(track, look.playerInfoFields)
            if (infoLine.isNotEmpty()) {
                Text(
                    text = infoLine,
                    style = typography.labelMedium,
                    color = colors.secondaryText.copy(alpha = 0.8f),
                    textAlign = textAlign,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (showFavorite) {
            Spacer(modifier = Modifier.width(12.dp))
            KateIconButton(onClick = {
                haptics.toggle(!isFavorite)
                onFavoriteToggle()
            }) {
                KateIcon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.player_remove_from_favorites else R.string.player_add_to_favorites
                    ),
                    tint = if (isFavorite) colors.primary else buttonTint(colors.onSurface),
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            scaleX = favScale
                            scaleY = favScale
                        }
                )
            }
        }
    }
}

@Composable
private fun PlayerQuickActionsRow(actions: PlayerQuickActions, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val context = LocalContext.current
    val style = LocalLookAndFeel.current
    val trackId = PlayerController.currentTrack?.id
    val stepMs = style.seekStepSeconds * 1000L
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            style.playerQuickList.forEach { action ->
                val active = actionActive(action, trackId)
                QuickActionButton(
                    icon = PlayerActions.icon(action, trackId),
                    label = stringResource(PlayerActions.label(action)),
                    tint = if (active) colors.primary else buttonTint(colors.secondaryText),
                    onClick = {
                        if (action == PlayerActions.FAVORITE) haptics.toggle(!active) else haptics.click()
                        if (trackId != null) PlayerActions.run(action, actions, context, trackId, stepMs)
                    },
                    longClickLabel = when (action) {
                        PlayerActions.EQUALIZER -> stringResource(R.string.eq_quick_toggle)
                        PlayerActions.VISUALIZER -> stringResource(R.string.visualizer_edit)
                        else -> null
                    },
                    onLongClick = if (action == PlayerActions.EQUALIZER) {
                        {
                            val eqOn = EqualizerSettings.enabled
                            haptics.toggle(!eqOn)
                            EqualizerSettings.setEnabled(context, !eqOn)
                        }
                    } else if (action == PlayerActions.SHUFFLE) {
                        {
                            haptics.longPress()
                            actions.onShuffleLongPress()
                        }
                    } else if (action == PlayerActions.VISUALIZER) {
                        {
                            haptics.longPress()
                            VisualizerFocus.sheetOpen = true
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    longClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null
) {
    KateIconButton(onClick = onClick, onClickLabel = label, onLongClick = onLongClick, onLongClickLabel = longClickLabel) {
        KateIcon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

private const val BackdropArtSizePx = 48
private val SideControlSlot = 48.dp
private val BackdropBlur = 48.dp

@Composable
private fun PlaybackControlsRow(
    isPlaying: Boolean,
    onShuffleLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalKateHaptics.current
    val style = LocalLookAndFeel.current
    val spacing = controlsSpacing(style.buttonSpacing)
    Column(modifier = modifier.fillMaxWidth()) {
    if (style.visualizerStyle != VisualizerPreferences.STYLE_OFF && VisualizerPreferences.PLACE_STRIP in style.visualizerPlacements) {
        AudioVisualizer(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .padding(horizontal = 28.dp, vertical = 4.dp)
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacing.arrangement,
        modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.horizontalPadding)
    ) {
        SlotControl(style.playerSlotLeft, side = true, onShuffleLongPress = onShuffleLongPress)
        SlotControl(style.playerSlotInnerLeft, side = false, onShuffleLongPress = onShuffleLongPress)
        KatePlayButton(isPlaying = isPlaying, onClick = {
            haptics.click()
            PlayerController.togglePlayPause()
        })
        SlotControl(style.playerSlotInnerRight, side = false, onShuffleLongPress = onShuffleLongPress)
        SlotControl(style.playerSlotRight, side = true, onShuffleLongPress = onShuffleLongPress)
    }
    }
}

private fun seekIcon(forward: Boolean, seconds: Int): ImageVector = when (seconds) {
    5 -> if (forward) Icons.Filled.Forward5 else Icons.Filled.Replay5
    10 -> if (forward) Icons.Filled.Forward10 else Icons.Filled.Replay10
    30 -> if (forward) Icons.Filled.Forward30 else Icons.Filled.Replay30
    else -> if (forward) Icons.Filled.FastForward else Icons.Filled.FastRewind
}

internal fun actionActive(action: String, trackId: String?): Boolean = when (action) {
    PlayerActions.SHUFFLE -> PlayerController.shuffleEnabled
    PlayerActions.REPEAT -> PlayerController.repeatMode != 0
    PlayerActions.FAVORITE -> trackId != null && FavoritesStore.isFavorite(trackId)
    PlayerActions.SLEEP_TIMER -> PlayerController.sleepTimerEndsAt != null || PlayerController.sleepAtEndOfTrack
    PlayerActions.EQUALIZER -> EqualizerSettings.enabled
    PlayerActions.AUDIO -> abs(AudioSettings.playbackSpeed - 1f) > 0.001f
    PlayerActions.VISUALIZER -> VisualizerFocus.mode != VisualizerFocus.OFF
    else -> false
}

@Composable
private fun SlotControl(action: String, side: Boolean, onShuffleLongPress: () -> Unit) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val context = LocalContext.current
    val style = LocalLookAndFeel.current
    val handler = LocalPlayerActions.current
    val trackId = PlayerController.currentTrack?.id
    val stepMs = style.seekStepSeconds * 1000L
    val label = stringResource(PlayerActions.label(action))
    when (action) {
        PlayerActions.NONE -> Spacer(modifier = Modifier.size(SideControlSlot))
        PlayerActions.SHUFFLE -> {
            val isShuffle = PlayerController.shuffleEnabled
            val smartMode = ShufflePreferences.mode != ShuffleMode.RANDOM
            KateControlButton(
                icon = Icons.Filled.Shuffle,
                contentDescription = label,
                tint = if (isShuffle) colors.primary else colors.secondaryText,
                iconSize = 22.dp,
                slotSize = SideControlSlot,
                highlighted = isShuffle,
                onClickLabel = label,
                onLongClickLabel = stringResource(R.string.shuffle_mode_choose),
                onLongClick = {
                    haptics.longPress()
                    onShuffleLongPress()
                },
                onClick = {
                    haptics.toggle(!isShuffle)
                    PlayerController.toggleShuffle()
                }
            ) {
                if (smartMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 7.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isShuffle) colors.primary else colors.secondaryText)
                    )
                }
            }
        }
        PlayerActions.PREVIOUS, PlayerActions.NEXT -> {
            val next = action == PlayerActions.NEXT
            KateControlButton(
                icon = if (next) Icons.Filled.SkipNext else Icons.Filled.SkipPrevious,
                contentDescription = label,
                iconSize = if (side) 26.dp else 34.dp,
                slotSize = SideControlSlot,
                onClick = {
                    haptics.click()
                    if (next) PlayerController.skipNext() else PlayerController.skipPrevious()
                }
            )
        }
        PlayerActions.REWIND, PlayerActions.FORWARD -> {
            val forward = action == PlayerActions.FORWARD
            KateControlButton(
                icon = seekIcon(forward, style.seekStepSeconds),
                contentDescription = label,
                tint = if (side) colors.secondaryText else colors.onSurface,
                iconSize = if (side) 24.dp else 30.dp,
                slotSize = SideControlSlot,
                onClickLabel = label,
                onLongClickLabel = stringResource(if (forward) R.string.player_next else R.string.player_previous),
                onLongClick = {
                    haptics.longPress()
                    if (forward) PlayerController.skipNext() else PlayerController.skipPrevious()
                },
                onClick = {
                    haptics.click()
                    PlayerActions.seekBy(if (forward) stepMs else -stepMs)
                }
            )
        }
        else -> {
            val active = actionActive(action, trackId)
            KateControlButton(
                icon = PlayerActions.icon(action, trackId),
                contentDescription = label,
                tint = if (active) colors.primary else colors.secondaryText,
                iconSize = 22.dp,
                slotSize = SideControlSlot,
                highlighted = active,
                onClickLabel = label,
                onLongClickLabel = if (action == PlayerActions.VISUALIZER) stringResource(R.string.visualizer_edit) else null,
                onLongClick = if (action == PlayerActions.VISUALIZER) {
                    {
                        haptics.longPress()
                        VisualizerFocus.sheetOpen = true
                    }
                } else {
                    null
                },
                onClick = {
                    if (action == PlayerActions.FAVORITE) haptics.toggle(!active) else haptics.click()
                    if (handler != null && trackId != null) PlayerActions.run(action, handler, context, trackId, stepMs)
                }
            )
        }
    }
}

private const val PlayPauseGlyphScale = 0.62f
private const val PlayPauseCornerFraction = 0.06f

private val PlayGlyph = arrayOf(
    floatArrayOf(0.12f, 0f, 0.55f, 0.25f, 0.55f, 0.75f, 0.12f, 1f),
    floatArrayOf(0.55f, 0.25f, 0.98f, 0.5f, 0.98f, 0.5f, 0.55f, 0.75f)
)

private val PauseGlyph = arrayOf(
    floatArrayOf(0.1f, 0f, 0.4f, 0f, 0.4f, 1f, 0.1f, 1f),
    floatArrayOf(0.6f, 0f, 0.9f, 0f, 0.9f, 1f, 0.6f, 1f)
)

@Composable
internal fun PlayPauseIcon(isPlaying: Boolean, tint: Color, iconSize: Dp) {
    val morph by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
        label = "play_pause_morph"
    )
    val description = stringResource(if (isPlaying) R.string.player_pause else R.string.player_play)
    val path = remember { Path() }
    Canvas(
        modifier = Modifier
            .size(iconSize)
            .semantics { contentDescription = description }
    ) {
        val glyph = size.minDimension * PlayPauseGlyphScale
        val left = (size.width - glyph) / 2f
        val top = (size.height - glyph) / 2f
        val t = morph.coerceIn(0f, 1f)
        path.reset()
        for (half in PlayGlyph.indices) {
            val from = PlayGlyph[half]
            val to = PauseGlyph[half]
            for (point in 0 until 4) {
                val x = left + (from[point * 2] + (to[point * 2] - from[point * 2]) * t) * glyph
                val y = top + (from[point * 2 + 1] + (to[point * 2 + 1] - from[point * 2 + 1]) * t) * glyph
                if (point == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
        }
        drawPath(path = path, color = tint)
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = glyph * PlayPauseCornerFraction * 2f, join = StrokeJoin.Round)
        )
    }
}
