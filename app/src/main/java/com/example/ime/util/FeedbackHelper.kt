package com.example.ime.util

import android.content.Context
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.example.ime.settings.KeyboardSettings

class FeedbackHelper(private val context: Context, private val settings: KeyboardSettings) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    fun performKeyPressFeedback(view: View) {
        // 1. 震動回饋
        if (settings.isVibrateEnabled) {
            try {
                view.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
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
