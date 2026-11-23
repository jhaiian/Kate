package com.jhaiian.kate.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jhaiian.kate.ui.theme.PlayerStylePreferences
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal data class DayNight(val day: Color, val night: Color) {
    fun resolve(night: Boolean): Color = if (night) this.night else day
    fun withAlpha(alpha: Float): DayNight = DayNight(day.copy(alpha = alpha), night.copy(alpha = alpha))

    companion object {
        fun same(color: Color) = DayNight(color, color)
    }
}

internal class WidgetPalette(
    val background: DayNight,
    val content: DayNight,
    val secondary: DayNight,
    val accent: DayNight,
    val onAccent: DayNight,
    val track: DayNight,
    val placeholder: DayNight
)

internal fun Context.isNightMode(): Boolean =
    (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

private fun Context.systemColor(id: Int, fallback: Long): Color =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Color(getColor(id)) else Color(fallback)

private data class SystemTones(
    val background: DayNight,
    val content: DayNight,
    val secondary: DayNight,
    val accent: DayNight,
    val onAccent: DayNight,
    val track: DayNight
)

private fun Context.systemTones(): SystemTones {
    val s = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    fun tone(id: Int, fallback: Long) = if (s) systemColor(id, fallback) else Color(fallback)
    return SystemTones(
        background = DayNight(tone(android.R.color.system_neutral1_50, 0xFFF7F2FA), tone(android.R.color.system_neutral1_900, 0xFF1D1B20)),
        content = DayNight(tone(android.R.color.system_neutral1_900, 0xFF1D1B20), tone(android.R.color.system_neutral1_50, 0xFFF4EFF4)),
        secondary = DayNight(tone(android.R.color.system_neutral2_700, 0xFF49454F), tone(android.R.color.system_neutral2_200, 0xFFCAC4D0)),
        accent = DayNight(tone(android.R.color.system_accent1_600, 0xFF6750A4), tone(android.R.color.system_accent1_200, 0xFFD0BCFF)),
        onAccent = DayNight(tone(android.R.color.system_accent1_0, 0xFFFFFFFF), tone(android.R.color.system_accent1_800, 0xFF381E72)),
        track = DayNight(tone(android.R.color.system_neutral2_200, 0xFFE7E0EC), tone(android.R.color.system_neutral2_700, 0xFF49454F))
    )
}

internal fun widgetPalette(context: Context, settings: WidgetSettings, artSeed: Int?): WidgetPalette {
    val alpha = settings.opacity / 100f
    val tones = context.systemTones()
    return when (settings.background) {
        WidgetSettings.BACKGROUND_DARK -> WidgetPalette(
            background = DayNight.same(Color(0xFF161618)).withAlpha(alpha),
            content = DayNight.same(Color.White),
            secondary = DayNight.same(Color.White.copy(alpha = 0.7f)),
            accent = DayNight.same(tones.accent.night),
            onAccent = DayNight.same(tones.onAccent.night),
            track = DayNight.same(Color.White.copy(alpha = 0.2f)),
            placeholder = DayNight.same(Color.White.copy(alpha = 0.12f))
        )
        WidgetSettings.BACKGROUND_LIGHT -> WidgetPalette(
            background = DayNight.same(Color(0xFFFAFAFC)).withAlpha(alpha),
            content = DayNight.same(Color(0xFF1B1B1F)),
            secondary = DayNight.same(Color(0xFF5E5E66)),
            accent = DayNight.same(tones.accent.day),
            onAccent = DayNight.same(tones.onAccent.day),
            track = DayNight.same(Color.Black.copy(alpha = 0.12f)),
            placeholder = DayNight.same(Color.Black.copy(alpha = 0.08f))
        )
        WidgetSettings.BACKGROUND_ARTWORK -> {
            val seed = artSeed?.let { Color(it) } ?: tones.accent.night
            WidgetPalette(
                background = DayNight.same(deepen(seed)).withAlpha(alpha),
                content = DayNight.same(Color.White),
                secondary = DayNight.same(Color.White.copy(alpha = 0.72f)),
                accent = DayNight.same(brighten(seed)),
                onAccent = DayNight.same(deepen(seed)),
                track = DayNight.same(Color.White.copy(alpha = 0.22f)),
                placeholder = DayNight.same(Color.White.copy(alpha = 0.14f))
            )
        }
        else -> WidgetPalette(
            background = tones.background.withAlpha(alpha),
            content = tones.content,
            secondary = tones.secondary,
            accent = tones.accent,
            onAccent = tones.onAccent,
            track = tones.track,
            placeholder = DayNight(tones.track.day.copy(alpha = 0.7f), tones.track.night.copy(alpha = 0.7f))
        )
    }
}

private fun hsv(color: Color): FloatArray {
    val values = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), values)
    return values
}

private fun deepen(color: Color): Color {
    val values = hsv(color)
    values[1] = min(1f, values[1] * 1.1f + 0.08f)
    values[2] = min(values[2], 0.32f).coerceAtLeast(0.16f)
    return Color(android.graphics.Color.HSVToColor(values))
}

