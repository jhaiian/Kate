package com.jhaiian.kate.ui.settings

import android.media.AudioManager
import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateRowArrow
import com.jhaiian.kate.ui.components.kateTileTint
import com.jhaiian.kate.ui.components.kateIconTile
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.BluetoothAudio
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.HeadsetOff
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.SurroundSound
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.AudioEffects
import com.jhaiian.kate.data.EqualizerSettings
import com.jhaiian.kate.data.OutputRoute
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.theme.ClintSwitch
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

object AudioPages {
    const val SOUND = "settings/audio/sound"
    const val VOLUME = "settings/audio/volume"
    const val PLAYBACK = "settings/audio/playback"
    const val HEADPHONES = "settings/audio/headphones"
    const val ADVANCED = "settings/audio/advanced"
}

@Composable
private fun AudioPage(
    title: String,
    resetMessage: String,
    onReset: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AudioSettings.ensureLoaded(context)
        EqualizerSettings.ensureLoaded(context)
    }
    var confirmReset by remember { mutableStateOf(false) }
    LookAndFeelPage(
        title = title,
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            overlay()
            ResetDefaultsDialog(
                visible = confirmReset,
                message = resetMessage,
                onConfirm = {
                    confirmReset = false
                    onReset()
                },
                onDismiss = { confirmReset = false }
            )
        },
        content = content
    )
}

@Composable
private fun activeSummary(defaultRes: Int, active: List<Int>): String {
    if (active.isEmpty()) return stringResource(defaultRes)
    val names = active.map { stringResource(it) }
    return stringResource(R.string.audio_summary_on, names.joinToString(", "))
}

@Composable
fun AudioSettingsScreen(
    onBack: () -> Unit,
    onEqualizerClick: () -> Unit,
    onPageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val sound = buildList {
        if (AudioSettings.stereoWidth != AudioSettings.NORMAL_STEREO_WIDTH) add(R.string.stereo_width_title)
        if (AudioSettings.monoAudio) add(R.string.mono_title)
        if (AudioSettings.balance != 0f) add(R.string.balance_title)
        if (EqualizerSettings.perDevice) add(R.string.per_device_eq_title)
    }
    val volume = buildList {
        if (AudioSettings.replayGainMode != AudioSettings.REPLAY_GAIN_OFF) add(R.string.rg_title)
        if (AudioSettings.volumeLimit < AudioSettings.MAX_VOLUME_LIMIT) add(R.string.volume_limit_title)
        if (AudioSettings.volumeSubSteps > 1) add(R.string.volume_steps_title)
    }
    val playback = buildList {
        if (AudioSettings.crossfadeSeconds > 0) add(R.string.row_crossfade_title)
        if (abs(AudioSettings.playbackSpeed - 1f) > 0.001f) add(R.string.speed_title)
        if (AudioSettings.skipSilence) add(R.string.skip_silence_title)
        if (AudioSettings.playPauseFadeMs > 0) add(R.string.play_pause_fade_title)
        if (AudioSettings.rewindSeconds > 0) add(R.string.rewind_title)
        if (AudioSettings.longTrackMinutes > 0) add(R.string.long_track_title)
    }
    val headphones = buildList {
        if (AudioSettings.pauseWhenMuted) add(R.string.mute_pause_title)
        if (AudioSettings.autoPlayWired) add(R.string.autoplay_wired_title)
        if (AudioSettings.autoPlayBluetooth) add(R.string.autoplay_bluetooth_title)
        if (AudioSettings.startVolume > 0) add(R.string.start_volume_title)
    }
    val advanced = buildList {
        if (AudioSettings.audioOffload) add(R.string.offload_title)
        if (AudioSettings.hiResOutput) add(R.string.hires_title)
        if (AudioSettings.resampleToDevice) add(R.string.resample_title)
        if (AudioSettings.bitPerfect) add(R.string.bitperfect_title)
        if (AudioSettings.nativeOutput) add(R.string.native_title)
    }
    AudioPage(
        title = stringResource(R.string.section_audio),
        resetMessage = stringResource(R.string.audio_reset_all_message),
        onReset = { AudioSettings.resetAll(context) },
        onBack = onBack,
        modifier = modifier
    ) {
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.GraphicEq,
                title = stringResource(R.string.eq_title),
                subtitle = equalizerStatus(),
                colors = colors,
                onClick = onEqualizerClick
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Speaker,
                title = stringResource(R.string.section_sound),
                subtitle = activeSummary(R.string.audio_sound_summary, sound),
                colors = colors,
                onClick = { onPageClick(AudioPages.SOUND) }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                title = stringResource(R.string.audio_page_volume),
                subtitle = activeSummary(R.string.audio_volume_summary, volume),
                colors = colors,
                onClick = { onPageClick(AudioPages.VOLUME) }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.PlayCircle,
                title = stringResource(R.string.section_playback),
                subtitle = activeSummary(R.string.audio_playback_summary, playback),
                colors = colors,
                onClick = { onPageClick(AudioPages.PLAYBACK) }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Headphones,
                title = stringResource(R.string.audio_page_headphones),
                subtitle = activeSummary(R.string.audio_headphones_summary, headphones),
                colors = colors,
                onClick = { onPageClick(AudioPages.HEADPHONES) }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.HighQuality,
                title = stringResource(R.string.section_advanced_audio),
                subtitle = activeSummary(R.string.audio_advanced_summary, advanced),
                colors = colors,
                onClick = { onPageClick(AudioPages.ADVANCED) }
            )
        }
    }
}

