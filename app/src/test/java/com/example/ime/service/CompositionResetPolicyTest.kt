package com.example.ime.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositionResetPolicyTest {

    @Test
    fun `cursor moved during composition resets`() {
        assertTrue(
            CompositionResetPolicy.shouldReset(
                hasComposing = true,
                oldSelStart = 5, oldSelEnd = 5,
                newSelStart = 2, newSelEnd = 2
            )
        )
    }

    @Test
    fun `selection changed during composition resets`() {
        assertTrue(
            CompositionResetPolicy.shouldReset(
                hasComposing = true,
                oldSelStart = 5, oldSelEnd = 5,
                newSelStart = 2, newSelEnd = 7
            )
        )
    }

    @Test
    fun `unchanged selection keeps composition`() {
        assertFalse(
            CompositionResetPolicy.shouldReset(
                hasComposing = true,
                oldSelStart = 5, oldSelEnd = 5,
                newSelStart = 5, newSelEnd = 5
            )
        )
    }

    @Test
    fun `no composition never resets`() {
        assertFalse(
            CompositionResetPolicy.shouldReset(
                hasComposing = false,
                oldSelStart = 5, oldSelEnd = 5,
                newSelStart = 9, newSelEnd = 9
            )
        )
    }

    @Test
    fun `ordinary commit callback does not double reset`() {
        assertFalse(
            CompositionResetPolicy.shouldReset(
                hasComposing = false,
                oldSelStart = 5, oldSelEnd = 5,
                newSelStart = 6, newSelEnd = 6
            )
        )
    }
}
