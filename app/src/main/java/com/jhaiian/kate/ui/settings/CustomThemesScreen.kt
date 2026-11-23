package com.jhaiian.kate.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.theme.CustomPalette
import com.jhaiian.kate.ui.theme.CustomTheme
import com.jhaiian.kate.ui.theme.CustomThemeRepository
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.MAX_CUSTOM_THEME_DESCRIPTION_LENGTH
import com.jhaiian.kate.ui.theme.MAX_CUSTOM_THEME_NAME_LENGTH
import com.jhaiian.kate.ui.theme.ThemePreferences
import com.jhaiian.kate.ui.theme.ThemeSlot
import com.jhaiian.kate.ui.theme.ThemeSlotGroup
import com.jhaiian.kate.ui.theme.customAccentFor

private class EditorSession(val theme: CustomTheme, val isNew: Boolean)

private const val ActionEdit = "edit"
private const val ActionDuplicate = "duplicate"
private const val ActionDelete = "delete"

@Composable
fun CustomThemesScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themes = remember { mutableStateListOf<CustomTheme>().apply { addAll(CustomThemeRepository.all(context)) } }
    var session by remember { mutableStateOf<EditorSession?>(null) }
    var pendingDelete by remember { mutableStateOf<CustomTheme?>(null) }
    var actionsFor by remember { mutableStateOf<CustomTheme?>(null) }

    fun reload() {
        themes.clear()
        themes.addAll(CustomThemeRepository.all(context))
    }

    val editing = session
    if (editing != null) {
        CustomThemeEditor(
            initial = editing.theme,
            isNew = editing.isNew,
            takenNames = themes.filter { it.id != editing.theme.id }.map { it.name },
            modifier = modifier,
            onSave = { saved ->
                if (CustomThemeRepository.save(context, saved)) {
                    reload()
                    session = null
                }
            },
            onExit = { session = null }
        )
    } else {
        CustomThemesList(
            themes = themes,
            activeAccent = settings.accent,
            modifier = modifier,
            onBack = onBack,
            onCreate = {
                var index = themes.size + 1
                var name = context.getString(R.string.custom_theme_default_name, index)
                while (CustomThemeRepository.isNameTaken(context, name)) {
                    index++
                    name = context.getString(R.string.custom_theme_default_name, index)
                }
                session = EditorSession(CustomThemeRepository.createFromCurrent(context, name), true)
            },
            onApply = { theme -> onSettingsChange(settings.copy(accent = customAccentFor(theme.id))) },
            onEdit = { theme -> session = EditorSession(theme, false) },
            onMore = { theme -> actionsFor = theme }
        )
    }

    actionsFor?.let { theme ->
        TextOptionSheet(
            title = theme.name,
            options = listOf(
                ActionEdit to stringResource(R.string.custom_theme_edit),
                ActionDuplicate to stringResource(R.string.custom_theme_duplicate),
                ActionDelete to stringResource(R.string.custom_theme_delete)
            ),
            current = "",
            onSelect = { action ->
                actionsFor = null
                when (action) {
                    ActionEdit -> session = EditorSession(theme, false)
                    ActionDuplicate -> {
                        val name = CustomThemeRepository.uniqueName(context, context.getString(R.string.custom_theme_copy_name, theme.name))
                        CustomThemeRepository.save(context, theme.copy(id = CustomThemeRepository.newId(), name = name))
                        reload()
                    }
                    ActionDelete -> pendingDelete = theme
                }
            },
            onDismiss = { actionsFor = null }
        )
    }

    ConfirmDialog(
        visible = pendingDelete != null,
        title = stringResource(R.string.custom_theme_delete_title),
        message = stringResource(R.string.custom_theme_delete_message, pendingDelete?.name.orEmpty()),
        confirmLabel = stringResource(R.string.custom_theme_delete),
        destructive = true,
        onConfirm = {
            pendingDelete?.let { theme ->
                if (settings.accent == customAccentFor(theme.id)) {
                    onSettingsChange(settings.copy(accent = ThemePreferences.DEFAULT_ACCENT))
                }
                CustomThemeRepository.delete(context, theme.id)
                reload()
            }
            pendingDelete = null
        },
        onDismiss = { pendingDelete = null }
    )
}