@Composable
fun AudioSoundScreen(onBack: () -> Unit, onEqualizerClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AudioPage(
        title = stringResource(R.string.section_sound),
        resetMessage = stringResource(R.string.audio_reset_sound_message),
        onReset = {
            AudioSettings.resetSound(context)
            AudioEffects.resetEffects(context)
        },
        onBack = onBack,
        modifier = modifier
    ) {
        EqualizerRow(onClick = onEqualizerClick)
        Spacer(modifier = Modifier.height(12.dp))
        PerDeviceEqCard()
        Spacer(modifier = Modifier.height(12.dp))
        StereoWidthCard()
        Spacer(modifier = Modifier.height(12.dp))
        ChannelCard()
        Spacer(modifier = Modifier.height(12.dp))
        HeadphoneEffectsCard()
        Spacer(modifier = Modifier.height(12.dp))
        ToneFrequencyCard()
    }
}

@Composable
fun AudioVolumeScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AudioPage(
        title = stringResource(R.string.audio_page_volume),
        resetMessage = stringResource(R.string.audio_reset_volume_message),
        onReset = { AudioSettings.resetVolume(context) },
        onBack = onBack,
        modifier = modifier
    ) {
        ReplayGainCard()
        Spacer(modifier = Modifier.height(12.dp))
        LimiterCard()
        Spacer(modifier = Modifier.height(12.dp))
        VolumeStepsCard()
    }
}

@Composable
fun AudioPlaybackScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AudioPage(
        title = stringResource(R.string.section_playback),
        resetMessage = stringResource(R.string.audio_reset_playback_message),
        onReset = { AudioSettings.resetPlayback(context) },
        onBack = onBack,
        modifier = modifier
    ) {
        SpeedCard()
        Spacer(modifier = Modifier.height(12.dp))
        CrossfadeCard()
        Spacer(modifier = Modifier.height(12.dp))
        GaplessCard()
        Spacer(modifier = Modifier.height(12.dp))
        PlaybackExtrasCard()
        Spacer(modifier = Modifier.height(12.dp))
        LongTrackCard()
        Spacer(modifier = Modifier.height(12.dp))
        SleepFadeCard()
    }
}

@Composable
private fun SleepFadeCard() {
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val seconds = AudioSettings.sleepFadeSeconds
    val step = AudioSettings.SLEEP_FADE_STEP_SECONDS
    val off = stringResource(R.string.crossfade_off)
    AudioCard {
        IconSlider(
            icon = Icons.Outlined.Bedtime,
            title = stringResource(R.string.sleep_fade_title),
            subtitle = stringResource(R.string.sleep_fade_subtitle),
            valueText = if (seconds == 0) off else pluralStringResource(R.plurals.crossfade_seconds, seconds, seconds),
            value = seconds.toFloat(),
            range = 0f..AudioSettings.MAX_SLEEP_FADE_SECONDS.toFloat(),
            steps = AudioSettings.MAX_SLEEP_FADE_SECONDS / step - 1,
            startLabel = off,
            endLabel = pluralStringResource(R.plurals.crossfade_seconds, AudioSettings.MAX_SLEEP_FADE_SECONDS, AudioSettings.MAX_SLEEP_FADE_SECONDS),
            onValueChange = { raw ->
                val snapped = (raw / step).roundToInt() * step
                if (snapped != seconds) {
                    haptics.tick()
                    AudioSettings.setSleepFadeSeconds(context, snapped)
                }
            }
        )
    }
}

@Composable
fun AudioHeadphonesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    var showBluetoothDevices by remember { mutableStateOf(false) }
    AudioPage(
        title = stringResource(R.string.audio_page_headphones),
        resetMessage = stringResource(R.string.audio_reset_headphones_message),
        onReset = { AudioSettings.resetHeadphones(context) },
        onBack = onBack,
        modifier = modifier,
        overlay = {
            BluetoothDevicesDialog(visible = showBluetoothDevices, onDismiss = { showBluetoothDevices = false })
        }
    ) {
        SectionHeader(stringResource(R.string.section_interruptions), colors.primary)
        FocusCard()
        Spacer(modifier = Modifier.height(12.dp))
        MuteCard()
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.section_headphones), colors.primary)
        HeadphonesCard(onChooseDevices = { showBluetoothDevices = true })
    }
}

@Composable
fun AudioAdvancedScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AudioPage(
        title = stringResource(R.string.section_advanced_audio),
        resetMessage = stringResource(R.string.audio_reset_advanced_message),
        onReset = { AudioSettings.resetAdvanced(context) },
        onBack = onBack,
        modifier = modifier
    ) {
        OffloadCard()
        Spacer(modifier = Modifier.height(12.dp))
        HiFiCard()
    }
}

@Composable
private fun EqualizerRow(onClick: () -> Unit) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
            .clickable {
                haptics.click()
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = Icons.Outlined.GraphicEq, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
            Text(
                text = stringResource(R.string.eq_title),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = equalizerStatus(),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        KateRowArrow()
    }
}

private fun formatDb(value: Float): String {
    if (abs(value) < 0.05f) return "0"
    val whole = value.roundToInt().toFloat() == value
    return if (whole) String.format(Locale.getDefault(), "%+d", value.roundToInt())
    else String.format(Locale.getDefault(), "%+.1f", value)
}

@Composable
internal fun AudioCard(content: @Composable () -> Unit) {
    val colors = LocalClintColors.current
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
    ) {
        content()
    }
}

@Composable
private fun CardDivider() {
    val colors = LocalClintColors.current
    RowDivider(color = colors.divider.copy(alpha = 0.6f))
}

@Composable
internal fun CardHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    value: String? = null
) {
    val colors = LocalClintColors.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth().settingsSpotlight(title)) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = icon, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
    }
}

