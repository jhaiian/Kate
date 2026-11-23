package com.jhaiian.kate.data

import android.content.Context
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class NetEaseSong(val id: Long, val name: String, val artists: String, val album: String, val durationMs: Long)

class NetEaseLyrics(val wordSynced: String?, val lineSynced: String?)

object NetEase {
    private const val API_DOMAIN = "https://interface.music.163.com"
    private const val EAPI_KEY = "e82ckenh8dichen8"
    private const val SEPARATOR = "-36cd479b6b5-"
    private const val USER_AGENT = "NeteaseMusic 9.0.90/5038 (iPhone; iOS 16.2; zh_CN)"
    private const val TIMEOUT_MILLIS = 12_000
    private const val PREFS_NAME = "netease"
    private const val KEY_DEVICE = "device_id"

    sealed interface Outcome<out T> {
        class Ok<T>(val value: T) : Outcome<T>
        data object Failed : Outcome<Nothing>
    }

    fun search(context: Context, query: String, limit: Int = 8): Outcome<List<NetEaseSong>> {
        val body = JSONObject().apply {
            put("s", query)
            put("type", 1)
            put("limit", limit)
            put("offset", 0)
            put("total", true)
        }
        val json = request(context, "/api/cloudsearch/pc", body) ?: return Outcome.Failed
        val songs = json.optJSONObject("result")?.optJSONArray("songs") ?: return Outcome.Ok(emptyList())
        val result = (0 until songs.length()).mapNotNull { index ->
            val song = songs.optJSONObject(index) ?: return@mapNotNull null
            val id = song.optLong("id", -1L).takeIf { it > 0 } ?: return@mapNotNull null
            val artists = song.optJSONArray("ar") ?: song.optJSONArray("artists")
            val artistNames = if (artists == null) "" else (0 until artists.length()).mapNotNull {
                artists.optJSONObject(it)?.optString("name")?.takeIf { name -> name.isNotBlank() }
            }.joinToString(", ")
            val album = (song.optJSONObject("al") ?: song.optJSONObject("album"))?.optString("name").orEmpty()
            val duration = song.optLong("dt", song.optLong("duration", 0L))
            NetEaseSong(id, song.optString("name"), artistNames, album, duration)
        }
        return Outcome.Ok(result)
    }

    fun lyrics(context: Context, id: Long): Outcome<NetEaseLyrics> {
        val body = JSONObject().apply {
            put("id", id.toString())
            put("cp", false)
            put("tv", 0)
            put("lv", 0)
            put("rv", 0)
            put("kv", 0)
            put("yv", 0)
            put("ytv", 0)
            put("yrv", 0)
        }
        val json = request(context, "/api/song/lyric/v1", body) ?: return Outcome.Failed
        val yrc = json.optJSONObject("yrc")?.optString("lyric")?.takeIf { it.isNotBlank() }
        val lrc = json.optJSONObject("lrc")?.optString("lyric")?.takeIf { it.isNotBlank() }
        return Outcome.Ok(NetEaseLyrics(yrc?.let { WordLyrics.yrcToLrc(it) }, lrc))
    }

    private fun deviceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(KEY_DEVICE, null)?.let { return it }
        val bytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val created = bytes.joinToString("") { "%02X".format(it) }
        prefs.edit().putString(KEY_DEVICE, created).apply()
        return created
    }

    private fun request(context: Context, path: String, data: JSONObject): JSONObject? {
        val now = System.currentTimeMillis()
        val header = linkedMapOf(
            "osver" to "16.2",
            "deviceId" to deviceId(context),
            "os" to "iPhone OS",
            "appver" to "9.0.90",
            "versioncode" to "140",
            "mobilename" to "",
            "buildver" to (now / 1_000L).toString(),
            "resolution" to "1920x1080",
            "__csrf" to "",
            "channel" to "distribution",
            "requestId" to "${now}_${"%04d".format((0..999).random())}"
        )
        data.put("header", JSONObject(header as Map<*, *>))
        data.put("e_r", false)
        val text = data.toString()
        val digest = md5Hex("nobody${path}use${text}md5forencrypt")
        val params = aesHex("$path$SEPARATOR$text$SEPARATOR$digest")
        val cookie = header.entries.joinToString("; ") { (key, value) ->
            URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8").replace("+", "%20")
        }
        return try {
            val request = Request.Builder()
                .url(API_DOMAIN + "/eapi/" + path.removePrefix("/api/"))
                .header("User-Agent", USER_AGENT)
                .header("Cookie", cookie)
                .post("params=$params".toByteArray(Charsets.UTF_8).toRequestBody(FORM_TYPE))
                .build()
            http.newCall(request).execute().use { response ->
                if (response.code != 200) return null
                val json = JSONObject(response.body.string())
                val code = json.optInt("code", 200)
                if (code != 200) null else json
            }
        } catch (error: Exception) {
            null
        }
    }

    private val FORM_TYPE = "application/x-www-form-urlencoded".toMediaType()
    private val http by lazy { KateHttp.client(TIMEOUT_MILLIS) }

    private fun md5Hex(text: String): String =
        MessageDigest.getInstance("MD5").digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    private fun aesHex(text: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(EAPI_KEY.toByteArray(Charsets.UTF_8), "AES"))
        return cipher.doFinal(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02X".format(it) }
    }
}
