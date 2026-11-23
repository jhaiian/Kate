package com.jhaiian.kate.playback

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.jhaiian.kate.data.AudioSettings
import kotlin.math.roundToInt

internal class HeadsetAutoPlay(context: Context, private val onConnected: () -> Unit) {
    private companion object {
        const val WIRED_DELAY_MS = 400L
        const val BLUETOOTH_DELAY_MS = 1_500L
    }

    private val appContext = context.applicationContext
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val known = HashSet<Int>()
    private var registered = false
    private var pendingPlay = false
    private var pendingVolume = false

    private val connectRunnable = Runnable {
        if (pendingVolume) applyStartVolume()
        if (pendingPlay) onConnected()
        pendingPlay = false
        pendingVolume = false
    }

    private val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            var wired = false
            var bluetooth = false
            var bluetoothAllowed = false
            for (device in addedDevices) {
                if (!device.isSink || !known.add(device.id)) continue
                if (isWired(device.type)) wired = true
                if (isBluetooth(device.type)) {
                    bluetooth = true
                    val name = device.productName?.toString()?.trim().orEmpty()
                    if (name.isNotEmpty()) AudioSettings.rememberBluetoothDevice(appContext, name)
                    if (AudioSettings.allowsBluetoothAutoPlay(name)) bluetoothAllowed = true
                }
            }
            if (!wired && !bluetooth) return
            val play = (wired && AudioSettings.autoPlayWired) || (bluetoothAllowed && AudioSettings.autoPlayBluetooth)
            val volume = AudioSettings.startVolume > 0
            if (!play && !volume) return
            pendingPlay = pendingPlay || play
            pendingVolume = pendingVolume || volume
            handler.removeCallbacks(connectRunnable)
            handler.postDelayed(connectRunnable, if (bluetooth) BLUETOOTH_DELAY_MS else WIRED_DELAY_MS)
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            removedDevices.forEach { known.remove(it.id) }
        }
    }

    fun start() {
        if (registered) return
        registered = true
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).forEach { known.add(it.id) }
        audioManager.registerAudioDeviceCallback(callback, handler)
    }

    fun stop() {
        if (!registered) return
        registered = false
        handler.removeCallbacks(connectRunnable)
        pendingPlay = false
        pendingVolume = false
        audioManager.unregisterAudioDeviceCallback(callback)
        known.clear()
    }

    private fun applyStartVolume() {
        val percent = AudioSettings.startVolume
        if (percent <= 0) return
        runCatching {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC) else 0
            val index = (max * percent / 100f).roundToInt().coerceIn(min.coerceAtLeast(1), max)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0)
        }
    }

    private fun isWired(type: Int): Boolean =
        type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            type == AudioDeviceInfo.TYPE_USB_HEADSET

    private fun isBluetooth(type: Int): Boolean {
        if (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP) return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            (type == AudioDeviceInfo.TYPE_BLE_HEADSET || type == AudioDeviceInfo.TYPE_BLE_SPEAKER)
        ) {
            return true
        }
        return false
    }
}