@Composable
internal fun ChoiceRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceVariant)
            .padding(4.dp)
    ) {
        options.forEach { (key, label) ->
            val isSelected = key == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .kateChoice(isSelected, chip = true)
                    .clickable {
                        if (!isSelected) {
                            haptics.click()
                            onSelect(key)
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = kateChoiceText(isSelected),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun KateSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0
) {
    val colors = LocalClintColors.current
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = range,
        steps = steps,
        colors = SliderDefaults.colors(
            thumbColor = colors.primary,
            activeTrackColor = colors.primary,
            inactiveTrackColor = colors.primary.copy(alpha = 0.2f),
            activeTickColor = colors.onPrimary.copy(alpha = 0.6f),
            inactiveTickColor = colors.primary.copy(alpha = 0.5f)
        ),
        modifier = modifier
    )
}

@Composable
private fun SliderEdgeLabels(start: String, end: String) {
    val colors = LocalClintColors.current
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Text(
            text = start,
            style = MaterialTheme.typography.labelSmall,
            color = colors.secondaryText,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = end,
            style = MaterialTheme.typography.labelSmall,
            color = colors.secondaryText
        )
    }
}

@Composable
internal fun ValueSlider(
    title: String,
    subtitle: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    steps: Int = 0
) {
    val colors = LocalClintColors.current
    Column(modifier = Modifier.fillMaxWidth().settingsSpotlight(title).padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
        KateSlider(value = value, onValueChange = onValueChange, range = range, steps = steps)
    }
}

@Composable
private fun IconSlider(
    icon: ImageVector,
    title: String,
    subtitle: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    startLabel: String,
    endLabel: String,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp)) {
        CardHeader(icon = icon, title = title, subtitle = subtitle, value = valueText)
        KateSlider(
            value = value,
            onValueChange = onValueChange,
            range = range,
            steps = steps,
            modifier = Modifier.padding(top = 8.dp)
        )
        SliderEdgeLabels(start = startLabel, end = endLabel)
    }
}

@Composable
internal fun BoostCard() {
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val bass = AudioSettings.bassBoostDb
    val treble = AudioSettings.trebleBoostDb
    val off = stringResource(R.string.crossfade_off)
    AudioCard {
        IconSlider(
            icon = Icons.Outlined.Speaker,
            title = stringResource(R.string.bass_title),
            subtitle = stringResource(R.string.bass_subtitle),
            valueText = if (bass == 0) off else stringResource(R.string.eq_db_value, "+$bass"),
            value = bass.toFloat(),
            range = 0f..AudioSettings.MAX_BASS_DB.toFloat(),
            steps = AudioSettings.MAX_BASS_DB - 1,
            startLabel = off,
            endLabel = stringResource(R.string.eq_db_value, "+${AudioSettings.MAX_BASS_DB}"),
            onValueChange = { raw ->
                val snapped = raw.roundToInt()
                if (snapped != bass) {
                    haptics.tick()
                    AudioSettings.setBassBoostDb(context, snapped)
                    EqualizerSettings.setEnabled(context, true)
                }
            }
        )
        CardDivider()
        IconSlider(
            icon = Icons.Outlined.Waves,
            title = stringResource(R.string.treble_title),
            subtitle = stringResource(R.string.treble_subtitle),
            valueText = if (treble == 0) off else stringResource(R.string.eq_db_value, "+$treble"),
            value = treble.toFloat(),
            range = 0f..AudioSettings.MAX_TREBLE_DB.toFloat(),
            steps = AudioSettings.MAX_TREBLE_DB - 1,
            startLabel = off,
            endLabel = stringResource(R.string.eq_db_value, "+${AudioSettings.MAX_TREBLE_DB}"),
            onValueChange = { raw ->
                val snapped = raw.roundToInt()
                if (snapped != treble) {
                    haptics.tick()
                    AudioSettings.setTrebleBoostDb(context, snapped)
                    EqualizerSettings.setEnabled(context, true)
                }
            }
        )
    }
}

@Composable
private fun StereoWidthCard() {
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val width = AudioSettings.stereoWidth
    val widthStep = AudioSettings.STEREO_WIDTH_STEP
    AudioCard {
        IconSlider(
            icon = Icons.Outlined.SurroundSound,
            title = stringResource(R.string.stereo_width_title),
            subtitle = stringResource(R.string.stereo_width_subtitle),
            valueText = if (width == AudioSettings.NORMAL_STEREO_WIDTH) {
                stringResource(R.string.width_normal)
            } else {
                stringResource(R.string.percent_value, width)
            },
            value = width.toFloat(),
            range = 0f..AudioSettings.MAX_STEREO_WIDTH.toFloat(),
            steps = AudioSettings.MAX_STEREO_WIDTH / widthStep - 1,
            startLabel = stringResource(R.string.width_narrow),
            endLabel = stringResource(R.string.width_wide),
            onValueChange = { raw ->
                val snapped = (raw / widthStep).roundToInt() * widthStep
                if (snapped != width) {
                    haptics.tick()
                    AudioSettings.setStereoWidth(context, snapped)
                }
            }
        )
    }
}

