package com.jhaiian.kate.ui.settings

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Timer10
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateWindowDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.player.PlayerActions
import com.jhaiian.kate.ui.theme.ClintSwitch
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.LyricsPreferences
import com.jhaiian.kate.ui.theme.PlayerLayoutOptions
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import com.jhaiian.kate.ui.theme.ThemePreferences
import kotlin.math.roundToInt

@Composable
internal fun ActionPickerRow(
    icon: ImageVector,
    title: String,
    current: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    val colors = LocalClintColors.current
    var open by remember { mutableStateOf(false) }
    SettingsRow(
        icon = icon,
        title = title,
        subtitle = stringResource(PlayerActions.label(current)),
        colors = colors,
        onClick = { open = true }
    )
    if (open) {
        TextOptionSheet(
            title = title,
            options = options.map { it to stringResource(PlayerActions.label(it)) },
            current = current,
            onSelect = {
                onSelect(it)
                open = false
            },
            onDismiss = { open = false }
        )
    }
}

@Composable
internal fun PlayerSlotRows(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    ActionPickerRow(
        icon = PlayerActions.icon(settings.playerSlotLeft),
        title = stringResource(R.string.slot_left_title),
        current = settings.playerSlotLeft,
        options = PlayerActions.SideSlots,
        onSelect = { onSettingsChange(settings.copy(playerSlotLeft = it)) }
    )
    RowDivider(colors.divider)
    ActionPickerRow(
        icon = PlayerActions.icon(settings.playerSlotInnerLeft),
        title = stringResource(R.string.slot_inner_left_title),
        current = settings.playerSlotInnerLeft,
        options = PlayerActions.InnerSlots,
        onSelect = { onSettingsChange(settings.copy(playerSlotInnerLeft = it)) }
    )
    RowDivider(colors.divider)
    ActionPickerRow(
        icon = PlayerActions.icon(settings.playerSlotInnerRight),
        title = stringResource(R.string.slot_inner_right_title),
        current = settings.playerSlotInnerRight,
        options = PlayerActions.InnerSlots,
        onSelect = { onSettingsChange(settings.copy(playerSlotInnerRight = it)) }
    )
    RowDivider(colors.divider)
    ActionPickerRow(
        icon = PlayerActions.icon(settings.playerSlotRight),
        title = stringResource(R.string.slot_right_title),
        current = settings.playerSlotRight,
        options = PlayerActions.SideSlots,
        onSelect = { onSettingsChange(settings.copy(playerSlotRight = it)) }
    )
    RowDivider(colors.divider)
    SeekStepRow(settings, onSettingsChange)
}

@Composable
private fun SeekStepRow(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var open by remember { mutableStateOf(false) }
    val labels = PlayerStylePreferences.SeekSteps.associateWith { pluralStringResource(R.plurals.seconds_value, it, it) }
    SettingsRow(
        icon = Icons.Outlined.Timer10,
        title = stringResource(R.string.seek_step_title),
        subtitle = labels[settings.seekStepSeconds].orEmpty(),
        colors = colors,
        onClick = { open = true }
    )
    if (open) {
        TextOptionSheet(
            title = stringResource(R.string.seek_step_title),
            options = PlayerStylePreferences.SeekSteps.map { it.toString() to labels[it].orEmpty() },
            current = settings.seekStepSeconds.toString(),
            onSelect = {
                onSettingsChange(settings.copy(seekStepSeconds = it.toInt()))
                open = false
            },
            onDismiss = { open = false }
        )
    }
}

@Composable
internal fun QuickActionsEditorRow(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var open by remember { mutableStateOf(false) }
    val names = settings.playerQuickList.map { stringResource(PlayerActions.label(it)) }
    SettingsRow(
        icon = Icons.Outlined.Widgets,
        title = stringResource(R.string.quick_actions_edit_title),
        subtitle = names.joinToString(", ").ifEmpty { stringResource(R.string.action_none) },
        colors = colors,
        onClick = { open = true }
    )
    QuickActionsDialog(
        visible = open,
        current = settings.playerQuickList,
        onSave = {
            onSettingsChange(settings.copy(playerQuickList = it))
            open = false
        },
        onDismiss = { open = false }
    )
}

