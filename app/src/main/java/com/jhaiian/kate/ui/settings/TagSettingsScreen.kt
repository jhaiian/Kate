package com.jhaiian.kate.ui.settings

import com.jhaiian.kate.ui.components.KateChip
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.TagSettings
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.theme.LocalClintColors

private enum class TagDialog { ARTIST_SEPARATOR, GENRE_SEPARATOR, UNSPLIT_NAME }

private const val MAX_NAME_LENGTH = 100

@Composable
fun TagSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { TagSettings.ensureLoaded(context) }
    val config = TagSettings.config
    var dialog by remember { mutableStateOf<TagDialog?>(null) }
    val resetMessage = stringResource(R.string.tags_reset_done)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 20.dp, top = 12.dp, bottom = 8.dp)
            ) {
                KateIconButton(onClick = onBack) {
                    KateIcon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = buttonTint(colors.onSurface)
                    )
                }
                Text(
                    text = stringResource(R.string.section_tags),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomContentPadding.current),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                        SectionHeader(stringResource(R.string.section_artist_separators), colors.primary)
                        SeparatorCard(
                            summary = stringResource(R.string.artist_separators_summary),
                            options = TagSettings.ArtistSeparatorOptions,
                            selected = config.artistSeparators,
                            onToggle = { TagSettings.toggleArtistSeparator(context, it) },
                            onAdd = { dialog = TagDialog.ARTIST_SEPARATOR }
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        SectionHeader(stringResource(R.string.section_unsplit_artists), colors.primary)
                        UnsplitNamesCard(
                            names = config.unsplitArtists,
                            onRemove = { TagSettings.removeUnsplitArtist(context, it) },
                            onAdd = { dialog = TagDialog.UNSPLIT_NAME }
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        SectionHeader(stringResource(R.string.section_genre_separators), colors.primary)
                        SeparatorCard(
                            summary = stringResource(R.string.genre_separators_summary),
                            options = TagSettings.GenreSeparatorOptions,
                            selected = config.genreSeparators,
                            onToggle = { TagSettings.toggleGenreSeparator(context, it) },
                            onAdd = { dialog = TagDialog.GENRE_SEPARATOR }
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        SectionHeader(stringResource(R.string.section_albums), colors.primary)
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.cardBackground)
                        ) {
                            SettingsSwitchRow(
                                icon = Icons.Outlined.Album,
                                title = stringResource(R.string.row_group_albums_title),
                                subtitle = stringResource(R.string.row_group_albums_summary),
                                checked = config.groupAlbumsByMainArtist,
                                colors = colors,
                                onCheckedChange = { TagSettings.setGroupAlbumsByMainArtist(context, it) }
                            )
                            RowDivider(colors.divider)
                            ResetRow(
                                enabled = !TagSettings.isDefault,
                                onClick = {
                                    TagSettings.resetToDefaults(context)
                                    Toast.makeText(context, resetMessage, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        TagTextDialog(
            visible = dialog == TagDialog.ARTIST_SEPARATOR || dialog == TagDialog.GENRE_SEPARATOR,
            title = stringResource(R.string.separator_add_title),
            hint = stringResource(R.string.separator_hint),
            maxLength = TagSettings.MAX_SEPARATOR_LENGTH,
            onConfirm = { text ->
                if (dialog == TagDialog.GENRE_SEPARATOR) {
                    TagSettings.addGenreSeparator(context, text)
                } else {
                    TagSettings.addArtistSeparator(context, text)
                }
                dialog = null
            },
            onDismiss = { dialog = null }
        )
        TagTextDialog(
            visible = dialog == TagDialog.UNSPLIT_NAME,
            title = stringResource(R.string.unsplit_artist_add),
            hint = stringResource(R.string.unsplit_artist_hint),
            maxLength = MAX_NAME_LENGTH,
            onConfirm = { text ->
                TagSettings.addUnsplitArtist(context, text)
                dialog = null
            },
            onDismiss = { dialog = null }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeparatorCard(
    summary: String,
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onAdd: () -> Unit
) {
    val colors = LocalClintColors.current
    val shown = options + selected.filterNot { it in options }
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
            .padding(16.dp)
    ) {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            shown.forEach { token ->
                val isSelected = token in selected
                KateChip(
                    label = token,
                    selected = isSelected,
                    icon = if (isSelected) Icons.Filled.Check else null,
                    onClick = { onToggle(token) }
                )
            }
            KateChip(
                label = stringResource(R.string.separator_add),
                selected = false,
                icon = Icons.Filled.Add,
                accentOutline = true,
                onClick = onAdd
            )
        }
    }
}

@Composable
private fun UnsplitNamesCard(
    names: List<String>,
    onRemove: (String) -> Unit,
    onAdd: () -> Unit
) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
    ) {
        Text(
            text = stringResource(R.string.unsplit_artists_summary),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        if (names.isEmpty()) {
            Text(
                text = stringResource(R.string.unsplit_artists_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        names.forEach { name ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                KateIconButton(onClick = { onRemove(name) }) {
                    KateIcon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.unsplit_artist_remove, name),
                        tint = buttonTint(colors.secondaryText),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAdd)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            KateIcon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.unsplit_artist_add),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.primary,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

@Composable
private fun ResetRow(enabled: Boolean, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    val alpha = if (enabled) 1f else 0.4f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(
                imageVector = Icons.Outlined.RestartAlt,
                contentDescription = null,
                tint = kateTileTint().copy(alpha = alpha),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = stringResource(R.string.row_reset_tags_title),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface.copy(alpha = alpha),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.row_reset_tags_summary),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun TagTextDialog(
    visible: Boolean,
    title: String,
    hint: String,
    maxLength: Int,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    var text by remember(visible) { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val submit = { if (text.isNotBlank()) onConfirm(text) }
    KateDialog(visible = visible, onDismiss = onDismiss) {
        LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .fillMaxWidth()
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
            TextField(
                value = text,
                onValueChange = { text = it.replace("\n", "").take(maxLength) },
                singleLine = true,
                placeholder = { Text(text = hint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceVariant,
                    unfocusedContainerColor = colors.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,
                    cursorColor = colors.primary,
                    focusedPlaceholderColor = colors.secondaryText,
                    unfocusedPlaceholderColor = colors.secondaryText
                ),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_cancel),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.action_add),
                    role = DialogButtonRole.MAIN,
                    enabled = text.isNotBlank(),
                    onClick = { submit() }
                )
            }
        }
    }
}
