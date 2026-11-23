package com.jhaiian.kate.ui.player

import com.jhaiian.kate.ui.components.CustomImageBackdrop
import com.jhaiian.kate.data.LyricsOffsetStore
import com.jhaiian.kate.ui.theme.FineTune
import com.jhaiian.kate.ui.theme.ThemePreferences
import androidx.compose.material.icons.outlined.AvTimer
import com.jhaiian.kate.ui.components.withLyricsFont
import androidx.compose.material.icons.outlined.TravelExplore
import com.jhaiian.kate.ui.components.KateActionButton
import androidx.compose.material.icons.outlined.Edit
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.animation.core.Animatable
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.jhaiian.kate.R
import com.jhaiian.kate.data.FavoritesStore
import androidx.compose.ui.draw.shadow
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import com.jhaiian.kate.data.LibraryTrackCache
import com.jhaiian.kate.data.LyricLine
import com.jhaiian.kate.data.LyricWord
import com.jhaiian.kate.data.Lyrics
import com.jhaiian.kate.data.LyricsRepository
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.AlbumArt
import com.jhaiian.kate.ui.components.ImmersiveEffect
import com.jhaiian.kate.ui.components.KeepScreenOnEffect
import com.jhaiian.kate.ui.components.KateControlButton
import com.jhaiian.kate.ui.components.KatePlayButton
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.VisualizerPreferences
import com.jhaiian.kate.ui.theme.LyricsPreferences
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

private const val FOLLOW_RESUME_MS = 5_000L
private const val POSITION_POLL_MS = 60L
private const val ACTIVE_LINE_FRACTION = 0.4f
private const val CHROME_HIDE_MS = 4_000L
private const val LyricsBackdropSizePx = 64
private const val MaxSoftenedDistance = 4
private const val SWEEP_MS_PER_CHAR = 90L
private const val SWEEP_MIN_MS = 1_200L
private const val SWEEP_MAX_MS = 10_000L
private const val SWEEP_LEAD_MS = 200L
private const val BACKWARD_TOLERANCE_MS = 300L
private val LyricsBackdropBlur = 56.dp
private val LyricsEdgeFade = 56.dp
private val LyricsMaxWidth = 760.dp
private val WideLayoutMinWidth = 720.dp
private val SwipeDismissDistance = 96.dp
private const val BackgroundVocalScale = 0.78f

private sealed interface LyricsState {
    data object Loading : LyricsState
    data class Loaded(val lyrics: Lyrics?) : LyricsState
}

@Stable
private class LyricsFollowState {
    var following by mutableStateOf(true)
    var resumeRequests by mutableIntStateOf(0)
        private set

    fun resume() {
        following = true
        resumeRequests += 1
    }
}

@Composable
internal fun LyricsPanel(
    visible: Boolean,
    track: Track,
    isPlaying: Boolean,
    onClose: () -> Unit,
    onMenuClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(durationMillis = 260)) + slideInVertically(
            spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)
        ) { it / 10 },
        exit = fadeOut(tween(durationMillis = 200)) + slideOutVertically(tween(durationMillis = 200)) { it / 14 }
    ) {
        BackHandler(onBack = onClose)
        val settings = LocalLookAndFeel.current
        ImmersiveEffect(enabled = settings.lyricsImmersive)
        KeepScreenOnEffect(enabled = settings.lyricsKeepScreenOn && isPlaying)
        val context = LocalContext.current
        val state by produceState<LyricsState>(LyricsState.Loading, track.id, LyricsRepository.version) {
            value = LyricsState.Loading
            value = LyricsState.Loaded(LyricsRepository.load(context, track))
        }
        val synced = (state as? LyricsState.Loaded)?.lyrics?.synced == true
        val follow = remember(track.id) { LyricsFollowState() }
        var chromeVisible by remember { mutableStateOf(true) }
        var sharing by remember(track.id) { mutableStateOf(false) }
        var offsetOpen by remember(track.id) { mutableStateOf(false) }
        val loadedLyrics = (state as? LyricsState.Loaded)?.lyrics
        val canShare = loadedLyrics?.lines?.any { it.text.isNotBlank() } == true
        var interactions by remember { mutableIntStateOf(0) }
        LaunchedEffect(track.id) { chromeVisible = true }
        LaunchedEffect(chromeVisible, isPlaying, synced, interactions) {
            if (chromeVisible && isPlaying && synced) {
                delay(CHROME_HIDE_MS)
                chromeVisible = false
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.type == PointerEventType.Press) interactions += 1
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { chromeVisible = !chromeVisible }
                }
        ) {
            LyricsBackdrop(track = track, style = settings.lyricsBackground)
            if (VisualizerPreferences.PLACE_LYRICS in settings.visualizerPlacements) {
                AudioVisualizer(modifier = Modifier.fillMaxSize(), alpha = 0.28f)
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                val wide = maxWidth >= WideLayoutMinWidth && maxWidth > maxHeight
                if (wide) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        LyricsArtPane(
                            track = track,
                            isPlaying = isPlaying,
                            onSwipeDown = onClose,
                            onMenuClick = onMenuClick,
                            modifier = Modifier.weight(0.42f).fillMaxHeight()
                        )
                        LyricsStage(
                            state = state,
                            track = track,
                            isPlaying = isPlaying,
                            follow = follow,
                            chromeVisible = chromeVisible,
                            showTrackInfo = false,
                            onClose = onClose,
                            onShare = if (canShare) ({ sharing = true }) else null,
                            onOffset = if (synced) ({ offsetOpen = true }) else null,
                            onUserScroll = { chromeVisible = true },
                            modifier = Modifier.weight(0.58f).fillMaxHeight()
                        )
                    }
                } else {
                    LyricsStage(
                        state = state,
                        track = track,
                        isPlaying = isPlaying,
                        follow = follow,
                        chromeVisible = chromeVisible,
                        showTrackInfo = true,
                        onClose = onClose,
                        onShare = if (canShare) ({ sharing = true }) else null,
                        onOffset = if (synced) ({ offsetOpen = true }) else null,
                        onUserScroll = { chromeVisible = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            LyricsOffsetDialog(visible = offsetOpen, trackId = track.id, onDismiss = { offsetOpen = false })
            LyricsShareSheet(
                visible = sharing,
                track = track,
                lyrics = loadedLyrics,
                onDismiss = { sharing = false }
            )
        }
    }
}

