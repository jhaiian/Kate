package com.jhaiian.kate.playback

import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.jhaiian.kate.data.AudioSettings
import com.jhaiian.kate.data.OutputRoute
import kotlin.math.pow

object FineVolume {
    private const val FALLBACK_RANGE_DB = 60.0

    private var subStep = 0
    private var expectedIndex = -1

    fun handleKey(context: Context, up: Boolean): Boolean {
        AudioSettings.ensureLoaded(context)
        val steps = AudioSettings.volumeSubSteps
        if (steps <= 1) return false
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return false
        return runCatching {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC) else 0
            val index = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (index != expectedIndex) subStep = 0
            val level = (index * steps - subStep + if (up) 1 else -1).coerceIn(min * steps, max * steps)
            val newIndex = (level + steps - 1) / steps
            val newSub = newIndex * steps - level
            expectedIndex = newIndex
            subStep = newSub
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newIndex, AudioManager.FLAG_SHOW_UI)
            AudioSettings.setFineVolumeGain(gainFor(audioManager, newIndex, newSub, steps, min, max))
            true
        }.getOrDefault(false)
    }

    fun onSystemVolume(index: Int) {
        if (index == expectedIndex) return
        expectedIndex = index
        subStep = 0
        AudioSettings.setFineVolumeGain(1f)
    }

    private fun gainFor(audioManager: AudioManager, index: Int, sub: Int, steps: Int, min: Int, max: Int): Float {
        if (sub <= 0 || index <= 0) return 1f
        val upper = amplitude(audioManager, index, min, max)
        val lower = if (index - 1 <= 0) 0.0 else amplitude(audioManager, index - 1, min, max)
        if (upper <= 0.0) return 1f
        val fraction = (steps - sub).toDouble() / steps
        val target = lower + (upper - lower) * fraction
        return (target / upper).toFloat().coerceIn(0f, 1f)
    }

    private fun amplitude(audioManager: AudioManager, index: Int, min: Int, max: Int): Double {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val db = runCatching {
                audioManager.getStreamVolumeDb(AudioManager.STREAM_MUSIC, index, OutputRoute.deviceType)
            }.getOrNull()
            if (db != null && db.isFinite()) return 10.0.pow(db / 20.0)
        }
        if (max <= min) return 1.0
        val db = -FALLBACK_RANGE_DB * (max - index).toDouble() / (max - min).coerceAtLeast(1)
        return 10.0.pow(db / 20.0)
    }
}
