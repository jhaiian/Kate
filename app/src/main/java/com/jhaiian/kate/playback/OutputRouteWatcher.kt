package com.jhaiian.kate.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.jhaiian.kate.data.EqualizerSettings
import com.jhaiian.kate.data.OutputRoute

internal class OutputRouteWatcher(context: Context) {
    private companion object {
        const val SETTLE_MS = 300L
    }

    private val appContext = context.applicationContext
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var registered = false
    private val mediaAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val refreshRunnable = Runnable { refresh() }

    private val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            schedule()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            schedule()
        }
    }

    fun start() {
        if (registered) return
        registered = true
        audioManager.registerAudioDeviceCallback(callback, handler)
        refresh()
    }

    fun stop() {
        if (!registered) return
        registered = false
        handler.removeCallbacks(refreshRunnable)
        audioManager.unregisterAudioDeviceCallback(callback)
    }

    private fun schedule() {
        handler.removeCallbacks(refreshRunnable)
        handler.postDelayed(refreshRunnable, SETTLE_MS)
    }

    private fun refresh() {
        val type = currentType()
        val route = routeOf(type)
        OutputRoute.update(route, type)
        EqualizerSettings.onRouteChanged(appContext, route)
        BitPerfect.refresh(appContext)
    }

    private fun currentType(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val routed = runCatching { audioManager.getAudioDevicesForAttributes(mediaAttributes) }.getOrNull()
            routed?.firstOrNull()?.let { return it.type }
        }
        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).map { it.type }
        return outputs.firstOrNull { routeOf(it) == OutputRoute.BLUETOOTH }
            ?: outputs.firstOrNull { routeOf(it) == OutputRoute.USB }
            ?: outputs.firstOrNull { routeOf(it) == OutputRoute.WIRED }
            ?: AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
    }

    private fun routeOf(type: Int): String {
        if (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) return OutputRoute.BLUETOOTH
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            (type == AudioDeviceInfo.TYPE_BLE_HEADSET || type == AudioDeviceInfo.TYPE_BLE_SPEAKER)
        ) {
            return OutputRoute.BLUETOOTH
        }
        if (type == AudioDeviceInfo.TYPE_USB_HEADSET || type == AudioDeviceInfo.TYPE_USB_DEVICE || type == AudioDeviceInfo.TYPE_USB_ACCESSORY) {
            return OutputRoute.USB
        }
        if (type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            type == AudioDeviceInfo.TYPE_LINE_ANALOG ||
            type == AudioDeviceInfo.TYPE_AUX_LINE
        ) {
            return OutputRoute.WIRED
        }
        return OutputRoute.SPEAKER
    }
}
