package com.jhaiian.kate.ui.player

import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.foundation.verticalScroll
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.text.format.Formatter
import android.net.Uri
import android.content.Intent
import androidx.compose.material.icons.outlined.TravelExplore
import android.content.ClipboardManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.DraftLine
import com.jhaiian.kate.data.Lyrics
import com.jhaiian.kate.data.LyricsRepository
import com.jhaiian.kate.data.LyricsTagWriter
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.KateChip
import com.jhaiian.kate.ui.components.KateControlButton
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.KatePlayButton
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.actionShape
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val TapReactionMs = 150L
private const val NudgeMs = 50L
private const val SkipMs = 5_000L
private const val PreviewLeadMs = 2_000L
private const val MinHoldMs = 80L
private val SpeedChoices = listOf(0.5f, 0.75f, 1f)

private enum class MakerStep { TEXT, SYNC }

private enum class SyncMode { LINE, WORD }

private class Snapshot(val index: Int, val line: DraftLine, val target: Int, val wordTarget: Int)

object LyricsMakerRequest {
    var track by mutableStateOf<Track?>(null)
        private set
    var initial by mutableStateOf<Lyrics?>(null)
        private set
    var session by mutableIntStateOf(0)
        private set

    fun open(track: Track, lyrics: Lyrics?) {
        this.track = track
        initial = lyrics
        session++
    }

    fun close() {
        track = null
        initial = null
    }
}

@Composable
fun LyricsMakerHost() {
    val track = LyricsMakerRequest.track
    AnimatedVisibility(
        visible = track != null,
        enter = fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 8 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 10 }
    ) {
        if (track != null) {
            key(LyricsMakerRequest.session) {
                LyricsMakerContent(
                    track = track,
                    initial = LyricsMakerRequest.initial,
                    onClose = { LyricsMakerRequest.close() }
                )
            }
        }
    }
}

private fun initialText(lyrics: Lyrics?): String =
    lyrics?.lines
        ?.filter { it.text.isNotBlank() }
        ?.joinToString("\n") { LyricsTagWriter.voicePrefix(it.voice, lyrics.duet) + it.text }
        .orEmpty()

private fun formatStamp(timeMs: Long?): String =
    if (timeMs == null) "--:--.--" else LyricsTagWriter.formatTag(timeMs)

private fun shifted(values: List<Long?>?, delta: Long): List<Long?>? =
    values?.map { value -> value?.let { (it + delta).coerceAtLeast(0L) } }

private fun speedLabel(speed: Float): String = when (speed) {
    0.5f -> "0.5×"
    0.75f -> "0.75×"
    else -> "1×"
}

