package com.example.ime.service

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.example.androidkeyboard.input.EditorPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * D3c RED: EditorPolicy maps to native personalized learning.
 * Fails now because NativeLearningPolicy does not exist yet.
 */
class NativeLearningPolicyTest {

    private fun normal() = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT,
        EditorInfo.IME_ACTION_DONE,
    )

    private fun noLearning() = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT,
        EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
    )

    private fun password() = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
        EditorInfo.IME_ACTION_DONE,
    )

    private fun forceAscii() = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT,
        EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_FORCE_ASCII,
    )

    @Test
    fun `normal editor enables native learning`() {
        assertTrue(NativeLearningPolicy.shouldEnableLearning(normal()))
    }

    @Test
    fun `no personalized learning flag disables native learning`() {
        assertFalse(NativeLearningPolicy.shouldEnableLearning(noLearning()))
    }

    @Test
    fun `password disables native learning`() {
        assertFalse(NativeLearningPolicy.shouldEnableLearning(password()))
    }

    @Test
    fun `force ascii disables native learning`() {
        assertFalse(NativeLearningPolicy.shouldEnableLearning(forceAscii()))
    }
}
