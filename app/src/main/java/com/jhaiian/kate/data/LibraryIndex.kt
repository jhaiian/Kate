package com.jhaiian.kate.data

data class AlbumGroup(
    val key: String,
    val title: String,
    val artist: String?,
    val year: Int?,
    val tracks: List<Track>
) {
    val artTrack: Track? get() = tracks.firstOrNull { it.artKey != null } ?: tracks.firstOrNull()
    val durationMs: Long get() = tracks.sumOf { it.durationMs }
}

data class ArtistGroup(
    val key: String,
    val name: String,
    val albums: List<AlbumGroup>,
    val tracks: List<Track>
) {
    val artTrack: Track? get() = tracks.firstOrNull { it.artKey != null } ?: tracks.firstOrNull()
}

data class TrackGroup(
    val key: String,
    val name: String,
    val detail: String,
    val tracks: List<Track>
) {
    val artTrack: Track? get() = tracks.firstOrNull { it.artKey != null } ?: tracks.firstOrNull()
}

data class LibraryIndex(
    val albums: List<AlbumGroup>,
    val artists: List<ArtistGroup>,
    val albumArtists: List<ArtistGroup>,
    val genres: List<TrackGroup>,
    val folders: List<TrackGroup>,
    val years: List<TrackGroup>,
    val recentlyAdded: List<Track>
) {
    fun album(key: String): AlbumGroup? = albums.firstOrNull { it.key == key }

    fun artist(key: String, albumArtist: Boolean): ArtistGroup? =
        (if (albumArtist) albumArtists else artists).firstOrNull { it.key == key }

    fun groups(category: LibraryCategory): List<TrackGroup> = when (category) {
        LibraryCategory.GENRES -> genres
        LibraryCategory.FOLDERS -> folders
        LibraryCategory.YEARS -> years
        else -> emptyList()
    }

    fun group(category: LibraryCategory, key: String): TrackGroup? = groups(category).firstOrNull { it.key == key }

    fun count(category: LibraryCategory, songCount: Int, favoriteCount: Int): Int = when (category) {
        LibraryCategory.SONGS -> songCount
        LibraryCategory.ALBUMS -> albums.size
        LibraryCategory.ARTISTS -> artists.size
        LibraryCategory.ALBUM_ARTISTS -> albumArtists.size
        LibraryCategory.GENRES -> genres.size
        LibraryCategory.FOLDERS -> folders.size
        LibraryCategory.YEARS -> years.size
        LibraryCategory.RECENTLY_ADDED -> recentlyAdded.size
        LibraryCategory.FAVORITES -> favoriteCount
        LibraryCategory.PLAYLISTS -> PlaylistStore.playlists.size
    }

    companion object {
        private const val RECENTLY_ADDED_LIMIT = 100
        private const val KEY_SEPARATOR = '\u001F'

        val Empty = LibraryIndex(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())

        fun build(tracks: List<Track>, config: TagConfig = TagSettings.config): LibraryIndex {
            if (tracks.isEmpty()) return Empty
            val artistKeys = HashMap<String, List<String>>(tracks.size)
            val artistNames = HashMap<String, String>()
            val artistTracks = LinkedHashMap<String, MutableList<Track>>()
            for (track in tracks) {
                val names = ArtistNames.split(track.artist, config).ifEmpty { listOf("") }
                val keys = names.map { name ->
                    val key = ArtistNames.key(name)
                    artistNames.putIfAbsent(key, name)
                    artistTracks.getOrPut(key) { ArrayList() }.add(track)
                    key
                }
                artistKeys[track.id] = keys
            }

            val albumBuckets = LinkedHashMap<String, MutableList<Track>>()
            val bucketTitles = HashMap<String, String>()
            val bucketArtists = HashMap<String, MutableSet<String>>()
            val bucketByFolder = HashMap<String, String>()
            val untagged = ArrayList<Track>()
            for (track in tracks) {
                val owner = albumOwner(track.albumArtist, config)
                if (owner.isEmpty()) {
                    untagged.add(track)
                    continue
                }
                val titleKey = ArtistNames.key(track.album)
                val key = titleKey + KEY_SEPARATOR + ArtistNames.key(owner)
                albumBuckets.getOrPut(key) { ArrayList() }.add(track)
                bucketTitles[key] = titleKey
                bucketArtists.getOrPut(key) { HashSet() }
                    .addAll(ArtistNames.split(track.albumArtist, config).map { ArtistNames.key(it) })
                bucketByFolder.putIfAbsent(titleKey + KEY_SEPARATOR + folderOf(track), key)
            }
            val redirects = HashMap<String, String>()
            if (config.groupAlbumsByMainArtist) {
                mergeSharedArtists(albumBuckets, bucketTitles, bucketArtists, redirects)
            }
            for (track in untagged) {
                val titleKey = ArtistNames.key(track.album)
                val folder = folderOf(track)
                val byArtist = titleKey + KEY_SEPARATOR + ArtistNames.key(primaryArtist(track.artist, config))
                val key = bucketByFolder[titleKey + KEY_SEPARATOR + folder]?.let { redirects[it] ?: it }
                    ?: (redirects[byArtist] ?: byArtist).takeIf {
                        config.groupAlbumsByMainArtist && titleKey.isNotEmpty() && albumBuckets.containsKey(it)
                    }
                    ?: (titleKey + KEY_SEPARATOR + "\u0002" + folder)
                albumBuckets.getOrPut(key) { ArrayList() }.add(track)
            }
            val albums = albumBuckets.map { (key, albumTracks) ->
                val first = albumTracks.first()
                val taggedArtists = albumTracks.map { it.albumArtist.trim() }.filter { it.isNotEmpty() }
                val artist = if (taggedArtists.isNotEmpty()) {
                    taggedArtists.groupingBy { it }.eachCount().entries
                        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.length })
                        .first().key
                } else {
                    val primary = albumTracks.map { ArtistNames.split(it.artist, config).firstOrNull().orEmpty() }.distinctBy { it.lowercase() }
                    if (primary.size == 1) primary.first() else null
                }
                AlbumGroup(
                    key = key,
                    title = first.album.trim(),
                    artist = artist,
                    year = albumTracks.mapNotNull { it.year }.minOrNull(),
                    tracks = sortTracks(albumTracks, LibrarySortOrder.DISC_TRACK)
                )
            }.sortedWith(compareBy<AlbumGroup> { it.title.isEmpty() }.thenBy { it.title.lowercase() }.thenBy { it.artist.orEmpty().lowercase() })

            val albumsByArtist = HashMap<String, MutableList<AlbumGroup>>()
            for (album in albums) {
                album.tracks.flatMap { artistKeys[it.id].orEmpty() }.distinct().forEach { key ->
                    albumsByArtist.getOrPut(key) { ArrayList() }.add(album)
                }
            }
            val artists = artistTracks.map { (key, list) ->
                val artistAlbums = albumsByArtist[key].orEmpty().sortedWith(albumYearOrder)
                ArtistGroup(
                    key = key,
                    name = artistNames[key].orEmpty(),
                    albums = artistAlbums,
                    tracks = sortTracks(list, LibrarySortOrder.YEAR_ALBUM)
                )
            }.sortedWith(compareBy<ArtistGroup> { it.name.isEmpty() }.thenBy { it.name.lowercase() })

            val albumArtists = albums
                .filter { !it.artist.isNullOrBlank() }
                .groupBy { ArtistNames.key(it.artist.orEmpty()) }
                .map { (key, ownAlbums) ->
                    val sortedAlbums = ownAlbums.sortedWith(albumYearOrder)
                    ArtistGroup(
                        key = key,
                        name = ownAlbums.first().artist.orEmpty(),
                        albums = sortedAlbums,
                        tracks = sortedAlbums.flatMap { it.tracks }
                    )
                }
                .sortedBy { it.name.lowercase() }

            val genreNames = HashMap<String, String>()
            val genreTracks = LinkedHashMap<String, MutableList<Track>>()
            for (track in tracks) {
                val names = ArtistNames.splitGenres(track.genre, config).ifEmpty { listOf("") }
                for (name in names) {
                    val key = ArtistNames.key(name)
                    genreNames.putIfAbsent(key, name)
                    genreTracks.getOrPut(key) { ArrayList() }.add(track)
                }
            }
            val genres = genreTracks.map { (key, list) -> TrackGroup(key, genreNames[key].orEmpty(), "", list) }
                .sortedWith(compareBy<TrackGroup> { it.name.isEmpty() }.thenBy { it.name.lowercase() })

            val folders = tracks.groupBy { folderOf(it) }
                .map { (folder, list) -> TrackGroup(folder, folderName(folder), folderDetail(folder), list) }
                .sortedBy { it.detail.lowercase() }

            val years = tracks.groupBy { it.year?.toString().orEmpty() }
                .map { (year, list) -> TrackGroup(year, year, "", list) }
                .sortedWith(compareBy<TrackGroup> { it.name.isEmpty() }.thenByDescending { it.name.toIntOrNull() ?: 0 })

            val recentlyAdded = tracks.sortedByDescending { it.dateAddedMs }.take(RECENTLY_ADDED_LIMIT)

            return LibraryIndex(albums, artists, albumArtists, genres, folders, years, recentlyAdded)
        }

        private val albumYearOrder = compareBy<AlbumGroup> { it.year == null }
            .thenBy { it.year ?: 0 }
            .thenBy { it.title.lowercase() }

        private fun mergeSharedArtists(
            buckets: MutableMap<String, MutableList<Track>>,
            titles: Map<String, String>,
            artists: Map<String, MutableSet<String>>,
            redirects: MutableMap<String, String>
        ) {
            val byTitle = buckets.keys.groupBy { titles[it].orEmpty() }
            for ((title, keys) in byTitle) {
                if (title.isEmpty() || keys.size < 2) continue
                val roots = ArrayList<String>()
                for (key in keys.sortedByDescending { buckets[it]?.size ?: 0 }) {
                    val names = artists[key].orEmpty()
                    val root = roots.firstOrNull { candidate -> artists[candidate].orEmpty().any { it in names } }
                    if (root == null) {
                        roots.add(key)
                        continue
                    }
                    buckets.remove(key)?.let { buckets.getValue(root).addAll(it) }
                    artists[root]?.addAll(names)
                    redirects[key] = root
                }
            }
        }

        private fun primaryArtist(name: String, config: TagConfig): String =
            ArtistNames.split(name, config).firstOrNull()?.trim().orEmpty()

        private fun albumOwner(albumArtist: String, config: TagConfig): String =
            if (config.groupAlbumsByMainArtist) primaryArtist(albumArtist, config) else albumArtist.trim()

        private fun folderOf(track: Track): String {
            val path = track.path
            val cut = path.lastIndexOf('/')
            return if (cut >= 0) path.substring(0, cut) else path.substringBefore(':')
        }

        private fun folderName(folder: String): String {
            val name = folder.substringAfterLast('/').substringAfter(':')
            return name.ifEmpty { folder.substringBefore(':') }
        }

        private fun folderDetail(folder: String): String = folder.substringAfter(':').ifEmpty { folderName(folder) }
    }
}
