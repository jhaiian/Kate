package com.jhaiian.kate.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioMixerAttributes
import android.os.Build
import androidx.annotation.RequiresApi
import com.jhaiian.kate.data.AudioSettings

internal object BitPerfect {
    private val mediaAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    @Volatile
    private var device: AudioDeviceInfo? = null

    fun refresh(context: Context) {
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return
        val candidate = if (AudioSettings.bitPerfect && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            findDevice(audioManager)
        } else {
            null
        }
        val previous = device
        if (previous != null && previous.id != candidate?.id && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            clear(audioManager, previous)
        }
        device = candidate
        AudioSettings.updateBitPerfectEngaged(candidate != null)
    }

    fun release(context: Context) {
        val audioManager = context.getSystemService(AudioManager::class.java)
        val previous = device
        device = null
        if (audioManager != null && previous != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            clear(audioManager, previous)
        }
        AudioSettings.updateBitPerfectEngaged(false)
    }

    fun prepare(context: Context, sampleRate: Int, channelCount: Int, encoding: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        if (!AudioSettings.pipelineParams.bitPerfect) return
        val target = device ?: return
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return
        applyMixer(audioManager, target, sampleRate, channelCount, encoding)
    }

    fun chooseNativeFormat(context: Context, sampleRate: Int, channelCount: Int, inputFloat: Boolean): Int? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return null
        val target = device ?: return null
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return null
        return pickNativeMixer(audioManager, target, sampleRate, channelCount, inputFloat)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun pickNativeMixer(
        audioManager: AudioManager,
        target: AudioDeviceInfo,
        sampleRate: Int,
        channelCount: Int,
        inputFloat: Boolean
    ): Int? = runCatching {
        val order = if (inputFloat) {
            listOf(AudioFormat.ENCODING_PCM_32BIT, AudioFormat.ENCODING_PCM_24BIT_PACKED, AudioFormat.ENCODING_PCM_FLOAT, AudioFormat.ENCODING_PCM_16BIT)
        } else {
            listOf(AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_24BIT_PACKED, AudioFormat.ENCODING_PCM_32BIT, AudioFormat.ENCODING_PCM_FLOAT)
        }
        val candidates = audioManager.getSupportedMixerAttributes(target).filter { mixer ->
            mixer.mixerBehavior == AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT &&
                mixer.format.sampleRate == sampleRate &&
                Integer.bitCount(mixer.format.channelMask) == channelCount
        }
        val chosen = order.firstNotNullOfOrNull { encoding -> candidates.firstOrNull { it.format.encoding == encoding } }
        if (chosen == null) {
            audioManager.clearPreferredMixerAttributes(mediaAttributes, target)
            null
        } else {
            audioManager.setPreferredMixerAttributes(mediaAttributes, target, chosen)
            when (chosen.format.encoding) {
                AudioFormat.ENCODING_PCM_16BIT -> NativeAudio.FORMAT_I16
                AudioFormat.ENCODING_PCM_24BIT_PACKED -> NativeAudio.FORMAT_I24
                AudioFormat.ENCODING_PCM_32BIT -> NativeAudio.FORMAT_I32
                else -> NativeAudio.FORMAT_FLOAT
            }
        }
    }.getOrNull()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun applyMixer(audioManager: AudioManager, target: AudioDeviceInfo, sampleRate: Int, channelCount: Int, encoding: Int) {
        runCatching {
            val match = audioManager.getSupportedMixerAttributes(target).firstOrNull { mixer ->
                val format = mixer.format
                mixer.mixerBehavior == AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT &&
                    format.sampleRate == sampleRate &&
                    format.encoding == encoding &&
                    Integer.bitCount(format.channelMask) == channelCount
            }
            if (match != null) {
                audioManager.setPreferredMixerAttributes(mediaAttributes, target, match)
            } else {
                audioManager.clearPreferredMixerAttributes(mediaAttributes, target)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun findDevice(audioManager: AudioManager): AudioDeviceInfo? =
        audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).firstOrNull { info ->
            (info.type == AudioDeviceInfo.TYPE_USB_DEVICE || info.type == AudioDeviceInfo.TYPE_USB_HEADSET) &&
                runCatching {
                    audioManager.getSupportedMixerAttributes(info).any { it.mixerBehavior == AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT }
                }.getOrDefault(false)
        }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun clear(audioManager: AudioManager, target: AudioDeviceInfo) {
        runCatching { audioManager.clearPreferredMixerAttributes(mediaAttributes, target) }
    }
}