@Composable
private fun LimiterCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val limit = AudioSettings.volumeLimit
    val step = AudioSettings.VOLUME_LIMIT_STEP
    AudioCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Compress,
            title = stringResource(R.string.limiter_title),
            subtitle = stringResource(R.string.limiter_subtitle),
            checked = AudioSettings.limiter,
            colors = colors,
            onCheckedChange = { AudioSettings.setLimiter(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
        CardDivider()
        IconSlider(
            icon = Icons.AutoMirrored.Outlined.VolumeDown,
            title = stringResource(R.string.volume_limit_title),
            subtitle = stringResource(R.string.volume_limit_subtitle),
            valueText = if (limit >= AudioSettings.MAX_VOLUME_LIMIT) {
                stringResource(R.string.volume_limit_none)
            } else {
                stringResource(R.string.percent_value, limit)
            },
            value = limit.toFloat(),
            range = AudioSettings.MIN_VOLUME_LIMIT.toFloat()..AudioSettings.MAX_VOLUME_LIMIT.toFloat(),
            steps = (AudioSettings.MAX_VOLUME_LIMIT - AudioSettings.MIN_VOLUME_LIMIT) / step - 1,
            startLabel = stringResource(R.string.percent_value, AudioSettings.MIN_VOLUME_LIMIT),
            endLabel = stringResource(R.string.volume_limit_none),
            onValueChange = { raw ->
                val snapped = (raw / step).roundToInt() * step
                if (snapped != limit) {
                    haptics.tick()
                    AudioSettings.setVolumeLimit(context, snapped)
                }
            }
        )
    }
}

@Composable
private fun LongTrackCard() {
    val context = LocalContext.current
    val minutes = AudioSettings.longTrackMinutes
    val off = stringResource(R.string.crossfade_off)
    val labels = AudioSettings.LongTrackMinutes.map { value ->
        value to if (value == 0) off else stringResource(R.string.minutes_value, value)
    }
    AudioCard {
        CardHeader(
            icon = Icons.Outlined.Restore,
            title = stringResource(R.string.long_track_title),
            subtitle = stringResource(if (minutes == 0) R.string.long_track_off_hint else R.string.long_track_on_hint),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
        )
        ChoiceRow(
            options = labels.map { (value, label) -> value.toString() to label },
            selected = minutes.toString(),
            onSelect = { key -> key.toIntOrNull()?.let { AudioSettings.setLongTrackMinutes(context, it) } },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun ChannelCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val balance = AudioSettings.balance
    val percent = (abs(balance) * 100f).roundToInt()
    val balanceText = when {
        percent == 0 -> stringResource(R.string.balance_center)
        balance < 0f -> stringResource(R.string.balance_left_value, percent)
        else -> stringResource(R.string.balance_right_value, percent)
    }
    AudioCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Hearing,
            title = stringResource(R.string.mono_title),
            subtitle = stringResource(R.string.mono_subtitle),
            checked = AudioSettings.monoAudio,
            colors = colors,
            onCheckedChange = { AudioSettings.setMonoAudio(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
        CardDivider()
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp)) {
            ValueSlider(
                title = stringResource(R.string.balance_title),
                subtitle = stringResource(R.string.balance_subtitle),
                valueText = balanceText,
                value = balance,
                range = -1f..1f,
                onValueChange = { raw ->
                    val snapped = ((raw * 20f).roundToInt() / 20f).coerceIn(-1f, 1f)
                    if (snapped != balance) {
                        if ((snapped * 10f).roundToInt() != (balance * 10f).roundToInt() || snapped == 0f) haptics.tick()
                        AudioSettings.setBalance(context, snapped)
                    }
                }
            )
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SliderEdgeLabels(
                    start = stringResource(R.string.balance_left),
                    end = stringResource(R.string.balance_right)
                )
            }
        }
    }
}

