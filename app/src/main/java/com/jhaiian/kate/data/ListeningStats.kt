package com.jhaiian.kate.data

import android.content.Context
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class StatsRange { WEEK, MONTH, YEAR, ALL_TIME }

data class StatsPeriod(val range: StatsRange, val start: LocalDate) {
    val end: LocalDate
        get() = when (range) {
            StatsRange.WEEK -> start.plusWeeks(1)
            StatsRange.MONTH -> start.plusMonths(1)
            StatsRange.YEAR -> start.plusYears(1)
            StatsRange.ALL_TIME -> LocalDate.now().withDayOfMonth(1).plusMonths(1)
        }

    private val monthSpan: Int
        get() = ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1)).toInt().coerceAtLeast(1)

    val groupsByYear: Boolean
        get() = range == StatsRange.ALL_TIME && monthSpan > MAX_MONTH_BUCKETS

    val bucketCount: Int
        get() = when (range) {
            StatsRange.WEEK -> 7
            StatsRange.MONTH -> start.lengthOfMonth()
            StatsRange.YEAR -> 12
            StatsRange.ALL_TIME -> if (groupsByYear) end.minusDays(1).year - start.year + 1 else monthSpan
        }

    val canStep: Boolean get() = range != StatsRange.ALL_TIME

    fun previous(): StatsPeriod = when (range) {
        StatsRange.WEEK -> copy(start = start.minusWeeks(1))
        StatsRange.MONTH -> copy(start = start.minusMonths(1))
        StatsRange.YEAR -> copy(start = start.minusYears(1))
        StatsRange.ALL_TIME -> this
    }

    fun next(): StatsPeriod = when (range) {
        StatsRange.WEEK -> copy(start = start.plusWeeks(1))
        StatsRange.MONTH -> copy(start = start.plusMonths(1))
        StatsRange.YEAR -> copy(start = start.plusYears(1))
        StatsRange.ALL_TIME -> this
    }

    fun contains(date: LocalDate): Boolean = !date.isBefore(start) && date.isBefore(end)

    fun bucketOf(date: LocalDate): Int = when (range) {
        StatsRange.WEEK, StatsRange.MONTH -> (date.toEpochDay() - start.toEpochDay()).toInt()
        StatsRange.YEAR -> date.monthValue - 1
        StatsRange.ALL_TIME -> if (groupsByYear) {
            date.year - start.year
        } else {
            ChronoUnit.MONTHS.between(start.withDayOfMonth(1), date.withDayOfMonth(1)).toInt()
        }
    }

    fun bucketStart(bucket: Int): LocalDate = when (range) {
        StatsRange.WEEK, StatsRange.MONTH -> start.plusDays(bucket.toLong())
        StatsRange.YEAR -> start.plusMonths(bucket.toLong())
        StatsRange.ALL_TIME -> if (groupsByYear) {
            LocalDate.of(start.year + bucket, 1, 1)
        } else {
            start.withDayOfMonth(1).plusMonths(bucket.toLong())
        }
    }

    fun startMillis(zone: ZoneId): Long = start.atStartOfDay(zone).toInstant().toEpochMilli()

    fun endMillis(zone: ZoneId): Long = end.atStartOfDay(zone).toInstant().toEpochMilli()

    companion object {
        private const val MAX_MONTH_BUCKETS = 24

        fun containing(range: StatsRange, date: LocalDate, locale: Locale = Locale.getDefault()): StatsPeriod = when (range) {
            StatsRange.WEEK -> {
                val firstDay: DayOfWeek = WeekFields.of(locale).firstDayOfWeek
                StatsPeriod(range, date.with(TemporalAdjusters.previousOrSame(firstDay)))
            }
            StatsRange.MONTH -> StatsPeriod(range, date.withDayOfMonth(1))
            StatsRange.YEAR -> StatsPeriod(range, date.withDayOfYear(1))
            StatsRange.ALL_TIME -> StatsPeriod(range, date.withDayOfMonth(1))
        }
    }
}

sealed interface WrappedAvailability {
    data object Ready : WrappedAvailability
    data class Locked(val unlocksOn: LocalDate) : WrappedAvailability
    data object NotEnough : WrappedAvailability
    data object Unavailable : WrappedAvailability
}

data class RankedItem(
    val key: String,
    val name: String,
    val subtitle: String,
    val plays: Int,
    val listenedMs: Long,
    val trackId: String?
)

