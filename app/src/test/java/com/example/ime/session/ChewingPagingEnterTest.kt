package com.example.ime.session

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class PagingFakeEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    private var page = 0
    private var activeComposition = false
    var commitCalls = 0

    private val pages = listOf(
        listOf("尼", "泥"),
        listOf("你", "擬"),
    )

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() {}
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate = EngineUpdate(
        consumed = true,
        preedit = "ㄋㄧ",
        candidates = pages[page],
        committedText = ""
    ).also { activeComposition = true }
    override fun backspaceUpdate(): EngineUpdate = EngineUpdate(
        consumed = false, preedit = "", candidates = emptyList(), committedText = ""
    )
    override fun selectCandidateUpdate(index: Int): EngineUpdate = EngineUpdate(
        consumed = false, preedit = "", candidates = emptyList(), committedText = ""
    )
    override fun commitUpdate(): EngineUpdate {
        commitCalls++
        page = 0
        activeComposition = false
        return EngineUpdate(
            consumed = true,
            preedit = "",
            candidates = emptyList(),
            committedText = "尼"
        )
    }
    override fun nextPageUpdate(): EngineUpdate? {
        if (page >= pages.size - 1) return null
        page++
        return EngineUpdate(consumed = true, preedit = "ㄋㄧ", candidates = pages[page], committedText = "")
    }
    override fun prevPageUpdate(): EngineUpdate? {
        if (page <= 0) return null
        page--
        return EngineUpdate(consumed = true, preedit = "ㄋㄧ", candidates = pages[page], committedText = "")
    }
    override fun canPageCandidatesBackward(): Boolean = activeComposition && page > 0
    override fun canPageCandidatesForward(): Boolean =
        activeComposition && page < pages.size - 1
    override fun close() {}
}

/** D3e-7: Enter completes the decoder composition, never the page by UI guess. */
class ChewingPagingEnterTest {

    @Test
    fun `page forward then enter commits decoder text once with projected paging state`() {
        val engine = PagingFakeEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        val paged = session.dispatch(ImeCommand.PageForward)
        assertTrue(paged.consumed)
        assertEquals(listOf("你", "擬"), session.state.value.candidates)
        val result = session.dispatch(ImeCommand.Enter)
        assertTrue(result.consumed)
        assertEquals(1, engine.commitCalls)
        assertEquals(listOf("尼"), orderedCommits(result))
        assertEquals("", session.state.value.preedit)
        assertTrue(session.state.value.candidates.isEmpty())
        assertFalse(session.state.value.canPageBackward)
        assertFalse(session.state.value.canPageForward)
    }

    @Test
    fun `page forward backward then enter commits once with no stale page`() {
        val engine = PagingFakeEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        session.dispatch(ImeCommand.PageForward)
        val back = session.dispatch(ImeCommand.PageBackward)
        assertTrue(back.consumed)
        assertEquals(listOf("尼", "泥"), session.state.value.candidates)
        val result = session.dispatch(ImeCommand.Enter)
        assertEquals(listOf("尼"), orderedCommits(result))
        assertEquals(1, engine.commitCalls)
        assertTrue(session.state.value.candidates.isEmpty())
    }
}
