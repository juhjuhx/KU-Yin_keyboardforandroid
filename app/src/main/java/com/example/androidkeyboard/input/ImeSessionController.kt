package com.example.androidkeyboard.input

/** Keyboard surface selected for the current editor session. */
enum class SessionKeyboard {
    DACHEN,
    ASCII,
}

/** Immutable runtime decisions for one editor session. */
data class ImeSession(
    val keyboard: SessionKeyboard,
    val allowComposition: Boolean,
    val allowCandidates: Boolean,
    val personalizedLearningEnabled: Boolean,
    val editorAction: EditorAction,
)

/**
 * Pure session state machine. Android lifecycle callbacks translate EditorInfo
 * into EditorPolicy, then delegate policy decisions here.
 */
class ImeSessionController {

    private var currentSession = ImeSession(
        keyboard = SessionKeyboard.ASCII,
        allowComposition = false,
        allowCandidates = false,
        personalizedLearningEnabled = false,
        editorAction = EditorAction.NONE,
    )

    fun begin(policy: EditorPolicy): ImeSession {
        currentSession = ImeSession(
            keyboard = if (policy.allowComposition) SessionKeyboard.DACHEN else SessionKeyboard.ASCII,
            allowComposition = policy.allowComposition,
            allowCandidates = policy.allowCandidates,
            personalizedLearningEnabled = policy.allowPersonalizedLearning,
            editorAction = policy.action,
        )
        return currentSession
    }

    fun current(): ImeSession = currentSession

    /**
     * Mirrors the Android sample policy: when the editor moves selection away
     * from the end of our composing range, the IME must drop stale composition.
     */
    fun shouldResetComposition(
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int,
    ): Boolean {
        if (!currentSession.allowComposition) return false
        if (candidatesStart < 0 || candidatesEnd < 0) return false
        return newSelStart != candidatesEnd || newSelEnd != candidatesEnd
    }
}
