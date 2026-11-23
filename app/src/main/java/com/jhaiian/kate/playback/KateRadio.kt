package com.jhaiian.kate.playback

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jhaiian.kate.R
import com.jhaiian.kate.data.ArtistNames
import com.jhaiian.kate.data.LibraryCategory
import com.jhaiian.kate.data.LibraryStore
import com.jhaiian.kate.data.ListeningHistory
import com.jhaiian.kate.data.PlayCounts
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.data.TrackPlays
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.random.Random

enum class RadioSeedType(val key: String) {
    SONG("song"),
    ARTIST("artist"),
    ALBUM("album"),
    GENRE("genre");

    companion object {
        fun fromKey(key: String?): RadioSeedType? = entries.firstOrNull { it.key == key }
    }
}

data class RadioSeed(val type: RadioSeedType, val key: String, val label: String)

private class RadioProfile(
    val seedIds: Set<String>,
    val genres: Map<String, Double>,
    val artists: Set<String>,
    val related: Set<String>,
    val albums: Set<String>,
    val years: List<Int>,
    val coListened: Map<String, Int>
)

object KateRadio {
    private const val PREFS_NAME = "kate_radio"
    private const val KEY_ADVENTURE = "adventure"
    private const val KEY_SEED_TYPE = "seed_type"
    private const val KEY_SEED_KEY = "seed_key"
    private const val KEY_SEED_LABEL = "seed_label"
    private const val FIRST_BATCH = 25
    private const val REFILL_BATCH = 15
    private const val REFILL_WHEN_LEFT = 5
    private const val NEIGHBOR_WINDOW = 3
    private const val NEIGHBOR_GAP_MS = 45 * 60_000L
    private const val RECENT_MS = 3 * 3_600_000L
    const val DEFAULT_ADVENTURE = 0.35f

    var session by mutableStateOf<RadioSeed?>(null)
        private set
    var adventure by mutableFloatStateOf(DEFAULT_ADVENTURE)
        private set
    var starting by mutableStateOf(false)
        private set

