package com.jhaiian.kate.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.playback.SpectrumAnalyzer
import com.jhaiian.kate.playback.VisualizerTap
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.VisualizerPreferences
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.sin

private const val FFT_SIZE = 1024
private const val WAVE_POINTS = 192
private const val MIN_FREQUENCY = 40.0

@Composable
fun AudioVisualizer(modifier: Modifier = Modifier, alpha: Float = 1f) {
    val look = LocalLookAndFeel.current
    val style = look.visualizerStyle
    if (style == VisualizerPreferences.STYLE_OFF) return
    val colors = LocalClintColors.current
    val barCount = look.visualizerBars
    val sensitivity = look.visualizerSensitivity / 100f
    val smoothing = look.visualizerSmoothing / 100f
    val fps = look.visualizerFps
    DisposableEffect(Unit) {
        VisualizerTap.acquire()
        onDispose { VisualizerTap.release() }
    }
    val analyzer = remember { SpectrumAnalyzer(FFT_SIZE) }
    val spectrum = remember { FloatArray(FFT_SIZE / 2) }
    val levels = remember(barCount) { FloatArray(barCount) }
    val wave = remember { FloatArray(WAVE_POINTS) }
    var frame by remember { mutableLongStateOf(0L) }
    LaunchedEffect(barCount, sensitivity, smoothing, fps) {
        val minFrameNs = if (fps > 0) 1_000_000_000L / fps else 0L
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (now - last >= minFrameNs) {
                    last = now
                    val fresh = PlayerController.isPlaying && VisualizerTap.read(analyzer.samples)
                    if (fresh) {
                        analyzer.magnitudes(spectrum)
                        fillBars(spectrum, levels, VisualizerTap.rate, sensitivity, smoothing)
                        val stride = FFT_SIZE / WAVE_POINTS
                        for (i in wave.indices) {
                            val target = (analyzer.samples[i * stride] * 2.2f * sensitivity).coerceIn(-1f, 1f)
                            wave[i] = wave[i] * smoothing * 0.5f + target * (1f - smoothing * 0.5f)
                        }
                    } else {
                        for (i in levels.indices) levels[i] *= 0.86f
                        for (i in wave.indices) wave[i] *= 0.8f
                    }
                    frame = now
                }
            }
        }
    }
    val primary = when (look.visualizerColor) {
        VisualizerPreferences.COLOR_CUSTOM -> Color(look.visualizerCustomColor)
        else -> colors.primary
    }
    val secondary = if (look.visualizerColor == VisualizerPreferences.COLOR_GRADIENT) shiftedHue(colors.primary) else primary
    Canvas(modifier = modifier) {
        if (frame < 0L) return@Canvas
        val brush = Brush.verticalGradient(listOf(secondary.copy(alpha = alpha), primary.copy(alpha = alpha)))
        when (style) {
            VisualizerPreferences.STYLE_MIRROR -> drawMirror(levels, brush)
            VisualizerPreferences.STYLE_LINE -> drawSpectrumLine(levels, primary.copy(alpha = alpha), secondary.copy(alpha = alpha * 0.35f))
            VisualizerPreferences.STYLE_WAVE -> drawWave(wave, primary.copy(alpha = alpha))
            VisualizerPreferences.STYLE_RING -> drawRing(levels, primary.copy(alpha = alpha), secondary.copy(alpha = alpha))
            VisualizerPreferences.STYLE_PULSE -> drawPulse(levels, primary.copy(alpha = alpha), secondary.copy(alpha = alpha))
            else -> drawBars(levels, brush)
        }
    }
}

private fun fillBars(spectrum: FloatArray, levels: FloatArray, rate: Int, sensitivity: Float, smoothing: Float) {
    val bins = spectrum.size
    val binHz = rate.toDouble() / FFT_SIZE
    val maxFrequency = rate / 2.0 * 0.95
    val logMin = ln(MIN_FREQUENCY)
    val logSpan = ln(maxFrequency) - logMin
    for (bar in levels.indices) {
        val lowHz = exp(logMin + logSpan * bar / levels.size)
        val highHz = exp(logMin + logSpan * (bar + 1) / levels.size)
        val low = (lowHz / binHz).toInt().coerceIn(1, bins - 1)
        val high = (highHz / binHz).toInt().coerceIn(low, bins - 1)
        var peak = 0f
        for (bin in low..high) if (spectrum[bin] > peak) peak = spectrum[bin]
        val tilt = 1f + bar.toFloat() / levels.size
        val db = 20f * log10(peak * tilt + 1e-9f)
        val target = ((db + 62f) / 56f * sensitivity).coerceIn(0f, 1f)
        val current = levels[bar]
        levels[bar] = if (target > current) current + (target - current) * 0.65f else current * smoothing + target * (1f - smoothing)
    }
}

