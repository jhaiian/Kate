package com.jhaiian.kate.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AudioEffects
import com.jhaiian.kate.data.ParametricFilter
import com.jhaiian.kate.playback.BiquadMath
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.KateWindowDialog
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.theme.LocalClintColors
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

private const val CURVE_RANGE_DB = 20f

private fun frequencyToSlider(frequency: Float): Float =
    (ln(frequency / AudioEffects.MIN_FREQUENCY) / ln(AudioEffects.MAX_FREQUENCY / AudioEffects.MIN_FREQUENCY)).coerceIn(0f, 1f)

private fun sliderToFrequency(value: Float): Float =
    AudioEffects.MIN_FREQUENCY * (AudioEffects.MAX_FREQUENCY / AudioEffects.MIN_FREQUENCY).pow(value)

private fun dbLabel(value: Float): String = String.format(Locale.US, "%+.1f dB", value)

@Composable
private fun filterTypeLabel(type: String): String = stringResource(
    when (type) {
        AudioEffects.FILTER_LOW_SHELF -> R.string.filter_low_shelf
        AudioEffects.FILTER_HIGH_SHELF -> R.string.filter_high_shelf
        AudioEffects.FILTER_LOW_PASS -> R.string.filter_low_pass
        AudioEffects.FILTER_HIGH_PASS -> R.string.filter_high_pass
        AudioEffects.FILTER_NOTCH -> R.string.filter_notch
        else -> R.string.filter_peak
    }
)

