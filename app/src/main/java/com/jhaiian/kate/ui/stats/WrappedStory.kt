package com.jhaiian.kate.ui.stats

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.outlined.BrandingWatermark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AchievementCenter
import com.jhaiian.kate.data.AchievementState
import com.jhaiian.kate.data.ArtistImageStore
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.data.ListeningStats
import com.jhaiian.kate.data.RankedItem
import com.jhaiian.kate.data.StatsRange
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.library.ArtThumb
import com.jhaiian.kate.ui.theme.ClintSwitch
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZoneId
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class WrappedPage {
    INTRO, MINUTES, GENRE, CLOCK, TOP_ARTIST, TOP_ARTISTS, TOP_SONG, TOP_SONGS, TOP_ALBUM, DISCOVERIES, BIG_DAY, ACHIEVEMENTS, SUMMARY
}

private const val PAGE_MS = 5_500
private const val LIST_PAGE_MS = 7_500

private val WrappedText: Color
    @Composable @ReadOnlyComposable get() = LocalClintColors.current.onSurface

private val WrappedSubtle: Color
    @Composable @ReadOnlyComposable get() = LocalClintColors.current.secondaryText

private class WrappedMetrics(val artSize: Dp, val compact: Boolean, val listCount: Int)

private fun buildPages(stats: ListeningStats, achievements: Int): List<WrappedPage> = buildList {
    add(WrappedPage.INTRO)
    add(WrappedPage.MINUTES)
    if (stats.topGenres.isNotEmpty()) add(WrappedPage.GENRE)
    if (stats.peakHour != null) add(WrappedPage.CLOCK)
    if (stats.topArtists.isNotEmpty()) add(WrappedPage.TOP_ARTIST)
    if (stats.topArtists.size >= 3) add(WrappedPage.TOP_ARTISTS)
    if (stats.topSongs.isNotEmpty()) add(WrappedPage.TOP_SONG)
    if (stats.topSongs.size >= 3) add(WrappedPage.TOP_SONGS)
    if (stats.topAlbums.isNotEmpty()) add(WrappedPage.TOP_ALBUM)
    if (stats.hasEarlierHistory && stats.newArtists.isNotEmpty()) add(WrappedPage.DISCOVERIES)
    if (stats.biggestDay != null) add(WrappedPage.BIG_DAY)
    if (achievements > 0) add(WrappedPage.ACHIEVEMENTS)
    add(WrappedPage.SUMMARY)
}