@Composable
private fun LyricsBackdrop(track: Track, style: String) {
    when {
        style == PlayerStylePreferences.BACKGROUND_PLAIN -> LyricsPlainBackdrop()
        style == PlayerStylePreferences.BACKGROUND_IMAGE -> {
            val look = LocalLookAndFeel.current
            LyricsPlainBackdrop()
            CustomImageBackdrop(look.playerBackgroundImage, look.imageBlur, look.imageDim)
        }
        style == PlayerStylePreferences.BACKGROUND_DRIFT || track.artKey == null -> LyricsDriftBackdrop()
        else -> LyricsArtworkBackdrop(track)
    }
}

@Composable
private fun LyricsPlainBackdrop() {
    Box(modifier = Modifier.fillMaxSize().background(LocalClintColors.current.background))
}

@Composable
private fun LyricsDriftBackdrop() {
    val colors = LocalClintColors.current
    val drift = rememberInfiniteTransition(label = "lyrics_drift")
    val phase by drift.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lyrics_drift_phase"
    )
    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = phase * size.width * 0.18f
                    translationY = -size.height * 0.28f
                }
                .background(Brush.radialGradient(listOf(colors.primary.copy(alpha = 0.30f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = -phase * size.width * 0.16f
                    translationY = size.height * 0.30f
                }
                .background(Brush.radialGradient(listOf(colors.buttonBackground.copy(alpha = 0.24f), Color.Transparent)))
        )
    }
}

@Composable
private fun LyricsArtworkBackdrop(track: Track) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val drift = rememberInfiniteTransition(label = "lyrics_backdrop")
    val phase by drift.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 26_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lyrics_backdrop_phase"
    )
    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        val fadeMs = remember(track.artKey) {
            if (skipTrackAnimation()) 0 else PlayerController.crossfadeTransitionMs().takeIf { it > 0 } ?: 700
        }
        Crossfade(
            targetState = track.artKey,
            animationSpec = tween(durationMillis = fadeMs),
            label = "lyrics_backdrop_art"
        ) { artKey ->
            if (artKey != null) {
                val request = remember(artKey) {
                    ImageRequest.Builder(context)
                        .data(LibraryTrackCache.artFile(context, artKey))
                        .size(LyricsBackdropSizePx)
                        .memoryCacheKey("$artKey:lyrics_backdrop")
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
                    alpha = 0.72f,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val scale = 1.3f + phase * 0.18f
                            scaleX = scale
                            scaleY = scale
                            rotationZ = (phase - 0.5f) * 10f
                            translationX = (phase - 0.5f) * size.width * 0.08f
                        }
                        .blur(LyricsBackdropBlur, BlurredEdgeTreatment.Rectangle)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.background.copy(alpha = 0.5f),
                            colors.background.copy(alpha = 0.7f),
                            colors.background.copy(alpha = 0.9f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(listOf(colors.primary.copy(alpha = 0.16f), Color.Transparent))
                )
        )
    }
}