@Composable
private fun QuickActionsDialog(
    visible: Boolean,
    current: List<String>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    var draft by remember(visible, current) { mutableStateOf(current) }
    val ordered = draft + PlayerActions.QuickRow.filter { it !in draft }
    KateWindowDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(R.string.quick_actions_edit_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 4.dp)
            )
            Text(
                text = stringResource(R.string.quick_actions_edit_hint, PlayerActions.MAX_QUICK),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                ordered.forEach { action ->
                    val index = draft.indexOf(action)
                    val enabled = index >= 0
                    val canAdd = enabled || draft.size < PlayerActions.MAX_QUICK
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp)
                            .heightIn(min = 52.dp)
                    ) {
                        KateIcon(
                            imageVector = PlayerActions.icon(action),
                            contentDescription = null,
                            tint = if (enabled) colors.primary else colors.secondaryText,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(PlayerActions.label(action)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(start = 14.dp).alpha(if (canAdd) 1f else 0.5f)
                        )
                        if (enabled) {
                            KateIconButton(
                                onClick = { if (index > 0) draft = draft.toMutableList().apply { add(index - 1, removeAt(index)) } },
                                enabled = index > 0
                            ) {
                                KateIcon(imageVector = Icons.Outlined.KeyboardArrowUp, contentDescription = stringResource(R.string.nav_tab_move_up), tint = buttonTint(colors.secondaryText))
                            }
                            KateIconButton(
                                onClick = { if (index < draft.lastIndex) draft = draft.toMutableList().apply { add(index + 1, removeAt(index)) } },
                                enabled = index < draft.lastIndex
                            ) {
                                KateIcon(imageVector = Icons.Outlined.KeyboardArrowDown, contentDescription = stringResource(R.string.nav_tab_move_down), tint = buttonTint(colors.secondaryText))
                            }
                        }
                        ClintSwitch(
                            checked = enabled,
                            enabled = canAdd,
                            onCheckedChange = { checked -> draft = if (checked) draft + action else draft - action }
                        )
                    }
                }
            }
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 6.dp, bottom = 10.dp)) {
                KateTextButton(label = stringResource(R.string.action_cancel), role = DialogButtonRole.SECONDARY, onClick = onDismiss)
                KateTextButton(label = stringResource(R.string.style_save), role = DialogButtonRole.MAIN, onClick = { onSave(draft) })
            }
        }
    }
}

@Composable
internal fun InfoFieldsRow(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var open by remember { mutableStateOf(false) }
    val labels = mapOf(
        PlayerStylePreferences.INFO_TRACK to stringResource(R.string.info_field_track),
        PlayerStylePreferences.INFO_YEAR to stringResource(R.string.song_info_year),
        PlayerStylePreferences.INFO_GENRE to stringResource(R.string.song_info_genre),
        PlayerStylePreferences.INFO_FORMAT to stringResource(R.string.song_info_format),
        PlayerStylePreferences.INFO_QUALITY to stringResource(R.string.info_field_quality),
        PlayerStylePreferences.INFO_PLAYS to stringResource(R.string.info_field_plays)
    )
    SettingsRow(
        icon = Icons.Outlined.Info,
        title = stringResource(R.string.info_fields_title),
        subtitle = PlayerStylePreferences.InfoFields.filter { it in settings.playerInfoFields }.mapNotNull { labels[it] }
            .joinToString(", ").ifEmpty { stringResource(R.string.info_fields_none) },
        colors = colors,
        onClick = { open = true }
    )
    KateWindowDialog(visible = open, onDismiss = { open = false }) {
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
                text = stringResource(R.string.info_fields_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp)
            )
            PlayerStylePreferences.InfoFields.forEach { field ->
                val checked = field in settings.playerInfoFields
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSettingsChange(settings.copy(playerInfoFields = if (checked) settings.playerInfoFields - field else settings.playerInfoFields + field))
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = labels[field].orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    ClintSwitch(
                        checked = checked,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(playerInfoFields = if (it) settings.playerInfoFields + field else settings.playerInfoFields - field))
                        }
                    )
                }
            }
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 6.dp)) {
                KateTextButton(label = stringResource(R.string.audio_done), role = DialogButtonRole.MAIN, onClick = { open = false })
            }
        }
    }
}

