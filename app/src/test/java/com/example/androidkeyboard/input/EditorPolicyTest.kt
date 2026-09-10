package com.example.androidkeyboard.input

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorPolicyTest {

    @Test
    fun normalTextAllowsChewingAndPersonalizedLearning() {
        val policy = EditorPolicy.from(
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
            imeOptions = EditorInfo.IME_ACTION_NONE,
        )

        assertFalse(policy.isSensitive)
        assertTrue(policy.allowPersonalizedLearning)
        assertTrue(policy.allowCandidates)
        assertTrue(policy.allowComposition)
        assertFalse(policy.forceAscii)
        assertEquals(EditorAction.NONE, policy.action)
    }

    @Test
    fun textPasswordIsSensitiveAndSuppressesCompositionAndCandidates() {
        val policy = EditorPolicy.from(
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            imeOptions = EditorInfo.IME_ACTION_DONE,
        )

        assertTrue(policy.isSensitive)
        assertFalse(policy.allowPersonalizedLearning)
        assertFalse(policy.allowCandidates)
        assertFalse(policy.allowComposition)
        assertEquals(EditorAction.DONE, policy.action)
    }

    @Test
    fun visiblePasswordIsStillSensitive() {
        val policy = EditorPolicy.from(
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            imeOptions = EditorInfo.IME_ACTION_NONE,
        )

        assertTrue(policy.isSensitive)
        assertFalse(policy.allowPersonalizedLearning)
        assertFalse(policy.allowCandidates)
    }

    @Test
    fun webPasswordIsSensitive() {
        val policy = EditorPolicy.from(
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            imeOptions = EditorInfo.IME_ACTION_NONE,
        )

        assertTrue(policy.isSensitive)
        assertFalse(policy.allowComposition)
    }

    @Test
    fun numericPasswordIsSensitive() {
        val policy = EditorPolicy.from(
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD,
            imeOptions = EditorInfo.IME_ACTION_NONE,
        )

        assertTrue(policy.isSensitive)
        assertFalse(policy.allowPersonalizedLearning)
        assertFalse(policy.allowCandidates)
        assertFalse(policy.allowComposition)
    }

    @Test
    fun noPersonalizedLearningDisablesLearningWithoutMakingNormalFieldSensitive() {
        val policy = EditorPolicy.from(
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
            imeOptions = EditorInfo.IME_ACTION_NONE or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING,
        )

        assertFalse(policy.isSensitive)
        assertFalse(policy.allowPersonalizedLearning)
        assertTrue(policy.allowCandidates)
        assertTrue(policy.allowComposition)
    }

    @Test
    fun forceAsciiSuppressesChewingCompositionAndCandidates() {
        val policy = EditorPolicy.from(
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL,
            imeOptions = EditorInfo.IME_ACTION_GO or EditorInfo.IME_FLAG_FORCE_ASCII,
        )

        assertTrue(policy.forceAscii)
        assertFalse(policy.allowCandidates)
        assertFalse(policy.allowComposition)
        assertEquals(EditorAction.GO, policy.action)
    }

    @Test
    fun editorActionsAreNormalizedIntoDomainEnum() {
        val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL

        assertEquals(EditorAction.SEARCH, EditorPolicy.from(inputType, EditorInfo.IME_ACTION_SEARCH).action)
        assertEquals(EditorAction.SEND, EditorPolicy.from(inputType, EditorInfo.IME_ACTION_SEND).action)
        assertEquals(EditorAction.NEXT, EditorPolicy.from(inputType, EditorInfo.IME_ACTION_NEXT).action)
        assertEquals(EditorAction.DONE, EditorPolicy.from(inputType, EditorInfo.IME_ACTION_DONE).action)
        assertEquals(EditorAction.PREVIOUS, EditorPolicy.from(inputType, EditorInfo.IME_ACTION_PREVIOUS).action)
        assertEquals(EditorAction.UNSPECIFIED, EditorPolicy.from(inputType, EditorInfo.IME_ACTION_UNSPECIFIED).action)
    }
}