data class ListeningStats(
    val period: StatsPeriod,
    val totalMs: Long,
    val plays: Int,
    val uniqueSongs: Int,
    val uniqueArtists: Int,
    val previousTotalMs: Long,
    val topSongs: List<RankedItem>,
    val topArtists: List<RankedItem>,
    val topAlbums: List<RankedItem>,
    val topGenres: List<RankedItem>,
    val buckets: List<Long>,
    val hours: List<Long>,
    val biggestDay: LocalDate?,
    val biggestDayMs: Long,
    val newArtists: List<String>,
    val newSongs: Int,
    val hasEarlierHistory: Boolean
) {
    val isEmpty: Boolean get() = plays == 0

    val peakHour: Int? get() = hours.indices.maxByOrNull { hours[it] }?.takeIf { hours[it] > 0L }

    val changePercent: Int?
        get() = if (previousTotalMs <= 0L) null else (((totalMs - previousTotalMs) * 100) / previousTotalMs).toInt()

    val genreTotalMs: Long get() = topGenres.sumOf { it.listenedMs }
}

fun ListeningStats.wrappedAvailability(today: LocalDate = LocalDate.now()): WrappedAvailability {
    val unlocksOn = when (period.range) {
        StatsRange.WEEK, StatsRange.MONTH, StatsRange.YEAR -> period.end
        StatsRange.ALL_TIME -> return WrappedAvailability.Unavailable
    }
    if (today.isBefore(unlocksOn)) return WrappedAvailability.Locked(unlocksOn)
    val minimumPlays = when (period.range) {
        StatsRange.WEEK -> 5
        StatsRange.MONTH -> 15
        else -> 30
    }
    return if (plays >= minimumPlays) WrappedAvailability.Ready else WrappedAvailability.NotEnough
}

object ListeningStatsRepository {
    private const val TOP_LIMIT = 10

    private val Whitespace = Regex("\\s+")

    suspend fun load(context: Context, period: StatsPeriod, config: TagConfig, library: List<Track>): ListeningStats {
        val zone = ZoneId.systemDefault()
        val from = period.startMillis(zone)
        val to = period.endMillis(zone)
        val previous = period.previous()
        val plays = ListeningHistory.plays(context, from, to)
        val previousTotal = ListeningHistory.listenedBetween(context, previous.startMillis(zone), from)
        val knownSongs = if (plays.isEmpty()) emptyList() else ListeningHistory.songsBefore(context, from)
        val knownArtistTags = if (plays.isEmpty()) emptySet() else ListeningHistory.distinctBefore(context, ListeningHistory.COLUMN_ARTIST, from)
        return withContext(Dispatchers.Default) {
            compute(period, zone, plays, previousTotal, knownSongs, knownArtistTags, config, library)
        }
    }

    suspend fun earliestPeriod(context: Context, range: StatsRange): StatsPeriod? {
        val first = ListeningHistory.firstPlayAt(context) ?: return null
        val date = Instant.ofEpochMilli(first).atZone(ZoneId.systemDefault()).toLocalDate()
        return StatsPeriod.containing(range, date)
    }

    private class Tally(val name: String, var subtitle: String, var trackId: String?) {
        var plays = 0
        var listenedMs = 0L

        fun add(record: PlayRecord) {
            plays++
            listenedMs += record.listenedMs
        }

        fun prefer(candidate: String, libraryIds: Set<String>) {
            if (trackId !in libraryIds && candidate in libraryIds) trackId = candidate
        }

        fun toItem(key: String) = RankedItem(key, name, subtitle, plays, listenedMs, trackId)
    }

    private fun normalize(value: String): String = value.trim().lowercase().replace(Whitespace, " ")

    private fun songKey(title: String, artist: String, config: TagConfig): String {
        val artists = ArtistNames.split(artist, config).map { ArtistNames.key(it) }.sorted()
        val artistPart = if (artists.isEmpty()) normalize(artist) else artists.joinToString(",")
        return normalize(title) + '\u001F' + artistPart
    }

