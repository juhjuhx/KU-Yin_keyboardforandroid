package com.example.ime.service

import com.example.androidkeyboard.input.EditorPolicy
import com.example.ime.engine.KeyboardMode

/**
 * A5/P1-C: restricted editors (non-text / password / FORCE_ASCII) temporarily
 * use English. The user's preferred mode is stored separately and never
 * overwritten by forced inputs, so returning to a normal editor restores it.
 */
object EffectiveModePolicy {
    fun effectiveMode(userPreferred: KeyboardMode, policy: EditorPolicy): KeyboardMode =
        if (!policy.allowComposition) KeyboardMode.ENGLISH else userPreferred
}
