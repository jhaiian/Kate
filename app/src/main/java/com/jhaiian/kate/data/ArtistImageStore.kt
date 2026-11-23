package com.jhaiian.kate.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import okhttp3.Request
import java.net.URLEncoder
import java.security.MessageDigest
import java.text.Normalizer

data class ArtistImage(val file: File, val version: Long)

enum class ArtistImageSource { DOWNLOADED, MISSING, CUSTOM, REMOVED }

object ArtistImageStore {
    private const val PREFS_NAME = "artist_images"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_WIFI_ONLY = "wifi_only"
    private const val ENTRY_PREFIX = "artist_"
    private const val FIELD_SEPARATOR = '\u001F'
    private const val IMAGE_DIR = "artist_images"
    private const val SEARCH_URL = "https://api.deezer.com/search/artist?limit=10&q="
    private const val EMPTY_PICTURE_MARKER = "/artist//"
    private const val USER_AGENT = "Kate/1.0 (Android)"
    private const val TIMEOUT_MILLIS = 15_000
    private const val MAX_DOWNLOAD_BYTES = 8 * 1024 * 1024
    private const val REQUEST_GAP_MILLIS = 300L
    private const val MAX_CUSTOM_SIZE = 1200
    private const val JPEG_QUALITY = 90

    private class Entry(val source: ArtistImageSource, val version: Long)

    private class SyncTrigger(
        val enabled: Boolean,
        val wifiOnly: Boolean,
        val network: Int,
        val refresh: Int,
        val index: LibraryIndex
    )

    var enabled by mutableStateOf(false)
        private set
    var wifiOnly by mutableStateOf(true)
        private set
    var images by mutableStateOf<Map<String, ArtistImage>>(emptyMap())
        private set
    private var entries by mutableStateOf<Map<String, Entry>>(emptyMap())
    private var networkVersion by mutableIntStateOf(0)
    private var refreshVersion by mutableIntStateOf(0)
    private var started = false
    private var lastUnmetered: Boolean? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun start(context: Context) {
        if (started) return
        started = true
        val appContext = context.applicationContext
        load(appContext)
        registerNetworkCallback(appContext)
        scope.launch {
            snapshotFlow { SyncTrigger(enabled, wifiOnly, networkVersion, refreshVersion, LibraryStore.index) }
                .collectLatest { trigger ->
                    if (!trigger.enabled) return@collectLatest
                    downloadMissing(appContext, artistsOf(trigger.index))
                }
        }
    }

    fun sourceOf(artistKey: String): ArtistImageSource? = entries[artistKey]?.source

