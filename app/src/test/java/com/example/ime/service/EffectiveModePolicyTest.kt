package com.example.ime.service

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.example.androidkeyboard.input.EditorPolicy
import com.example.ime.engine.KeyboardMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A5: forced ASCII is a temporary override; userPreferredMode is never
 * polluted (C4-D2.5). Pure logic, plain JUnit.
 */
class EffectiveModePolicyTest {

    private fun passwordPolicy() = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
        EditorInfo.IME_ACTION_DONE
    )

    private fun forceAsciiPolicy() = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT,
        EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_FORCE_ASCII
    )

    private fun normalPolicy() = EditorPolicy.from(
        InputType.TYPE_CLASS_TEXT,
        EditorInfo.IME_ACTION_DONE
    )

    @Test
    fun `zhuyin survives password detour`() {
        var preferred = KeyboardMode.ZHUYIN
        assertEquals(
            KeyboardMode.ENGLISH,
            EffectiveModePolicy.effectiveMode(preferred, passwordPolicy())
        )
        assertEquals(KeyboardMode.ZHUYIN, preferred)
        assertEquals(
            KeyboardMode.ZHUYIN,
            EffectiveModePolicy.effectiveMode(preferred, normalPolicy())
        )
    }

    @Test
    fun `user english stays english through password`() {
        val preferred = KeyboardMode.ENGLISH
        assertEquals(
            KeyboardMode.ENGLISH,
            EffectiveModePolicy.effectiveMode(preferred, passwordPolicy())
        )
        assertEquals(
            KeyboardMode.ENGLISH,
            EffectiveModePolicy.effectiveMode(preferred, normalPolicy())
        )
    }

    @Test
    fun `zhuyin survives force-ascii detour`() {
        val preferred = KeyboardMode.ZHUYIN
        assertEquals(
            KeyboardMode.ENGLISH,
            EffectiveModePolicy.effectiveMode(preferred, forceAsciiPolicy())
        )
        assertEquals(
            KeyboardMode.ZHUYIN,
            EffectiveModePolicy.effectiveMode(preferred, normalPolicy())
        )
    }

    @Test
    fun `non text editors force English for every preferred mode`() {
        listOf(
            InputType.TYPE_NULL,
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE,
            InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME,
        ).forEach { inputType ->
            val policy = EditorPolicy.from(inputType, EditorInfo.IME_ACTION_NEXT)
            KeyboardMode.entries.forEach { preferred ->
                assertEquals(
                    "inputType=$inputType preferred=$preferred",
                    KeyboardMode.ENGLISH,
                    EffectiveModePolicy.effectiveMode(preferred, policy),
                )
            }
        }
    }

    @Test
    fun `non text detour restores every preferred mode on normal text`() {
        val restricted = EditorPolicy.from(InputType.TYPE_CLASS_NUMBER, EditorInfo.IME_ACTION_DONE)
        KeyboardMode.entries.forEach { preferred ->
            assertEquals(KeyboardMode.ENGLISH, EffectiveModePolicy.effectiveMode(preferred, restricted))
            assertEquals(preferred, EffectiveModePolicy.effectiveMode(preferred, normalPolicy()))
        }
    }
}
