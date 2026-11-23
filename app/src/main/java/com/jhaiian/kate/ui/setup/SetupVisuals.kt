package com.jhaiian.kate.ui.setup

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private const val TAU = (2.0 * PI).toFloat()

private object KateMark {
    val Disc = Color(0xFF0F0A16)
    val Groove = Color(0xFF3B2B57)
    val GrooveShine = Color(0xFF6B54A0)
    val Label = Color(0xFF8B5CF6)
    val LabelRing = Color(0xFFCDB0F5)
}

@Composable
internal fun rememberLoopClock(periodMillis: Int, label: String): State<Float> {
    val transition = rememberInfiniteTransition(label = label)
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing)),
        label = label
    )
}

private fun DrawScope.drawKateMark(
    center: Offset,
    radius: Float,
    disc: Float,
    grooves: List<Float>,
    label: Float,
    ring: Float,
    hub: Float,
    shine: Float
) {
    val unit = radius / 30f
    if (disc > 0f) {
        drawCircle(KateMark.Disc, radius * disc, center)
    }
    val grooveStroke = Stroke(width = (0.8f * unit).coerceAtLeast(1f), cap = StrokeCap.Round)
    listOf(27f, 24f, 21f).forEachIndexed { i, r ->
        val progress = grooves.getOrElse(i) { 0f }
        if (progress > 0f) {
            val gr = r * unit
            drawArc(
                color = KateMark.Groove,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(center.x - gr, center.y - gr),
                size = Size(gr * 2f, gr * 2f),
                style = grooveStroke
            )
            if (progress >= 1f) {
                val start = shine * 360f * (1f + i * 0.35f) - 90f + i * 120f
                drawArc(
                    color = KateMark.GrooveShine,
                    startAngle = start,
                    sweepAngle = 38f - i * 6f,
                    useCenter = false,
                    topLeft = Offset(center.x - gr, center.y - gr),
                    size = Size(gr * 2f, gr * 2f),
                    style = Stroke(width = (1.2f * unit).coerceAtLeast(1.5f), cap = StrokeCap.Round)
                )
            }
        }
    }
    if (label > 0f) drawCircle(KateMark.Label, 17f * unit * label, center)
    if (ring > 0f) drawCircle(KateMark.LabelRing, 11f * unit * ring, center)
    if (hub > 0f) {
        drawCircle(KateMark.Label, 5f * unit * hub, center)
        drawCircle(KateMark.Disc, 1.6f * unit * hub, center)
    }
}

@Composable
internal fun VinylHero(modifier: Modifier = Modifier) {
    val disc = remember { Animatable(0f) }
    val grooves = remember { List(3) { Animatable(0f) } }
    val label = remember { Animatable(0f) }
    val ring = remember { Animatable(0f) }
    val hub = remember { Animatable(0f) }
    val shine = rememberLoopClock(6000, "kate_mark_shine")
    val float = rememberLoopClock(5000, "kate_mark_float")
    LaunchedEffect(Unit) {
        launch { disc.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 180f)) }
        grooves.forEachIndexed { i, groove ->
            launch {
                delay(180L + i * 110L)
                groove.animateTo(1f, tween(620, easing = FastOutSlowInEasing))
            }
        }
        delay(520)
        launch { label.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 260f)) }
        delay(110)
        launch { ring.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 300f)) }
        delay(110)
        hub.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 340f))
    }
    Canvas(modifier) {
        val radius = size.minDimension * 0.34f
        val bob = sin(float.value * TAU) * radius * 0.03f
        drawKateMark(
            center = Offset(center.x, center.y + bob),
            radius = radius,
            disc = disc.value,
            grooves = grooves.map { it.value },
            label = label.value,
            ring = ring.value,
            hub = hub.value,
            shine = shine.value
        )
    }
}

@Composable
internal fun KateMarkSmall(modifier: Modifier = Modifier) {
    val shine = rememberLoopClock(6000, "kate_mark_small")
    Canvas(modifier) {
        drawKateMark(
            center = center,
            radius = size.minDimension / 2f,
            disc = 1f,
            grooves = listOf(1f, 1f, 1f),
            label = 1f,
            ring = 1f,
            hub = 1f,
            shine = shine.value
        )
    }
}