@Composable
private fun ThemePageHeader(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)
    ) {
        KateIconButton(onClick = onBack) {
            KateIcon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = buttonTint(colors.onSurface))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
private fun CustomThemesList(
    themes: List<CustomTheme>,
    activeAccent: String,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onApply: (CustomTheme) -> Unit,
    onEdit: (CustomTheme) -> Unit,
    onMore: (CustomTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val bottomPadding = LocalBottomContentPadding.current
    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ThemePageHeader(title = stringResource(R.string.custom_themes_title), onBack = onBack)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = bottomPadding)
            ) {
                Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                    SettingsCard {
                        Text(
                            text = stringResource(R.string.custom_themes_intro),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.secondaryText,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsCard {
                        SettingsRow(
                            icon = Icons.Filled.Add,
                            title = stringResource(R.string.custom_theme_create),
                            subtitle = stringResource(R.string.custom_theme_create_hint),
                            colors = colors,
                            onClick = onCreate
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    if (themes.isEmpty()) {
                        EmptyThemes()
                    } else {
                        SettingsCard {
                            themes.forEachIndexed { index, theme ->
                                if (index > 0) RowDivider(colors.divider)
                                ThemeRow(
                                    theme = theme,
                                    active = activeAccent == customAccentFor(theme.id),
                                    onApply = { onApply(theme) },
                                    onEdit = { onEdit(theme) },
                                    onMore = { onMore(theme) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyThemes() {
    val colors = LocalClintColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        KateIcon(
            imageVector = Icons.Outlined.ColorLens,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(R.string.custom_theme_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp)
        )
        Text(
            text = stringResource(R.string.custom_theme_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun ThemeRow(theme: CustomTheme, active: Boolean, onApply: () -> Unit, onEdit: () -> Unit, onMore: () -> Unit) {
    val colors = LocalClintColors.current
    val palette = theme.palette(colors.isLight)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onApply)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(palette.color(ThemeSlot.BACKGROUND))
                .border(BorderStroke(1.dp, colors.divider), RoundedCornerShape(12.dp))
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(palette.color(ThemeSlot.CARD_BACKGROUND))
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(palette.color(ThemeSlot.PRIMARY))
            )
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(
                text = theme.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (theme.description.isNotBlank()) {
                Text(
                    text = theme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (active || theme.description.isBlank()) {
                Text(
                    text = stringResource(if (active) R.string.custom_theme_active else R.string.custom_theme_tap_to_apply),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) colors.primary else colors.secondaryText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        KateIconButton(onClick = onEdit) {
            KateIcon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = stringResource(R.string.custom_theme_edit),
                tint = buttonTint(colors.secondaryText)
            )
        }
        KateIconButton(onClick = onMore) {
            KateIcon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.custom_theme_more),
                tint = buttonTint(colors.secondaryText)
            )
        }
    }
}

@Composable
private fun CustomThemeEditor(
    initial: CustomTheme,
    isNew: Boolean,
    takenNames: List<String>,
    onSave: (CustomTheme) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    var name by remember { mutableStateOf(initial.name) }
    var description by remember { mutableStateOf(initial.description) }
    var light by remember { mutableStateOf(initial.light) }
    var dark by remember { mutableStateOf(initial.dark) }
    var editingLight by remember { mutableStateOf(colors.isLight) }
    var tab by remember { mutableIntStateOf(0) }
    var pickerSlot by remember { mutableStateOf<ThemeSlot?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }

    val palette = if (editingLight) light else dark
    val initialPalette = if (editingLight) initial.light else initial.dark
    val nameTaken = takenNames.any { it.trim().equals(name.trim(), ignoreCase = true) }
    val dirty = name != initial.name || description != initial.description || light != initial.light || dark != initial.dark
    val modeDirty = palette != initialPalette
    val canSave = name.isNotBlank() && !nameTaken && (dirty || isNew)
    val leave: () -> Unit = { if (dirty) confirmDiscard = true else onExit() }

    BackHandler(onBack = leave)

    val fields: @Composable () -> Unit = {
        ThemeTextField(
            value = name,
            label = stringResource(R.string.custom_theme_name_label),
            isError = name.isBlank() || nameTaken,
            singleLine = true,
            onValueChange = { if (it.length <= MAX_CUSTOM_THEME_NAME_LENGTH) name = it }
        )
        if (name.isBlank() || nameTaken) {
            Text(
                text = stringResource(if (name.isBlank()) R.string.custom_theme_name_required else R.string.custom_theme_name_duplicate),
                style = MaterialTheme.typography.bodySmall,
                color = colors.colorError,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        ThemeTextField(
            value = description,
            label = stringResource(R.string.custom_theme_description_label),
            isError = false,
            singleLine = false,
            onValueChange = { input ->
                val clean = input.replace('\n', ' ')
                if (clean.length <= MAX_CUSTOM_THEME_DESCRIPTION_LENGTH) description = clean
            }
        )
        Spacer(modifier = Modifier.height(14.dp))
        SegmentedToggle(
            options = listOf(stringResource(R.string.custom_theme_mode_light), stringResource(R.string.custom_theme_mode_dark)),
            selected = if (editingLight) 0 else 1,
            onSelect = { editingLight = it == 0 }
        )
        Text(
            text = stringResource(R.string.custom_theme_palette_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp)
        )
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            ThemePageHeader(
                title = stringResource(if (isNew) R.string.custom_theme_editor_new_title else R.string.custom_theme_editor_edit_title),
                onBack = leave,
                actions = {
                    KateIconButton(
                        enabled = modeDirty,
                        onClick = { if (editingLight) light = initial.light else dark = initial.dark }
                    ) {
                        KateIcon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = stringResource(R.string.custom_theme_reset),
                            tint = buttonTint(colors.secondaryText).copy(alpha = if (modeDirty) 1f else 0.38f)
                        )
                    }
                    KateTextButton(
                        label = stringResource(R.string.custom_theme_save),
                        role = DialogButtonRole.MAIN,
                        enabled = canSave,
                        onClick = { onSave(CustomTheme(initial.id, name.trim(), light, dark, description.trim())) }
                    )
                }
            )
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth().imePadding()) {
                if (maxWidth >= 840.dp) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                        ) {
                            fields()
                            Spacer(modifier = Modifier.height(8.dp))
                            ColorSlotGroups(palette) { pickerSlot = it }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                        ) {
                            ThemePreviewPage(palette, editingLight)
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth()) {
                            fields()
                            Spacer(modifier = Modifier.height(14.dp))
                            SegmentedToggle(
                                options = listOf(stringResource(R.string.custom_theme_tab_colors), stringResource(R.string.custom_theme_tab_preview)),
                                selected = tab,
                                onSelect = { tab = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (tab == 0) {
                                ColorSlotGroups(palette) { pickerSlot = it }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                ThemePreviewPage(palette, editingLight)
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    val slot = pickerSlot
    ColorPickerDialog(
        visible = slot != null,
        title = slot?.let { stringResource(it.titleRes) }.orEmpty(),
        initial = slot?.let { palette[it] } ?: ThemePreferences.DEFAULT_CUSTOM_ACCENT,
        resetLabel = stringResource(R.string.custom_theme_color_restore),
        onPick = { argb ->
            slot?.let { if (editingLight) light = light.with(it, argb) else dark = dark.with(it, argb) }
            pickerSlot = null
        },
        onReset = {
            slot?.let {
                if (editingLight) light = light.with(it, initial.light[it]) else dark = dark.with(it, initial.dark[it])
            }
            pickerSlot = null
        },
        onDismiss = { pickerSlot = null }
    )

    ConfirmDialog(
        visible = confirmDiscard,
        title = stringResource(R.string.custom_theme_discard_title),
        message = stringResource(R.string.custom_theme_discard_message),
        confirmLabel = stringResource(R.string.custom_theme_discard_action),
        destructive = true,
        onConfirm = {
            confirmDiscard = false
            onExit()
        },
        onDismiss = { confirmDiscard = false }
    )
}

@Composable
private fun ThemeTextField(
    value: String,
    label: String,
    isError: Boolean,
    singleLine: Boolean,
    onValueChange: (String) -> Unit
) {
    val colors = LocalClintColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 2,
        maxLines = if (singleLine) 1 else 3,
        isError = isError,
        label = { Text(label) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.onSurface,
            unfocusedTextColor = colors.onSurface,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.divider,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.secondaryText,
            cursorColor = colors.primary,
            errorBorderColor = colors.colorError,
            errorLabelColor = colors.colorError,
            errorTextColor = colors.onSurface,
            errorCursorColor = colors.colorError
        ),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )
}

@Composable
private fun SegmentedToggle(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val colors = LocalClintColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) colors.primary else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) })
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.onPrimary else colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorSlotGroups(palette: CustomPalette, onPick: (ThemeSlot) -> Unit) {
    val colors = LocalClintColors.current
    ThemeSlotGroup.entries.forEach { group ->
        Text(
            text = stringResource(group.titleRes).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 10.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.cardBackground)
        ) {
            ThemeSlot.entries.filter { it.group == group }.forEachIndexed { index, slot ->
                if (index > 0) RowDivider(colors.divider)
                ColorRow(
                    title = stringResource(slot.titleRes),
                    subtitle = colorHex(palette[slot]),
                    swatch = palette.color(slot),
                    onClick = { onPick(slot) }
                )
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            Text(
                text = message,
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
                    label = confirmLabel,
                    role = if (destructive) DialogButtonRole.DESTRUCTIVE else DialogButtonRole.MAIN,
                    onClick = onConfirm
                )
            }
        }
    }
}
