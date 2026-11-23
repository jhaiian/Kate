package com.jhaiian.kate.ui.player

import com.jhaiian.kate.ui.components.kateChoiceTint
import com.jhaiian.kate.ui.components.kateChoiceText
import com.jhaiian.kate.ui.components.kateChoice
import com.jhaiian.kate.ui.components.KateIcon
import com.jhaiian.kate.ui.components.KateActionButton
import com.jhaiian.kate.ui.components.buttonTint
import com.jhaiian.kate.ui.components.KateIconButton
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LibraryTrackCache
import com.jhaiian.kate.data.Lyrics
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.ui.components.LocalKateHaptics
import com.jhaiian.kate.ui.stats.WrappedExport
import com.jhaiian.kate.ui.theme.LocalClintColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MaxCardLines = 6
private const val CardWidth = 1080
private const val CardHeight = 1350
private const val BlurScale = 3
private const val BlurRadius = 4
private const val BlurPasses = 3

internal enum class LyricCardStyle { ARTWORK, ACCENT, DARK }

internal object LyricsCardRenderer {
    fun render(
        context: Context,
        track: Track,
        lines: List<String>,
        style: LyricCardStyle,
        accent: Int,
        footer: String
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CardWidth, CardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val art = loadArt(context, track)
        val dark = 0xFF141416.toInt()
        val accentIsLight = Color(accent).luminance() > 0.6f
        var lightText = true
        when {
            style == LyricCardStyle.ARTWORK && art != null -> {
                drawBlurredArt(canvas, art)
                val scrim = Paint(Paint.DITHER_FLAG).apply {
                    shader = LinearGradient(
                        0f, 0f, 0f, CardHeight.toFloat(),
                        intArrayOf(0x59000000, 0x8C000000.toInt(), 0xBF000000.toInt()),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, CardWidth.toFloat(), CardHeight.toFloat(), scrim)
            }
            style == LyricCardStyle.DARK -> {
                canvas.drawColor(dark)
                val glow = Paint().apply {
                    shader = RadialGradient(0f, 0f, CardWidth * 1.1f, withAlpha(accent, 0x66), 0x00000000, Shader.TileMode.CLAMP)
                }
                canvas.drawRect(0f, 0f, CardWidth.toFloat(), CardHeight.toFloat(), glow)
            }
            else -> {
                val paint = Paint().apply {
                    shader = LinearGradient(
                        0f, 0f, CardWidth.toFloat(), CardHeight.toFloat(),
                        accent, darken(accent, 0.45f), Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, CardWidth.toFloat(), CardHeight.toFloat(), paint)
                lightText = !accentIsLight
            }
        }
        val textColor = if (lightText) 0xFFFFFFFF.toInt() else 0xFF1A1A1A.toInt()
        val softText = withAlpha(textColor, 0xBB)
        val pad = 90f
        val artSize = 200f
        var textLeft = pad
        if (art != null) {
            val dst = RectF(pad, pad, pad + artSize, pad + artSize)
            val side = minOf(art.width, art.height)
            val src = Rect((art.width - side) / 2, (art.height - side) / 2, (art.width + side) / 2, (art.height + side) / 2)
            canvas.save()
            canvas.clipPath(Path().apply { addRoundRect(dst, 28f, 28f, Path.Direction.CW) })
            canvas.drawBitmap(art, src, dst, Paint(Paint.FILTER_BITMAP_FLAG))
            canvas.restore()
            textLeft = pad + artSize + 40f
        }
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val artistPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = softText
            textSize = 36f
        }
        val headerWidth = CardWidth - pad - textLeft
        val title = TextUtils.ellipsize(track.title, titlePaint, headerWidth, TextUtils.TruncateAt.END).toString()
        val artist = TextUtils.ellipsize(track.artist, artistPaint, headerWidth, TextUtils.TruncateAt.END).toString()
        val headerCenter = pad + artSize / 2f
        canvas.drawText(title, textLeft, headerCenter - 6f, titlePaint)
        canvas.drawText(artist, textLeft, headerCenter + 44f, artistPaint)

        val quotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (style == LyricCardStyle.ACCENT) withAlpha(textColor, 0x55) else withAlpha(accent, 0xCC)
            textSize = 220f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText("“", pad - 12f, 520f, quotePaint)

        val lyricsTop = 500f
        val lyricsBottom = CardHeight - 200f
        val available = lyricsBottom - lyricsTop
        val lyricPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val text = lines.joinToString("\n")
        val width = (CardWidth - pad * 2).toInt()
        var size = 76f
        var layout: StaticLayout
        while (true) {
            lyricPaint.textSize = size
            layout = StaticLayout.Builder.obtain(text, 0, text.length, lyricPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.18f)
                .setIncludePad(false)
                .build()
            if (layout.height <= available || size <= 34f) break
            size -= 4f
        }
        canvas.save()
        canvas.translate(pad, lyricsTop + ((available - layout.height) / 2f).coerceAtLeast(0f))
        layout.draw(canvas)
        canvas.restore()

        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (style == LyricCardStyle.ACCENT) textColor else accent
        }
        canvas.drawRoundRect(RectF(pad, CardHeight - pad - 44f, pad + 56f, CardHeight - pad - 36f), 4f, 4f, barPaint)
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = softText
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(footer, pad, CardHeight - pad, footerPaint)
        return bitmap
    }

