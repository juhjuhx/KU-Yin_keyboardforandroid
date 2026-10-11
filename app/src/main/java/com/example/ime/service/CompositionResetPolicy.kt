package com.example.ime.service

object CompositionResetPolicy {
    fun shouldReset(
        hasComposing: Boolean,
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int
    ): Boolean {
        if (!hasComposing) return false
        return newSelStart != oldSelStart || newSelEnd != oldSelEnd
    }
}
