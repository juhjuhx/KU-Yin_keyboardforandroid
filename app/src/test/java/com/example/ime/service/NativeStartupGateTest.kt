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
 *
 * P1-A: factory/init failures must fall back to null (single init ownership).
 */
class NativeStartupGateTest {

    @Test
    fun `installer success plus ready engine returns engine with single init`() {
        val engine = StartupFakeEngine(ready = true)
        val owner = NativeChewingEngineOwner { engine }
        val result = NativeStartupGate.start(
            install = { },
            openEngine = { owner.ensureStarted() },
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
        val owner = NativeChewingEngineOwner { engine }
        val result = NativeStartupGate.start(
            install = { },
            openEngine = { owner.ensureStarted() },
        )
        assertNull(result)
        assertEquals(1, engine.closeCalls)
    }

    @Test
    fun `factory failure falls back to null without crash`() {
        var attempts = 0
        val result = NativeStartupGate.start(
            install = { },
            openEngine = { attempts++; throw IllegalStateException("libchewing paths missing") },
        )
        assertNull(result)
        assertEquals(1, attempts)
    }

    @Test
    fun `first init failure closes engine once and falls back`() {
        val engine = ThrowingInitFakeEngine()
        val owner = NativeChewingEngineOwner { engine }
        val result = NativeStartupGate.start(
            install = { },
            openEngine = { owner.ensureStarted() },
        )
        assertNull(result)
        assertEquals(1, engine.closeCalls)
    }

    @Test
    fun `failed creation retains no engine and next start can succeed`() {
        var attempts = 0
        val first = NativeStartupGate.start(
            install = { },
            openEngine = { attempts++; throw IllegalStateException("boom") },
        )
        assertNull(first)
        val good = StartupFakeEngine(ready = true)
        val goodOwner = NativeChewingEngineOwner { good }
        val second = NativeStartupGate.start(
            install = { },
            openEngine = { attempts++; goodOwner.ensureStarted() },
        )
        assertEquals(good, second)
        assertEquals(1, good.initCalls)
        assertEquals(2, attempts)
    }
}

private class ThrowingInitFakeEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    var closeCalls = 0

    private fun empty(consumed: Boolean) = EngineUpdate(
        consumed = consumed,
        preedit = "",
        candidates = emptyList(),
        committedText = "",
    )

    override fun init(layout: ChewingEngine.Layout): Unit =
        throw IllegalStateException("native init blew up")
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