@Composable
private fun LyricsArtPane(
    track: Track,
    isPlaying: Boolean,
    onSwipeDown: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val cardLayout = LocalLookAndFeel.current.playerLayout == PlayerStylePreferences.LAYOUT_CARD
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    val currentOnSwipeDown by rememberUpdatedState(onSwipeDown)
    Column(
        modifier = modifier
            .pointerInput(Unit) {
                val threshold = SwipeDismissDistance.toPx()
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset.value > threshold) {
                            haptics.confirm()
                            currentOnSwipeDown()
                        } else {
                            scope.launch { dragOffset.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) }
                        }
                    },
                    onDragCancel = {
                        scope.launch { dragOffset.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)) }
                    },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        scope.launch { dragOffset.snapTo((dragOffset.value + amount).coerceAtLeast(0f)) }
                    }
                )
            }
            .graphicsLayer {
                translationY = dragOffset.value
                alpha = 1f - (dragOffset.value / size.height.coerceAtLeast(1f)).coerceIn(0f, 0.6f)
            }
            .padding(start = 32.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LyricsPaneArt(
            track = track,
            isPlaying = isPlaying,
            onMenuClick = onMenuClick,
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 440.dp)
        )
        if (!cardLayout) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = track.artist.ifBlank { stringResource(R.string.player_unknown_artist) },
                style = MaterialTheme.typography.bodyLarge,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun LyricsPaneArt(track: Track, isPlaying: Boolean, onMenuClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    when (look.playerLayout) {
        PlayerStylePreferences.LAYOUT_CARD -> {
            val context = LocalContext.current
            val haptics = LocalKateHaptics.current
            val shape = if (look.playerArtShape == PlayerStylePreferences.ART_SHAPE_SQUARE) {
                RoundedCornerShape(4.dp)
            } else {
                RoundedCornerShape(28.dp)
            }
            val shadowColor = colors.primary.copy(alpha = 0.5f)
            val isFavorite = FavoritesStore.isFavorite(track.id)
            Box(
                modifier = modifier
                    .aspectRatio(1f)
                    .shadow(
                        elevation = if (look.playerArtShadow) 20.dp else 0.dp,
                        shape = shape,
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                    .clip(shape)
            ) {
                LyricsArtImage(track = track, modifier = Modifier.fillMaxSize())
                CardTrackLabels(
                    track = track,
                    marquee = look.playerMarquee,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = CardInset, bottom = CardInset, end = CardInset * 2 + CardButtonSize)
                )
                CardArtButtons(
                    isFavorite = isFavorite,
                    showFavorite = look.playerShowFavorite,
                    enabled = true,
                    onFavoriteToggle = {
                        haptics.toggle(!isFavorite)
                        FavoritesStore.toggle(context, track.id)
                    },
                    onMenuClick = onMenuClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(CardInset)
                )
            }
        }
        PlayerStylePreferences.LAYOUT_VINYL -> BoxWithConstraints(
            modifier = modifier.aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            val size = minOf(maxWidth, maxHeight)
            val tonearm = look.playerVinylTonearm
            val geometry = remember(size, tonearm) { VinylGeometry(size, tonearm) }
            val spin = rememberSpinAngle(
                running = isPlaying,
                degreesPerSecond = PlayerStylePreferences.vinylDegreesPerSecond(look.playerVinylSpeed)
            )
            Box(modifier = Modifier.size(size)) {
                VinylPanel(
                    size = size,
                    isPlaying = isPlaying,
                    active = true,
                    tonearm = tonearm,
                    artSlotModifier = Modifier
                )
                LyricsArtImage(
                    track = track,
                    modifier = Modifier
                        .offset(x = geometry.centerX - geometry.label / 2, y = geometry.centerY - geometry.label / 2)
                        .size(geometry.label)
                        .graphicsLayer { rotationZ = spin.value }
                        .clip(CircleShape)
                )
                Box(
                    modifier = Modifier
                        .offset(x = geometry.centerX - geometry.label * 0.05f, y = geometry.centerY - geometry.label * 0.05f)
                        .size(geometry.label * 0.1f)
                        .clip(CircleShape)
                        .background(colors.background)
                )
            }
        }
        PlayerStylePreferences.LAYOUT_CASSETTE -> BoxWithConstraints(
            modifier = modifier.aspectRatio(CassetteAspect),
            contentAlignment = Alignment.Center
        ) {
            val width = minOf(maxWidth, maxHeight * CassetteAspect)
            val geometry = remember(width) { CassetteGeometry(width) }
            Box(modifier = Modifier.size(width, geometry.height)) {
                CassettePanel(
                    width = width,
                    track = track,
                    isPlaying = isPlaying,
                    active = true,
                    tapeColor = look.playerCassetteColor,
                    artSlotModifier = Modifier
                )
                LyricsArtImage(
                    track = track,
                    modifier = Modifier
                        .offset(x = geometry.artX, y = geometry.artY)
                        .size(geometry.art)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
        }
        else -> {
            val shape = when (look.playerArtShape) {
                PlayerStylePreferences.ART_SHAPE_CIRCLE -> CircleShape
                PlayerStylePreferences.ART_SHAPE_SQUARE -> RoundedCornerShape(4.dp)
                else -> RoundedCornerShape(28.dp)
            }
            LyricsArtImage(
                track = track,
                modifier = modifier
                    .aspectRatio(1f)
                    .clip(shape)
            )
        }
    }
}

@Composable
private fun LyricsArtImage(track: Track, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    AlbumArt(
        track = track,
        modifier = modifier.background(colors.surfaceVariant)
    ) {
        KateIcon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.fillMaxSize(0.4f)
        )
    }
}

@Composable
private fun LyricsStage(
    state: LyricsState,
    track: Track,
    isPlaying: Boolean,
    follow: LyricsFollowState,
    chromeVisible: Boolean,
    showTrackInfo: Boolean,
    onClose: () -> Unit,
    onShare: (() -> Unit)?,
    onOffset: (() -> Unit)? = null,
    onUserScroll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var topReservePx by remember { mutableIntStateOf(0) }
    var bottomReservePx by remember { mutableIntStateOf(0) }
    val synced = (state as? LyricsState.Loaded)?.lyrics?.synced == true
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .fadingEdges(LyricsEdgeFade)
        ) {
            when (state) {
                LyricsState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = LocalClintColors.current.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                }
                is LyricsState.Loaded -> {
                    val lyrics = state.lyrics
                    when {
                        lyrics == null -> NoLyrics(
                            onMake = { LyricsMakerRequest.open(track, null) },
                            onSearch = { LrcLibRequest.open(track) }
                        )
                        lyrics.synced -> SyncedLyrics(
                            lyrics = lyrics,
                            follow = follow,
                            onUserScroll = onUserScroll
                        )
                        else -> PlainLyrics(
                            lyrics = lyrics,
                            topPadding = with(density) { topReservePx.toDp() },
                            bottomPadding = with(density) { bottomReservePx.toDp() }
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = chromeVisible,
            enter = fadeIn(tween(durationMillis = 240)) + slideInVertically(tween(durationMillis = 280)) { -it / 2 },
            exit = fadeOut(tween(durationMillis = 240)) + slideOutVertically(tween(durationMillis = 280)) { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topReservePx = it.height }
        ) {
            LyricsTopBar(
                track = track,
                showTrackInfo = showTrackInfo,
                onClose = onClose,
                onShare = onShare,
                onEdit = { LyricsMakerRequest.open(track, (state as? LyricsState.Loaded)?.lyrics) },
                onOffset = onOffset
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(
                visible = synced && !follow.following,
                enter = fadeIn(tween(durationMillis = 200)) + scaleIn(
                    spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                    initialScale = 0.8f
                ),
                exit = fadeOut(tween(durationMillis = 160)) + scaleOut(tween(durationMillis = 160), targetScale = 0.8f)
            ) {
                JumpToCurrentPill(onClick = { follow.resume() })
            }
            AnimatedVisibility(
                visible = chromeVisible,
                enter = fadeIn(tween(durationMillis = 240)) + expandVertically(
                    tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Top
                ),
                exit = fadeOut(tween(durationMillis = 240)) + shrinkVertically(
                    tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Top
                ),
                modifier = Modifier.onSizeChanged { bottomReservePx = it.height }
            ) {
                LyricsControls(track = track, isPlaying = isPlaying)
            }
        }
    }
}

private fun Modifier.fadingEdges(edge: Dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        if (size.height <= 0f) return@drawWithContent
        val fraction = (edge.toPx() / size.height).coerceIn(0f, 0.3f)
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                fraction to Color.Black,
                1f - fraction to Color.Black,
                1f to Color.Transparent
            ),
            blendMode = BlendMode.DstIn
        )
    }

@Composable
private fun LyricsTopBar(
    track: Track,
    showTrackInfo: Boolean,
    onClose: () -> Unit,
    onShare: (() -> Unit)?,
    onEdit: () -> Unit,
    onOffset: (() -> Unit)? = null
) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
    ) {
        KateIconButton(onClick = onClose) {
            KateIcon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = stringResource(R.string.lyrics_close),
                tint = buttonTint(colors.onSurface),
                modifier = Modifier.size(28.dp)
            )
        }
        if (showTrackInfo) {
            Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist.ifBlank { stringResource(R.string.player_unknown_artist) },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        if (onOffset != null) {
            KateIconButton(onClick = onOffset) {
                KateIcon(
                    imageVector = Icons.Outlined.AvTimer,
                    contentDescription = stringResource(R.string.lyrics_offset_title),
                    tint = buttonTint(if (LyricsOffsetStore.offsetFor(track.id) != 0L) colors.primary else colors.onSurface)
                )
            }
        }
        KateIconButton(onClick = onEdit) {
            KateIcon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.sync_title),
                tint = buttonTint(colors.onSurface)
            )
        }
        if (onShare != null) {
            KateIconButton(onClick = onShare) {
                KateIcon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.lyrics_share),
                    tint = buttonTint(colors.onSurface)
                )
            }
        }
    }
}

