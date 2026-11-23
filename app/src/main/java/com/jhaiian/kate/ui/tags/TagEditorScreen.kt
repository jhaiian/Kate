package com.jhaiian.kate.ui.tags

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.CoverEdit
import com.jhaiian.kate.data.TagEditor
import com.jhaiian.kate.data.TagField
import com.jhaiian.kate.data.TagMatch
import com.jhaiian.kate.data.TagSnapshot
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
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TagEditorRequest {
    var queue by mutableStateOf<List<Track>>(emptyList())
        private set
    var index by mutableIntStateOf(0)
        private set
    var session by mutableIntStateOf(0)
        private set
    val current: Track? get() = queue.getOrNull(index)

    fun open(tracks: List<Track>) {
        val unique = tracks.distinctBy { it.id }
        if (unique.isEmpty()) return
        queue = unique
        index = 0
        session++
    }

    fun next() {
        if (index < queue.lastIndex) {
            index++
            session++
        } else {
            close()
        }
    }

    fun close() {
        queue = emptyList()
        index = 0
    }
}

@Composable
fun TagEditorHost() {
    val track = TagEditorRequest.current
    AnimatedVisibility(
        visible = track != null,
        enter = fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 8 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 10 }
    ) {
        if (track != null) {
            key(TagEditorRequest.session) {
                TagEditorContent(
                    track = track,
                    position = TagEditorRequest.index + 1,
                    total = TagEditorRequest.queue.size,
                    onDone = { TagEditorRequest.next() },
                    onClose = { TagEditorRequest.close() }
                )
            }
        }
    }
}

private val TrioFields = listOf(TagField.YEAR, TagField.TRACK, TagField.DISC)

@Composable
private fun fieldLabel(field: TagField): String = stringResource(
    when (field) {
        TagField.TITLE -> R.string.tag_field_title
        TagField.ARTIST -> R.string.tag_field_artist
        TagField.ALBUM -> R.string.tag_field_album
        TagField.ALBUM_ARTIST -> R.string.tag_field_album_artist
        TagField.GENRE -> R.string.tag_field_genre
        TagField.YEAR -> R.string.tag_field_year
        TagField.TRACK -> R.string.tag_field_track
        TagField.DISC -> R.string.tag_field_disc
        TagField.COMPOSER -> R.string.tag_field_composer
        TagField.COMMENT -> R.string.tag_field_comment
        TagField.LYRICS -> R.string.tag_field_lyrics
    }
)