@Composable
fun ParametricEqScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { AudioEffects.ensureLoaded(context) }
    var editing by remember { mutableStateOf<Int?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    val importedMessage = stringResource(R.string.parametric_imported)
    val importFailed = stringResource(R.string.parametric_import_failed)
    val exportedMessage = stringResource(R.string.parametric_exported)
    val exportFailed = stringResource(R.string.style_export_failed)
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            Toast.makeText(context, if (AudioEffects.importAutoEq(context, uri)) importedMessage else importFailed, Toast.LENGTH_SHORT).show()
        }
    }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            Toast.makeText(context, if (AudioEffects.exportAutoEq(context, uri)) exportedMessage else exportFailed, Toast.LENGTH_SHORT).show()
        }
    }
    val filters = AudioEffects.filters
    LookAndFeelPage(
        title = stringResource(R.string.parametric_title),
        onBack = onBack,
        onReset = { confirmReset = true },
        modifier = modifier,
        overlay = {
            ResetDefaultsDialog(
                visible = confirmReset,
                message = stringResource(R.string.parametric_reset_message),
                onConfirm = {
                    confirmReset = false
                    AudioEffects.setFilters(context, emptyList())
                    AudioEffects.setParametricPreamp(context, 0f)
                    AudioEffects.setParametricEnabled(context, false)
                },
                onDismiss = { confirmReset = false }
            )
        }
    ) {
        SettingsCard {
            ParametricCurve(filters = filters, preampDb = AudioEffects.parametricPreampDb, enabled = AudioEffects.parametricEnabled)
        }
        Spacer(modifier = Modifier.height(20.dp))
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.GraphicEq,
                title = stringResource(R.string.parametric_enable),
                subtitle = stringResource(R.string.parametric_enable_subtitle),
                checked = AudioEffects.parametricEnabled,
                colors = colors,
                onCheckedChange = { AudioEffects.setParametricEnabled(context, it) }
            )
            RowDivider(colors.divider)
            FineTuneSlider(
                title = stringResource(R.string.eq_preamp),
                value = AudioEffects.parametricPreampDb,
                range = AudioEffects.MIN_PREAMP..AudioEffects.MAX_PREAMP,
                format = ::dbLabel,
                onCommit = { AudioEffects.setParametricPreamp(context, (it * 10).toInt() / 10f) }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.parametric_filters), colors.primary)
        SettingsCard {
            filters.forEachIndexed { index, filter ->
                if (index > 0) RowDivider(colors.divider)
                SettingsRow(
                    icon = Icons.Outlined.Tune,
                    title = "${index + 1}. ${filterTypeLabel(filter.type)} · ${hzLabel(filter.frequency)}",
                    subtitle = if (filter.enabled) {
                        "${dbLabel(filter.gainDb)} · Q ${String.format(Locale.US, "%.2f", filter.q)}"
                    } else {
                        stringResource(R.string.effect_off)
                    },
                    colors = colors,
                    onClick = { editing = index }
                )
            }
            if (filters.isNotEmpty()) RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.Add,
                title = stringResource(R.string.parametric_add),
                subtitle = stringResource(R.string.parametric_add_subtitle, AudioEffects.MAX_FILTERS),
                colors = colors,
                onClick = {
                    if (filters.size < AudioEffects.MAX_FILTERS) {
                        AudioEffects.setFilters(context, filters + ParametricFilter())
                        AudioEffects.setParametricEnabled(context, true)
                        editing = filters.size
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        SectionHeader(stringResource(R.string.parametric_autoeq), colors.primary)
        SettingsCard {
            SettingsRow(
                icon = Icons.Outlined.FileDownload,
                title = stringResource(R.string.parametric_import),
                subtitle = stringResource(R.string.parametric_import_subtitle),
                colors = colors,
                onClick = { importer.launch(arrayOf("text/*", "application/octet-stream")) }
            )
            RowDivider(colors.divider)
            SettingsRow(
                icon = Icons.Outlined.FileUpload,
                title = stringResource(R.string.parametric_export),
                subtitle = stringResource(R.string.parametric_export_subtitle),
                colors = colors,
                onClick = { exporter.launch("ParametricEQ.txt") }
            )
            Text(
                text = stringResource(R.string.parametric_autoeq_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)
            )
        }
    }

    val index = editing
    val filter = index?.let { filters.getOrNull(it) }
    FilterEditDialog(
        filter = filter,
        onChange = { updated ->
            if (index != null) AudioEffects.setFilters(context, filters.toMutableList().also { it[index] = updated })
        },
        onDelete = {
            if (index != null) AudioEffects.setFilters(context, filters.filterIndexed { position, _ -> position != index })
            editing = null
        },
        onDismiss = { editing = null }
    )
}

@Composable
private fun ParametricCurve(filters: List<ParametricFilter>, preampDb: Float, enabled: Boolean) {
    val colors = LocalClintColors.current
    val points = remember(filters, preampDb) {
        List(96) { step ->
            val frequency = sliderToFrequency(step / 95f).toDouble()
            BiquadMath.responseDb(filters, preampDb, frequency).toFloat()
        }
    }
    val line = if (enabled) colors.primary else colors.secondaryText
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
    ) {
        val mid = size.height / 2f
        drawLine(colors.divider, Offset(0f, mid), Offset(size.width, mid), strokeWidth = 1.dp.toPx())
        listOf(100f, 1_000f, 10_000f).forEach { hz ->
            val x = frequencyToSlider(hz) * size.width
            drawLine(colors.divider.copy(alpha = 0.5f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
        }
        val path = Path()
        points.forEachIndexed { step, db ->
            val x = step / 95f * size.width
            val y = mid - (db / CURVE_RANGE_DB).coerceIn(-1f, 1f) * mid
            if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, line, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun FilterEditDialog(
    filter: ParametricFilter?,
    onChange: (ParametricFilter) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalClintColors.current
    KateWindowDialog(visible = filter != null, onDismiss = onDismiss) {
        if (filter == null) return@KateWindowDialog
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.parametric_edit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                AudioEffects.FilterTypes.forEach { type ->
                    val selected = type == filter.type
                    Text(
                        text = filterTypeLabel(type),
                        style = MaterialTheme.typography.labelLarge,
                        color = kateChoiceText(selected),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier
                            .kateChoice(selected, chip = true)
                            .clickable { onChange(filter.copy(type = type)) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
            FineTuneSlider(
                title = stringResource(R.string.parametric_frequency),
                value = frequencyToSlider(filter.frequency),
                range = 0f..1f,
                format = { hzLabel(sliderToFrequency(it)) },
                onCommit = { onChange(filter.copy(frequency = sliderToFrequency(it).let { hz -> if (hz >= 1_000f) (hz / 10f).toInt() * 10f else hz.toInt().toFloat() })) }
            )
            val usesGain = filter.type == AudioEffects.FILTER_PEAK || filter.type == AudioEffects.FILTER_LOW_SHELF || filter.type == AudioEffects.FILTER_HIGH_SHELF
            if (usesGain) {
                FineTuneSlider(
                    title = stringResource(R.string.parametric_gain),
                    value = filter.gainDb,
                    range = AudioEffects.MIN_FILTER_GAIN..AudioEffects.MAX_FILTER_GAIN,
                    format = ::dbLabel,
                    onCommit = { onChange(filter.copy(gainDb = (it * 10).toInt() / 10f)) }
                )
            }
            FineTuneSlider(
                title = stringResource(R.string.parametric_q),
                value = filter.q,
                range = AudioEffects.MIN_Q..AudioEffects.MAX_Q,
                format = { String.format(Locale.US, "%.2f", it) },
                onCommit = { onChange(filter.copy(q = (it * 100).toInt() / 100f)) }
            )
            SettingsSwitchRow(
                icon = Icons.Outlined.GraphicEq,
                title = stringResource(R.string.parametric_filter_on),
                subtitle = stringResource(R.string.parametric_filter_on_subtitle),
                checked = filter.enabled,
                colors = colors,
                onCheckedChange = { onChange(filter.copy(enabled = it)) }
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 6.dp)) {
                KateTextButton(label = stringResource(R.string.action_delete), role = DialogButtonRole.DESTRUCTIVE, onClick = onDelete)
                KateTextButton(label = stringResource(R.string.audio_done), role = DialogButtonRole.MAIN, onClick = onDismiss)
            }
        }
    }
}
