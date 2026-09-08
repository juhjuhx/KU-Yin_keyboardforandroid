package com.example.androidkeyboard.engines.core

import com.example.androidkeyboard.input.KeyboardLayout

/**
 * Wave 4 T21: Platform-agnostic libchewing decoder interface.
 *
 * This interface is the boundary between UI and decoder layers.
 * UI code only talks to this interface; JNI calls are confined to
 * the Android implementation in engines.android.AndroidChewingEngine.
 *
 * iOS can provide its own implementation using the same interface.
 */
interface ChewingEngine {

    enum class Layout {
        DACHEN, HSU, Eten26
    }

    fun init(layout: Layout = Layout.DACHEN)
    fun reset()
    fun handleKeyEvent(keyCode: Int): Boolean
    fun getPreedit(): String
    fun getCandidates(): List<String>
    fun selectCandidate(index: Int)
    fun commit()
    fun backspace(): Boolean
    fun toggleFullHalf(): Boolean
    fun loadUserDict(path: String): Boolean
    fun saveUserDict(path: String)
    val isReady: Boolean
}
