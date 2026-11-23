package com.jhaiian.kate.ui.library

import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.jhaiian.kate.ui.components.KateSheetWindowInsets
import com.jhaiian.kate.ui.components.MatchAppSystemBars
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import com.jhaiian.kate.R
import com.jhaiian.kate.data.DetectedVolume
import com.jhaiian.kate.data.MusicFolder
import com.jhaiian.kate.data.detectStorageVolumes
import com.jhaiian.kate.data.labelForTreeUri
import com.jhaiian.kate.ui.components.rememberSheetFlingConnection
import com.jhaiian.kate.ui.components.rememberSheetMaxHeight
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.settings.SectionHeader
import com.jhaiian.kate.ui.theme.ClintSwitch
import com.jhaiian.kate.ui.theme.LocalClintColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderPickerDialog(
    folders: List<MusicFolder>,
    onDismiss: () -> Unit,
    onFolderAdded: (Uri, String, String) -> Unit,
    onFolderToggled: (Uri, Boolean) -> Unit,
    onFolderRemoved: (Uri) -> Unit,
    onSaveAndScan: () -> Unit
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val volumes = remember { detectStorageVolumes(context) }
    var pendingLabel by remember { mutableStateOf("Storage") }

    val treeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            val name = runCatching { DocumentFile.fromTreeUri(context, uri)?.name }.getOrNull()
                ?: uri.lastPathSegment ?: "Folder"
            onFolderAdded(uri, name, labelForTreeUri(uri))
        }
    }

    val volumeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            val name = runCatching { DocumentFile.fromTreeUri(context, uri)?.name }.getOrNull()
                ?: uri.lastPathSegment ?: "Folder"
            onFolderAdded(uri, name, pendingLabel)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.cardBackground,
        contentWindowInsets = { KateSheetWindowInsets }
    ) {
        MatchAppSystemBars()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(rememberSheetMaxHeight())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 8.dp, top = 4.dp, bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.folder_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                KateIconButton(onClick = onDismiss) {
                    KateIcon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.folder_picker_close),
                        tint = buttonTint(colors.secondaryText)
                    )
                }
            }

            LazyColumn(modifier = Modifier.weight(1f).nestedScroll(rememberSheetFlingConnection(sheetState, consumeFling = false))) {
                if (volumes.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.folder_picker_detected_storage), colors.primary) }
                    items(volumes, key = { it.id }) { volume ->
                        DetectedVolumeRow(
                            volume = volume,
                            alreadyAdded = folders.any { it.locationLabel == volume.label },
                            onEnable = {
                                pendingLabel = volume.label
                                volumeLauncher.launch(volume.intent)
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                item { SectionHeader(stringResource(R.string.folder_picker_selected_folders), colors.primary) }

                if (folders.isEmpty()) {
                    item {
                        EmptyFoldersHint()
                    }
                } else {
                    items(folders, key = { it.uri.toString() }) { folder ->
                        Column(modifier = Modifier.animateItem()) {
                            FolderRow(
                                folder = folder,
                                onToggle = { onFolderToggled(folder.uri, it) },
                                onRemove = { onFolderRemoved(folder.uri) }
                            )
                            RowDivider(colors.divider)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                KateActionButton(
                    label = stringResource(R.string.folder_picker_browse_button),
                    icon = Icons.Outlined.CreateNewFolder,
                    primary = false,
                    allowIconOnly = false,
                    minHeight = 52.dp,
                    onClick = { treeLauncher.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                KateActionButton(
                    label = stringResource(R.string.folder_picker_scan_button),
                    enabled = folders.any { it.enabled },
                    minHeight = 52.dp,
                    onClick = onSaveAndScan,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun DetectedVolumeRow(volume: DetectedVolume, alreadyAdded: Boolean, onEnable: () -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = Icons.Outlined.SdCard, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(20.dp))
        }
        Text(
            text = volume.label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f).padding(start = 14.dp)
        )
        Text(
            text = stringResource(R.string.folder_picker_enable),
            style = MaterialTheme.typography.labelLarge,
            color = if (alreadyAdded) colors.secondaryText else colors.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onEnable)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun FolderRow(folder: MusicFolder, onToggle: (Boolean) -> Unit, onRemove: () -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = Icons.Outlined.Folder, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(text = folder.displayName, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface, fontWeight = FontWeight.Medium)
            Text(text = folder.locationLabel, style = MaterialTheme.typography.bodySmall, color = colors.secondaryText)
        }
        KateIconButton(onClick = onRemove) {
            KateIcon(imageVector = Icons.Filled.Close, contentDescription = stringResource(R.string.folder_picker_remove_content_desc), tint = buttonTint(colors.secondaryText), modifier = Modifier.size(18.dp))
        }
        ClintSwitch(checked = folder.enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun EmptyFoldersHint() {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 28.dp)
    ) {
        KateIcon(
            imageVector = Icons.Outlined.CreateNewFolder,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = stringResource(R.string.folder_picker_empty_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = stringResource(R.string.folder_picker_empty_subtext),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
