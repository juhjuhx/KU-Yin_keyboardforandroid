package com.example.androidkeyboard.input

enum class DefaultInputMode {
    ZHUYIN,
    ENGLISH,
}

enum class KeyboardHeight {
    COMPACT,
    STANDARD,
    TALL,
}

data class KeyboardPreferences(
    val defaultMode: DefaultInputMode = DefaultInputMode.ZHUYIN,
    val rememberLastMode: Boolean = false,
    val bottomRowProfile: BottomRowProfile = BottomRowProfile.default(),
    val showSecondaryLabels: Boolean = true,
    val showLanguageKey: Boolean = true,
    val showEmojiKey: Boolean = false,
    val showNextImeKey: Boolean = false,
    val keyboardHeight: KeyboardHeight = KeyboardHeight.STANDARD,
    val hapticEnabled: Boolean = true,
    val proximityTolerance: Float = DEFAULT_PROXIMITY_TOLERANCE,
    val candidateExpandedByDefault: Boolean = false,
) {
    companion object {
        const val DEFAULT_PROXIMITY_TOLERANCE = 0.15f
    }
}

object KeyboardPreferenceKeys {
    const val HAPTIC_ENABLED = "haptic_enabled"
    const val PROXIMITY_TOLERANCE = "proximity_tolerance"
    const val DEFAULT_INPUT_MODE = "keyboard_default_input_mode"
    const val REMEMBER_LAST_MODE = "keyboard_remember_last_mode"
    const val SHOW_SECONDARY_LABELS = "keyboard_show_secondary_labels"
    const val SHOW_LANGUAGE_KEY = "keyboard_show_language_key"
    const val SHOW_EMOJI_KEY = "keyboard_show_emoji_key"
    const val SHOW_NEXT_IME_KEY = "keyboard_show_next_ime_key"
    const val KEYBOARD_HEIGHT = "keyboard_height"
    const val CANDIDATE_EXPANDED_BY_DEFAULT = "candidate_expanded_by_default"
}

object KeyboardPreferenceCodec {
    fun decode(values: Map<String, *>): KeyboardPreferences {
        val proximity = when (val stored = values[KeyboardPreferenceKeys.PROXIMITY_TOLERANCE]) {
            is Number -> stored.toFloat()
            is String -> stored.toFloatOrNull() ?: KeyboardPreferences.DEFAULT_PROXIMITY_TOLERANCE
            else -> KeyboardPreferences.DEFAULT_PROXIMITY_TOLERANCE
        }.coerceIn(0f, 0.5f)

        return KeyboardPreferences(
            defaultMode = enumValue(
                values[KeyboardPreferenceKeys.DEFAULT_INPUT_MODE],
                DefaultInputMode.ZHUYIN,
            ),
            rememberLastMode = booleanValue(
                values[KeyboardPreferenceKeys.REMEMBER_LAST_MODE],
                false,
            ),
            showSecondaryLabels = booleanValue(
                values[KeyboardPreferenceKeys.SHOW_SECONDARY_LABELS],
                true,
            ),
            showLanguageKey = booleanValue(
                values[KeyboardPreferenceKeys.SHOW_LANGUAGE_KEY],
                true,
            ),
            showEmojiKey = booleanValue(
                values[KeyboardPreferenceKeys.SHOW_EMOJI_KEY],
                false,
            ),
            showNextImeKey = booleanValue(
                values[KeyboardPreferenceKeys.SHOW_NEXT_IME_KEY],
                false,
            ),
            keyboardHeight = enumValue(
                values[KeyboardPreferenceKeys.KEYBOARD_HEIGHT],
                KeyboardHeight.STANDARD,
            ),
            hapticEnabled = booleanValue(
                values[KeyboardPreferenceKeys.HAPTIC_ENABLED],
                true,
            ),
            proximityTolerance = proximity,
            candidateExpandedByDefault = booleanValue(
                values[KeyboardPreferenceKeys.CANDIDATE_EXPANDED_BY_DEFAULT],
                false,
            ),
        )
    }

    private fun booleanValue(raw: Any?, default: Boolean): Boolean = raw as? Boolean ?: default

    private inline fun <reified T : Enum<T>> enumValue(raw: Any?, default: T): T {
        val name = raw as? String ?: return default
        return enumValues<T>().firstOrNull { it.name == name } ?: default
    }
}
