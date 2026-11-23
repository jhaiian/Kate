package com.jhaiian.kate.ui.settings

import com.jhaiian.kate.ui.player.VisualizerFocus
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.ViewDay
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.player.AudioVisualizer
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.VisualizerPreferences
import kotlin.math.roundToInt

private enum class VisualizerSheet { STYLE, COLOR, FPS, FOCUS }

@Composable
fun VisualizerScreen(
    settings: LookAndFeelSettings,
    onSettingsChange: (LookAndFeelSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    var sheet by remember { mutableStateOf<VisualizerSheet?>(null) }
    var picking by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    val styleLabels = mapOf(
        VisualizerPreferences.STYLE_OFF to stringResource(R.string.visualizer_style_off),
        VisualizerPreferences.STYLE_BARS to stringResource(R.string.visualizer_style_bars),
        VisualizerPreferences.STYLE_MIRROR to stringResource(R.string.visualizer_style_mirror),
        VisualizerPreferences.STYLE_LINE to stringResource(R.string.visualizer_style_line),
        VisualizerPreferences.STYLE_WAVE to stringResource(R.string.visualizer_style_wave),
        VisualizerPreferences.STYLE_RING to stringResource(R.string.visualizer_style_ring),
        VisualizerPreferences.STYLE_PULSE to stringResource(R.string.visualizer_style_pulse)
    )
    val colorLabels = mapOf(
        VisualizerPreferences.COLOR_ACCENT to stringResource(R.string.seek_color_accent),
        VisualizerPreferences.COLOR_GRADIENT to stringResource(R.string.seek_color_gradient),
        VisualizerPreferences.COLOR_CUSTOM to stringResource(R.string.accent_custom)
    )
    val focusLabels = mapOf(
        VisualizerFocus.OFF to stringResource(R.string.visualizer_focus_off),
        VisualizerFocus.FADE to stringResource(R.string.visualizer_focus_fade),
        VisualizerFocus.FULL to stringResource(R.string.visualizer_focus_full)
    )
    val context = LocalContext.current
    val fpsLabels = mapOf(
        30 to stringResource(R.string.visualizer_fps_value, 30),
        60 to stringResource(R.string.visualizer_fps_value, 60),
        0 to stringResource(R.string.visualizer_fps_max)
    )
    LookAndFeelPage(
        title = stringResource(R.string.visualizer_title),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.visualizer_reset_message),
                onConfirm = {
                    confirmReset = false
                    val defaults = LookAndFeelSettings()
                    onSettingsChange(
                        settings.copy(
                            visualizerStyle = defaults.visualizerStyle,
                            visualizerPlacements = defaults.visualizerPlacements,
                            visualizerBars = defaults.visualizerBars,
                            visualizerSensitivity = defaults.visualizerSensitivity,
                            visualizerSmoothing = defaults.visualizerSmoothing,
                            visualizerColor = defaults.visualizerColor,
                            visualizerCustomColor = defaults.visualizerCustomColor,
                            visualizerFps = defaults.visualizerFps
                        )
                    )
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
        ) {
            if (settings.visualizerStyle == VisualizerPreferences.STYLE_OFF) {
                Text(
                    text = stringResource(R.string.visualizer_preview_off),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText
                )
            } else {
                AudioVisualizer(modifier = Modifier.fillMaxSize().padding(16.dp))
            }
        }
        Text(
            text = stringResource(R.string.visualizer_preview_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.visualizer_section_look), colors.primary)
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Equalizer,
                title = stringResource(R.string.visualizer_style_title),
                subtitle = styleLabels[settings.visualizerStyle].orEmpty(),
                colors = colors,
                onClick = { sheet = VisualizerSheet.STYLE }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Palette,
                title = stringResource(R.string.visualizer_color_title),
                subtitle = colorLabels[settings.visualizerColor].orEmpty(),
                colors = colors,
                onClick = { sheet = VisualizerSheet.COLOR }
            )
            if (settings.visualizerColor == VisualizerPreferences.COLOR_CUSTOM) {
                RowDivider(colors.divider)
                ColorRow(
                    title = stringResource(R.string.custom_color_title),
                    subtitle = colorHex(settings.visualizerCustomColor),
                    swatch = Color(settings.visualizerCustomColor),
                    onClick = { picking = true }
                )
            }
            RowDivider(colors.divider)
            FineTuneSlider(
                title = stringResource(R.string.visualizer_bars),
                value = settings.visualizerBars.toFloat(),
                range = VisualizerPreferences.MIN_BARS.toFloat()..VisualizerPreferences.MAX_BARS.toFloat(),
                format = { "${it.roundToInt()}" },
                onCommit = { onSettingsChange(settings.copy(visualizerBars = it.roundToInt())) }
            )
            FineTuneSlider(
                title = stringResource(R.string.visualizer_sensitivity),
                value = settings.visualizerSensitivity / 100f,
                range = VisualizerPreferences.MIN_SENSITIVITY / 100f..VisualizerPreferences.MAX_SENSITIVITY / 100f,
                format = ::percentLabel,
                onCommit = { onSettingsChange(settings.copy(visualizerSensitivity = (it * 100).roundToInt())) }
            )
            FineTuneSlider(
                title = stringResource(R.string.visualizer_smoothing),
                value = settings.visualizerSmoothing / 100f,
                range = 0f..VisualizerPreferences.MAX_SMOOTHING / 100f,
                format = ::percentLabel,
                onCommit = { onSettingsChange(settings.copy(visualizerSmoothing = (it * 100).roundToInt())) }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Speed,
                title = stringResource(R.string.visualizer_fps_title),
                subtitle = fpsLabels[settings.visualizerFps].orEmpty(),
                colors = colors,
                onClick = { sheet = VisualizerSheet.FPS }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.visualizer_section_where), colors.primary)
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.Fullscreen,
                title = stringResource(R.string.visualizer_focus_title),
                subtitle = focusLabels[VisualizerFocus.mode].orEmpty(),
                colors = colors,
                onClick = { sheet = VisualizerSheet.FOCUS }
            )
            Text(
                text = stringResource(R.string.visualizer_focus_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
            )
            RowDivider(colors.divider)
            val places = listOf(
                Triple(VisualizerPreferences.PLACE_BACKGROUND, Icons.Outlined.Wallpaper, R.string.visualizer_place_background),
                Triple(VisualizerPreferences.PLACE_STRIP, Icons.Outlined.ViewDay, R.string.visualizer_place_strip),
                Triple(VisualizerPreferences.PLACE_MINI, Icons.Outlined.MusicNote, R.string.visualizer_place_mini),
                Triple(VisualizerPreferences.PLACE_LYRICS, Icons.Outlined.Lyrics, R.string.visualizer_place_lyrics)
            )
            places.forEachIndexed { index, (key, icon, label) ->
                if (index > 0) RowDivider(colors.divider)
                SettingsSwitchRow(
                    icon = icon,
                    title = stringResource(label),
                    subtitle = stringResource(R.string.visualizer_place_subtitle),
                    checked = key in settings.visualizerPlacements,
                    colors = colors,
                    onCheckedChange = { on ->
                        onSettingsChange(
                            settings.copy(visualizerPlacements = if (on) settings.visualizerPlacements + key else settings.visualizerPlacements - key)
                        )
                    }
                )
            }
            Text(
                text = stringResource(R.string.visualizer_battery_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
            )
        }
    }

    when (sheet) {
        VisualizerSheet.STYLE -> TextOptionSheet(
            title = stringResource(R.string.visualizer_style_title),
            options = VisualizerPreferences.Styles.map { it to styleLabels[it].orEmpty() },
            current = settings.visualizerStyle,
            onSelect = { onSettingsChange(settings.copy(visualizerStyle = it)); sheet = null },
            onDismiss = { sheet = null }
        )
        VisualizerSheet.COLOR -> TextOptionSheet(
            title = stringResource(R.string.visualizer_color_title),
            options = VisualizerPreferences.ColorSources.map { it to colorLabels[it].orEmpty() },
            current = settings.visualizerColor,
            onSelect = { onSettingsChange(settings.copy(visualizerColor = it)); sheet = null },
            onDismiss = { sheet = null }
        )
        VisualizerSheet.FPS -> TextOptionSheet(
            title = stringResource(R.string.visualizer_fps_title),
            options = VisualizerPreferences.FrameRates.map { it.toString() to fpsLabels[it].orEmpty() },
            current = settings.visualizerFps.toString(),
            onSelect = { onSettingsChange(settings.copy(visualizerFps = it.toInt())); sheet = null },
            onDismiss = { sheet = null }
        )
        VisualizerSheet.FOCUS -> TextOptionSheet(
            title = stringResource(R.string.visualizer_focus_title),
            options = VisualizerFocus.Modes.map { it to focusLabels[it].orEmpty() },
            current = VisualizerFocus.mode,
            onSelect = { VisualizerFocus.setMode(context, it); sheet = null },
            onDismiss = { sheet = null }
        )
        null -> {}
    }
    ColorPickerDialog(
        visible = picking,
        title = stringResource(R.string.custom_color_title),
        initial = settings.visualizerCustomColor,
        resetLabel = stringResource(R.string.color_reset_default),
        onPick = {
            onSettingsChange(settings.copy(visualizerCustomColor = it))
            picking = false
        },
        onReset = {
            onSettingsChange(settings.copy(visualizerCustomColor = LookAndFeelSettings().visualizerCustomColor))
            picking = false
        },
        onDismiss = { picking = false }
    )
}
