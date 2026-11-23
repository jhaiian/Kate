package com.jhaiian.kate.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.playback.KateRadio
import com.jhaiian.kate.playback.PlayerController
import com.jhaiian.kate.playback.RadioSeed
import com.jhaiian.kate.ui.settings.RowDivider
import com.jhaiian.kate.ui.theme.LocalClintColors

object RadioRequest {
    var seed by mutableStateOf<RadioSeed?>(null)
        private set
    var managing by mutableStateOf(false)
        private set

    fun open(seed: RadioSeed) {
        managing = false
        this.seed = seed
    }

    fun manage() {
        if (KateRadio.session == null) return
        seed = null
        managing = true
    }

    fun close() {
        seed = null
        managing = false
    }
}

@Composable
fun KateRadioHost() {
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val colors = LocalClintColors.current
    val requested = RadioRequest.seed
    val managing = RadioRequest.managing
    val visible = requested != null || managing
    val holder = remember { RadioDialogSession() }
    if (visible && !holder.open) {
        holder.open = true
        holder.session++
    }
    if (!visible) holder.open = false
    if (requested != null) holder.seed = requested
    if (managing) holder.seed = KateRadio.session
    val shownSeed = holder.seed
    val isManaging = managing || (requested == null && holder.managing)
    holder.managing = isManaging
    val session = holder.session
    if (visible) KateRadio.ensureLoaded(context)
    var mix by remember(session) { mutableFloatStateOf(KateRadio.adventure) }
    var mixStep by remember(session) { mutableIntStateOf(stepOf(KateRadio.adventure)) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    KateDialog(visible = visible, onDismiss = { RadioRequest.close() }) {
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
                Box(
                    modifier = Modifier
                        .kateIconTile(34.dp),
                    contentAlignment = Alignment.Center
                ) {
                    KateIcon(imageVector = Icons.Filled.Radio, contentDescription = null, tint = kateTileTint(), modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        text = stringResource(R.string.radio_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface,
                        maxLines = 1
                    )
                    if (shownSeed != null) {
                        Text(
                            text = stringResource(R.string.radio_based_on, shownSeed.label),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                KateIconButton(onClick = { RadioRequest.close() }, modifier = Modifier.size(30.dp)) {
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
                    .padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.radio_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText
                )
                Text(
                    text = stringResource(R.string.radio_mix_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface,
                    modifier = Modifier.padding(top = 20.dp)
                )
                Slider(
                    value = mix,
                    onValueChange = { value ->
                        mix = value
                        val step = stepOf(value)
                        if (step != mixStep) {
                            mixStep = step
                            haptics.tick()
                        }
                    },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.primary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.radio_mix_familiar),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.secondaryText,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.radio_mix_adventurous),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.secondaryText
                    )
                }
                Text(
                    text = stringResource(
                        when (mixStep) {
                            0 -> R.string.radio_mix_close
                            1 -> R.string.radio_mix_balanced
                            else -> R.string.radio_mix_wild
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            RowDivider(color = colors.divider.copy(alpha = 0.6f))
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 8.dp, bottom = 8.dp)) {
                if (isManaging) {
                    KateTextButton(
                        label = stringResource(R.string.radio_stop),
                        role = DialogButtonRole.DESTRUCTIVE,
                        onClick = {
                        haptics.click()
                        KateRadio.stop(context)
                        RadioRequest.close()
                    }
                    )
                    KateTextButton(
                        label = stringResource(R.string.radio_apply),
                        role = DialogButtonRole.MAIN,
                        onClick = {
                        haptics.confirm()
                        KateRadio.applyMix(context, mix)
                        RadioRequest.close()
                    }
                    )
                } else {
                    KateTextButton(
                        label = stringResource(R.string.action_cancel),
                        role = DialogButtonRole.SECONDARY,
                        onClick = { RadioRequest.close() }
                    )
                    KateTextButton(
                        label = stringResource(R.string.radio_start),
                        role = DialogButtonRole.MAIN,
                        enabled = !KateRadio.starting,
                        onClick = {
                            val seed = RadioRequest.seed
                            if (seed != null) {
                                haptics.confirm()
                                KateRadio.start(context, seed, mix)
                                PlayerController.requestOpenPlayer()
                            }
                            RadioRequest.close()
                        }
                    )
                }
            }
        }
    }
}

private class RadioDialogSession {
    var open = false
    var session = 0
    var seed: RadioSeed? = null
    var managing = false
}

private fun stepOf(value: Float): Int = when {
    value < 0.34f -> 0
    value < 0.67f -> 1
    else -> 2
}
