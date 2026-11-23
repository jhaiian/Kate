package com.jhaiian.kate.ui.player

import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jhaiian.kate.R
import com.jhaiian.kate.data.Lyrics
import com.jhaiian.kate.data.LyricsRepository
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.data.WaveformStore
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.SeekBarPreferences
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

private const val ScrubHapticSteps = 20
private const val SectionGapMs = 6_000L
private const val GradientHueShift = 48f
private const val TwoPi = (2.0 * PI).toFloat()

@Immutable
internal data class SeekBarLook(
    val style: String,
    val thumb: String,
    val thickness: String,
    val color: String,
    val bubble: Boolean,
    val lyricMarkers: Boolean,
    val waveShape: String,
    val waveDensity: String,
    val squiggleHeight: String,
    val squiggleLength: String,
    val squiggleSpeed: String,
    val squiggleReactive: Boolean
) {
    val needsWaveform: Boolean
        get() = style == SeekBarPreferences.STYLE_WAVEFORM ||
            (style == SeekBarPreferences.STYLE_SQUIGGLE && squiggleReactive)

    val trackHeight: Dp
        get() = when (style) {
            SeekBarPreferences.STYLE_WAVEFORM -> 44.dp
            SeekBarPreferences.STYLE_SQUIGGLE, SeekBarPreferences.STYLE_PILL -> 32.dp
            else -> 28.dp
        }
}

internal fun LookAndFeelSettings.seekBarLook(): SeekBarLook = SeekBarLook(
    style = seekStyle,
    thumb = seekThumb,
    thickness = seekThickness,
    color = seekColor,
    bubble = seekBubble,
    lyricMarkers = seekLyricMarkers,
    waveShape = seekWaveShape,
    waveDensity = seekWaveDensity,
    squiggleHeight = seekSquiggleHeight,
    squiggleLength = seekSquiggleLength,
    squiggleSpeed = seekSquiggleSpeed,
    squiggleReactive = seekSquiggleReactive
)

private fun thicknessFor(size: String): Dp = when (size) {
    SeekBarPreferences.SIZE_SMALL -> 3.dp
    SeekBarPreferences.SIZE_LARGE -> 8.dp
    else -> 5.dp
}

private fun squiggleAmplitude(size: String): Dp = when (size) {
    SeekBarPreferences.SIZE_SMALL -> 2.5.dp
    SeekBarPreferences.SIZE_LARGE -> 6.5.dp
    else -> 4.dp
}

private fun squiggleWavelength(size: String): Dp = when (size) {
    SeekBarPreferences.SIZE_SMALL -> 16.dp
    SeekBarPreferences.SIZE_LARGE -> 44.dp
    else -> 26.dp
}

internal fun squiggleCyclesPerSecond(size: String): Float = when (size) {
    SeekBarPreferences.SIZE_SMALL -> 0.45f
    SeekBarPreferences.SIZE_LARGE -> 1.4f
    else -> 0.8f
}

internal fun shiftedHue(color: Color): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), hsv)
    hsv[0] = (hsv[0] + GradientHueShift) % 360f
    hsv[1] = (hsv[1] * 1.05f).coerceAtMost(1f)
    return Color(android.graphics.Color.HSVToColor((color.alpha * 255).roundToInt(), hsv))
}

internal fun FloatArray.sampleAt(position: Float): Float {
    if (isEmpty()) return 0f
    val exact = position.coerceIn(0f, 1f) * (size - 1)
    val low = floor(exact).toInt().coerceIn(0, size - 1)
    val high = (low + 1).coerceAtMost(size - 1)
    val t = exact - low
    return this[low] + (this[high] - this[low]) * t
}

internal fun formatPlaybackTime(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}

@Composable
internal fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f) > 0f
    }
}

@Composable
internal fun rememberWaveform(track: Track, enabled: Boolean): FloatArray? {
    val context = LocalContext.current
    val state = produceState(initialValue = if (enabled) WaveformStore.cached(track.id) else null, track.id, enabled) {
        if (!enabled) {
            value = null
            return@produceState
        }
        value = WaveformStore.cached(track.id)
        if (value == null) value = WaveformStore.load(context, track)
    }
    return state.value
}

