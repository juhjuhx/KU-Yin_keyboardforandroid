package com.example.androidkeyboard.engines.core

/** Immutable result of one decoder transition. */
data class EngineUpdate(
    val consumed: Boolean,
    val preedit: String,
    val candidates: List<String>,
    val committedText: String,
)

/**
 * Platform boundary for the Chewing decoder session used by the IME service.
 * JNI and filesystem details stay in the Android adapter.
 */
interface ChewingEngine {

    enum class Layout {
        DACHEN,
    }

    val isReady: Boolean

    fun init(layout: Layout = Layout.DACHEN)
    fun reset()
    fun handleKeyUpdate(keyCode: Int): EngineUpdate
    fun backspaceUpdate(): EngineUpdate
    fun selectCandidateUpdate(index: Int): EngineUpdate
    fun commitUpdate(): EngineUpdate
    fun nextPageUpdate(): EngineUpdate?
    fun prevPageUpdate(): EngineUpdate?
    fun close()
}
