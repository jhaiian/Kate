package com.jhaiian.kate.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PlayRecord(
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val genre: String,
    val durationMs: Long,
    val startedAt: Long,
    val listenedMs: Long
)

object ListeningHistory {
    private const val PREFS_NAME = "listening_history"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_WRAPPED_BRANDING = "wrapped_branding"

    var enabled by mutableStateOf(true)
        private set
    var wrappedBranding by mutableStateOf(true)
        private set
    var version by mutableIntStateOf(0)
        private set
    private var loaded = false
    private var database: HistoryDatabase? = null
    private val writer = Dispatchers.IO.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val stored = prefs(context)
        enabled = stored.getBoolean(KEY_ENABLED, true)
        wrappedBranding = stored.getBoolean(KEY_WRAPPED_BRANDING, true)
    }

    fun setWrappedBranding(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (wrappedBranding == value) return
        wrappedBranding = value
        prefs(context).edit().putBoolean(KEY_WRAPPED_BRANDING, value).apply()
    }

    fun setEnabled(context: Context, value: Boolean) {
        ensureLoaded(context)
        if (enabled == value) return
        enabled = value
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    internal fun save(context: Context, playId: Long, record: PlayRecord, isNew: Boolean) {
        val appContext = context.applicationContext
        scope.launch {
            withContext(writer) {
                val values = ContentValues().apply {
                    put(HistoryDatabase.COLUMN_ID, playId)
                    put(HistoryDatabase.COLUMN_TRACK_ID, record.trackId)
                    put(HistoryDatabase.COLUMN_TITLE, record.title)
                    put(HistoryDatabase.COLUMN_ARTIST, record.artist)
                    put(HistoryDatabase.COLUMN_ALBUM, record.album)
                    put(HistoryDatabase.COLUMN_ALBUM_ARTIST, record.albumArtist)
                    put(HistoryDatabase.COLUMN_GENRE, record.genre)
                    put(HistoryDatabase.COLUMN_DURATION, record.durationMs)
                    put(HistoryDatabase.COLUMN_STARTED_AT, record.startedAt)
                    put(HistoryDatabase.COLUMN_LISTENED, record.listenedMs)
                }
                runCatching {
                    db(appContext).writableDatabase.insertWithOnConflict(
                        HistoryDatabase.TABLE_PLAYS,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                    )
                }
            }
            if (isNew) version++
        }
    }

    suspend fun plays(context: Context, fromMs: Long, toMs: Long): List<PlayRecord> = read(context) { database ->
        val result = ArrayList<PlayRecord>()
        database.query(
            HistoryDatabase.TABLE_PLAYS,
            arrayOf(
                HistoryDatabase.COLUMN_TRACK_ID,
                HistoryDatabase.COLUMN_TITLE,
                HistoryDatabase.COLUMN_ARTIST,
                HistoryDatabase.COLUMN_ALBUM,
                HistoryDatabase.COLUMN_ALBUM_ARTIST,
                HistoryDatabase.COLUMN_GENRE,
                HistoryDatabase.COLUMN_DURATION,
                HistoryDatabase.COLUMN_STARTED_AT,
                HistoryDatabase.COLUMN_LISTENED
            ),
            "${HistoryDatabase.COLUMN_STARTED_AT} >= ? AND ${HistoryDatabase.COLUMN_STARTED_AT} < ?",
            arrayOf(fromMs.toString(), toMs.toString()),
            null,
            null,
            "${HistoryDatabase.COLUMN_STARTED_AT} ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result.add(
                    PlayRecord(
                        trackId = cursor.getString(0).orEmpty(),
                        title = cursor.getString(1).orEmpty(),
                        artist = cursor.getString(2).orEmpty(),
                        album = cursor.getString(3).orEmpty(),
                        albumArtist = cursor.getString(4).orEmpty(),
                        genre = cursor.getString(5).orEmpty(),
                        durationMs = cursor.getLong(6),
                        startedAt = cursor.getLong(7),
                        listenedMs = cursor.getLong(8)
                    )
                )
            }
        }
        result
    } ?: emptyList()

    suspend fun listenedBetween(context: Context, fromMs: Long, toMs: Long): Long = read(context) { database ->
        database.rawQuery(
            "SELECT COALESCE(SUM(${HistoryDatabase.COLUMN_LISTENED}), 0) FROM ${HistoryDatabase.TABLE_PLAYS} " +
                "WHERE ${HistoryDatabase.COLUMN_STARTED_AT} >= ? AND ${HistoryDatabase.COLUMN_STARTED_AT} < ?",
            arrayOf(fromMs.toString(), toMs.toString())
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else 0L }
    } ?: 0L

    suspend fun distinctBefore(context: Context, column: String, beforeMs: Long): Set<String> = read(context) { database ->
        val result = HashSet<String>()
        database.rawQuery(
            "SELECT DISTINCT $column FROM ${HistoryDatabase.TABLE_PLAYS} WHERE ${HistoryDatabase.COLUMN_STARTED_AT} < ?",
            arrayOf(beforeMs.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) cursor.getString(0)?.let { result.add(it) }
        }
        result
    } ?: emptySet()

    suspend fun songsBefore(context: Context, beforeMs: Long): List<Pair<String, String>> = read(context) { database ->
        val result = ArrayList<Pair<String, String>>()
        database.rawQuery(
            "SELECT DISTINCT ${HistoryDatabase.COLUMN_TITLE}, ${HistoryDatabase.COLUMN_ARTIST} FROM ${HistoryDatabase.TABLE_PLAYS} " +
                "WHERE ${HistoryDatabase.COLUMN_STARTED_AT} < ?",
            arrayOf(beforeMs.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) result.add(cursor.getString(0).orEmpty() to cursor.getString(1).orEmpty())
        }
        result
    } ?: emptyList()

    suspend fun trackPlayStats(context: Context): Map<String, TrackPlays> = read(context) { database ->
        val result = HashMap<String, TrackPlays>()
        database.rawQuery(
            "SELECT ${HistoryDatabase.COLUMN_TRACK_ID}, COUNT(*), MAX(${HistoryDatabase.COLUMN_STARTED_AT}) " +
                "FROM ${HistoryDatabase.TABLE_PLAYS} GROUP BY ${HistoryDatabase.COLUMN_TRACK_ID}",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0) ?: continue
                result[id] = TrackPlays(count = cursor.getInt(1), lastPlayedAt = cursor.getLong(2))
            }
        }
        result
    } ?: emptyMap()

    suspend fun playSequence(context: Context): List<Pair<String, Long>> = read(context) { database ->
        val result = ArrayList<Pair<String, Long>>()
        database.rawQuery(
            "SELECT ${HistoryDatabase.COLUMN_TRACK_ID}, ${HistoryDatabase.COLUMN_STARTED_AT} " +
                "FROM ${HistoryDatabase.TABLE_PLAYS} ORDER BY ${HistoryDatabase.COLUMN_STARTED_AT} ASC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0) ?: continue
                result.add(id to cursor.getLong(1))
            }
        }
        result
    } ?: emptyList()

    suspend fun forEachPlay(context: Context, block: (Long, PlayRecord) -> Unit): Boolean = read(context) { database ->
        database.query(
            HistoryDatabase.TABLE_PLAYS,
            arrayOf(
                HistoryDatabase.COLUMN_ID,
                HistoryDatabase.COLUMN_TRACK_ID,
                HistoryDatabase.COLUMN_TITLE,
                HistoryDatabase.COLUMN_ARTIST,
                HistoryDatabase.COLUMN_ALBUM,
                HistoryDatabase.COLUMN_ALBUM_ARTIST,
                HistoryDatabase.COLUMN_GENRE,
                HistoryDatabase.COLUMN_DURATION,
                HistoryDatabase.COLUMN_STARTED_AT,
                HistoryDatabase.COLUMN_LISTENED
            ),
            null,
            null,
            null,
            null,
            "${HistoryDatabase.COLUMN_STARTED_AT} ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                block(
                    cursor.getLong(0),
                    PlayRecord(
                        trackId = cursor.getString(1).orEmpty(),
                        title = cursor.getString(2).orEmpty(),
                        artist = cursor.getString(3).orEmpty(),
                        album = cursor.getString(4).orEmpty(),
                        albumArtist = cursor.getString(5).orEmpty(),
                        genre = cursor.getString(6).orEmpty(),
                        durationMs = cursor.getLong(7),
                        startedAt = cursor.getLong(8),
                        listenedMs = cursor.getLong(9)
                    )
                )
            }
        }
        true
    } ?: false

    suspend fun playCount(context: Context): Int = read(context) { database ->
        database.rawQuery("SELECT COUNT(*) FROM ${HistoryDatabase.TABLE_PLAYS}", null)
            .use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }
    } ?: 0

    suspend fun importPlays(context: Context, plays: List<Pair<Long, PlayRecord>>): Int {
        val appContext = context.applicationContext
        return withContext(writer) {
            runCatching {
                val database = db(appContext).writableDatabase
                database.beginTransaction()
                try {
                    plays.forEach { (id, record) ->
                        val values = ContentValues().apply {
                            put(HistoryDatabase.COLUMN_ID, id)
                            put(HistoryDatabase.COLUMN_TRACK_ID, record.trackId)
                            put(HistoryDatabase.COLUMN_TITLE, record.title)
                            put(HistoryDatabase.COLUMN_ARTIST, record.artist)
                            put(HistoryDatabase.COLUMN_ALBUM, record.album)
                            put(HistoryDatabase.COLUMN_ALBUM_ARTIST, record.albumArtist)
                            put(HistoryDatabase.COLUMN_GENRE, record.genre)
                            put(HistoryDatabase.COLUMN_DURATION, record.durationMs)
                            put(HistoryDatabase.COLUMN_STARTED_AT, record.startedAt)
                            put(HistoryDatabase.COLUMN_LISTENED, record.listenedMs)
                        }
                        database.insertWithOnConflict(HistoryDatabase.TABLE_PLAYS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                    }
                    database.setTransactionSuccessful()
                } finally {
                    database.endTransaction()
                }
                plays.size
            }.getOrDefault(0)
        }
    }

    suspend fun firstPlayAt(context: Context): Long? = read(context) { database ->
        database.rawQuery(
            "SELECT MIN(${HistoryDatabase.COLUMN_STARTED_AT}) FROM ${HistoryDatabase.TABLE_PLAYS}",
            null
        ).use { cursor -> if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null }
    }

    fun clear(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            withContext(writer) {
                runCatching { db(appContext).writableDatabase.delete(HistoryDatabase.TABLE_PLAYS, null, null) }
            }
            version++
        }
    }

    internal const val COLUMN_ARTIST = HistoryDatabase.COLUMN_ARTIST

    private suspend fun <T> read(context: Context, block: (SQLiteDatabase) -> T): T? {
        val appContext = context.applicationContext
        return withContext(writer) {
            runCatching { block(db(appContext).readableDatabase) }.getOrNull()
        }
    }

    @Synchronized
    private fun db(context: Context): HistoryDatabase =
        database ?: HistoryDatabase(context).also { database = it }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

