package com.example.ime.service

import com.example.androidkeyboard.input.EditorPolicy

/** D3c: single source mapping EditorPolicy to native personalized learning. */
object NativeLearningPolicy {
    fun shouldEnableLearning(policy: EditorPolicy): Boolean =
        policy.allowPersonalizedLearning && !policy.forceAscii
}