@Composable
private fun TagEditorContent(track: Track, position: Int, total: Int, onDone: () -> Unit, onClose: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    var reading by remember { mutableStateOf(true) }
    var snapshot by remember { mutableStateOf<TagSnapshot?>(null) }
    LaunchedEffect(Unit) {
        snapshot = TagEditor.read(context, track.uri)
        reading = false
    }
    val loaded = snapshot
    val original = remember(loaded) { loaded?.values.orEmpty() }
    val originalCover = loaded?.cover
    val edited = remember { mutableStateMapOf<TagField, String>() }
    var cover by remember { mutableStateOf<CoverEdit>(CoverEdit.Keep) }
    var lookupOpen by remember { mutableStateOf(false) }
    var chosenMatch by remember { mutableStateOf<TagMatch?>(null) }
    var saving by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var skipAfterDiscard by remember { mutableStateOf(false) }
    val hasNext = position < total

    val changes = edited.filter { (field, value) -> original[field]?.trim() != value.trim() }
    val dirty = changes.isNotEmpty() || cover !is CoverEdit.Keep
    val valueOf: (TagField) -> String = { field -> edited[field] ?: original[field].orEmpty() }

    val coverFailed = stringResource(R.string.tag_cover_failed)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val raw = withContext(Dispatchers.IO) {
                    runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
                }
                val prepared = raw?.let { TagEditor.prepareCover(it) }
                if (prepared != null) {
                    haptics.click()
                    cover = CoverEdit.Replace(prepared)
                } else {
                    haptics.reject()
                    Toast.makeText(context, coverFailed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val savedMessage = stringResource(R.string.tag_editor_saved)
    val saveFailedMessage = stringResource(R.string.tag_editor_save_failed)
    val save: () -> Unit = {
        if (!saving && dirty && loaded != null) {
            saving = true
            val pending = changes.toMap()
            val coverEdit = cover
            scope.launch {
                val ok = TagEditor.save(context, track, pending, coverEdit)
                saving = false
                if (ok) {
                    haptics.confirm()
                    Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                    onDone()
                } else {
                    haptics.reject()
                    Toast.makeText(context, saveFailedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    val back: () -> Unit = {
        when {
            chosenMatch != null -> chosenMatch = null
            lookupOpen -> lookupOpen = false
            dirty && !saving -> {
                skipAfterDiscard = false
                confirmDiscard = true
            }
            !saving -> onClose()
        }
    }
    val skip: () -> Unit = {
        when {
            saving -> Unit
            dirty -> {
                skipAfterDiscard = true
                confirmDiscard = true
            }
            else -> onDone()
        }
    }
    BackHandler(onBack = back)

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
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
            ) {
                KateIconButton(onClick = back) {
                    KateIcon(
                        imageVector = if (lookupOpen) Icons.AutoMirrored.Filled.ArrowBack else Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.folder_picker_close),
                        tint = buttonTint(colors.onSurface)
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(
                        text = stringResource(if (lookupOpen) R.string.tag_lookup_title else R.string.tag_editor_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = (listOfNotNull(stringResource(R.string.tag_editor_progress, position, total).takeIf { total > 1 }) +
                            listOf(track.title, track.artist).filter { it.isNotBlank() }).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!lookupOpen && hasNext) {
                    KateActionButton(
                        label = stringResource(R.string.tag_editor_skip),
                        icon = Icons.Filled.SkipNext,
                        primary = false,
                        enabled = !saving,
                        compact = true,
                        onClick = skip,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                if (!lookupOpen && loaded != null) {
                    KateActionButton(
                        label = stringResource(if (saving) R.string.tag_editor_saving else R.string.tag_editor_save),
                        icon = Icons.Filled.Check,
                        enabled = dirty && !saving,
                        compact = true,
                        allowIconOnly = false,
                        onClick = save
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                when {
                    reading -> LoadingState(stringResource(R.string.tag_editor_loading))
                    loaded == null -> MessageState(stringResource(R.string.tag_editor_unreadable))
                    lookupOpen -> TagLookupPane(
                        track = track,
                        initialTitle = valueOf(TagField.TITLE).ifBlank { track.filename.substringBeforeLast('.') },
                        initialArtist = valueOf(TagField.ARTIST),
                        album = valueOf(TagField.ALBUM),
                        currentValue = valueOf,
                        currentCover = when (val current = cover) {
                            is CoverEdit.Replace -> current.bytes
                            CoverEdit.Remove -> null
                            CoverEdit.Keep -> originalCover
                        },
                        chosen = chosenMatch,
                        onChoose = { chosenMatch = it },
                        onApply = { values, bytes ->
                            values.forEach { (field, value) -> edited[field] = value }
                            if (bytes != null) cover = CoverEdit.Replace(bytes)
                            chosenMatch = null
                            lookupOpen = false
                        }
                    )
                    else -> EditorBody(
                        track = track,
                        valueOf = valueOf,
                        isEdited = { field -> field in changes },
                        onValueChange = { field, value -> edited[field] = value },
                        onReset = { field -> edited.remove(field) },
                        cover = cover,
                        originalCover = originalCover,
                        onPickCover = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onRemoveCover = {
                            haptics.click()
                            cover = CoverEdit.Remove
                        },
                        onUndoCover = { cover = CoverEdit.Keep },
                        onLookup = {
                            haptics.click()
                            lookupOpen = true
                        }
                    )
                }
            }
        }
        DiscardDialog(
            visible = confirmDiscard,
            onDiscard = {
                confirmDiscard = false
                if (skipAfterDiscard) onDone() else onClose()
            },
            onDismiss = { confirmDiscard = false }
        )
    }
}

@Composable
private fun EditorBody(
    track: Track,
    valueOf: (TagField) -> String,
    isEdited: (TagField) -> Boolean,
    onValueChange: (TagField, String) -> Unit,
    onReset: (TagField) -> Unit,
    cover: CoverEdit,
    originalCover: ByteArray?,
    onPickCover: () -> Unit,
    onRemoveCover: () -> Unit,
    onUndoCover: () -> Unit,
    onLookup: () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val wide = width >= 640.dp
        val coverPanel: @Composable (Dp) -> Unit = { size ->
            CoverPanel(
                size = size,
                cover = cover,
                originalCover = originalCover,
                onPick = onPickCover,
                onRemove = onRemoveCover,
                onUndo = onUndoCover,
                onLookup = onLookup
            )
        }
        val fieldsPanel: @Composable () -> Unit = {
            FieldsPanel(
                track = track,
                valueOf = valueOf,
                isEdited = isEdited,
                onValueChange = onValueChange,
                onReset = onReset
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (wide) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.widthIn(max = 1000.dp).fillMaxWidth()
                ) {
                    Box(modifier = Modifier.width(280.dp)) { coverPanel(280.dp) }
                    Box(modifier = Modifier.weight(1f)) { fieldsPanel() }
                }
            } else {
                val coverSize = (width * 0.6f).coerceAtMost(240.dp)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = 640.dp).fillMaxWidth()
                ) {
                    coverPanel(coverSize)
                    Box(modifier = Modifier.padding(top = 16.dp)) { fieldsPanel() }
                }
            }
        }
    }
}

@Composable
private fun CoverPanel(
    size: Dp,
    cover: CoverEdit,
    originalCover: ByteArray?,
    onPick: () -> Unit,
    onRemove: () -> Unit,
    onUndo: () -> Unit,
    onLookup: () -> Unit
) {
    val colors = LocalClintColors.current
    val bytes = when (cover) {
        is CoverEdit.Replace -> cover.bytes
        CoverEdit.Remove -> null
        CoverEdit.Keep -> originalCover
    }
    val preview = rememberPreview(bytes, 900)
    val caption = when {
        cover is CoverEdit.Remove -> stringResource(R.string.tag_cover_removed)
        bytes == null -> stringResource(R.string.tag_cover_none)
        else -> remember(bytes) { TagEditor.sizeLabel(bytes) }.orEmpty()
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surfaceVariant)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.5f)), RoundedCornerShape(20.dp))
        ) {
            if (preview != null) {
                Image(bitmap = preview, contentDescription = stringResource(R.string.tag_cover), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                KateIcon(imageVector = Icons.Filled.Album, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(size / 3))
            }
        }
        if (caption.isNotBlank()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().padding(top = 10.dp)
        ) {
            KateActionButton(
                label = stringResource(R.string.tag_cover_change),
                icon = Icons.Filled.Image,
                primary = false,
                compact = true,
                allowIconOnly = false,
                onClick = onPick,
                modifier = Modifier.weight(1f)
            )
            if (cover !is CoverEdit.Keep) {
                KateActionButton(
                    label = stringResource(R.string.tag_editor_reset),
                    icon = Icons.AutoMirrored.Filled.Undo,
                    primary = false,
                    compact = true,
                    allowIconOnly = false,
                    onClick = onUndo,
                    modifier = Modifier.weight(1f)
                )
            } else if (bytes != null) {
                KateActionButton(
                    label = stringResource(R.string.tag_cover_remove),
                    icon = Icons.Filled.Delete,
                    primary = false,
                    compact = true,
                    allowIconOnly = false,
                    onClick = onRemove,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        KateActionButton(
            label = stringResource(R.string.tag_lookup_button),
            icon = Icons.Outlined.TravelExplore,
            primary = true,
            allowIconOnly = false,
            onClick = onLookup,
            modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth().padding(top = 10.dp)
        )
    }
}

@Composable
private fun FieldsPanel(
    track: Track,
    valueOf: (TagField) -> String,
    isEdited: (TagField) -> Boolean,
    onValueChange: (TagField, String) -> Unit,
    onReset: (TagField) -> Unit
) {
    val colors = LocalClintColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        val field: @Composable (TagField, Modifier) -> Unit = { item, modifier ->
            TagTextField(
                field = item,
                value = valueOf(item),
                edited = isEdited(item),
                onValueChange = { onValueChange(item, it) },
                onReset = { onReset(item) },
                modifier = modifier
            )
        }
        var trioShown = false
        TagField.entries.forEach { item ->
            if (item in TrioFields) {
                if (!trioShown) {
                    trioShown = true
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        TrioFields.forEach { part -> field(part, Modifier.weight(1f)) }
                    }
                }
            } else {
                field(item, Modifier.fillMaxWidth())
            }
        }
        Text(
            text = listOf(track.filename, track.format).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun TagTextField(
    field: TagField,
    value: String,
    edited: Boolean,
    onValueChange: (String) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier
) {
    val colors = LocalClintColors.current
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onValueChange(if (field.numeric) text.filter { it.isDigit() }.take(4) else text) },
        label = { Text(fieldLabel(field), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingIcon = if (edited) {
            {
                KateIconButton(onClick = onReset) {
                    KateIcon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = stringResource(R.string.tag_editor_reset),
                        tint = colors.primary
                    )
                }
            }
        } else {
            null
        },
        singleLine = !field.multiLine,
        minLines = when (field) {
            TagField.LYRICS -> 6
            TagField.COMMENT -> 2
            else -> 1
        },
        maxLines = when (field) {
            TagField.LYRICS -> 16
            TagField.COMMENT -> 6
            else -> 1
        },
        keyboardOptions = KeyboardOptions(
            capitalization = if (field.numeric) KeyboardCapitalization.None else KeyboardCapitalization.Words,
            keyboardType = if (field.numeric) KeyboardType.Number else KeyboardType.Text
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = if (edited) colors.primary.copy(alpha = 0.6f) else colors.divider,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = if (edited) colors.primary else colors.secondaryText,
            focusedSupportingTextColor = colors.secondaryText,
            unfocusedSupportingTextColor = colors.secondaryText,
            cursorColor = colors.primary
        ),
        modifier = modifier
    )
}

@Composable
internal fun rememberPreview(bytes: ByteArray?, targetPx: Int): ImageBitmap? {
    val image by produceState<ImageBitmap?>(initialValue = null, bytes) {
        value = if (bytes == null) null else withContext(Dispatchers.Default) { TagEditor.decodePreview(bytes, targetPx)?.asImageBitmap() }
    }
    return image
}

@Composable
internal fun LoadingState(message: String) {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(top = 64.dp)
    ) {
        CircularProgressIndicator(color = colors.primary)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp)
        )
    }
}

@Composable
private fun MessageState(message: String) {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp)
    ) {
        KateIcon(imageVector = Icons.Outlined.ErrorOutline, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(56.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun DiscardDialog(visible: Boolean, onDiscard: () -> Unit, onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(R.string.tag_editor_discard_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.tag_editor_discard_message),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.tag_editor_keep_editing),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.tag_editor_discard_confirm),
                    role = DialogButtonRole.DESTRUCTIVE,
                    onClick = onDiscard
                )
            }
        }
    }
}
