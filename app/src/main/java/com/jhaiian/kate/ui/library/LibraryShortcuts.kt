package com.jhaiian.kate.ui.library

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.jhaiian.kate.MainActivity
import com.jhaiian.kate.R
import com.jhaiian.kate.data.LibraryCategory

internal fun pinCategoryShortcut(context: Context, category: LibraryCategory, label: String): Boolean {
    if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) return false
    val intent = Intent(context, MainActivity::class.java)
        .setAction(Intent.ACTION_VIEW)
        .putExtra(MainActivity.EXTRA_LIBRARY_CATEGORY, category.key)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val shortcut = ShortcutInfoCompat.Builder(context, "library_${category.key}")
        .setShortLabel(label)
        .setLongLabel(label)
        .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
        .setIntent(intent)
        .build()
    return ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
}