    fun setEnabled(context: Context, value: Boolean) {
        enabled = value
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    fun setWifiOnly(context: Context, value: Boolean) {
        wifiOnly = value
        prefs(context).edit().putBoolean(KEY_WIFI_ONLY, value).apply()
    }

    suspend fun setCustomImage(context: Context, artistKey: String, uri: Uri): Boolean {
        val appContext = context.applicationContext
        val file = fileFor(appContext, artistKey)
        val saved = withContext(Dispatchers.IO) {
            runCatching { importImage(appContext, uri, file) }.getOrDefault(false)
        }
        if (saved) setEntry(appContext, artistKey, ArtistImageSource.CUSTOM)
        return saved
    }

    fun removeImage(context: Context, artistKey: String) {
        val appContext = context.applicationContext
        fileFor(appContext, artistKey).delete()
        setEntry(appContext, artistKey, ArtistImageSource.REMOVED)
    }

    fun restoreImage(context: Context, artistKey: String) {
        val appContext = context.applicationContext
        fileFor(appContext, artistKey).delete()
        clearEntry(appContext, artistKey)
        refreshVersion++
    }

    fun deleteDownloaded(context: Context) {
        val appContext = context.applicationContext
        val downloaded = entries.filterValues {
            it.source == ArtistImageSource.DOWNLOADED || it.source == ArtistImageSource.MISSING
        }.keys
        val editor = prefs(appContext).edit()
        for (key in downloaded) {
            fileFor(appContext, key).delete()
            editor.remove(ENTRY_PREFIX + hash(key))
        }
        editor.apply()
        entries = entries - downloaded
        images = images - downloaded
        refreshVersion++
    }

    private fun artistsOf(index: LibraryIndex): List<Pair<String, String>> =
        (index.artists + index.albumArtists)
            .filter { it.name.isNotBlank() }
            .distinctBy { it.key }
            .map { it.key to it.name }

    private suspend fun downloadMissing(context: Context, artists: List<Pair<String, String>>) {
        for ((key, name) in artists) {
            if (entries.containsKey(key)) continue
            if (!canUseNetwork(context)) return
            val found = withContext(Dispatchers.IO) {
                runCatching { lookup(context, key, name) }.getOrNull()
            } ?: return
            setEntry(context, key, if (found) ArtistImageSource.DOWNLOADED else ArtistImageSource.MISSING)
            delay(REQUEST_GAP_MILLIS)
        }
    }

    private fun lookup(context: Context, artistKey: String, name: String): Boolean {
        val response = JSONObject(String(httpGet(SEARCH_URL + URLEncoder.encode(name, "UTF-8")), Charsets.UTF_8))
        if (response.has("error")) throw IOException(response.optJSONObject("error")?.optString("message").orEmpty())
        val results = response.optJSONArray("data") ?: return false
        val target = normalize(name)
        var bestUrl: String? = null
        var bestFans = -1
        for (position in 0 until results.length()) {
            val artist = results.optJSONObject(position) ?: continue
            if (normalize(artist.optString("name")) != target) continue
            val picture = artist.optString("picture_xl")
            if (picture.isBlank() || picture.contains(EMPTY_PICTURE_MARKER)) continue
            val fans = artist.optInt("nb_fan", 0)
            if (fans > bestFans) {
                bestFans = fans
                bestUrl = picture
            }
        }
        val url = bestUrl ?: return false
        val bytes = httpGet(url)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false
        writeAtomically(fileFor(context, artistKey), bytes)
        return true
    }

    private val http by lazy { KateHttp.client(TIMEOUT_MILLIS) }

    private fun httpGet(address: String): ByteArray {
        val request = Request.Builder()
            .url(address)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json, image/*")
            .build()
        http.newCall(request).execute().use { response ->
            if (response.code != 200) {
                throw IOException("HTTP ${response.code}")
            }
            response.body.byteStream().use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(16 * 1024)
                var total = 0
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    total += read
                    if (total > MAX_DOWNLOAD_BYTES) throw IOException("Response too large")
                    output.write(buffer, 0, read)
                }
                return output.toByteArray()
            }
        }
    }

    private fun importImage(context: Context, uri: Uri, target: File): Boolean {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
            true
        } ?: return false
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_CUSTOM_SIZE) sample *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return false
        val orientation = resolver.openInputStream(uri)?.use {
            runCatching { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
                .getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        val rotated = applyOrientation(decoded, orientation)
        val scale = MAX_CUSTOM_SIZE.toFloat() / maxOf(rotated.width, rotated.height)
        val finalBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(rotated, (rotated.width * scale).toInt().coerceAtLeast(1), (rotated.height * scale).toInt().coerceAtLeast(1), true)
        } else {
            rotated
        }
        val output = ByteArrayOutputStream()
        val compressed = finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        if (finalBitmap !== decoded) finalBitmap.recycle()
        if (rotated !== decoded && rotated !== finalBitmap) rotated.recycle()
        decoded.recycle()
        if (!compressed) return false
        writeAtomically(target, output.toByteArray())
        return true
    }

    private fun applyOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun writeAtomically(target: File, bytes: ByteArray) {
        target.parentFile?.mkdirs()
        val temp = File(target.parentFile, target.name + ".tmp")
        temp.writeBytes(bytes)
        if (!temp.renameTo(target)) {
            target.delete()
            if (!temp.renameTo(target)) {
                temp.delete()
                throw IOException("Could not save image")
            }
        }
    }

    private fun canUseNetwork(context: Context): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return false
        return !wifiOnly || capabilities.isUnmetered()
    }

    private fun NetworkCapabilities.isUnmetered(): Boolean =
        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

    private fun registerNetworkCallback(context: Context) {
        val manager = context.getSystemService(ConnectivityManager::class.java) ?: return
        runCatching {
            manager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    scope.launch { networkVersion++ }
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val unmetered = networkCapabilities.isUnmetered()
                    scope.launch {
                        if (lastUnmetered != unmetered) {
                            lastUnmetered = unmetered
                            networkVersion++
                        }
                    }
                }

                override fun onLost(network: Network) {
                    scope.launch {
                        lastUnmetered = null
                        networkVersion++
                    }
                }
            })
        }
    }

    private fun load(context: Context) {
        val preferences = prefs(context)
        enabled = preferences.getBoolean(KEY_ENABLED, false)
        wifiOnly = preferences.getBoolean(KEY_WIFI_ONLY, true)
        val loaded = HashMap<String, Entry>()
        val loadedImages = HashMap<String, ArtistImage>()
        for ((prefKey, value) in preferences.all) {
            if (!prefKey.startsWith(ENTRY_PREFIX) || value !is String) continue
            val parts = value.split(FIELD_SEPARATOR)
            if (parts.size < 3) continue
            val source = runCatching { ArtistImageSource.valueOf(parts[0]) }.getOrNull() ?: continue
            val version = parts[1].toLongOrNull() ?: 0L
            val artistKey = parts.subList(2, parts.size).joinToString(FIELD_SEPARATOR.toString())
            loaded[artistKey] = Entry(source, version)
            val file = fileFor(context, artistKey)
            if (source.hasImage() && file.exists()) loadedImages[artistKey] = ArtistImage(file, version)
        }
        entries = loaded
        images = loadedImages
    }

    private fun setEntry(context: Context, artistKey: String, source: ArtistImageSource) {
        val version = System.currentTimeMillis()
        prefs(context).edit()
            .putString(ENTRY_PREFIX + hash(artistKey), "${source.name}$FIELD_SEPARATOR$version$FIELD_SEPARATOR$artistKey")
            .apply()
        entries = entries + (artistKey to Entry(source, version))
        val file = fileFor(context, artistKey)
        images = if (source.hasImage() && file.exists()) {
            images + (artistKey to ArtistImage(file, version))
        } else {
            images - artistKey
        }
    }

    private fun clearEntry(context: Context, artistKey: String) {
        prefs(context).edit().remove(ENTRY_PREFIX + hash(artistKey)).apply()
        entries = entries - artistKey
        images = images - artistKey
    }

    private fun ArtistImageSource.hasImage(): Boolean =
        this == ArtistImageSource.DOWNLOADED || this == ArtistImageSource.CUSTOM

    private fun fileFor(context: Context, artistKey: String): File =
        File(File(context.filesDir, IMAGE_DIR), hash(artistKey) + ".jpg")

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun hash(value: String): String =
        MessageDigest.getInstance("SHA-1").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()
}
