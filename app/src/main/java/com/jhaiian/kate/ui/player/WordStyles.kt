package com.jhaiian.kate.ui.player

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.data.LyricWord
import com.jhaiian.kate.ui.theme.LyricsPreferences
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

internal class WordSegment(val index: Int, val rect: Rect, val layout: TextLayoutResult)

internal class WordSegments(val all: List<WordSegment>, val first: List<WordSegment?>)

internal class WordSegmentCache {
    private var layout: TextLayoutResult? = null
    private var words: List<LyricWord> = emptyList()
    private var cached: WordSegments? = null

    fun get(layout: TextLayoutResult, words: List<LyricWord>, measurer: TextMeasurer): WordSegments {
        val current = cached
        if (current != null && this.layout === layout && this.words == words) return current
        val built = buildWordSegments(layout, words, measurer)
        this.layout = layout
        this.words = words
        cached = built
        return built
    }
}

private class WordEffect(
    val scale: Float = 1f,
    val dy: Float = 0f,
    val baseAlpha: Float = 1f,
    val fillAlpha: Float = 0f,
    val glow: Float = 0f,
    val clip: Float = 1f
)

private fun buildWordSegments(layout: TextLayoutResult, words: List<LyricWord>, measurer: TextMeasurer): WordSegments {
    val text = layout.layoutInput.text.text
    val style = layout.layoutInput.style.copy(color = Color.Unspecified)
    val all = ArrayList<WordSegment>()
    val first = arrayOfNulls<WordSegment>(words.size)
    var cursor = 0
    for (index in words.indices) {
        val start = if (index == 0) 0 else words[index].charStart.coerceIn(cursor, text.length)
        val rawEnd = if (index == words.lastIndex) text.length else words[index + 1].charStart.coerceIn(start, text.length)
        cursor = rawEnd
        var from = start
        var to = rawEnd
        while (from < to && text[from].isWhitespace()) from++
        while (to > from && text[to - 1].isWhitespace()) to--
        if (to <= from) continue
        val firstLine = layout.getLineForOffset(from)
        val lastLine = layout.getLineForOffset(to - 1)
        for (line in firstLine..lastLine) {
            var a = max(from, layout.getLineStart(line))
            var b = min(to, layout.getLineEnd(line, visibleEnd = true))
            while (a < b && text[a].isWhitespace()) a++
            while (b > a && text[b - 1].isWhitespace()) b--
            if (b <= a) continue
            val rect = layout.getPathForRange(a, b).getBounds()
            val wordLayout = measurer.measure(
                text = AnnotatedString(text.substring(a, b)),
                style = style,
                softWrap = false,
                maxLines = 1
            )
            val segment = WordSegment(index, rect, wordLayout)
            all.add(segment)
            if (first[index] == null) first[index] = segment
        }
    }
    return WordSegments(all, first.toList())
}

private fun smooth(x: Float): Float {
    val v = x.coerceIn(0f, 1f)
    return v * v * (3f - 2f * v)
}

private fun linear(x: Float): Float = x.coerceIn(0f, 1f)

private fun mix(from: Float, to: Float, fraction: Float): Float = from + (to - from) * fraction

private fun currentWord(words: List<LyricWord>, positionMs: Long): Int {
    var current = -1
    for (index in words.indices) {
        if (words[index].startMs <= positionMs) current = index else break
    }
    return current
}

private fun waveCursor(words: List<LyricWord>, positionMs: Long): Float {
    val current = currentWord(words, positionMs)
    if (current < 0) return -2f
    val start = words[current].startMs
    val last = current == words.lastIndex
    val next = if (last) max(words[current].endMs, start) + 450L else words[current + 1].startMs
    val fraction = ((positionMs - start).toFloat() / max(next - start, 1L).toFloat()).coerceIn(0f, 1f)
    return current + fraction * (if (last) 3f else 1f)
}

