package com.jhaiian.kate.ui.settings

import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateDialogActions
import com.jhaiian.kate.ui.components.DialogButtonRole
import com.jhaiian.kate.ui.components.KateTextButton
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jhaiian.kate.R
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.BuiltInEqPresets
import com.jhaiian.kate.data.EqualizerMath
import com.jhaiian.kate.data.EqualizerSettings
import com.jhaiian.kate.ui.components.KateDialog
import com.jhaiian.kate.ui.components.LocalBottomContentPadding
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.library.PlaylistNameDialog
import com.jhaiian.kate.ui.theme.ClintSwitch
import com.jhaiian.kate.ui.theme.LocalClintColors
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

private const val CURVE_POINTS = 96
private const val CURVE_MIN_HZ = 20.0
private const val CURVE_MAX_HZ = 20_000.0
private const val CURVE_RANGE_DB = 15f
private const val GAIN_STEP_DB = 0.5f
private val BandSliderHeight = 220.dp

private data class PresetChip(val key: String, val label: String, val custom: Boolean)

private fun formatGain(gain: Float): String {
    if (abs(gain) < 0.05f) return "0"
    val whole = gain.roundToInt().toFloat() == gain
    return if (whole) String.format(Locale.getDefault(), "%+d", gain.roundToInt())
    else String.format(Locale.getDefault(), "%+.1f", gain)
}

private fun snapGain(value: Float): Float =
    ((value / GAIN_STEP_DB).roundToInt() * GAIN_STEP_DB)
        .coerceIn(EqualizerSettings.MIN_GAIN_DB, EqualizerSettings.MAX_GAIN_DB)

@Composable
fun equalizerStatus(): String {
    val context = LocalContext.current
    LaunchedEffect(Unit) { EqualizerSettings.ensureLoaded(context) }
    if (!EqualizerSettings.enabled) return stringResource(R.string.eq_status_off)
    val key = EqualizerSettings.presetKey
    val name = BuiltInEqPresets.firstOrNull { it.key == key }?.let { stringResource(it.nameRes) }
        ?: EqualizerSettings.customPresets.firstOrNull { it.id == key }?.name
        ?: stringResource(R.string.eq_preset_custom)
    return stringResource(R.string.eq_status_on, name)
}

