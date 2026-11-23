package com.jhaiian.kate.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.jhaiian.kate.playback.PlayerController
import com.materialkolor.quantize.QuantizerCelebi
import com.materialkolor.score.Score
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

data class ArtworkSeed(val argb: Int, val monochrome: Boolean)

object ArtworkColors {
    private const val PREFS_NAME = "artwork_colors"
    private const val KEY_CURRENT = "current"
    private const val MONOCHROME_FLAG = "m"
    private const val COLOR_FLAG = "c"
    private const val FIELD_SEPARATOR = ':'
    private const val SAMPLE_SIZE_PX = 128
    private const val MAX_COLORS = 128

    var seed by mutableStateOf<ArtworkSeed?>(null)
        private set

    private var started = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun start(context: Context) {
        if (started) return
        started = true
        val appContext = context.applicationContext
        val prefs = prefs(appContext)
        seed = decode(prefs.getString(KEY_CURRENT, null))
        scope.launch {
            snapshotFlow { PlayerController.currentTrack }
                .filter { it != null }
                .distinctUntilChanged { old, new -> old?.artKey == new?.artKey }
                .collectLatest { track ->
                    val artKey = track?.artKey
                    val next = if (artKey == null) null else withContext(Dispatchers.Default) { seedFor(appContext, prefs, artKey) }
                    seed = next
                    withContext(Dispatchers.IO) { prefs.edit().putString(KEY_CURRENT, encode(next)).apply() }
                }
        }
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun seedFor(context: Context, prefs: SharedPreferences, artKey: String): ArtworkSeed? {
        decode(prefs.getString(artKey, null))?.let { return it }
        val extracted = extract(LibraryTrackCache.artFile(context, artKey)) ?: return null
        prefs.edit().putString(artKey, encode(extracted)).apply()
        return extracted
    }

    private fun extract(file: File): ArtworkSeed? = runCatching {
        if (!file.exists()) return@runCatching null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= SAMPLE_SIZE_PX && bounds.outHeight / (sampleSize * 2) >= SAMPLE_SIZE_PX) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeFile(file.path, decodeOptions) ?: return@runCatching null
        val scale = SAMPLE_SIZE_PX.toFloat() / maxOf(decoded.width, decoded.height)
        val bitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).roundToInt().coerceAtLeast(1),
                (decoded.height * scale).roundToInt().coerceAtLeast(1),
                true
            )
        } else {
            decoded
        }
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        if (bitmap !== decoded) bitmap.recycle()
        decoded.recycle()
        val quantized = QuantizerCelebi.quantize(pixels, MAX_COLORS)
        if (quantized.isEmpty()) return@runCatching null
        val best = Score.score(quantized, desired = 1, fallbackColorArgb = null).firstOrNull()
        if (best != null) {
            ArtworkSeed(best, monochrome = false)
        } else {
            ArtworkSeed(quantized.maxBy { it.value }.key, monochrome = true)
        }
    }.getOrNull()

    private fun encode(value: ArtworkSeed?): String? =
        value?.let { "${it.argb}$FIELD_SEPARATOR${if (it.monochrome) MONOCHROME_FLAG else COLOR_FLAG}" }

    private fun decode(raw: String?): ArtworkSeed? {
        if (raw.isNullOrBlank()) return null
        val argb = raw.substringBefore(FIELD_SEPARATOR).toIntOrNull() ?: return null
        return ArtworkSeed(argb, raw.substringAfter(FIELD_SEPARATOR, COLOR_FLAG) == MONOCHROME_FLAG)
    }
}
