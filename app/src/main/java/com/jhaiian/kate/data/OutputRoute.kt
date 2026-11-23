package com.jhaiian.kate.data

import android.media.AudioDeviceInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object OutputRoute {
    const val SPEAKER = "speaker"
    const val WIRED = "wired"
    const val BLUETOOTH = "bluetooth"
    const val USB = "usb"

    var current by mutableStateOf(SPEAKER)
        private set

    @Volatile
    var deviceType = AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        private set

    fun update(route: String, type: Int) {
        deviceType = type
        if (current != route) current = route
    }
}
