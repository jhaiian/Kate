package com.jhaiian.kate.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.storage.StorageManager

data class DetectedVolume(
    val id: String,
    val label: String,
    val intent: Intent
)

fun detectStorageVolumes(context: Context): List<DetectedVolume> {
    val manager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager ?: return emptyList()
    return manager.storageVolumes.mapNotNull { volume ->
        runCatching {
            DetectedVolume(
                id = volume.uuid ?: if (volume.isPrimary) "primary" else volume.hashCode().toString(),
                label = volume.getDescription(context)
                    ?: if (volume.isPrimary) "Internal storage" else "External storage",
                intent = volume.createOpenDocumentTreeIntent()
            )
        }.getOrNull()
    }
}

fun labelForTreeUri(uri: Uri): String {
    val id = uri.lastPathSegment ?: return "Storage"
    return if (id.startsWith("primary")) "Internal storage" else "SD card"
}
