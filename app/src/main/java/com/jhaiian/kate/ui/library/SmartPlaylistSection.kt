package com.jhaiian.kate.ui.library

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.data.PlayCounts
import com.jhaiian.kate.data.SmartField
import com.jhaiian.kate.data.SmartFieldKind
import com.jhaiian.kate.data.SmartOperator
import com.jhaiian.kate.data.SmartPlaylist
import com.jhaiian.kate.data.SmartPlaylistEngine
import com.jhaiian.kate.data.SmartPlaylistStore
import com.jhaiian.kate.data.SmartPreset
import com.jhaiian.kate.data.SmartRule
import com.jhaiian.kate.data.SmartSort
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.theme.LibraryStylePreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel

private val SmartCardWidth = 148.dp
private val SmartLimitOptions = listOf(0, 25, 50, 100, 250, 500)

internal fun smartArtKey(id: String) = "library_smart_art_$id"

@Composable
internal fun smartPlaylistName(playlist: SmartPlaylist): String = when {
    playlist.name.isNotBlank() -> playlist.name
    playlist.decade != null -> stringResource(R.string.smart_decade_name, playlist.decade)
    playlist.preset == SmartPreset.MOST_PLAYED -> stringResource(R.string.smart_preset_most_played)
    playlist.preset == SmartPreset.RECENTLY_ADDED -> stringResource(R.string.smart_preset_recently_added)
    playlist.preset == SmartPreset.NEVER_PLAYED -> stringResource(R.string.smart_preset_never_played)
    playlist.preset == SmartPreset.FORGOTTEN_GEMS -> stringResource(R.string.smart_preset_forgotten_gems)
    else -> stringResource(R.string.smart_playlists_title)
}

internal fun smartPlaylistIcon(playlist: SmartPlaylist): ImageVector = when {
    playlist.decade != null -> Icons.Filled.CalendarMonth
    playlist.preset == SmartPreset.MOST_PLAYED -> Icons.Filled.LocalFireDepartment
    playlist.preset == SmartPreset.RECENTLY_ADDED -> Icons.Filled.NewReleases
    playlist.preset == SmartPreset.NEVER_PLAYED -> Icons.Filled.Explore
    playlist.preset == SmartPreset.FORGOTTEN_GEMS -> Icons.Filled.Diamond
    else -> Icons.Filled.AutoAwesome
}

@Composable
internal fun rememberLibrarySnapshot(): List<Track> {
    val snapshot by remember { derivedStateOf { LibraryStore.tracks.toList() } }
    return snapshot
}

@Composable
internal fun rememberSmartTracks(playlist: SmartPlaylist?, library: List<Track> = rememberLibrarySnapshot()): List<Track> {
    val plays = PlayCounts.stats
    val favorites = FavoritesStore.ids
    return remember(playlist, library, plays, favorites) {
        if (playlist == null) emptyList() else SmartPlaylistEngine.evaluate(playlist, library, plays, favorites)
    }
}

@Composable
internal fun SmartPlaylistsLoader() {
    val context = LocalContext.current
    val historyVersion = ListeningHistory.version
    LaunchedEffect(Unit) {
        SmartPlaylistStore.ensureLoaded(context)
        FavoritesStore.ensureLoaded(context)
    }
    LaunchedEffect(historyVersion) { PlayCounts.refresh(context) }
}

@Composable
internal fun SmartPlaylistShelf(onOpen: (String) -> Unit, onCreate: () -> Unit) {
    val colors = LocalClintColors.current
    SmartPlaylistsLoader()
    val stored = SmartPlaylistStore.playlists
    val library = rememberLibrarySnapshot()
    val decades = remember(library) { SmartPlaylistEngine.decades(library) }
    val playlists = remember(stored, decades) { stored + decades.map { SmartPlaylistStore.decade(it) } }
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.smart_playlists_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = stringResource(R.string.smart_playlists_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            KateTextButton(
                label = stringResource(R.string.smart_playlist_new_short),
                role = DialogButtonRole.LINK,
                icon = Icons.Filled.Add,
                onClick = onCreate
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            items(playlists, key = { it.id }) { playlist ->
                SmartPlaylistCard(playlist = playlist, library = library, onClick = { onOpen(playlist.id) })
            }
        }
    }
}

