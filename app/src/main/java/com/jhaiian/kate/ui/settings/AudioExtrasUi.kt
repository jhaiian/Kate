package com.jhaiian.kate.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AudioEffects
import com.jhaiian.kate.data.EqAutoPresets
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.ui.theme.LocalClintColors
import java.util.Locale
import kotlin.math.roundToInt

internal fun hzLabel(value: Float): String =
    if (value >= 1_000f) String.format(Locale.US, "%.1f kHz", value / 1_000f) else "${value.roundToInt()} Hz"

@Composable
internal fun HeadphoneEffectsCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { AudioEffects.ensureLoaded(context) }
    AudioCard {
        Column16 {
            CardHeader(
                icon = Icons.Outlined.Headphones,
                title = stringResource(R.string.crossfeed_title),
                subtitle = stringResource(R.string.crossfeed_subtitle)
            )
            ChoiceRow(
                options = listOf(
                    "0" to stringResource(R.string.effect_off),
                    "1" to stringResource(R.string.effect_low),
                    "2" to stringResource(R.string.effect_medium),
                    "3" to stringResource(R.string.effect_high)
                ),
                selected = AudioEffects.crossfeed.toString(),
                onSelect = { AudioEffects.setCrossfeed(context, it.toInt()) },
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        RowDivider(colors.divider.copy(alpha = 0.6f))
        Column16 {
            CardHeader(
                icon = Icons.Outlined.Compress,
                title = stringResource(R.string.compressor_title),
                subtitle = stringResource(R.string.compressor_subtitle)
            )
            ChoiceRow(
                options = listOf(
                    "0" to stringResource(R.string.effect_off),
                    "1" to stringResource(R.string.compressor_light),
                    "2" to stringResource(R.string.effect_medium),
                    "3" to stringResource(R.string.compressor_night)
                ),
                selected = AudioEffects.compressor.toString(),
                onSelect = { AudioEffects.setCompressor(context, it.toInt()) },
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        RowDivider(colors.divider.copy(alpha = 0.6f))
        FineTuneSlider(
            title = stringResource(R.string.loudness_title),
            value = AudioEffects.loudness / 100f,
            range = 0f..1f,
            format = { if (it < 0.01f) context.getString(R.string.effect_off) else percentLabel(it) },
            onCommit = { AudioEffects.setLoudness(context, (it * 100).roundToInt()) }
        )
        Text(
            text = stringResource(R.string.loudness_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        )
    }
}

@Composable
internal fun ToneFrequencyCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    AudioCard {
        FineTuneSlider(
            title = stringResource(R.string.tone_bass_frequency),
            value = AudioEffects.bassHz.toFloat(),
            range = AudioEffects.MIN_BASS_HZ.toFloat()..AudioEffects.MAX_BASS_HZ.toFloat(),
            format = ::hzLabel,
            onCommit = { AudioEffects.setToneFrequencies(context, it.roundToInt(), AudioEffects.trebleHz) }
        )
        FineTuneSlider(
            title = stringResource(R.string.tone_treble_frequency),
            value = AudioEffects.trebleHz.toFloat(),
            range = AudioEffects.MIN_TREBLE_HZ.toFloat()..AudioEffects.MAX_TREBLE_HZ.toFloat(),
            format = ::hzLabel,
            onCommit = { AudioEffects.setToneFrequencies(context, AudioEffects.bassHz, (it / 100f).roundToInt() * 100) }
        )
        Text(
            text = stringResource(R.string.tone_frequency_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        )
    }
}

@Composable
private fun Column16(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { content() }
}

@Composable
internal fun EqExtras(onParametricClick: (() -> Unit)?) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AudioEffects.ensureLoaded(context)
        EqAutoPresets.ensureLoaded(context)
    }
    val linkedMessage = stringResource(R.string.eq_auto_linked)
    if (onParametricClick != null) {
    Spacer(modifier = Modifier.height(20.dp))
    SettingsCard {
        val activeFilters = AudioEffects.filters.count { it.enabled }
        SettingsRow(
            icon = Icons.Outlined.Tune,
            title = stringResource(R.string.parametric_title),
            subtitle = if (AudioEffects.parametricEnabled) {
                pluralStringResource(R.plurals.parametric_summary_on, activeFilters, activeFilters)
            } else {
                stringResource(R.string.parametric_summary_off)
            },
            colors = colors,
            onClick = onParametricClick
        )
    }
    }
    Spacer(modifier = Modifier.height(20.dp))
    SectionHeader(stringResource(R.string.eq_auto_title), colors.primary)
    SettingsCard {
        val track = PlayerController.currentTrack
        if (track != null) {
            val targets = listOf(
                Triple(EqAutoPresets.SCOPE_TRACK, Icons.Outlined.MusicNote, R.string.eq_auto_link_song),
                Triple(EqAutoPresets.SCOPE_ALBUM, Icons.Outlined.Album, R.string.eq_auto_link_album),
                Triple(EqAutoPresets.SCOPE_FOLDER, Icons.Outlined.Folder, R.string.eq_auto_link_folder),
                Triple(EqAutoPresets.SCOPE_GENRE, Icons.Outlined.Category, R.string.eq_auto_link_genre)
            ).filter { EqAutoPresets.keyFor(it.first, track) != null }
            targets.forEachIndexed { index, (scope, icon, label) ->
                if (index > 0) RowDivider(colors.divider)
                SettingsRow(
                    icon = icon,
                    title = stringResource(label),
                    subtitle = EqAutoPresets.labelFor(scope, track),
                    colors = colors,
                    onClick = {
                        if (EqAutoPresets.link(context, scope, track)) {
                            Toast.makeText(context, linkedMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        } else {
            Text(
                text = stringResource(R.string.eq_auto_no_song),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(16.dp)
            )
        }
        val scopeLabels = mapOf(
            EqAutoPresets.SCOPE_TRACK to stringResource(R.string.eq_auto_scope_song),
            EqAutoPresets.SCOPE_ALBUM to stringResource(R.string.eq_auto_scope_album),
            EqAutoPresets.SCOPE_FOLDER to stringResource(R.string.eq_auto_scope_folder),
            EqAutoPresets.SCOPE_GENRE to stringResource(R.string.eq_auto_scope_genre)
        )
        EqAutoPresets.rules.forEach { rule ->
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.LinkOff,
                title = rule.label.ifBlank { scopeLabels[rule.scope].orEmpty() },
                subtitle = stringResource(R.string.eq_auto_rule_subtitle, scopeLabels[rule.scope].orEmpty()),
                colors = colors,
                onClick = { EqAutoPresets.remove(context, rule) }
            )
        }
        Text(
            text = stringResource(R.string.eq_auto_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)
        )
    }
}
