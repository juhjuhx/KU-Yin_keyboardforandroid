package com.example.ime.session

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class PagingIntegrityFakeEngine : ChewingEngine {
    override val isReady: Boolean = true
    override val personalizedLearningEnabled: Boolean = false
    private var page = 0
    val chosenGlobals = mutableListOf<Int>()
    private val pageSize = 2
    private val total = 5

    private fun pageItems(): List<String> {
        val start = page * pageSize
        val end = minOf(start + pageSize, total)
        if (start >= total) return emptyList()
        return (start until end).map { "c$it" }
    }

    private fun update(consumed: Boolean) = EngineUpdate(
        consumed = consumed,
        preedit = "ㄋㄧ",
        candidates = pageItems(),
        committedText = ""
    )

    override fun init(layout: ChewingEngine.Layout) {}
    override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
    override fun reset() {}
    override fun handleKeyUpdate(keyCode: Int): EngineUpdate = update(true)
    override fun backspaceUpdate(): EngineUpdate = update(false)
    override fun selectCandidateUpdate(index: Int): EngineUpdate {
        chosenGlobals.add(page * pageSize + index)
        return update(true)
    }
    override fun commitUpdate(): EngineUpdate = update(true)
    override fun nextPageUpdate(): EngineUpdate? {
        if ((page + 1) * pageSize >= total) return null
        page++
        return update(true)
    }
    override fun prevPageUpdate(): EngineUpdate? {
        if (page <= 0) return null
        page--
        return update(true)
    }
    override fun canPageCandidatesBackward(): Boolean = page > 0
    override fun canPageCandidatesForward(): Boolean = (page + 1) * pageSize < total
    override fun close() {}
}

/** D3g: rendered candidate index must address the native page it was drawn from. */
class ChewingPagingIntegrityTest {

    @Test
    fun `forward at last page is unconsumed with state unchanged`() {
        val session = ChewingEngineSession(PagingIntegrityFakeEngine())
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        session.dispatch(ImeCommand.PageForward)
        session.dispatch(ImeCommand.PageForward)
        val before = session.state.value
        val result = session.dispatch(ImeCommand.PageForward)
        assertFalse(result.consumed)
        assertEquals(before, session.state.value)
    }

    @Test
    fun `backward at first page is unconsumed`() {
        val session = ChewingEngineSession(PagingIntegrityFakeEngine())
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        assertFalse(session.dispatch(ImeCommand.PageBackward).consumed)
    }

    @Test
    fun `select on second page addresses global native index`() {
        val engine = PagingIntegrityFakeEngine()
        val session = ChewingEngineSession(engine)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        session.dispatch(ImeCommand.PageForward)
        assertEquals(listOf("c2", "c3"), session.state.value.candidates)
        session.dispatch(ImeCommand.SelectCandidate(1))
        assertEquals(listOf(3), engine.chosenGlobals)
    }

    @Test
    fun `page round trip restores first page with no stale flags`() {
        val session = ChewingEngineSession(PagingIntegrityFakeEngine())
        session.dispatch(ImeCommand.TapZhuyinKey("ㄋ"))
        session.dispatch(ImeCommand.PageForward)
        assertTrue(session.state.value.canPageBackward)
        session.dispatch(ImeCommand.PageBackward)
        assertEquals(listOf("c0", "c1"), session.state.value.candidates)
        assertFalse(session.state.value.canPageBackward)
        assertTrue(session.state.value.canPageForward)
    }
}
