package com.jhaiian.kate.playback

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.jhaiian.kate.data.LibraryTrackCache
import java.io.FileNotFoundException

class ArtworkProvider : ContentProvider() {
    companion object {
        private const val PATH = "art"
        private val SAFE_KEY = Regex("^[A-Za-z0-9_-]{1,128}$")

        fun uriFor(context: Context, artKey: String): Uri =
            Uri.Builder()
                .scheme("content")
                .authority(context.packageName + ".artwork")
                .appendPath(PATH)
                .appendPath(artKey)
                .build()
    }

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String = "image/jpeg"

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r") throw SecurityException("Read only")
        val segments = uri.pathSegments
        if (segments.size != 2 || segments[0] != PATH) throw FileNotFoundException(uri.toString())
        val key = segments[1]
        if (!SAFE_KEY.matches(key)) throw FileNotFoundException(uri.toString())
        val context = context ?: throw FileNotFoundException(uri.toString())
        val file = LibraryTrackCache.artFile(context, key)
        if (!file.isFile) throw FileNotFoundException(uri.toString())
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
