package com.example.ime.service

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private class StartupFakeEngine(var ready: Boolean = true) : ChewingEngine {
    override val isReady: Boolean get() = ready
    override val personalizedLearningEnabled: Boolean = false
    var initCalls = 0
    var closeCalls = 0

    private fun empty(consumed: Boolean) = EngineUpdate(
        consumed = consumed,
        preedit = "",
        candidates = emptyList(),
        committedText = "",
    )

    override fun init(layout: ChewingEngine.Layout) { initCalls++ }
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() {}
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
 * D3b RED: safe native startup falls back to DictionarySession on failure.
 * Fails now because NativeStartupGate does not exist yet.
 */
class NativeStartupGateTest {

    @Test
    fun `installer success plus ready engine returns engine`() {
        val engine = StartupFakeEngine(ready = true)
        val result = NativeStartupGate.start(
            install = { },
            openEngine = { engine },
        )
        assertEquals(engine, result)
        assertEquals(1, engine.initCalls)
    }

    @Test
    fun `installer failure returns null and never opens engine`() {
        var opened = 0
        val result = NativeStartupGate.start(
            install = { throw IllegalStateException("assets missing") },
            openEngine = { opened++; StartupFakeEngine() },
        )
        assertNull(result)
        assertEquals(0, opened)
    }

    @Test
    fun `unready engine is closed and returns null fallback`() {
        val engine = StartupFakeEngine(ready = false)
        val result = NativeStartupGate.start(
            install = { },
            openEngine = { engine },
        )
        assertNull(result)
        assertEquals(1, engine.closeCalls)
    }
}
