package com.jhaiian.kate.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object BackgroundStore {
    const val SLOT_APP = "app"
    const val SLOT_PLAYER = "player"
    private const val DIR_NAME = "backgrounds"
    private const val MAX_SIDE = 1600

    fun directory(context: Context): File = File(context.applicationContext.filesDir, DIR_NAME)

    fun file(context: Context, name: String): File? =
        name.takeIf { it.isNotEmpty() }?.let { File(directory(context), it) }?.takeIf { it.isFile }

    suspend fun import(context: Context, uri: Uri, slot: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
            val bitmap = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@runCatching null
            val folder = directory(context).apply { mkdirs() }
            val name = "${slot}_${System.currentTimeMillis()}.jpg"
            File(folder, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            bitmap.recycle()
            folder.listFiles()?.filter { it.name.startsWith("${slot}_") && it.name != name }?.forEach { it.delete() }
            name
        }.getOrNull()
    }

    fun clear(context: Context, slot: String) {
        directory(context).listFiles()?.filter { it.name.startsWith("${slot}_") }?.forEach { it.delete() }
    }
}
