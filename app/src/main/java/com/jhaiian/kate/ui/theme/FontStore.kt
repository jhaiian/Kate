package com.jhaiian.kate.ui.theme

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.documentfile.provider.DocumentFile
import java.io.File

object FontStore {
    const val PREFIX = "file:"
    private const val DIR_NAME = "fonts"
    private const val MAX_BYTES = 20L * 1024 * 1024

    private var dir: File? = null
    private val families = HashMap<String, FontFamily?>()

    var fonts by mutableStateOf<List<String>>(emptyList())
        private set

    fun ensureLoaded(context: Context) {
        if (dir != null) return
        dir = File(context.applicationContext.filesDir, DIR_NAME).apply { mkdirs() }
        refresh()
    }

    private fun refresh() {
        fonts = dir?.listFiles()?.filter { it.isFile && !it.name.endsWith(".tmp") }?.map { it.name }?.sorted().orEmpty()
    }

    fun isFontKey(key: String): Boolean = key.startsWith(PREFIX)

    fun key(name: String): String = PREFIX + name

    fun label(key: String): String = key.removePrefix(PREFIX).substringBeforeLast('.')

    fun family(key: String): FontFamily? {
        val name = key.removePrefix(PREFIX)
        val folder = dir ?: return null
        return synchronized(families) {
            families.getOrPut(name) {
                val file = File(folder, name)
                if (!file.isFile) null else runCatching { FontFamily(Typeface.createFromFile(file)) }.getOrNull()
            }
        }
    }

    fun import(context: Context, uri: Uri): String? {
        ensureLoaded(context)
        val folder = dir ?: return null
        val display = runCatching { DocumentFile.fromSingleUri(context, uri)?.name }.getOrNull().orEmpty()
        val extension = display.substringAfterLast('.', "ttf").lowercase().takeIf { it in setOf("ttf", "otf", "ttc") } ?: "ttf"
        val base = display.substringBeforeLast('.').replace(Regex("[^A-Za-z0-9 _-]"), "").trim().take(40).ifEmpty { "Font" }
        var name = "$base.$extension"
        var index = 2
        while (File(folder, name).exists()) {
            name = "$base $index.$extension"
            index++
        }
        val temp = File(folder, "$name.tmp")
        val copied = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_BYTES) return@runCatching false
                        output.write(buffer, 0, read)
                    }
                }
                true
            } ?: false
        }.getOrDefault(false)
        val valid = copied && runCatching {
            val typeface = Typeface.createFromFile(temp)
            typeface != null && typeface != Typeface.DEFAULT
        }.getOrDefault(false)
        if (!valid) {
            temp.delete()
            return null
        }
        val target = File(folder, name)
        if (!temp.renameTo(target)) {
            temp.delete()
            return null
        }
        refresh()
        return key(name)
    }

    fun delete(key: String) {
        val folder = dir ?: return
        val name = key.removePrefix(PREFIX)
        File(folder, name).delete()
        synchronized(families) { families.remove(name) }
        refresh()
    }
}