    private var loaded = false
    private var profile: RadioProfile? = null
    private var profileSeed: RadioSeed? = null
    private var refillJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        val prefs = prefs(context)
        adventure = prefs.getFloat(KEY_ADVENTURE, DEFAULT_ADVENTURE).coerceIn(0f, 1f)
        val type = RadioSeedType.fromKey(prefs.getString(KEY_SEED_TYPE, null))
        val key = prefs.getString(KEY_SEED_KEY, null)
        if (type != null && key != null) {
            session = RadioSeed(type, key, prefs.getString(KEY_SEED_LABEL, null).orEmpty())
        }
    }

    fun start(context: Context, seed: RadioSeed, mix: Float) {
        val appContext = context.applicationContext
        ensureLoaded(appContext)
        saveAdventure(appContext, mix)
        refillJob?.cancel()
        refillJob = null
        starting = true
        scope.launch {
            LibraryStore.ensureLoaded(appContext)
            PlayCounts.refresh(appContext)
            val library = LibraryStore.tracks.toList()
            val built = buildProfile(appContext, seed, library)
            if (built == null) {
                starting = false
                Toast.makeText(appContext, R.string.radio_not_enough, Toast.LENGTH_SHORT).show()
                return@launch
            }
            val first = when (seed.type) {
                RadioSeedType.SONG -> library.firstOrNull { it.id == seed.key }
                else -> library.filter { it.id in built.seedIds }.randomOrNull()
            }
            val plays = PlayCounts.stats
            val picks = withContext(Dispatchers.Default) {
                pick(built, library, listOfNotNull(first), FIRST_BATCH - 1, adventure, plays)
            }
            val tracks = listOfNotNull(first) + picks
            starting = false
            if (tracks.size < 2) {
                Toast.makeText(appContext, R.string.radio_not_enough, Toast.LENGTH_SHORT).show()
                return@launch
            }
            profile = built
            profileSeed = seed
            PlayerController.startRadioQueue(appContext, tracks)
            setSession(appContext, seed)
        }
    }

    fun applyMix(context: Context, mix: Float) {
        val appContext = context.applicationContext
        val changed = mix != adventure
        saveAdventure(appContext, mix)
        if (!changed || session == null) return
        refill(appContext, replaceUpcoming = true)
    }

    fun stop(context: Context?) {
        refillJob?.cancel()
        refillJob = null
        profile = null
        profileSeed = null
        if (session == null) return
        session = null
        context?.let { clearSaved(it) }
    }

    internal fun onQueueReplaced(context: Context) {
        stop(context)
    }

    internal fun onTrackChanged(context: Context) {
        if (session == null) return
        val current = PlayerController.currentQueueIndex()
        val left = PlayerController.queue.size - current - 1
        if (current >= 0 && left < REFILL_WHEN_LEFT) refill(context.applicationContext, replaceUpcoming = false)
    }

    private fun refill(context: Context, replaceUpcoming: Boolean) {
        val seed = session ?: return
        if (refillJob?.isActive == true && !replaceUpcoming) return
        refillJob?.cancel()
        refillJob = scope.launch {
            val library = LibraryStore.tracks.toList()
            val built = if (profileSeed == seed) profile else buildProfile(context, seed, library)
            if (built == null) return@launch
            profile = built
            profileSeed = seed
            val current = PlayerController.currentQueueIndex().coerceAtLeast(0)
            val history = if (replaceUpcoming) PlayerController.queue.take(current + 1) else PlayerController.queue
            val count = if (replaceUpcoming) FIRST_BATCH - 1 else REFILL_BATCH
            val plays = PlayCounts.stats
            val mix = adventure
            val picks = withContext(Dispatchers.Default) { pick(built, library, history, count, mix, plays) }
            if (picks.isEmpty() || session != seed) return@launch
            if (replaceUpcoming) {
                PlayerController.replaceUpcoming(context, picks)
            } else {
                PlayerController.appendRadioTracks(context, picks)
            }
        }
    }

    private suspend fun buildProfile(context: Context, seed: RadioSeed, library: List<Track>): RadioProfile? {
        val index = LibraryStore.index
        val seedTracks = when (seed.type) {
            RadioSeedType.SONG -> library.filter { it.id == seed.key }
            RadioSeedType.ARTIST -> (index.artist(seed.key, false) ?: index.artist(seed.key, true))?.tracks.orEmpty()
            RadioSeedType.ALBUM -> index.album(seed.key)?.tracks.orEmpty()
            RadioSeedType.GENRE -> index.group(LibraryCategory.GENRES, seed.key)?.tracks.orEmpty()
        }
        if (seedTracks.isEmpty() || library.size < 2) return null
        val sequence = ListeningHistory.playSequence(context)
        return withContext(Dispatchers.Default) {
            val seedIds = seedTracks.mapTo(HashSet()) { it.id }
            val genreCounts = HashMap<String, Int>()
            seedTracks.forEach { track -> genresOf(track).forEach { genreCounts[it] = (genreCounts[it] ?: 0) + 1 } }
            val genreMax = (genreCounts.values.maxOrNull() ?: 1).toDouble()
            val genres = genreCounts.mapValues { (_, count) -> count / genreMax }
            val artists = HashSet<String>()
            seedTracks.forEach { artists.addAll(artistsOf(it)) }
            val related = HashSet<String>()
            library.forEach { track ->
                val credited = artistsOf(track)
                if (credited.any { it in artists }) related.addAll(credited)
            }
            related.removeAll(artists)
            val coListened = HashMap<String, Int>()
            sequence.forEachIndexed { position, (id, at) ->
                if (id !in seedIds) return@forEachIndexed
                val from = (position - NEIGHBOR_WINDOW).coerceAtLeast(0)
                val to = (position + NEIGHBOR_WINDOW).coerceAtMost(sequence.lastIndex)
                for (other in from..to) {
                    if (other == position) continue
                    val (otherId, otherAt) = sequence[other]
                    if (otherId in seedIds || abs(otherAt - at) > NEIGHBOR_GAP_MS) continue
                    coListened[otherId] = (coListened[otherId] ?: 0) + 1
                }
            }
            val byId = library.associateBy { it.id }
            coListened.keys.forEach { id ->
                byId[id]?.let { related.addAll(artistsOf(it) - artists) }
            }
            RadioProfile(
                seedIds = seedIds,
                genres = genres,
                artists = artists,
                related = related,
                albums = seedTracks.mapTo(HashSet()) { albumOf(it) },
                years = seedTracks.mapNotNull { it.year?.takeIf { year -> year > 0 } },
                coListened = coListened
            )
        }
    }

    private fun pick(
        profile: RadioProfile,
        library: List<Track>,
        history: List<Track>,
        count: Int,
        mix: Float,
        plays: Map<String, TrackPlays>
    ): List<Track> {
        if (count <= 0) return emptyList()
        val now = System.currentTimeMillis()
        val used = history.mapTo(HashSet()) { it.id }
        var pool = library.filter { it.id !in used }
        if (pool.size < count) {
            val keep = history.takeLast(20).mapTo(HashSet()) { it.id }
            pool = library.filter { it.id !in keep }
        }
        if (pool.isEmpty()) return emptyList()
        val temperature = 0.25 + mix * 2.75
        val base = pool.associate { it.id to score(profile, it, mix) }
        val picks = ArrayList<Track>(count)
        val recentArtists = ArrayDeque<String>()
        history.takeLast(4).forEach { recentArtists.addLast(primaryArtist(it)) }
        val available = pool.toMutableList()
        val random = Random.Default
        repeat(count) {
            if (available.isEmpty()) return picks
            val weights = DoubleArray(available.size)
            var total = 0.0
            available.forEachIndexed { position, track ->
                var weight = exp((base[track.id] ?: 0.0) / temperature)
                val last = plays[track.id]?.lastPlayedAt
                if (last != null && now - last < RECENT_MS) weight *= 0.15
                val artist = primaryArtist(track)
                if (recentArtists.lastOrNull() == artist) weight *= 0.1 else if (artist in recentArtists) weight *= 0.4
                weights[position] = weight
                total += weight
            }
            var target = random.nextDouble() * total
            var chosen = available.lastIndex
            for (position in weights.indices) {
                target -= weights[position]
                if (target <= 0.0) {
                    chosen = position
                    break
                }
            }
            val track = available.removeAt(chosen)
            picks.add(track)
            recentArtists.addLast(primaryArtist(track))
            if (recentArtists.size > 4) recentArtists.removeFirst()
        }
        return picks
    }

    private fun score(profile: RadioProfile, track: Track, mix: Float): Double {
        var score = 0.0
        val genres = genresOf(track)
        if (genres.isNotEmpty() && profile.genres.isNotEmpty()) {
            score += min(1.0, genres.sumOf { profile.genres[it] ?: 0.0 }) * 3.0
        }
        val artists = artistsOf(track)
        val sameArtist = artists.any { it in profile.artists }
        if (sameArtist) {
            score += 2.5 * (1.0 - mix * 0.6)
        } else if (artists.any { it in profile.related }) {
            score += 1.6
        }
        if (albumOf(track) in profile.albums) score += 0.6 * (1.0 - mix)
        val year = track.year
        if (year != null && year > 0 && profile.years.isNotEmpty()) {
            val distance = profile.years.minOf { abs(it - year) }
            score += exp(-distance / 5.0) * 1.2
        }
        val together = profile.coListened[track.id] ?: 0
        if (together > 0) score += min(1.0, together / 3.0) * 2.0
        if (track.id in profile.seedIds) score += 1.0 * (1.0 - mix)
        return score
    }

    private fun genresOf(track: Track): List<String> = ArtistNames.splitGenres(track.genre).map { ArtistNames.key(it) }

    private fun artistsOf(track: Track): Set<String> =
        (ArtistNames.split(track.artist) + ArtistNames.split(track.albumArtist)).mapTo(HashSet()) { ArtistNames.key(it) }

    private fun primaryArtist(track: Track): String =
        ArtistNames.split(track.artist).firstOrNull()?.let { ArtistNames.key(it) } ?: ArtistNames.key(track.artist)

    private fun albumOf(track: Track): String =
        ArtistNames.key(track.album) + "\u0000" + ArtistNames.key(track.albumArtist.ifBlank { track.artist })

    private fun saveAdventure(context: Context, value: Float) {
        adventure = value.coerceIn(0f, 1f)
        prefs(context).edit().putFloat(KEY_ADVENTURE, adventure).apply()
    }

    private fun setSession(context: Context, seed: RadioSeed) {
        session = seed
        prefs(context).edit()
            .putString(KEY_SEED_TYPE, seed.type.key)
            .putString(KEY_SEED_KEY, seed.key)
            .putString(KEY_SEED_LABEL, seed.label)
            .apply()
    }

    private fun clearSaved(context: Context) {
        prefs(context).edit()
            .remove(KEY_SEED_TYPE)
            .remove(KEY_SEED_KEY)
            .remove(KEY_SEED_LABEL)
            .apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