private class HistoryDatabase(context: Context) : SQLiteOpenHelper(context, NAME, null, VERSION) {
    companion object {
        const val NAME = "listening_history.db"
        const val VERSION = 1
        const val TABLE_PLAYS = "plays"
        const val COLUMN_ID = "id"
        const val COLUMN_TRACK_ID = "track_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ARTIST = "artist"
        const val COLUMN_ALBUM = "album"
        const val COLUMN_ALBUM_ARTIST = "album_artist"
        const val COLUMN_GENRE = "genre"
        const val COLUMN_DURATION = "duration_ms"
        const val COLUMN_STARTED_AT = "started_at"
        const val COLUMN_LISTENED = "listened_ms"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE $TABLE_PLAYS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY, " +
                "$COLUMN_TRACK_ID TEXT NOT NULL, " +
                "$COLUMN_TITLE TEXT NOT NULL, " +
                "$COLUMN_ARTIST TEXT NOT NULL, " +
                "$COLUMN_ALBUM TEXT NOT NULL, " +
                "$COLUMN_ALBUM_ARTIST TEXT NOT NULL, " +
                "$COLUMN_GENRE TEXT NOT NULL, " +
                "$COLUMN_DURATION INTEGER NOT NULL, " +
                "$COLUMN_STARTED_AT INTEGER NOT NULL, " +
                "$COLUMN_LISTENED INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX plays_started_at ON $TABLE_PLAYS($COLUMN_STARTED_AT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}
