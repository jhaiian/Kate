package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class Achievement(val key: String, val target: Long, val percent: Boolean = false) {
    PLAYS_100("plays_100", 100),
    PLAYS_1000("plays_1000", 1_000),
    PLAYS_10000("plays_10000", 10_000),
    HOURS_10("hours_10", 10),
    HOURS_100("hours_100", 100),
    STREAK_7("streak_7", 7),
    NIGHT_OWL("night_owl", 50),
    EARLY_BIRD("early_bird", 50),
    ALBUM_FINISHER("album_finisher", 100, percent = true),
    EXPLORER("explorer", 100, percent = true),
    GENRE_HOPPER("genre_hopper", 10),
    LOYAL_FAN("loyal_fan", 100)
}

data class AchievementState(
    val achievement: Achievement,
    val progress: Long,
    val unlockedAt: Long?
) {
    val unlocked: Boolean get() = unlockedAt != null

    val fraction: Float
        get() = if (unlocked) 1f else (progress.toFloat() / achievement.target).coerceIn(0f, 1f)
}

object AchievementEngine {
    private const val HOUR_MS = 3_600_000L
    private const val MIN_ALBUM_SIZE = 3

    fun compute(
        plays: List<PlayRecord>,
        library: List<Track>,
        albums: List<AlbumGroup>,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<AchievementState> {
        val unlocked = HashMap<Achievement, Long>()
        fun unlock(achievement: Achievement, at: Long) {
            if (achievement !in unlocked) unlocked[achievement] = at
        }
        val libraryIds = library.mapTo(HashSet()) { it.id }
        val albumOf = HashMap<String, Int>()
        val albumSizes = ArrayList<Int>()
        albums.filter { it.tracks.size >= MIN_ALBUM_SIZE }.forEachIndexed { index, album ->
            albumSizes.add(album.tracks.size)
            album.tracks.forEach { albumOf[it.id] = index }
        }
        val albumHeard = HashMap<Int, HashSet<String>>()
        var bestAlbumPercent = 0L
        val explored = HashSet<String>()
        val genres = HashSet<String>()
        val artistCounts = HashMap<String, Int>()
        var bestArtist = 0
        var count = 0L
        var listened = 0L
        var night = 0L
        var early = 0L
        var lastDate: LocalDate? = null
        var streak = 0
        var bestStreak = 0

        plays.forEach { play ->
            val at = play.startedAt
            count++
            if (count == Achievement.PLAYS_100.target) unlock(Achievement.PLAYS_100, at)
            if (count == Achievement.PLAYS_1000.target) unlock(Achievement.PLAYS_1000, at)
            if (count == Achievement.PLAYS_10000.target) unlock(Achievement.PLAYS_10000, at)
            listened += play.listenedMs
            if (listened >= Achievement.HOURS_10.target * HOUR_MS) unlock(Achievement.HOURS_10, at)
            if (listened >= Achievement.HOURS_100.target * HOUR_MS) unlock(Achievement.HOURS_100, at)

            val local = Instant.ofEpochMilli(at).atZone(zone)
            val date = local.toLocalDate()
            val previous = lastDate
            if (previous == null || date != previous) {
                streak = if (previous != null && date == previous.plusDays(1)) streak + 1 else 1
                lastDate = date
                if (streak > bestStreak) bestStreak = streak
                if (streak >= Achievement.STREAK_7.target) unlock(Achievement.STREAK_7, at)
            }
            when (local.hour) {
                in 0..3 -> {
                    night++
                    if (night >= Achievement.NIGHT_OWL.target) unlock(Achievement.NIGHT_OWL, at)
                }
                in 4..6 -> {
                    early++
                    if (early >= Achievement.EARLY_BIRD.target) unlock(Achievement.EARLY_BIRD, at)
                }
            }

            ArtistNames.splitGenres(play.genre).forEach { genres.add(ArtistNames.key(it)) }
            if (genres.size >= Achievement.GENRE_HOPPER.target) unlock(Achievement.GENRE_HOPPER, at)

            ArtistNames.split(play.artist).forEach { name ->
                val key = ArtistNames.key(name)
                val artistPlays = (artistCounts[key] ?: 0) + 1
                artistCounts[key] = artistPlays
                if (artistPlays > bestArtist) bestArtist = artistPlays
                if (artistPlays >= Achievement.LOYAL_FAN.target) unlock(Achievement.LOYAL_FAN, at)
            }

            albumOf[play.trackId]?.let { album ->
                val heard = albumHeard.getOrPut(album) { HashSet() }
                heard.add(play.trackId)
                val size = albumSizes[album]
                val percent = heard.size * 100L / size
                if (percent > bestAlbumPercent) bestAlbumPercent = percent
                if (heard.size >= size) unlock(Achievement.ALBUM_FINISHER, at)
            }

            if (play.trackId in libraryIds) {
                explored.add(play.trackId)
                if (libraryIds.isNotEmpty() && explored.size >= libraryIds.size) unlock(Achievement.EXPLORER, at)
            }
        }

        val explorerPercent = if (libraryIds.isEmpty()) 0L else explored.size * 100L / libraryIds.size
        return Achievement.entries.map { achievement ->
            val progress = when (achievement) {
                Achievement.PLAYS_100, Achievement.PLAYS_1000, Achievement.PLAYS_10000 -> count
                Achievement.HOURS_10, Achievement.HOURS_100 -> listened / HOUR_MS
                Achievement.STREAK_7 -> bestStreak.toLong()
                Achievement.NIGHT_OWL -> night
                Achievement.EARLY_BIRD -> early
                Achievement.ALBUM_FINISHER -> bestAlbumPercent
                Achievement.EXPLORER -> explorerPercent
                Achievement.GENRE_HOPPER -> genres.size.toLong()
                Achievement.LOYAL_FAN -> bestArtist.toLong()
            }
            AchievementState(achievement, progress.coerceAtMost(achievement.target), unlocked[achievement])
        }
    }
}

object AchievementCenter {
    private const val PREFS_NAME = "achievements"
    private const val KEY_ANNOUNCED = "announced"
    private const val KEY_INITIALIZED = "initialized"