@Composable
fun EqualizerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onParametricClick: (() -> Unit)? = null
) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    LaunchedEffect(Unit) { EqualizerSettings.ensureLoaded(context) }
    val enabled = EqualizerSettings.enabled
    val gains = EqualizerSettings.gains
    val preamp = EqualizerSettings.preampDb
    val presetKey = EqualizerSettings.presetKey
    val contentAlpha by animateFloatAsState(if (enabled) 1f else 0.5f, label = "eq_content_alpha")
    var savingPreset by remember { mutableStateOf(false) }
    var deletingPreset by remember { mutableStateOf<PresetChip?>(null) }

    val customLabel = stringResource(R.string.eq_preset_custom)
    val builtInLabels = BuiltInEqPresets.map { stringResource(it.nameRes) }
    val chips = buildList {
        if (presetKey == EqualizerSettings.PRESET_CUSTOM) add(PresetChip(EqualizerSettings.PRESET_CUSTOM, customLabel, custom = false))
        BuiltInEqPresets.forEachIndexed { index, preset -> add(PresetChip(preset.key, builtInLabels[index], custom = false)) }
        EqualizerSettings.customPresets.forEach { add(PresetChip(it.id, it.name, custom = true)) }
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
            ) {
                KateIconButton(onClick = onBack) {
                    KateIcon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = buttonTint(colors.onSurface)
                    )
                }
                Text(
                    text = stringResource(R.string.eq_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                ClintSwitch(
                    checked = enabled,
                    onCheckedChange = {
                        haptics.toggle(it)
                        EqualizerSettings.setEnabled(context, it)
                    }
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LocalBottomContentPadding.current + 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item(key = "curve") {
                    ResponseCurve(
                        gains = gains,
                        preampDb = preamp,
                        bassDb = AudioSettings.bassBoostDb,
                        trebleDb = AudioSettings.trebleBoostDb,
                        modifier = Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .alpha(contentAlpha)
                    )
                }
                item(key = "presets") {
                    Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader(stringResource(R.string.eq_section_presets), colors.primary)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(chips, key = { it.key }) { chip ->
                                EqPresetChip(
                                    label = chip.label,
                                    selected = enabled && chip.key == presetKey,
                                    onClick = {
                                        if (chip.key != EqualizerSettings.PRESET_CUSTOM) {
                                            haptics.click()
                                            EqualizerSettings.applyPreset(context, chip.key)
                                        }
                                    },
                                    onLongClick = if (chip.custom) {
                                        {
                                            haptics.longPress()
                                            deletingPreset = chip
                                        }
                                    } else {
                                        null
                                    },
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }
                    }
                }
                item(key = "bands") {
                    Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader(stringResource(R.string.eq_section_bands), colors.primary)
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.cardBackground)
                                .padding(horizontal = 6.dp, vertical = 14.dp)
                                .alpha(contentAlpha)
                        ) {
                            EqualizerSettings.BandFrequencies.forEachIndexed { band, frequency ->
                                val shortLabel = if (frequency >= 1_000) {
                                    stringResource(R.string.eq_band_khz_short, frequency / 1_000)
                                } else {
                                    frequency.toString()
                                }
                                val spokenLabel = if (frequency >= 1_000) {
                                    stringResource(R.string.eq_band_khz, frequency / 1_000)
                                } else {
                                    stringResource(R.string.eq_band_hz, frequency)
                                }
                                BandSlider(
                                    gain = gains.getOrElse(band) { 0f },
                                    label = shortLabel,
                                    spokenLabel = spokenLabel,
                                    onChange = { EqualizerSettings.setBandGain(context, band, it) },
                                    onReset = {
                                        haptics.click()
                                        EqualizerSettings.setBandGain(context, band, 0f)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                item(key = "preamp") {
                    Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        PreampCard(
                            preampDb = preamp,
                            onChange = { EqualizerSettings.setPreamp(context, it) },
                            modifier = Modifier.alpha(contentAlpha)
                        )
                    }
                }
                item(key = "boost") {
                    Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader(stringResource(R.string.eq_section_boost), colors.primary)
                        Box(modifier = Modifier.alpha(contentAlpha)) { BoostCard() }
                    }
                }
                item(key = "actions") {
                    Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            EqActionButton(
                                icon = Icons.Outlined.BookmarkAdd,
                                label = stringResource(R.string.eq_save_preset),
                                onClick = {
                                    haptics.click()
                                    savingPreset = true
                                },
                                modifier = Modifier.weight(1f)
                            )
                            EqActionButton(
                                icon = Icons.Outlined.RestartAlt,
                                label = stringResource(R.string.eq_reset),
                                onClick = {
                                    haptics.confirm()
                                    EqualizerSettings.reset(context)
                                    AudioSettings.resetBoost(context)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text(
                            text = stringResource(R.string.eq_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        EqExtras(onParametricClick = onParametricClick)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
        PlaylistNameDialog(
            visible = savingPreset,
            title = stringResource(R.string.eq_save_preset_title),
            initialName = "",
            confirmLabel = stringResource(R.string.eq_save),
            onConfirm = { name ->
                EqualizerSettings.saveCustomPreset(context, name)
                haptics.confirm()
                savingPreset = false
            },
            onDismiss = { savingPreset = false },
            fieldLabel = stringResource(R.string.eq_preset_name)
        )
        DeletePresetDialog(
            name = deletingPreset?.label,
            onConfirm = {
                deletingPreset?.let { EqualizerSettings.deleteCustomPreset(context, it.key) }
                haptics.confirm()
                deletingPreset = null
            },
            onDismiss = { deletingPreset = null }
        )
    }
}

@Composable
private fun ResponseCurve(gains: List<Float>, preampDb: Float, bassDb: Int, trebleDb: Int, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val description = stringResource(R.string.eq_curve_description)
    val response = remember(gains, preampDb, bassDb, trebleDb) {
        val logMin = ln(CURVE_MIN_HZ)
        val logMax = ln(CURVE_MAX_HZ)
        FloatArray(CURVE_POINTS) { index ->
            val fraction = index / (CURVE_POINTS - 1).toDouble()
            val frequency = kotlin.math.exp(logMin + (logMax - logMin) * fraction)
            EqualizerMath.responseDb(gains, preampDb, frequency, bassDb = bassDb, trebleDb = trebleDb).toFloat()
        }
    }
    val bandPositions = remember {
        val logMin = ln(CURVE_MIN_HZ)
        val logMax = ln(CURVE_MAX_HZ)
        EqualizerSettings.BandFrequencies.map { ((ln(it.toDouble()) - logMin) / (logMax - logMin)).toFloat() }
    }
    val bandResponse = remember(gains, preampDb, bassDb, trebleDb) {
        EqualizerSettings.BandFrequencies.map {
            EqualizerMath.responseDb(gains, preampDb, it.toDouble(), bassDb = bassDb, trebleDb = trebleDb).toFloat()
        }
    }
    val path = remember { Path() }
    val fill = remember { Path() }
    Canvas(
        modifier = modifier
            .height(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
            .semantics { contentDescription = description }
    ) {
        val padding = 12.dp.toPx()
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val center = padding + height / 2f
        fun yFor(db: Float): Float = center - (db.coerceIn(-CURVE_RANGE_DB, CURVE_RANGE_DB) / CURVE_RANGE_DB) * (height / 2f)
        listOf(-12f, -6f, 6f, 12f).forEach { db ->
            drawLine(
                color = colors.divider.copy(alpha = 0.5f),
                start = Offset(padding, yFor(db)),
                end = Offset(padding + width, yFor(db)),
                strokeWidth = 1.dp.toPx()
            )
        }
        drawLine(
            color = colors.secondaryText.copy(alpha = 0.5f),
            start = Offset(padding, center),
            end = Offset(padding + width, center),
            strokeWidth = 1.dp.toPx()
        )
        bandPositions.forEach { position ->
            val x = padding + width * position
            drawLine(
                color = colors.divider.copy(alpha = 0.35f),
                start = Offset(x, padding),
                end = Offset(x, padding + height),
                strokeWidth = 1.dp.toPx()
            )
        }
        path.reset()
        fill.reset()
        response.forEachIndexed { index, db ->
            val x = padding + width * index / (CURVE_POINTS - 1)
            val y = yFor(db)
            if (index == 0) {
                path.moveTo(x, y)
                fill.moveTo(x, center)
                fill.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fill.lineTo(x, y)
            }
        }
        fill.lineTo(padding + width, center)
        fill.close()
        drawPath(
            path = fill,
            brush = Brush.verticalGradient(
                listOf(colors.primary.copy(alpha = 0.35f), colors.primary.copy(alpha = 0.05f), colors.primary.copy(alpha = 0.35f)),
                startY = padding,
                endY = padding + height
            )
        )
        drawPath(
            path = path,
            color = colors.primary,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        bandPositions.forEachIndexed { band, position ->
            val x = padding + width * position
            val y = yFor(bandResponse[band])
            drawCircle(color = colors.cardBackground, radius = 5.dp.toPx(), center = Offset(x, y))
            drawCircle(color = colors.primary, radius = 3.5.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
private fun BandSlider(
    gain: Float,
    label: String,
    spokenLabel: String,
    onChange: (Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    val currentGain by rememberUpdatedState(gain)
    val currentOnChange by rememberUpdatedState(onChange)
    val currentOnReset by rememberUpdatedState(onReset)
    val gainText = formatGain(gain)
    val spokenValue = stringResource(R.string.eq_db_value, gainText)
    val range = EqualizerSettings.MAX_GAIN_DB - EqualizerSettings.MIN_GAIN_DB
    fun update(y: Float, height: Int) {
        if (height <= 0) return
        val fraction = 1f - (y / height).coerceIn(0f, 1f)
        val next = snapGain(EqualizerSettings.MIN_GAIN_DB + fraction * range)
        if (next != currentGain) {
            if (next.roundToInt() != currentGain.roundToInt() || next == 0f) haptics.tick()
            currentOnChange(next)
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            text = gainText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (gain == 0f) colors.secondaryText else colors.primary,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(BandSliderHeight)
                .width(40.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { currentOnReset() },
                        onTap = { offset -> update(offset.y, size.height) }
                    )
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { offset -> update(offset.y, size.height) },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            update(change.position.y, size.height)
                        }
                    )
                }
                .semantics(mergeDescendants = true) {
                    contentDescription = spokenLabel
                    stateDescription = spokenValue
                    progressBarRangeInfo = ProgressBarRangeInfo(
                        current = gain,
                        range = EqualizerSettings.MIN_GAIN_DB..EqualizerSettings.MAX_GAIN_DB,
                        steps = (range / GAIN_STEP_DB).toInt() - 1
                    )
                    setProgress { target ->
                        currentOnChange(snapGain(target))
                        true
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackWidth = 4.dp.toPx()
                val thumbRadius = 9.dp.toPx()
                val top = thumbRadius
                val bottom = size.height - thumbRadius
                val usable = bottom - top
                val centerX = size.width / 2f
                fun yFor(db: Float): Float = bottom - ((db - EqualizerSettings.MIN_GAIN_DB) / range) * usable
                val zeroY = yFor(0f)
                val valueY = yFor(gain)
                drawRoundRect(
                    color = colors.surfaceVariant,
                    topLeft = Offset(centerX - trackWidth / 2f, top),
                    size = Size(trackWidth, usable),
                    cornerRadius = CornerRadius(trackWidth / 2f)
                )
                drawLine(
                    color = colors.secondaryText.copy(alpha = 0.6f),
                    start = Offset(centerX - 8.dp.toPx(), zeroY),
                    end = Offset(centerX + 8.dp.toPx(), zeroY),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawRoundRect(
                    color = colors.primary,
                    topLeft = Offset(centerX - trackWidth / 2f, minOf(zeroY, valueY)),
                    size = Size(trackWidth, abs(zeroY - valueY)),
                    cornerRadius = CornerRadius(trackWidth / 2f)
                )
                drawCircle(color = colors.primary, radius = thumbRadius, center = Offset(centerX, valueY))
                drawCircle(color = colors.onPrimary, radius = thumbRadius * 0.4f, center = Offset(centerX, valueY))
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.secondaryText,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PreampCard(preampDb: Float, onChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    val haptics = LocalKateHaptics.current
    Column(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.cardBackground)
            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = stringResource(R.string.eq_preamp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSurface
                )
                Text(
                    text = stringResource(R.string.eq_preamp_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = stringResource(R.string.eq_db_value, formatGain(preampDb)),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
        Slider(
            value = preampDb,
            onValueChange = { value ->
                val snapped = ((value / GAIN_STEP_DB).roundToInt() * GAIN_STEP_DB)
                    .coerceIn(EqualizerSettings.MIN_PREAMP_DB, EqualizerSettings.MAX_PREAMP_DB)
                if (snapped != preampDb) {
                    if (snapped.roundToInt() != preampDb.roundToInt()) haptics.tick()
                    onChange(snapped)
                }
            },
            valueRange = EqualizerSettings.MIN_PREAMP_DB..EqualizerSettings.MAX_PREAMP_DB,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.primary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun EqPresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = LocalClintColors.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .kateChoice(selected, chip = true, unselected = colors.cardBackground)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = kateChoiceText(selected),
            maxLines = 1
        )
    }
}

@Composable
private fun EqActionButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalClintColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        KateIcon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            maxLines = 1,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun DeletePresetDialog(name: String?, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    val lastName = remember { arrayOf("") }
    if (name != null) lastName[0] = name
    KateDialog(visible = name != null, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cardBackground)
                .border(BorderStroke(1.dp, colors.divider.copy(alpha = 0.6f)), RoundedCornerShape(24.dp))
        ) {
            Text(
                text = stringResource(R.string.eq_delete_preset_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.eq_delete_preset_message, lastName[0]),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.secondaryText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            KateDialogActions(modifier = Modifier.fillMaxWidth().padding(end = 12.dp, top = 10.dp, bottom = 10.dp)) {
                KateTextButton(
                    label = stringResource(R.string.action_cancel),
                    role = DialogButtonRole.SECONDARY,
                    onClick = onDismiss
                )
                KateTextButton(
                    label = stringResource(R.string.action_delete),
                    role = DialogButtonRole.DESTRUCTIVE,
                    onClick = onConfirm
                )
            }
        }
    }
}
