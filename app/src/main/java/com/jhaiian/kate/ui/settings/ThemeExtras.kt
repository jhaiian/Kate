package com.jhaiian.kate.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateWindowDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateRowArrow
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.MatchAppSystemBars
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.components.rememberSheetFlingConnection
import com.jhaiian.kate.ui.theme.AccentOptions
import com.jhaiian.kate.ui.theme.ContrastOptions
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.PaletteStyleOptions
import com.jhaiian.kate.ui.theme.PlayerStyleOption
import com.jhaiian.kate.ui.theme.ThemePreferences
import com.jhaiian.kate.ui.theme.customAccentLabel
import com.jhaiian.kate.ui.theme.isCustomAccent
import java.util.Locale

private val QuickColors = listOf(
    0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF5E35B1, 0xFF3949AB, 0xFF1E88E5,
    0xFF00ACC1, 0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFFFDD835, 0xFFFB8C00,
    0xFF6D4C41, 0xFF546E7A, 0xFF101014, 0xFFF5F5F7
).map { it.toInt() }

internal fun colorHex(argb: Int): String = String.format(Locale.US, "#%06X", argb and 0xFFFFFF)

internal fun parseColorHex(text: String): Int? {
    val digits = text.trim().removePrefix("#")
    if (digits.length != 6 || !digits.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
    return digits.toLong(16).toInt() or 0xFF000000.toInt()
}

@Composable
internal fun accentLabel(accent: String): String =
    if (isCustomAccent(accent)) {
        customAccentLabel(LocalContext.current, accent)
    } else {
        stringResource(AccentOptions.first { it.key == accent }.labelRes)
    }

private enum class ColorTarget(@StringRes val titleRes: Int) {
    ACCENT(R.string.custom_color_title),
    BACKGROUND(R.string.color_override_background),
    CARD(R.string.color_override_card),
    TEXT(R.string.color_override_text),
    SECONDARY_TEXT(R.string.color_override_secondary_text)
}

private enum class EngineSheet { STYLE, CONTRAST }

@Composable
internal fun ColorEngineSection(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var target by remember { mutableStateOf<ColorTarget?>(null) }
    var sheet by remember { mutableStateOf<EngineSheet?>(null) }
    val styleApplies = settings.accent == "material_you" || settings.accent == ThemePreferences.ACCENT_CUSTOM ||
        settings.appArtworkColors || settings.playerArtworkColors

    Spacer(modifier = Modifier.height(20.dp))
    SectionHeader(stringResource(R.string.look_section_color_engine), colors.primary)
    SettingsCard {
        if (settings.accent == ThemePreferences.ACCENT_CUSTOM) {
            ColorRow(
                title = stringResource(R.string.custom_color_title),
                subtitle = colorHex(settings.customAccent),
                swatch = Color(settings.customAccent),
                onClick = { target = ColorTarget.ACCENT }
            )
            RowDivider(colors.divider)
        }
        SettingsRow(
            icon = Icons.Outlined.AutoAwesome,
            title = stringResource(R.string.palette_style_title),
            subtitle = stringResource(PaletteStyleOptions.first { it.key == settings.paletteStyle }.labelRes),
            colors = colors,
            onClick = { sheet = EngineSheet.STYLE }
        )
        RowDivider(colors.divider)
        SettingsRow(
            icon = Icons.Outlined.Contrast,
            title = stringResource(R.string.contrast_title),
            subtitle = stringResource(ContrastOptions.first { it.key == settings.contrast }.labelRes),
            colors = colors,
            onClick = { sheet = EngineSheet.CONTRAST }
        )
        Text(
            text = stringResource(if (styleApplies) R.string.palette_style_hint else R.string.palette_style_hint_named),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
    SectionHeader(stringResource(R.string.look_section_color_overrides), colors.primary)
    SettingsCard {
        val rows = listOf(
            ColorTarget.BACKGROUND to settings.colorBackground,
            ColorTarget.CARD to settings.colorCard,
            ColorTarget.TEXT to settings.colorText,
            ColorTarget.SECONDARY_TEXT to settings.colorSecondaryText
        )
        rows.forEachIndexed { index, (rowTarget, value) ->
            if (index > 0) RowDivider(colors.divider)
            ColorRow(
                title = stringResource(rowTarget.titleRes),
                subtitle = if (value == ThemePreferences.NO_COLOR) stringResource(R.string.color_override_none) else colorHex(value),
                swatch = if (value == ThemePreferences.NO_COLOR) null else Color(value),
                onClick = { target = rowTarget }
            )
        }
        Text(
            text = stringResource(R.string.color_override_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        )
    }

    val current = target
    val initial = when (current) {
        ColorTarget.ACCENT -> settings.customAccent
        ColorTarget.BACKGROUND -> settings.colorBackground.takeIf { it != ThemePreferences.NO_COLOR } ?: colors.background.toArgb()
        ColorTarget.CARD -> settings.colorCard.takeIf { it != ThemePreferences.NO_COLOR } ?: colors.cardBackground.toArgb()
        ColorTarget.TEXT -> settings.colorText.takeIf { it != ThemePreferences.NO_COLOR } ?: colors.onSurface.toArgb()
        ColorTarget.SECONDARY_TEXT -> settings.colorSecondaryText.takeIf { it != ThemePreferences.NO_COLOR } ?: colors.secondaryText.toArgb()
        null -> ThemePreferences.DEFAULT_CUSTOM_ACCENT
    }
    fun store(rowTarget: ColorTarget, value: Int): LookAndFeelSettings = when (rowTarget) {
        ColorTarget.ACCENT -> settings.copy(customAccent = if (value == ThemePreferences.NO_COLOR) ThemePreferences.DEFAULT_CUSTOM_ACCENT else value)
        ColorTarget.BACKGROUND -> settings.copy(colorBackground = value)
        ColorTarget.CARD -> settings.copy(colorCard = value)
        ColorTarget.TEXT -> settings.copy(colorText = value)
        ColorTarget.SECONDARY_TEXT -> settings.copy(colorSecondaryText = value)
    }
    ColorPickerDialog(
        visible = current != null,
        title = current?.let { stringResource(it.titleRes) }.orEmpty(),
        initial = initial,
        resetLabel = stringResource(if (current == ColorTarget.ACCENT) R.string.color_reset_default else R.string.color_use_theme),
        onPick = { value ->
            current?.let { onSettingsChange(store(it, value)) }
            target = null
        },
        onReset = {
            current?.let { onSettingsChange(store(it, ThemePreferences.NO_COLOR)) }
            target = null
        },
        onDismiss = { target = null }
    )

    when (sheet) {
        EngineSheet.STYLE -> OptionSheet(
            title = stringResource(R.string.palette_style_title),
            options = PaletteStyleOptions,
            current = settings.paletteStyle,
            onSelect = { onSettingsChange(settings.copy(paletteStyle = it)); sheet = null },
            onDismiss = { sheet = null }
        )
        EngineSheet.CONTRAST -> OptionSheet(
            title = stringResource(R.string.contrast_title),
            options = ContrastOptions,
            current = settings.contrast,
            onSelect = { onSettingsChange(settings.copy(contrast = it)); sheet = null },
            onDismiss = { sheet = null }
        )
        null -> {}
    }
}

@Composable
internal fun ColorRow(title: String, subtitle: String, swatch: Color?, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .settingsSpotlight(title)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(swatch ?: colors.surfaceVariant)
                .border(BorderStroke(1.dp, colors.divider), CircleShape)
        )
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface, fontWeight = FontWeight.Medium)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        KateRowArrow()
    }
}

@Composable
internal fun OptionSheet(
    title: String,
    options: List<PlayerStyleOption>,
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    TextOptionSheet(
        title = title,
        options = options.map { it.key to stringResource(it.labelRes) },
        current = current,
        onSelect = onSelect,
        onDismiss = onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TextOptionSheet(
    title: String,
    options: List<Pair<String, String>>,
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = colors.cardBackground) {
        MatchAppSystemBars()
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .nestedScroll(rememberSheetFlingConnection(sheetState, consumeFling = false))
                .verticalScroll(rememberScrollState())
        ) {
            options.forEach { (key, label) ->
                val selected = key == current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .kateChoice(selected)
                        .clickable { onSelect(key) }
                        .padding(14.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = kateChoiceText(selected),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (selected) {
                        KateIcon(imageVector = Icons.Filled.Check, contentDescription = null, tint = colors.primary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun ColorPickerDialog(
    visible: Boolean,
    title: String,
    initial: Int,
    resetLabel: String,
    onPick: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    val start = remember(visible, initial) { FloatArray(3).also { android.graphics.Color.colorToHSV(initial, it) } }
    var hue by remember(visible, initial) { mutableFloatStateOf(start[0]) }
    var saturation by remember(visible, initial) { mutableFloatStateOf(start[1]) }
    var brightness by remember(visible, initial) { mutableFloatStateOf(start[2]) }
    var hex by remember(visible, initial) { mutableStateOf(colorHex(initial)) }
    val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
    fun setFrom(value: Int) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(value, hsv)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
    }
    KateWindowDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .imePadding()
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 12.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(argb))
                        .border(BorderStroke(1.dp, colors.divider), RoundedCornerShape(16.dp))
                )
                OutlinedTextField(
                    value = hex,
                    onValueChange = { text ->
                        hex = text.take(7)
                        parseColorHex(text)?.let { setFrom(it) }
                    },
                    singleLine = true,
                    label = { Text(stringResource(R.string.color_hex_label)) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.divider,
                        focusedLabelColor = colors.primary,
                        unfocusedLabelColor = colors.secondaryText,
                        cursorColor = colors.primary
                    ),
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            GradientSlider(
                label = stringResource(R.string.color_hue),
                value = hue,
                range = 0f..360f,
                brush = Brush.horizontalGradient(listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f).map { Color.hsv(it, 1f, 1f) }),
                onValueChange = {
                    hue = it
                    hex = colorHex(android.graphics.Color.HSVToColor(floatArrayOf(it, saturation, brightness)))
                }
            )
            GradientSlider(
                label = stringResource(R.string.color_saturation),
                value = saturation,
                range = 0f..1f,
                brush = Brush.horizontalGradient(listOf(Color.hsv(hue, 0f, brightness), Color.hsv(hue, 1f, brightness))),
                onValueChange = {
                    saturation = it
                    hex = colorHex(android.graphics.Color.HSVToColor(floatArrayOf(hue, it, brightness)))
                }
            )
            GradientSlider(
                label = stringResource(R.string.color_brightness),
                value = brightness,
                range = 0f..1f,
                brush = Brush.horizontalGradient(listOf(Color.Black, Color.hsv(hue, saturation, 1f))),
                onValueChange = {
                    brightness = it
                    hex = colorHex(android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, it)))
                }
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                QuickColors.forEach { quick ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(quick))
                            .border(BorderStroke(1.dp, colors.divider), CircleShape)
                            .clickable {
                                setFrom(quick)
                                hex = colorHex(quick)
                            }
                    )
                }
            }
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 6.dp, bottom = 10.dp)) {
                KateTextButton(label = resetLabel, role = DialogButtonRole.SECONDARY, onClick = onReset)
                KateTextButton(label = stringResource(R.string.action_cancel), role = DialogButtonRole.SECONDARY, onClick = onDismiss)
                KateTextButton(label = stringResource(R.string.color_apply), role = DialogButtonRole.MAIN, onClick = { onPick(argb) })
            }
        }
    }
}

@Composable
private fun GradientSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    brush: Brush,
    onValueChange: (Float) -> Unit
) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = colors.secondaryText)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(40.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = colors.onSurface,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