private fun wordEffect(
    style: String,
    word: LyricWord,
    index: Int,
    height: Float,
    positionMs: Long,
    emphasis: Float,
    cursor: Float
): WordEffect {
    val start = word.startMs
    val end = max(word.endMs, start + 1L)
    val duration = (end - start).toFloat()
    val since = (positionMs - start).toFloat()
    val after = (positionMs - end).toFloat()
    return when (style) {
        LyricsPreferences.WORD_STYLE_POP -> {
            val up = smooth(since / 140f)
            val bump = up * (1f - smooth(after / 380f))
            WordEffect(
                scale = 1f + 0.1f * bump * emphasis,
                dy = -height * 0.1f * bump * emphasis,
                fillAlpha = up * emphasis
            )
        }
        LyricsPreferences.WORD_STYLE_FADE -> {
            val span = (duration * 0.6f).coerceIn(140f, 420f)
            WordEffect(fillAlpha = smooth(since / span) * emphasis)
        }
        LyricsPreferences.WORD_STYLE_GLOW -> {
            val up = smooth(since / 120f)
            val glow = up * (1f - smooth(after / 520f)) * emphasis
            WordEffect(scale = 1f + 0.03f * glow, fillAlpha = up * emphasis, glow = glow)
        }
        LyricsPreferences.WORD_STYLE_HIGHLIGHTER,
        LyricsPreferences.WORD_STYLE_PILL -> WordEffect(fillAlpha = smooth(since / 120f) * emphasis)
        LyricsPreferences.WORD_STYLE_REVEAL -> {
            val revealed = smooth(since / 220f)
            WordEffect(
                dy = (1f - revealed) * height * 0.25f * emphasis,
                baseAlpha = 1f - 0.9f * emphasis * (1f - revealed),
                fillAlpha = revealed * emphasis
            )
        }
        LyricsPreferences.WORD_STYLE_WAVE -> {
            val distance = index - cursor
            val lift = exp(-(distance * distance) / 0.98f)
            WordEffect(dy = -height * 0.2f * lift * emphasis, fillAlpha = smooth(since / 140f) * emphasis)
        }
        LyricsPreferences.WORD_STYLE_STRETCH -> {
            val grow = 0.03f + 0.12f * ((duration - 350f) / 1650f).coerceIn(0f, 1f)
            val progress = linear(since / duration)
            val scale = 1f + grow * smooth(progress) * (1f - smooth(after / 420f)) * emphasis
            WordEffect(scale = scale, fillAlpha = emphasis, clip = progress)
        }
        LyricsPreferences.WORD_STYLE_FOCUS -> {
            val focus = smooth((since + 200f) / 260f)
            val bump = smooth(since / 120f) * (1f - smooth(after / 300f))
            val scale = mix(0.9f, 1f, focus) + 0.05f * bump
            WordEffect(
                scale = 1f + (scale - 1f) * emphasis,
                baseAlpha = mix(1f - 0.45f * emphasis, 1f, focus),
                fillAlpha = focus * emphasis
            )
        }
        else -> WordEffect()
    }
}

internal fun DrawScope.drawWordStyle(
    style: String,
    segments: WordSegments,
    words: List<LyricWord>,
    positionMs: Long,
    baseColor: Color,
    fillColor: Color,
    emphasis: Float
) {
    val emph = emphasis.coerceIn(0f, 1f)
    val unit = 1.dp.toPx()
    when (style) {
        LyricsPreferences.WORD_STYLE_PILL -> drawPill(segments, words, positionMs, fillColor, emph, unit)
        LyricsPreferences.WORD_STYLE_HIGHLIGHTER -> drawHighlighter(segments, words, positionMs, fillColor, emph, unit)
    }
    val cursor = if (style == LyricsPreferences.WORD_STYLE_WAVE) waveCursor(words, positionMs) else 0f
    for (segment in segments.all) {
        val word = words.getOrNull(segment.index) ?: continue
        val effect = wordEffect(style, word, segment.index, segment.rect.height, positionMs, emph, cursor)
        drawSegment(segment, effect, baseColor, fillColor, unit)
    }
}