@Composable
private fun JumpToCurrentPill(onClick: () -> Unit) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(bottom = 12.dp)
            .clip(CircleShape)
            .background(colors.primary)
            .clickable {
                haptics.click()
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        KateIcon(
            imageVector = Icons.Filled.MyLocation,
            contentDescription = null,
            tint = colors.onPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.lyrics_jump_to_current),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onPrimary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun lyricsTextStyle(base: TextStyle, size: String): TextStyle {
    val custom = FineTune.value(size)
    return when {
        custom != null -> base.copy(fontSize = 28.sp * custom, lineHeight = 37.sp * custom)
        size == LyricsPreferences.TEXT_SIZE_SMALL -> base.copy(fontSize = 22.sp, lineHeight = 30.sp)
        size == LyricsPreferences.TEXT_SIZE_LARGE -> base.copy(fontSize = 34.sp, lineHeight = 44.sp)
        else -> base.copy(fontSize = 28.sp, lineHeight = 37.sp)
    }
}

private fun plainTextStyle(base: TextStyle, size: String): TextStyle {
    val custom = FineTune.value(size)
    return when {
        custom != null -> base.copy(fontSize = 20.sp * custom, lineHeight = 30.sp * custom)
        size == LyricsPreferences.TEXT_SIZE_SMALL -> base.copy(fontSize = 17.sp, lineHeight = 26.sp)
        size == LyricsPreferences.TEXT_SIZE_LARGE -> base.copy(fontSize = 24.sp, lineHeight = 34.sp)
        else -> base.copy(fontSize = 20.sp, lineHeight = 30.sp)
    }
}

private fun TextStyle.withSpacing(percent: Int): TextStyle =
    if (percent == 100) this else copy(lineHeight = lineHeight * (percent / 100f))

@Composable
private fun lyricColor(argb: Int, fallback: Color): Color = if (argb == ThemePreferences.NO_COLOR) fallback else Color(argb)

@Composable
private fun SyncedLyrics(
    lyrics: Lyrics,
    follow: LyricsFollowState,
    onUserScroll: () -> Unit
) {
    val settings = LocalLookAndFeel.current
    val haptics = LocalKateHaptics.current
    val listState = rememberLazyListState()
    val dragged by listState.interactionSource.collectIsDraggedAsState()
    val scrolling = listState.isScrollInProgress
    val currentOnUserScroll by rememberUpdatedState(onUserScroll)
    var activeIndex by remember(lyrics) { mutableIntStateOf(lyrics.activeIndex(LyricsOffsetStore.currentPositionMs())) }
    val textStyle = lyricsTextStyle(MaterialTheme.typography.headlineSmall, settings.lyricsTextSize)
        .withLyricsFont(settings.lyricsFont)
        .withSpacing(settings.lyricsLineSpacing)
    val centered = settings.lyricsAlignment == LyricsPreferences.ALIGN_CENTER
    val aligns = remember(lyrics, centered) { lineAligns(lyrics, centered) }
    val smallStyle = textStyle.copy(
        fontSize = textStyle.fontSize * BackgroundVocalScale,
        lineHeight = textStyle.lineHeight * BackgroundVocalScale
    )
    val karaokeWords = remember(lyrics, settings.lyricsWordByWord, settings.lyricsLineSweep) {
        lyrics.lines.mapIndexed { index, line ->
            when {
                line.text.isBlank() -> emptyList()
                line.hasWordTiming -> if (settings.lyricsWordByWord) normalizeWords(line.words) else emptyList()
                settings.lyricsLineSweep -> sweepWords(line, lyrics.nextLineStartMs(index))
                else -> emptyList()
            }
        }
    }

    LaunchedEffect(lyrics) {
        while (true) {
            val index = lyrics.activeIndex(LyricsOffsetStore.currentPositionMs())
            if (index != activeIndex) activeIndex = index
            delay(POSITION_POLL_MS)
        }
    }
    LaunchedEffect(dragged) {
        if (dragged) {
            follow.following = false
            currentOnUserScroll()
        }
    }
    LaunchedEffect(follow.following, scrolling) {
        if (!follow.following && !scrolling) {
            delay(FOLLOW_RESUME_MS)
            follow.following = true
        }
    }
    LaunchedEffect(lyrics) {
        snapshotFlow { listState.layoutInfo.totalItemsCount > 0 }.first { it }
        listState.centerOn(activeIndex.coerceAtLeast(0), animate = false)
    }
    LaunchedEffect(activeIndex, follow.following, follow.resumeRequests) {
        if (follow.following) listState.centerOn(activeIndex.coerceAtLeast(0), animate = true)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val edgePadding = maxHeight * 0.42f
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = edgePadding, bottom = edgePadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(lyrics.lines, key = { position, line -> "${line.timeMs}_$position" }) { position, line ->
                SyncedLine(
                    line = line,
                    words = karaokeWords.getOrElse(position) { emptyList() },
                    distance = position - activeIndex,
                    textStyle = if (line.voice == LyricLine.VOICE_BACKGROUND) smallStyle else textStyle,
                    align = aligns.getOrElse(position) { TextAlign.Start },
                    soften = settings.lyricsSoftenLines,
                    onClick = {
                        haptics.click()
                        PlayerController.seekTo((line.timeMs - LyricsOffsetStore.offsetFor(PlayerController.currentTrack?.id)).coerceAtLeast(0L))
                        if (!PlayerController.isPlaying) PlayerController.togglePlayPause()
                        activeIndex = position
                        follow.resume()
                    }
                )
            }
        }
    }
}

