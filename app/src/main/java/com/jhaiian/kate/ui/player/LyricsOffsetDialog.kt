package com.jhaiian.kate.ui.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LyricsOffsetStore
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.theme.LocalClintColors
import java.util.Locale

private val OffsetSteps = listOf(-500L, -100L, 100L, 500L)

private fun offsetLabel(ms: Long): String = String.format(Locale.US, "%+.1f s", ms / 1000f)

@Composable
internal fun LyricsOffsetDialog(visible: Boolean, trackId: String, onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val offset = LyricsOffsetStore.offsetFor(trackId)
    KateDialog(visible = visible, onDismiss = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(R.string.lyrics_offset_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 4.dp)
            )
            Text(
                text = stringResource(R.string.lyrics_offset_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            )
            Text(
                text = offsetLabel(offset),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 14.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                OffsetSteps.forEach { step ->
                    KateTextButton(
                        label = offsetLabel(step),
                        role = DialogButtonRole.SECONDARY,
                        onClick = {
                            haptics.click()
                            LyricsOffsetStore.set(context, trackId, offset + step)
                        }
                    )
                }
            }
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 8.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.lyrics_offset_reset),
                    role = DialogButtonRole.SECONDARY,
                    onClick = { LyricsOffsetStore.set(context, trackId, 0L) }
                )
                KateTextButton(label = stringResource(R.string.audio_done), role = DialogButtonRole.MAIN, onClick = onDismiss)
            }
        }
    }
}