    private fun loadArt(context: Context, track: Track): Bitmap? {
        val key = track.artKey ?: return null
        val file = LibraryTrackCache.artFile(context, key)
        if (!file.isFile) return null
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= 512 && bounds.outHeight / (sample * 2) >= 512) sample *= 2
            BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
        }.getOrNull()
    }

    private fun drawBlurredArt(canvas: Canvas, art: Bitmap) {
        val smallWidth = CardWidth / BlurScale
        val smallHeight = CardHeight / BlurScale
        val target = smallWidth.toFloat() / smallHeight
        val source = art.width.toFloat() / art.height
        val crop = if (source > target) {
            val width = (art.height * target).toInt().coerceIn(1, art.width)
            Rect((art.width - width) / 2, 0, (art.width + width) / 2, art.height)
        } else {
            val height = (art.width / target).toInt().coerceIn(1, art.height)
            Rect(0, (art.height - height) / 2, art.width, (art.height + height) / 2)
        }
        val small = Bitmap.createBitmap(smallWidth, smallHeight, Bitmap.Config.ARGB_8888)
        Canvas(small).apply {
            drawColor(0xFF000000.toInt())
            drawBitmap(art, crop, Rect(0, 0, smallWidth, smallHeight), Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
        }
        val pixels = IntArray(smallWidth * smallHeight)
        val buffer = IntArray(smallWidth * smallHeight)
        small.getPixels(pixels, 0, smallWidth, 0, 0, smallWidth, smallHeight)
        repeat(BlurPasses) {
            blurPass(pixels, buffer, smallWidth, smallHeight, horizontal = true)
            blurPass(buffer, pixels, smallWidth, smallHeight, horizontal = false)
        }
        small.setPixels(pixels, 0, smallWidth, 0, 0, smallWidth, smallHeight)
        canvas.drawBitmap(
            small,
            null,
            Rect(0, 0, CardWidth, CardHeight),
            Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        )
        small.recycle()
    }

    private fun blurPass(input: IntArray, output: IntArray, width: Int, height: Int, horizontal: Boolean) {
        val lines = if (horizontal) height else width
        val length = if (horizontal) width else height
        val window = BlurRadius * 2 + 1
        val half = window / 2
        for (line in 0 until lines) {
            var red = 0
            var green = 0
            var blue = 0
            for (i in -BlurRadius..BlurRadius) {
                val pixel = input[offset(line, i, length, width, horizontal)]
                red += (pixel shr 16) and 0xFF
                green += (pixel shr 8) and 0xFF
                blue += pixel and 0xFF
            }
            for (i in 0 until length) {
                output[offset(line, i, length, width, horizontal)] = (0xFF shl 24) or
                    (((red + half) / window) shl 16) or
                    (((green + half) / window) shl 8) or
                    ((blue + half) / window)
                val incoming = input[offset(line, i + BlurRadius + 1, length, width, horizontal)]
                val outgoing = input[offset(line, i - BlurRadius, length, width, horizontal)]
                red += ((incoming shr 16) and 0xFF) - ((outgoing shr 16) and 0xFF)
                green += ((incoming shr 8) and 0xFF) - ((outgoing shr 8) and 0xFF)
                blue += (incoming and 0xFF) - (outgoing and 0xFF)
            }
        }
    }

    private fun offset(line: Int, position: Int, length: Int, width: Int, horizontal: Boolean): Int {
        val clamped = position.coerceIn(0, length - 1)
        return if (horizontal) line * width + clamped else clamped * width + line
    }

    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)

    private fun darken(color: Int, amount: Float): Int {
        val r = ((color shr 16) and 0xFF) * (1f - amount)
        val g = ((color shr 8) and 0xFF) * (1f - amount)
        val b = (color and 0xFF) * (1f - amount)
        return (0xFF shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
    }
}