@Composable
private fun SmartPlaylistCard(playlist: SmartPlaylist, library: List<Track>, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    val tracks = rememberSmartTracks(playlist, library)
    val artTrack = remember(tracks) { tracks.firstOrNull { it.artKey != null } }
    val corner = LibraryStylePreferences.scaledCorner(18.dp, LocalLookAndFeel.current.libraryCorners)
    val shape = RoundedCornerShape(corner)
    val icon = smartPlaylistIcon(playlist)
    Column(
        modifier = Modifier
            .width(SmartCardWidth)
            .clip(RoundedCornerShape(corner))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(SmartCardWidth - 8.dp)
                .clip(shape)
                .background(Brush.linearGradient(listOf(colors.primary, colors.primary.copy(alpha = 0.45f))))
        ) {
            if (artTrack != null) {
                ArtThumb(
                    track = artTrack,
                    icon = icon,
                    shape = shape,
                    iconSize = 40.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, colors.primary.copy(alpha = 0.35f), colors.primary.copy(alpha = 0.9f))
                            )
                        )
                )
            }
            Box(
                modifier = Modifier
                    .align(if (artTrack != null) Alignment.BottomStart else Alignment.Center)
                    .padding(if (artTrack != null) 10.dp else 0.dp)
                    .size(if (artTrack != null) 34.dp else 56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.onPrimary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                KateIcon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(if (artTrack != null) 20.dp else 32.dp)
                )
            }
            KateIcon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = colors.onPrimary.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(16.dp)
            )
        }
        Text(
            text = smartPlaylistName(playlist),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 8.dp)
        )
        Text(
            text = pluralStringResource(R.plurals.library_song_count, tracks.size, tracks.size),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

private class EditorSession {
    var current: SmartPlaylist? = null
    var last: SmartPlaylist? = null
    var session = 0
}

private sealed interface SmartPicker {
    data class Field(val index: Int) : SmartPicker
    data class Operator(val index: Int) : SmartPicker
    data object Sort : SmartPicker
}

@Composable
internal fun SmartPlaylistEditor(
    initial: SmartPlaylist?,
    onDismiss: () -> Unit,
    onSaved: (SmartPlaylist) -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val visible = initial != null
    val holder = remember { EditorSession() }
    if (initial != null && holder.current == null) holder.session++
    if (initial != null) holder.last = initial
    holder.current = initial
    val source = initial ?: holder.last
    val session = holder.session
    val initialName = source?.let { if (it.name.isBlank() && (it.preset != null || it.decade != null)) smartPlaylistName(it) else it.name }.orEmpty()
    var draft by remember(session) { mutableStateOf(source ?: SmartPlaylistStore.newDraft()) }
    var name by remember(session) { mutableStateOf(initialName) }
    var picker by remember(session) { mutableStateOf<SmartPicker?>(null) }
    val matches = rememberSmartTracks(if (source != null) draft else null)
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val isNew = source == null || source.id.isBlank() || source.isDecade
    val canSave = name.isNotBlank()
    val updateRule: (Int, SmartRule) -> Unit = { index, rule ->
        draft = draft.copy(rules = draft.rules.toMutableList().also { if (index in it.indices) it[index] = rule })
    }
    val save = {
        if (canSave) {
            val keepPresetName = source?.preset != null && name.trim() == initialName.trim()
            val stored = SmartPlaylistStore.save(context, draft.copy(name = if (keepPresetName) "" else name.trim()))
            haptics.confirm()
            onSaved(stored)
        }
    }

    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 10.dp, top = 16.dp, bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .kateIconTile(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    KateIcon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(18.dp))
                }
                Text(
                    text = stringResource(if (isNew) R.string.smart_playlist_new else R.string.smart_playlist_edit),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
                KateIconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    KateIcon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.folder_picker_close),
                        tint = buttonTint(colors.secondaryText),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.playlist_name)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = smartFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                EditorLabel(stringResource(R.string.smart_match_label))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SegmentChip(
                        label = stringResource(R.string.smart_match_all),
                        selected = draft.matchAll,
                        modifier = Modifier.weight(1f),
                        onClick = { draft = draft.copy(matchAll = true) }
                    )
                    SegmentChip(
                        label = stringResource(R.string.smart_match_any),
                        selected = !draft.matchAll,
                        modifier = Modifier.weight(1f),
                        onClick = { draft = draft.copy(matchAll = false) }
                    )
                }
                if (draft.rules.isEmpty()) {
                    Text(
                        text = stringResource(R.string.smart_no_rules),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                draft.rules.forEachIndexed { index, rule ->
                    RuleCard(
                        rule = rule,
                        onPickField = { picker = SmartPicker.Field(index) },
                        onPickOperator = { picker = SmartPicker.Operator(index) },
                        onValueChange = { value -> updateRule(index, rule.copy(value = value)) },
                        onValue2Change = { value -> updateRule(index, rule.copy(value2 = value)) },
                        onRemove = {
                            haptics.tick()
                            draft = draft.copy(rules = draft.rules.toMutableList().also { it.removeAt(index) })
                        }
                    )
                }
                KateTextButton(
                    label = stringResource(R.string.smart_add_rule),
                    role = DialogButtonRole.LINK,
                    icon = Icons.Filled.Add,
                    modifier = Modifier.padding(top = 6.dp),
                    onClick = {
                        haptics.tick()
                        draft = draft.copy(rules = draft.rules + SmartRule(SmartField.ARTIST, SmartOperator.CONTAINS))
                    }
                )
                EditorLabel(stringResource(R.string.smart_sort_label))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectChip(
                        label = sortLabel(draft.sort),
                        modifier = Modifier.weight(1f),
                        onClick = { picker = SmartPicker.Sort }
                    )
                    if (draft.sort != SmartSort.RANDOM) {
                        SegmentChip(
                            label = stringResource(if (draft.descending) R.string.smart_sort_descending else R.string.smart_sort_ascending),
                            selected = true,
                            icon = if (draft.descending) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                haptics.tick()
                                draft = draft.copy(descending = !draft.descending)
                            }
                        )
                    }
                }
                EditorLabel(stringResource(R.string.smart_limit_label))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    SmartLimitOptions.forEach { limit ->
                        SegmentChip(
                            label = if (limit == 0) {
                                stringResource(R.string.smart_limit_none)
                            } else {
                                pluralStringResource(R.plurals.smart_limit_songs, limit, limit)
                            },
                            selected = draft.limit == limit,
                            onClick = { draft = draft.copy(limit = limit) }
                        )
                    }
                }
                if (draft.usesPlays && !ListeningHistory.enabled) {
                    Text(
                        text = stringResource(R.string.smart_history_off),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.colorError,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                }
            }
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            Text(
                text = pluralStringResource(R.plurals.smart_match_count, matches.size, matches.size),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp)
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 4.dp, bottom = 8.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_cancel),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.playlist_save),
                    role = DialogButtonRole.MAIN,
                    enabled = canSave,
                    onClick = save
                )
            }
        }
    }

    val activePicker = picker
    val pickerTitle: String
    val pickerOptions: List<Pair<String, Boolean>>
    val onPick: (Int) -> Unit
    when (activePicker) {
        is SmartPicker.Field -> {
            val rule = draft.rules.getOrNull(activePicker.index)
            pickerTitle = stringResource(R.string.smart_pick_field)
            pickerOptions = SmartField.entries.map { fieldLabel(it) to (it == rule?.field) }
            onPick = { position ->
                if (rule != null) updateRule(activePicker.index, rule.withField(SmartField.entries[position]))
                picker = null
            }
        }
        is SmartPicker.Operator -> {
            val rule = draft.rules.getOrNull(activePicker.index)
            val operators = rule?.field?.operators.orEmpty()
            pickerTitle = stringResource(R.string.smart_pick_condition)
            pickerOptions = operators.map { operatorLabel(rule?.field ?: SmartField.TITLE, it) to (it == rule?.operator) }
            onPick = { position ->
                if (rule != null) updateRule(activePicker.index, rule.copy(operator = operators[position]))
                picker = null
            }
        }
        SmartPicker.Sort -> {
            pickerTitle = stringResource(R.string.smart_sort_label)
            pickerOptions = SmartSort.entries.map { sortLabel(it) to (it == draft.sort) }
            onPick = { position ->
                val sort = SmartSort.entries[position]
                val descending = when (sort) {
                    SmartSort.PLAY_COUNT, SmartSort.LAST_PLAYED, SmartSort.DATE_ADDED, SmartSort.YEAR -> true
                    else -> false
                }
                draft = draft.copy(sort = sort, descending = if (sort == draft.sort) draft.descending else descending)
                picker = null
            }
        }
        null -> {
            pickerTitle = ""
            pickerOptions = emptyList()
            onPick = {}
        }
    }
    SmartOptionDialog(
        visible = visible && activePicker != null,
        title = pickerTitle,
        options = pickerOptions,
        onDismiss = { picker = null },
        onPick = onPick
    )
}