private fun DrawScope.drawBars(levels: FloatArray, brush: Brush) {
    val slot = size.width / levels.size
    val width = slot * 0.66f
    val radius = CornerRadius(width / 2f, width / 2f)
    levels.forEachIndexed { index, level ->
        val height = (level * size.height).coerceAtLeast(width)
        drawRoundRect(
            brush = brush,
            topLeft = Offset(index * slot + (slot - width) / 2f, size.height - height),
            size = Size(width, height),
            cornerRadius = radius
        )
    }
}

private fun DrawScope.drawMirror(levels: FloatArray, brush: Brush) {
    val slot = size.width / levels.size
    val width = slot * 0.6f
    val radius = CornerRadius(width / 2f, width / 2f)
    val center = size.height / 2f
    levels.forEachIndexed { index, level ->
        val half = (level * size.height / 2f).coerceAtLeast(width / 2f)
        drawRoundRect(
            brush = brush,
            topLeft = Offset(index * slot + (slot - width) / 2f, center - half),
            size = Size(width, half * 2f),
            cornerRadius = radius
        )
    }
}

private fun DrawScope.drawSpectrumLine(levels: FloatArray, stroke: Color, fill: Color) {
    if (levels.size < 2) return
    val step = size.width / (levels.size - 1)
    val path = Path()
    levels.forEachIndexed { index, level ->
        val x = index * step
        val y = size.height - level * size.height
        if (index == 0) path.moveTo(x, y) else {
            val previousX = (index - 1) * step
            val previousY = size.height - levels[index - 1] * size.height
            val midX = (previousX + x) / 2f
            path.cubicTo(midX, previousY, midX, y, x, y)
        }
    }
    val area = Path().apply {
        addPath(path)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(area, Brush.verticalGradient(listOf(fill, Color.Transparent)))
    drawPath(path, stroke, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawWave(wave: FloatArray, color: Color) {
    val step = size.width / (wave.size - 1)
    val center = size.height / 2f
    val path = Path()
    wave.forEachIndexed { index, value ->
        val x = index * step
        val y = center - value * center * 0.9f
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path, color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawRing(levels: FloatArray, inner: Color, outer: Color) {
    val minSide = min(size.width, size.height)
    val radius = minSide * 0.3f
    val reach = minSide * 0.18f
    val center = Offset(size.width / 2f, size.height / 2f)
    val strokeWidth = (2 * PI * radius / levels.size * 0.55).toFloat().coerceAtLeast(1.5f)
    levels.forEachIndexed { index, level ->
        val angle = 2 * PI * index / levels.size - PI / 2
        val dx = cos(angle).toFloat()
        val dy = sin(angle).toFloat()
        val length = level * reach + strokeWidth
        drawLine(
            brush = Brush.linearGradient(
                listOf(inner, outer),
                start = Offset(center.x + dx * radius, center.y + dy * radius),
                end = Offset(center.x + dx * (radius + reach), center.y + dy * (radius + reach))
            ),
            start = Offset(center.x + dx * radius, center.y + dy * radius),
            end = Offset(center.x + dx * (radius + length), center.y + dy * (radius + length)),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPulse(levels: FloatArray, core: Color, glow: Color) {
    val bassCount = (levels.size / 6).coerceAtLeast(1)
    var bass = 0f
    for (i in 0 until bassCount) bass += levels[i]
    bass /= bassCount
    var overall = 0f
    for (level in levels) overall += level
    overall /= levels.size
    val minSide = min(size.width, size.height)
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = minSide * (0.2f + bass * 0.22f)
    drawCircle(
        brush = Brush.radialGradient(listOf(glow.copy(alpha = glow.alpha * 0.8f), Color.Transparent), center = center, radius = radius * 2.2f),
        radius = radius * 2.2f,
        center = center
    )
    drawCircle(color = core.copy(alpha = core.alpha * (0.25f + overall * 0.5f)), radius = radius, center = center)
    drawCircle(color = core, radius = radius * (1.04f + overall * 0.12f), center = center, style = Stroke(width = 2.dp.toPx()))
}