@Composable
internal fun LyricsShareSheet(
    visible: Boolean,
    track: Track,
    lyrics: Lyrics?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible && lyrics != null,
        enter = fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 8 },
        exit = fadeOut(tween(180)) + slideOutVertically(tween(200)) { it / 10 }
    ) {
        if (lyrics != null) LyricsShareContent(track = track, lyrics = lyrics, onDismiss = onDismiss)
    }
}

@Composable
private fun LyricsShareContent(track: Track, lyrics: Lyrics, onDismiss: () -> Unit) {
    val colors = LocalClintColors.current
    val context = LocalContext.current
    val haptics = LocalKateHaptics.current
    val scope = rememberCoroutineScope()
    BackHandler(onBack = onDismiss)
    val lines = remember(lyrics) {
        lyrics.lines.map { it.text.trim() }.filter { it.isNotBlank() }
    }
    var selected by remember(lyrics) { mutableStateOf(if (lines.isEmpty()) emptyList() else listOf(0)) }
    var style by remember { mutableStateOf(if (track.artKey != null) LyricCardStyle.ARTWORK else LyricCardStyle.ACCENT) }
    var busy by remember { mutableStateOf(false) }
    val accent = colors.primary.toArgb()
    val footer = stringResource(R.string.lyrics_card_footer)
    val chosen = selected.sorted().mapNotNull { lines.getOrNull(it) }
    var preview by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(chosen, style, accent) {
        if (chosen.isEmpty()) {
            preview = null
            return@LaunchedEffect
        }
        preview = withContext(Dispatchers.Default) {
            runCatching { LyricsCardRenderer.render(context, track, chosen, style, accent, footer) }.getOrNull()
        }
    }
    val fileName = "Kate-lyrics-${System.currentTimeMillis()}.png"
    val savedMessage = stringResource(R.string.lyrics_card_saved)
    val failedMessage = stringResource(R.string.lyrics_card_failed)
    val permissionMessage = stringResource(R.string.lyrics_card_permission)
    val shareTitle = stringResource(R.string.lyrics_share)
    val limitMessage = pluralStringResource(R.plurals.lyrics_card_limit, MaxCardLines, MaxCardLines)
    val save: () -> Unit = {
        val bitmap = preview
        if (bitmap != null && !busy) {
            busy = true
            scope.launch {
                val ok = WrappedExport.save(context, bitmap, fileName)
                busy = false
                if (ok) haptics.confirm()
                Toast.makeText(context, if (ok) savedMessage else failedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) save() else Toast.makeText(context, permissionMessage, Toast.LENGTH_SHORT).show()
    }
    val onSave: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        if (WrappedExport.needsStoragePermission && !granted) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            save()
        }
    }
    val onShare: () -> Unit = {
        val bitmap = preview
        if (bitmap != null && !busy) {
            busy = true
            scope.launch {
                val ok = WrappedExport.share(context, bitmap, fileName, shareTitle)
                busy = false
                if (!ok) Toast.makeText(context, failedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val toggle: (Int) -> Unit = { index ->
        selected = when {
            index in selected -> selected - index
            selected.size >= MaxCardLines -> {
                Toast.makeText(context, limitMessage, Toast.LENGTH_SHORT).show()
                selected
            }
            else -> selected + index
        }
        haptics.tick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) { detectTapGestures { } }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp)
            ) {
                KateIconButton(onClick = onDismiss) {
                    KateIcon(imageVector = Icons.Outlined.Close, contentDescription = stringResource(R.string.folder_picker_close), tint = buttonTint(colors.onSurface))
                }
                Column(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                    Text(
                        text = stringResource(R.string.lyrics_card_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Text(
                        text = pluralStringResource(R.plurals.lyrics_card_hint, MaxCardLines, MaxCardLines),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val wide = maxWidth > maxHeight && maxWidth >= 600.dp
                val previewHeight = maxHeight * 0.42f
                if (wide) {
                    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            CardPreview(preview, Modifier.weight(1f))
                            StylePicker(style, track.artKey != null) { style = it }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        LineList(lines, selected, toggle, Modifier.weight(1f).fillMaxHeight())
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CardPreview(
                            preview,
                            Modifier
                                .fillMaxWidth()
                                .height(previewHeight)
                                .padding(top = 8.dp)
                        )
                        StylePicker(style, track.artKey != null) { style = it }
                        LineList(lines, selected, toggle, Modifier.weight(1f).fillMaxWidth())
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                val enabled = preview != null && !busy
                KateActionButton(
                    label = stringResource(R.string.lyrics_card_save),
                    icon = Icons.Outlined.FileDownload,
                    primary = false,
                    enabled = enabled,
                    minHeight = 50.dp,
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                )
                KateActionButton(
                    label = stringResource(R.string.lyrics_card_share),
                    icon = Icons.Outlined.Share,
                    enabled = enabled,
                    minHeight = 50.dp,
                    onClick = onShare,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CardPreview(bitmap: Bitmap?, modifier: Modifier) {
    val colors = LocalClintColors.current
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(CardWidth.toFloat() / CardHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceVariant)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.lyrics_card_title),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = stringResource(R.string.lyrics_card_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StylePicker(selected: LyricCardStyle, hasArt: Boolean, onSelect: (LyricCardStyle) -> Unit) {
    val colors = LocalClintColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        LyricCardStyle.entries.filter { hasArt || it != LyricCardStyle.ARTWORK }.forEach { option ->
            val isSelected = option == selected
            Text(
                text = stringResource(
                    when (option) {
                        LyricCardStyle.ARTWORK -> R.string.lyrics_card_style_artwork
                        LyricCardStyle.ACCENT -> R.string.lyrics_card_style_accent
                        LyricCardStyle.DARK -> R.string.lyrics_card_style_dark
                    }
                ),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = kateChoiceText(isSelected),
                modifier = Modifier
                    .kateChoice(isSelected)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun LineList(lines: List<String>, selected: List<Int>, onToggle: (Int) -> Unit, modifier: Modifier) {
    val colors = LocalClintColors.current
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        itemsIndexed(lines) { index, line ->
            val isSelected = index in selected
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) colors.primary.copy(alpha = 0.14f) else Color.Transparent)
                    .clickable { onToggle(index) }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) colors.primary else Color.Transparent)
                        .border(BorderStroke(1.5.dp, if (isSelected) colors.primary else colors.secondaryText), CircleShape)
                ) {
                    if (isSelected) {
                        KateIcon(imageVector = Icons.Filled.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(14.dp))
                    }
                }
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) colors.onSurface else colors.secondaryText,
                    modifier = Modifier.weight(1f).padding(start = 12.dp)
                )
            }
        }
    }
}
