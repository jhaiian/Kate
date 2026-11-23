package com.jhaiian.kate.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.KateMiniControls
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.VisualizerPreferences
import com.jhaiian.kate.ui.theme.MiniProgressPreferences
import com.jhaiian.kate.ui.theme.SeekBarPreferences
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

private val ArtRingOutset = 3.dp
private val MiniArtRingCorner = 12.dp
private val PlayRingRadius = 19.dp
private const val FillAlpha = 0.16f
private const val MiniTwoPi = (2.0 * PI).toFloat()

private fun miniThickness(key: String): Dp = when (key) {
    MiniProgressPreferences.THICKNESS_HAIRLINE -> 1.dp
    MiniProgressPreferences.THICKNESS_THICK -> 3.5.dp
    else -> 2.dp
}

private fun miniWavelength(key: String): Dp = when (key) {
    SeekBarPreferences.SIZE_SMALL -> 10.dp
    SeekBarPreferences.SIZE_LARGE -> 26.dp
    else -> 16.dp
}

private fun miniAmplitude(key: String): Dp = when (key) {
    SeekBarPreferences.SIZE_SMALL -> 1.2.dp
    SeekBarPreferences.SIZE_LARGE -> 2.6.dp
    else -> 1.9.dp
}

private class MiniColors(val start: Color, val end: Color, val inactive: Color) {
    val gradient: Boolean get() = start != end
    fun at(position: Float): Color = if (gradient) lerp(start, end, position.coerceIn(0f, 1f)) else start
    fun brush(width: Float, alpha: Float = 1f): Brush =
        if (gradient) {
            Brush.horizontalGradient(listOf(start.copy(alpha = alpha), end.copy(alpha = alpha)), startX = 0f, endX = width)
        } else {
            SolidColor(start.copy(alpha = alpha))
        }
}