    var states by mutableStateOf<List<AchievementState>>(emptyList())
        private set
    var loaded by mutableStateOf(false)
        private set
    var pending by mutableStateOf<List<Achievement>>(emptyList())
        private set

    private var computedVersion = -1
    private var computedLibrarySize = -1
    private var computedIndex: LibraryIndex? = null
    private var job: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val unlockedCount: Int get() = states.count { it.unlocked }

    fun unlockedBetween(fromMs: Long, toMs: Long): List<AchievementState> =
        states.filter { state -> state.unlockedAt?.let { it in fromMs until toMs } == true }
            .sortedBy { it.unlockedAt }

    fun refresh(context: Context) {
        val appContext = context.applicationContext
        ListeningHistory.ensureLoaded(appContext)
        val version = ListeningHistory.version
        val librarySize = LibraryStore.tracks.size
        val index = LibraryStore.index
        if (librarySize == 0 || index.albums.isEmpty()) return
        if (version == computedVersion && librarySize == computedLibrarySize && index === computedIndex && loaded) return
        if (job?.isActive == true) return
        job = scope.launch {
            val plays = ListeningHistory.plays(appContext, 0L, Long.MAX_VALUE)
            val library = LibraryStore.tracks.toList()
            val albums = index.albums
            val result = withContext(Dispatchers.Default) { AchievementEngine.compute(plays, library, albums) }
            states = result
            loaded = true
            computedVersion = version
            computedLibrarySize = librarySize
            computedIndex = index
            announce(appContext, result)
            job = null
            if (ListeningHistory.version != version || LibraryStore.tracks.size != librarySize || LibraryStore.index !== index) {
                refresh(appContext)
            }
        }
    }

    fun consume(achievement: Achievement) {
        pending = pending - achievement
    }

    private fun announce(context: Context, result: List<AchievementState>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val unlockedKeys = result.filter { it.unlocked }.map { it.achievement.key }.toSet()
        val announced = prefs.getStringSet(KEY_ANNOUNCED, null)?.toSet() ?: emptySet()
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            prefs.edit().putBoolean(KEY_INITIALIZED, true).putStringSet(KEY_ANNOUNCED, HashSet(unlockedKeys)).apply()
            return
        }
        val fresh = result.filter { it.unlocked && it.achievement.key !in announced }.map { it.achievement }
        if (fresh.isEmpty()) return
        prefs.edit().putStringSet(KEY_ANNOUNCED, HashSet(announced + unlockedKeys)).apply()
        pending = pending + fresh.filterNot { it in pending }
    }
}
