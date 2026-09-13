package com.example.androidkeyboard.engines.android

import org.junit.Assert.assertEquals
import org.junit.Test

class VisiblePreeditTest {

    @Test
    fun `bopomofo alone is visible`() {
        assertEquals("ㄅ", composeVisiblePreedit("", "ㄅ", 0))
    }

    @Test
    fun `bopomofo is inserted at libchewing cursor`() {
        assertEquals("你ㄏ好", composeVisiblePreedit("你好", "ㄏ", 1))
    }

    @Test
    fun `missing bopomofo preserves composition buffer`() {
        assertEquals("你好", composeVisiblePreedit("你好", "", 1))
    }

    @Test
    fun `cursor is safely clamped`() {
        assertEquals("你好ㄇ", composeVisiblePreedit("你好", "ㄇ", 99))
        assertEquals("ㄇ你好", composeVisiblePreedit("你好", "ㄇ", -1))
    }
}