@Composable
private fun EditorLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = LocalClintColors.current.secondaryText,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
    )
}

@Composable
private fun RuleCard(
    rule: SmartRule,
    onPickField: () -> Unit,
    onPickOperator: () -> Unit,
    onValueChange: (String) -> Unit,
    onValue2Change: (String) -> Unit,
    onRemove: () -> Unit
) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.primary.copy(alpha = 0.06f))
            .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.5f)), RoundedCornerShape(16.dp))
            .padding(start = 10.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            SelectChip(label = fieldLabel(rule.field), modifier = Modifier.weight(1f), onClick = onPickField)
            Spacer(modifier = Modifier.width(8.dp))
            SelectChip(label = operatorLabel(rule.field, rule.operator), modifier = Modifier.weight(1f), onClick = onPickOperator)
            KateIconButton(onClick = onRemove) {
                KateIcon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.smart_remove_rule),
                    tint = buttonTint(colors.secondaryText),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (rule.operator.valueCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, end = 6.dp)
            ) {
                RuleValueField(
                    value = rule.value,
                    field = rule.field,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f)
                )
                if (rule.operator.valueCount > 1) {
                    Text(
                        text = stringResource(R.string.smart_value_and),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    RuleValueField(
                        value = rule.value2,
                        field = rule.field,
                        onValueChange = onValue2Change,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleValueField(value: String, field: SmartField, onValueChange: (String) -> Unit, modifier: Modifier) {
    val colors = LocalClintColors.current
    val numeric = field.kind != SmartFieldKind.TEXT
    val unit = when (field) {
        SmartField.DURATION -> stringResource(R.string.smart_unit_minutes)
        SmartField.LAST_PLAYED, SmartField.DATE_ADDED -> stringResource(R.string.smart_unit_days)
        else -> null
    }
    OutlinedTextField(
        value = value,
        onValueChange = { next ->
            onValueChange(if (numeric) next.filter { it.isDigit() || it == '.' }.take(6) else next)
        },
        singleLine = true,
        placeholder = { Text(stringResource(R.string.smart_value_hint), color = colors.secondaryText) },
        suffix = if (unit != null) {
            { Text(unit, color = colors.secondaryText) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = when {
                field == SmartField.DURATION -> KeyboardType.Decimal
                numeric -> KeyboardType.Number
                else -> KeyboardType.Text
            },
            capitalization = if (numeric) KeyboardCapitalization.None else KeyboardCapitalization.Words
        ),
        colors = smartFieldColors(),
        modifier = modifier
    )
}

@Composable
private fun smartFieldColors() = LocalClintColors.current.let { colors ->
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        focusedBorderColor = colors.primary,
        unfocusedBorderColor = colors.divider,
        focusedLabelColor = colors.primary,
        unfocusedLabelColor = colors.secondaryText,
        cursorColor = colors.primary
    )
}

@Composable
private fun SelectChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.cardBackground)
            .border(BorderStroke(1.dp, colors.divider), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        KateIcon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SegmentChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val colors = LocalClintColors.current
    val background by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.16f) else colors.primary.copy(alpha = 0f),
        animationSpec = tween(durationMillis = 200),
        label = "smart_segment_background"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .border(BorderStroke(1.dp, if (selected) colors.primary else colors.divider), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (icon != null) {
            KateIcon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colors.primary else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SmartOptionDialog(
    visible: Boolean,
    title: String,
    options: List<Pair<String, Boolean>>,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit
) {
    val colors = LocalClintColors.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .widthIn(max = 360.dp)
                .heightIn(max = screenHeight * 0.8f)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
            RowDivider(color = colors.surfaceVariant)
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 4.dp)) {
                options.forEachIndexed { index, (label, selected) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) colors.primary.copy(alpha = 0.14f) else Color.Transparent)
                            .clickable { onPick(index) }
                            .padding(horizontal = 14.dp, vertical = 13.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) colors.primary else colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (selected) {
                            KateIcon(imageVector = Icons.Filled.Check, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun fieldLabel(field: SmartField): String = stringResource(
    when (field) {
        SmartField.TITLE -> R.string.smart_field_title
        SmartField.ARTIST -> R.string.smart_field_artist
        SmartField.ALBUM -> R.string.smart_field_album
        SmartField.ALBUM_ARTIST -> R.string.smart_field_album_artist
        SmartField.GENRE -> R.string.smart_field_genre
        SmartField.YEAR -> R.string.smart_field_year
        SmartField.DURATION -> R.string.smart_field_duration
        SmartField.PLAY_COUNT -> R.string.smart_field_play_count
        SmartField.LAST_PLAYED -> R.string.smart_field_last_played
        SmartField.DATE_ADDED -> R.string.smart_field_date_added
        SmartField.FAVORITE -> R.string.smart_field_favorite
        SmartField.FOLDER -> R.string.smart_field_folder
        SmartField.FORMAT -> R.string.smart_field_format
    }
)

@Composable
private fun operatorLabel(field: SmartField, operator: SmartOperator): String = stringResource(
    when (operator) {
        SmartOperator.CONTAINS -> R.string.smart_op_contains
        SmartOperator.NOT_CONTAINS -> R.string.smart_op_not_contains
        SmartOperator.IS -> R.string.smart_op_is
        SmartOperator.IS_NOT -> R.string.smart_op_is_not
        SmartOperator.STARTS_WITH -> R.string.smart_op_starts_with
        SmartOperator.AT_LEAST -> R.string.smart_op_at_least
        SmartOperator.AT_MOST -> R.string.smart_op_at_most
        SmartOperator.BETWEEN -> R.string.smart_op_between
        SmartOperator.WITHIN_DAYS -> R.string.smart_op_within_days
        SmartOperator.OLDER_THAN_DAYS -> R.string.smart_op_older_than_days
        SmartOperator.IS_TRUE -> R.string.smart_op_is_true
        SmartOperator.IS_FALSE -> R.string.smart_op_is_false
    }
)

@Composable
private fun sortLabel(sort: SmartSort): String = stringResource(
    when (sort) {
        SmartSort.TITLE -> R.string.smart_field_title
        SmartSort.ARTIST -> R.string.smart_field_artist
        SmartSort.ALBUM -> R.string.smart_field_album
        SmartSort.YEAR -> R.string.smart_field_year
        SmartSort.DATE_ADDED -> R.string.smart_field_date_added
        SmartSort.PLAY_COUNT -> R.string.smart_field_play_count
        SmartSort.LAST_PLAYED -> R.string.smart_field_last_played
        SmartSort.DURATION -> R.string.smart_field_duration
        SmartSort.RANDOM -> R.string.smart_sort_random
    }
)