@Composable
private fun ReplayGainCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val mode = AudioSettings.replayGainMode
    val modeLabels = mapOf(
        AudioSettings.REPLAY_GAIN_OFF to stringResource(R.string.rg_mode_off),
        AudioSettings.REPLAY_GAIN_TRACK to stringResource(R.string.rg_mode_track),
        AudioSettings.REPLAY_GAIN_ALBUM to stringResource(R.string.rg_mode_album)
    )
    AudioCard {
        CardHeader(
            icon = Icons.AutoMirrored.Outlined.VolumeUp,
            title = stringResource(R.string.rg_title),
            subtitle = stringResource(
                when (mode) {
                    AudioSettings.REPLAY_GAIN_TRACK -> R.string.rg_mode_track_hint
                    AudioSettings.REPLAY_GAIN_ALBUM -> R.string.rg_mode_album_hint
                    else -> R.string.rg_mode_off_hint
                }
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
        )
        ChoiceRow(
            options = AudioSettings.ReplayGainModes.map { it to modeLabels.getValue(it) },
            selected = mode,
            onSelect = { AudioSettings.setReplayGainMode(context, it) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
        AnimatedVisibility(visible = mode != AudioSettings.REPLAY_GAIN_OFF) {
            Column {
                GainSlider(
                    title = stringResource(R.string.rg_preamp_title),
                    subtitle = stringResource(R.string.rg_preamp_subtitle),
                    value = AudioSettings.replayGainPreampDb,
                    range = AudioSettings.MIN_RG_PREAMP_DB..AudioSettings.MAX_RG_PREAMP_DB,
                    onChange = { AudioSettings.setReplayGainPreamp(context, it) }
                )
                GainSlider(
                    title = stringResource(R.string.rg_fallback_title),
                    subtitle = stringResource(R.string.rg_fallback_subtitle),
                    value = AudioSettings.replayGainFallbackDb,
                    range = AudioSettings.MIN_RG_FALLBACK_DB..AudioSettings.MAX_RG_FALLBACK_DB,
                    onChange = { AudioSettings.setReplayGainFallback(context, it) }
                )
                SettingsSwitchRow(
                    icon = Icons.Outlined.Shield,
                    title = stringResource(R.string.rg_prevent_clipping_title),
                    subtitle = stringResource(R.string.rg_prevent_clipping_subtitle),
                    checked = AudioSettings.replayGainPreventClipping,
                    colors = colors,
                    onCheckedChange = { AudioSettings.setReplayGainPreventClipping(context, it) },
                    subtitleLines = Int.MAX_VALUE
                )
            }
        }
        Text(
            text = stringResource(R.string.rg_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)
        )
    }
}

@Composable
private fun GainSlider(
    title: String,
    subtitle: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    val haptics = LocalKateHaptics.current
    ValueSlider(
        title = title,
        subtitle = subtitle,
        valueText = stringResource(R.string.eq_db_value, formatDb(value)),
        value = value,
        range = range,
        onValueChange = { raw ->
            val snapped = ((raw * 2f).roundToInt() / 2f).coerceIn(range.start, range.endInclusive)
            if (snapped != value) {
                if (snapped.roundToInt() != value.roundToInt()) haptics.tick()
                onChange(snapped)
            }
        }
    )
}

@Composable
private fun GaplessCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    AudioCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Album,
            title = stringResource(R.string.gapless_title),
            subtitle = stringResource(R.string.gapless_subtitle),
            checked = AudioSettings.gaplessAlbums,
            colors = colors,
            onCheckedChange = { AudioSettings.setGaplessAlbums(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
    }
}

@Composable
private fun CrossfadeCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val seconds = AudioSettings.crossfadeSeconds
    val curve = AudioSettings.crossfadeCurve
    val curveLabels = mapOf(
        AudioSettings.CURVE_SMOOTH to stringResource(R.string.curve_smooth),
        AudioSettings.CURVE_LINEAR to stringResource(R.string.curve_linear),
        AudioSettings.CURVE_S to stringResource(R.string.curve_s)
    )
    AudioCard {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp)) {
            CardHeader(
                icon = Icons.Outlined.Tune,
                title = stringResource(R.string.row_crossfade_title),
                subtitle = stringResource(R.string.row_crossfade_summary),
                value = if (seconds == 0) {
                    stringResource(R.string.crossfade_off)
                } else {
                    pluralStringResource(R.plurals.crossfade_seconds, seconds, seconds)
                }
            )
            KateSlider(
                value = seconds.toFloat(),
                onValueChange = { AudioSettings.setCrossfadeSeconds(context, it.roundToInt()) },
                range = 0f..AudioSettings.MAX_CROSSFADE_SECONDS.toFloat(),
                steps = AudioSettings.MAX_CROSSFADE_SECONDS - 1,
                modifier = Modifier.padding(top = 8.dp)
            )
            SliderEdgeLabels(
                start = stringResource(R.string.crossfade_off),
                end = pluralStringResource(R.plurals.crossfade_seconds, AudioSettings.MAX_CROSSFADE_SECONDS, AudioSettings.MAX_CROSSFADE_SECONDS)
            )
        }
        AnimatedVisibility(visible = seconds > 0) {
            Column {
                CardDivider()
                Text(
                    text = stringResource(R.string.crossfade_curve_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
                )
                Text(
                    text = stringResource(
                        when (curve) {
                            AudioSettings.CURVE_LINEAR -> R.string.curve_linear_hint
                            AudioSettings.CURVE_S -> R.string.curve_s_hint
                            else -> R.string.curve_smooth_hint
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp)
                )
                ChoiceRow(
                    options = AudioSettings.CrossfadeCurves.map { it to curveLabels.getValue(it) },
                    selected = curve,
                    onSelect = { AudioSettings.setCrossfadeCurve(context, it) },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
                )
                SettingsSwitchRow(
                    icon = Icons.Outlined.SkipNext,
                    title = stringResource(R.string.crossfade_skip_title),
                    subtitle = stringResource(R.string.crossfade_skip_subtitle),
                    checked = AudioSettings.crossfadeOnSkip,
                    colors = colors,
                    onCheckedChange = { AudioSettings.setCrossfadeOnSkip(context, it) },
                    subtitleLines = Int.MAX_VALUE
                )
                SettingsSwitchRow(
                    icon = Icons.Outlined.Gradient,
                    title = stringResource(R.string.crossfade_animation_title),
                    subtitle = stringResource(R.string.crossfade_animation_subtitle),
                    checked = AudioSettings.crossfadeAnimation,
                    colors = colors,
                    onCheckedChange = { AudioSettings.setCrossfadeAnimation(context, it) },
                    subtitleLines = Int.MAX_VALUE
                )
            }
        }
    }
}

@Composable
private fun PlaybackExtrasCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val fadeMs = AudioSettings.playPauseFadeMs
    val step = AudioSettings.PLAY_PAUSE_FADE_STEP_MS
    val max = AudioSettings.MAX_PLAY_PAUSE_FADE_MS
    val rewind = AudioSettings.rewindSeconds
    val rewindStep = AudioSettings.REWIND_STEP_SECONDS
    AudioCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.FastForward,
            title = stringResource(R.string.skip_silence_title),
            subtitle = stringResource(R.string.skip_silence_subtitle),
            checked = AudioSettings.skipSilence,
            colors = colors,
            onCheckedChange = { AudioSettings.setSkipSilence(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
        CardDivider()
        SettingsSwitchRow(
            icon = Icons.Outlined.Waves,
            title = stringResource(R.string.smooth_seek_title),
            subtitle = stringResource(R.string.smooth_seek_subtitle),
            checked = AudioSettings.smoothSeeking,
            colors = colors,
            onCheckedChange = { AudioSettings.setSmoothSeeking(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
        CardDivider()
        Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp)) {
            CardHeader(
                icon = Icons.Outlined.Gradient,
                title = stringResource(R.string.play_pause_fade_title),
                subtitle = stringResource(R.string.play_pause_fade_subtitle),
                value = if (fadeMs == 0) stringResource(R.string.crossfade_off) else stringResource(R.string.fade_ms_value, fadeMs)
            )
            KateSlider(
                value = fadeMs.toFloat(),
                onValueChange = { raw ->
                    val snapped = (raw / step).roundToInt() * step
                    if (snapped != fadeMs) {
                        haptics.tick()
                        AudioSettings.setPlayPauseFadeMs(context, snapped)
                    }
                },
                range = 0f..max.toFloat(),
                steps = max / step - 1,
                modifier = Modifier.padding(top = 8.dp)
            )
            SliderEdgeLabels(
                start = stringResource(R.string.crossfade_off),
                end = stringResource(R.string.fade_ms_value, max)
            )
        }
        CardDivider()
        IconSlider(
            icon = Icons.Outlined.Replay,
            title = stringResource(R.string.rewind_title),
            subtitle = stringResource(R.string.rewind_subtitle),
            valueText = if (rewind == 0) {
                stringResource(R.string.crossfade_off)
            } else {
                pluralStringResource(R.plurals.crossfade_seconds, rewind, rewind)
            },
            value = rewind.toFloat(),
            range = 0f..AudioSettings.MAX_REWIND_SECONDS.toFloat(),
            steps = AudioSettings.MAX_REWIND_SECONDS / rewindStep - 1,
            startLabel = stringResource(R.string.crossfade_off),
            endLabel = pluralStringResource(R.plurals.crossfade_seconds, AudioSettings.MAX_REWIND_SECONDS, AudioSettings.MAX_REWIND_SECONDS),
            onValueChange = { raw ->
                val snapped = (raw / rewindStep).roundToInt() * rewindStep
                if (snapped != rewind) {
                    haptics.tick()
                    AudioSettings.setRewindSeconds(context, snapped)
                }
            }
        )
    }
}

@Composable
private fun FocusCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val mode = AudioSettings.focusMode
    val duck = AudioSettings.duckPercent
    val step = AudioSettings.DUCK_PERCENT_STEP
    val modeLabels = mapOf(
        AudioSettings.FOCUS_PAUSE to stringResource(R.string.focus_pause),
        AudioSettings.FOCUS_DUCK to stringResource(R.string.focus_duck),
        AudioSettings.FOCUS_IGNORE to stringResource(R.string.focus_ignore)
    )
    AudioCard {
        CardHeader(
            icon = Icons.Outlined.NotificationsPaused,
            title = stringResource(R.string.focus_title),
            subtitle = stringResource(
                when (mode) {
                    AudioSettings.FOCUS_DUCK -> R.string.focus_duck_hint
                    AudioSettings.FOCUS_IGNORE -> R.string.focus_ignore_hint
                    else -> R.string.focus_pause_hint
                }
            ),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
        )
        ChoiceRow(
            options = AudioSettings.FocusModes.map { it to modeLabels.getValue(it) },
            selected = mode,
            onSelect = { AudioSettings.setFocusMode(context, it) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
        AnimatedVisibility(visible = mode != AudioSettings.FOCUS_IGNORE) {
            Column {
                ValueSlider(
                    title = stringResource(R.string.duck_title),
                    subtitle = stringResource(R.string.duck_subtitle),
                    valueText = stringResource(R.string.percent_value, duck),
                    value = duck.toFloat(),
                    range = AudioSettings.MIN_DUCK_PERCENT.toFloat()..AudioSettings.MAX_DUCK_PERCENT.toFloat(),
                    steps = (AudioSettings.MAX_DUCK_PERCENT - AudioSettings.MIN_DUCK_PERCENT) / step - 1,
                    onValueChange = { raw ->
                        val snapped = (raw / step).roundToInt() * step
                        if (snapped != duck) {
                            haptics.tick()
                            AudioSettings.setDuckPercent(context, snapped)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                CardDivider()
                SettingsSwitchRow(
                    icon = Icons.Outlined.PlayCircle,
                    title = stringResource(R.string.resume_title),
                    subtitle = stringResource(R.string.resume_subtitle),
                    checked = AudioSettings.resumeAfterInterruption,
                    colors = colors,
                    onCheckedChange = { AudioSettings.setResumeAfterInterruption(context, it) },
                    subtitleLines = Int.MAX_VALUE
                )
            }
        }
    }
}

@Composable
private fun HeadphonesCard(onChooseDevices: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val startVolume = AudioSettings.startVolume
    val volumeStep = AudioSettings.START_VOLUME_STEP
    val off = stringResource(R.string.crossfade_off)
    AudioCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.HeadsetOff,
            title = stringResource(R.string.disconnect_title),
            subtitle = stringResource(R.string.disconnect_subtitle),
            checked = AudioSettings.pauseOnDisconnect,
            colors = colors,
            onCheckedChange = { AudioSettings.setPauseOnDisconnect(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
        CardDivider()
        SettingsSwitchRow(
            icon = Icons.Outlined.Headphones,
            title = stringResource(R.string.autoplay_wired_title),
            subtitle = stringResource(R.string.autoplay_wired_subtitle),
            checked = AudioSettings.autoPlayWired,
            colors = colors,
            onCheckedChange = { AudioSettings.setAutoPlayWired(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
        CardDivider()
        SettingsSwitchRow(
            icon = Icons.Outlined.BluetoothAudio,
            title = stringResource(R.string.autoplay_bluetooth_title),
            subtitle = stringResource(R.string.autoplay_bluetooth_subtitle),
            checked = AudioSettings.autoPlayBluetooth,
            colors = colors,
            onCheckedChange = { AudioSettings.setAutoPlayBluetooth(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
        AnimatedVisibility(visible = AudioSettings.autoPlayBluetooth) {
            Column {
                CardDivider()
                NavigationRow(
                    icon = Icons.Outlined.TaskAlt,
                    title = stringResource(R.string.bt_devices_title),
                    subtitle = if (AudioSettings.bluetoothOnlyChosen) {
                        val count = AudioSettings.chosenBluetoothDevices.size
                        pluralStringResource(R.plurals.bt_devices_chosen, count, count)
                    } else {
                        stringResource(R.string.bt_devices_any)
                    },
                    onClick = onChooseDevices
                )
            }
        }
        CardDivider()
        IconSlider(
            icon = Icons.AutoMirrored.Outlined.VolumeUp,
            title = stringResource(R.string.start_volume_title),
            subtitle = stringResource(R.string.start_volume_subtitle),
            valueText = if (startVolume == 0) off else stringResource(R.string.percent_value, startVolume),
            value = startVolume.toFloat(),
            range = 0f..AudioSettings.MAX_START_VOLUME.toFloat(),
            steps = AudioSettings.MAX_START_VOLUME / volumeStep - 1,
            startLabel = off,
            endLabel = stringResource(R.string.percent_value, AudioSettings.MAX_START_VOLUME),
            onValueChange = { raw ->
                val snapped = (raw / volumeStep).roundToInt() * volumeStep
                if (snapped != startVolume) {
                    haptics.tick()
                    AudioSettings.setStartVolume(context, snapped)
                }
            }
        )
        Text(
            text = stringResource(R.string.autoplay_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        )
    }
}

@Composable
private fun OffloadCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val supported = AudioSettings.offloadSupported
    val enabled = AudioSettings.audioOffload
    val subtitle = when {
        !supported -> stringResource(R.string.offload_unsupported)
        enabled && AudioSettings.effectsBlockOffload -> stringResource(R.string.offload_on_hold)
        else -> stringResource(R.string.offload_subtitle)
    }
    AudioCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.BatterySaver,
            title = stringResource(R.string.offload_title),
            subtitle = subtitle,
            checked = supported && enabled,
            colors = colors,
            onCheckedChange = { AudioSettings.setAudioOffload(context, it) },
            enabled = supported,
            subtitleLines = Int.MAX_VALUE
        )
    }
}

@Composable
private fun NavigationRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptics.click()
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(40.dp),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = icon, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp, end = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        KateRowArrow()
    }
}

@Composable
internal fun routeLabel(route: String): String = stringResource(
    when (route) {
        OutputRoute.WIRED -> R.string.route_wired
        OutputRoute.BLUETOOTH -> R.string.route_bluetooth
        OutputRoute.USB -> R.string.route_usb
        else -> R.string.route_speaker
    }
)

@Composable
private fun PerDeviceEqCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val perDevice = EqualizerSettings.perDevice
    AudioCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Devices,
            title = stringResource(R.string.per_device_eq_title),
            subtitle = if (perDevice) {
                stringResource(R.string.per_device_eq_on, routeLabel(EqualizerSettings.activeRoute))
            } else {
                stringResource(R.string.per_device_eq_off)
            },
            checked = perDevice,
            colors = colors,
            onCheckedChange = { EqualizerSettings.setPerDevice(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
    }
}

@Composable
private fun VolumeStepsCard() {
    val context = LocalContext.current
    val steps = AudioSettings.volumeSubSteps
    val labels = AudioSettings.VolumeSubSteps.map { value ->
        value.toString() to if (value == 1) {
            stringResource(R.string.volume_steps_normal)
        } else {
            stringResource(R.string.volume_steps_value, value)
        }
    }
    AudioCard {
        CardHeader(
            icon = Icons.Outlined.LinearScale,
            title = stringResource(R.string.volume_steps_title),
            subtitle = stringResource(if (steps == 1) R.string.volume_steps_off_hint else R.string.volume_steps_on_hint),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
        )
        ChoiceRow(
            options = labels,
            selected = steps.toString(),
            onSelect = { key -> key.toIntOrNull()?.let { AudioSettings.setVolumeSubSteps(context, it) } },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

internal fun formatSpeed(value: Float): String =
    String.format(Locale.getDefault(), "%.2f", value).trimEnd('0').trimEnd('.', ',')

@Composable
private fun SpeedCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val speed = AudioSettings.playbackSpeed
    val min = AudioSettings.MIN_SPEED
    val max = AudioSettings.MAX_SPEED
    val step = AudioSettings.SPEED_STEP
    AudioCard {
        IconSlider(
            icon = Icons.Outlined.Speed,
            title = stringResource(R.string.speed_title),
            subtitle = stringResource(R.string.speed_subtitle),
            valueText = stringResource(R.string.speed_value, formatSpeed(speed)),
            value = speed,
            range = min..max,
            steps = ((max - min) / step).roundToInt() - 1,
            startLabel = stringResource(R.string.speed_value, formatSpeed(min)),
            endLabel = stringResource(R.string.speed_value, formatSpeed(max)),
            onValueChange = { raw ->
                val snapped = (min + ((raw - min) / step).roundToInt() * step).coerceIn(min, max)
                if (abs(snapped - speed) > 0.001f) {
                    haptics.tick()
                    AudioSettings.setPlaybackSpeed(context, snapped)
                }
            }
        )
        CardDivider()
        SettingsSwitchRow(
            icon = Icons.Outlined.MusicNote,
            title = stringResource(R.string.keep_pitch_title),
            subtitle = stringResource(R.string.keep_pitch_subtitle),
            checked = AudioSettings.keepPitch,
            colors = colors,
            onCheckedChange = { AudioSettings.setKeepPitch(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
    }
}

@Composable
private fun MuteCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    AudioCard {
        SettingsSwitchRow(
            icon = Icons.AutoMirrored.Outlined.VolumeOff,
            title = stringResource(R.string.mute_pause_title),
            subtitle = stringResource(R.string.mute_pause_subtitle),
            checked = AudioSettings.pauseWhenMuted,
            colors = colors,
            onCheckedChange = { AudioSettings.setPauseWhenMuted(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
    }
}

@Composable
private fun BluetoothDevicesDialog(visible: Boolean, onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val onlyChosen = AudioSettings.bluetoothOnlyChosen
    val devices = AudioSettings.knownBluetoothDevices
    val chosen = AudioSettings.chosenBluetoothDevices
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .widthIn(max = 480.dp)
                .heightIn(max = screenHeight * 0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(R.string.bt_devices_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 4.dp)
            )
            SettingsSwitchRow(
                icon = Icons.Outlined.BluetoothAudio,
                title = stringResource(R.string.bt_dialog_any_title),
                subtitle = stringResource(R.string.bt_dialog_any_subtitle),
                checked = !onlyChosen,
                colors = colors,
                onCheckedChange = { AudioSettings.setBluetoothOnlyChosen(context, !it) },
                subtitleLines = Int.MAX_VALUE
            )
            CardDivider()
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                if (devices.isEmpty()) {
                    Text(
                        text = stringResource(R.string.bt_dialog_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
                devices.forEach { name ->
                    BluetoothDeviceRow(
                        name = name,
                        checked = name in chosen,
                        enabled = onlyChosen,
                        onCheckedChange = { AudioSettings.setBluetoothChosen(context, name, it) },
                        onForget = { AudioSettings.forgetBluetoothDevice(context, name) }
                    )
                }
            }
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.audio_done),
                    role = DialogButtonRole.MAIN,
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun BluetoothDeviceRow(
    name: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onForget: () -> Unit
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val change: (Boolean) -> Unit = {
        haptics.toggle(it)
        onCheckedChange(it)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { change(!checked) }
            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .kateIconTile(36.dp)
                .alpha(if (enabled) 1f else 0.6f),
            contentAlignment = Alignment.Center
        ) {
            KateIcon(imageVector = Icons.Outlined.BluetoothAudio, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(18.dp))
        }
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .alpha(if (enabled) 1f else 0.6f)
        )
        KateIconButton(onClick = onForget, modifier = Modifier.size(36.dp)) {
            KateIcon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.bt_forget, name),
                tint = buttonTint(colors.secondaryText),
                modifier = Modifier.size(18.dp)
            )
        }
        ClintSwitch(checked = checked, onCheckedChange = change, enabled = enabled)
    }
}

private fun formatRate(rate: Int): String {
    val khz = rate / 1000.0
    return if (rate % 1000 == 0) {
        String.format(Locale.getDefault(), "%d", rate / 1000)
    } else {
        String.format(Locale.getDefault(), "%.1f", khz)
    }
}

@Composable
private fun HiFiCard() {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val deviceRate = remember {
        context.getSystemService(AudioManager::class.java)
            ?.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
            ?.toIntOrNull()
            ?: 0
    }
    val bitPerfectSupported = AudioSettings.bitPerfectSupported
    val bitPerfectSubtitle = when {
        !bitPerfectSupported -> stringResource(R.string.bitperfect_unsupported)
        AudioSettings.bitPerfect && AudioSettings.bitPerfectEngaged -> stringResource(R.string.bitperfect_active)
        AudioSettings.bitPerfect -> stringResource(R.string.bitperfect_waiting)
        else -> stringResource(R.string.bitperfect_off)
    }
    AudioCard {
        SettingsSwitchRow(
            icon = Icons.Outlined.Memory,
            title = stringResource(R.string.native_title),
            subtitle = stringResource(R.string.native_subtitle),
            checked = AudioSettings.nativeOutput,
            colors = colors,
            onCheckedChange = { AudioSettings.setNativeOutput(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
        CardDivider()
        SettingsSwitchRow(
            icon = Icons.Outlined.HighQuality,
            title = stringResource(R.string.hires_title),
            subtitle = stringResource(if (AudioSettings.nativeOutput) R.string.hires_subtitle_native else R.string.hires_subtitle),
            checked = AudioSettings.hiResOutput,
            colors = colors,
            onCheckedChange = { AudioSettings.setHiResOutput(context, it) },
            subtitleLines = Int.MAX_VALUE
        )
        CardDivider()
        SettingsSwitchRow(
            icon = Icons.Outlined.Transform,
            title = stringResource(R.string.resample_title),
            subtitle = if (deviceRate > 0) {
                stringResource(R.string.resample_subtitle, stringResource(R.string.resample_rate, formatRate(deviceRate)))
            } else {
                stringResource(R.string.resample_subtitle_unknown)
            },
            checked = AudioSettings.resampleToDevice,
            colors = colors,
            onCheckedChange = { AudioSettings.setResampleToDevice(context, it) },
            enabled = deviceRate > 0,
            subtitleLines = Int.MAX_VALUE
        )
        CardDivider()
        SettingsSwitchRow(
            icon = Icons.Outlined.Usb,
            title = stringResource(R.string.bitperfect_title),
            subtitle = bitPerfectSubtitle,
            checked = bitPerfectSupported && AudioSettings.bitPerfect,
            colors = colors,
            onCheckedChange = { AudioSettings.setBitPerfect(context, it) },
            enabled = bitPerfectSupported,
            subtitleLines = Int.MAX_VALUE
        )
    }
}
