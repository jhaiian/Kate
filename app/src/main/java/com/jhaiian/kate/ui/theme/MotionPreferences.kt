package com.jhaiian.kate.ui.theme

import android.content.Context
import androidx.preference.PreferenceManager
import com.jhaiian.kate.ui.components.KateHaptics

object MotionPreferences {
    private const val KEY_HAPTICS = "haptics_level"
    private const val KEY_ANIMATION_SPEED = "animation_speed"

    const val SPEED_OFF = "off"
    const val SPEED_FAST = "fast"
    const val SPEED_NORMAL = "normal"
    const val SPEED_SLOW = "slow"

    const val DEFAULT_HAPTICS = KateHaptics.HAPTICS_LIGHT
    const val DEFAULT_ANIMATION_SPEED = SPEED_NORMAL

    fun getAnimationSpeed(context: Context): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_ANIMATION_SPEED, DEFAULT_ANIMATION_SPEED)
            ?: DEFAULT_ANIMATION_SPEED
        return if (AnimationSpeedOptions.any { it.key == value }) value else DEFAULT_ANIMATION_SPEED
    }

    fun setAnimationSpeed(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_ANIMATION_SPEED, value).apply()
    }

    fun getHaptics(context: Context): String {
        val value = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_HAPTICS, DEFAULT_HAPTICS) ?: DEFAULT_HAPTICS
        return if (HapticsOptions.any { it.key == value }) value else DEFAULT_HAPTICS
    }

    fun setHaptics(context: Context, value: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY_HAPTICS, value).apply()
    }
}
