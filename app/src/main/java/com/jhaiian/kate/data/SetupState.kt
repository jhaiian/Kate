package com.jhaiian.kate.data

import android.content.Context

object SetupState {
    const val PREFS_NAME = "first_run"
    private const val KEY_DONE = "done"
    private const val KEY_NOTIFICATIONS_ASKED = "notifications_asked"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDone(context: Context): Boolean {
        val prefs = prefs(context)
        if (prefs.getBoolean(KEY_DONE, false)) return true
        if (LibraryPreferences.getFolders(context.applicationContext).isNotEmpty()) {
            prefs.edit().putBoolean(KEY_DONE, true).apply()
            return true
        }
        return false
    }

    fun markDone(context: Context) {
        prefs(context).edit().putBoolean(KEY_DONE, true).apply()
    }

    fun notificationsAsked(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTIFICATIONS_ASKED, false)

    fun markNotificationsAsked(context: Context) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS_ASKED, true).apply()
    }
}
