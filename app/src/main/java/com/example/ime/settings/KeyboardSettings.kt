package com.example.ime.settings

import android.content.Context
import android.content.SharedPreferences

class KeyboardSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("kuyin_settings", Context.MODE_PRIVATE)

    var isVibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATE, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var keyboardHeightDp: Int
        get() = prefs.getInt(KEY_HEIGHT, 260)
        set(value) = prefs.edit().putInt(KEY_HEIGHT, value).apply()

    var keyboardThemeIndex: Int
        get() = prefs.getInt(KEY_THEME_INDEX, 0)
        set(value) = prefs.edit().putInt(KEY_THEME_INDEX, value).apply()

    var isClipboardBarEnabled: Boolean
        get() = prefs.getBoolean(KEY_CLIPBOARD_BAR, false)
        set(value) = prefs.edit().putBoolean(KEY_CLIPBOARD_BAR, value).apply()

    var candidateFontSizeSp: Int
        get() = prefs.getInt(KEY_FONT_SIZE, 18)
        set(value) = prefs.edit().putInt(KEY_FONT_SIZE, value).apply()

    var isSimplifiedOutput: Boolean
        get() = prefs.getBoolean(KEY_SIMPLIFIED, false)
        set(value) = prefs.edit().putBoolean(KEY_SIMPLIFIED, value).apply()

    companion object {
        private const val KEY_VIBRATE = "pref_vibrate"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_HEIGHT = "pref_keyboard_height"
        private const val KEY_THEME_INDEX = "pref_keyboard_theme_index"
        private const val KEY_CLIPBOARD_BAR = "pref_clipboard_bar"
        private const val KEY_FONT_SIZE = "pref_candidate_font_size"
        private const val KEY_SIMPLIFIED = "pref_simplified_output"
    }
}
