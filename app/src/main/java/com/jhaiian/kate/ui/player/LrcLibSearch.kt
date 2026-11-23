package com.jhaiian.kate.ui.player

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LrcLib
import com.jhaiian.kate.data.LrcLibRecord
import com.jhaiian.kate.data.LrcLibResult
import com.jhaiian.kate.data.LyricsSource
import com.jhaiian.kate.data.LrcParser
import com.jhaiian.kate.data.Lyrics
import com.jhaiian.kate.data.LyricsRepository
import com.jhaiian.kate.data.LyricsTagWriter
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.launch

object LrcLibRequest {
    var track by mutableStateOf<Track?>(null)
        private set
    var onPick by mutableStateOf<((Lyrics) -> Unit)?>(null)
        private set
    var session by mutableIntStateOf(0)
        private set

    fun open(track: Track, onPick: ((Lyrics) -> Unit)? = null) {
        this.track = track
        this.onPick = onPick
        session++
    }

    fun close() {
        track = null
        onPick = null
    }
}

private sealed interface SearchState {
    data object Consent : SearchState
    data object Loading : SearchState
    data class Done(val result: LrcLibResult) : SearchState
}

@Composable
fun LrcLibSearchHost() {
    val track = LrcLibRequest.track
    AnimatedVisibility(
        visible = track != null,
        enter = fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 8 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 10 }
    ) {
        if (track != null) {
            key(LrcLibRequest.session) {
                LrcLibSearchContent(
                    track = track,
                    onPick = LrcLibRequest.onPick,
                    onClose = { LrcLibRequest.close() }
                )
            }
        }
    }
}

@Composable
private fun sourceLabel(record: LrcLibRecord): String = when (record.source) {
    LyricsSource.AMLL -> record.credit?.let { stringResource(R.string.online_source_amll_by, it) } ?: stringResource(R.string.online_source_amll)
    LyricsSource.LRCRED -> stringResource(R.string.online_source_lrcred)
    LyricsSource.LYRICSPLUS -> stringResource(R.string.online_source_lyricsplus)
    LyricsSource.NETEASE -> stringResource(R.string.online_source_netease)
    LyricsSource.KUGOU -> stringResource(R.string.online_source_kugou)
    LyricsSource.LRCLIB -> stringResource(R.string.online_source_lrclib)
}

private fun badgeFor(record: LrcLibRecord): Int = when {
    record.hasWords -> R.string.lrclib_badge_words
    record.hasSynced -> R.string.lrclib_badge_synced
    else -> R.string.lrclib_badge_plain
}

private fun formatSeconds(seconds: Double): String {
    val total = seconds.toLong().coerceAtLeast(0L)
    return "%d:%02d".format(java.util.Locale.US, total / 60, total % 60)
}

