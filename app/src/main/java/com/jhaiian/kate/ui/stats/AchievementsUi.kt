package com.jhaiian.kate.ui.stats

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.Achievement
import com.jhaiian.kate.data.AchievementCenter
import com.jhaiian.kate.data.AchievementState
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

internal fun achievementTitle(achievement: Achievement): Int = when (achievement) {
    Achievement.PLAYS_100 -> R.string.achievement_plays_100
    Achievement.PLAYS_1000 -> R.string.achievement_plays_1000
    Achievement.PLAYS_10000 -> R.string.achievement_plays_10000
    Achievement.HOURS_10 -> R.string.achievement_hours_10
    Achievement.HOURS_100 -> R.string.achievement_hours_100
    Achievement.STREAK_7 -> R.string.achievement_streak_7
    Achievement.NIGHT_OWL -> R.string.achievement_night_owl
    Achievement.EARLY_BIRD -> R.string.achievement_early_bird
    Achievement.ALBUM_FINISHER -> R.string.achievement_album_finisher
    Achievement.EXPLORER -> R.string.achievement_explorer
    Achievement.GENRE_HOPPER -> R.string.achievement_genre_hopper
    Achievement.LOYAL_FAN -> R.string.achievement_loyal_fan
}

internal fun achievementDescription(achievement: Achievement): Int = when (achievement) {
    Achievement.PLAYS_100 -> R.string.achievement_plays_100_description
    Achievement.PLAYS_1000 -> R.string.achievement_plays_1000_description
    Achievement.PLAYS_10000 -> R.string.achievement_plays_10000_description
    Achievement.HOURS_10 -> R.string.achievement_hours_10_description
    Achievement.HOURS_100 -> R.string.achievement_hours_100_description
    Achievement.STREAK_7 -> R.string.achievement_streak_7_description
    Achievement.NIGHT_OWL -> R.string.achievement_night_owl_description
    Achievement.EARLY_BIRD -> R.string.achievement_early_bird_description
    Achievement.ALBUM_FINISHER -> R.string.achievement_album_finisher_description
    Achievement.EXPLORER -> R.string.achievement_explorer_description
    Achievement.GENRE_HOPPER -> R.string.achievement_genre_hopper_description
    Achievement.LOYAL_FAN -> R.string.achievement_loyal_fan_description
}

internal fun achievementIcon(achievement: Achievement): ImageVector = when (achievement) {
    Achievement.PLAYS_100, Achievement.PLAYS_1000, Achievement.PLAYS_10000 -> Icons.Filled.PlayCircle
    Achievement.HOURS_10, Achievement.HOURS_100 -> Icons.Filled.Schedule
    Achievement.STREAK_7 -> Icons.Filled.LocalFireDepartment
    Achievement.NIGHT_OWL -> Icons.Filled.NightsStay
    Achievement.EARLY_BIRD -> Icons.Filled.WbSunny
    Achievement.ALBUM_FINISHER -> Icons.Filled.Album
    Achievement.EXPLORER -> Icons.Filled.TravelExplore
    Achievement.GENRE_HOPPER -> Icons.Filled.Style
    Achievement.LOYAL_FAN -> Icons.Filled.Favorite
}

@Composable
internal fun AchievementBadge(
    achievement: Achievement,
    unlocked: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
    fraction: Float = if (unlocked) 1f else 0f,
    accent: Color = LocalClintColors.current.primary
) {
    val colors = LocalClintColors.current
    val ringTrack = colors.surfaceVariant
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .drawBehind {
                if (!unlocked) {
                    val stroke = 3.dp.toPx()
                    val inset = stroke / 2f
                    val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
                    drawArc(
                        color = ringTrack,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arcSize,
                        style = Stroke(width = stroke)
                    )
                    if (fraction > 0f) {
                        drawArc(
                            color = accent,
                            startAngle = -90f,
                            sweepAngle = 360f * fraction,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
            }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (unlocked) size else size - 10.dp)
                .clip(CircleShape)
                .background(
                    if (unlocked) {
                        Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.55f)))
                    } else {
                        Brush.linearGradient(listOf(colors.surfaceVariant, colors.surfaceVariant))
                    }
                )
        ) {
            KateIcon(
                imageVector = achievementIcon(achievement),
                contentDescription = null,
                tint = if (unlocked) colors.onPrimary else colors.secondaryText.copy(alpha = 0.6f),
                modifier = Modifier.size(size * 0.46f)
            )
        }
    }
}

@Composable
private fun progressText(state: AchievementState): String {
    if (state.unlocked) {
        val date = state.unlockedAt?.let { DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(it)) }.orEmpty()
        return stringResource(R.string.achievement_unlocked_on, date)
    }
    if (state.achievement.percent) return stringResource(R.string.achievement_percent, state.progress.toInt())
    val format = NumberFormat.getIntegerInstance()
    return stringResource(R.string.achievement_progress, format.format(state.progress), format.format(state.achievement.target))
}

