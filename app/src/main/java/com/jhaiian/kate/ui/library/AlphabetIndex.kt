package com.jhaiian.kate.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.delay
import java.text.Normalizer

internal class AlphabetSection(val letter: String, val firstIndex: Int)

private const val MinTilesForAlphabet = 30
private const val OtherSection = "#"
private const val MinStrayBlocks = 3
private const val StrayBlockDivisor = 4
private const val PillHideDelayMillis = 700L
private val RailWidth = 20.dp
private val RailLetterSpacing = 22.dp
private val BubbleSize = 72.dp
private val BubbleGap = 56.dp

internal fun sectionLetter(title: String): String {
    val first = title.trim().firstOrNull() ?: return OtherSection
    if (!first.isLetter()) return OtherSection
    val base = Normalizer.normalize(first.toString(), Normalizer.Form.NFD).firstOrNull() ?: first
    val upper = base.uppercaseChar()
    return if (upper in 'A'..'Z') upper.toString() else OtherSection
}

internal fun alphabetSections(titles: List<String>): List<AlphabetSection>? {
    if (titles.size < MinTilesForAlphabet) return null
    val firstIndex = LinkedHashMap<String, Int>()
    var blocks = 0
    var previous: String? = null
    titles.forEachIndexed { index, title ->
        val letter = sectionLetter(title)
        if (letter != previous) {
            blocks++
            previous = letter
        }
        if (letter !in firstIndex) firstIndex[letter] = index
    }
    val distinct = firstIndex.size
    if (distinct < 2) return null
    if (blocks > distinct + maxOf(MinStrayBlocks, distinct / StrayBlockDivisor)) return null
    return firstIndex.map { (letter, index) -> AlphabetSection(letter, index) }.sortedBy { it.firstIndex }
}

@Composable
internal fun BoxScope.AlphabetRail(
    sections: List<AlphabetSection>,
    topPadding: Dp,
    bottomPadding: Dp,
    onJump: (Int) -> Unit
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    var activeIndex by remember(sections) { mutableStateOf<Int?>(null) }
    val jump by rememberUpdatedState(onJump)
    val activate: (Int) -> Unit = { index ->
        if (index != activeIndex) {
            activeIndex = index
            haptics.tick()
            jump(sections[index].firstIndex)
        }
    }
    val activateState by rememberUpdatedState(activate)
    var lastBubbleLetter by remember { mutableStateOf("") }
    val bubbleLetter = activeIndex?.let { sections.getOrNull(it)?.letter }
    SideEffect { if (bubbleLetter != null) lastBubbleLetter = bubbleLetter }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(top = topPadding, bottom = bottomPadding)
            .width(RailWidth)
            .heightIn(max = RailLetterSpacing * sections.size)
            .fillMaxHeight()
            .clip(RoundedCornerShape(RailWidth / 2))
            .background(if (activeIndex != null) colors.primary.copy(alpha = 0.16f) else colors.background.copy(alpha = 0.72f))
            .pointerInput(sections) {
                fun indexAt(y: Float): Int = (y / size.height * sections.size).toInt().coerceIn(0, sections.lastIndex)
                awaitEachGesture {
                    try {
                        val down = awaitFirstDown()
                        down.consume()
                        activateState(indexAt(down.position.y))
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) break
                            change.consume()
                            activateState(indexAt(change.position.y))
                        }
                    } finally {
                        activeIndex = null
                    }
                }
            }
    ) {
        sections.forEachIndexed { index, section ->
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = section.letter,
                    fontSize = 9.sp,
                    fontWeight = if (index == activeIndex) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (index == activeIndex) colors.primary else colors.secondaryText,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }

    AnimatedVisibility(
        visible = activeIndex != null,
        enter = fadeIn(tween(durationMillis = 120)) + scaleIn(initialScale = 0.7f),
        exit = fadeOut(tween(durationMillis = 160)) + scaleOut(targetScale = 0.8f),
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = BubbleGap)
    ) {
        Box(
            modifier = Modifier
                .size(BubbleSize)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(colors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bubbleLetter ?: lastBubbleLetter,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimary
            )
        }
    }
}

@Composable
internal fun BoxScope.SectionPill(letter: String?, scrolling: Boolean) {
    val colors = LocalClintColors.current
    var visible by remember { mutableStateOf(false) }
    var lastLetter by remember { mutableStateOf("") }
    SideEffect { if (letter != null) lastLetter = letter }
    LaunchedEffect(scrolling) {
        if (scrolling) {
            visible = true
        } else {
            delay(PillHideDelayMillis)
            visible = false
        }
    }
    AnimatedVisibility(
        visible = visible && letter != null,
        enter = fadeIn(tween(durationMillis = 150)) + scaleIn(initialScale = 0.85f),
        exit = fadeOut(tween(durationMillis = 220)),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(colors.cardBackground)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter ?: lastLetter,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
    }
}
