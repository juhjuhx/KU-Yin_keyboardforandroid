package com.example.ime

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.ime.session.ChewingEngineSession
import com.example.ime.session.ImeCommand
import org.junit.Assert.*
import org.junit.Test

/** Decoder contract double; the separate native harness proves real decoding. */
internal class TypingContractEngine : ChewingEngine {
    override val isReady = true
    override val personalizedLearningEnabled = false
    var value = EngineUpdate(true, "", emptyList(), "")
    var commits = 0
    var selectionUnavailable: String? = null
    var commitUnavailable: String? = null
    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() { value = value.copy(preedit = "", candidates = emptyList(), committedText = "") }
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate {
        value = value.copy(preedit = "ㄨㄛ", candidates = listOf("我", "握"), committedText = "")
        return value
    }
    override fun selectCandidateUpdate(index: Int): EngineUpdate {
        selectionUnavailable?.let { return value.copy(unavailableReason = it) }
        value = value.copy(preedit = value.candidates[index], committedText = "")
        return value
    }
    override fun commitUpdate(): EngineUpdate {
        commits++
        commitUnavailable?.let { return value.copy(unavailableReason = it) }
        val text = value.preedit
        reset()
        return value.copy(committedText = text)
    }
    override fun backspaceUpdate(): EngineUpdate { reset(); return value }
    override fun nextPageUpdate(): EngineUpdate? = null
    override fun prevPageUpdate(): EngineUpdate? = null
    override fun canPageCandidatesBackward() = false
    override fun canPageCandidatesForward() = false
    override fun close() {}
}

class CoreTypingSessionTest {
    @Test fun `rejected native candidate cannot commit a different default word`() {
        val engine = TypingContractEngine().apply { selectionUnavailable = "候選已更新，請重新選擇" }
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val result = session.dispatch(ImeCommand.SelectCandidate(0))
        assertNull(result.commitText)
        assertEquals(0, engine.commits)
        assertEquals("ㄨㄛ", session.state.value.preedit)
        assertNotNull(session.state.value.unavailableReason)
    }
    @Test fun `unconvertible composition cannot be displaced by punctuation`() {
        val engine = TypingContractEngine().apply { commitUnavailable = "這組注音尚無可用候選" }
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val result = session.dispatch(ImeCommand.Punctuation("，"))
        assertNull(result.commitText)
        assertTrue(result.additionalCommits.isEmpty())
        assertEquals("ㄨㄛ", session.state.value.preedit)
    }
    @Test fun `candidate tap completes native replacement once`() {
        val engine = TypingContractEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val result = session.dispatch(ImeCommand.SelectCandidate(0))
        assertEquals("我", result.commitText)
        assertTrue(result.consumed)
        assertEquals(1, engine.commits)
        assertEquals("", session.state.value.preedit)
        assertNull(session.dispatch(ImeCommand.Complete).commitText)
        assertEquals(1, engine.commits)
    }
}
