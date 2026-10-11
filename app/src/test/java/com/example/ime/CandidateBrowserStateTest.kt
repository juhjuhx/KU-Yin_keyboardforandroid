package com.example.ime

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.androidkeyboard.engines.core.EngineCandidatePage
import com.example.ime.session.*
import org.junit.Assert.*
import org.junit.Test

internal class BrowserTestEngine(private val pages: Int = 2, private val labels: List<String>? = null,
    private val preedit: String = "ㄨㄛ") : ChewingEngine {
    override val isReady = true
    override val personalizedLearningEnabled = false
    var page = 0
    var selectionCalls = 0
    var commits = 0
    var queries = 0
    private fun update(): EngineUpdate {
        val choices = labels ?: listOf(if (page == 0) "我" else "握", "窩")
        return EngineUpdate(true, preedit, choices, "",
            candidatePage = EngineCandidatePage(page, 10, pages, choices.indices.map { "choice-$it" }))
    }
    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() { page = 0 }
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate { page = 0; queries++; return update() }
    override fun backspaceUpdate() = update()
    override fun selectCandidateUpdate(index: Int): EngineUpdate { selectionCalls++; return update() }
    override fun commitUpdate(): EngineUpdate { commits++; return EngineUpdate(true, "", emptyList(), if (page == 0) "我" else "握") }
    override fun nextPageUpdate(): EngineUpdate? { if (page == pages - 1) return null; page++; queries++; return update() }
    override fun prevPageUpdate(): EngineUpdate? { if (page == 0) return null; page--; queries++; return update() }
    override fun canPageCandidatesBackward() = page > 0
    override fun canPageCandidatesForward() = page < pages - 1
    override fun close() {}
}

class CandidateBrowserStateTest {
    @Test fun `stale generation reference is rejected before native selection`() {
        val engine = BrowserTestEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val stale = session.state.value.candidatePage!!.ref(0)!!.copy(snapshotGeneration = session.state.value.generation - 1)
        assertTrue(orderedCommits(session.dispatch(ImeCommand.SelectCandidate(0, stale))).isEmpty())
        assertEquals(0, engine.selectionCalls)
    }
    @Test fun `wrong page reference is rejected before native selection`() {
        val engine = BrowserTestEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val wrong = session.state.value.candidatePage!!.ref(0)!!.copy(pageIndex = 99)
        assertTrue(orderedCommits(session.dispatch(ImeCommand.SelectCandidate(0, wrong))).isEmpty())
        assertEquals(0, engine.selectionCalls)
    }
    @Test fun `session provides immutable candidate selection snapshot`() {
        val session = ChewingEngineSession(BrowserTestEngine())
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        assertNotNull(session.state.value.candidatePage)
    }
    @Test fun `old paging command cannot skip a second page`() {
        val engine = BrowserTestEngine(3)
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val first = session.state.value.candidatePage!!
        session.dispatch(ImeCommand.NextCandidatePage(first))
        assertFalse(session.dispatch(ImeCommand.NextCandidatePage(first)).consumed)
        assertEquals(1, engine.page)
        assertEquals(2, engine.queries)
    }
    @Test fun `returning to same page does not revive old tap and wrong provider is rejected`() {
        val engine = BrowserTestEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val first = session.state.value.candidatePage!!
        session.dispatch(ImeCommand.NextCandidatePage(first))
        session.dispatch(ImeCommand.PreviousCandidatePage(session.state.value.candidatePage!!))
        assertTrue(orderedCommits(session.dispatch(ImeCommand.SelectCandidate(0, first.ref(0)))).isEmpty())
        val current = session.state.value.candidatePage!!.ref(0)!!
        assertFalse(session.dispatch(ImeCommand.SelectCandidate(0, current.copy(providerIdentity = "other"))).consumed)
        assertFalse(session.dispatch(ImeCommand.SelectCandidate(0, current.copy(selectionIdentity = "other"))).consumed)
        assertEquals(0, engine.selectionCalls)
    }
    @Test fun `new composition resets page and invalidates snapshot`() {
        val session = ChewingEngineSession(BrowserTestEngine())
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val first = session.state.value.candidatePage!!
        session.dispatch(ImeCommand.NextCandidatePage(first))
        val second = session.state.value.candidatePage!!
        session.dispatch(ImeCommand.TapZhuyinKey("ㄛ"))
        assertEquals(0, session.state.value.candidatePage!!.pageIndex)
        assertNotEquals(second.compositionGeneration, session.state.value.candidatePage!!.compositionGeneration)
        assertFalse(session.dispatch(ImeCommand.SelectCandidate(0, second.ref(0))).consumed)
    }
    @Test fun `close invalidates pending tap without changing composition or querying decoder`() {
        val engine = BrowserTestEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val before = session.state.value
        session.dispatch(ImeCommand.OpenExpandedCandidates)
        session.dispatch(ImeCommand.CloseExpandedCandidates)
        assertEquals(before.preedit, session.state.value.preedit)
        assertEquals(before.candidates, session.state.value.candidates)
        assertFalse(session.dispatch(ImeCommand.SelectCandidate(0, before.candidatePage!!.ref(0))).consumed)
        assertEquals(1, engine.queries)
        assertEquals(0, engine.selectionCalls)
    }
    @Test fun `reset closes expanded browser`() {
        val session = ChewingEngineSession(BrowserTestEngine())
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        session.dispatch(ImeCommand.ToggleCandidatesExpanded)
        session.dispatch(ImeCommand.Reset)
        assertFalse(session.state.value.candidatesExpanded)
    }
    @Test fun `candidate commit closes expanded browser`() {
        val session = ChewingEngineSession(BrowserTestEngine())
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        session.dispatch(ImeCommand.ToggleCandidatesExpanded)
        assertEquals(listOf("我"), orderedCommits(session.dispatch(ImeCommand.SelectCandidate(0, session.state.value.candidatePage?.ref(0)))))
        assertFalse(session.state.value.candidatesExpanded)
    }
    @Test fun `empty composition cannot expose expanded browser`() {
        val session = ChewingEngineSession(BrowserTestEngine())
        session.dispatch(ImeCommand.ToggleCandidatesExpanded)
        assertFalse(session.state.value.candidatesExpanded)
    }
    @Test fun `expansion and collapse preserve preedit candidates mode and native queries`() {
        val engine = BrowserTestEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val before = session.state.value
        repeat(2) { assertTrue(orderedCommits(session.dispatch(ImeCommand.ToggleCandidatesExpanded)).isEmpty()) }
        assertEquals(before, session.state.value)
        assertEquals(1, engine.queries)
        assertEquals(0, engine.commits)
    }
    @Test fun `next previous and boundaries preserve composition and do not commit`() {
        val session = ChewingEngineSession(BrowserTestEngine())
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val first = session.state.value
        assertFalse(session.dispatch(ImeCommand.PageBackward).consumed)
        assertEquals(first, session.state.value)
        assertTrue(orderedCommits(session.dispatch(ImeCommand.PageForward)).isEmpty())
        assertEquals("握", session.state.value.candidates.first())
        assertEquals(first.preedit, session.state.value.preedit)
        assertFalse(session.dispatch(ImeCommand.PageForward).consumed)
        session.dispatch(ImeCommand.PageBackward)
        assertEquals(first.candidates, session.state.value.candidates)
    }
}
