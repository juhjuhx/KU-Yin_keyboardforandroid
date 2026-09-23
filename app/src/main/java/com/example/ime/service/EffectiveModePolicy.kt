package com.example.ime.service

import com.example.androidkeyboard.input.EditorPolicy
import com.example.ime.engine.KeyboardMode

/**
 * A5: forced ASCII (password / FORCE_ASCII) is a temporary effective-mode
 * override. The user's preferred mode is stored separately and never
 * overwritten by forced inputs, so returning to a normal editor restores it.
 */
object EffectiveModePolicy {
    fun effectiveMode(userPreferred: KeyboardMode, policy: EditorPolicy): KeyboardMode =
        if (policy.isSensitive || policy.forceAscii) KeyboardMode.ENGLISH else userPreferred
}
