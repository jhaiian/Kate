package com.jhaiian.kate.data

import android.net.Uri

data class MusicFolder(
    val uri: Uri,
    val displayName: String,
    val locationLabel: String,
    val enabled: Boolean = true
)