@Composable
private fun LrcLibSearchContent(track: Track, onPick: ((Lyrics) -> Unit)?, onClose: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<SearchState>(if (LrcLib.isEnabled(context)) SearchState.Loading else SearchState.Consent) }
    var attempt by remember { mutableIntStateOf(0) }
    var chosen by remember { mutableStateOf<LrcLibRecord?>(null) }
    var saving by remember { mutableStateOf(false) }
    val savedMessage = stringResource(R.string.sync_saved)
    val saveFailedMessage = stringResource(R.string.sync_save_failed)

    LaunchedEffect(state is SearchState.Loading, attempt) {
        if (state is SearchState.Loading) state = SearchState.Done(LrcLib.find(context, track))
    }
    BackHandler {
        if (chosen != null) chosen = null else onClose()
    }

    val save: (LrcLibRecord) -> Unit = { record ->
        val text = record.bestText?.let { LyricsTagWriter.withCredits(it, track, record.source.creditName) }
        if (text != null && !saving) {
            saving = true
            scope.launch {
                val ok = LyricsTagWriter.write(context, track.uri, text)
                saving = false
                if (ok) {
                    LyricsRepository.update(track.id, text)
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
    val edit: (LrcLibRecord) -> Unit = { record ->
        val parsed = record.bestText?.let { LrcParser.parse(it) }?.copy(source = record.source.creditName)
        if (parsed != null) {
            haptics.click()
            onClose()
            if (onPick != null) onPick(parsed) else LyricsMakerRequest.open(track, parsed)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) { detectTapGestures { } }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 8.dp)
            ) {
                KateIconButton(onClick = { if (chosen != null) chosen = null else onClose() }) {
                    KateIcon(
                        imageVector = if (chosen != null) Icons.AutoMirrored.Filled.ArrowBack else Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.folder_picker_close),
                        tint = buttonTint(colors.onSurface)
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(
                        text = stringResource(R.string.lrclib_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOf(track.title, track.artist).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                val record = chosen
                val current = state
                when {
                    record != null -> PreviewPane(
                        record = record,
                        saving = saving,
                        pickMode = onPick != null,
                        onSave = { save(record) },
                        onEdit = { edit(record) }
                    )
                    current is SearchState.Consent -> Unit
                    current is SearchState.Loading -> Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp)
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                        Text(
                            text = stringResource(R.string.lrclib_searching),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.secondaryText,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    current is SearchState.Done -> when (val result = current.result) {
                        is LrcLibResult.Found -> ResultList(records = result.records, onChoose = {
                            haptics.tick()
                            chosen = it
                        })
                        else -> EmptyState(
                            message = when (result) {
                                is LrcLibResult.RateLimited -> pluralStringResource(R.plurals.lrclib_rate_limited, result.retryAfterSeconds, result.retryAfterSeconds)
                                LrcLibResult.Offline -> stringResource(R.string.lrclib_offline)
                                else -> stringResource(R.string.lrclib_not_found)
                            },
                            onRetry = {
                                attempt++
                                state = SearchState.Loading
                            }
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.lrclib_credit),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
        LrcLibConsentDialog(
            visible = state is SearchState.Consent,
            onConfirm = {
                LrcLib.setEnabled(context, true)
                state = SearchState.Loading
            },
            onDismiss = onClose
        )
    }
}

@Composable
private fun ResultList(records: List<LrcLibRecord>, onChoose: (LrcLibRecord) -> Unit) {
    val colors = LocalClintColors.current
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        items(records, key = { it.key }) { record ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.cardBackground)
                    .clickable { onChoose(record) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.trackName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOf(
                            sourceLabel(record),
                            record.artistName,
                            record.albumName,
                            if (record.durationSeconds > 0) formatSeconds(record.durationSeconds) else ""
                        )
                            .filter { it.isNotBlank() }
                            .joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = stringResource(badgeFor(record)),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (record.hasSynced) colors.primary else colors.secondaryText,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .kateChoice(record.hasSynced, chip = true)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PreviewPane(
    record: LrcLibRecord,
    saving: Boolean,
    pickMode: Boolean,
    onSave: () -> Unit,
    onEdit: () -> Unit
) {
    val colors = LocalClintColors.current
    val preview = remember(record.id) {
        LrcParser.parse(record.bestText.orEmpty())?.lines?.map { it.text }?.filter { it.isNotBlank() }.orEmpty()
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(badgeFor(record)),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
            preview.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(vertical = 12.dp)
        ) {
            KateActionButton(
                label = stringResource(if (pickMode) R.string.lrclib_use else R.string.lrclib_edit),
                icon = Icons.Filled.Edit,
                primary = pickMode,
                allowIconOnly = false,
                onClick = onEdit,
                modifier = Modifier.weight(1f)
            )
            if (!pickMode) {
                KateActionButton(
                    label = stringResource(if (saving) R.string.sync_saving else R.string.lrclib_save),
                    icon = Icons.Filled.Save,
                    enabled = !saving,
                    allowIconOnly = false,
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, onRetry: () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp)
    ) {
        KateIcon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(56.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        KateActionButton(
            label = stringResource(R.string.lrclib_retry),
            icon = Icons.Filled.Refresh,
            primary = false,
            allowIconOnly = false,
            onClick = onRetry,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}

@Composable
internal fun LrcLibConsentDialog(visible: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(R.string.lrclib_consent_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.lrclib_consent_message),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_cancel),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.lrclib_consent_confirm),
                    role = DialogButtonRole.MAIN,
                    onClick = onConfirm
                )
            }
        }
    }
}
