package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class Playlist(
    val id: String,
    val name: String,
    val trackIds: List<String>,
    val createdAt: Long,
    val updatedAt: Long
)

object PlaylistStore {
    private const val FILE_NAME = "playlists.json"
    private const val KEY_ID = "id"
    private const val KEY_NAME = "name"
    private const val KEY_TRACKS = "tracks"
    private const val KEY_CREATED = "created"
    private const val KEY_UPDATED = "updated"

    var playlists by mutableStateOf<List<Playlist>>(emptyList())
        private set
    private var loaded = false
    private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val app = context.applicationContext
        appContext = app
        playlists = runCatching { parse(File(app.filesDir, FILE_NAME).readText()) }.getOrDefault(emptyList())
    }

    fun playlist(id: String): Playlist? = playlists.firstOrNull { it.id == id }

    fun tracksOf(playlist: Playlist, library: List<Track>): List<Track> {
        val byId = library.associateBy { it.id }
        return playlist.trackIds.mapNotNull { byId[it] }
    }

    fun create(context: Context, name: String, trackIds: List<String> = emptyList()): Playlist {
        ensureLoaded(context)
        val now = System.currentTimeMillis()
        val playlist = Playlist(UUID.randomUUID().toString(), name.trim(), trackIds.distinct(), now, now)
        playlists = playlists + playlist
        persist()
        return playlist
    }

    fun rename(id: String, name: String) = update(id) { it.copy(name = name.trim()) }

    fun delete(id: String) {
        playlists = playlists.filterNot { it.id == id }
        persist()
    }

    fun addTracks(id: String, trackIds: List<String>): Int {
        var added = 0
        update(id) { playlist ->
            val existing = playlist.trackIds.toHashSet()
            val fresh = trackIds.distinct().filter { it !in existing }
            added = fresh.size
            if (fresh.isEmpty()) playlist else playlist.copy(trackIds = playlist.trackIds + fresh)
        }
        return added
    }

    fun moveTrack(id: String, from: Int, to: Int) = update(id) { playlist ->
        if (from !in playlist.trackIds.indices || to !in playlist.trackIds.indices) return@update playlist
        playlist.copy(trackIds = playlist.trackIds.toMutableList().also { it.add(to, it.removeAt(from)) })
    }

    fun removeTrackAt(id: String, index: Int) = update(id) { playlist ->
        if (index !in playlist.trackIds.indices) return@update playlist
        playlist.copy(trackIds = playlist.trackIds.toMutableList().also { it.removeAt(index) })
    }

    private fun update(id: String, transform: (Playlist) -> Playlist) {
        var changed = false
        playlists = playlists.map { playlist ->
            if (playlist.id != id) return@map playlist
            val next = transform(playlist)
            if (next == playlist) playlist else next.copy(updatedAt = System.currentTimeMillis()).also { changed = true }
        }
        if (changed) persist()
    }

    private fun persist() {
        val context = appContext ?: return
        val snapshot = playlists
        scope.launch {
            runCatching {
                val target = File(context.filesDir, FILE_NAME)
                val temp = File(context.filesDir, "$FILE_NAME.tmp")
                temp.writeText(serialize(snapshot))
                if (!temp.renameTo(target)) {
                    target.writeText(temp.readText())
                    temp.delete()
                }
            }
        }
    }

    private fun serialize(items: List<Playlist>): String {
        val array = JSONArray()
        items.forEach { playlist ->
            array.put(
                JSONObject()
                    .put(KEY_ID, playlist.id)
                    .put(KEY_NAME, playlist.name)
                    .put(KEY_TRACKS, JSONArray(playlist.trackIds))
                    .put(KEY_CREATED, playlist.createdAt)
                    .put(KEY_UPDATED, playlist.updatedAt)
            )
        }
        return array.toString()
    }

    private fun parse(raw: String): List<Playlist> {
        val array = JSONArray(raw)
        return (0 until array.length()).mapNotNull { position ->
            val item = array.optJSONObject(position) ?: return@mapNotNull null
            val id = item.optString(KEY_ID).takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val tracks = item.optJSONArray(KEY_TRACKS)
            Playlist(
                id = id,
                name = item.optString(KEY_NAME),
                trackIds = if (tracks == null) emptyList() else (0 until tracks.length()).map { tracks.optString(it) }.filter { it.isNotBlank() },
                createdAt = item.optLong(KEY_CREATED),
                updatedAt = item.optLong(KEY_UPDATED)
            )
        }
    }
}
