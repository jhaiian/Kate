package com.jhaiian.kate.ui.settings

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.BackupInfo
import com.jhaiian.kate.data.BackupManager
import com.jhaiian.kate.data.BackupSection
import com.jhaiian.kate.data.FavoritesStore
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.data.RestoreResult
import com.jhaiian.kate.data.SmartPlaylistStore
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun BackupScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    val bottomInset = LocalBottomContentPadding.current
    var exportSections by remember { mutableStateOf(BackupSection.entries.toSet()) }
    var working by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var pendingInfo by remember { mutableStateOf<BackupInfo?>(null) }
    var restoreSections by remember { mutableStateOf<Set<BackupSection>>(emptySet()) }
    var result by remember { mutableStateOf<RestoreResult?>(null) }
    var playCount by remember { mutableIntStateOf(0) }
    val exportedMessage = stringResource(R.string.backup_exported)
    val exportFailedMessage = stringResource(R.string.backup_export_failed)
    val invalidMessage = stringResource(R.string.backup_invalid)
    val restoreFailedMessage = stringResource(R.string.backup_restore_failed)

    LaunchedEffect(Unit) {
        PlaylistStore.ensureLoaded(context)
        SmartPlaylistStore.ensureLoaded(context)
        FavoritesStore.ensureLoaded(context)
        playCount = ListeningHistory.playCount(context)
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupManager.MIME_TYPE)) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        working = true
        scope.launch {
            val ok = BackupManager.export(context, uri, exportSections)
            working = false
            if (ok) haptics.confirm()
            Toast.makeText(context, if (ok) exportedMessage else exportFailedMessage, Toast.LENGTH_SHORT).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        working = true
        scope.launch {
            val info = BackupManager.inspect(context, uri)
            working = false
            if (info == null || info.sections.isEmpty()) {
                Toast.makeText(context, invalidMessage, Toast.LENGTH_SHORT).show()
            } else {
                pendingUri = uri
                pendingInfo = info
                restoreSections = info.sections
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomInset + 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
                    ) {
                        KateIconButton(onClick = onBack) {
                            KateIcon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.library_back),
                                tint = buttonTint(colors.onSurface)
                            )
                        }
                        Text(
                            text = stringResource(R.string.backup_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = stringResource(R.string.backup_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SectionHeader(stringResource(R.string.backup_export_section), colors.primary)
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.cardBackground)
                    ) {
                        BackupSection.entries.forEachIndexed { index, section ->
                            if (index > 0) RowDivider(colors.divider)
                            SettingsSwitchRow(
                                icon = sectionIcon(section),
                                title = stringResource(sectionTitle(section)),
                                subtitle = sectionSubtitle(section, playCount),
                                checked = section in exportSections,
                                colors = colors,
                                onCheckedChange = { on -> exportSections = if (on) exportSections + section else exportSections - section }
                            )
                        }
                    }
                    ActionButton(
                        icon = Icons.Outlined.FileDownload,
                        label = stringResource(R.string.backup_export_button),
                        enabled = exportSections.isNotEmpty() && !working,
                        filled = true,
                        onClick = { exportLauncher.launch(BackupManager.defaultFileName()) }
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    SectionHeader(stringResource(R.string.backup_import_section), colors.primary)
                    Text(
                        text = stringResource(R.string.backup_import_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    ActionButton(
                        icon = Icons.Outlined.FileUpload,
                        label = stringResource(R.string.backup_import_button),
                        enabled = !working,
                        filled = false,
                        onClick = { importLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")) }
                    )
                    if (working) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                        ) {
                            CircularProgressIndicator(color = colors.primary, strokeWidth = 3.dp, modifier = Modifier.size(22.dp))
                            Text(
                                text = stringResource(R.string.backup_working),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.secondaryText,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            }
        }

        val info = pendingInfo
        RestoreDialog(
            info = info,
            selected = restoreSections,
            working = working,
            onToggle = { section, on -> restoreSections = if (on) restoreSections + section else restoreSections - section },
            onDismiss = {
                if (!working) {
                    pendingInfo = null
                    pendingUri = null
                }
            },
            onRestore = {
                val uri = pendingUri ?: return@RestoreDialog
                working = true
                scope.launch {
                    val restored = BackupManager.restore(context, uri, restoreSections)
                    working = false
                    pendingInfo = null
                    pendingUri = null
                    if (restored == null) {
                        Toast.makeText(context, restoreFailedMessage, Toast.LENGTH_SHORT).show()
                    } else {
                        haptics.confirm()
                        result = restored
                    }
                }
            }
        )
        RestoreDoneDialog(result = result, onRestart = { BackupManager.restartApp(context) })
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, enabled: Boolean, filled: Boolean, onClick: () -> Unit) {
    KateActionButton(
        label = label,
        icon = icon,
        primary = filled,
        enabled = enabled,
        allowIconOnly = false,
        minHeight = 52.dp,
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp)
            .fillMaxWidth()
    )
}

@Composable
private fun RestoreDialog(
    info: BackupInfo?,
    selected: Set<BackupSection>,
    working: Boolean,
    onToggle: (BackupSection, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onRestore: () -> Unit
) {
    val colors = LocalClintColors.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    KateDialog(visible = info != null, onDismiss = onDismiss) {
        if (info == null) return@KateDialog
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(max = 520.dp)
                .heightIn(max = screenHeight * 0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 10.dp)) {
                Text(
                    text = stringResource(R.string.backup_restore_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.backup_restore_created,
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(info.createdAt))
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            RowDivider(colors.divider)
            Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                BackupSection.entries.filter { it in info.sections }.forEach { section ->
                    SettingsSwitchRow(
                        icon = sectionIcon(section),
                        title = stringResource(sectionTitle(section)),
                        subtitle = restoreSubtitle(section, info),
                        checked = section in selected,
                        colors = colors,
                        onCheckedChange = { on -> onToggle(section, on) },
                        enabled = !working
                    )
                }
                Text(
                    text = stringResource(R.string.backup_restore_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
            RowDivider(colors.divider)
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
                if (working) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, bottom = 6.dp)
                    ) {
                        CircularProgressIndicator(color = colors.primary, strokeWidth = 3.dp, modifier = Modifier.size(20.dp))
                        Text(
                            text = stringResource(R.string.backup_working),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.secondaryText,
                            modifier = Modifier.weight(1f).padding(start = 10.dp)
                        )
                    }
                }
                KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp)) {
                    KateTextButton(
                        label = stringResource(R.string.action_cancel),
                        role = DialogButtonRole.SECONDARY,
                        enabled = !working,
                        onClick = onDismiss
                    )
                    KateTextButton(
                        label = stringResource(R.string.backup_restore_button),
                        role = DialogButtonRole.MAIN,
                        enabled = !working && selected.isNotEmpty(),
                        onClick = onRestore
                    )
                }
            }
        }
    }
}

