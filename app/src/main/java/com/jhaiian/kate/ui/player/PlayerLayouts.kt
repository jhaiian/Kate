package com.jhaiian.kate.ui.player

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

internal const val VinylLabelFraction = 0.36f
internal const val CassetteAspect = 1.6f
private const val TonearmRestDegrees = -8f
private const val TonearmStartDegrees = 12f
private const val TonearmEndDegrees = 27f
private const val CassetteReelDegreesPerSecond = 140f

private val VinylBlack = Color(0xFF121214)
private val CassetteLabelCream = Color(0xFFF3EAD7)
private val CassetteInk = Color(0xFF3A3A3A)
private val CassetteWindow = Color(0xFF2A2A2E)
private val CassetteTape = Color(0xFF4A3426)

@Composable
internal fun rememberSpinAngle(running: Boolean, degreesPerSecond: Float): State<Float> {
    val angle = remember { mutableFloatStateOf(0f) }
    val speed = animateFloatAsState(
        targetValue = if (running) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "spin_speed"
    )
    LaunchedEffect(running, degreesPerSecond) {
        if (!running && speed.value <= 0.001f) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (isActive) {
            val now = withFrameNanos { it }
            val seconds = (now - last) / 1_000_000_000f
            last = now
            val factor = speed.value
            if (!running && factor <= 0.001f) break
            angle.floatValue = (angle.floatValue + seconds * degreesPerSecond * factor) % 360f
        }
    }
    return angle
}

private fun playbackFraction(): Float {
    val duration = PlayerController.durationMs
    if (duration <= 0L) return 0f
    return (PlayerController.estimatedPositionMs().toFloat() / duration).coerceIn(0f, 1f)
}

internal class VinylGeometry(val size: Dp, tonearm: Boolean) {
    val diameter: Dp = size * if (tonearm) 0.84f else 0.96f
    val centerX: Dp = if (tonearm) size * 0.46f else size / 2
    val centerY: Dp = size / 2
    val label: Dp = diameter * VinylLabelFraction
}

@Composable
internal fun VinylPanel(
    size: Dp,
    isPlaying: Boolean,
    active: Boolean,
    tonearm: Boolean,
    artSlotModifier: Modifier
) {
    val colors = LocalClintColors.current
    val geometry = remember(size, tonearm) { VinylGeometry(size, tonearm) }
    val armOn = animateFloatAsState(
        targetValue = if (isPlaying && active) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "tonearm"
    )
    val accent = colors.primary
    val armColor = colors.secondaryText
    val baseColor = colors.surfaceVariant
    Box(modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(geometry.centerX.toPx(), geometry.centerY.toPx())
            val radius = geometry.diameter.toPx() / 2f
            drawRecord(center, radius, accent)
            if (tonearm) {
                val angle = TonearmRestDegrees +
                    (TonearmStartDegrees + (TonearmEndDegrees - TonearmStartDegrees) * playbackFraction() - TonearmRestDegrees) * armOn.value
                drawTonearm(this.size.width, angle, armColor, baseColor)
            }
        }
        Spacer(
            modifier = Modifier
                .offset(x = geometry.centerX - geometry.label / 2, y = geometry.centerY - geometry.label / 2)
                .size(geometry.label)
                .then(artSlotModifier)
        )
    }
}

private fun DrawScope.drawRecord(center: Offset, radius: Float, accent: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent),
            center = center + Offset(0f, radius * 0.04f),
            radius = radius * 1.08f
        ),
        radius = radius * 1.08f,
        center = center + Offset(0f, radius * 0.04f)
    )
    drawCircle(color = VinylBlack, radius = radius, center = center)
    val grooves = 30
    val inner = radius * 0.4f
    val outer = radius * 0.97f
    for (index in 0..grooves) {
        val r = inner + (outer - inner) * index / grooves
        drawCircle(
            color = Color.White.copy(alpha = if (index % 3 == 0) 0.06f else 0.025f),
            radius = r,
            center = center,
            style = Stroke(width = 0.6.dp.toPx())
        )
    }
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.10f),
                Color.Transparent,
                Color.Transparent,
                Color.White.copy(alpha = 0.07f),
                Color.Transparent,
                Color.Transparent
            ),
            center = center
        ),
        radius = radius,
        center = center
    )
    drawCircle(color = accent.copy(alpha = 0.7f), radius = radius * (VinylLabelFraction + 0.02f), center = center, style = Stroke(width = 2.dp.toPx()))
    drawCircle(color = Color.White.copy(alpha = 0.12f), radius = radius, center = center, style = Stroke(width = 1.dp.toPx()))
}

