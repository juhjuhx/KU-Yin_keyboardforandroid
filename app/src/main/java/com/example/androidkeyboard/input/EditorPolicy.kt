package com.example.androidkeyboard.input

import android.text.InputType
import android.view.inputmethod.EditorInfo

/** Domain-level editor action understood by the IME session. */
enum class EditorAction {
    NONE,
    UNSPECIFIED,
    GO,
    SEARCH,
    SEND,
    NEXT,
    DONE,
    PREVIOUS,
}

/**
 * Normalized policy derived from Android EditorInfo.
 *
 * Android-specific bit masks are collapsed here so the rest of the IME does
 * not need to duplicate privacy and editor-behaviour decisions.
 */
data class EditorPolicy(
    val isSensitive: Boolean,
    val allowPersonalizedLearning: Boolean,
    val allowCandidates: Boolean,
    val allowComposition: Boolean,
    val forceAscii: Boolean,
    val action: EditorAction,
) {
    companion object {
        fun from(inputType: Int, imeOptions: Int): EditorPolicy {
            val inputClass = inputType and InputType.TYPE_MASK_CLASS
            val variation = inputType and InputType.TYPE_MASK_VARIATION

            val isSensitive = when (inputClass) {
                InputType.TYPE_CLASS_TEXT -> variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                    variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD

                InputType.TYPE_CLASS_NUMBER -> variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
                else -> false
            }

            val forceAscii = imeOptions and EditorInfo.IME_FLAG_FORCE_ASCII != 0
            val noPersonalizedLearning =
                imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING != 0
            val supportsChewing = inputClass == InputType.TYPE_CLASS_TEXT && !isSensitive && !forceAscii

            return EditorPolicy(
                isSensitive = isSensitive,
                allowPersonalizedLearning = !isSensitive && !noPersonalizedLearning,
                allowCandidates = supportsChewing,
                allowComposition = supportsChewing,
                forceAscii = forceAscii,
                action = normalizeAction(imeOptions),
            )
        }

        private fun normalizeAction(imeOptions: Int): EditorAction = when (
            imeOptions and EditorInfo.IME_MASK_ACTION
        ) {
            EditorInfo.IME_ACTION_NONE -> EditorAction.NONE
            EditorInfo.IME_ACTION_UNSPECIFIED -> EditorAction.UNSPECIFIED
            EditorInfo.IME_ACTION_GO -> EditorAction.GO
            EditorInfo.IME_ACTION_SEARCH -> EditorAction.SEARCH
            EditorInfo.IME_ACTION_SEND -> EditorAction.SEND
            EditorInfo.IME_ACTION_NEXT -> EditorAction.NEXT
            EditorInfo.IME_ACTION_DONE -> EditorAction.DONE
            EditorInfo.IME_ACTION_PREVIOUS -> EditorAction.PREVIOUS
            else -> EditorAction.UNSPECIFIED
        }
    }
}