@Composable
internal fun MiniPlayerContent(
    track: Track,
    isPlaying: Boolean,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val settings = LocalLookAndFeel.current
    val style = settings.miniProgressStyle
    var barWidthPx by remember { mutableFloatStateOf(0f) }
    val position = rememberPlaybackPositionMs { if (active) max(barWidthPx, 1f) else 1f }
    val durationMs = PlayerController.durationMs.takeIf { it > 0 } ?: track.durationMs
    val progress: () -> Float = {
        if (durationMs > 0) (position.value.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }
    val gradient = settings.miniProgressColor == SeekBarPreferences.COLOR_GRADIENT
    val endColor = remember(colors.primary, gradient) { if (gradient) shiftedHue(colors.primary) else colors.primary }
    val miniColors = MiniColors(colors.primary, endColor, colors.surfaceVariant)
    val thickness = miniThickness(settings.miniProgressThickness)
    val lineEdge = style == MiniProgressPreferences.STYLE_BOTTOM || style == MiniProgressPreferences.STYLE_TOP
    val look = settings.seekBarLook()
    val lineStyle = if (lineEdge && settings.miniProgressMatchPlayer) look.style else SeekBarPreferences.STYLE_CLASSIC
    val needsWave = lineStyle == SeekBarPreferences.STYLE_WAVEFORM ||
        (lineStyle == SeekBarPreferences.STYLE_SQUIGGLE && look.squiggleReactive)
    val waveform = rememberWaveform(track, enabled = active && needsWave)
    val remainingSeconds by remember(durationMs) {
        derivedStateOf { ((durationMs - position.value).coerceAtLeast(0L)) / 1000L }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { barWidthPx = it.width.toFloat() }
            .then(
                if (style == MiniProgressPreferences.STYLE_FILL) {
                    Modifier.drawBehind { drawFill(progress(), miniColors) }
                } else {
                    Modifier
                }
            )
    ) {
        if (active && VisualizerPreferences.PLACE_MINI in settings.visualizerPlacements) {
            AudioVisualizer(modifier = Modifier.matchParentSize(), alpha = 0.22f)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(MiniPlayerRowHeight)
                .padding(start = MiniArtStart, end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(MiniArtSize)
                    .then(
                        if (style == MiniProgressPreferences.STYLE_ART_RING) {
                            Modifier.artRing(progress, thickness, miniColors)
                        } else {
                            Modifier
                        }
                    )
            )
            AnimatedContent(
                targetState = track,
                contentKey = { it.id },
                transitionSpec = { infoTrackTransition() },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                label = "mini_player_info"
            ) { animatedTrack ->
                Column {
                    Text(
                        text = animatedTrack.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (active) Modifier.basicMarquee() else Modifier
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Text(
                            text = animatedTrack.artist.ifBlank { stringResource(R.string.player_unknown_artist) },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (settings.miniProgressTimeLeft && animatedTrack.id == track.id && durationMs > 0) {
                            Text(
                                text = stringResource(R.string.mini_time_left, formatPlaybackTime(remainingSeconds * 1000L)),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.secondaryText,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            val ringed = style == MiniProgressPreferences.STYLE_PLAY_RING
            KateMiniControls(
                isPlaying = isPlaying,
                ringed = ringed,
                playModifier = if (ringed) Modifier.playRing(progress, thickness, miniColors) else Modifier,
                onPlayPause = {
                    haptics.click()
                    PlayerController.togglePlayPause()
                },
                onNext = {
                    haptics.click()
                    PlayerController.skipNext()
                },
                onPrevious = {
                    haptics.click()
                    PlayerController.skipPrevious()
                }
            )
        }
        if (lineEdge) {
            MiniEdgeLine(
                lineStyle = lineStyle,
                atTop = style == MiniProgressPreferences.STYLE_TOP,
                progress = progress,
                thickness = thickness,
                colors = miniColors,
                playing = isPlaying && active,
                waveform = waveform,
                squiggleHeight = look.squiggleHeight,
                squiggleLength = look.squiggleLength,
                squiggleSpeed = look.squiggleSpeed,
                reactive = look.squiggleReactive,
                modifier = Modifier
                    .align(if (style == MiniProgressPreferences.STYLE_TOP) Alignment.TopStart else Alignment.BottomStart)
                    .fillMaxWidth()
            )
        }
    }
}

private fun DrawScope.drawFill(progress: Float, colors: MiniColors) {
    val width = size.width * progress
    if (width <= 0f) return
    drawRect(brush = colors.brush(size.width, FillAlpha), size = Size(width, size.height))
    val edge = 2.dp.toPx()
    drawRect(
        color = colors.at(progress).copy(alpha = 0.45f),
        topLeft = Offset((width - edge).coerceAtLeast(0f), 0f),
        size = Size(edge.coerceAtMost(width), size.height)
    )
}

private fun Modifier.playRing(progress: () -> Float, thickness: Dp, colors: MiniColors): Modifier = drawBehind {
    val stroke = (thickness + 0.5.dp).toPx()
    val radius = PlayRingRadius.toPx()
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2f, radius * 2f)
    drawCircle(color = colors.inactive, radius = radius, center = center, style = Stroke(width = stroke))
    val sweep = 360f * progress()
    if (sweep > 0f) {
        drawArc(
            brush = if (colors.gradient) Brush.linearGradient(listOf(colors.start, colors.end)) else SolidColor(colors.start),
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

private class RingCache {
    val path = Path()
    val segment = Path()
    val measure = PathMeasure()
    var key = Size.Zero
    var length = 0f
}

@Composable
private fun Modifier.artRing(progress: () -> Float, thickness: Dp, colors: MiniColors): Modifier {
    val cache = remember { RingCache() }
    return drawBehind {
        val stroke = (thickness + 0.5.dp).toPx()
        val outset = ArtRingOutset.toPx()
        val left = -outset
        val top = -outset
        val right = size.width + outset
        val bottom = size.height + outset
        val radius = (MiniArtRingCorner + ArtRingOutset).toPx()
        if (cache.key != size) {
            cache.key = size
            val path = cache.path
            path.reset()
            path.moveTo((left + right) / 2f, top)
            path.lineTo(right - radius, top)
            path.arcTo(Rect(right - radius * 2f, top, right, top + radius * 2f), -90f, 90f, false)
            path.lineTo(right, bottom - radius)
            path.arcTo(Rect(right - radius * 2f, bottom - radius * 2f, right, bottom), 0f, 90f, false)
            path.lineTo(left + radius, bottom)
            path.arcTo(Rect(left, bottom - radius * 2f, left + radius * 2f, bottom), 90f, 90f, false)
            path.lineTo(left, top + radius)
            path.arcTo(Rect(left, top, left + radius * 2f, top + radius * 2f), 180f, 90f, false)
            path.close()
            cache.measure.setPath(path, false)
            cache.length = cache.measure.length
        }
        drawRoundRect(
            color = colors.inactive,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            cornerRadius = CornerRadius(radius, radius),
            style = Stroke(width = stroke)
        )
        val distance = cache.length * progress()
        if (distance > 0f) {
            cache.segment.reset()
            cache.measure.getSegment(0f, distance, cache.segment, true)
            drawPath(
                path = cache.segment,
                brush = if (colors.gradient) Brush.linearGradient(listOf(colors.start, colors.end)) else SolidColor(colors.start),
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
private fun MiniEdgeLine(
    lineStyle: String,
    atTop: Boolean,
    progress: () -> Float,
    thickness: Dp,
    colors: MiniColors,
    playing: Boolean,
    waveform: FloatArray?,
    squiggleHeight: String,
    squiggleLength: String,
    squiggleSpeed: String,
    reactive: Boolean,
    modifier: Modifier = Modifier
) {
    val motionEnabled = rememberMotionEnabled()
    val squiggle = lineStyle == SeekBarPreferences.STYLE_SQUIGGLE
    val amplitudeScale by animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "mini_squiggle_amp"
    )
    val waveReveal by animateFloatAsState(
        targetValue = if (waveform != null) 1f else 0f,
        label = "mini_wave_reveal"
    )
    val phase = remember { mutableFloatStateOf(0f) }
    val cycles = squiggleCyclesPerSecond(squiggleSpeed)
    LaunchedEffect(squiggle, playing, motionEnabled, cycles) {
        if (!squiggle || !playing || !motionEnabled) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val seconds = (now - last) / 1_000_000_000f
                last = now
                phase.floatValue = (phase.floatValue + seconds * cycles * MiniTwoPi) % (MiniTwoPi * 1000f)
            }
        }
    }
    val amplitude = miniAmplitude(squiggleHeight)
    val height = when (lineStyle) {
        SeekBarPreferences.STYLE_SQUIGGLE -> thickness + amplitude * 2 + 1.dp
        SeekBarPreferences.STYLE_WAVEFORM -> 8.dp
        SeekBarPreferences.STYLE_DOTS -> max(thickness.value * 1.8f, 3.dp.value).dp
        else -> thickness
    }
    val path = remember { Path() }
    Canvas(modifier = modifier.height(height)) {
        val width = size.width
        if (width <= 0f) return@Canvas
        val value = progress()
        val playheadX = width * value
        val stroke = thickness.toPx()
        when (lineStyle) {
            SeekBarPreferences.STYLE_SQUIGGLE -> {
                val centerY = size.height / 2f
                val amp = amplitude.toPx() * amplitudeScale
                val wavelength = miniWavelength(squiggleLength).toPx()
                drawLine(colors.inactive, Offset(playheadX, centerY), Offset(width, centerY), strokeWidth = stroke)
                path.reset()
                path.moveTo(0f, centerY)
                var x = 0f
                val step = 2.dp.toPx()
                val wave = if (reactive) waveform else null
                while (x <= playheadX) {
                    val scale = if (wave != null) 0.3f + 0.7f * wave.sampleAt(x / width) * waveReveal + 0.7f * (1f - waveReveal) else 1f
                    path.lineTo(x, centerY + amp * scale * sin(x / wavelength * MiniTwoPi - phase.floatValue))
                    x += step
                }
                drawPath(path = path, brush = colors.brush(width), style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            SeekBarPreferences.STYLE_WAVEFORM -> {
                val barWidth = 2.dp.toPx()
                val gap = 1.dp.toPx()
                val count = ((width + gap) / (barWidth + gap)).toInt().coerceAtLeast(8)
                val step = width / count
                for (index in 0 until count) {
                    val position = (index + 0.5f) / count
                    val target = waveform?.sampleAt(position) ?: 0.25f
                    val level = 0.25f + (target - 0.25f) * waveReveal
                    val barHeight = max(1.dp.toPx(), size.height * level)
                    val top = if (atTop) 0f else size.height - barHeight
                    drawRect(
                        color = if (position <= value) colors.at(position) else colors.inactive,
                        topLeft = Offset(index * step + (step - barWidth) / 2f, top),
                        size = Size(barWidth, barHeight)
                    )
                }
            }
            SeekBarPreferences.STYLE_DOTS -> {
                val radius = max(stroke * 0.6f, 1.2.dp.toPx())
                val spacing = max(radius * 3.2f, 6.dp.toPx())
                val count = (width / spacing).toInt().coerceAtLeast(8)
                val gapWidth = width / count
                for (index in 0 until count) {
                    val position = (index + 0.5f) / count
                    drawCircle(
                        color = if (position <= value) colors.at(position) else colors.inactive,
                        radius = radius,
                        center = Offset(gapWidth * (index + 0.5f), size.height / 2f)
                    )
                }
            }
            SeekBarPreferences.STYLE_PILL -> {
                val corner = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(color = colors.inactive, size = size, cornerRadius = corner)
                if (playheadX > 0f) {
                    drawRoundRect(brush = colors.brush(width), size = Size(max(playheadX, size.height), size.height), cornerRadius = corner)
                }
            }
            else -> {
                drawRect(color = colors.inactive)
                drawRect(brush = colors.brush(width), size = Size(playheadX, size.height))
            }
        }
    }
}