@Composable
fun WrappedOverlay(
    stats: ListeningStats?,
    onClose: () -> Unit,
    onPlayTracks: (List<Track>, Track) -> Unit
) {
    var last by remember { mutableStateOf<ListeningStats?>(null) }
    SideEffect { if (stats != null) last = stats }
    val display = stats ?: last
    AnimatedVisibility(
        visible = stats != null,
        enter = fadeIn(tween(260)) + scaleIn(initialScale = 0.94f, animationSpec = tween(320, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(220)) + scaleOut(targetScale = 0.96f, animationSpec = tween(220))
    ) {
        if (display != null) WrappedStory(stats = display, onClose = onClose, onPlayTracks = onPlayTracks)
    }
}

@Composable
private fun WrappedStory(
    stats: ListeningStats,
    onClose: () -> Unit,
    onPlayTracks: (List<Track>, Track) -> Unit
) {
    val colors = LocalClintColors.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(Unit) { AchievementCenter.refresh(context) }
    val achievementStates = AchievementCenter.states
    val earned = remember(stats, achievementStates) {
        val zone = ZoneId.systemDefault()
        AchievementCenter.unlockedBetween(stats.period.startMillis(zone), stats.period.endMillis(zone))
    }
    val pages = remember(stats, earned.size) { buildPages(stats, earned.size) }
    var page by remember(stats) { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var progressFor by remember(stats) { mutableIntStateOf(-1) }
    val progress = remember(stats) { Animatable(0f) }
    val dismiss = remember { Animatable(0f) }
    val tracks = LibraryStore.tracks
    val trackMap = remember(stats, tracks.size) { tracks.associateBy { it.id } }
    val current = pages[page.coerceIn(0, pages.lastIndex)]

    fun goTo(target: Int) {
        val clamped = target.coerceIn(0, pages.lastIndex)
        if (clamped == page) return
        page = clamped
    }

    BackHandler(onBack = onClose)

    LaunchedEffect(page, paused) {
        if (progressFor != page) {
            progress.snapTo(0f)
            progressFor = page
        }
        if (paused) return@LaunchedEffect
        if (current == WrappedPage.SUMMARY) {
            progress.animateTo(1f, tween(400))
            return@LaunchedEffect
        }
        val duration = when (current) {
            WrappedPage.TOP_ARTISTS, WrappedPage.TOP_SONGS -> LIST_PAGE_MS
            else -> PAGE_MS
        }
        val remaining = ((1f - progress.value) * duration).toInt().coerceAtLeast(1)
        progress.animateTo(1f, tween(remaining, easing = LinearEasing))
        goTo(page + 1)
    }

    val gradient = remember(colors, page) { wrappedGradient(colors, page + 1) }
    val top by animateColorAsState(gradient[0], tween(700), label = "wrapped_top")
    val middle by animateColorAsState(gradient[1], tween(700), label = "wrapped_middle")
    val bottom by animateColorAsState(gradient[2], tween(700), label = "wrapped_bottom")
    val accent = colors.primary

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationY = dismiss.value
                val fraction = (dismiss.value / (size.height.coerceAtLeast(1f))).coerceIn(0f, 1f)
                scaleX = 1f - fraction * 0.12f
                scaleY = 1f - fraction * 0.12f
                alpha = 1f - fraction * 0.6f
                shape = RoundedCornerShape(28.dp)
                clip = dismiss.value > 0f
            }
            .background(Brush.verticalGradient(listOf(top, middle, bottom)))
            .pointerInput(pages.size) {
                detectTapGestures(
                    onPress = {
                        paused = true
                        tryAwaitRelease()
                        paused = false
                    },
                    onTap = { offset ->
                        if (offset.x < size.width * 0.3f) goTo(page - 1) else goTo(page + 1)
                    }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { paused = true },
                    onDragEnd = {
                        paused = false
                        scope.launch {
                            if (dismiss.value > size.height * 0.18f) {
                                onClose()
                            } else {
                                dismiss.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                    },
                    onDragCancel = {
                        paused = false
                        scope.launch { dismiss.animateTo(0f) }
                    },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        scope.launch { dismiss.snapTo((dismiss.value + amount).coerceAtLeast(0f)) }
                    }
                )
            }
    ) {
        val metrics = remember(maxWidth, maxHeight) {
            val compact = maxHeight < 600.dp
            WrappedMetrics(
                artSize = min(min(maxWidth * 0.62f, maxHeight * if (compact) 0.34f else 0.36f), 300.dp),
                compact = compact,
                listCount = if (compact) 3 else 5
            )
        }
        FloatingBlobs(accent = accent)
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            StoryProgress(
                count = pages.size,
                current = page,
                progress = { progress.value },
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 4.dp, top = 4.dp)
            ) {
                Text(
                    text = stringResource(summaryTitleRes(stats.period.range)).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = WrappedSubtle,
                    modifier = Modifier.weight(1f)
                )
                KateIconButton(onClick = onClose) {
                    KateIcon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.wrapped_close), tint = WrappedText)
                }
            }
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    val forward = targetState >= initialState
                    val enter = slideInHorizontally(tween(420, easing = FastOutSlowInEasing)) { width -> if (forward) width / 3 else -width / 3 } +
                        fadeIn(tween(320)) + scaleIn(initialScale = 0.92f, animationSpec = tween(420))
                    val exit = slideOutHorizontally(tween(380, easing = FastOutSlowInEasing)) { width -> if (forward) -width / 3 else width / 3 } +
                        fadeOut(tween(220)) + scaleOut(targetScale = 1.04f, animationSpec = tween(380))
                    enter togetherWith exit
                },
                label = "wrapped_page",
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { index ->
                val target = pages[index.coerceIn(0, pages.lastIndex)]
                if (target == WrappedPage.SUMMARY) {
                    SummaryScreen(
                        stats = stats,
                        trackMap = trackMap,
                        onDone = onClose,
                        modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 12.dp)
                    ) {
                        Box(modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            WrappedPageContent(
                                page = target,
                                stats = stats,
                                earned = earned,
                                metrics = metrics,
                                accent = accent,
                                trackMap = trackMap,
                                onPlayTracks = onPlayTracks
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun summaryTitleRes(range: StatsRange): Int = when (range) {
    StatsRange.WEEK -> R.string.wrapped_summary_week
    StatsRange.MONTH -> R.string.wrapped_summary_month
    StatsRange.YEAR, StatsRange.ALL_TIME -> R.string.wrapped_summary_year
}

private fun introTitleRes(range: StatsRange): Int = when (range) {
    StatsRange.WEEK -> R.string.wrapped_intro_week
    StatsRange.MONTH -> R.string.wrapped_intro_month
    StatsRange.YEAR, StatsRange.ALL_TIME -> R.string.wrapped_intro_year
}

@Composable
private fun StoryProgress(count: Int, current: Int, progress: () -> Float, modifier: Modifier = Modifier) {
    val fillColor = LocalClintColors.current.onSurface
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier.fillMaxWidth()) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(fillColor.copy(alpha = 0.22f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val fill = when {
                        index < current -> 1f
                        index == current -> progress()
                        else -> 0f
                    }
                    drawRect(color = fillColor, size = size.copy(width = size.width * fill.coerceIn(0f, 1f)))
                }
            }
        }
    }
}

@Composable
private fun FloatingBlobs(accent: Color) {
    val transition = rememberInfiniteTransition(label = "wrapped_blobs")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing), RepeatMode.Restart),
        label = "wrapped_blob_phase"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val blobs = listOf(
            Triple(0.18f, 0.22f, 0.55f),
            Triple(0.82f, 0.38f, 0.45f),
            Triple(0.35f, 0.82f, 0.6f)
        )
        blobs.forEachIndexed { index, (x, y, scale) ->
            val angle = phase + index * 2.1f
            val center = Offset(
                size.width * (x + 0.08f * cos(angle)),
                size.height * (y + 0.05f * sin(angle * 1.3f))
            )
            val radius = size.minDimension * scale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.32f), accent.copy(alpha = 0f)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