private fun sectionStarts(lyrics: Lyrics, durationMs: Long): List<Float> {
    if (!lyrics.synced || durationMs <= 0L) return emptyList()
    val result = ArrayList<Float>()
    lyrics.lines.forEachIndexed { index, line ->
        if (line.text.isBlank() || line.timeMs < 0L || line.timeMs >= durationMs) return@forEachIndexed
        val previous = lyrics.lines.getOrNull(index - 1)
        val startsSection = previous == null || previous.text.isBlank() || line.timeMs - previous.timeMs >= SectionGapMs
        if (startsSection) result.add(line.timeMs.toFloat() / durationMs)
    }
    return result
}

@Composable
private fun rememberLyricMarkers(track: Track, durationMs: Long, enabled: Boolean): List<Float> {
    val context = LocalContext.current
    val state = produceState(initialValue = emptyList<Float>(), track.id, durationMs, enabled) {
        if (!enabled || durationMs <= 0L) {
            value = emptyList()
            return@produceState
        }
        value = LyricsRepository.load(context, track)?.let { sectionStarts(it, durationMs) }.orEmpty()
    }
    return state.value
}

@Composable
internal fun SeekBarTrack(
    look: SeekBarLook,
    fraction: () -> Float,
    playing: Boolean,
    scrubbing: Boolean,
    waveform: FloatArray?,
    markers: List<Float>,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val motionEnabled = rememberMotionEnabled()
    val activeColor = colors.primary
    val endColor = remember(activeColor, look.color) {
        if (look.color == SeekBarPreferences.COLOR_GRADIENT) shiftedHue(activeColor) else activeColor
    }
    val inactiveColor = colors.surfaceVariant
    val waveInactive = colors.onSurface.copy(alpha = 0.22f)
    val markerColor = colors.onSurface.copy(alpha = 0.45f)
    val waveReveal by animateFloatAsState(
        targetValue = if (waveform != null) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "seek_wave_reveal"
    )
    val squiggleAmp by animateFloatAsState(
        targetValue = when {
            !playing -> 0f
            scrubbing -> 0.35f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow),
        label = "seek_squiggle_amp"
    )
    val pillExpand by animateFloatAsState(
        targetValue = if (scrubbing) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "seek_pill_expand"
    )
    val thumbScale by animateFloatAsState(
        targetValue = if (scrubbing) 1.5f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
        label = "seek_thumb_scale"
    )
    val thumbAlpha by animateFloatAsState(
        targetValue = if (look.thumb != SeekBarPreferences.THUMB_HIDDEN || scrubbing) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "seek_thumb_alpha"
    )
    val phase = remember { mutableFloatStateOf(0f) }
    val squiggle = look.style == SeekBarPreferences.STYLE_SQUIGGLE
    val cyclesPerSecond = squiggleCyclesPerSecond(look.squiggleSpeed)
    LaunchedEffect(squiggle, playing, motionEnabled, cyclesPerSecond) {
        if (!squiggle || !playing || !motionEnabled) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val seconds = (now - last) / 1_000_000_000f
                last = now
                phase.floatValue = (phase.floatValue + seconds * cyclesPerSecond * TwoPi) % (TwoPi * 1000f)
            }
        }
    }
    val path = remember { Path() }
    Canvas(modifier = modifier.fillMaxWidth().height(look.trackHeight)) {
        val width = size.width
        if (width <= 0f) return@Canvas
        val progress = fraction().coerceIn(0f, 1f)
        val playheadX = width * progress
        val thickness = thicknessFor(look.thickness).toPx()
        val centerY = size.height / 2f
        val activeBrush = if (endColor == activeColor) SolidColor(activeColor) else Brush.horizontalGradient(listOf(activeColor, endColor), startX = 0f, endX = width)
        fun colorAt(position: Float): Color = if (endColor == activeColor) activeColor else lerp(activeColor, endColor, position)
        when (look.style) {
            SeekBarPreferences.STYLE_WAVEFORM -> drawWaveform(
                look = look,
                waveform = waveform,
                reveal = waveReveal,
                progress = progress,
                inactive = waveInactive,
                colorAt = ::colorAt
            )
            SeekBarPreferences.STYLE_SQUIGGLE -> {
                val amplitude = squiggleAmplitude(look.squiggleHeight).toPx() * squiggleAmp
                val wavelength = squiggleWavelength(look.squiggleLength).toPx()
                drawLine(
                    color = inactiveColor,
                    start = Offset(playheadX, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = thickness * 0.85f,
                    cap = StrokeCap.Round
                )
                path.reset()
                path.moveTo(0f, centerY)
                var x = 0f
                val step = 2.dp.toPx()
                val reactive = look.squiggleReactive && waveform != null
                while (x <= playheadX) {
                    val scale = if (reactive) 0.3f + 0.7f * waveform.sampleAt(x / width) * waveReveal + 0.7f * (1f - waveReveal) else 1f
                    val y = centerY + amplitude * scale * sin(x / wavelength * TwoPi - phase.floatValue)
                    path.lineTo(x, y)
                    x += step
                }
                val endScale = if (reactive) 0.3f + 0.7f * waveform.sampleAt(progress) * waveReveal + 0.7f * (1f - waveReveal) else 1f
                path.lineTo(playheadX, centerY + amplitude * endScale * sin(playheadX / wavelength * TwoPi - phase.floatValue))
                drawPath(
                    path = path,
                    brush = activeBrush,
                    style = Stroke(width = thickness, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            SeekBarPreferences.STYLE_PILL -> {
                val height = thickness * 1.9f + pillExpand * thickness * 1.4f
                val top = centerY - height / 2f
                val corner = CornerRadius(height / 2f, height / 2f)
                drawRoundRect(color = inactiveColor, topLeft = Offset(0f, top), size = Size(width, height), cornerRadius = corner)
                clipRect(right = playheadX) {
                    drawRoundRect(brush = activeBrush, topLeft = Offset(0f, top), size = Size(width, height), cornerRadius = corner)
                }
            }
            SeekBarPreferences.STYLE_DOTS -> {
                val spacing = max(thickness * 2.6f, 9.dp.toPx())
                val count = (width / spacing).toInt().coerceAtLeast(8)
                val gap = width / count
                val radius = max(thickness * 0.55f, 2.dp.toPx())
                val activeIndex = (progress * count).toInt().coerceAtMost(count - 1)
                for (index in 0 until count) {
                    val position = (index + 0.5f) / count
                    val center = Offset(gap * (index + 0.5f), centerY)
                    when {
                        index < activeIndex -> drawCircle(color = colorAt(position), radius = radius, center = center)
                        index == activeIndex -> drawCircle(color = colorAt(position), radius = radius * 1.35f, center = center)
                        else -> drawCircle(color = inactiveColor, radius = radius, center = center)
                    }
                }
            }
            else -> {
                val top = centerY - thickness / 2f
                val corner = CornerRadius(thickness / 2f, thickness / 2f)
                drawRoundRect(color = inactiveColor, topLeft = Offset(0f, top), size = Size(width, thickness), cornerRadius = corner)
                drawRoundRect(brush = activeBrush, topLeft = Offset(0f, top), size = Size(playheadX, thickness), cornerRadius = corner)
            }
        }
        if (markers.isNotEmpty()) {
            val markerRadius = 1.6.dp.toPx()
            val minGap = 5.dp.toPx()
            val markerY = size.height - markerRadius
            var lastX = -minGap
            markers.forEach { position ->
                val x = width * position
                if (x - lastX < minGap) return@forEach
                lastX = x
                drawCircle(
                    color = if (position <= progress) colorAt(position) else markerColor,
                    radius = markerRadius,
                    center = Offset(x, markerY)
                )
            }
        }
        if (thumbAlpha > 0.01f && look.style != SeekBarPreferences.STYLE_DOTS) {
            val thumbColor = colorAt(progress)
            if (look.thumb == SeekBarPreferences.THUMB_LINE) {
                val lineWidth = max(thickness * 0.8f, 3.dp.toPx()) * thumbScale
                val lineHeight = size.height * 0.72f
                drawRoundRect(
                    color = thumbColor,
                    alpha = thumbAlpha,
                    topLeft = Offset(playheadX - lineWidth / 2f, centerY - lineHeight / 2f),
                    size = Size(lineWidth, lineHeight),
                    cornerRadius = CornerRadius(lineWidth / 2f, lineWidth / 2f)
                )
            } else {
                val radius = max(thickness * 1.4f, 6.dp.toPx()) * thumbScale
                drawCircle(color = thumbColor, alpha = thumbAlpha, radius = radius, center = Offset(playheadX, centerY))
            }
        }
    }
}

private fun DrawScope.drawWaveform(
    look: SeekBarLook,
    waveform: FloatArray?,
    reveal: Float,
    progress: Float,
    inactive: Color,
    colorAt: (Float) -> Color
) {
    val chunky = look.waveDensity == SeekBarPreferences.DENSITY_CHUNKY
    val barWidth = (if (chunky) 4.5.dp else 2.5.dp).toPx()
    val gap = (if (chunky) 2.5.dp else 1.5.dp).toPx()
    val count = ((size.width + gap) / (barWidth + gap)).toInt().coerceAtLeast(8)
    val step = size.width / count
    val mirrored = look.waveShape == SeekBarPreferences.WAVE_MIRRORED
    val maxHeight = size.height * 0.9f
    val corner = CornerRadius(barWidth / 2f, barWidth / 2f)
    for (index in 0 until count) {
        val position = (index + 0.5f) / count
        val target = waveform?.sampleAt(position) ?: 0.18f
        val level = 0.18f + (target - 0.18f) * reveal
        val height = max(barWidth, maxHeight * level)
        val left = index * step + (step - barWidth) / 2f
        val top = if (mirrored) (size.height - height) / 2f else size.height - (size.height - maxHeight) / 2f - height
        drawRoundRect(
            color = if (position <= progress) colorAt(position) else inactive,
            topLeft = Offset(left, top),
            size = Size(barWidth, height),
            cornerRadius = corner
        )
    }
}

@Composable
internal fun PlayerSeekBar(
    track: Track,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val look = LocalLookAndFeel.current.seekBarLook()
    val haptics by rememberUpdatedState(LocalKateHaptics.current)
    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    var scrubFraction by remember { mutableStateOf<Float?>(null) }
    var lastScrubFraction by remember { mutableFloatStateOf(0f) }
    var scrubStep by remember { mutableIntStateOf(-1) }
    val position = rememberPlaybackPositionMs { if (active) trackWidthPx else 1f }
    val durationMs = PlayerController.durationMs.takeIf { it > 0 } ?: track.durationMs
    val currentDuration by rememberUpdatedState(durationMs)
    val waveform = rememberWaveform(track, enabled = active && look.needsWaveform)
    val markers = rememberLyricMarkers(track, durationMs, enabled = active && look.lyricMarkers)
    LaunchedEffect(Unit) { SeekBarPreferences.ensureRemainingLoaded(context) }
    val elapsedSeconds by remember(durationMs) {
        derivedStateOf {
            val scrub = scrubFraction
            val ms = if (scrub != null) (scrub * durationMs).toLong() else position.value
            ms / 1000L
        }
    }
    val fraction: () -> Float = {
        scrubFraction ?: if (durationMs > 0) (position.value.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    }
    val description = stringResource(R.string.seek_bar_description)
    val positionText = stringResource(R.string.seek_bar_position, formatPlaybackTime(elapsedSeconds * 1000L), formatPlaybackTime(durationMs))
    val bubbleAlpha by animateFloatAsState(
        targetValue = if (scrubFraction != null && look.bubble) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "seek_bubble_alpha"
    )
    var bubbleWidth by remember { mutableIntStateOf(0) }
    var bubbleHeight by remember { mutableIntStateOf(0) }
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SeekBarTrack(
                look = look,
                fraction = fraction,
                playing = active && PlayerController.isPlaying,
                scrubbing = scrubFraction != null,
                waveform = waveform,
                markers = markers,
                modifier = Modifier
                    .onSizeChanged { trackWidthPx = it.width.toFloat() }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            if (size.width > 0) {
                                haptics.click()
                                PlayerController.seekToFraction((offset.x / size.width).coerceIn(0f, 1f))
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                if (size.width > 0) {
                                    val value = (offset.x / size.width).coerceIn(0f, 1f)
                                    scrubFraction = value
                                    lastScrubFraction = value
                                    scrubStep = (value * ScrubHapticSteps).toInt()
                                    haptics.tick()
                                }
                            },
                            onDragEnd = {
                                scrubFraction?.let { PlayerController.seekToFraction(it) }
                                scrubFraction = null
                                scrubStep = -1
                            },
                            onDragCancel = {
                                scrubFraction = null
                                scrubStep = -1
                            },
                            onHorizontalDrag = { change, _ ->
                                change.consume()
                                if (size.width > 0) {
                                    val value = (change.position.x / size.width).coerceIn(0f, 1f)
                                    scrubFraction = value
                                    lastScrubFraction = value
                                    val step = (value * ScrubHapticSteps).toInt()
                                    if (step != scrubStep) {
                                        scrubStep = step
                                        haptics.tick()
                                    }
                                }
                            }
                        )
                    }
                    .semantics {
                        contentDescription = description
                        stateDescription = positionText
                        progressBarRangeInfo = ProgressBarRangeInfo(fraction(), 0f..1f)
                        setProgress { target ->
                            if (currentDuration <= 0L) return@setProgress false
                            PlayerController.seekToFraction(target.coerceIn(0f, 1f))
                            true
                        }
                    }
            )
            if (bubbleAlpha > 0.01f) {
                val gapPx = with(density) { 8.dp.roundToPx() }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .zIndex(1f)
                        .offset {
                            val center = trackWidthPx * lastScrubFraction
                            val maxX = (trackWidthPx - bubbleWidth).roundToInt().coerceAtLeast(0)
                            IntOffset((center - bubbleWidth / 2f).roundToInt().coerceIn(0, maxX), -bubbleHeight - gapPx)
                        }
                        .onSizeChanged {
                            bubbleWidth = it.width
                            bubbleHeight = it.height
                        }
                        .graphicsLayer {
                            alpha = bubbleAlpha
                            val scale = 0.85f + 0.15f * bubbleAlpha
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primary)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = formatPlaybackTime((lastScrubFraction * durationMs).toLong()),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimary
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatPlaybackTime(elapsedSeconds * 1000L),
                style = MaterialTheme.typography.labelSmall,
                color = colors.secondaryText
            )
            val showRemaining = SeekBarPreferences.showRemaining
            Text(
                text = if (showRemaining) {
                    "-" + formatPlaybackTime((durationMs - elapsedSeconds * 1000L).coerceAtLeast(0L))
                } else {
                    formatPlaybackTime(durationMs)
                },
                style = MaterialTheme.typography.labelSmall,
                color = colors.secondaryText,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClickLabel = stringResource(R.string.seek_toggle_remaining)) {
                        haptics.click()
                        SeekBarPreferences.toggleRemaining(context)
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
internal fun SeekBarPreview(look: SeekBarLook, progress: () -> Float, modifier: Modifier = Modifier) {
    val sample = remember {
        FloatArray(WaveformStore.BUCKETS) { index ->
            val t = index / WaveformStore.BUCKETS.toFloat()
            val swell = 0.45f + 0.35f * sin(t * 9.4f) * sin(t * 2.3f + 0.6f)
            val texture = 0.18f * sin(t * 71f) * sin(t * 37f + 1.1f)
            (swell + texture).coerceIn(0.08f, 1f)
        }
    }
    SeekBarTrack(
        look = look,
        fraction = progress,
        playing = true,
        scrubbing = false,
        waveform = if (look.needsWaveform) sample else null,
        markers = if (look.lyricMarkers) listOf(0.12f, 0.34f, 0.58f, 0.81f) else emptyList(),
        modifier = modifier
    )
}
