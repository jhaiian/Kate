package com.jhaiian.kate.ui.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BluetoothAudio
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.BuiltInEqPresets
import com.jhaiian.kate.data.EqualizerSettings
import com.jhaiian.kate.data.OutputRoute
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateChip
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateIconButton
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.settings.SettingsSwitchRow
import com.jhaiian.kate.ui.settings.equalizerStatus
import com.jhaiian.kate.ui.settings.formatSpeed
import com.jhaiian.kate.ui.settings.routeLabel
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlin.math.abs
import kotlin.math.roundToInt

private fun routeIcon(route: String): ImageVector = when (route) {
    OutputRoute.WIRED -> Icons.Outlined.Headphones
    OutputRoute.BLUETOOTH -> Icons.Outlined.BluetoothAudio
    OutputRoute.USB -> Icons.Outlined.Usb
    else -> Icons.Outlined.Smartphone
}

private fun speedChanged(): Boolean = abs(AudioSettings.playbackSpeed - 1f) > 0.001f

internal fun audioStatusVisible(): Boolean =
    OutputRoute.current != OutputRoute.SPEAKER ||
        AudioSettings.hiResOutput ||
        AudioSettings.bitPerfectEngaged ||
        speedChanged()

@Composable
internal fun audioMenuSummary(): String {
    val parts = mutableListOf(stringResource(R.string.speed_value, formatSpeed(AudioSettings.playbackSpeed)))
    parts.add(routeLabel(OutputRoute.current))
    if (AudioSettings.bitPerfectEngaged) parts.add(stringResource(R.string.badge_bitperfect))
    if (AudioSettings.hiResOutput) parts.add(stringResource(R.string.badge_hires))
    return parts.joinToString(" · ")
}

@Composable
private fun AudioBadge(text: String) {
    val colors = LocalClintColors.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = colors.primary,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.primary.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
internal fun AudioStatusChip(onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (!audioStatusVisible()) return
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val route = OutputRoute.current
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(50))
                .background(colors.secondaryText.copy(alpha = 0.12f))
                .clickable {
                    haptics.click()
                    onClick()
                }
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            if (route != OutputRoute.SPEAKER) {
                KateIcon(
                    imageVector = routeIcon(route),
                    contentDescription = null,
                    tint = colors.secondaryText,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = routeLabel(route),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (speedChanged()) {
                Text(
                    text = stringResource(R.string.speed_value, formatSpeed(AudioSettings.playbackSpeed)),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    maxLines = 1
                )
            }
            if (AudioSettings.bitPerfectEngaged) AudioBadge(stringResource(R.string.badge_bitperfect))
            if (AudioSettings.hiResOutput) AudioBadge(stringResource(R.string.badge_hires))
        }
    }
}

@Composable
internal fun AudioQuickDialog(visible: Boolean, onDismiss: () -> Unit, onOpenEqualizer: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    LaunchedEffect(visible) {
        if (visible) {
            AudioSettings.ensureLoaded(context)
            EqualizerSettings.ensureLoaded(context)
        }
    }
    val route = OutputRoute.current
    val speed = AudioSettings.playbackSpeed
    val min = AudioSettings.MIN_SPEED
    val max = AudioSettings.MAX_SPEED
    val step = AudioSettings.SPEED_STEP
    val presetKey = EqualizerSettings.presetKey
    val presets = BuiltInEqPresets.map { it.key to stringResource(it.nameRes) } +
        EqualizerSettings.customPresets.map { it.id to it.name }
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 440.dp)
                .heightIn(max = screenHeight * 0.85f)
                .clip(RoundedCornerShape(28.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(28.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 10.dp, top = 16.dp, bottom = 14.dp)
            ) {
                Box(modifier = Modifier.kateIconTile(34.dp), contentAlignment = Alignment.Center) {
                    KateIcon(imageVector = Icons.Outlined.GraphicEq, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(18.dp))
                }
                Text(
                    text = stringResource(R.string.audio_quick_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
                KateIconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    KateIcon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.folder_picker_close),
                        tint = buttonTint(colors.secondaryText),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Box(modifier = Modifier.kateIconTile(40.dp), contentAlignment = Alignment.Center) {
                        KateIcon(imageVector = routeIcon(route), contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(
                            text = stringResource(R.string.audio_quick_output, routeLabel(route)),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        if (AudioSettings.bitPerfectEngaged || AudioSettings.hiResOutput) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                if (AudioSettings.bitPerfectEngaged) AudioBadge(stringResource(R.string.badge_bitperfect))
                                if (AudioSettings.hiResOutput) AudioBadge(stringResource(R.string.badge_hires))
                            }
                        }
                    }
                }
                RowDivider(color = colors.divider.copy(alpha = 0.6f))
                Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.kateIconTile(40.dp), contentAlignment = Alignment.Center) {
                            KateIcon(imageVector = Icons.Outlined.Speed, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = stringResource(R.string.speed_title),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurface,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.speed_value, formatSpeed(speed)),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                    Slider(
                        value = speed,
                        onValueChange = { raw ->
                            val snapped = (min + ((raw - min) / step).roundToInt() * step).coerceIn(min, max)
                            if (abs(snapped - speed) > 0.001f) {
                                haptics.tick()
                                AudioSettings.setPlaybackSpeed(context, snapped)
                            }
                        },
                        valueRange = min..max,
                        steps = ((max - min) / step).roundToInt() - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.primary.copy(alpha = 0.2f),
                            activeTickColor = colors.onPrimary.copy(alpha = 0.6f),
                            inactiveTickColor = colors.primary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    if (speedChanged()) {
                        KateDialogActions(modifier = Modifier.fillMaxWidth()) {
                            KateTextButton(
                                label = stringResource(R.string.look_reset_confirm),
                                role = DialogButtonRole.SECONDARY,
                                onClick = {
                                    haptics.click()
                                    AudioSettings.setPlaybackSpeed(context, 1f)
                                }
                            )
                        }
                    }
                }
                SettingsSwitchRow(
                    icon = Icons.Outlined.MusicNote,
                    title = stringResource(R.string.keep_pitch_title),
                    subtitle = stringResource(R.string.keep_pitch_subtitle),
                    checked = AudioSettings.keepPitch,
                    colors = colors,
                    onCheckedChange = { AudioSettings.setKeepPitch(context, it) },
                    subtitleLines = Int.MAX_VALUE
                )
                RowDivider(color = colors.divider.copy(alpha = 0.6f))
                SettingsSwitchRow(
                    icon = Icons.Outlined.GraphicEq,
                    title = stringResource(R.string.eq_title),
                    subtitle = equalizerStatus(),
                    checked = EqualizerSettings.enabled,
                    colors = colors,
                    onCheckedChange = { EqualizerSettings.setEnabled(context, it) }
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    presets.forEach { (key, label) ->
                        KateChip(
                            label = label,
                            selected = EqualizerSettings.enabled && key == presetKey,
                            onClick = {
                                haptics.click()
                                EqualizerSettings.applyPreset(context, key)
                            }
                        )
                    }
                }
            }
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.audio_quick_open_eq),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onOpenEqualizer
                )
                KateTextButton(
                    label = stringResource(R.string.audio_done),
                    role = DialogButtonRole.MAIN,
                    onClick = onDismiss
                )
            }
        }
    }
}
