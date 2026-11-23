package com.jhaiian.kate.ui.tags

import android.util.LruCache
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.TagEditor
import com.jhaiian.kate.data.TagField
import com.jhaiian.kate.data.TagLookup
import com.jhaiian.kate.data.TagLookupResult
import com.jhaiian.kate.data.TagMatch
import com.jhaiian.kate.data.TagSource
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val CompareFields = listOf(
    TagField.TITLE,
    TagField.ARTIST,
    TagField.ALBUM,
    TagField.ALBUM_ARTIST,
    TagField.GENRE,
    TagField.YEAR,
    TagField.TRACK,
    TagField.DISC
)

private val ThumbCache = LruCache<String, ImageBitmap>(64)

private sealed interface LookupState {
    data object Consent : LookupState
    data object Loading : LookupState
    data class Done(val result: TagLookupResult) : LookupState
}

@Composable
private fun sourceName(source: TagSource): String = stringResource(
    when (source) {
        TagSource.MUSICBRAINZ -> R.string.tag_source_musicbrainz
        TagSource.DEEZER -> R.string.tag_source_deezer
        TagSource.ITUNES -> R.string.tag_source_itunes
    }
)

private fun formatDuration(ms: Long): String {
    val total = (ms / 1_000L).coerceAtLeast(0L)
    return "%d:%02d".format(java.util.Locale.US, total / 60, total % 60)
}

