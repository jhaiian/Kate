package com.jhaiian.kate.data

import android.net.Uri

data class Track(
    val id: String,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val format: String,
    val bitDepth: Int? = null,
    val hasArt: Boolean = false,
    val artKey: String? = null,
    val trackNumber: Int? = null,
    val discNumber: Int? = null,
    val year: Int? = null,
    val filename: String = "",
    val path: String = "",
    val dateAddedMs: Long = 0L,
    val albumArtist: String = "",
    val genre: String = "",
    val sampleRate: Int? = null
)
