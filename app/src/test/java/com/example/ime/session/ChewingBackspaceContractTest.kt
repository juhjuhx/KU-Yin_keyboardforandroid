package com.example.ime.session

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class BackspaceFakeEngine(private val consumes: Boolean) : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() {}
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate = EngineUpdate(
        consumed = true,
        preedit = "ㄋ",
        candidates = listOf("你"),
        committedText = ""
    )
    override fun backspaceUpdate(): EngineUpdate = if (consumes) {
        EngineUpdate(consumed = true, preedit = "", candidates = emptyList(), committedText = "")
    } else {
        EngineUpdate(consumed = false, preedit = "", candidates = emptyList(), committedText = "")
    }
    override fun selectCandidateUpdate(index: Int): EngineUpdate = EngineUpdate(
        consumed = false, preedit = "", candidates = emptyList(), committedText = ""
    )
    override fun commitUpdate(): EngineUpdate = EngineUpdate(
        consumed = false, preedit = "", candidates = emptyList(), committedText = ""
    )
    override fun nextPageUpdate(): EngineUpdate? = null
    override fun prevPageUpdate(): EngineUpdate? = null
    override fun canPageCandidatesBackward(): Boolean = false
    override fun canPageCandidatesForward(): Boolean = false
    override fun close() {}
}

/** D3e-3: native-consumed Backspace never reaches the editor; fallback is unconsumed. */
class ChewingBackspaceContractTest {

    @Test
    fun `native consumed backspace projects state and needs no editor delete`() {
        val session = ChewingEngineSession(BackspaceFakeEngine(consumes = true))
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        val result = session.dispatch(ImeCommand.Backspace)
        assertTrue(result.consumed)
        assertTrue(orderedCommits(result).isEmpty())
        assertEquals("", session.state.value.preedit)
    }

    @Test
    fun `unconsumed backspace leaves editor fallback path open`() {
        val session = ChewingEngineSession(BackspaceFakeEngine(consumes = false))
        val result = session.dispatch(ImeCommand.Backspace)
        assertFalse(result.consumed)
    }
}
