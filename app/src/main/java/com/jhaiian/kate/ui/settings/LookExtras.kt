package com.jhaiian.kate.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateWindowDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateTextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.ui.theme.BackgroundStore
import com.jhaiian.kate.ui.theme.DisplayPreferences
import com.jhaiian.kate.ui.theme.FontStore
import com.jhaiian.kate.ui.theme.FontStyleOptions
import com.jhaiian.kate.ui.theme.LocalClintColors
import com.jhaiian.kate.ui.theme.LyricsWordStyleOptions
import com.jhaiian.kate.ui.theme.LookAndFeelSettings
import com.jhaiian.kate.ui.theme.LyricsPreferences
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import com.jhaiian.kate.ui.theme.TitleWeightOptions
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun FineTuneSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    format: (Float) -> String,
    onCommit: (Float) -> Unit
) {
    val colors = LocalClintColors.current
    var dragging by remember { mutableStateOf<Float?>(null) }
    val shown = dragging ?: value
    Column(modifier = Modifier.fillMaxWidth().settingsSpotlight(title).padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = format(shown),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
        Slider(
            value = shown.coerceIn(range.start, range.endInclusive),
            onValueChange = { dragging = it },
            onValueChangeFinished = {
                dragging?.let(onCommit)
                dragging = null
            },
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.primary.copy(alpha = 0.2f)
            )
        )
    }
}

internal fun percentLabel(value: Float): String = "${(value * 100).roundToInt()}%"

@Composable
internal fun TitleWeightRow(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var open by remember { mutableStateOf(false) }
    SettingsRow(
        icon = Icons.Outlined.FormatBold,
        title = stringResource(R.string.title_weight_title),
        subtitle = stringResource(TitleWeightOptions.first { it.key == settings.playerTitleWeight }.labelRes),
        colors = colors,
        onClick = { open = true }
    )
    if (open) {
        OptionSheet(
            title = stringResource(R.string.title_weight_title),
            options = TitleWeightOptions,
            current = settings.playerTitleWeight,
            onSelect = {
                onSettingsChange(settings.copy(playerTitleWeight = it))
                open = false
            },
            onDismiss = { open = false }
        )
    }
}

@Composable
internal fun LyricsWordStyleRow(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var open by remember { mutableStateOf(false) }
    val current = LyricsWordStyleOptions.firstOrNull { it.key == settings.lyricsWordStyle } ?: LyricsWordStyleOptions.first()
    SettingsRow(
        icon = Icons.Outlined.AutoAwesome,
        title = stringResource(R.string.lyrics_word_style_title),
        subtitle = stringResource(current.labelRes),
        colors = colors,
        onClick = { open = true }
    )
    if (open) {
        OptionSheet(
            title = stringResource(R.string.lyrics_word_style_title),
            options = LyricsWordStyleOptions,
            current = settings.lyricsWordStyle,
            onSelect = {
                onSettingsChange(settings.copy(lyricsWordStyle = it))
                open = false
            },
            onDismiss = { open = false }
        )
    }
}

@Composable
private fun fontChoices(includeApp: Boolean): List<Pair<String, String>> {
    val builtIn = FontStyleOptions.map { it.key to stringResource(it.labelRes) }
    val imported = FontStore.fonts.map { FontStore.key(it) to FontStore.label(it) }
    val app = if (includeApp) listOf(LyricsPreferences.FONT_APP to stringResource(R.string.lyrics_font_app)) else emptyList()
    return app + builtIn + imported
}

@Composable
internal fun LyricsFontRow(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    var open by remember { mutableStateOf(false) }
    val choices = fontChoices(includeApp = true)
    SettingsRow(
        icon = Icons.Outlined.FontDownload,
        title = stringResource(R.string.lyrics_font_title),
        subtitle = choices.firstOrNull { it.first == settings.lyricsFont }?.second ?: stringResource(R.string.lyrics_font_app),
        colors = colors,
        onClick = { open = true }
    )
    if (open) {
        TextOptionSheet(
            title = stringResource(R.string.lyrics_font_title),
            options = choices,
            current = settings.lyricsFont,
            onSelect = {
                onSettingsChange(settings.copy(lyricsFont = it))
                open = false
            },
            onDismiss = { open = false }
        )
    }
}

