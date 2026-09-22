package com.example.ime.session

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeChewingEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    var handleCalls = 0
    var commitCalls = 0

    private fun tapped(): EngineUpdate = EngineUpdate(
        consumed = true,
        preedit = "ㄋ",
        candidates = listOf("你", "尼"),
        committedText = ""
    )

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() {}
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate {
        handleCalls++
        return tapped()
    }

    override fun backspaceUpdate(): EngineUpdate = tapped()
    override fun selectCandidateUpdate(index: Int): EngineUpdate =
        tapped().copy(committedText = "你")

    override fun commitUpdate(): EngineUpdate {
        commitCalls++
        return tapped().copy(preedit = "", candidates = emptyList(), committedText = "ㄋ")
    }

    override fun nextPageUpdate(): EngineUpdate? = null
    override fun prevPageUpdate(): EngineUpdate? = null
    override fun canPageCandidatesBackward(): Boolean = false
    override fun canPageCandidatesForward(): Boolean = false
    override fun close() {}
}

class ChewingEngineSessionTest {

    @Test
    fun `tap projects engine preedit and candidates with no commit`() {
        val session = ChewingEngineSession(FakeChewingEngine())
        assertNull(session.dispatch(ImeCommand.TapZhuyinKey("ㄋ")))
        val state = session.state.value
        assertEquals("ㄋ", state.preedit)
        assertEquals(listOf("你", "尼"), state.candidates)
    }

    @Test
    fun `select returns engine committed text`() {
        val engine = FakeChewingEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        assertEquals("你", session.dispatch(ImeCommand.SelectCandidate(0)))
    }

    @Test
    fun `space on empty preedit yields literal without touching engine`() {
        val engine = FakeChewingEngine()
        val session = ChewingEngineSession(engine)
        assertEquals(" ", session.dispatch(ImeCommand.Space))
        assertEquals(0, engine.commitCalls)
        assertTrue(session.state.value.candidates.isEmpty())
    }
}
