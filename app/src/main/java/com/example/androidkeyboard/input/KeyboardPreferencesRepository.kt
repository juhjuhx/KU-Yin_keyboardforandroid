package com.example.androidkeyboard.input

import android.content.SharedPreferences

/** Small storage boundary so preference decoding stays testable without Android runtime. */
interface KeyboardPreferenceStore {
    fun snapshot(): Map<String, *>
    fun putBoolean(key: String, value: Boolean)
    fun putString(key: String, value: String)
    fun remove(keys: Set<String>)
}

/** Typed keyboard-shell preferences backed by a simple key/value store. */
class KeyboardPreferencesRepository(
    private val store: KeyboardPreferenceStore,
) {
    fun load(): KeyboardPreferences = KeyboardPreferenceCodec.decode(store.snapshot())

    fun setHapticEnabled(enabled: Boolean) {
        store.putBoolean(KeyboardPreferenceKeys.HAPTIC_ENABLED, enabled)
    }

    fun setProximityTolerance(value: Float) {
        store.putString(
            KeyboardPreferenceKeys.PROXIMITY_TOLERANCE,
            value.coerceIn(0f, 0.5f).toString(),
        )
    }

    /** Reset only v0.2 shell preferences. Existing typing feel settings stay untouched. */
    fun resetKeyboardShellPreferences() {
        store.remove(KEYBOARD_SHELL_KEYS)
    }

    companion object {
        private val KEYBOARD_SHELL_KEYS = setOf(
            KeyboardPreferenceKeys.DEFAULT_INPUT_MODE,
            KeyboardPreferenceKeys.REMEMBER_LAST_MODE,
            KeyboardPreferenceKeys.SHOW_SECONDARY_LABELS,
            KeyboardPreferenceKeys.SHOW_LANGUAGE_KEY,
            KeyboardPreferenceKeys.SHOW_EMOJI_KEY,
            KeyboardPreferenceKeys.SHOW_NEXT_IME_KEY,
            KeyboardPreferenceKeys.KEYBOARD_HEIGHT,
            KeyboardPreferenceKeys.CANDIDATE_EXPANDED_BY_DEFAULT,
        )
    }
}

/** Android adapter for the same preference file already used by the released IME. */
class SharedPreferencesKeyboardPreferenceStore(
    private val preferences: SharedPreferences,
) : KeyboardPreferenceStore {
    override fun snapshot(): Map<String, *> = preferences.all

    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun remove(keys: Set<String>) {
        val editor = preferences.edit()
        keys.forEach(editor::remove)
        editor.apply()
    }
}

/** Android-facing facade used by IMEConfig and the future settings surface. */
class SharedPreferencesKeyboardPreferencesRepository(
    sharedPreferences: SharedPreferences,
) {
    private val delegate = KeyboardPreferencesRepository(
        SharedPreferencesKeyboardPreferenceStore(sharedPreferences),
    )

    fun load(): KeyboardPreferences = delegate.load()

    fun setHapticEnabled(enabled: Boolean) = delegate.setHapticEnabled(enabled)

    fun setProximityTolerance(value: Float) = delegate.setProximityTolerance(value)

    fun resetKeyboardShellPreferences() = delegate.resetKeyboardShellPreferences()
}
