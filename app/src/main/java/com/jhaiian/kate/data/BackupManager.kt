package com.jhaiian.kate.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

enum class BackupSection(val key: String) {
    SETTINGS("settings"),
    PLAYLISTS("playlists"),
    FAVORITES("favorites"),
    HISTORY("history"),
    ARTIST_IMAGES("artist_images");

    companion object {
        fun fromKey(key: String?): BackupSection? = entries.firstOrNull { it.key == key }
    }
}

data class BackupInfo(
    val createdAt: Long,
    val appVersion: String,
    val sections: Set<BackupSection>,
    val playlists: Int,
    val smartPlaylists: Int,
    val favorites: Int,
    val plays: Int
)

data class RestoreResult(
    val sections: Set<BackupSection>,
    val playlists: Int,
    val matchedSongs: Int,
    val totalSongs: Int,
    val plays: Int
)

object BackupManager {
    const val MIME_TYPE = "application/zip"
    private const val FORMAT = 1
    private const val MANIFEST = "manifest.json"
    private const val PREFS_DIR = "prefs/"
    private const val DEFAULT_PREFS = "default"
    private const val PLAYLISTS_FILE = "playlists/playlists.json"
    private const val SMART_FILE = "playlists/smart_playlists.json"
    private const val FAVORITES_FILE = "favorites.json"
    private const val TRACKS_FILE = "tracks.json"
    private const val HISTORY_FILE = "history.json"
    private const val ACHIEVEMENTS_FILE = "achievements.json"
    private const val ARTIST_PREFS_FILE = "artist_images/prefs.json"
    private const val ARTIST_FILES_DIR = "artist_images/files/"
    private const val ARTIST_IMAGE_DIR = "artist_images"
    private const val PLAYLIST_STORE_FILE = "playlists.json"
    private const val SMART_STORE_FILE = "smart_playlists.json"
    private const val STYLES_FILE = "settings/styles.json"
    private const val STYLES_STORE_FILE = "styles.json"
    private val MEDIA_DIRS = listOf("fonts", "backgrounds")

    private val settingsPrefs = listOf("audio_settings", "equalizer", "tag_settings", "shuffle_preferences", "kate_radio", "widget_settings", "listening_history", "track_positions", "update_settings", "style_rules", "scan_filters", "lyrics_offsets", "audio_effects", "eq_auto_presets", "system_controls", "app_language", SetupState.PREFS_NAME)
    private val excludedKeys = mapOf(
        DEFAULT_PREFS to setOf("music_folders"),
        "kate_radio" to setOf("seed_type", "seed_key", "seed_label")
    )

    fun defaultFileName(): String =
        "Kate-backup-" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + ".zip"

