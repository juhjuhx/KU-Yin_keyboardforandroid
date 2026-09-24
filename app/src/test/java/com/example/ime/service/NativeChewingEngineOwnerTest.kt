package com.example.ime.service

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Test

private class CountingFakeEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    var initCalls = 0
    var resetCalls = 0
    var closeCalls = 0

    private fun empty(consumed: Boolean) = EngineUpdate(
        consumed = consumed,
        preedit = "",
        candidates = emptyList(),
        committedText = "",
    )

    override fun init(layout: ChewingEngine.Layout) { initCalls++ }
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() { resetCalls++ }
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate = empty(true)
    override fun backspaceUpdate(): EngineUpdate = empty(false)
    override fun selectCandidateUpdate(index: Int): EngineUpdate = empty(false)
    override fun commitUpdate(): EngineUpdate = empty(false)
    override fun nextPageUpdate(): EngineUpdate? = null
    override fun prevPageUpdate(): EngineUpdate? = null
    override fun canPageCandidatesBackward(): Boolean = false
    override fun canPageCandidatesForward(): Boolean = false
    override fun close() { closeCalls++ }
}

/**
 * D3a RED: one service lifetime owns ONE native engine.
 * Fails now because NativeChewingEngineOwner does not exist yet.
 */
class NativeChewingEngineOwnerTest {

    @Test
    fun `repeated ensureStarted inits native engine exactly once`() {
        var creations = 0
        val engine = CountingFakeEngine()
        val owner = NativeChewingEngineOwner { creations++; engine }
        owner.ensureStarted()
        owner.ensureStarted()
        owner.ensureStarted()
        assertEquals(1, creations)
        assertEquals(1, engine.initCalls)
    }

    @Test
    fun `session reset does not recreate native engine`() {
        var creations = 0
        val engine = CountingFakeEngine()
        val owner = NativeChewingEngineOwner { creations++; engine }
        owner.ensureStarted()
        owner.resetSession()
        owner.resetSession()
        assertEquals(1, creations)
        assertEquals(1, engine.initCalls)
        assertEquals(2, engine.resetCalls)
    }

    @Test
    fun `close exactly once and repeated close does not double free`() {
        val engine = CountingFakeEngine()
        val owner = NativeChewingEngineOwner { engine }
        owner.ensureStarted()
        owner.close()
        owner.close()
        owner.close()
        assertEquals(1, engine.closeCalls)
    }
}