@Composable
internal fun LandscapeLayoutRow(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var open by remember { mutableStateOf(false) }
    val options = listOf(PlayerStylePreferences.LAYOUT_SAME to stringResource(R.string.landscape_layout_same)) +
        PlayerLayoutOptions.map { it.key to stringResource(it.labelRes) }
    SettingsRow(
        icon = Icons.Outlined.ScreenRotation,
        title = stringResource(R.string.landscape_layout_title),
        subtitle = options.firstOrNull { it.first == settings.playerLayoutLandscape }?.second.orEmpty(),
        colors = colors,
        onClick = { open = true }
    )
    if (open) {
        TextOptionSheet(
            title = stringResource(R.string.landscape_layout_title),
            options = options,
            current = settings.playerLayoutLandscape,
            onSelect = {
                onSettingsChange(settings.copy(playerLayoutLandscape = it))
                open = false
            },
            onDismiss = { open = false }
        )
    }
}

private enum class LyricColorTarget { ACTIVE, INACTIVE }

@Composable
internal fun LyricsLookExtras(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var target by remember { mutableStateOf<LyricColorTarget?>(null) }
    ColorRow(
        title = stringResource(R.string.lyrics_active_color),
        subtitle = if (settings.lyricsActiveColor == ThemePreferences.NO_COLOR) stringResource(R.string.color_override_none) else colorHex(settings.lyricsActiveColor),
        swatch = if (settings.lyricsActiveColor == ThemePreferences.NO_COLOR) colors.primary else Color(settings.lyricsActiveColor),
        onClick = { target = LyricColorTarget.ACTIVE }
    )
    RowDivider(colors.divider)
    ColorRow(
        title = stringResource(R.string.lyrics_inactive_color),
        subtitle = if (settings.lyricsInactiveColor == ThemePreferences.NO_COLOR) stringResource(R.string.color_override_none) else colorHex(settings.lyricsInactiveColor),
        swatch = if (settings.lyricsInactiveColor == ThemePreferences.NO_COLOR) colors.onSurface else Color(settings.lyricsInactiveColor),
        onClick = { target = LyricColorTarget.INACTIVE }
    )
    RowDivider(colors.divider)
    FineTuneSlider(
        title = stringResource(R.string.fine_tune_lyrics_size),
        value = LyricsPreferences.textScale(settings.lyricsTextSize),
        range = LyricsPreferences.MIN_TEXT_SCALE..LyricsPreferences.MAX_TEXT_SCALE,
        format = ::percentLabel,
        onCommit = { onSettingsChange(settings.copy(lyricsTextSize = com.jhaiian.kate.ui.theme.FineTune.key(it))) }
    )
    FineTuneSlider(
        title = stringResource(R.string.lyrics_line_spacing),
        value = settings.lyricsLineSpacing / 100f,
        range = LyricsPreferences.MIN_LINE_SPACING / 100f..LyricsPreferences.MAX_LINE_SPACING / 100f,
        format = ::percentLabel,
        onCommit = { onSettingsChange(settings.copy(lyricsLineSpacing = (it * 100).roundToInt())) }
    )
    Spacer(modifier = Modifier.height(6.dp))
    val current = target
    ColorPickerDialog(
        visible = current != null,
        title = stringResource(if (current == LyricColorTarget.INACTIVE) R.string.lyrics_inactive_color else R.string.lyrics_active_color),
        initial = when (current) {
            LyricColorTarget.ACTIVE -> settings.lyricsActiveColor.takeIf { it != ThemePreferences.NO_COLOR } ?: colors.primary.toArgb()
            LyricColorTarget.INACTIVE -> settings.lyricsInactiveColor.takeIf { it != ThemePreferences.NO_COLOR } ?: colors.onSurface.toArgb()
            null -> colors.primary.toArgb()
        },
        resetLabel = stringResource(R.string.color_use_theme),
        onPick = { value ->
            onSettingsChange(if (current == LyricColorTarget.INACTIVE) settings.copy(lyricsInactiveColor = value) else settings.copy(lyricsActiveColor = value))
            target = null
        },
        onReset = {
            onSettingsChange(
                if (current == LyricColorTarget.INACTIVE) settings.copy(lyricsInactiveColor = ThemePreferences.NO_COLOR)
                else settings.copy(lyricsActiveColor = ThemePreferences.NO_COLOR)
            )
            target = null
        },
        onDismiss = { target = null }
    )
}
