package com.jhaiian.kate.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.components.MatchAppSystemBars
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.components.rememberSheetFlingConnection
import com.jhaiian.kate.ui.settings.FineTuneSlider
import com.jhaiian.kate.ui.settings.percentLabel
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LocalLookAndFeel
import com.jhaiian.kate.ui.theme.LocalLookAndFeelUpdate
import com.jhaiian.kate.ui.theme.VisualizerPreferences
import kotlin.math.roundToInt

@Composable
private fun ChipRow(options: List<Pair<String, String>>, isSelected: (String) -> Boolean, onClick: (String) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        options.forEach { (key, label) ->
            val selected = isSelected(key)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = kateChoiceText(selected),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier
                    .kateChoice(selected, chip = true)
                    .clickable { onClick(key) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = LocalClintColors.current.secondaryText,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VisualizerQuickSheet(onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val look = LocalLookAndFeel.current
    val update = LocalLookAndFeelUpdate.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val styles = listOf(
        VisualizerPreferences.STYLE_OFF to stringResource(R.string.visualizer_style_off),
        VisualizerPreferences.STYLE_BARS to stringResource(R.string.visualizer_style_bars),
        VisualizerPreferences.STYLE_MIRROR to stringResource(R.string.visualizer_style_mirror),
        VisualizerPreferences.STYLE_LINE to stringResource(R.string.visualizer_style_line),
        VisualizerPreferences.STYLE_WAVE to stringResource(R.string.visualizer_style_wave),
        VisualizerPreferences.STYLE_RING to stringResource(R.string.visualizer_style_ring),
        VisualizerPreferences.STYLE_PULSE to stringResource(R.string.visualizer_style_pulse)
    )
    val focusModes = listOf(
        VisualizerFocus.OFF to stringResource(R.string.visualizer_focus_off),
        VisualizerFocus.FADE to stringResource(R.string.visualizer_focus_fade),
        VisualizerFocus.FULL to stringResource(R.string.visualizer_focus_full)
    )
    val colorSources = listOf(
        VisualizerPreferences.COLOR_ACCENT to stringResource(R.string.seek_color_accent),
        VisualizerPreferences.COLOR_GRADIENT to stringResource(R.string.seek_color_gradient)
    ) + if (look.visualizerColor == VisualizerPreferences.COLOR_CUSTOM) {
        listOf(VisualizerPreferences.COLOR_CUSTOM to stringResource(R.string.accent_custom))
    } else {
        emptyList()
    }
    val places = listOf(
        VisualizerPreferences.PLACE_BACKGROUND to stringResource(R.string.visualizer_place_background),
        VisualizerPreferences.PLACE_STRIP to stringResource(R.string.visualizer_place_strip),
        VisualizerPreferences.PLACE_MINI to stringResource(R.string.visualizer_place_mini),
        VisualizerPreferences.PLACE_LYRICS to stringResource(R.string.visualizer_place_lyrics)
    )
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = colors.cardBackground) {
        MatchAppSystemBars()
        Column(modifier = Modifier.nestedScroll(rememberSheetFlingConnection(sheetState, consumeFling = false)).verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(R.string.visualizer_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceVariant)
            ) {
                if (look.visualizerStyle == VisualizerPreferences.STYLE_OFF) {
                    Text(
                        text = stringResource(R.string.visualizer_preview_off),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText
                    )
                } else {
                    AudioVisualizer(modifier = Modifier.fillMaxSize().padding(12.dp))
                }
            }
            GroupLabel(stringResource(R.string.visualizer_style_title))
            ChipRow(styles, { it == look.visualizerStyle }) { update { l -> l.copy(visualizerStyle = it) } }
            GroupLabel(stringResource(R.string.visualizer_focus_title))
            ChipRow(focusModes, { it == VisualizerFocus.mode }) { VisualizerFocus.setMode(context, it) }
            GroupLabel(stringResource(R.string.visualizer_section_where))
            ChipRow(places, { it in look.visualizerPlacements }) { key ->
                update { l -> l.copy(visualizerPlacements = if (key in l.visualizerPlacements) l.visualizerPlacements - key else l.visualizerPlacements + key) }
            }
            GroupLabel(stringResource(R.string.visualizer_color_title))
            ChipRow(colorSources, { it == look.visualizerColor }) { update { l -> l.copy(visualizerColor = it) } }
            FineTuneSlider(
                title = stringResource(R.string.visualizer_bars),
                value = look.visualizerBars.toFloat(),
                range = VisualizerPreferences.MIN_BARS.toFloat()..VisualizerPreferences.MAX_BARS.toFloat(),
                format = { "${it.roundToInt()}" },
                onCommit = { v -> update { l -> l.copy(visualizerBars = v.roundToInt()) } }
            )
            FineTuneSlider(
                title = stringResource(R.string.visualizer_sensitivity),
                value = look.visualizerSensitivity / 100f,
                range = VisualizerPreferences.MIN_SENSITIVITY / 100f..VisualizerPreferences.MAX_SENSITIVITY / 100f,
                format = ::percentLabel,
                onCommit = { v -> update { l -> l.copy(visualizerSensitivity = (v * 100).roundToInt()) } }
            )
            FineTuneSlider(
                title = stringResource(R.string.visualizer_smoothing),
                value = look.visualizerSmoothing / 100f,
                range = 0f..VisualizerPreferences.MAX_SMOOTHING / 100f,
                format = ::percentLabel,
                onCommit = { v -> update { l -> l.copy(visualizerSmoothing = (v * 100).roundToInt()) } }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