private fun DrawScope.drawTonearm(box: Float, degrees: Float, armColor: Color, baseColor: Color) {
    val pivot = Offset(box * 0.88f, box * 0.12f)
    val length = box * 0.62f
    val radians = Math.toRadians(degrees.toDouble())
    val direction = Offset(-sin(radians).toFloat(), cos(radians).toFloat())
    val head = pivot + direction * length
    val stroke = box * 0.022f
    drawCircle(color = baseColor, radius = box * 0.065f, center = pivot)
    drawCircle(color = armColor.copy(alpha = 0.5f), radius = box * 0.065f, center = pivot, style = Stroke(width = 1.5.dp.toPx()))
    drawLine(color = armColor, start = pivot - direction * (box * 0.09f), end = pivot, strokeWidth = stroke * 2.2f, cap = StrokeCap.Round)
    drawLine(color = armColor, start = pivot, end = head, strokeWidth = stroke, cap = StrokeCap.Round)
    val shellEnd = head + direction * (box * 0.05f) + Offset(direction.y, -direction.x) * (box * 0.02f)
    drawLine(color = armColor, start = head, end = shellEnd, strokeWidth = stroke * 2.4f, cap = StrokeCap.Round)
    drawCircle(color = armColor.copy(alpha = 0.9f), radius = box * 0.03f, center = pivot)
}

internal class CassetteGeometry(val width: Dp) {
    val height: Dp = width / CassetteAspect
    val artX: Dp = width * 0.07f
    val artY: Dp = height * 0.11f
    val art: Dp = height * 0.42f
    val textX: Dp = width * 0.36f
    val textY: Dp = height * 0.11f
    val textWidth: Dp = width * 0.56f
}