@Composable
private fun RestoreDoneDialog(result: RestoreResult?, onRestart: () -> Unit) {
    val colors = LocalClintColors.current
    BackHandler(enabled = result != null) {}
    KateDialog(visible = result != null, onDismiss = {}) {
        if (result == null) return@KateDialog
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 440.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.primary.copy(alpha = 0.14f))
            ) {
                KateIcon(imageVector = Icons.Outlined.TaskAlt, contentDescription = null, tint = colors.primary, modifier = Modifier.size(28.dp))
            }
            Text(
                text = stringResource(R.string.backup_done_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(top = 14.dp)
            )
            if (result.totalSongs > 0) {
                Text(
                    text = stringResource(R.string.backup_done_matched, result.matchedSongs, result.totalSongs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (result.plays > 0) {
                Text(
                    text = pluralStringResource(R.plurals.backup_done_plays, result.plays, result.plays),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = stringResource(R.string.backup_done_restart),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 8.dp)
            )
            KateActionButton(
                label = stringResource(R.string.backup_restart_button),
                icon = Icons.Outlined.RestartAlt,
                allowIconOnly = false,
                minHeight = 50.dp,
                onClick = onRestart,
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth()
            )
        }
    }
}

private fun sectionIcon(section: BackupSection): ImageVector = when (section) {
    BackupSection.SETTINGS -> Icons.Outlined.Tune
    BackupSection.PLAYLISTS -> Icons.AutoMirrored.Outlined.QueueMusic
    BackupSection.FAVORITES -> Icons.Outlined.FavoriteBorder
    BackupSection.HISTORY -> Icons.Outlined.Insights
    BackupSection.ARTIST_IMAGES -> Icons.Outlined.Person
}

private fun sectionTitle(section: BackupSection): Int = when (section) {
    BackupSection.SETTINGS -> R.string.backup_section_settings
    BackupSection.PLAYLISTS -> R.string.backup_section_playlists
    BackupSection.FAVORITES -> R.string.backup_section_favorites
    BackupSection.HISTORY -> R.string.backup_section_history
    BackupSection.ARTIST_IMAGES -> R.string.backup_section_artist_images
}

@Composable
private fun sectionSubtitle(section: BackupSection, plays: Int): String = when (section) {
    BackupSection.SETTINGS -> stringResource(R.string.backup_section_settings_subtitle)
    BackupSection.PLAYLISTS -> stringResource(
        R.string.backup_two_parts,
        pluralStringResource(R.plurals.library_playlist_count, PlaylistStore.playlists.size, PlaylistStore.playlists.size),
        pluralStringResource(R.plurals.backup_smart_count, SmartPlaylistStore.playlists.size, SmartPlaylistStore.playlists.size)
    )
    BackupSection.FAVORITES -> pluralStringResource(R.plurals.library_song_count, FavoritesStore.ids.size, FavoritesStore.ids.size)
    BackupSection.HISTORY -> pluralStringResource(R.plurals.backup_plays_count, plays, plays)
    BackupSection.ARTIST_IMAGES -> stringResource(R.string.backup_section_artist_images_subtitle)
}

@Composable
private fun restoreSubtitle(section: BackupSection, info: BackupInfo): String = when (section) {
    BackupSection.SETTINGS -> stringResource(R.string.backup_section_settings_subtitle)
    BackupSection.PLAYLISTS -> stringResource(
        R.string.backup_two_parts,
        pluralStringResource(R.plurals.library_playlist_count, info.playlists, info.playlists),
        pluralStringResource(R.plurals.backup_smart_count, info.smartPlaylists, info.smartPlaylists)
    )
    BackupSection.FAVORITES -> pluralStringResource(R.plurals.library_song_count, info.favorites, info.favorites)
    BackupSection.HISTORY -> pluralStringResource(R.plurals.backup_plays_count, info.plays, info.plays)
    BackupSection.ARTIST_IMAGES -> stringResource(R.string.backup_section_artist_images_subtitle)
}
