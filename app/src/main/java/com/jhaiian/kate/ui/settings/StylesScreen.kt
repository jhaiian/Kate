package com.jhaiian.kate.ui.settings

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.jhaiian.kate.R
import com.jhaiian.kate.data.OutputRoute
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.library.PlaylistNameDialog
import com.jhaiian.kate.ui.theme.BuiltInStyles
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.SavedStyle
import com.jhaiian.kate.ui.theme.StyleRules
import com.jhaiian.kate.ui.theme.StyleStore
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar

private sealed interface StyleSheet {
    data class Route(val route: String) : StyleSheet
    data object Day : StyleSheet
    data object Night : StyleSheet
    data object NightStart : StyleSheet
    data object NightEnd : StyleSheet
}

private fun hourLabel(hour: Int): String =
    LocalTime.of(hour, 0).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

@Composable
fun StylesScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { StyleStore.ensureLoaded(context) }
    val styles = StyleStore.styles
    val rules = StyleStore.rules
    var naming by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf<SavedStyle?>(null) }
    var selected by remember { mutableStateOf<SavedStyle?>(null) }
    var exporting by remember { mutableStateOf<SavedStyle?>(null) }
    var sheet by remember { mutableStateOf<StyleSheet?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    val appliedMessage = stringResource(R.string.style_applied)
    val savedMessage = stringResource(R.string.style_saved)
    val importedMessage = stringResource(R.string.style_imported)
    val importFailedMessage = stringResource(R.string.style_import_failed)
    val exportedMessage = stringResource(R.string.style_exported)
    val exportFailedMessage = stringResource(R.string.style_export_failed)
    val fullMessage = stringResource(R.string.style_limit_reached)
    val shareTitle = stringResource(R.string.style_share)

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val imported = StyleStore.import(context, uri)
            Toast.makeText(context, if (imported != null) importedMessage else importFailedMessage, Toast.LENGTH_SHORT).show()
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val style = exporting
        exporting = null
        if (uri != null && style != null) {
            val ok = StyleStore.export(context, uri, style)
            Toast.makeText(context, if (ok) exportedMessage else exportFailedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun applyStyle(target: LookAndFeelSettings) {
        onSettingsChange(StyleStore.sanitize(context, target))
        Toast.makeText(context, appliedMessage, Toast.LENGTH_SHORT).show()
    }

    fun styleName(id: String): String =
        StyleStore.find(id)?.name ?: context.getString(R.string.style_rule_none)

    LookAndFeelPage(
        title = stringResource(R.string.styles_title),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.style_rules_reset_message),
                onConfirm = {
                    confirmReset = false
                    StyleStore.setRules(context, StyleRules())
                },
                onDismiss = { confirmReset = false }
            )
            PlaylistNameDialog(
                visible = naming,
                title = stringResource(R.string.style_save_title),
                initialName = "",
                confirmLabel = stringResource(R.string.style_save),
                fieldLabel = stringResource(R.string.style_name),
                onConfirm = { name ->
                    naming = false
                    val saved = StyleStore.save(context, name, settings)
                    Toast.makeText(context, if (saved != null) savedMessage else fullMessage, Toast.LENGTH_SHORT).show()
                },
                onDismiss = { naming = false }
            )
            PlaylistNameDialog(
                visible = renaming != null,
                title = stringResource(R.string.style_rename),
                initialName = renaming?.name.orEmpty(),
                confirmLabel = stringResource(R.string.style_save),
                fieldLabel = stringResource(R.string.style_name),
                onConfirm = { name ->
                    renaming?.let { StyleStore.rename(context, it.id, name) }
                    renaming = null
                },
                onDismiss = { renaming = null }
            )
            StyleActionsDialog(
                style = selected,
                onApply = { style ->
                    selected = null
                    applyStyle(style.settings)
                },
                onUpdate = { style ->
                    selected = null
                    StyleStore.overwrite(context, style.id, settings)
                    Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                },
                onRename = { style ->
                    selected = null
                    renaming = style
                },
                onExport = { style ->
                    selected = null
                    exporting = style
                    exportLauncher.launch("${style.name}.kate-style.json")
                },
                onShare = { style ->
                    selected = null
                    val file = StyleStore.shareFile(context, style)
                    if (file != null) {
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        runCatching { context.startActivity(Intent.createChooser(send, shareTitle)) }
                    } else {
                        Toast.makeText(context, exportFailedMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = { style ->
                    selected = null
                    StyleStore.delete(context, style.id)
                },
                onDismiss = { selected = null }
            )
        }
    ) {
        SectionHeader(stringResource(R.string.styles_built_in), colors.primary)
        SettingsCard {
            BuiltInStyles.forEachIndexed { index, style ->
                if (index > 0) RowDivider(colors.divider)
                SettingsRow(
                    icon = Icons.Outlined.Style,
                    title = stringResource(style.nameRes),
                    subtitle = stringResource(style.descriptionRes),
                    colors = colors,
                    onClick = { applyStyle(style.transform(settings)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.styles_yours), colors.primary)
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Add,
                title = stringResource(R.string.style_save_current),
                subtitle = stringResource(R.string.style_save_current_summary),
                colors = colors,
                onClick = { naming = true }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.FileOpen,
                title = stringResource(R.string.style_import),
                subtitle = stringResource(R.string.style_import_summary),
                colors = colors,
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }
            )
            styles.forEach { style ->
                RowDivider(colors.divider)
                SettingsRow(
                    icon = Icons.Outlined.Style,
                    title = style.name,
                    subtitle = stringResource(R.string.style_tap_options),
                    colors = colors,
                    onClick = { selected = style }
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.styles_auto_switch), colors.primary)
        SettingsCard {
            val routeIcons = mapOf(
                OutputRoute.SPEAKER to Icons.Outlined.Speaker,
                OutputRoute.WIRED to Icons.Outlined.Headphones,
                OutputRoute.BLUETOOTH to Icons.Outlined.Bluetooth,
                OutputRoute.USB to Icons.Outlined.Usb
            )
            StyleStore.Routes.forEachIndexed { index, route ->
                if (index > 0) RowDivider(colors.divider)
                SettingsRow(
                    icon = routeIcons.getValue(route),
                    title = stringResource(R.string.style_rule_route, routeLabel(route)),
                    subtitle = rules.routes[route]?.let { styleName(it) } ?: stringResource(R.string.style_rule_none),
                    colors = colors,
                    onClick = { sheet = StyleSheet.Route(route) }
                )
            }
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.WbSunny,
                title = stringResource(R.string.style_rule_day),
                subtitle = rules.dayStyle.takeIf { it.isNotEmpty() }?.let { styleName(it) } ?: stringResource(R.string.style_rule_none),
                colors = colors,
                onClick = { sheet = StyleSheet.Day }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Bedtime,
                title = stringResource(R.string.style_rule_night),
                subtitle = rules.nightStyle.takeIf { it.isNotEmpty() }?.let { styleName(it) } ?: stringResource(R.string.style_rule_none),
                colors = colors,
                onClick = { sheet = StyleSheet.Night }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Schedule,
                title = stringResource(R.string.style_rule_night_hours),
                subtitle = stringResource(R.string.style_rule_hours_value, hourLabel(rules.nightStart), hourLabel(rules.nightEnd)),
                colors = colors,
                onClick = { sheet = StyleSheet.NightStart }
            )
            Text(
                text = stringResource(R.string.style_rule_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
    }

    val styleChoices = listOf("" to stringResource(R.string.style_rule_none)) + styles.map { it.id to it.name }
    val hourChoices = (0..23).map { it.toString() to hourLabel(it) }
    when (val open = sheet) {
        is StyleSheet.Route -> TextOptionSheet(
            title = stringResource(R.string.style_rule_route, routeLabel(open.route)),
            options = styleChoices,
            current = rules.routes[open.route].orEmpty(),
            onSelect = { id ->
                val routes = if (id.isEmpty()) rules.routes - open.route else rules.routes + (open.route to id)
                StyleStore.setRules(context, rules.copy(routes = routes))
                sheet = null
            },
            onDismiss = { sheet = null }
        )
        StyleSheet.Day -> TextOptionSheet(
            title = stringResource(R.string.style_rule_day),
            options = styleChoices,
            current = rules.dayStyle,
            onSelect = { StyleStore.setRules(context, rules.copy(dayStyle = it)); sheet = null },
            onDismiss = { sheet = null }
        )
        StyleSheet.Night -> TextOptionSheet(
            title = stringResource(R.string.style_rule_night),
            options = styleChoices,
            current = rules.nightStyle,
            onSelect = { StyleStore.setRules(context, rules.copy(nightStyle = it)); sheet = null },
            onDismiss = { sheet = null }
        )
        StyleSheet.NightStart -> TextOptionSheet(
            title = stringResource(R.string.style_rule_night_starts),
            options = hourChoices,
            current = rules.nightStart.toString(),
            onSelect = { StyleStore.setRules(context, rules.copy(nightStart = it.toInt())); sheet = StyleSheet.NightEnd },
            onDismiss = { sheet = null }
        )
        StyleSheet.NightEnd -> TextOptionSheet(
            title = stringResource(R.string.style_rule_night_ends),
            options = hourChoices,
            current = rules.nightEnd.toString(),
            onSelect = { StyleStore.setRules(context, rules.copy(nightEnd = it.toInt())); sheet = null },
            onDismiss = { sheet = null }
        )
        null -> {}
    }
}

@Composable
private fun StyleActionsDialog(
    style: SavedStyle?,
    onApply: (SavedStyle) -> Unit,
    onUpdate: (SavedStyle) -> Unit,
    onRename: (SavedStyle) -> Unit,
    onExport: (SavedStyle) -> Unit,
    onShare: (SavedStyle) -> Unit,
    onDelete: (SavedStyle) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    var last by remember { mutableStateOf(style) }
    if (style != null) last = style
    val shown = last
    KateDialog(visible = style != null, onDismiss = onDismiss) {
        if (shown != null) {
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
                    text = shown.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp)
                )
                ActionRow(Icons.Outlined.Check, stringResource(R.string.style_apply)) { onApply(shown) }
                ActionRow(Icons.Outlined.Save, stringResource(R.string.style_update)) { onUpdate(shown) }
                ActionRow(Icons.Outlined.Edit, stringResource(R.string.style_rename)) { onRename(shown) }
                ActionRow(Icons.Outlined.Upload, stringResource(R.string.style_export)) { onExport(shown) }
                ActionRow(Icons.Outlined.Share, stringResource(R.string.style_share)) { onShare(shown) }
                ActionRow(Icons.Outlined.Delete, stringResource(R.string.action_delete), destructive = true) { onDelete(shown) }
            }
        }
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, destructive: Boolean = false, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    val tint = if (destructive) colors.colorError else colors.onSurface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        KateIcon(imageVector = icon, contentDescription = null, tint = if (destructive) tint else kateTileTint(), modifier = Modifier.kateIconTile(36.dp).padding(8.dp).size(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
            modifier = Modifier.padding(start = 14.dp)
        )
    }
}

@Composable
fun StyleAutoSwitch(current: LookAndFeelSettings, onApply: (LookAndFeelSettings) -> Unit) {
    val context = LocalContext.current
    var hour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    LaunchedEffect(Unit) {
        StyleStore.ensureLoaded(context)
        while (true) {
            delay(30_000L)
            hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        }
    }
    val target = StyleStore.targetStyle(OutputRoute.current, hour)
    LaunchedEffect(target) {
        val last = StyleStore.lastAuto(context)
        if (target == null) {
            if (last.isNotEmpty()) {
                StyleStore.setLastAuto(context, "")
                StyleStore.takeSnapshot(context)?.let { onApply(StyleStore.sanitize(context, it)) }
            }
            return@LaunchedEffect
        }
        if (target == last) return@LaunchedEffect
        val style = StyleStore.find(target) ?: return@LaunchedEffect
        if (last.isEmpty()) StyleStore.saveSnapshot(context, current)
        StyleStore.setLastAuto(context, target)
        onApply(StyleStore.sanitize(context, style.settings))
    }
}