@Composable
internal fun EqualizerBars(modifier: Modifier = Modifier, bars: Int = 28, color: Color = LocalClintColors.current.primary) {
    val clock = rememberLoopClock(4200, "equalizer")
    Canvas(modifier) {
        val time = clock.value
        val slot = size.width / bars
        val barWidth = slot * 0.5f
        val centerY = size.height / 2f
        for (i in 0 until bars) {
            val a = 0.5f + 0.5f * sin(TAU * time * (1 + i % 3) + i * 0.7f)
            val b = 0.5f + 0.5f * sin(TAU * time * (2 + (i * 7) % 4) + i * 1.3f)
            val envelope = 0.45f + 0.55f * sin(PI.toFloat() * (i + 0.5f) / bars)
            val fraction = (0.12f + 0.88f * (a * 0.6f + b * 0.4f)) * envelope
            val barHeight = (size.height * fraction).coerceAtLeast(barWidth)
            val x = i * slot + (slot - barWidth) / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(x, centerY - barHeight / 2f),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
internal fun FloatingNotesHero(icon: ImageVector, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val clock = rememberLoopClock(6400, "notes")
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 180f)) }
    val notes = listOf(Icons.Outlined.MusicNote, Icons.Outlined.Album, Icons.AutoMirrored.Outlined.QueueMusic, Icons.Outlined.MusicNote, Icons.Outlined.GraphicEq)
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val tile = (minOf(maxWidth, maxHeight) * 0.34f).coerceIn(72.dp, 150.dp)
        notes.forEachIndexed { index, vector ->
            val angle = -PI.toFloat() / 2f + (index - (notes.size - 1) / 2f) * 0.45f
            Icon(
                imageVector = vector,
                contentDescription = null,
                tint = if (index % 2 == 0) colors.primary else colors.secondaryText,
                modifier = Modifier
                    .size(tile * 0.28f)
                    .graphicsLayer {
                        val p = (clock.value + index.toFloat() / notes.size) % 1f
                        val distance = tile.toPx() * (0.5f + 1.1f * p)
                        translationX = cos(angle) * distance
                        translationY = sin(angle) * distance
                        alpha = sin(p * PI.toFloat()).coerceAtLeast(0f).pow(0.8f) * pop.value.coerceIn(0f, 1f)
                        scaleX = 0.7f + 0.4f * p
                        scaleY = 0.7f + 0.4f * p
                    }
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(tile)
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                }
                .clip(RoundedCornerShape(tile * 0.3f))
                .background(colors.primary)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.onPrimary,
                modifier = Modifier.size(tile * 0.48f)
            )
        }
    }
}

@Composable
internal fun ThemePreviewHero(modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val progress = rememberLoopClock(9000, "preview_progress")
    val spin = rememberLoopClock(9000, "preview_spin")
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 160f)) }
    val primary = colors.primary
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val cardWidth = (minOf(maxWidth * 0.78f, maxHeight * 0.62f)).coerceIn(180.dp, 300.dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(cardWidth)
                .graphicsLayer {
                    val v = pop.value
                    scaleX = 0.9f + 0.1f * v
                    scaleY = 0.9f + 0.1f * v
                    alpha = v.coerceIn(0f, 1f)
                }
                .clip(RoundedCornerShape(28.dp))
                .background(colors.cardBackground)
                .padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.35f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(primary.copy(alpha = 0.18f))
            ) {
                KateMarkSmall(
                    Modifier
                        .fillMaxSize(0.62f)
                        .graphicsLayer { rotationZ = spin.value * 360f }
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.setup_preview_song),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.setup_preview_artist),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.divider)
                    .drawBehind {
                        val width = size.width * (0.08f + 0.9f * progress.value)
                        drawRoundRect(primary, size = Size(width, size.height), cornerRadius = CornerRadius(size.height / 2f))
                    }
            )
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Icon(Icons.Outlined.SkipPrevious, null, tint = colors.onSurface, modifier = Modifier.size(26.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(primary)
                ) {
                    Icon(Icons.Outlined.Pause, null, tint = colors.onPrimary, modifier = Modifier.size(28.dp))
                }
                Icon(Icons.Outlined.SkipNext, null, tint = colors.onSurface, modifier = Modifier.size(26.dp))
            }
        }
    }
}

