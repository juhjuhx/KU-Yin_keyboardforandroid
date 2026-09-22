package com.example.ime.util

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.example.ime.settings.KeyboardSettings

class FeedbackHelper(private val context: Context, private val settings: KeyboardSettings) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun performKeyPressFeedback(view: View? = null) {
        // 1. 震動回饋
        if (settings.isVibrateEnabled) {
            try {
                if (view != null) {
                    view.performHapticFeedback(
                        HapticFeedbackConstants.KEYBOARD_TAP,
                        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                    )
                } else if (vibrator?.hasVibrator() == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(20)
                    }
                }
            } catch (_: Exception) {
                // Ignore safety
            }
        }

        // 2. 按鍵音效
        if (settings.isSoundEnabled) {
            try {
                audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.4f)
            } catch (_: Exception) {
                // Ignore safety
            }
        }
    }
}