@Composable
internal fun TagLookupPane(
    track: Track,
    initialTitle: String,
    initialArtist: String,
    album: String,
    currentValue: (TagField) -> String,
    currentCover: ByteArray?,
    chosen: TagMatch?,
    onChoose: (TagMatch?) -> Unit,
    onApply: (Map<TagField, String>, ByteArray?) -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    var title by remember { mutableStateOf(initialTitle) }
    var artist by remember { mutableStateOf(initialArtist) }
    var attempt by remember { mutableIntStateOf(0) }
    var state by remember {
        mutableStateOf<LookupState>(if (TagLookup.isEnabled(context)) LookupState.Loading else LookupState.Consent)
    }
    LaunchedEffect(state is LookupState.Loading, attempt) {
        if (state is LookupState.Loading) {
            state = LookupState.Done(TagLookup.search(context, title, artist, album, track.durationMs))
        }
    }
    val search: () -> Unit = {
        if (title.isNotBlank() && state !is LookupState.Consent) {
            haptics.click()
            attempt++
            state = LookupState.Loading
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        val match = chosen
        if (match != null) {
            ComparePane(match = match, currentValue = currentValue, currentCover = currentCover, onApply = onApply)
        } else {
            SearchBar(
                title = title,
                artist = artist,
                onTitleChange = { title = it },
                onArtistChange = { artist = it },
                onSearch = search
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                when (val current = state) {
                    LookupState.Consent -> Unit
                    LookupState.Loading -> LoadingState(stringResource(R.string.tag_lookup_searching))
                    is LookupState.Done -> when (val result = current.result) {
                        is TagLookupResult.Found -> MatchList(matches = result.matches, onChoose = {
                            haptics.tick()
                            onChoose(it)
                        })
                        else -> EmptyLookup(
                            message = stringResource(
                                if (result is TagLookupResult.Offline) R.string.tag_lookup_offline else R.string.tag_lookup_not_found
                            ),
                            onRetry = search
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.tag_lookup_credit),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
    TagLookupConsentDialog(
        visible = state is LookupState.Consent,
        onConfirm = {
            TagLookup.setEnabled(context, true)
            state = LookupState.Loading
        },
        onDismiss = { onApply(emptyMap(), null) }
    )
}

@Composable
private fun SearchBar(
    title: String,
    artist: String,
    onTitleChange: (String) -> Unit,
    onArtistChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val colors = LocalClintColors.current
    val focus = LocalFocusManager.current
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.divider,
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.secondaryText,
        cursorColor = colors.primary
    )
    val submit = {
        focus.clearFocus()
        onSearch()
    }
    BoxWithConstraints(
        modifier = Modifier
            .widthIn(max = 760.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        val narrow = maxWidth < 480.dp
        val fields: @Composable (Modifier) -> Unit = { fieldModifier ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = fieldModifier) {
                OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            singleLine = true,
            label = { Text(stringResource(R.string.tag_field_title), maxLines = 1) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submit() }),
            colors = fieldColors,
            modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
            value = artist,
            onValueChange = onArtistChange,
            singleLine = true,
            label = { Text(stringResource(R.string.tag_field_artist), maxLines = 1) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submit() }),
            colors = fieldColors,
            modifier = Modifier.weight(1f)
                )
            }
        }
        if (narrow) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                fields(Modifier.fillMaxWidth())
                KateActionButton(
                    label = stringResource(R.string.tag_lookup_search),
                    icon = Icons.Filled.Search,
                    enabled = title.isNotBlank(),
                    allowIconOnly = false,
                    onClick = submit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                fields(Modifier.weight(1f))
                KateActionButton(
                    label = stringResource(R.string.tag_lookup_search),
                    icon = Icons.Filled.Search,
                    enabled = title.isNotBlank(),
                    compact = true,
                    allowIconOnly = false,
                    onClick = submit,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MatchList(matches: List<TagMatch>, onChoose: (TagMatch) -> Unit) {
    val colors = LocalClintColors.current
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        items(matches, key = { it.key }) { match ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.cardBackground)
                    .clickable { onChoose(match) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                RemoteThumb(url = match.thumbUrl, size = 56.dp)
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        text = match.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = listOf(match.artist, match.album).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurface.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = listOf(
                            sourceName(match.source),
                            match.year,
                            match.trackNumber?.let { stringResource(R.string.tag_lookup_track, it) }.orEmpty(),
                            if (match.durationMs > 0) formatDuration(match.durationMs) else ""
                        ).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparePane(
    match: TagMatch,
    currentValue: (TagField) -> String,
    currentCover: ByteArray?,
    onApply: (Map<TagField, String>, ByteArray?) -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    val rows = remember(match.key) { CompareFields.filter { match.valueOf(it).isNotBlank() } }
    val selected = remember(match.key) {
        mutableStateMapOf<TagField, Boolean>().apply {
            rows.forEach { field -> put(field, match.valueOf(field).trim() != currentValue(field).trim()) }
        }
    }
    val hasCover = match.coverUrls.isNotEmpty()
    var useCover by remember(match.key) { mutableStateOf(hasCover) }
    var applying by remember { mutableStateOf(false) }
    val coverFailed = stringResource(R.string.tag_cover_download_failed)
    val anySelected = selected.values.any { it } || (hasCover && useCover)

    val apply: () -> Unit = {
        if (!applying && anySelected) {
            val values = rows.filter { selected[it] == true }.associateWith { match.valueOf(it) }
            if (hasCover && useCover) {
                applying = true
                scope.launch {
                    val raw = TagLookup.downloadImage(context, match.coverUrls)
                    val prepared = raw?.let { TagEditor.prepareCover(it) }
                    applying = false
                    if (prepared == null) {
                        haptics.reject()
                        Toast.makeText(context, coverFailed, Toast.LENGTH_LONG).show()
                    } else {
                        haptics.confirm()
                    }
                    onApply(values, prepared)
                }
            } else {
                haptics.confirm()
                onApply(values, null)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            item(key = "hint") {
                Text(
                    text = stringResource(R.string.tag_lookup_compare_hint, sourceName(match.source)),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
            if (hasCover) {
                item(key = "cover") {
                    CompareRow(checked = useCover, label = stringResource(R.string.tag_cover), onToggle = {
                        haptics.tick()
                        useCover = !useCover
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                            LocalThumb(bytes = currentCover, size = 64.dp)
                            KateIcon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = colors.secondaryText,
                                modifier = Modifier.padding(horizontal = 10.dp).size(18.dp)
                            )
                            RemoteThumb(url = match.thumbUrl, size = 64.dp)
                        }
                    }
                }
            }
            items(rows, key = { it.name }) { field ->
                val now = currentValue(field)
                val next = match.valueOf(field)
                CompareRow(checked = selected[field] == true, label = fieldLabelText(field), onToggle = {
                    haptics.tick()
                    selected[field] = selected[field] != true
                }) {
                    Text(
                        text = stringResource(R.string.tag_lookup_now, now.ifBlank { stringResource(R.string.tag_lookup_empty) }),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = stringResource(R.string.tag_lookup_new, next),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        KateActionButton(
            label = stringResource(if (applying) R.string.tag_lookup_applying else R.string.tag_lookup_apply),
            icon = Icons.Filled.Check,
            enabled = anySelected && !applying,
            allowIconOnly = false,
            onClick = apply,
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun CompareRow(checked: Boolean, label: String, onToggle: () -> Unit, content: @Composable () -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .widthIn(max = 760.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        KateIcon(
            imageVector = if (checked) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (checked) colors.primary else colors.secondaryText,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (checked) colors.primary else colors.secondaryText
            )
            content()
        }
    }
}

@Composable
private fun fieldLabelText(field: TagField): String = stringResource(
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
private fun RemoteThumb(url: String?, size: Dp) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val image by produceState(initialValue = url?.let { ThumbCache.get(it) }, url) {
        if (url != null && value == null) {
            val bytes = TagLookup.downloadImage(context, listOf(url))
            val bitmap = bytes?.let { withContext(Dispatchers.Default) { TagEditor.decodePreview(it, 256)?.asImageBitmap() } }
            if (bitmap != null) {
                ThumbCache.put(url, bitmap)
                value = bitmap
            }
        }
    }
    ThumbFrame(image = image, size = size, colors = colors)
}

@Composable
private fun LocalThumb(bytes: ByteArray?, size: Dp) {
    ThumbFrame(image = rememberPreview(bytes, 256), size = size, colors = LocalClintColors.current)
}

@Composable
private fun ThumbFrame(image: ImageBitmap?, size: Dp, colors: com.jhaiian.kate.ui.theme.ClintColors) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceVariant)
    ) {
        if (image != null) {
            Image(bitmap = image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            KateIcon(imageVector = Icons.Filled.Album, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(size / 2))
        }
    }
}

@Composable
private fun EmptyLookup(message: String, onRetry: () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp)
    ) {
        KateIcon(imageVector = Icons.Outlined.SearchOff, contentDescription = null, tint = colors.secondaryText, modifier = Modifier.size(56.dp))
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
internal fun TagLookupConsentDialog(visible: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
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
                text = stringResource(R.string.tag_lookup_consent_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.tag_lookup_consent_message),
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
