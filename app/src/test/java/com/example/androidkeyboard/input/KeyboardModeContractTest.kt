package com.example.androidkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardModeContractTest {

    @Test
    fun dachenAndAsciiLayoutsBothExposeEnter() {
        assertTrue(KeyboardLayout.Dachen.allKeys.any { it.action == KeyAction.ENTER })
        assertTrue(KeyboardLayout.Ascii.allKeys.any { it.action == KeyAction.ENTER })
    }

    @Test
    fun asciiLayoutProvidesDirectAsciiInputWithoutZhuyinLabels() {
        val inputKeys = KeyboardLayout.Ascii.allKeys.filter { it.action == KeyAction.INPUT }

        assertTrue(inputKeys.isNotEmpty())
        assertTrue(inputKeys.all { key -> key.label.length == 1 && key.label.single().code in 0x20..0x7e })
        assertFalse(inputKeys.any { it.label.startsWith("ㄅ") || it.label.startsWith("ㄆ") })
    }

    @Test
    fun asciiLetterKeyCodesMatchTheirLabels() {
        val q = KeyboardLayout.Ascii.allKeys.first { it.label == "q" }
        assertEquals('q'.code, q.code)
    }

    @Test
    fun asciiPasswordSurfaceIncludesDigitsAndShift() {
        val inputLabels = KeyboardLayout.Ascii.allKeys
            .filter { it.action == KeyAction.INPUT }
            .map { it.label }
            .toSet()

        assertTrue(('0'..'9').all { it.toString() in inputLabels })
        assertTrue(KeyboardLayout.Ascii.allKeys.any { it.action == KeyAction.SHIFT })
    }

    @Test
    fun shiftedAsciiRowsRenderUppercaseLettersWithoutChangingPhysicalCodes() {
        val shiftedKeys = KeyboardLayout.asciiRows(shifted = true)
            .flatMap { it.keys }
        val q = shiftedKeys.first { it.label == "Q" }

        assertEquals('q'.code, q.code)
        assertTrue(shiftedKeys.any { it.label == "A" })
        assertTrue(shiftedKeys.any { it.action == KeyAction.SHIFT })
    }
}
