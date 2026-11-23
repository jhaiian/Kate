package com.jhaiian.kate.playback

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jhaiian.kate.data.ArtistNames
import com.jhaiian.kate.data.Track
import com.jhaiian.kate.data.TrackPlays
import kotlin.math.ln
import kotlin.random.Random

enum class ShuffleMode(val key: String) {
    RANDOM("random"),
    SPREAD_ARTISTS("spread_artists"),
    LESS_REPEATS("less_repeats"),
    DISCOVERY("discovery"),
    ALBUMS("albums"),
    ALBUMS_MIXED("albums_mixed");

    val usesPlays: Boolean get() = this == LESS_REPEATS || this == DISCOVERY

    companion object {
        fun fromKey(key: String?): ShuffleMode = entries.firstOrNull { it.key == key } ?: RANDOM
    }
}

object ShufflePreferences {
    private const val PREFS_NAME = "shuffle_preferences"
    private const val KEY_MODE = "mode"

    var mode by mutableStateOf(ShuffleMode.RANDOM)
        private set
    private var loaded = false

    fun ensureLoaded(context: Context) {
        if (loaded) return
        loaded = true
        mode = ShuffleMode.fromKey(prefs(context).getString(KEY_MODE, null))
    }

    fun setMode(context: Context, value: ShuffleMode) {
        ensureLoaded(context)
        mode = value
        prefs(context).edit().putString(KEY_MODE, value.key).apply()
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

object SmartShuffle {
    private const val HOUR_MS = 3_600_000L
    private const val DAY_MS = 24 * HOUR_MS

    fun arrange(
        tracks: List<Track>,
        mode: ShuffleMode,
        plays: Map<String, TrackPlays>,
        now: Long = System.currentTimeMillis(),
        random: Random = Random.Default
    ): List<Track> {
        if (tracks.size < 2) return tracks
        return when (mode) {
            ShuffleMode.RANDOM -> tracks.shuffled(random)
            ShuffleMode.SPREAD_ARTISTS -> spreadArtists(tracks, random)
            ShuffleMode.LESS_REPEATS -> weighted(tracks, random) { recencyWeight(plays[it.id], now) }
            ShuffleMode.DISCOVERY -> weighted(tracks, random) { discoveryWeight(plays[it.id]) }
            ShuffleMode.ALBUMS -> albums(tracks, random)
            ShuffleMode.ALBUMS_MIXED -> albumsMixed(tracks, random)
        }
    }

    fun arrangeFrom(
        tracks: List<Track>,
        first: Track,
        mode: ShuffleMode,
        plays: Map<String, TrackPlays>,
        now: Long = System.currentTimeMillis(),
        random: Random = Random.Default
    ): List<Track> {
        if (mode == ShuffleMode.ALBUMS_MIXED) {
            val albumKey = albumKey(first)
            val lead = listOf(first) + tracks.filter { albumKey(it) == albumKey && it.id != first.id }.shuffled(random)
            val leadIds = lead.mapTo(HashSet()) { it.id }
            return lead + albumsMixed(tracks.filterNot { it.id in leadIds }, random)
        }
        if (mode == ShuffleMode.ALBUMS) {
            val albumKey = albumKey(first)
            val sameAlbum = tracks.filter { albumKey(it) == albumKey }.sortedWith(AlbumOrder)
            val start = sameAlbum.indexOfFirst { it.id == first.id }.coerceAtLeast(0)
            val lead = listOf(first) + sameAlbum.drop(start + 1).filterNot { it.id == first.id }
            val leadIds = lead.mapTo(HashSet()) { it.id }
            return lead + albums(tracks.filterNot { it.id in leadIds }, random)
        }
        val rest = tracks.filterNot { it.id == first.id }
        val arranged = arrange(rest, mode, plays, now, random)
        if (mode != ShuffleMode.SPREAD_ARTISTS || arranged.isEmpty()) return listOf(first) + arranged
        val firstArtist = artistKey(first)
        if (artistKey(arranged.first()) != firstArtist) return listOf(first) + arranged
        val swap = arranged.indexOfFirst { artistKey(it) != firstArtist }
        if (swap <= 0) return listOf(first) + arranged
        val fixed = arranged.toMutableList()
        fixed.add(0, fixed.removeAt(swap))
        return listOf(first) + fixed
    }

    private val AlbumOrder = compareBy<Track> { it.discNumber ?: 0 }
        .thenBy { it.trackNumber ?: Int.MAX_VALUE }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }

    private fun artistKey(track: Track): String =
        ArtistNames.split(track.artist).firstOrNull()?.let { ArtistNames.key(it) } ?: ArtistNames.key(track.artist)

    private fun albumKey(track: Track): String =
        ArtistNames.key(track.album) + "\u0000" + ArtistNames.key(track.albumArtist.ifBlank { track.artist })

    private fun spreadArtists(tracks: List<Track>, random: Random): List<Track> {
        val keys = HashMap<String, String>(tracks.size)
        tracks.forEach { keys[it.id] = artistKey(it) }
        val groups = tracks.groupBy { keys.getValue(it.id) }.values.map { it.shuffled(random) }
        val placed = ArrayList<Pair<Double, Track>>(tracks.size)
        groups.forEach { group ->
            val size = group.size
            val offset = random.nextDouble() / size
            group.forEachIndexed { index, track ->
                val jitter = (random.nextDouble() - 0.5) * 0.2 / size
                placed.add((offset + index.toDouble() / size + jitter) to track)
            }
        }
        val ordered = placed.sortedBy { it.first }.map { it.second }.toMutableList()
        for (index in 1 until ordered.size) {
            val previous = keys.getValue(ordered[index - 1].id)
            if (keys.getValue(ordered[index].id) != previous) continue
            var swap = -1
            for (candidate in index + 1 until ordered.size) {
                if (keys.getValue(ordered[candidate].id) != previous) {
                    swap = candidate
                    break
                }
            }
            if (swap < 0) break
            ordered.add(index, ordered.removeAt(swap))
        }
        return ordered
    }

    private inline fun weighted(tracks: List<Track>, random: Random, weight: (Track) -> Double): List<Track> =
        tracks.map { track ->
            val w = weight(track).coerceAtLeast(0.0001)
            val u = random.nextDouble().coerceAtLeast(1e-12)
            (ln(u) / w) to track
        }.sortedByDescending { it.first }.map { it.second }

    private fun recencyWeight(plays: TrackPlays?, now: Long): Double {
        val last = plays?.lastPlayedAt ?: return 1.0
        val age = now - last
        return when {
            age < 6 * HOUR_MS -> 0.03
            age < DAY_MS -> 0.08
            age < 3 * DAY_MS -> 0.2
            age < 7 * DAY_MS -> 0.4
            age < 30 * DAY_MS -> 0.7
            else -> 1.0
        }
    }

    private fun discoveryWeight(plays: TrackPlays?): Double {
        val count = plays?.count ?: 0
        return if (count == 0) 3.0 else 1.0 / (1.0 + count)
    }

    private fun albumsMixed(tracks: List<Track>, random: Random): List<Track> =
        tracks.groupBy { albumKey(it) }
            .values
            .shuffled(random)
            .flatMap { it.shuffled(random) }

    private fun albums(tracks: List<Track>, random: Random): List<Track> =
        tracks.groupBy { albumKey(it) }
            .values
            .shuffled(random)
            .flatMap { it.sortedWith(AlbumOrder) }
}