@Composable
private fun LyricsMakerContent(track: Track, initial: Lyrics?, onClose: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(MakerStep.TEXT) }
    var base by remember { mutableStateOf(initial) }
    var text by remember { mutableStateOf(initialText(initial)) }
    val lines = remember { mutableStateListOf<DraftLine>() }
    val history = remember { mutableStateListOf<Snapshot>() }
    var target by remember { mutableIntStateOf(0) }
    var wordTarget by remember { mutableIntStateOf(0) }
    var mode by remember { mutableStateOf(SyncMode.LINE) }
    var holding by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var dirty by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val sameTrack = PlayerController.currentTrack?.id == track.id
    val savedMessage = stringResource(R.string.sync_saved)
    val saveFailedMessage = stringResource(R.string.sync_save_failed)

    DisposableEffect(Unit) {
        onDispose {
            AudioSettings.ensureLoaded(context)
            val restore = AudioSettings.playbackSpeed
            if (PlayerController.playbackSpeed != restore) PlayerController.setSpeed(restore)
        }
    }

    LaunchedEffect(Unit) {
        if (base == null) {
            val loaded = LyricsRepository.load(context, track)
            if (loaded != null && text.isBlank() && !dirty) {
                base = loaded
                text = initialText(loaded)
            }
        }
    }

    val requestClose: () -> Unit = {
        if (dirty) confirmDiscard = true else onClose()
    }
    BackHandler(enabled = !confirmDiscard) {
        if (step == MakerStep.SYNC) step = MakerStep.TEXT else requestClose()
    }

    val goToSync: () -> Unit = {
        val source = base
        val previous = if (lines.isNotEmpty()) {
            lines.toList()
        } else if (source != null && source.synced) {
            source.lines.filter { it.text.isNotBlank() }.map { line ->
                DraftLine(
                    text = LyricsTagWriter.voicePrefix(line.voice, source.duet) + line.text,
                    timeMs = line.timeMs,
                    words = LyricsTagWriter.wordStarts(line),
                    ends = LyricsTagWriter.wordEnds(line)
                )
            }
        } else {
            emptyList()
        }
        val fresh = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var pointer = 0
        val built = fresh.map { line ->
            val match = previous.getOrNull(pointer)
            if (match != null && match.text.trim() == line) {
                pointer++
                match.copy(text = line)
            } else {
                DraftLine(line, null)
            }
        }
        lines.clear()
        lines.addAll(built)
        history.clear()
        target = built.indexOfFirst { it.timeMs == null }.takeIf { it >= 0 } ?: 0
        wordTarget = 0
        step = MakerStep.SYNC
    }

    fun record(index: Int) {
        history.add(Snapshot(index, lines[index], target, wordTarget))
    }

    fun advanceLine() {
        target = (target + 1).coerceAtMost(lines.size)
        wordTarget = 0
    }

    val pressDown: () -> Unit = {
        if (target in lines.indices) {
            val now = (PlayerController.estimatedPositionMs() - TapReactionMs).coerceAtLeast(0L)
            val line = lines[target]
            record(target)
            dirty = true
            haptics.tick()
            if (mode == SyncMode.LINE) {
                val old = line.timeMs
                val delta = if (old != null) now - old else 0L
                lines[target] = line.copy(
                    timeMs = now,
                    words = if (old != null) shifted(line.words, delta) else null,
                    ends = if (old != null) shifted(line.ends, delta) else null
                )
                advanceLine()
            } else {
                val count = LyricsTagWriter.wordsOf(line.text).size
                if (count == 0) {
                    lines[target] = line.copy(timeMs = now)
                    advanceLine()
                } else {
                    val index = wordTarget.coerceIn(0, count - 1)
                    val starts = (line.words?.takeIf { it.size == count } ?: List(count) { null }).toMutableList()
                    val ends = (line.ends?.takeIf { it.size == count } ?: List(count) { null }).toMutableList()
                    starts[index] = now
                    ends[index] = null
                    val lineTime = if (index == 0 || line.timeMs == null) now else line.timeMs
                    lines[target] = line.copy(timeMs = lineTime, words = starts, ends = ends)
                    wordTarget = index
                    holding = true
                }
            }
        }
    }
    val pressUp: () -> Unit = {
        if (holding && mode == SyncMode.WORD && target in lines.indices) {
            holding = false
            val now = (PlayerController.estimatedPositionMs() - TapReactionMs).coerceAtLeast(0L)
            val line = lines[target]
            val count = LyricsTagWriter.wordsOf(line.text).size
            val start = line.words?.getOrNull(wordTarget)
            if (start != null && now - start >= MinHoldMs) {
                val ends = (line.ends?.takeIf { it.size == count } ?: List(count) { null }).toMutableList()
                if (wordTarget in ends.indices) ends[wordTarget] = now
                lines[target] = line.copy(ends = ends)
            }
            if (wordTarget + 1 >= count) advanceLine() else wordTarget += 1
        } else {
            holding = false
        }
    }
    val undo: () -> Unit = {
        val last = history.removeLastOrNull()
        if (last != null) {
            lines[last.index] = last.line
            target = last.target
            wordTarget = last.wordTarget
            haptics.click()
        }
    }
    val nudge: (Int, Long) -> Unit = { index, delta ->
        val line = lines.getOrNull(index)
        if (line != null) {
            val count = LyricsTagWriter.wordsOf(line.text).size
            val word = wordTarget
            val wordStart = line.words?.takeIf { it.size == count }?.getOrNull(word)
            if (mode == SyncMode.WORD && index == target && wordStart != null) {
                record(index)
                val starts = line.words.orEmpty().toMutableList()
                val ends = line.ends?.takeIf { it.size == count }?.toMutableList()
                starts[word] = (wordStart + delta).coerceAtLeast(0L)
                ends?.let { list -> list[word] = list[word]?.let { (it + delta).coerceAtLeast(0L) } }
                val lineTime = if (word == 0) starts[word] else line.timeMs
                lines[index] = line.copy(timeMs = lineTime, words = starts, ends = ends)
                dirty = true
                haptics.tick()
            } else if (line.timeMs != null) {
                record(index)
                lines[index] = line.copy(
                    timeMs = (line.timeMs + delta).coerceAtLeast(0L),
                    words = shifted(line.words, delta),
                    ends = shifted(line.ends, delta)
                )
                dirty = true
                haptics.tick()
            }
        }
    }
    val clearLine: (Int) -> Unit = { index ->
        if (index in lines.indices) {
            record(index)
            lines[index] = lines[index].copy(timeMs = null, words = null, ends = null)
            target = index
            wordTarget = 0
            dirty = true
            haptics.click()
        }
    }
    val playFrom: (Int) -> Unit = { index ->
        val line = lines.getOrNull(index)
        val start = line?.timeMs ?: lines.take(index).lastOrNull { it.timeMs != null }?.timeMs
        if (start != null && sameTrack) {
            haptics.click()
            PlayerController.seekTo((start - PreviewLeadMs).coerceAtLeast(0L))
            if (!PlayerController.isPlaying) PlayerController.togglePlayPause()
        }
    }
    val save: () -> Unit = {
        if (!saving) {
            saving = true
            scope.launch {
                val output = LyricsTagWriter.withCredits(LyricsTagWriter.buildText(lines.toList()), track, base?.source ?: LyricsTagWriter.MAKER_SOURCE)
                val ok = LyricsTagWriter.write(context, track.uri, output)
                saving = false
                if (ok) {
                    LyricsRepository.update(track.id, output)
                    haptics.confirm()
                    Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                    onClose()
                } else {
                    haptics.reject()
                    Toast.makeText(context, saveFailedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) { detectTapGestures { } }
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 8.dp)
            ) {
                KateIconButton(onClick = { if (step == MakerStep.SYNC) step = MakerStep.TEXT else requestClose() }) {
                    KateIcon(
                        imageVector = if (step == MakerStep.SYNC) Icons.AutoMirrored.Filled.ArrowBack else Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.folder_picker_close),
                        tint = buttonTint(colors.onSurface)
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(
                        text = stringResource(R.string.sync_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (step == MakerStep.TEXT) {
                    KateTextButton(
                        label = stringResource(R.string.sync_next),
                        role = DialogButtonRole.MAIN,
                        enabled = text.isNotBlank(),
                        onClick = goToSync
                    )
                } else {
                    KateTextButton(
                        label = stringResource(if (saving) R.string.sync_saving else R.string.sync_save),
                        role = DialogButtonRole.MAIN,
                        enabled = !saving && lines.isNotEmpty(),
                        onClick = save
                    )
                }
            }
            if (step == MakerStep.TEXT) {
                TextStep(
                    onSearch = {
                        LrcLibRequest.open(track) { found ->
                            base = found
                            text = initialText(found)
                            lines.clear()
                            history.clear()
                            dirty = true
                        }
                    },
                    text = text,
                    onTextChange = {
                        text = it
                        dirty = true
                    },
                    onPaste = {
                        val clip = context.getSystemService(ClipboardManager::class.java)?.primaryClip
                        val pasted = clip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()
                        if (!pasted.isNullOrBlank()) {
                            text = if (text.isBlank()) pasted else text.trimEnd() + "\n" + pasted
                            dirty = true
                            haptics.click()
                        }
                    },
                    onNext = goToSync,
                    modifier = Modifier.weight(1f)
                )
            } else {
                OptionsRow(
                    mode = mode,
                    onMode = {
                        mode = it
                        wordTarget = 0
                        holding = false
                        haptics.click()
                    },
                    enabled = sameTrack,
                    onSpeed = {
                        haptics.click()
                        PlayerController.setSpeed(it)
                    }
                )
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val wide = maxWidth >= 600.dp && maxWidth > maxHeight
                    val list: @Composable (Modifier) -> Unit = { listModifier ->
                        SyncLineList(
                            lines = lines,
                            target = target,
                            wordTarget = wordTarget,
                            mode = mode,
                            onSelect = {
                                if (it != target) wordTarget = 0
                                target = it
                                haptics.tick()
                            },
                            onSelectWord = {
                                wordTarget = it
                                haptics.tick()
                            },
                            onNudge = nudge,
                            onPlayFrom = playFrom,
                            onClearLine = clearLine,
                            modifier = listModifier
                        )
                    }
                    val controls: @Composable (Modifier) -> Unit = { controlsModifier ->
                        SyncControls(
                            track = track,
                            lines = lines,
                            sameTrack = sameTrack,
                            target = target,
                            wordTarget = wordTarget,
                            mode = mode,
                            canUndo = history.isNotEmpty(),
                            onDown = pressDown,
                            onUp = pressUp,
                            onUndo = undo,
                            onClear = {
                                history.clear()
                                for (index in lines.indices) lines[index] = lines[index].copy(timeMs = null, words = null, ends = null)
                                target = 0
                                wordTarget = 0
                                dirty = true
                            },
                            modifier = controlsModifier
                        )
                    }
                    if (wide) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            list(Modifier.weight(1f).fillMaxHeight())
                            controls(Modifier.weight(0.8f).fillMaxHeight())
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            list(Modifier.weight(1f).fillMaxWidth())
                            controls(Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
        DiscardDialog(
            visible = confirmDiscard,
            onKeep = { confirmDiscard = false },
            onDiscard = {
                confirmDiscard = false
                onClose()
            }
        )
    }
}

@Composable
private fun OptionsRow(
    mode: SyncMode,
    onMode: (SyncMode) -> Unit,
    enabled: Boolean,
    onSpeed: (Float) -> Unit
) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        KateChip(
            label = stringResource(R.string.sync_mode_line),
            selected = mode == SyncMode.LINE,
            onClick = { onMode(SyncMode.LINE) }
        )
        KateChip(
            label = stringResource(R.string.sync_mode_word),
            selected = mode == SyncMode.WORD,
            onClick = { onMode(SyncMode.WORD) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.sync_speed),
            style = MaterialTheme.typography.labelLarge,
            color = colors.secondaryText
        )
        SpeedChoices.forEach { speed ->
            KateChip(
                label = speedLabel(speed),
                selected = abs(PlayerController.playbackSpeed - speed) < 0.01f,
                onClick = { if (enabled) onSpeed(speed) }
            )
        }
    }
}

@Composable
private fun TextStep(
    onSearch: () -> Unit,
    text: String,
    onTextChange: (String) -> Unit,
    onPaste: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.sync_text_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text(stringResource(R.string.sync_text_placeholder), color = colors.secondaryText) },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onSurface,
                unfocusedTextColor = colors.onSurface,
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.divider,
                cursorColor = colors.primary
            ),
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 12.dp)
        )
        KateActionButton(
            label = stringResource(R.string.lrclib_search_button),
            icon = Icons.Outlined.TravelExplore,
            primary = false,
            allowIconOnly = false,
            onClick = onSearch,
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(top = 12.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(vertical = 12.dp)
        ) {
            KateActionButton(
                label = stringResource(R.string.sync_paste),
                icon = Icons.Filled.ContentPaste,
                primary = false,
                onClick = onPaste,
                modifier = Modifier.weight(1f)
            )
            KateActionButton(
                label = stringResource(R.string.sync_next),
                enabled = text.isNotBlank(),
                allowIconOnly = false,
                onClick = onNext,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun rememberPlaybackNow(): Long {
    var now by remember { mutableLongStateOf(PlayerController.estimatedPositionMs()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = PlayerController.estimatedPositionMs()
            delay(60L)
        }
    }
    return now
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SyncLineList(
    lines: List<DraftLine>,
    target: Int,
    wordTarget: Int,
    mode: SyncMode,
    onSelect: (Int) -> Unit,
    onSelectWord: (Int) -> Unit,
    onNudge: (Int, Long) -> Unit,
    onPlayFrom: (Int) -> Unit,
    onClearLine: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val listState = rememberLazyListState()
    val now = rememberPlaybackNow()
    val playing = lines.indexOfLast { it.timeMs != null && it.timeMs <= now }
    LaunchedEffect(target) {
        if (target in lines.indices) listState.animateScrollToItem((target - 2).coerceAtLeast(0))
    }
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        itemsIndexed(lines) { index, line ->
            val selected = index == target
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .kateChoice(selected, unselected = Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatStamp(line.timeMs),
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        color = if (line.timeMs == null) colors.secondaryText else colors.primary,
                        modifier = Modifier.width(84.dp)
                    )
                    Text(
                        text = line.text,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selected || index == playing) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == playing && !selected) colors.primary else kateChoiceText(selected),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (selected) {
                    val words = LyricsTagWriter.wordsOf(line.text)
                    val starts = line.words?.takeIf { it.size == words.size }
                    if (mode == SyncMode.WORD && words.isNotEmpty()) {
                        val playingWord = starts?.indexOfLast { it != null && it <= now } ?: -1
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp, end = 8.dp)
                        ) {
                            words.forEachIndexed { wordIndex, word ->
                                val timed = starts?.getOrNull(wordIndex) != null
                                KateChip(
                                    label = word,
                                    selected = wordIndex == wordTarget || (wordIndex == playingWord && index == playing),
                                    accentOutline = timed && wordIndex != wordTarget && !(wordIndex == playingWord && index == playing),
                                    onClick = { onSelectWord(wordIndex) }
                                )
                            }
                        }
                        val chosen = starts?.getOrNull(wordTarget)
                        Text(
                            text = stringResource(
                                R.string.sync_word_time,
                                words.getOrElse(wordTarget) { "" },
                                formatStamp(chosen)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.secondaryText,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        KateIconButton(onClick = { onNudge(index, -NudgeMs) }) {
                            KateIcon(imageVector = Icons.Filled.Remove, contentDescription = stringResource(R.string.sync_earlier), tint = buttonTint(colors.onSurface))
                        }
                        KateIconButton(onClick = { onNudge(index, NudgeMs) }) {
                            KateIcon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.sync_later), tint = buttonTint(colors.onSurface))
                        }
                        KateIconButton(onClick = { onPlayFrom(index) }) {
                            KateIcon(imageVector = Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.sync_play_from), tint = buttonTint(colors.onSurface))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        KateIconButton(onClick = { onClearLine(index) }) {
                            KateIcon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.sync_clear_line), tint = buttonTint(colors.secondaryText))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncControls(
    track: Track,
    lines: List<DraftLine>,
    sameTrack: Boolean,
    target: Int,
    wordTarget: Int,
    mode: SyncMode,
    canUndo: Boolean,
    onDown: () -> Unit,
    onUp: () -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val now = rememberPlaybackNow()
    val total = lines.size
    val done = target >= total
    val currentWords = lines.getOrNull(target)?.let { LyricsTagWriter.wordsOf(it.text) }.orEmpty()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        if (!sameTrack) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary.copy(alpha = 0.12f))
                    .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.sync_other_song),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                KateTextButton(
                    label = stringResource(R.string.sync_play_song),
                    role = DialogButtonRole.LINK,
                    onClick = { PlayerController.playTrack(context, listOf(track), track) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            KateControlButton(
                icon = Icons.Filled.FastRewind,
                contentDescription = stringResource(R.string.sync_back_5),
                iconSize = 28.dp,
                enabled = sameTrack,
                onClick = {
                    haptics.click()
                    PlayerController.seekTo((PlayerController.estimatedPositionMs() - SkipMs).coerceAtLeast(0L))
                }
            )
            KatePlayButton(
                isPlaying = PlayerController.isPlaying && sameTrack,
                baseSize = 60.dp,
                followSize = false,
                allowGlow = false,
                enabled = sameTrack,
                onClick = {
                    haptics.click()
                    PlayerController.togglePlayPause()
                }
            )
            KateControlButton(
                icon = Icons.Filled.FastForward,
                contentDescription = stringResource(R.string.sync_forward_5),
                iconSize = 28.dp,
                enabled = sameTrack,
                onClick = {
                    haptics.click()
                    PlayerController.seekTo(PlayerController.estimatedPositionMs() + SkipMs)
                }
            )
        }
        Text(
            text = formatPlaybackTime(if (sameTrack) now else 0L) + " / " + formatPlaybackTime(track.durationMs),
            style = MaterialTheme.typography.labelMedium,
            color = colors.secondaryText,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(top = 10.dp)
        ) {
            KateIconButton(onClick = onUndo, enabled = canUndo) {
                KateIcon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = stringResource(R.string.sync_undo),
                    tint = if (canUndo) buttonTint(colors.onSurface) else colors.secondaryText.copy(alpha = 0.4f)
                )
            }
            val label = when {
                done -> stringResource(R.string.sync_tap_done)
                mode == SyncMode.WORD && currentWords.isNotEmpty() ->
                    stringResource(R.string.sync_hold_word, currentWords.getOrElse(wordTarget) { currentWords.last() })
                else -> stringResource(R.string.sync_tap_line, target + 1, total)
            }
            TapPad(
                label = label,
                enabled = sameTrack && !done,
                onDown = onDown,
                onUp = onUp,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().padding(top = 6.dp)
        ) {
            Text(
                text = stringResource(if (mode == SyncMode.WORD) R.string.sync_word_hint else R.string.sync_tap_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.weight(1f)
            )
            KateTextButton(
                label = stringResource(R.string.sync_clear),
                role = DialogButtonRole.LINK,
                onClick = onClear
            )
        }
    }
}

@Composable
private fun TapPad(
    label: String,
    enabled: Boolean,
    onDown: () -> Unit,
    onUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val shape = actionShape(LocalLookAndFeel.current.actionShape)
    var pressed by remember { mutableStateOf(false) }
    val currentDown by rememberUpdatedState(onDown)
    val currentUp by rememberUpdatedState(onUp)
    val scale by animateFloatAsState(targetValue = if (pressed) 0.96f else 1f, label = "tap_pad_scale")
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(
                when {
                    !enabled -> colors.surfaceVariant
                    pressed -> colors.primary.copy(alpha = 0.8f)
                    else -> colors.primary
                }
            )
            .semantics {
                role = Role.Button
                onClick {
                    if (enabled) {
                        currentDown()
                        currentUp()
                    }
                    enabled
                }
            }
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            pressed = true
                            currentDown()
                            tryAwaitRelease()
                            pressed = false
                            currentUp()
                        }
                    )
                }
            }
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (enabled) colors.onPrimary else colors.secondaryText,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DiscardDialog(visible: Boolean, onKeep: () -> Unit, onDiscard: () -> Unit) {
    val colors = LocalClintColors.current
    KateDialog(visible = visible, onDismiss = onKeep) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(R.string.sync_discard_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.sync_discard_message),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.sync_keep_editing),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onKeep
                )
                KateTextButton(
                    label = stringResource(R.string.sync_discard),
                    role = DialogButtonRole.DESTRUCTIVE,
                    onClick = onDiscard
                )
            }
        }
    }
}