@Composable
private fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, color: Color = LocalClintColors.current.primary) {
    val colors = LocalClintColors.current
    val grow = remember { Animatable(0f) }
    LaunchedEffect(fraction) { grow.animateTo(fraction, tween(durationMillis = 900, easing = FastOutSlowInEasing)) }
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(colors.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(grow.value.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

@Composable
internal fun AchievementsCard(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { AchievementCenter.refresh(context) }
    val states = AchievementCenter.states
    if (states.isEmpty()) return
    val unlocked = states.count { it.unlocked }
    val preview = remember(states) {
        states.filter { it.unlocked }.sortedByDescending { it.unlockedAt } +
            states.filterNot { it.unlocked }.sortedByDescending { it.fraction }
    }.take(6)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .clickable(onClick = onOpen)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            KateIcon(imageVector = Icons.Filled.EmojiEvents, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = stringResource(R.string.achievements_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = stringResource(R.string.achievements_count, unlocked, states.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText
                )
            }
            Text(
                text = stringResource(R.string.achievements_see_all),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.primary
            )
            KateIcon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        ProgressBar(
            fraction = unlocked.toFloat() / states.size,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            preview.forEachIndexed { index, state ->
                PopIn(index) {
                    AchievementBadge(
                        achievement = state.achievement,
                        unlocked = state.unlocked,
                        fraction = state.fraction,
                        size = 48.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun PopIn(order: Int, content: @Composable () -> Unit) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(order * 60L)
        anim.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow))
    }
    Box(
        modifier = Modifier.graphicsLayer {
            val value = anim.value
            alpha = value.coerceIn(0f, 1f)
            scaleX = 0.6f + 0.4f * value
            scaleY = 0.6f + 0.4f * value
        }
    ) {
        content()
    }
}

@Composable
fun AchievementsScreen(
    bottomBarConnection: NestedScrollConnection?,
    onBack: () -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { AchievementCenter.refresh(context) }
    val states = AchievementCenter.states
    val unlocked = states.count { it.unlocked }
    val bottomInset = LocalBottomContentPadding.current
    Box(modifier = Modifier.fillMaxSize().background(colors.background), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomInset + 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .widthIn(max = 960.dp)
                .fillMaxSize()
                .let { if (bottomBarConnection != null) it.nestedScroll(bottomBarConnection) else it }
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    ) {
                        KateIconButton(onClick = onBack) {
                            KateIcon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.library_back),
                                tint = colors.primary
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.achievements_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                    if (states.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.achievements_count, unlocked, states.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.secondaryText,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                        ProgressBar(
                            fraction = unlocked.toFloat() / states.size,
                            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 12.dp)
                        )
                    }
                    if (!ListeningHistory.enabled) {
                        Text(
                            text = stringResource(R.string.achievements_history_off),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.colorError,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 12.dp)
                        )
                    }
                    if (states.isEmpty() && LibraryStore.tracks.isEmpty()) {
                        Text(
                            text = stringResource(R.string.achievements_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.secondaryText,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            itemsIndexed(states, key = { _, state -> state.achievement.key }) { index, state ->
                PopIn(index) { AchievementTile(state) }
            }
        }
    }
}

@Composable
private fun AchievementTile(state: AchievementState) {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
            .border(
                BorderStroke(1.dp, if (state.unlocked) colors.primary.copy(alpha = 0.35f) else colors.divider.copy(alpha = 0.4f)),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AchievementBadge(
                achievement = state.achievement,
                unlocked = state.unlocked,
                fraction = state.fraction,
                size = 68.dp
            )
            if (!state.unlocked) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(colors.cardBackground)
                ) {
                    KateIcon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(R.string.achievements_locked),
                        tint = colors.secondaryText,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
        Text(
            text = stringResource(achievementTitle(state.achievement)),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (state.unlocked) colors.onSurface else colors.onSurface.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = stringResource(achievementDescription(state.achievement)),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (!state.unlocked) {
            ProgressBar(fraction = state.fraction, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        }
        Text(
            text = progressText(state),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (state.unlocked) colors.primary else colors.secondaryText,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = if (state.unlocked) 12.dp else 6.dp)
        )
    }
}

@Composable
fun AchievementToastHost(onOpen: () -> Unit) {
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val colors = LocalClintColors.current
    val version = ListeningHistory.version
    val librarySize = LibraryStore.tracks.size
    val index = LibraryStore.index
    LaunchedEffect(version, librarySize, index) { AchievementCenter.refresh(context) }
    val next = AchievementCenter.pending.firstOrNull()
    var shown by remember { mutableStateOf<Achievement?>(null) }
    var last by remember { mutableStateOf<Achievement?>(null) }
    LaunchedEffect(next) {
        if (next == null) return@LaunchedEffect
        last = next
        shown = next
        haptics.confirm()
        delay(4_000L)
        shown = null
        delay(450L)
        AchievementCenter.consume(next)
    }
    val display = shown ?: last
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = shown != null,
            enter = slideInVertically(spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)) { -it } +
                fadeIn(tween(200)) + scaleIn(initialScale = 0.9f),
            exit = slideOutVertically(tween(300)) { -it } + fadeOut(tween(250))
        ) {
            if (display != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(colors.cardBackground)
                        .border(BorderStroke(1.dp, colors.primary.copy(alpha = 0.45f)), RoundedCornerShape(22.dp))
                        .clickable {
                            shown = null
                            onOpen()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    PopIn(0) { AchievementBadge(achievement = display, unlocked = true, size = 46.dp) }
                    Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(
                            text = stringResource(R.string.achievement_unlocked),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.primary
                        )
                        Text(
                            text = stringResource(achievementTitle(display)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(achievementDescription(display)),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}
