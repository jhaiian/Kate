package com.jhaiian.kate.ui.settings

import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShortText
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.LibraryTrackCache
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.data.ScanFilters
import com.jhaiian.kate.data.SmartPlaylistStore
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.KateWindowDialog
import com.jhaiian.kate.ui.library.icon
import com.jhaiian.kate.ui.library.labelRes
import com.jhaiian.kate.ui.theme.ClintSwitch
import com.jhaiian.kate.ui.theme.HomeSectionOptions
import com.jhaiian.kate.ui.theme.LibraryLayoutPreferences
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.NavTabOption
import kotlin.math.roundToInt

private fun <T> List<T>.moved(index: Int, offset: Int): List<T> {
    val target = index + offset
    if (index !in indices || target !in indices) return this
    return toMutableList().apply { add(target, removeAt(index)) }
}

@Composable
internal fun LibraryCategoriesSection(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    Spacer(modifier = Modifier.height(20.dp))
    SectionHeader(stringResource(R.string.library_categories_title), colors.primary)
    SettingsCard {
        val order = settings.libraryCategoryOrder
        order.forEachIndexed { index, key ->
            val category = LibraryCategory.fromKey(key) ?: return@forEachIndexed
            if (index > 0) RowDivider(colors.divider)
            val shown = key !in settings.libraryHiddenCategories
            TabOrderRow(
                option = NavTabOption(key, category.labelRes, category.icon),
                shown = shown,
                locked = category == LibraryCategory.SONGS,
                canMoveUp = index > 0,
                canMoveDown = index < order.lastIndex,
                onShownChange = { visible ->
                    val hidden = if (visible) settings.libraryHiddenCategories - key else settings.libraryHiddenCategories + key
                    onSettingsChange(settings.copy(libraryHiddenCategories = hidden))
                },
                onMoveUp = { onSettingsChange(settings.copy(libraryCategoryOrder = order.moved(index, -1))) },
                onMoveDown = { onSettingsChange(settings.copy(libraryCategoryOrder = order.moved(index, 1))) }
            )
        }
        Text(
            text = stringResource(R.string.library_categories_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)
        )
    }
}

@Composable
internal fun HomeOrderRows(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    val order = settings.homeSectionOrder
    order.forEachIndexed { index, key ->
        val option = HomeSectionOptions.firstOrNull { it.key == key } ?: return@forEachIndexed
        if (index > 0) RowDivider(colors.divider)
        TabOrderRow(
            option = option,
            shown = settings.showsHomeSection(key),
            locked = false,
            canMoveUp = index > 0,
            canMoveDown = index < order.lastIndex,
            onShownChange = { show ->
                val hidden = if (show) settings.homeHiddenSections - key else settings.homeHiddenSections + key
                onSettingsChange(settings.copy(homeHiddenSections = hidden))
            },
            onMoveUp = { onSettingsChange(settings.copy(homeSectionOrder = order.moved(index, -1))) },
            onMoveDown = { onSettingsChange(settings.copy(homeSectionOrder = order.moved(index, 1))) }
        )
    }
    RowDivider(colors.divider)
    HomePinsRow(settings, onSettingsChange)
}

@Composable
private fun HomePinsRow(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        PlaylistStore.ensureLoaded(context)
        SmartPlaylistStore.ensureLoaded(context)
    }
    val choices = PlaylistStore.playlists.map { LibraryLayoutPreferences.PIN_PLAYLIST + it.id to it.name } +
        SmartPlaylistStore.playlists.map { LibraryLayoutPreferences.PIN_SMART + it.id to it.name }
    SettingsRow(
        icon = Icons.Outlined.PushPin,
        title = stringResource(R.string.home_pins_title),
        subtitle = if (settings.homePins.isEmpty()) {
            stringResource(R.string.home_pins_none)
        } else {
            pluralStringResource(R.plurals.home_pins_count, settings.homePins.size, settings.homePins.size)
        },
        colors = colors,
        onClick = { open = true }
    )
    KateWindowDialog(visible = open, onDismiss = { open = false }) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.home_pins_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 4.dp)
            )
            Text(
                text = stringResource(R.string.home_pins_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Column(modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()).padding(vertical = 6.dp)) {
                if (choices.isEmpty()) {
                    Text(
                        text = stringResource(R.string.home_pins_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                choices.forEach { (key, name) ->
                    val pinned = key in settings.homePins
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSettingsChange(settings.copy(homePins = if (pinned) settings.homePins - key else settings.homePins + key))
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        ClintSwitch(
                            checked = pinned,
                            onCheckedChange = {
                                onSettingsChange(settings.copy(homePins = if (it) settings.homePins + key else settings.homePins - key))
                            }
                        )
                    }
                }
            }
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 6.dp)) {
                KateTextButton(label = stringResource(R.string.audio_done), role = DialogButtonRole.MAIN, onClick = { open = false })
            }
        }
    }
}

