package com.jhaiian.kate.ui.stats

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.ArtistImageStore
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.data.ListeningStats
import com.jhaiian.kate.data.ListeningStatsRepository
import com.jhaiian.kate.data.RankedItem
import com.jhaiian.kate.data.StatsPeriod
import com.jhaiian.kate.data.StatsRange
import com.jhaiian.kate.data.TagSettings
import com.jhaiian.kate.data.WrappedAvailability
import com.jhaiian.kate.data.wrappedAvailability
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.library.ArtThumb
import com.jhaiian.kate.ui.theme.LocalClintColors
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private const val COLLAPSED_COUNT = 5

internal fun periodAt(range: StatsRange, offset: Int): StatsPeriod {
    var period = StatsPeriod.containing(range, LocalDate.now())
    repeat(-offset) { period = period.previous() }
    return period
}

@Composable
fun StatsScreen(
    bottomBarConnection: NestedScrollConnection?,
    onOpenWrapped: (ListeningStats) -> Unit,
    onPlayTracks: (List<Track>, Track) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String, Boolean) -> Unit,
    onGenreClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onOpenAchievements: () -> Unit = {}
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ListeningHistory.ensureLoaded(context)
        TagSettings.ensureLoaded(context)
    }
    var range by rememberSaveable { mutableStateOf(StatsRange.WEEK) }
    var offset by rememberSaveable(range) { mutableIntStateOf(0) }
    val version = ListeningHistory.version
    val config = TagSettings.config

    val earliest by produceState<StatsPeriod?>(null, range, version) {
        value = ListeningStatsRepository.earliestPeriod(context, range)
    }
    val stepped = remember(range, offset) { periodAt(range, offset) }
    val period = if (range == StatsRange.ALL_TIME) earliest ?: stepped else stepped
    val librarySize = LibraryStore.tracks.size
    val stats by produceState<ListeningStats?>(null, period, version, config, librarySize) {
        value = ListeningStatsRepository.load(context, period, config, LibraryStore.tracks.toList())
    }
    val canGoBack = earliest?.let { period.start.isAfter(it.start) } ?: false
    val canGoForward = offset < 0

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        val wide = maxWidth >= 720.dp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it }
                .verticalScroll(rememberScrollState())
                .padding(bottom = LocalBottomContentPadding.current + 24.dp)
        ) {
            Column(modifier = Modifier.widthIn(max = 960.dp).fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.stats_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 16.dp)
                )
                AchievementsCard(
                    onOpen = onOpenAchievements,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                )
                RangeSelector(
                    selected = range,
                    onSelect = { range = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                PeriodNavigator(
                    label = periodLabel(period),
                    detail = if (period.canStep) null else periodDates(context, period),
                    showArrows = period.canStep,
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    onBack = { offset-- },
                    onForward = { offset++ },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                AnimatedContent(
                    targetState = stats,
                    contentKey = { it?.period },
                    transitionSpec = {
                        val forward = (targetState?.period?.start ?: LocalDate.MIN) >= (initialState?.period?.start ?: LocalDate.MIN)
                        (slideInHorizontally(tween(320)) { width -> if (forward) width / 5 else -width / 5 } + fadeIn(tween(240)))
                            .togetherWith(slideOutHorizontally(tween(320)) { width -> if (forward) -width / 5 else width / 5 } + fadeOut(tween(180)))
                    },
                    label = "stats_content"
                ) { current ->
                    when {
                        current == null -> LoadingState()
                        current.isEmpty -> EmptyState()
                        else -> StatsContent(
                            stats = current,
                            wide = wide,
                            onOpenWrapped = { onOpenWrapped(current) },
                            onPlayTracks = onPlayTracks,
                            onAlbumClick = onAlbumClick,
                            onArtistClick = onArtistClick,
                            onGenreClick = onGenreClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RangeSelector(selected: StatsRange, onSelect: (StatsRange) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .padding(4.dp)
    ) {
        StatsRange.entries.forEach { range ->
            val isSelected = range == selected
            val background by animateColorAsState(if (isSelected) colors.primary else Color.Transparent, label = "range_bg")
            val content by animateColorAsState(if (isSelected) colors.onPrimary else colors.secondaryText, label = "range_fg")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(background)
                    .clickable { onSelect(range) }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(
                        when (range) {
                            StatsRange.WEEK -> R.string.stats_range_week
                            StatsRange.MONTH -> R.string.stats_range_month
                            StatsRange.YEAR -> R.string.stats_range_year
                            StatsRange.ALL_TIME -> R.string.stats_range_all_time
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = content
                )
            }
        }
    }
}

@Composable
private fun PeriodNavigator(
    label: String,
    detail: String?,
    showArrows: Boolean,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val arrowAlpha = if (showArrows) 1f else 0f
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        KateIconButton(
            onClick = onBack,
            enabled = showArrows && canGoBack,
            modifier = Modifier.graphicsLayer { alpha = arrowAlpha }
        ) {
            KateIcon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.stats_previous_period),
                tint = if (canGoBack) buttonTint(colors.onSurface) else buttonTint(colors.secondaryText).copy(alpha = 0.35f)
            )
        }
        AnimatedContent(
            targetState = label to detail,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "period_label",
            modifier = Modifier.weight(1f)
        ) { (text, subtitle) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.secondaryText,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        KateIconButton(
            onClick = onForward,
            enabled = showArrows && canGoForward,
            modifier = Modifier.graphicsLayer { alpha = arrowAlpha }
        ) {
            KateIcon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.stats_next_period),
                tint = if (canGoForward) buttonTint(colors.onSurface) else buttonTint(colors.secondaryText).copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
private fun LoadingState() {
    val colors = LocalClintColors.current
    Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colors.primary, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
    }
}

@Composable
private fun EmptyState() {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = Icons.Outlined.Insights, contentDescription = null, tint = colors.primary, modifier = Modifier.size(36.dp))
        }
        Text(
            text = stringResource(R.string.stats_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = stringResource(if (ListeningHistory.enabled) R.string.stats_empty_message else R.string.stats_disabled_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp).widthIn(max = 420.dp)
        )
    }
}

@Composable
private fun StatsContent(
    stats: ListeningStats,
    wide: Boolean,
    onOpenWrapped: () -> Unit,
    onPlayTracks: (List<Track>, Track) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String, Boolean) -> Unit,
    onGenreClick: (String) -> Unit
) {
    val tracks = LibraryStore.tracks
    val trackMap = remember(stats, tracks.size) { tracks.associateBy { it.id } }
    val index = LibraryStore.index
    val playSong: (RankedItem) -> Unit = { item ->
        val queue = stats.topSongs.mapNotNull { song -> song.trackId?.let { trackMap[it] } }
        val start = item.trackId?.let { trackMap[it] }
        if (start != null) onPlayTracks(queue, start)
    }
    val openArtist: (RankedItem) -> Unit = { item ->
        if (index.artist(item.key, albumArtist = false) != null) onArtistClick(item.key, false)
    }
    val openAlbum: (RankedItem) -> Unit = { item ->
        val trackId = item.trackId
        val key = index.albums.firstOrNull { album -> album.tracks.any { it.id == trackId } }?.key
        if (key != null) onAlbumClick(key)
    }
    val openGenre: (RankedItem) -> Unit = { item ->
        if (index.group(LibraryCategory.GENRES, item.key) != null) onGenreClick(item.key)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        val availability = remember(stats) { stats.wrappedAvailability() }
        if (availability != WrappedAvailability.Unavailable) {
            WrappedHeroCard(stats = stats, availability = availability, onClick = onOpenWrapped)
            Spacer(modifier = Modifier.height(16.dp))
        }
        SummaryTiles(stats = stats, wide = wide)
        Spacer(modifier = Modifier.height(16.dp))
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatsCard(title = stringResource(R.string.stats_activity_title), modifier = Modifier.weight(1.4f)) {
                    ActivityChart(period = stats.period, buckets = stats.buckets)
                    ChartHint()
                }
                StatsCard(title = stringResource(R.string.stats_clock_title), modifier = Modifier.weight(1f)) {
                    ClockSection(stats)
                }
            }
        } else {
            StatsCard(title = stringResource(R.string.stats_activity_title)) {
                ActivityChart(period = stats.period, buckets = stats.buckets)
                ChartHint()
            }
            Spacer(modifier = Modifier.height(16.dp))
            StatsCard(title = stringResource(R.string.stats_clock_title)) {
                ClockSection(stats)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        val songsCard: @Composable (Modifier) -> Unit = { cardModifier ->
            RankedListCard(
                title = stringResource(R.string.stats_top_songs),
                items = stats.topSongs,
                modifier = cardModifier,
                art = { item -> ArtThumb(item.trackId?.let { trackMap[it] }, Icons.Filled.MusicNote, RoundedCornerShape(10.dp), 20.dp, Modifier.size(48.dp)) },
                onClick = playSong
            )
        }
        val artistsCard: @Composable (Modifier) -> Unit = { cardModifier ->
            RankedListCard(
                title = stringResource(R.string.stats_top_artists),
                items = stats.topArtists,
                modifier = cardModifier,
                art = { item ->
                    ArtThumb(
                        item.trackId?.let { trackMap[it] },
                        Icons.Filled.Person,
                        RoundedCornerShape(10.dp),
                        20.dp,
                        Modifier.size(48.dp),
                        artistImage = ArtistImageStore.images[item.key]
                    )
                },
                onClick = openArtist
            )
        }
        if (wide) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                songsCard(Modifier.weight(1f))
                artistsCard(Modifier.weight(1f))
            }
        } else {
            songsCard(Modifier)
            Spacer(modifier = Modifier.height(16.dp))
            artistsCard(Modifier)
        }
        if (stats.topAlbums.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            AlbumShelf(stats.topAlbums, trackMap, onClick = openAlbum)
        }
        if (stats.topGenres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            GenreCard(stats, onClick = openGenre)
        }
    }
}