@Composable
internal fun CassettePanel(
    width: Dp,
    track: Track,
    isPlaying: Boolean,
    active: Boolean,
    tapeColor: String,
    artSlotModifier: Modifier
) {
    val colors = LocalClintColors.current
    val density = LocalDensity.current
    val geometry = remember(width) { CassetteGeometry(width) }
    val spin = rememberSpinAngle(running = isPlaying && active, degreesPerSecond = CassetteReelDegreesPerSecond)
    val accent = colors.primary
    val body = when (tapeColor) {
        PlayerStylePreferences.CASSETTE_WHITE -> Color(0xFFECECEE)
        PlayerStylePreferences.CASSETTE_CLEAR -> colors.onSurface.copy(alpha = 0.10f)
        else -> Color(0xFF1F1F22)
    }
    val edge = when (tapeColor) {
        PlayerStylePreferences.CASSETTE_WHITE -> Color(0xFFCFCFD4)
        PlayerStylePreferences.CASSETTE_CLEAR -> colors.onSurface.copy(alpha = 0.30f)
        else -> Color(0xFF34343A)
    }
    val label = if (tapeColor == PlayerStylePreferences.CASSETTE_WHITE) Color.White else CassetteLabelCream
    val titleSize = with(density) { (width * 0.05f).toSp() }
    val sideSize = with(density) { (width * 0.035f).toSp() }
    Box(modifier = Modifier.size(width, geometry.height)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCassette(body, edge, label, accent, spin.value, playbackFraction())
        }
        Spacer(
            modifier = Modifier
                .offset(x = geometry.artX, y = geometry.artY)
                .size(geometry.art)
                .then(artSlotModifier)
        )
        Column(
            modifier = Modifier
                .offset(x = geometry.textX, y = geometry.textY)
                .width(geometry.textWidth)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.album.ifBlank { track.title },
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontSize = titleSize,
                    color = CassetteInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.player_cassette_side),
                    fontWeight = FontWeight.Black,
                    fontSize = sideSize,
                    color = accent,
                    textAlign = TextAlign.End
                )
            }
            Text(
                text = track.artist.ifBlank { stringResource(R.string.player_unknown_artist) },
                fontFamily = FontFamily.Cursive,
                fontSize = sideSize,
                color = CassetteInk.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun DrawScope.drawCassette(body: Color, edge: Color, label: Color, accent: Color, angle: Float, progress: Float) {
    val w = size.width
    val h = size.height
    val corner = CornerRadius(w * 0.045f)
    drawRoundRect(color = body, cornerRadius = corner)
    drawRoundRect(color = edge, cornerRadius = corner, style = Stroke(width = 1.5.dp.toPx()))
    listOf(Offset(w * 0.035f, h * 0.06f), Offset(w * 0.965f, h * 0.06f), Offset(w * 0.035f, h * 0.94f), Offset(w * 0.965f, h * 0.94f))
        .forEach { screw ->
            drawCircle(color = edge, radius = w * 0.013f, center = screw)
            drawLine(
                color = body,
                start = screw - Offset(w * 0.008f, 0f),
                end = screw + Offset(w * 0.008f, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    val labelTop = h * 0.07f
    val labelBottom = h * 0.62f
    drawRoundRect(
        color = label,
        topLeft = Offset(w * 0.05f, labelTop),
        size = Size(w * 0.90f, labelBottom - labelTop),
        cornerRadius = CornerRadius(w * 0.02f)
    )
    drawRect(color = accent, topLeft = Offset(w * 0.05f, h * 0.575f), size = Size(w * 0.90f, h * 0.025f))
    for (line in 0..1) {
        val y = h * (0.255f + line * 0.05f)
        drawLine(
            color = CassetteInk.copy(alpha = 0.15f),
            start = Offset(w * 0.36f, y + h * 0.04f),
            end = Offset(w * 0.92f, y + h * 0.04f),
            strokeWidth = 1.dp.toPx()
        )
    }
    val windowLeft = w * 0.38f
    val windowTop = h * 0.36f
    val windowWidth = w * 0.52f
    val windowHeight = h * 0.19f
    drawRoundRect(
        color = CassetteWindow,
        topLeft = Offset(windowLeft, windowTop),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(w * 0.02f)
    )
    val reelY = windowTop + windowHeight / 2f
    val leftReel = Offset(w * 0.49f, reelY)
    val rightReel = Offset(w * 0.79f, reelY)
    val minTape = w * 0.045f
    val maxTape = w * 0.11f
    clipRect(left = windowLeft, top = windowTop, right = windowLeft + windowWidth, bottom = windowTop + windowHeight) {
        drawCircle(color = CassetteTape, radius = minTape + (maxTape - minTape) * (1f - progress), center = leftReel)
        drawCircle(color = CassetteTape, radius = minTape + (maxTape - minTape) * progress, center = rightReel)
    }
    listOf(leftReel, rightReel).forEach { reel ->
        val hub = w * 0.032f
        drawCircle(color = Color(0xFFEDEDED), radius = hub, center = reel)
        rotate(degrees = angle, pivot = reel) {
            for (tooth in 0 until 6) {
                val radians = Math.toRadians((tooth * 60).toDouble())
                val direction = Offset(cos(radians).toFloat(), sin(radians).toFloat())
                drawLine(
                    color = Color(0xFF9A9AA0),
                    start = reel + direction * (hub * 0.35f),
                    end = reel + direction * (hub * 0.85f),
                    strokeWidth = hub * 0.22f,
                    cap = StrokeCap.Round
                )
            }
        }
        drawCircle(color = Color(0xFF5A5A60), radius = hub * 0.3f, center = reel)
    }
    val bottom = Path().apply {
        moveTo(w * 0.24f, h)
        lineTo(w * 0.29f, h * 0.79f)
        lineTo(w * 0.71f, h * 0.79f)
        lineTo(w * 0.76f, h)
        close()
    }
    drawPath(path = bottom, color = edge.copy(alpha = 0.7f))
    listOf(w * 0.36f, w * 0.64f).forEach { x ->
        drawCircle(color = body, radius = w * 0.018f, center = Offset(x, h * 0.9f))
    }
    drawRect(color = body, topLeft = Offset(w * 0.47f, h * 0.87f), size = Size(w * 0.06f, h * 0.06f))
}