@Composable
internal fun SongRowsSection(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var titleSheet by remember { mutableStateOf(false) }
    var tapSheet by remember { mutableStateOf(false) }
    var fieldsOpen by remember { mutableStateOf(false) }
    val fieldLabels = mapOf(
        LibraryLayoutPreferences.FIELD_ARTIST to stringResource(R.string.song_info_artist),
        LibraryLayoutPreferences.FIELD_ALBUM to stringResource(R.string.song_info_album),
        LibraryLayoutPreferences.FIELD_ALBUM_ARTIST to stringResource(R.string.song_info_album_artist),
        LibraryLayoutPreferences.FIELD_YEAR to stringResource(R.string.song_info_year),
        LibraryLayoutPreferences.FIELD_GENRE to stringResource(R.string.song_info_genre),
        LibraryLayoutPreferences.FIELD_FORMAT to stringResource(R.string.song_info_format),
        LibraryLayoutPreferences.FIELD_FOLDER to stringResource(R.string.song_row_folder)
    )
    val titleLabels = listOf(
        LibraryLayoutPreferences.TITLE_TAG to stringResource(R.string.song_row_title_tag),
        LibraryLayoutPreferences.TITLE_FILENAME to stringResource(R.string.song_row_title_filename)
    )
    val tapLabels = listOf(
        LibraryLayoutPreferences.TAP_PLAY_LIST to stringResource(R.string.song_tap_play_list),
        LibraryLayoutPreferences.TAP_PLAY_ONLY to stringResource(R.string.song_tap_play_only),
        LibraryLayoutPreferences.TAP_PLAY_NEXT to stringResource(R.string.selection_play_next),
        LibraryLayoutPreferences.TAP_ADD_TO_QUEUE to stringResource(R.string.selection_add_to_queue)
    )
    Spacer(modifier = Modifier.height(20.dp))
    SectionHeader(stringResource(R.string.song_rows_title), colors.primary)
    SettingsCard {
        SettingsRow(
            icon = Icons.Outlined.Title,
            title = stringResource(R.string.song_row_title_source),
            subtitle = titleLabels.firstOrNull { it.first == settings.songTitleSource }?.second.orEmpty(),
            colors = colors,
            onClick = { titleSheet = true }
        )
        RowDivider(colors.divider)
        SettingsRow(
            icon = Icons.AutoMirrored.Outlined.ShortText,
            title = stringResource(R.string.song_row_fields),
            subtitle = settings.songSubtitleFields.mapNotNull { fieldLabels[it] }.joinToString(" • ").ifEmpty { stringResource(R.string.info_fields_none) },
            colors = colors,
            onClick = { fieldsOpen = true }
        )
        RowDivider(colors.divider)
        SettingsSwitchRow(
            icon = Icons.Outlined.Numbers,
            title = stringResource(R.string.song_row_track_number),
            subtitle = stringResource(R.string.song_row_track_number_subtitle),
            checked = settings.songShowTrackNumber,
            colors = colors,
            onCheckedChange = { onSettingsChange(settings.copy(songShowTrackNumber = it)) }
        )
        RowDivider(colors.divider)
        SettingsRow(
            icon = Icons.Outlined.TouchApp,
            title = stringResource(R.string.song_tap_title),
            subtitle = tapLabels.firstOrNull { it.first == settings.songTapAction }?.second.orEmpty(),
            colors = colors,
            onClick = { tapSheet = true }
        )
        RowDivider(colors.divider)
        SettingsSwitchRow(
            icon = Icons.Outlined.OpenInFull,
            title = stringResource(R.string.song_tap_open_player),
            subtitle = stringResource(R.string.song_tap_open_player_subtitle),
            checked = settings.songTapOpensPlayer,
            colors = colors,
            enabled = settings.songTapAction == LibraryLayoutPreferences.TAP_PLAY_LIST || settings.songTapAction == LibraryLayoutPreferences.TAP_PLAY_ONLY,
            onCheckedChange = { onSettingsChange(settings.copy(songTapOpensPlayer = it)) }
        )
    }
    if (titleSheet) {
        TextOptionSheet(
            title = stringResource(R.string.song_row_title_source),
            options = titleLabels,
            current = settings.songTitleSource,
            onSelect = { onSettingsChange(settings.copy(songTitleSource = it)); titleSheet = false },
            onDismiss = { titleSheet = false }
        )
    }
    if (tapSheet) {
        TextOptionSheet(
            title = stringResource(R.string.song_tap_title),
            options = tapLabels,
            current = settings.songTapAction,
            onSelect = { onSettingsChange(settings.copy(songTapAction = it)); tapSheet = false },
            onDismiss = { tapSheet = false }
        )
    }
    KateWindowDialog(visible = fieldsOpen, onDismiss = { fieldsOpen = false }) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.song_row_fields),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp)
            )
            LibraryLayoutPreferences.Fields.forEach { field ->
                val checked = field in settings.songSubtitleFields
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val next = if (checked) settings.songSubtitleFields - field else settings.songSubtitleFields + field
                            onSettingsChange(settings.copy(songSubtitleFields = LibraryLayoutPreferences.Fields.filter { it in next }))
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = fieldLabels[field].orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    ClintSwitch(
                        checked = checked,
                        onCheckedChange = { on ->
                            val next = if (on) settings.songSubtitleFields + field else settings.songSubtitleFields - field
                            onSettingsChange(settings.copy(songSubtitleFields = LibraryLayoutPreferences.Fields.filter { it in next }))
                        }
                    )
                }
            }
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 6.dp)) {
                KateTextButton(label = stringResource(R.string.audio_done), role = DialogButtonRole.MAIN, onClick = { fieldsOpen = false })
            }
        }
    }
}

