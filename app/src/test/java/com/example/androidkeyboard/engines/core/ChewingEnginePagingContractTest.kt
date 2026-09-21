package com.example.androidkeyboard.engines.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakePagingEngine(private val totalPages: Int) : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    private var page: Int = 0

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() { page = 0 }
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate =
        EngineUpdate(consumed = true, preedit = "", candidates = emptyList(), committedText = "")

    override fun backspaceUpdate(): EngineUpdate =
        EngineUpdate(consumed = true, preedit = "", candidates = emptyList(), committedText = "")

    override fun selectCandidateUpdate(index: Int): EngineUpdate =
        EngineUpdate(consumed = true, preedit = "", candidates = emptyList(), committedText = "")

    override fun commitUpdate(): EngineUpdate {
        page = 0
        return EngineUpdate(consumed = true, preedit = "", candidates = emptyList(), committedText = "")
    }

    override fun nextPageUpdate(): EngineUpdate? {
        if (page >= totalPages - 1) return null
        page++
        return EngineUpdate(consumed = true, preedit = "", candidates = emptyList(), committedText = "")
    }

    override fun prevPageUpdate(): EngineUpdate? {
        if (page <= 0) return null
        page--
        return EngineUpdate(consumed = true, preedit = "", candidates = emptyList(), committedText = "")
    }

    override fun canPageCandidatesBackward(): Boolean = page > 0
    override fun canPageCandidatesForward(): Boolean = page < totalPages - 1
    override fun close() {}
}

class ChewingEnginePagingContractTest {

    @Test
    fun `fresh session has no pages`() {
        val engine = FakePagingEngine(totalPages = 0)
        assertFalse(engine.canPageCandidatesBackward())
        assertFalse(engine.canPageCandidatesForward())
    }

    @Test
    fun `single page has no pages`() {
        val engine = FakePagingEngine(totalPages = 1)
        assertFalse(engine.canPageCandidatesBackward())
        assertFalse(engine.canPageCandidatesForward())
    }

    @Test
    fun `forward opens only backward`() {
        val engine = FakePagingEngine(totalPages = 3)
        assertFalse(engine.canPageCandidatesBackward())
        assertTrue(engine.canPageCandidatesForward())
        engine.nextPageUpdate()
        assertTrue(engine.canPageCandidatesBackward())
        assertTrue(engine.canPageCandidatesForward())
        engine.nextPageUpdate()
        assertTrue(engine.canPageCandidatesBackward())
        assertFalse(engine.canPageCandidatesForward())
    }
}
