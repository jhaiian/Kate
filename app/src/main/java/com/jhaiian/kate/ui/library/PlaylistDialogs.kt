package com.jhaiian.kate.ui.library

import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.PlaylistStore
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.delay

private const val NameFocusDelayMillis = 250L

@Composable
internal fun AddToPlaylistDialog(trackIds: List<String>?, onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val resources = context.resources
    var naming by remember { mutableStateOf(false) }
    var pendingIds by remember { mutableStateOf<List<String>>(emptyList()) }
    val ids = trackIds ?: pendingIds
    KateDialog(visible = trackIds != null && !naming, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .widthIn(max = 380.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.playlist_add_to),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
            PlaylistChoiceRow(
                title = stringResource(R.string.playlist_new),
                subtitle = null,
                highlighted = true,
                onClick = {
                    pendingIds = ids
                    naming = true
                }
            )
            LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                items(PlaylistStore.playlists, key = { it.id }) { playlist ->
                    PlaylistChoiceRow(
                        title = playlist.name,
                        subtitle = pluralStringResource(R.plurals.library_song_count, playlist.trackIds.size, playlist.trackIds.size),
                        highlighted = false,
                        onClick = {
                            val added = PlaylistStore.addTracks(playlist.id, ids)
                            val message = if (added > 0) {
                                haptics.confirm()
                                resources.getQuantityString(R.plurals.playlist_added, added, added, playlist.name)
                            } else {
                                resources.getString(R.string.playlist_already_added, playlist.name)
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
    PlaylistNameDialog(
        visible = trackIds != null && naming,
        title = stringResource(R.string.playlist_new),
        initialName = "",
        confirmLabel = stringResource(R.string.playlist_create),
        onConfirm = { name ->
            val playlist = PlaylistStore.create(context, name, pendingIds)
            haptics.confirm()
            Toast.makeText(context, resources.getString(R.string.playlist_created, playlist.name), Toast.LENGTH_SHORT).show()
            naming = false
            onDismiss()
        },
        onDismiss = {
            naming = false
            onDismiss()
        }
    )
}

@Composable
private fun PlaylistChoiceRow(title: String, subtitle: String?, highlighted: Boolean, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp, highlighted),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(
                imageVector = if (highlighted) Icons.Filled.Add else Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = null,
                tint = kateTileTint(highlighted),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (highlighted) colors.primary else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun PlaylistNameDialog(
    visible: Boolean,
    title: String,
    initialName: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    fieldLabel: String = stringResource(R.string.playlist_name)
) {
    val colors = LocalClintColors.current
    var name by remember(visible, initialName) { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }
    val valid = name.isNotBlank()
    LaunchedEffect(visible) {
        if (visible) {
            delay(NameFocusDelayMillis)
            runCatching { focusRequester.requestFocus() }
        }
    }
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .imePadding()
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 12.dp)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(fieldLabel) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (valid) onConfirm(name.trim()) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.divider,
                    focusedLabelColor = colors.primary,
                    unfocusedLabelColor = colors.secondaryText,
                    cursorColor = colors.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .focusRequester(focusRequester)
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_cancel),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = confirmLabel,
                    role = DialogButtonRole.MAIN,
                    enabled = valid,
                    onClick = { onConfirm(name.trim()) }
                )
            }
        }
    }
}

@Composable
internal fun DeletePlaylistDialog(visible: Boolean, name: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
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
                text = stringResource(R.string.playlist_delete_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.playlist_delete_message, name),
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
                    label = stringResource(R.string.action_delete),
                    role = DialogButtonRole.DESTRUCTIVE,
                    onClick = onConfirm
                )
            }
        }
    }
}