@Composable
internal fun ScanFiltersSection() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { ScanFilters.ensureLoaded(context) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val documentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
            if (documentId != null) {
                ScanFilters.addExcluded(context, documentId)
                LibraryStore.refilter(context)
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
    SectionHeader(stringResource(R.string.scan_filters_title), colors.primary)
    SettingsCard {
        FineTuneSlider(
            title = stringResource(R.string.scan_min_duration),
            value = ScanFilters.minSeconds.toFloat(),
            range = 0f..ScanFilters.MAX_MIN_SECONDS.toFloat(),
            format = { value ->
                val seconds = value.roundToInt()
                if (seconds == 0) context.getString(R.string.scan_min_duration_off) else context.resources.getQuantityString(R.plurals.seconds_value, seconds, seconds)
            },
            onCommit = {
                ScanFilters.setMinSeconds(context, it.roundToInt())
                LibraryStore.refilter(context)
            }
        )
        RowDivider(colors.divider)
        SettingsSwitchRow(
            icon = Icons.Outlined.Image,
            title = stringResource(R.string.scan_prefer_folder_art),
            subtitle = stringResource(R.string.scan_prefer_folder_art_subtitle),
            checked = ScanFilters.preferFolderArtState,
            colors = colors,
            onCheckedChange = { on ->
                ScanFilters.setPreferFolderArt(context, on)
                if (!on) LibraryTrackCache.clear(context)
                LibraryStore.startScan(context)
            }
        )
        RowDivider(colors.divider)
        SettingsRow(
            icon = Icons.Outlined.CreateNewFolder,
            title = stringResource(R.string.scan_exclude_add),
            subtitle = stringResource(R.string.scan_exclude_add_subtitle),
            colors = colors,
            onClick = { picker.launch(null) }
        )
        ScanFilters.excluded.sorted().forEach { documentId ->
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.FolderOff,
                title = documentId.substringAfterLast('/').substringAfterLast(':').ifEmpty { documentId },
                subtitle = stringResource(R.string.scan_exclude_remove),
                colors = colors,
                onClick = {
                    ScanFilters.removeExcluded(context, documentId)
                    LibraryStore.refilter(context)
                }
            )
        }
        Text(
            text = stringResource(R.string.scan_filters_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)
        )
    }
}