private fun brighten(color: Color): Color {
    val values = hsv(color)
    values[1] = min(values[1], 0.55f)
    values[2] = max(values[2], 0.92f)
    return Color(android.graphics.Color.HSVToColor(values))
}

internal object WidgetArt {
    private val cache = LruCache<String, Bitmap>(16)
    private val seeds = LruCache<String, Int>(32)

    private fun decode(path: String, targetPx: Int): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= targetPx && bounds.outHeight / (sample * 2) >= targetPx) sample *= 2
        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }.getOrNull()

    private fun centerCrop(source: Bitmap, width: Int, height: Int): Bitmap {
        val scale = max(width.toFloat() / source.width, height.toFloat() / source.height)
        val scaledWidth = (source.width * scale).roundToInt().coerceAtLeast(width)
        val scaledHeight = (source.height * scale).roundToInt().coerceAtLeast(height)
        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        val x = ((scaledWidth - width) / 2).coerceAtLeast(0)
        val y = ((scaledHeight - height) / 2).coerceAtLeast(0)
        return Bitmap.createBitmap(scaled, x, y, width, height)
    }

    private fun mask(source: Bitmap, radiusPx: Float, circle: Boolean): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP) }
        val rect = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())
        if (circle) {
            canvas.drawOval(rect, paint)
        } else {
            canvas.drawRoundRect(rect, radiusPx, radiusPx, paint)
        }
        return output
    }

    fun square(path: String?, sizePx: Int, shape: String): Bitmap? {
        if (path == null || sizePx <= 0) return null
        val key = "square:$path:$sizePx:$shape"
        cache.get(key)?.let { return it }
        val source = decode(path, sizePx) ?: return null
        val cropped = centerCrop(source, sizePx, sizePx)
        val result = when (shape) {
            PlayerStylePreferences.ART_SHAPE_CIRCLE -> mask(cropped, 0f, circle = true)
            PlayerStylePreferences.ART_SHAPE_SQUARE -> mask(cropped, sizePx * 0.04f, circle = false)
            else -> mask(cropped, sizePx * 0.18f, circle = false)
        }
        cache.put(key, result)
        return result
    }

    fun cover(path: String?, widthPx: Int, heightPx: Int, radiusPx: Float): Bitmap? {
        if (path == null || widthPx <= 0 || heightPx <= 0) return null
        val key = "cover:$path:$widthPx:$heightPx:$radiusPx"
        cache.get(key)?.let { return it }
        val source = decode(path, max(widthPx, heightPx)) ?: return null
        val result = mask(centerCrop(source, widthPx, heightPx), radiusPx, circle = false)
        cache.put(key, result)
        return result
    }

    fun seed(path: String?): Int? {
        if (path == null) return null
        seeds.get(path)?.let { return it }
        val source = decode(path, 64) ?: return null
        val small = Bitmap.createScaledBitmap(source, 12, 12, true)
        var best = 0
        var bestScore = -1f
        var red = 0L
        var green = 0L
        var blue = 0L
        val values = FloatArray(3)
        for (x in 0 until small.width) {
            for (y in 0 until small.height) {
                val pixel = small.getPixel(x, y)
                red += android.graphics.Color.red(pixel)
                green += android.graphics.Color.green(pixel)
                blue += android.graphics.Color.blue(pixel)
                android.graphics.Color.colorToHSV(pixel, values)
                val score = values[1] * (0.35f + values[2])
                if (score > bestScore) {
                    bestScore = score
                    best = pixel
                }
            }
        }
        val count = small.width * small.height
        val average = android.graphics.Color.rgb((red / count).toInt(), (green / count).toInt(), (blue / count).toInt())
        val result = if (bestScore > 0.25f) best else average
        seeds.put(path, result)
        return result
    }

    fun roundedRect(color: Int, widthPx: Int, heightPx: Int, radiusPx: Float): Bitmap {
        val key = "rect:$color:$widthPx:$heightPx:$radiusPx"
        cache.get(key)?.let { return it }
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        Canvas(bitmap).drawRoundRect(RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()), radiusPx, radiusPx, paint)
        cache.put(key, bitmap)
        return bitmap
    }

    fun circle(color: Int, sizePx: Int): Bitmap {
        val key = "circle:$color:$sizePx"
        cache.get(key)?.let { return it }
        val bitmap = Bitmap.createBitmap(sizePx.coerceAtLeast(1), sizePx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        Canvas(bitmap).drawOval(RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()), paint)
        cache.put(key, bitmap)
        return bitmap
    }

    fun scrim(widthPx: Int, heightPx: Int, radiusPx: Float): Bitmap {
        val key = "scrim:$widthPx:$heightPx:$radiusPx"
        cache.get(key)?.let { return it }
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, bitmap.height.toFloat(),
                intArrayOf(0x00000000, 0x66000000, 0xD9000000.toInt()),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        Canvas(bitmap).drawRoundRect(RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()), radiusPx, radiusPx, paint)
        cache.put(key, bitmap)
        return bitmap
    }
}