private fun DrawScope.drawSegment(
    segment: WordSegment,
    effect: WordEffect,
    baseColor: Color,
    fillColor: Color,
    unit: Float
) {
    val rect = segment.rect
    val pivot = Offset(rect.center.x, rect.bottom)
    withTransform({
        translate(0f, effect.dy)
        scale(effect.scale, effect.scale, pivot)
    }) {
        if (effect.baseAlpha > 0.01f) {
            drawText(
                textLayoutResult = segment.layout,
                color = baseColor.copy(alpha = baseColor.alpha * effect.baseAlpha),
                topLeft = rect.topLeft
            )
        }
        if (effect.fillAlpha > 0.01f) {
            val color = fillColor.copy(alpha = fillColor.alpha * effect.fillAlpha)
            val shadow = if (effect.glow > 0.02f) {
                Shadow(color = fillColor.copy(alpha = 0.85f * effect.glow), offset = Offset.Zero, blurRadius = 14f * unit * effect.glow)
            } else {
                null
            }
            if (effect.clip < 1f) {
                if (effect.clip > 0f) {
                    val rtl = segment.layout.getParagraphDirection(0) == ResolvedTextDirection.Rtl
                    val width = rect.width * effect.clip
                    clipRect(
                        left = if (rtl) rect.right - width else rect.left - 4f * unit,
                        top = rect.top - 4f * unit,
                        right = if (rtl) rect.right + 4f * unit else rect.left + width,
                        bottom = rect.bottom + 4f * unit
                    ) {
                        drawText(textLayoutResult = segment.layout, color = color, topLeft = rect.topLeft, shadow = shadow)
                    }
                }
            } else {
                drawText(textLayoutResult = segment.layout, color = color, topLeft = rect.topLeft, shadow = shadow)
            }
        }
    }
}

private fun DrawScope.drawHighlighter(
    segments: WordSegments,
    words: List<LyricWord>,
    positionMs: Long,
    fillColor: Color,
    emphasis: Float,
    unit: Float
) {
    if (emphasis <= 0.01f) return
    for (segment in segments.all) {
        val word = words.getOrNull(segment.index) ?: continue
        val span = max(word.endMs - word.startMs, 1L).toFloat()
        val progress = linear((positionMs - word.startMs).toFloat() / span)
        if (progress <= 0f) continue
        val rect = segment.rect
        val rtl = segment.layout.getParagraphDirection(0) == ResolvedTextDirection.Rtl
        val width = (rect.width + 6f * unit) * progress
        val left = if (rtl) rect.right + 3f * unit - width else rect.left - 3f * unit
        val top = rect.top + rect.height * 0.5f
        val height = rect.height * 0.44f
        drawRoundRect(
            color = fillColor.copy(alpha = fillColor.alpha * 0.3f * emphasis),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(height * 0.3f)
        )
    }
}

private fun DrawScope.drawPill(
    segments: WordSegments,
    words: List<LyricWord>,
    positionMs: Long,
    fillColor: Color,
    emphasis: Float,
    unit: Float
) {
    if (emphasis <= 0.01f || words.isEmpty()) return
    val current = currentWord(words, positionMs)
    val index = max(current, 0)
    val currentRect = segments.first.getOrNull(index)?.rect ?: return
    val nextRect = segments.first.getOrNull(index + 1)?.rect
    val rect = if (current >= 0 && nextRect != null) {
        val hop = smooth((positionMs - (words[index + 1].startMs - 170L)).toFloat() / 170f)
        lerp(currentRect, nextRect, hop)
    } else {
        currentRect
    }
    val lead = smooth((positionMs - (words.first().startMs - 350L)).toFloat() / 350f)
    val tail = 1f - smooth((positionMs - words.last().endMs).toFloat() / 450f)
    val alpha = min(lead, tail) * emphasis
    if (alpha <= 0.01f) return
    val padX = 8f * unit
    val padY = 2f * unit
    drawRoundRect(
        color = fillColor.copy(alpha = fillColor.alpha * 0.26f * alpha),
        topLeft = Offset(rect.left - padX, rect.top + padY),
        size = Size(rect.width + padX * 2f, max(rect.height - padY * 2f, 0f)),
        cornerRadius = CornerRadius(rect.height * 0.38f)
    )
}