@Composable
private fun SyncedLine(
    line: LyricLine,
    words: List<LyricWord>,
    distance: Int,
    textStyle: TextStyle,
    align: TextAlign,
    soften: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalClintColors.current
    val look = LocalLookAndFeel.current
    val activeColor = lyricColor(look.lyricsActiveColor, colors.primary)
    val inactiveColor = lyricColor(look.lyricsInactiveColor, colors.onSurface)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val centered = align == TextAlign.Center
    val atEnd = align == TextAlign.End
    val active = distance == 0
    val color by animateColorAsState(
        targetValue = when {
            active && words.isNotEmpty() -> inactiveColor.copy(alpha = 0.45f)
            active -> activeColor
            distance < 0 -> inactiveColor.copy(alpha = 0.4f)
            else -> inactiveColor.copy(alpha = 0.62f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "lyric_color"
    )
    val emphasis by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "lyric_emphasis"
    )
    val farness by animateFloatAsState(
        targetValue = abs(distance).coerceAtMost(MaxSoftenedDistance).toFloat(),
        animationSpec = tween(durationMillis = 360),
        label = "lyric_farness"
    )
    val origin = when {
        centered -> TransformOrigin(0.5f, 0.5f)
        rtl != atEnd -> TransformOrigin(1f, 0.5f)
        else -> TransformOrigin(0f, 0.5f)
    }
    val blurRadius = if (soften) (farness * 1.4f).coerceAtMost(5f).dp else 0.dp
    Box(
        modifier = Modifier
            .widthIn(max = LyricsMaxWidth)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClickLabel = stringResource(R.string.lyrics_seek_here)) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .graphicsLayer {
                val scale = 0.93f + 0.07f * emphasis
                scaleX = scale
                scaleY = scale
                transformOrigin = origin
                alpha = if (soften) 1f - farness * 0.06f else 1f
            }
            .blur(blurRadius, BlurredEdgeTreatment.Unbounded)
    ) {
        if (line.text.isBlank()) {
            InstrumentalDots(active = active, color = color, centered = centered, textStyle = textStyle)
        } else if (words.isNotEmpty()) {
            KaraokeText(
                text = line.text,
                words = words,
                active = active,
                textStyle = textStyle,
                baseColor = color,
                fillColor = activeColor,
                textAlign = align,
                fillAlpha = { emphasis },
                wordStyle = if (line.hasWordTiming) look.lyricsWordStyle else LyricsPreferences.WORD_STYLE_SWEEP,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = line.text,
                style = textStyle,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = align,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun lineAligns(lyrics: Lyrics, centered: Boolean): List<TextAlign> {
    val base = if (centered) TextAlign.Center else TextAlign.Start
    if (!lyrics.duet) return lyrics.lines.map { base }
    var last = TextAlign.Start
    return lyrics.lines.map { line ->
        when (line.voice) {
            LyricLine.VOICE_SECOND -> TextAlign.End.also { last = it }
            LyricLine.VOICE_MAIN -> TextAlign.Start.also { last = it }
            else -> last
        }
    }
}

private fun sweepWords(line: LyricLine, nextLineMs: Long?): List<LyricWord> {
    val natural = (line.text.length * SWEEP_MS_PER_CHAR).coerceIn(SWEEP_MIN_MS, SWEEP_MAX_MS)
    val end = nextLineMs?.let { (it - SWEEP_LEAD_MS).coerceIn(line.timeMs, line.timeMs + SWEEP_MAX_MS) }
        ?: (line.timeMs + natural)
    return listOf(LyricWord(line.timeMs, end, 0, line.text.length))
}

private fun normalizeWords(words: List<LyricWord>): List<LyricWord> {
    if (words.size < 2) return words
    val sorted = words.sortedBy { it.startMs }
    return sorted.mapIndexed { index, word ->
        val limit = sorted.getOrNull(index + 1)?.startMs?.takeIf { it > word.startMs }
        val end = when {
            limit == null -> word.endMs
            word.endMs <= word.startMs -> limit
            else -> minOf(word.endMs, limit)
        }
        word.copy(endMs = end.coerceAtLeast(word.startMs))
    }
}

private fun smoothPosition(previous: Long, next: Long): Long =
    if (next < previous && previous - next < BACKWARD_TOLERANCE_MS) previous else next

private fun filledOffset(words: List<LyricWord>, positionMs: Long): Float {
    var result = 0f
    var previousEnd = 0f
    for ((index, word) in words.withIndex()) {
        if (positionMs < word.startMs) break
        val from = if (index > 0) minOf(previousEnd, word.charStart.toFloat()) else word.charStart.toFloat()
        val to = word.charEnd.toFloat()
        result = if (positionMs >= word.endMs || word.endMs <= word.startMs) {
            to
        } else {
            val fraction = (positionMs - word.startMs).toFloat() / (word.endMs - word.startMs).toFloat()
            from + (to - from) * fraction
        }
        previousEnd = to
    }
    return result
}

@Composable
private fun KaraokeText(
    text: String,
    words: List<LyricWord>,
    active: Boolean,
    textStyle: TextStyle,
    baseColor: Color,
    fillColor: Color,
    textAlign: TextAlign,
    fillAlpha: () -> Float,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    wordStyle: String = LyricsPreferences.WORD_STYLE_SWEEP
) {
    val custom = wordStyle != LyricsPreferences.WORD_STYLE_SWEEP
    val measurer = rememberTextMeasurer()
    val segmentCache = remember { WordSegmentCache() }
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val position = remember { mutableLongStateOf(LyricsOffsetStore.currentPositionMs()) }
    val playing = PlayerController.isPlaying
    val sampled = if (active) PlayerController.positionMs else 0L
    LaunchedEffect(active, playing, sampled) {
        if (!active) return@LaunchedEffect
        position.longValue = smoothPosition(position.longValue, LyricsOffsetStore.currentPositionMs())
        if (playing) {
            while (true) {
                withFrameMillis {
                    position.longValue = smoothPosition(position.longValue, LyricsOffsetStore.currentPositionMs())
                }
            }
        }
    }
    Text(
        text = text,
        style = textStyle,
        fontWeight = FontWeight.Bold,
        color = if (custom) Color.Transparent else baseColor,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layout = it },
        modifier = modifier.drawWithContent {
            val alpha = fillAlpha()
            val result = layout
            if (custom && result != null) {
                val segments = segmentCache.get(result, words, measurer)
                if (segments.all.isEmpty()) {
                    drawText(textLayoutResult = result, color = baseColor)
                } else {
                    drawWordStyle(wordStyle, segments, words, position.longValue, baseColor, fillColor, alpha)
                }
                return@drawWithContent
            }
            drawContent()
            if (result == null) return@drawWithContent
            if (alpha <= 0.01f) return@drawWithContent
            val filled = filledOffset(words, position.longValue)
            if (filled > 0f) drawKaraokeFill(result, filled, fillColor, alpha.coerceAtMost(1f))
        }
    )
}

private fun DrawScope.drawKaraokeFill(layout: TextLayoutResult, filled: Float, color: Color, alpha: Float) {
    val textLength = layout.layoutInput.text.length
    if (textLength == 0) return
    for (lineIndex in 0 until layout.lineCount) {
        val start = layout.getLineStart(lineIndex)
        val end = layout.getLineEnd(lineIndex, visibleEnd = true)
        if (filled <= start) break
        if (end <= start) continue
        val left = layout.getLineLeft(lineIndex)
        val right = layout.getLineRight(lineIndex)
        val rtl = layout.getParagraphDirection(start) == ResolvedTextDirection.Rtl
        val edge = if (filled >= end) {
            if (rtl) left else right
        } else {
            val index = filled.toInt().coerceIn(start, minOf(end, textLength) - 1)
            val box = layout.getBoundingBox(index)
            val fraction = (filled - index).coerceIn(0f, 1f)
            if (rtl) box.right - box.width * fraction else box.left + box.width * fraction
        }
        val clipLeft = if (rtl) edge else minOf(left, edge)
        val clipRight = if (rtl) maxOf(right, edge) else edge
        if (clipRight <= clipLeft) continue
        clipRect(
            left = clipLeft,
            top = layout.getLineTop(lineIndex),
            right = clipRight,
            bottom = layout.getLineBottom(lineIndex)
        ) {
            drawText(textLayoutResult = layout, color = color, alpha = alpha)
        }
    }
}

@Composable
private fun InstrumentalDots(active: Boolean, color: Color, centered: Boolean, textStyle: TextStyle) {
    val height = with(LocalDensity.current) { textStyle.lineHeight.toDp() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (centered) {
            Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        } else {
            Arrangement.spacedBy(8.dp)
        },
        modifier = Modifier.fillMaxWidth().height(height)
    ) {
        if (active) {
            val transition = rememberInfiniteTransition(label = "instrumental_dots")
            repeat(3) { dot ->
                val pulse by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1_200, delayMillis = dot * 180, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "instrumental_dot_$dot"
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer {
                            val scale = 0.7f + 0.5f * pulse
                            scaleX = scale
                            scaleY = scale
                            alpha = 0.45f + 0.55f * pulse
                        }
                        .clip(CircleShape)
                        .background(color)
                )
            }
        } else {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

private suspend fun LazyListState.centerOn(index: Int, animate: Boolean) {
    if (layoutInfo.totalItemsCount == 0) return
    var item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    if (item == null) {
        scrollToItem(index)
        item = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index } ?: return
    }
    val viewport = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    val target = layoutInfo.viewportStartOffset + viewport * ACTIVE_LINE_FRACTION
    val delta = item.offset + item.size / 2f - target
    if (animate) animateScrollBy(delta, spring(dampingRatio = 1f, stiffness = Spring.StiffnessLow)) else scrollBy(delta)
}

@Composable
private fun PlainLyrics(lyrics: Lyrics, topPadding: Dp, bottomPadding: Dp) {
    val colors = LocalClintColors.current
    val settings = LocalLookAndFeel.current
    val textStyle = plainTextStyle(MaterialTheme.typography.titleMedium, settings.lyricsTextSize)
        .withLyricsFont(settings.lyricsFont)
        .withSpacing(settings.lyricsLineSpacing)
    val centered = settings.lyricsAlignment == LyricsPreferences.ALIGN_CENTER
    val aligns = remember(lyrics, centered) { lineAligns(lyrics, centered) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = topPadding + 16.dp, bottom = bottomPadding + 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(lyrics.lines, key = { position, _ -> position }) { position, line ->
            val background = line.voice == LyricLine.VOICE_BACKGROUND
            Text(
                text = line.text,
                style = if (background) {
                    textStyle.copy(fontSize = textStyle.fontSize * BackgroundVocalScale, lineHeight = textStyle.lineHeight * BackgroundVocalScale)
                } else {
                    textStyle
                },
                fontWeight = FontWeight.SemiBold,
                color = lyricColor(settings.lyricsInactiveColor, colors.onSurface).copy(alpha = if (background) 0.65f else 0.9f),
                textAlign = aligns.getOrElse(position) { TextAlign.Start },
                modifier = Modifier
                    .widthIn(max = LyricsMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun NoLyrics(onMake: () -> Unit, onSearch: () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KateIcon(
            imageVector = Icons.Filled.FormatQuote,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = stringResource(R.string.lyrics_none_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.lyrics_none_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        KateActionButton(
            label = stringResource(R.string.lrclib_search_button),
            icon = Icons.Outlined.TravelExplore,
            allowIconOnly = false,
            onClick = onSearch,
            modifier = Modifier.padding(top = 20.dp)
        )
        KateActionButton(
            label = stringResource(R.string.sync_make_button),
            icon = Icons.Outlined.Edit,
            primary = false,
            allowIconOnly = false,
            onClick = onMake,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun LyricsControls(track: Track, isPlaying: Boolean) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(max = LyricsMaxWidth)
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        PlayerSeekBar(track = track, active = true)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            KateControlButton(
                icon = Icons.Filled.SkipPrevious,
                contentDescription = stringResource(R.string.player_previous),
                iconSize = 30.dp,
                slotSize = 56.dp,
                onClick = {
                    haptics.click()
                    PlayerController.skipPrevious()
                }
            )
            KatePlayButton(
                isPlaying = isPlaying,
                baseSize = 64.dp,
                allowGlow = false,
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick = {
                    haptics.click()
                    PlayerController.togglePlayPause()
                }
            )
            KateControlButton(
                icon = Icons.Filled.SkipNext,
                contentDescription = stringResource(R.string.player_next),
                iconSize = 30.dp,
                slotSize = 56.dp,
                onClick = {
                    haptics.click()
                    PlayerController.skipNext()
                }
            )
        }
    }
}

private val CurrentLineHeight = 56.dp
private const val LINE_POLL_MS = 120L

@Composable
internal fun CurrentLyricLine(
    lyrics: Lyrics?,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val settings = LocalLookAndFeel.current
    var activeIndex by remember(lyrics) {
        mutableIntStateOf(lyrics?.activeIndex(LyricsOffsetStore.currentPositionMs()) ?: -1)
    }
    LaunchedEffect(lyrics, active) {
        if (lyrics == null || !active || !lyrics.synced) return@LaunchedEffect
        while (true) {
            val index = lyrics.activeIndex(LyricsOffsetStore.currentPositionMs())
            if (index != activeIndex) activeIndex = index
            delay(LINE_POLL_MS)
        }
    }
    Box(
        modifier = modifier
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .height(CurrentLineHeight)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClickLabel = stringResource(R.string.lyrics_open)) { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedContent(
            targetState = activeIndex,
            transitionSpec = {
                val direction = if (targetState >= initialState) 1 else -1
                (slideInVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)) { direction * it / 2 } +
                    fadeIn(tween(durationMillis = 220))) togetherWith
                    (slideOutVertically(spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)) { -direction * it / 2 } +
                        fadeOut(tween(durationMillis = 160)))
            },
            contentAlignment = Alignment.CenterStart,
            label = "current_lyric_line"
        ) { index ->
            val line = lyrics?.lines?.getOrNull(index)
            val words = when {
                lyrics == null || line == null || line.text.isBlank() -> emptyList()
                line.hasWordTiming -> if (settings.lyricsWordByWord) normalizeWords(line.words) else emptyList()
                settings.lyricsLineSweep -> sweepWords(line, lyrics.nextLineStartMs(index))
                else -> emptyList()
            }
            if (line != null && words.isNotEmpty()) {
                KaraokeText(
                    text = line.text,
                    words = words,
                    active = active,
                    textStyle = MaterialTheme.typography.titleMedium,
                    baseColor = colors.primary.copy(alpha = 0.45f),
                    fillColor = colors.primary,
                    textAlign = TextAlign.Start,
                    fillAlpha = { 1f },
                    wordStyle = if (line.hasWordTiming) settings.lyricsWordStyle else LyricsPreferences.WORD_STYLE_SWEEP,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = line?.text?.ifBlank { "♪" } ?: "♪",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (line != null) colors.primary else colors.primary.copy(alpha = 0.5f),
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