@Composable
internal fun BackgroundImagesSection(settings: LookAndFeelSettings, onSettingsChange: (LookAndFeelSettings) -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var slot by remember { mutableStateOf(BackgroundStore.SLOT_APP) }
    val failed = stringResource(R.string.background_image_failed)
    val latest by androidx.compose.runtime.rememberUpdatedState(settings)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val target = slot
            scope.launch {
                val name = BackgroundStore.import(context, uri, target)
                if (name == null) {
                    Toast.makeText(context, failed, Toast.LENGTH_SHORT).show()
                } else {
                    val current = latest
                    onSettingsChange(
                        if (target == BackgroundStore.SLOT_APP) current.copy(appBackgroundImage = name)
                        else current.copy(playerBackgroundImage = name, playerBackground = PlayerStylePreferences.BACKGROUND_IMAGE)
                    )
                }
            }
        }
    }
    fun pick(target: String) {
        slot = target
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    Spacer(modifier = Modifier.height(20.dp))
    SectionHeader(stringResource(R.string.look_section_background_images), colors.primary)
    SettingsCard {
        SettingsRow(
            icon = Icons.Outlined.Wallpaper,
            title = stringResource(R.string.background_image_app),
            subtitle = stringResource(if (settings.appBackgroundImage.isEmpty()) R.string.background_image_none else R.string.background_image_change),
            colors = colors,
            onClick = { pick(BackgroundStore.SLOT_APP) }
        )
        if (settings.appBackgroundImage.isNotEmpty()) {
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.HideImage,
                title = stringResource(R.string.background_image_remove_app),
                subtitle = stringResource(R.string.background_image_remove_summary),
                colors = colors,
                onClick = {
                    BackgroundStore.clear(context, BackgroundStore.SLOT_APP)
                    onSettingsChange(settings.copy(appBackgroundImage = ""))
                }
            )
        }
        RowDivider(colors.divider)
        SettingsRow(
            icon = Icons.Outlined.Wallpaper,
            title = stringResource(R.string.background_image_player),
            subtitle = stringResource(if (settings.playerBackgroundImage.isEmpty()) R.string.background_image_none else R.string.background_image_change),
            colors = colors,
            onClick = { pick(BackgroundStore.SLOT_PLAYER) }
        )
        if (settings.playerBackgroundImage.isNotEmpty()) {
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.HideImage,
                title = stringResource(R.string.background_image_remove_player),
                subtitle = stringResource(R.string.background_image_remove_summary),
                colors = colors,
                onClick = {
                    BackgroundStore.clear(context, BackgroundStore.SLOT_PLAYER)
                    val background = if (settings.playerBackground == PlayerStylePreferences.BACKGROUND_IMAGE) {
                        PlayerStylePreferences.DEFAULT_BACKGROUND
                    } else {
                        settings.playerBackground
                    }
                    val lyricsBackground = if (settings.lyricsBackground == PlayerStylePreferences.BACKGROUND_IMAGE) {
                        LyricsPreferences.DEFAULT_BACKGROUND
                    } else {
                        settings.lyricsBackground
                    }
                    onSettingsChange(settings.copy(playerBackgroundImage = "", playerBackground = background, lyricsBackground = lyricsBackground))
                }
            )
        }
        if (settings.appBackgroundImage.isNotEmpty() || settings.playerBackgroundImage.isNotEmpty()) {
            RowDivider(colors.divider)
            FineTuneSlider(
                title = stringResource(R.string.background_image_blur),
                value = settings.imageBlur.toFloat(),
                range = 0f..DisplayPreferences.MAX_IMAGE_BLUR.toFloat(),
                format = { "${it.roundToInt()}" },
                onCommit = { onSettingsChange(settings.copy(imageBlur = it.roundToInt())) }
            )
            FineTuneSlider(
                title = stringResource(R.string.background_image_dim),
                value = settings.imageDim / 100f,
                range = DisplayPreferences.MIN_IMAGE_DIM / 100f..DisplayPreferences.MAX_IMAGE_DIM / 100f,
                format = ::percentLabel,
                onCommit = { onSettingsChange(settings.copy(imageDim = (it * 100).roundToInt())) }
            )
        }
        Text(
            text = stringResource(R.string.background_image_hint),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)
        )
    }
}

@Composable
internal fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
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
                KateTextButton(label = stringResource(R.string.action_cancel), role = DialogButtonRole.SECONDARY, onClick = onDismiss)
                KateTextButton(label = confirmLabel, role = DialogButtonRole.DESTRUCTIVE, onClick = onConfirm)
            }
        }
    }
}