    private fun compute(
        period: StatsPeriod,
        zone: ZoneId,
        plays: List<PlayRecord>,
        previousTotalMs: Long,
        knownSongs: List<Pair<String, String>>,
        knownArtistTags: Set<String>,
        config: TagConfig,
        library: List<Track>
    ): ListeningStats {
        val libraryIds = HashSet<String>(library.size)
        val libraryByKey = HashMap<String, String>(library.size)
        library.forEach { track ->
            libraryIds.add(track.id)
            libraryByKey.putIfAbsent(songKey(track.title, track.artist, config), track.id)
        }
        val knownSongKeys = knownSongs.mapTo(HashSet()) { (title, artist) -> songKey(title, artist, config) }
        val songs = LinkedHashMap<String, Tally>()
        val artists = LinkedHashMap<String, Tally>()
        val albums = LinkedHashMap<String, Tally>()
        val genres = LinkedHashMap<String, Tally>()
        val buckets = LongArray(period.bucketCount)
        val hours = LongArray(24)
        val days = HashMap<LocalDate, Long>()
        val knownArtists = HashSet<String>()
        knownArtistTags.forEach { tag -> ArtistNames.split(tag, config).forEach { knownArtists.add(ArtistNames.key(it)) } }
        val newArtists = LinkedHashMap<String, String>()
        val newSongs = HashSet<String>()
        var total = 0L

        for (record in plays) {
            total += record.listenedMs
            val time = Instant.ofEpochMilli(record.startedAt).atZone(zone)
            val date = time.toLocalDate()
            if (period.contains(date)) {
                val bucket = period.bucketOf(date)
                if (bucket in buckets.indices) buckets[bucket] += record.listenedMs
            }
            hours[time.hour] += record.listenedMs
            days[date] = (days[date] ?: 0L) + record.listenedMs

            val songId = songKey(record.title, record.artist, config)
            val trackId = if (record.trackId in libraryIds) record.trackId else libraryByKey[songId] ?: record.trackId
            songs.getOrPut(songId) { Tally(record.title, record.artist, trackId) }.apply {
                prefer(trackId, libraryIds)
                add(record)
            }
            if (songId !in knownSongKeys) newSongs.add(songId)

            val artistNames = ArtistNames.split(record.artist, config)
            artistNames.forEach { name ->
                val key = ArtistNames.key(name)
                artists.getOrPut(key) { Tally(name, "", trackId) }.apply {
                    prefer(trackId, libraryIds)
                    add(record)
                }
                if (key !in knownArtists && key !in newArtists) newArtists[key] = name
            }

            if (record.album.isNotBlank()) {
                val owner = record.albumArtist.ifBlank { artistNames.firstOrNull().orEmpty() }
                val mainOwner = ArtistNames.split(owner, config).firstOrNull() ?: owner
                val key = ArtistNames.key(record.album) + '\u001F' + ArtistNames.key(mainOwner)
                albums.getOrPut(key) { Tally(record.album, owner, trackId) }.apply {
                    prefer(trackId, libraryIds)
                    add(record)
                }
            }

            ArtistNames.splitGenres(record.genre, config).forEach { name ->
                genres.getOrPut(ArtistNames.key(name)) { Tally(name, "", trackId) }.apply {
                    prefer(trackId, libraryIds)
                    add(record)
                }
            }
        }

        val ranking = compareByDescending<RankedItem> { it.plays }.thenByDescending { it.listenedMs }.thenBy { it.name.lowercase() }
        val biggest = days.maxByOrNull { it.value }

        return ListeningStats(
            period = period,
            totalMs = total,
            plays = plays.size,
            uniqueSongs = songs.size,
            uniqueArtists = artists.size,
            previousTotalMs = previousTotalMs,
            topSongs = songs.map { (key, tally) -> tally.toItem(key) }.sortedWith(ranking).take(TOP_LIMIT),
            topArtists = artists.map { (key, tally) -> tally.toItem(key) }.sortedWith(ranking).take(TOP_LIMIT),
            topAlbums = albums.map { (key, tally) -> tally.toItem(key) }.sortedWith(ranking).take(TOP_LIMIT),
            topGenres = genres.map { (key, tally) -> tally.toItem(key) }
                .sortedWith(compareByDescending<RankedItem> { it.listenedMs }.thenBy { it.name.lowercase() })
                .take(TOP_LIMIT),
            buckets = buckets.toList(),
            hours = hours.toList(),
            biggestDay = biggest?.key,
            biggestDayMs = biggest?.value ?: 0L,
            newArtists = newArtists.values.toList(),
            newSongs = newSongs.size,
            hasEarlierHistory = knownSongKeys.isNotEmpty()
        )
    }
}