    suspend fun export(context: Context, uri: Uri, sections: Set<BackupSection>): Boolean = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        FavoritesStore.ensureLoaded(appContext)
        PlaylistStore.ensureLoaded(appContext)
        SmartPlaylistStore.ensureLoaded(appContext)
        LibraryStore.ensureLoaded(appContext)
        val playlists = PlaylistStore.playlists
        val smart = SmartPlaylistStore.playlists
        val favorites = FavoritesStore.ids
        val referenced = HashSet<String>()
        if (BackupSection.PLAYLISTS in sections) playlists.forEach { referenced.addAll(it.trackIds) }
        if (BackupSection.FAVORITES in sections) referenced.addAll(favorites)
        var plays = 0
        runCatching {
            val output = appContext.contentResolver.openOutputStream(uri, "w") ?: return@runCatching false
            ZipOutputStream(output.buffered()).use { zip ->
                if (BackupSection.SETTINGS in sections) {
                    putText(zip, PREFS_DIR + DEFAULT_PREFS + ".json", prefsToJson(PreferenceManager.getDefaultSharedPreferences(appContext), excludedKeys[DEFAULT_PREFS].orEmpty()).toString())
                    settingsPrefs.forEach { name ->
                        putText(zip, "$PREFS_DIR$name.json", prefsToJson(namedPrefs(appContext, name), excludedKeys[name].orEmpty()).toString())
                    }
                    putFile(zip, STYLES_FILE, File(appContext.filesDir, STYLES_STORE_FILE))
                    MEDIA_DIRS.forEach { dir ->
                        File(appContext.filesDir, dir).listFiles()?.filter { it.isFile }?.forEach { putFile(zip, "$dir/${it.name}", it) }
                    }
                }
                if (BackupSection.PLAYLISTS in sections) {
                    putFile(zip, PLAYLISTS_FILE, File(appContext.filesDir, PLAYLIST_STORE_FILE))
                    putFile(zip, SMART_FILE, File(appContext.filesDir, SMART_STORE_FILE))
                }
                if (BackupSection.FAVORITES in sections) {
                    putText(zip, FAVORITES_FILE, JSONArray(favorites.toList()).toString())
                }
                if (referenced.isNotEmpty()) {
                    val byId = LibraryStore.tracks.associateBy { it.id }
                    val array = JSONArray()
                    referenced.forEach { id ->
                        val track = byId[id]
                        val item = JSONObject().put("id", id)
                        if (track != null) {
                            item.put("path", track.path)
                                .put("file", track.filename)
                                .put("title", track.title)
                                .put("artist", track.artist)
                                .put("album", track.album)
                                .put("duration", track.durationMs)
                        }
                        array.put(item)
                    }
                    putText(zip, TRACKS_FILE, array.toString())
                }
                if (BackupSection.HISTORY in sections) {
                    zip.putNextEntry(ZipEntry(HISTORY_FILE))
                    val writer = JsonWriter(OutputStreamWriter(NonClosingStream(zip), Charsets.UTF_8))
                    writer.beginArray()
                    ListeningHistory.forEachPlay(appContext) { id, record ->
                        plays++
                        writer.beginObject()
                        writer.name("i").value(id)
                        writer.name("t").value(record.trackId)
                        writer.name("n").value(record.title)
                        writer.name("a").value(record.artist)
                        writer.name("l").value(record.album)
                        writer.name("aa").value(record.albumArtist)
                        writer.name("g").value(record.genre)
                        writer.name("d").value(record.durationMs)
                        writer.name("s").value(record.startedAt)
                        writer.name("m").value(record.listenedMs)
                        writer.endObject()
                    }
                    writer.endArray()
                    writer.flush()
                    zip.closeEntry()
                    putText(zip, ACHIEVEMENTS_FILE, prefsToJson(namedPrefs(appContext, "achievements"), emptySet()).toString())
                }
                if (BackupSection.ARTIST_IMAGES in sections) {
                    putText(zip, ARTIST_PREFS_FILE, prefsToJson(namedPrefs(appContext, "artist_images"), emptySet()).toString())
                    File(appContext.filesDir, ARTIST_IMAGE_DIR).listFiles()?.filter { it.isFile }?.forEach { file ->
                        putFile(zip, ARTIST_FILES_DIR + file.name, file)
                    }
                }
                val manifest = JSONObject()
                    .put("app", "Kate")
                    .put("format", FORMAT)
                    .put("version", appVersion(appContext))
                    .put("created", System.currentTimeMillis())
                    .put("sections", JSONArray(sections.map { it.key }))
                    .put("playlists", if (BackupSection.PLAYLISTS in sections) playlists.size else 0)
                    .put("smart_playlists", if (BackupSection.PLAYLISTS in sections) smart.size else 0)
                    .put("favorites", if (BackupSection.FAVORITES in sections) favorites.size else 0)
                    .put("plays", plays)
                putText(zip, MANIFEST, manifest.toString())
            }
            true
        }.getOrDefault(false)
    }

    suspend fun inspect(context: Context, uri: Uri): BackupInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val input = context.contentResolver.openInputStream(uri) ?: return@runCatching null
            ZipInputStream(input.buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == MANIFEST) {
                        val manifest = JSONObject(zip.readBytes().toString(Charsets.UTF_8))
                        if (manifest.optString("app") !in setOf("Kate", "KateMusic")) return@runCatching null
                        val sectionArray = manifest.optJSONArray("sections") ?: JSONArray()
                        val sections = (0 until sectionArray.length()).mapNotNull { BackupSection.fromKey(sectionArray.optString(it)) }.toSet()
                        return@runCatching BackupInfo(
                            createdAt = manifest.optLong("created"),
                            appVersion = manifest.optString("version"),
                            sections = sections,
                            playlists = manifest.optInt("playlists"),
                            smartPlaylists = manifest.optInt("smart_playlists"),
                            favorites = manifest.optInt("favorites"),
                            plays = manifest.optInt("plays")
                        )
                    }
                    entry = zip.nextEntry
                }
                null
            }
        }.getOrNull()
    }

    suspend fun restore(context: Context, uri: Uri, sections: Set<BackupSection>): RestoreResult? = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        LibraryStore.ensureLoaded(appContext)
        val texts = HashMap<String, String>()
        val history = ArrayList<Pair<Long, PlayRecord>>()
        val artistFiles = ArrayList<String>()
        val ok = runCatching {
            val input = appContext.contentResolver.openInputStream(uri) ?: return@runCatching false
            ZipInputStream(input.buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    when {
                        entry.isDirectory || name.contains("..") -> Unit
                        name == HISTORY_FILE && BackupSection.HISTORY in sections -> readHistory(zip, history)
                        name.startsWith(ARTIST_FILES_DIR) && BackupSection.ARTIST_IMAGES in sections -> {
                            val fileName = name.removePrefix(ARTIST_FILES_DIR)
                            if (fileName.isNotBlank() && !fileName.contains('/')) {
                                val directory = File(appContext.filesDir, ARTIST_IMAGE_DIR).apply { mkdirs() }
                                File(directory, fileName).outputStream().use { zip.copyTo(it) }
                                artistFiles.add(fileName)
                            }
                        }
                        BackupSection.SETTINGS in sections && MEDIA_DIRS.any { name.startsWith("$it/") } -> {
                            val dir = name.substringBefore('/')
                            val fileName = name.substringAfter('/')
                            if (fileName.isNotBlank() && !fileName.contains('/')) {
                                val directory = File(appContext.filesDir, dir).apply { mkdirs() }
                                File(directory, fileName).outputStream().use { zip.copyTo(it) }
                            }
                        }
                        name.endsWith(".json") -> texts[name] = zip.readBytes().toString(Charsets.UTF_8)
                    }
                    entry = zip.nextEntry
                }
            }
            true
        }.getOrDefault(false)
        if (!ok || MANIFEST !in texts) return@withContext null

        val matcher = TrackMatcher(LibraryStore.tracks.toList(), texts[TRACKS_FILE])
        var playlistCount = 0
        var restoredPlays = 0
        val restored = HashSet<BackupSection>()

        if (BackupSection.SETTINGS in sections) {
            var any = false
            texts.filterKeys { it.startsWith(PREFS_DIR) }.forEach { (name, raw) ->
                val prefsName = name.removePrefix(PREFS_DIR).removeSuffix(".json")
                val prefs = if (prefsName == DEFAULT_PREFS) {
                    PreferenceManager.getDefaultSharedPreferences(appContext)
                } else if (prefsName in settingsPrefs) {
                    namedPrefs(appContext, prefsName)
                } else {
                    return@forEach
                }
                runCatching { applyPrefs(prefs, JSONObject(raw), excludedKeys[prefsName].orEmpty()) }.onSuccess { any = true }
            }
            texts[STYLES_FILE]?.let { raw ->
                runCatching {
                    JSONObject(raw)
                    writeAtomically(File(appContext.filesDir, STYLES_STORE_FILE), raw)
                    any = true
                }
            }
            if (any) restored.add(BackupSection.SETTINGS)
        }

        if (BackupSection.PLAYLISTS in sections) {
            texts[PLAYLISTS_FILE]?.let { raw ->
                runCatching {
                    val incoming = JSONArray(raw)
                    val remapped = JSONArray()
                    for (index in 0 until incoming.length()) {
                        val item = incoming.optJSONObject(index) ?: continue
                        val ids = item.optJSONArray("tracks") ?: JSONArray()
                        val mapped = (0 until ids.length()).mapNotNull { matcher.match(ids.optString(it)) }.distinct()
                        item.put("tracks", JSONArray(mapped))
                        remapped.put(item)
                    }
                    playlistCount = remapped.length()
                    writeAtomically(File(appContext.filesDir, PLAYLIST_STORE_FILE), mergeById(readArray(File(appContext.filesDir, PLAYLIST_STORE_FILE)), remapped).toString())
                }
            }
            texts[SMART_FILE]?.let { raw ->
                runCatching {
                    val incoming = JSONArray(raw)
                    playlistCount += incoming.length()
                    writeAtomically(File(appContext.filesDir, SMART_STORE_FILE), mergeById(readArray(File(appContext.filesDir, SMART_STORE_FILE)), incoming).toString())
                }
            }
            restored.add(BackupSection.PLAYLISTS)
        }

        if (BackupSection.FAVORITES in sections) {
            texts[FAVORITES_FILE]?.let { raw ->
                runCatching {
                    val array = JSONArray(raw)
                    val mapped = (0 until array.length()).mapNotNull { matcher.match(array.optString(it)) }
                    val prefs = namedPrefs(appContext, "favorites")
                    val current = prefs.getStringSet("track_ids", null)?.toSet().orEmpty()
                    prefs.edit().putStringSet("track_ids", HashSet(current + mapped)).commit()
                    restored.add(BackupSection.FAVORITES)
                }
            }
        }

        if (BackupSection.HISTORY in sections && history.isNotEmpty()) {
            val remapped = history.map { (id, record) ->
                val trackId = matcher.match(record.trackId, record.title, record.artist, record.durationMs) ?: record.trackId
                id to record.copy(trackId = trackId)
            }
            restoredPlays = ListeningHistory.importPlays(appContext, remapped)
            texts[ACHIEVEMENTS_FILE]?.let { raw ->
                runCatching {
                    val prefs = namedPrefs(appContext, "achievements")
                    val incoming = JSONObject(raw).optJSONObject("announced")?.optJSONArray("v")
                    val announced = HashSet(prefs.getStringSet("announced", null).orEmpty())
                    if (incoming != null) for (index in 0 until incoming.length()) announced.add(incoming.optString(index))
                    prefs.edit().putBoolean("initialized", true).putStringSet("announced", announced).commit()
                }
            }
            restored.add(BackupSection.HISTORY)
        }

        if (BackupSection.ARTIST_IMAGES in sections) {
            texts[ARTIST_PREFS_FILE]?.let { raw ->
                runCatching { applyPrefs(namedPrefs(appContext, "artist_images"), JSONObject(raw), emptySet()) }
                restored.add(BackupSection.ARTIST_IMAGES)
            }
            if (artistFiles.isNotEmpty()) restored.add(BackupSection.ARTIST_IMAGES)
        }

        RestoreResult(
            sections = restored,
            playlists = playlistCount,
            matchedSongs = matcher.matched,
            totalSongs = matcher.requested,
            plays = restoredPlays
        )
    }

    fun restartApp(context: Context) {
        val appContext = context.applicationContext
        val intent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            appContext.startActivity(intent)
        }
        Runtime.getRuntime().exit(0)
    }

    private fun readHistory(input: ZipInputStream, out: MutableList<Pair<Long, PlayRecord>>) {
        val reader = JsonReader(InputStreamReader(NonClosingInput(input), Charsets.UTF_8))
        reader.beginArray()
        while (reader.hasNext()) {
            var id = 0L
            var trackId = ""
            var title = ""
            var artist = ""
            var album = ""
            var albumArtist = ""
            var genre = ""
            var duration = 0L
            var started = 0L
            var listened = 0L
            reader.beginObject()
            while (reader.hasNext()) {
                val key = reader.nextName()
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                    continue
                }
                when (key) {
                    "i" -> id = reader.nextLong()
                    "t" -> trackId = reader.nextString()
                    "n" -> title = reader.nextString()
                    "a" -> artist = reader.nextString()
                    "l" -> album = reader.nextString()
                    "aa" -> albumArtist = reader.nextString()
                    "g" -> genre = reader.nextString()
                    "d" -> duration = reader.nextLong()
                    "s" -> started = reader.nextLong()
                    "m" -> listened = reader.nextLong()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()
            if (trackId.isNotEmpty()) {
                out.add(id to PlayRecord(trackId, title, artist, album, albumArtist, genre, duration, started, listened))
            }
        }
        reader.endArray()
    }

    private fun prefsToJson(prefs: SharedPreferences, exclude: Set<String>): JSONObject {
        val json = JSONObject()
        prefs.all.forEach { (key, value) ->
            if (key in exclude) return@forEach
            val entry = when (value) {
                is String -> JSONObject().put("t", "s").put("v", value)
                is Boolean -> JSONObject().put("t", "b").put("v", value)
                is Int -> JSONObject().put("t", "i").put("v", value)
                is Long -> JSONObject().put("t", "l").put("v", value)
                is Float -> JSONObject().put("t", "f").put("v", value.toDouble())
                is Set<*> -> JSONObject().put("t", "ss").put("v", JSONArray(value.filterIsInstance<String>()))
                else -> null
            }
            if (entry != null) json.put(key, entry)
        }
        return json
    }

    private fun applyPrefs(prefs: SharedPreferences, json: JSONObject, exclude: Set<String>) {
        val editor = prefs.edit()
        json.keys().forEach { key ->
            if (key in exclude) return@forEach
            val entry = json.optJSONObject(key) ?: return@forEach
            when (entry.optString("t")) {
                "s" -> editor.putString(key, entry.optString("v"))
                "b" -> editor.putBoolean(key, entry.optBoolean("v"))
                "i" -> editor.putInt(key, entry.optInt("v"))
                "l" -> editor.putLong(key, entry.optLong("v"))
                "f" -> editor.putFloat(key, entry.optDouble("v").toFloat())
                "ss" -> {
                    val array = entry.optJSONArray("v") ?: JSONArray()
                    editor.putStringSet(key, (0 until array.length()).mapTo(HashSet()) { array.optString(it) })
                }
            }
        }
        editor.commit()
    }

    private fun mergeById(existing: JSONArray, incoming: JSONArray): JSONArray {
        val incomingIds = HashSet<String>()
        for (index in 0 until incoming.length()) incoming.optJSONObject(index)?.optString("id")?.let { incomingIds.add(it) }
        val merged = JSONArray()
        for (index in 0 until existing.length()) {
            val item = existing.optJSONObject(index) ?: continue
            if (item.optString("id") !in incomingIds) merged.put(item)
        }
        for (index in 0 until incoming.length()) incoming.optJSONObject(index)?.let { merged.put(it) }
        return merged
    }

    private fun readArray(file: File): JSONArray =
        runCatching { JSONArray(file.readText()) }.getOrDefault(JSONArray())

    private fun writeAtomically(target: File, content: String) {
        val temp = File(target.parentFile, target.name + ".restore")
        temp.writeText(content)
        if (!temp.renameTo(target)) {
            target.writeText(content)
            temp.delete()
        }
    }

    private fun putText(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun putFile(zip: ZipOutputStream, name: String, file: File) {
        if (!file.isFile) return
        zip.putNextEntry(ZipEntry(name))
        file.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }

    private fun namedPrefs(context: Context, name: String): SharedPreferences =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    @Suppress("DEPRECATION")
    private fun appVersion(context: Context): String =
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
}

private class TrackMatcher(library: List<Track>, rawIndex: String?) {
    private val byId = library.associateBy { it.id }
    private val byPath = HashMap<String, Track>()
    private val byFile = HashMap<String, MutableList<Track>>()
    private val byMeta = HashMap<String, MutableList<Track>>()
    private val index = HashMap<String, JSONObject>()
    private val cache = HashMap<String, String?>()
    var matched = 0
        private set
    var requested = 0
        private set

    init {
        library.forEach { track ->
            if (track.path.isNotBlank()) byPath[track.path.lowercase()] = track
            if (track.filename.isNotBlank()) byFile.getOrPut(track.filename.lowercase()) { ArrayList() }.add(track)
            byMeta.getOrPut(metaKey(track.title, track.artist)) { ArrayList() }.add(track)
        }
        if (rawIndex != null) {
            runCatching {
                val array = JSONArray(rawIndex)
                for (position in 0 until array.length()) {
                    val item = array.optJSONObject(position) ?: continue
                    index[item.optString("id")] = item
                }
            }
        }
    }

    fun match(id: String, title: String? = null, artist: String? = null, durationMs: Long? = null): String? {
        if (cache.containsKey(id)) return cache[id]
        requested++
        val result = find(id, title, artist, durationMs)
        if (result != null) matched++
        cache[id] = result
        return result
    }

    private fun find(id: String, title: String?, artist: String?, durationMs: Long?): String? {
        byId[id]?.let { return it.id }
        val info = index[id]
        val path = info?.optString("path").orEmpty()
        if (path.isNotBlank()) byPath[path.lowercase()]?.let { return it.id }
        val wantedTitle = info?.optString("title")?.takeIf { it.isNotBlank() } ?: title
        val wantedArtist = info?.takeIf { it.has("artist") }?.optString("artist") ?: artist
        val wantedDuration = info?.optLong("duration")?.takeIf { it > 0L } ?: durationMs
        if (wantedTitle != null && wantedArtist != null) {
            pick(byMeta[metaKey(wantedTitle, wantedArtist)], wantedDuration)?.let { return it.id }
        }
        val file = info?.optString("file").orEmpty()
        if (file.isNotBlank()) pick(byFile[file.lowercase()], wantedDuration)?.let { return it.id }
        return null
    }

    private fun pick(candidates: List<Track>?, durationMs: Long?): Track? {
        if (candidates.isNullOrEmpty()) return null
        if (durationMs == null || durationMs <= 0L) return candidates.first()
        return candidates.minByOrNull { kotlin.math.abs(it.durationMs - durationMs) }
            ?.takeIf { kotlin.math.abs(it.durationMs - durationMs) <= 4_000L || candidates.size == 1 }
    }

    private fun metaKey(title: String, artist: String): String =
        title.trim().lowercase() + "\u0000" + artist.trim().lowercase()
}

private class NonClosingStream(private val target: java.io.OutputStream) : java.io.OutputStream() {
    override fun write(b: Int) = target.write(b)
    override fun write(b: ByteArray, off: Int, len: Int) = target.write(b, off, len)
    override fun flush() = target.flush()
    override fun close() = flush()
}

private class NonClosingInput(private val source: java.io.InputStream) : java.io.InputStream() {
    override fun read(): Int = source.read()
    override fun read(b: ByteArray, off: Int, len: Int): Int = source.read(b, off, len)
    override fun available(): Int = source.available()
    override fun close() = Unit
}