@Composable
private fun Reveal(order: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(140L + order * 150L)
        anim.animateTo(1f, spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessLow))
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.graphicsLayer {
            alpha = anim.value.coerceIn(0f, 1f)
            translationY = (1f - anim.value) * 56.dp.toPx()
        }
    ) {
        content()
    }
}

@Composable
private fun CountUp(target: Long, style: TextStyle, color: Color = WrappedText, delayMillis: Long = 300L) {
    val anim = remember(target) { Animatable(0f) }
    LaunchedEffect(anim) {
        delay(delayMillis)
        anim.animateTo(1f, tween(1600, easing = FastOutSlowInEasing))
    }
    Text(
        text = formatNumber((target * anim.value).toLong()),
        style = style,
        fontWeight = FontWeight.Black,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

@Composable
private fun PageTitle(text: String, order: Int = 0) {
    Reveal(order) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = WrappedSubtle,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BigText(text: String, order: Int, compact: Boolean, color: Color = WrappedText) {
    Reveal(order) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Detail(text: String, order: Int) {
    Reveal(order) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = WrappedSubtle,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WrappedPageContent(
    page: WrappedPage,
    stats: ListeningStats,
    earned: List<AchievementState>,
    metrics: WrappedMetrics,
    accent: Color,
    trackMap: Map<String, Track>,
    onPlayTracks: (List<Track>, Track) -> Unit
) {
    val context = LocalContext.current
    val spacing = if (metrics.compact) 8.dp else 14.dp
    val topQueue = remember(stats, trackMap) { stats.topSongs.mapNotNull { song -> song.trackId?.let { trackMap[it] } } }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
        modifier = Modifier.fillMaxWidth()
    ) {
        when (page) {
            WrappedPage.INTRO -> {
                Reveal(0) { Bullseye(size = metrics.artSize * 0.9f, accent = accent) }
                BigText(stringResource(introTitleRes(stats.period.range)), 1, metrics.compact)
                Detail(periodDates(context, stats.period), 2)
                Reveal(4) {
                    Text(
                        text = stringResource(R.string.wrapped_intro_hint),
                        style = MaterialTheme.typography.labelLarge,
                        color = WrappedSubtle,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
            WrappedPage.MINUTES -> {
                val minutes = minutesOf(stats.totalMs)
                PageTitle(stringResource(R.string.wrapped_minutes_title))
                Reveal(1) {
                    CountUp(
                        target = minutes,
                        style = if (metrics.compact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge.copy(fontSize = 84.sp)
                    )
                }
                Reveal(2) {
                    Text(
                        text = pluralStringResource(R.plurals.stats_minutes_word, minutes.toPluralCount()),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
                if (minutes >= 120) Detail(stringResource(R.string.wrapped_hours_detail, formatNumber(minutes / 60)), 3)
                changeText(stats)?.let { Detail(it, 4) }
            }
            WrappedPage.GENRE -> {
                val genres = stats.topGenres.take(3)
                val total = stats.genreTotalMs.coerceAtLeast(1L)
                PageTitle(stringResource(R.string.wrapped_genre_title))
                BigText(genres.first().name, 1, metrics.compact, accent)
                Spacer(modifier = Modifier.height(8.dp))
                genres.forEachIndexed { position, genre ->
                    Reveal(2 + position) {
                        GenreLine(genre = genre, share = genre.listenedMs.toFloat() / total, rank = position + 1, accent = accent)
                    }
                }
            }
            WrappedPage.CLOCK -> {
                val hour = stats.peakHour ?: 0
                PageTitle(stringResource(R.string.wrapped_clock_title))
                BigText(stringResource(personaRes(hour)), 1, metrics.compact, accent)
                Reveal(2) {
                    ListeningClock(
                        hours = stats.hours,
                        interactive = false,
                        barColor = accent,
                        trackColor = WrappedText.copy(alpha = 0.12f),
                        labelColor = WrappedSubtle,
                        detailColor = WrappedText,
                        modifier = Modifier.size(metrics.artSize)
                    )
                }
                Detail(stringResource(R.string.stats_persona_detail, hourLabel(context, hour)), 3)
            }
            WrappedPage.TOP_ARTIST -> {
                val artist = stats.topArtists.first()
                PageTitle(stringResource(R.string.wrapped_top_artist_title))
                Reveal(1) {
                    BreathingArt(size = metrics.artSize) {
                        ArtThumb(
                            artist.trackId?.let { trackMap[it] },
                            Icons.Filled.Person,
                            RoundedCornerShape(28.dp),
                            64.dp,
                            Modifier.size(metrics.artSize),
                            artistImage = ArtistImageStore.images[artist.key]
                        )
                    }
                }
                BigText(artist.name, 2, metrics.compact)
                Detail(playsAndTime(artist), 3)
            }
            WrappedPage.TOP_ARTISTS -> {
                PageTitle(stringResource(R.string.wrapped_top_artists_title))
                stats.topArtists.take(metrics.listCount).forEachIndexed { position, item ->
                    Reveal(1 + position) {
                        StoryRow(rank = position + 1, item = item, accent = accent) {
                            ArtThumb(
                                item.trackId?.let { trackMap[it] },
                                Icons.Filled.Person,
                                RoundedCornerShape(12.dp),
                                22.dp,
                                Modifier.size(52.dp),
                                artistImage = ArtistImageStore.images[item.key]
                            )
                        }
                    }
                }
            }
            WrappedPage.TOP_SONG -> {
                val song = stats.topSongs.first()
                val track = song.trackId?.let { trackMap[it] }
                PageTitle(stringResource(R.string.wrapped_top_song_title))
                Reveal(1) {
                    BreathingArt(size = metrics.artSize) {
                        ArtThumb(track, Icons.Filled.MusicNote, RoundedCornerShape(28.dp), 64.dp, Modifier.size(metrics.artSize))
                    }
                }
                BigText(song.name, 2, metrics.compact)
                if (song.subtitle.isNotBlank()) Detail(song.subtitle, 3)
                Reveal(4) {
                    Text(
                        text = pluralStringResource(R.plurals.wrapped_top_song_detail, song.plays, song.plays),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        textAlign = TextAlign.Center
                    )
                }
                if (track != null) {
                    Reveal(5) {
                        StoryButton(
                            text = stringResource(R.string.wrapped_play_song),
                            filled = true,
                            icon = Icons.Filled.PlayArrow,
                            onClick = { onPlayTracks(topQueue.ifEmpty { listOf(track) }, track) }
                        )
                    }
                }
            }
            WrappedPage.TOP_SONGS -> {
                PageTitle(stringResource(R.string.wrapped_top_songs_title))
                stats.topSongs.take(metrics.listCount).forEachIndexed { position, item ->
                    Reveal(1 + position) {
                        StoryRow(rank = position + 1, item = item, accent = accent) {
                            ArtThumb(item.trackId?.let { trackMap[it] }, Icons.Filled.MusicNote, RoundedCornerShape(12.dp), 22.dp, Modifier.size(52.dp))
                        }
                    }
                }
            }
            WrappedPage.TOP_ALBUM -> {
                val album = stats.topAlbums.first()
                PageTitle(stringResource(R.string.wrapped_top_album_title))
                Reveal(1) {
                    BreathingArt(size = metrics.artSize) {
                        ArtThumb(album.trackId?.let { trackMap[it] }, Icons.Filled.Album, RoundedCornerShape(28.dp), 64.dp, Modifier.size(metrics.artSize))
                    }
                }
                BigText(album.name, 2, metrics.compact)
                if (album.subtitle.isNotBlank()) Detail(album.subtitle, 3)
                Detail(playsAndTime(album), 4)
            }
            WrappedPage.DISCOVERIES -> {
                val count = stats.newArtists.size
                PageTitle(stringResource(R.string.wrapped_discoveries_title))
                Reveal(1) {
                    CountUp(
                        target = count.toLong(),
                        style = if (metrics.compact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp),
                        color = accent
                    )
                }
                BigText(pluralStringResource(R.plurals.wrapped_new_artists_word, count), 2, metrics.compact)
                Detail(stringResource(R.string.wrapped_discoveries_including, stats.newArtists.first()), 3)
                if (stats.newSongs > 0) Detail(pluralStringResource(R.plurals.wrapped_new_songs, stats.newSongs, stats.newSongs), 4)
            }
            WrappedPage.BIG_DAY -> {
                val day = stats.biggestDay
                PageTitle(stringResource(R.string.wrapped_big_day_title))
                if (day != null) BigText(dayLabel(context, day), 1, metrics.compact, accent)
                Detail(stringResource(R.string.wrapped_big_day_detail, durationText(stats.biggestDayMs)), 2)
                Reveal(3) {
                    ActivityChart(
                        period = stats.period,
                        buckets = stats.buckets,
                        barColor = accent,
                        trackColor = WrappedText.copy(alpha = 0.12f),
                        labelColor = WrappedSubtle,
                        detailColor = WrappedText,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
            WrappedPage.ACHIEVEMENTS -> {
                PageTitle(stringResource(R.string.wrapped_achievements_title))
                BigText(pluralStringResource(R.plurals.wrapped_achievements_count, earned.size, earned.size), 1, metrics.compact, accent)
                val shown = earned.take(if (metrics.compact) 3 else 4)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    shown.forEachIndexed { position, state ->
                        Reveal(2 + position, modifier = Modifier.weight(1f, fill = false).widthIn(max = 96.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AchievementBadge(
                                    achievement = state.achievement,
                                    unlocked = true,
                                    size = if (metrics.compact) 56.dp else 68.dp,
                                    accent = accent
                                )
                                Text(
                                    text = stringResource(achievementTitle(state.achievement)),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WrappedText,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
                if (earned.size > shown.size) {
                    Detail(stringResource(R.string.wrapped_achievements_more, earned.size - shown.size), 2 + shown.size)
                }
            }
            WrappedPage.SUMMARY -> Unit
        }
    }
}

@Composable
internal fun playsAndTime(item: RankedItem): String =
    stringResource(
        R.string.stats_bucket_detail,
        pluralStringResource(R.plurals.stats_play_count, item.plays, item.plays),
        durationText(item.listenedMs)
    )

@Composable
private fun Bullseye(size: Dp, accent: Color) {
    val transition = rememberInfiniteTransition(label = "wrapped_bullseye")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "wrapped_bullseye_pulse"
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "wrapped_bullseye_spin"
    )
    val ring = LocalClintColors.current.onSurface
    val hole = LocalClintColors.current.background
    Canvas(modifier = Modifier.size(size).graphicsLayer { rotationZ = spin }) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension / 2f
        for (ring in 0 until 4) {
            val progress = ((ring / 4f) + pulse) % 1f
            drawCircle(
                color = accent.copy(alpha = 0.5f * (1f - progress)),
                radius = radius * (0.35f + progress * 0.65f),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        drawCircle(color = hole.copy(alpha = 0.6f), radius = radius * 0.58f, center = center)
        for (groove in 1..3) {
            drawCircle(
                color = ring.copy(alpha = 0.14f),
                radius = radius * (0.58f - groove * 0.07f),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        drawCircle(color = accent, radius = radius * 0.3f, center = center)
        drawCircle(color = hole, radius = radius * 0.18f, center = center)
        drawCircle(color = accent, radius = radius * 0.08f, center = center)
        drawArc(
            color = ring.copy(alpha = 0.4f),
            startAngle = -60f,
            sweepAngle = 40f,
            useCenter = false,
            topLeft = Offset(center.x - radius * 0.5f, center.y - radius * 0.5f),
            size = androidx.compose.ui.geometry.Size(radius, radius),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
private fun BreathingArt(size: Dp, content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "wrapped_art")
    val breath by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wrapped_art_breath"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = breath
                scaleY = breath
                shadowElevation = 24.dp.toPx()
                shape = RoundedCornerShape(28.dp)
                clip = true
            }
    ) {
        content()
    }
}

@Composable
private fun GenreLine(genre: RankedItem, share: Float, rank: Int, accent: Color) {
    val grow = remember { Animatable(0f) }
    LaunchedEffect(grow) {
        delay(300L + rank * 150L)
        grow.animateTo(share.coerceIn(0f, 1f), tween(900, easing = FastOutSlowInEasing))
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = genre.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WrappedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(grow.value * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (rank == 1) accent else WrappedSubtle
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 6.dp, bottom = 6.dp)
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(WrappedText.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(grow.value)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (rank == 1) accent else accent.copy(alpha = 0.45f))
            )
        }
    }
}

@Composable
private fun StoryRow(rank: Int, item: RankedItem, accent: Color, art: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (rank == 1) accent.copy(alpha = 0.18f) else WrappedText.copy(alpha = 0.06f))
            .padding(10.dp)
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = if (rank == 1) accent else WrappedText,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp)
        )
        art()
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WrappedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (item.subtitle.isNotBlank()) {
                    stringResource(R.string.stats_bucket_detail, item.subtitle, pluralStringResource(R.plurals.stats_play_count, item.plays, item.plays))
                } else {
                    pluralStringResource(R.plurals.stats_play_count, item.plays, item.plays)
                },
                style = MaterialTheme.typography.bodySmall,
                color = WrappedSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StoryButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val colors = LocalClintColors.current
    val content = if (filled) colors.onPrimary else colors.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
            .clip(RoundedCornerShape(50))
            .then(
                if (filled) {
                    Modifier.background(colors.primary)
                } else {
                    Modifier.border(BorderStroke(1.5.dp, colors.onSurface.copy(alpha = 0.4f)), RoundedCornerShape(50))
                }
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            KateIcon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryScreen(
    stats: ListeningStats,
    trackMap: Map<String, Track>,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    val captureLayer = rememberGraphicsLayer()
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { ListeningHistory.ensureLoaded(context) }
    val branding = ListeningHistory.wrappedBranding
    val fileName = remember(stats.period) {
        "kate-wrapped-${stats.period.range.name.lowercase()}-${stats.period.start}.png"
    }
    val shareTitle = stringResource(R.string.wrapped_share_title)
    val savedMessage = stringResource(R.string.wrapped_saved)
    val saveFailedMessage = stringResource(R.string.wrapped_save_failed)
    val imageFailedMessage = stringResource(R.string.wrapped_image_failed)
    val permissionDeniedMessage = stringResource(R.string.wrapped_storage_denied)
    val toast: (String) -> Unit = { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }

    suspend fun capture(): Bitmap? = runCatching {
        val bitmap = captureLayer.toImageBitmap().asAndroidBitmap()
        if (bitmap.config == Bitmap.Config.HARDWARE) bitmap.copy(Bitmap.Config.ARGB_8888, false) else bitmap
    }.getOrNull()

    fun export(action: suspend (Bitmap) -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            val bitmap = capture()
            if (bitmap == null) toast(imageFailedMessage) else action(bitmap)
            busy = false
        }
    }

    val save: () -> Unit = {
        export { bitmap ->
            val saved = WrappedExport.save(context, bitmap, fileName)
            if (saved) haptics.confirm()
            toast(if (saved) savedMessage else saveFailedMessage)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) save() else toast(permissionDeniedMessage)
    }
    val onDownload: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        if (WrappedExport.needsStoragePermission && !granted) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            save()
        }
    }
    val onShare: () -> Unit = {
        export { bitmap ->
            if (!WrappedExport.share(context, bitmap, fileName, shareTitle)) toast(imageFailedMessage)
        }
    }
    val onBrandingChange: (Boolean) -> Unit = { value ->
        haptics.toggle(value)
        ListeningHistory.setWrappedBranding(context, value)
    }

    BoxWithConstraints(modifier = modifier) {
        val wide = maxWidth > maxHeight && maxWidth >= 560.dp
        val sideWidth = min(maxWidth * 0.42f, 360.dp)
        val posterShape = RoundedCornerShape(22.dp)
        val poster: @Composable () -> Unit = {
            WrappedPosterFrame(
                modifier = Modifier
                    .shadow(elevation = 18.dp, shape = posterShape)
                    .clip(posterShape)
                    .border(BorderStroke(1.dp, colors.onSurface.copy(alpha = 0.12f)), posterShape)
            ) {
                WrappedPoster(stats = stats, trackMap = trackMap, branding = branding, captureLayer = captureLayer)
            }
        }
        val controls: @Composable () -> Unit = {
            SummaryControls(
                branding = branding,
                busy = busy,
                onBrandingChange = onBrandingChange,
                onShare = onShare,
                onDownload = onDownload,
                onDone = onDone
            )
        }
        if (wide) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxSize()
            ) {
                Reveal(0, modifier = Modifier.weight(1f).fillMaxHeight()) { poster() }
                Reveal(2, modifier = Modifier.width(sideWidth)) { controls() }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
                Reveal(0, modifier = Modifier.weight(1f).fillMaxWidth()) { poster() }
                Spacer(modifier = Modifier.height(14.dp))
                Reveal(2, modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth()) { controls() }
            }
        }
    }
}

@Composable
private fun SummaryControls(
    branding: Boolean,
    busy: Boolean,
    onBrandingChange: (Boolean) -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            WatermarkToggle(checked = branding, onCheckedChange = onBrandingChange, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.wrapped_done),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = WrappedSubtle,
                maxLines = 1,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onDone)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StoryButton(
                text = stringResource(R.string.wrapped_share),
                filled = true,
                icon = Icons.Filled.Share,
                enabled = !busy,
                onClick = onShare,
                modifier = Modifier.weight(1f)
            )
            StoryButton(
                text = stringResource(R.string.wrapped_download),
                filled = false,
                icon = Icons.Filled.Download,
                enabled = !busy,
                onClick = onDownload,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WatermarkToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(colors.onSurface.copy(alpha = 0.08f))
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
    ) {
        KateIcon(
            imageVector = Icons.AutoMirrored.Outlined.BrandingWatermark,
            contentDescription = null,
            tint = if (checked) colors.primary else colors.secondaryText,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(R.string.wrapped_watermark),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
        )
        ClintSwitch(checked = checked, onCheckedChange = null)
    }
}