@Composable
internal fun NotificationHero(modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val progress = rememberLoopClock(7000, "notif_progress")
    val drop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(150)
        drop.animateTo(1f, spring(dampingRatio = 0.65f, stiffness = 140f))
    }
    val primary = colors.primary
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val cardWidth = (maxWidth * 0.86f).coerceIn(220.dp, 360.dp)
        Column(
            modifier = Modifier
                .widthIn(max = cardWidth)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    val v = drop.value
                    translationY = (1f - v) * -size.height
                    alpha = v.coerceIn(0f, 1f)
                }
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KateMarkSmall(Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.secondaryText
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.setup_preview_now_playing),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primary.copy(alpha = 0.18f))
                ) {
                    KateMarkSmall(Modifier.size(32.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.setup_preview_song),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.setup_preview_artist),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.Outlined.SkipPrevious, null, tint = colors.onSurface, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(6.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(primary)
                ) {
                    Icon(Icons.Outlined.Pause, null, tint = colors.onPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.SkipNext, null, tint = colors.onSurface, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(colors.divider)
                    .drawBehind {
                        drawRoundRect(primary, size = Size(size.width * progress.value, size.height), cornerRadius = CornerRadius(size.height / 2f))
                    }
            )
        }
    }
}

private class Confetti(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val spin: Float,
    val tone: Int,
    val round: Boolean
)

@Composable
internal fun CelebrationHero(modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val badge = remember { Animatable(0f) }
    val check = remember { Animatable(0f) }
    val burst = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { badge.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 200f)) }
        launch { burst.animateTo(1f, tween(2400, easing = LinearOutSlowInEasing)) }
        delay(240)
        check.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
    }
    val pieces = remember {
        List(60) { i ->
            val random = Random(i * 17 + 3)
            Confetti(
                angle = random.nextFloat() * TAU,
                speed = 0.35f + random.nextFloat() * 0.7f,
                size = 3f + random.nextFloat() * 4f,
                spin = (random.nextFloat() - 0.5f) * 4f,
                tone = random.nextInt(3),
                round = random.nextBoolean()
            )
        }
    }
    val primary = colors.primary
    val onPrimary = colors.onPrimary
    val tones = listOf(primary, KateMark.Label, colors.secondaryText)
    Canvas(modifier) {
        val s = size.minDimension
        val c = center
        val r = s * 0.2f
        val b = burst.value
        val dp = 1.dp.toPx()
        pieces.forEach { piece ->
            val distance = piece.speed * b * s * 0.6f
            val x = c.x + cos(piece.angle) * distance
            val y = c.y + sin(piece.angle) * distance + b * b * s * 0.25f
            val alpha = if (b > 0.01f) (1f - b.pow(1.6f)).coerceIn(0f, 1f) else 0f
            if (alpha > 0f) {
                val tint = tones[piece.tone].copy(alpha = alpha)
                rotate(piece.spin * b * 540f, Offset(x, y)) {
                    if (piece.round) {
                        drawCircle(tint, piece.size * dp * 0.6f, Offset(x, y))
                    } else {
                        drawRect(
                            tint,
                            topLeft = Offset(x - piece.size * dp / 2f, y - piece.size * dp * 0.3f),
                            size = Size(piece.size * dp, piece.size * dp * 0.6f)
                        )
                    }
                }
            }
        }
        val badgeRadius = r * badge.value
        if (badgeRadius > 0f) {
            drawCircle(primary, badgeRadius, c)
        }
        if (check.value > 0f) {
            scale(badge.value.coerceIn(0f, 1.2f), c) {
                val path = Path().apply {
                    moveTo(c.x - r * 0.42f, c.y + r * 0.02f)
                    lineTo(c.x - r * 0.12f, c.y + r * 0.32f)
                    lineTo(c.x + r * 0.46f, c.y - r * 0.3f)
                }
                val measure = PathMeasure()
                measure.setPath(path, false)
                val segment = Path()
                measure.getSegment(0f, measure.length * check.value, segment, true)
                drawPath(segment, onPrimary, style = Stroke(r * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}
