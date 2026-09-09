package com.example.androidkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class KeyboardLayoutTest {

    private fun keyByCode(code: Int): KeyDef? =
        KeyboardLayout.Dachen.allKeys.firstOrNull { it.code == code }

    @Test
    fun `standard Dachen physical keys match libchewing`() {
        val expected = mapOf(
            'x'.code to "ㄌ",
            'c'.code to "ㄏ",
            'v'.code to "ㄒ",
            'b'.code to "ㄖ",
            'n'.code to "ㄙ",
            'm'.code to "ㄩ",
            ','.code to "ㄝ",
            '.'.code to "ㄡ",
            '/'.code to "ㄥ",
        )

        expected.forEach { (code, label) ->
            val key = keyByCode(code)
            assertNotNull("Missing physical key for ASCII $code", key)
            assertEquals("Unexpected Dachen label for ASCII $code", label, key?.label)
        }
    }

    @Test
    fun `space uses ASCII space`() {
        val space = KeyboardLayout.Dachen.allKeys.firstOrNull { it.label == " " }
        assertNotNull("Space key missing", space)
        assertEquals(' '.code, space?.code)
    }
}
