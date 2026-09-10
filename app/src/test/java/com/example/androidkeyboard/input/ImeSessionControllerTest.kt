package com.example.androidkeyboard.input

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeSessionControllerTest {

    @Test
    fun normalTextUsesDachenAndAllowsLearning() {
        val controller = ImeSessionController()
        val session = controller.begin(
            EditorPolicy.from(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
                EditorInfo.IME_ACTION_NONE,
            )
        )

        assertEquals(SessionKeyboard.DACHEN, session.keyboard)
        assertTrue(session.allowComposition)
        assertTrue(session.allowCandidates)
        assertTrue(session.personalizedLearningEnabled)
    }

    @Test
    fun passwordUsesAsciiAndDisablesLearning() {
        val controller = ImeSessionController()
        val session = controller.begin(
            EditorPolicy.from(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                EditorInfo.IME_ACTION_DONE,
            )
        )

        assertEquals(SessionKeyboard.ASCII, session.keyboard)
        assertFalse(session.allowComposition)
        assertFalse(session.allowCandidates)
        assertFalse(session.personalizedLearningEnabled)
        assertEquals(EditorAction.DONE, session.editorAction)
    }

    @Test
    fun noPersonalizedLearningKeepsDachenButHardDisablesLearning() {
        val controller = ImeSessionController()
        val session = controller.begin(
            EditorPolicy.from(
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
            )
        )

        assertEquals(SessionKeyboard.DACHEN, session.keyboard)
        assertTrue(session.allowComposition)
        assertFalse(session.personalizedLearningEnabled)
    }

    @Test
    fun forceAsciiUsesAsciiKeyboardAndBypassesComposition() {
        val controller = ImeSessionController()
        val session = controller.begin(
            EditorPolicy.from(
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_GO or EditorInfo.IME_FLAG_FORCE_ASCII,
            )
        )

        assertEquals(SessionKeyboard.ASCII, session.keyboard)
        assertFalse(session.allowComposition)
        assertFalse(session.allowCandidates)
        assertEquals(EditorAction.GO, session.editorAction)
    }

    @Test
    fun cursorLeavingComposingRangeRequestsReconciliation() {
        val controller = ImeSessionController()
        controller.begin(
            EditorPolicy.from(InputType.TYPE_CLASS_TEXT, EditorInfo.IME_ACTION_NONE)
        )

        assertFalse(
            controller.shouldResetComposition(
                newSelStart = 5,
                newSelEnd = 5,
                candidatesStart = 2,
                candidatesEnd = 5,
            )
        )
        assertTrue(
            controller.shouldResetComposition(
                newSelStart = 3,
                newSelEnd = 3,
                candidatesStart = 2,
                candidatesEnd = 5,
            )
        )
        assertFalse(
            controller.shouldResetComposition(
                newSelStart = 3,
                newSelEnd = 3,
                candidatesStart = -1,
                candidatesEnd = -1,
            )
        )
    }

    @Test
    fun selectionChangesDoNotCreateCompositionInAsciiSession() {
        val controller = ImeSessionController()
        controller.begin(
            EditorPolicy.from(
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                EditorInfo.IME_ACTION_NONE,
            )
        )

        assertFalse(
            controller.shouldResetComposition(
                newSelStart = 1,
                newSelEnd = 1,
                candidatesStart = 0,
                candidatesEnd = 2,
            )
        )
    }
}
