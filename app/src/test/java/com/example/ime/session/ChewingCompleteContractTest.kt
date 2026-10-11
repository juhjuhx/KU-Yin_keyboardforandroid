package com.example.ime.session

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class CompleteFakeEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    var commitCalls = 0

    private fun active(): EngineUpdate = EngineUpdate(
        consumed = true,
        preedit = "ㄋㄧ",
        candidates = listOf("尼", "泥"),
        committedText = ""
    )

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() {}
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate = active()
    override fun backspaceUpdate(): EngineUpdate = active()
    override fun selectCandidateUpdate(index: Int): EngineUpdate = active()
    override fun commitUpdate(): EngineUpdate {
        commitCalls++
        return EngineUpdate(
            consumed = true,
            preedit = "",
            candidates = emptyList(),
            committedText = "尼"
        )
    }
    override fun nextPageUpdate(): EngineUpdate? = null
    override fun prevPageUpdate(): EngineUpdate? = null
    override fun canPageCandidatesBackward(): Boolean = false
    override fun canPageCandidatesForward(): Boolean = false
    override fun close() {}
}

/** D3e-5 native side: Complete commits once and projects; empty is a no-op. */
class ChewingCompleteContractTest {

    @Test
    fun `complete on active native preedit commits once with clean projected state`() {
        val engine = CompleteFakeEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        val result = session.dispatch(ImeCommand.Complete)
        assertTrue(result.consumed)
        assertEquals(1, engine.commitCalls)
        assertEquals(listOf("尼"), orderedCommits(result))
        assertEquals("", session.state.value.preedit)
        assertTrue(session.state.value.candidates.isEmpty())
    }

    @Test
    fun `complete on empty native preedit has no text effect without touching engine`() {
        val engine = CompleteFakeEngine()
        val session = ChewingEngineSession(engine)
        val result = session.dispatch(ImeCommand.Complete)
        assertNull(result.commitText)
        assertFalse(result.consumed)
        assertEquals(0, engine.commitCalls)
    }
}
