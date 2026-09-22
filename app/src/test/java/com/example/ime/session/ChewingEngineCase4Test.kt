package com.example.ime.session

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class ContinuingFakeEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    val handledKeys = mutableListOf<Int>()
    var resetCalls = 0

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() { resetCalls++ }
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate {
        handledKeys.add(keyCode)
        return EngineUpdate(
            consumed = true,
            preedit = "ㄋㄧ",
            candidates = listOf("尼", "泥"),
            committedText = ""
        )
    }

    override fun backspaceUpdate(): EngineUpdate =
        EngineUpdate(consumed = false, preedit = "", candidates = emptyList(), committedText = "")

    override fun selectCandidateUpdate(index: Int): EngineUpdate =
        EngineUpdate(consumed = true, preedit = "ㄋㄧ", candidates = listOf("旎"), committedText = "你")

    override fun commitUpdate(): EngineUpdate =
        EngineUpdate(consumed = true, preedit = "", candidates = emptyList(), committedText = "ㄋㄧ")

    override fun nextPageUpdate(): EngineUpdate? = null
    override fun prevPageUpdate(): EngineUpdate? = null
    override fun canPageCandidatesBackward(): Boolean = false
    override fun canPageCandidatesForward(): Boolean = false
    override fun close() {}
}

class ChewingEngineCase4Test {

    @Test
    fun `select mid-composition then continue typing without reset`() {
        val engine = ContinuingFakeEngine()
        val session = ChewingEngineSession(engine)
        val commits = mutableListOf<String>()

        listOf(
            session.dispatch(ImeCommand.TapZhuyinKey("ㄋ")),
            session.dispatch(ImeCommand.SelectCandidate(0)),
            session.dispatch(ImeCommand.TapZhuyinKey("ㄏ"))
        ).mapNotNull { it.commitText }.forEach { commits.add(it) }

        assertEquals(listOf("你"), commits)
        assertEquals(2, engine.handledKeys.size)
        assertEquals(0, engine.resetCalls)
        assertEquals("ㄋㄧ", session.state.value.preedit)
        assertFalse(session.state.value.candidates.isEmpty())
    }

    @Test
    fun `out-of-range select is unconsumed and stateless`() {
        val engine = ContinuingFakeEngine()
        val session = ChewingEngineSession(engine)
        val result = session.dispatch(ImeCommand.SelectCandidate(9))
        assertFalse(result.consumed)
        assertEquals("", session.state.value.preedit)
    }
}
