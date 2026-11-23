package com.jhaiian.kate.ui.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.StatsPeriod
import com.jhaiian.kate.data.StatsRange
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun ActivityChart(
    period: StatsPeriod,
    buckets: List<Long>,
    modifier: Modifier = Modifier,
    barColor: Color = LocalClintColors.current.primary,
    trackColor: Color = LocalClintColors.current.surfaceVariant,
    labelColor: Color = LocalClintColors.current.secondaryText,
    detailColor: Color = LocalClintColors.current.onSurface
) {
    val context = LocalContext.current
    val max = (buckets.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val defaultSelection = buckets.indices.maxByOrNull { buckets[it] } ?: 0
    var selected by remember(period, buckets) { mutableStateOf(defaultSelection) }
    val grow = remember(period, buckets) { Animatable(0f) }
    LaunchedEffect(grow) { grow.animateTo(1f, tween(durationMillis = 1100, easing = FastOutSlowInEasing)) }
    val count = buckets.size.coerceAtLeast(1)

    Column(modifier = modifier) {
        val detail = buckets.getOrNull(selected)?.let { value ->
            stringResource(R.string.stats_bucket_detail, bucketLongLabel(context, period, selected), durationText(value))
        }.orEmpty()
        Text(
            text = detail,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = detailColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(top = 12.dp)
                .pointerInput(period, buckets) {
                    detectTapGestures { offset ->
                        selected = ((offset.x / size.width) * count).toInt().coerceIn(0, count - 1)
                    }
                }
                .pointerInput(period, buckets) {
                    detectHorizontalDragGestures { change, _ ->
                        selected = ((change.position.x / size.width) * count).toInt().coerceIn(0, count - 1)
                    }
                }
        ) {
            val slot = size.width / count
            val gap = (slot * if (count > 14) 0.28f else 0.36f).coerceAtLeast(2f)
            val barWidth = (slot - gap).coerceAtLeast(2f)
            val radius = CornerRadius(min(barWidth / 2f, 10.dp.toPx()))
            buckets.forEachIndexed { index, value ->
                val left = index * slot + gap / 2f
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = radius
                )
                val fraction = ((value.toFloat() / max) * staggered(grow.value, index, count)).coerceIn(0f, 1f)
                if (value > 0L) {
                    val height = (size.height * fraction).coerceAtLeast(barWidth.coerceAtMost(6.dp.toPx()))
                    drawRoundRect(
                        color = if (index == selected) barColor else barColor.copy(alpha = 0.45f),
                        topLeft = Offset(left, size.height - height),
                        size = Size(barWidth, height),
                        cornerRadius = radius
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            val step = when {
                period.range == StatsRange.MONTH -> 5
                buckets.size > 12 -> 3
                else -> 1
            }
            buckets.indices.forEach { index ->
                val show = index % step == 0 || index == selected
                Text(
                    text = if (show) bucketShortLabel(period, index) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == selected) barColor else labelColor,
                    fontWeight = if (index == selected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun ListeningClock(
    hours: List<Long>,
    modifier: Modifier = Modifier,
    barColor: Color = LocalClintColors.current.primary,
    trackColor: Color = LocalClintColors.current.surfaceVariant,
    labelColor: Color = LocalClintColors.current.secondaryText,
    detailColor: Color = LocalClintColors.current.onSurface,
    interactive: Boolean = true
) {
    val context = LocalContext.current
    val max = (hours.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val peak = hours.indices.maxByOrNull { hours[it] } ?: 0
    var selected by remember(hours) { mutableStateOf(peak) }
    val grow = remember(hours) { Animatable(0f) }
    LaunchedEffect(grow) { grow.animateTo(1f, tween(durationMillis = 1300, easing = FastOutSlowInEasing)) }
    val pointer by animateFloatAsState(
        targetValue = selected.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "clock_pointer"
    )

    Box(modifier = modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (interactive) {
                        Modifier.pointerInput(hours) {
                            detectTapGestures { offset ->
                                val dx = offset.x - size.width / 2f
                                val dy = offset.y - size.height / 2f
                                val angle = (atan2(dy, dx) * 180f / PI.toFloat() + 90f + 360f) % 360f
                                selected = ((angle + 7.5f) / 15f).toInt() % 24
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outer = size.minDimension / 2f
            val inner = outer * 0.46f
            val span = outer - inner
            val stroke = (2f * PI.toFloat() * inner / 24f) * 0.62f
            for (hour in 0 until 24) {
                val angle = (hour * 15f - 90f) * PI.toFloat() / 180f
                val direction = Offset(cos(angle), sin(angle))
                val start = center + direction * inner
                drawLine(
                    color = trackColor,
                    start = start,
                    end = center + direction * outer,
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                val value = hours.getOrElse(hour) { 0L }
                if (value > 0L) {
                    val length = span * ((value.toFloat() / max) * staggered(grow.value, hour, 24)).coerceIn(0f, 1f)
                    drawLine(
                        color = if (hour == selected) barColor else barColor.copy(alpha = 0.5f),
                        start = start,
                        end = start + direction * length.coerceAtLeast(1f),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }
            val pointerAngle = (pointer * 15f - 90f) * PI.toFloat() / 180f
            drawCircle(
                color = barColor,
                radius = stroke * 0.45f,
                center = center + Offset(cos(pointerAngle), sin(pointerAngle)) * (inner - stroke * 1.2f)
            )
            drawCircle(color = trackColor, radius = inner - stroke * 2.2f, center = center, style = Stroke(width = 1.dp.toPx()))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                text = hourLabel(context, selected),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = detailColor,
                maxLines = 1
            )
            Text(
                text = durationText(hours.getOrElse(selected) { 0L }),
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
                maxLines = 1
            )
        }
    }
}

private const val StaggerSpan = 0.45f

private fun staggered(progress: Float, index: Int, count: Int): Float {
    if (count <= 1) return progress
    val start = (index.toFloat() / (count - 1)) * (1f - StaggerSpan)
    val local = ((progress - start) / StaggerSpan).coerceIn(0f, 1f)
    val overshoot = 1.70158f
    val t = local - 1f
    return 1f + t * t * ((overshoot + 1f) * t + overshoot)
}

@Composable
internal fun GenreBar(
    shares: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    trackColor: Color = LocalClintColors.current.surfaceVariant
) {
    val grow = remember(shares) { Animatable(0f) }
    LaunchedEffect(grow) { grow.animateTo(1f, tween(durationMillis = 800, easing = FastOutSlowInEasing)) }
    Box(modifier = modifier.height(14.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = CornerRadius(size.height / 2f)
            drawRoundRect(color = trackColor, cornerRadius = radius)
            var left = 0f
            val gap = 2.dp.toPx()
            val reveal = size.width * grow.value
            shares.forEachIndexed { index, share ->
                val full = size.width * share
                val width = (reveal - left).coerceIn(0f, full)
                if (width > gap) {
                    drawRoundRect(
                        color = colors[index % colors.size],
                        topLeft = Offset(left, 0f),
                        size = Size(width - gap, size.height),
                        cornerRadius = radius
                    )
                }
                left += full
            }
        }
    }
}