@Composable
private fun ChartHint() {
    val colors = LocalClintColors.current
    Text(
        text = stringResource(R.string.stats_tap_hint),
        style = MaterialTheme.typography.labelSmall,
        color = colors.secondaryText,
        modifier = Modifier.padding(top = 10.dp)
    )
}

@Composable
private fun ClockSection(stats: ListeningStats) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        ListeningClock(hours = stats.hours, modifier = Modifier.widthIn(max = 260.dp).fillMaxWidth())
        stats.peakHour?.let { hour ->
            Text(
                text = stringResource(personaRes(hour)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = stringResource(R.string.stats_persona_detail, hourLabel(context, hour)),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = LocalClintColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            trailing?.invoke()
        }
        content()
    }
}

@Composable
private fun WrappedHeroCard(stats: ListeningStats, availability: WrappedAvailability, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val ready = availability == WrappedAvailability.Ready
    val transition = rememberInfiniteTransition(label = "wrapped_hero")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse),
        label = "wrapped_hero_drift"
    )
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "wrapped_hero_pulse"
    )
    val gradient = remember(colors) { wrappedGradient(colors, 0) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 168.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(enabled = ready, onClick = onClick)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                Brush.linearGradient(
                    colors = gradient,
                    start = Offset(size.width * drift, 0f),
                    end = Offset(size.width * (1f - drift), size.height)
                )
            )
            val center = Offset(size.width * 0.86f, size.height * 0.5f)
            val base = size.height * 0.62f
            for (ring in 0 until 5) {
                val progress = ((ring / 5f) + pulse) % 1f
                drawCircle(
                    color = colors.primary.copy(alpha = (if (ready) 0.4f else 0.18f) * (1f - progress)),
                    radius = base * (0.25f + progress),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            drawCircle(color = colors.primary, radius = base * 0.16f, center = center)
            drawCircle(color = colors.background, radius = base * 0.06f, center = center)
        }
        Column(modifier = Modifier.padding(22.dp).fillMaxWidth(0.72f)) {
            KateIcon(
                imageVector = if (ready) Icons.Filled.AutoAwesome else Icons.Filled.Lock,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = stringResource(wrappedCardTitleRes(stats.period.range)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = when (availability) {
                    is WrappedAvailability.Locked -> {
                        val days = ChronoUnit.DAYS.between(LocalDate.now(), availability.unlocksOn).toInt().coerceAtLeast(1)
                        pluralStringResource(R.plurals.wrapped_locked_days, days, days)
                    }
                    WrappedAvailability.NotEnough -> stringResource(R.string.wrapped_not_enough)
                    else -> stringResource(R.string.wrapped_card_subtitle)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (availability is WrappedAvailability.Locked || ready) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (ready) colors.primary else colors.primary.copy(alpha = 0.16f))
                        .padding(start = 10.dp, end = 14.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    val content = if (ready) colors.onPrimary else colors.primary
                    KateIcon(
                        imageVector = if (ready) Icons.Filled.PlayArrow else Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (availability is WrappedAvailability.Locked) {
                            stringResource(R.string.wrapped_unlocks_on, unlockDate(context, availability.unlocksOn))
                        } else {
                            periodLabel(stats.period)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = content,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryTiles(stats: ListeningStats, wide: Boolean) {
    val colors = LocalClintColors.current
    val minutes = minutesOf(stats.totalMs)
    val tiles = listOf(
        Triple(R.string.stats_minutes_label, minutes, true),
        Triple(R.string.stats_plays_label, stats.plays.toLong(), false),
        Triple(R.string.stats_songs_label, stats.uniqueSongs.toLong(), false),
        Triple(R.string.stats_artists_label, stats.uniqueArtists.toLong(), false)
    )
    Column {
        val rows = if (wide) listOf(tiles) else tiles.chunked(2)
        rows.forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, value, highlight) ->
                    StatTile(
                        label = stringResource(label),
                        value = value,
                        highlight = highlight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        changeText(stats)?.let { text ->
            val up = (stats.changePercent ?: 0) >= 0
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (up) colors.primary else colors.secondaryText,
                modifier = Modifier.padding(start = 4.dp, top = 10.dp)
            )
        }
    }
}

@Composable
private fun StatTile(label: String, value: Long, highlight: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val counter = remember(value) { Animatable(0f) }
    LaunchedEffect(counter) { counter.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing)) }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (highlight) colors.primary.copy(alpha = 0.16f) else colors.cardBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = formatNumber((value * counter.value).toLong()),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (highlight) colors.primary else colors.onSurface,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.secondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RankedListCard(
    title: String,
    items: List<RankedItem>,
    art: @Composable (RankedItem) -> Unit,
    onClick: (RankedItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    val canExpand = items.size > COLLAPSED_COUNT
    StatsCard(
        title = title,
        modifier = modifier.animateContentSize(),
        trailing = if (canExpand) {
            {
                Text(
                    text = stringResource(if (expanded) R.string.stats_show_less else R.string.stats_show_more),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        } else {
            null
        }
    ) {
        val maxPlays = (items.maxOfOrNull { it.plays } ?: 1).coerceAtLeast(1)
        val shown = if (expanded) items else items.take(COLLAPSED_COUNT)
        shown.forEachIndexed { position, item ->
            RankedRow(
                rank = position + 1,
                item = item,
                share = item.plays.toFloat() / maxPlays,
                art = { art(item) },
                onClick = { onClick(item) }
            )
        }
    }
}

@Composable
private fun RankedRow(
    rank: Int,
    item: RankedItem,
    share: Float,
    art: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val colors = LocalClintColors.current
    val grow = remember(item) { Animatable(0f) }
    LaunchedEffect(grow) { grow.animateTo(share, tween(durationMillis = 700, delayMillis = rank * 60, easing = FastOutSlowInEasing)) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (rank == 1) colors.primary else colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(28.dp)
        )
        art()
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(
                text = item.name.ifBlank { stringResource(R.string.player_unknown_artist) },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (item.subtitle.isNotBlank()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(grow.value.coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.primary.copy(alpha = if (rank == 1) 1f else 0.6f))
                )
            }
        }
        Text(
            text = pluralStringResource(R.plurals.stats_play_count, item.plays, item.plays),
            style = MaterialTheme.typography.labelMedium,
            color = colors.secondaryText,
            maxLines = 1
        )
    }
}

@Composable
private fun AlbumShelf(albums: List<RankedItem>, trackMap: Map<String, Track>, onClick: (RankedItem) -> Unit) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .padding(vertical = 18.dp)
    ) {
        Text(
            text = stringResource(R.string.stats_top_albums),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            itemsIndexed(albums, key = { _, item -> item.key }) { position, item ->
                AlbumTile(rank = position + 1, item = item, track = item.trackId?.let { trackMap[it] }, onClick = { onClick(item) })
            }
        }
    }
}

@Composable
private fun AlbumTile(rank: Int, item: RankedItem, track: Track?, onClick: () -> Unit, size: Dp = 132.dp) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .width(size)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box {
            ArtThumb(track, Icons.Filled.Album, RoundedCornerShape(16.dp), 36.dp, Modifier.fillMaxWidth().aspectRatio(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (rank == 1) colors.primary else colors.cardBackground.copy(alpha = 0.85f))
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (rank == 1) colors.onPrimary else colors.onSurface
                )
            }
        }
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = pluralStringResource(R.plurals.stats_play_count, item.plays, item.plays),
            style = MaterialTheme.typography.labelMedium,
            color = colors.secondaryText,
            maxLines = 1
        )
    }
}

@Composable
private fun GenreCard(stats: ListeningStats, onClick: (RankedItem) -> Unit) {
    val colors = LocalClintColors.current
    val genres = stats.topGenres.take(6)
    val total = stats.genreTotalMs.coerceAtLeast(1L)
    val palette = remember(colors) {
        primaryShades(colors, 6)
    }
    StatsCard(title = stringResource(R.string.stats_top_genres)) {
        GenreBar(
            shares = genres.map { it.listenedMs.toFloat() / total },
            colors = palette,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        genres.forEachIndexed { position, genre ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onClick(genre) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(palette[position % palette.size])
                )
                Text(
                    text = genre.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
                Text(
                    text = "${(genre.listenedMs * 100 / total)}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.secondaryText
                )
            }
        }
    }
}
