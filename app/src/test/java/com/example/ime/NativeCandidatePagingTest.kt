package com.example.ime

import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.ime.session.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Actual pinned JNI, independent scratch page projections, not device evidence. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NativeCandidatePagingTest {
    private val engines = mutableListOf<AndroidChewingEngine>()
    private fun engine(): AndroidChewingEngine {
        val data = System.getProperty("kuyin.native.host.data").orEmpty()
        Assume.assumeTrue("Host-native gate required", data.isNotEmpty())
        return AndroidChewingEngine(data, ":memory:").also { engines += it; it.init(); assertTrue(it.isReady) }
    }
    @After fun close() { engines.forEach { it.close() } }
    private fun wo(engine: AndroidChewingEngine): EngineUpdate {
        engine.handleKeyUpdate('j'.code)
        return engine.handleKeyUpdate('i'.code)
    }
    @Test fun `real pages expose different candidates and return exactly to first page`() {
        val engine = engine()
        val first = wo(engine)
        assertTrue(first.candidates.contains("我"))
        assertEquals(10, first.candidatePage!!.pageSize)
        assertEquals(0, first.candidatePage.pageIndex)
        assertNull(engine.prevPageUpdate())
        assertTrue(engine.canPageCandidatesForward())
        val second = engine.nextPageUpdate()!!
        assertNotEquals(first.candidates, second.candidates)
        assertEquals(first.preedit, second.preedit)
        assertEquals(1, second.candidatePage!!.pageIndex)
        val last = second.candidatePage.totalPages!! - 1
        repeat(last - 1) { assertNotNull(engine.nextPageUpdate()) }
        assertNull(engine.nextPageUpdate())
        var current = second
        repeat(last) { current = engine.prevPageUpdate()!! }
        assertEquals(first, current)
        assertNull(engine.prevPageUpdate())
        assertFalse(engine.canPageCandidatesBackward())
    }
    @Test fun `second page selection resolves native identity rather than raw page offset`() {
        val engine = engine()
        wo(engine)
        val second = engine.nextPageUpdate()!!
        assertTrue(second.candidates.isNotEmpty())
        val label = second.candidates.first()
        val selected = engine.selectCandidateUpdate(0)
        assertNull(selected.unavailableReason)
        assertEquals(label, selected.preedit)
        assertEquals(label, engine.commitUpdate().committedText)
        assertEquals("", engine.commitUpdate().committedText)
    }
    @Test fun `page preview preserves actual live decoder history and next syllable`() {
        val paged = engine()
        val clean = engine()
        val first = wo(paged)
        assertEquals(first, wo(clean))
        val actions = AndroidChewingEngine::class.java.getDeclaredField("nativeActions").apply { isAccessible = true }
        val before = (actions.get(paged) as List<*>).toList()
        paged.nextPageUpdate()
        paged.prevPageUpdate()
        assertEquals(before, (actions.get(paged) as List<*>).toList())
        for (key in "vu0") assertEquals(clean.handleKeyUpdate(key.code), paged.handleKeyUpdate(key.code))
    }
    @Test fun `native session page refs reject old page and selection commits exactly once`() {
        val session = ChewingEngineSession(engine())
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        session.dispatch(ImeCommand.TapZhuyinKey("ㄛ"))
        val first = session.state.value.candidatePage!!
        val timing = System.nanoTime()
        assertTrue(orderedCommits(session.dispatch(ImeCommand.NextCandidatePage(first))).isEmpty())
        val elapsed = System.nanoTime() - timing
        val second = session.state.value.candidatePage!!
        println("AIE_HOST_PAGE_NS=$elapsed")
        assertEquals(first.compositionGeneration, second.compositionGeneration)
        assertNotEquals(first.snapshotGeneration, second.snapshotGeneration)
        assertTrue(orderedCommits(session.dispatch(ImeCommand.SelectCandidate(0, first.ref(0)))).isEmpty())
        assertEquals(second, session.state.value.candidatePage)
        val label = second.candidates.first().label
        assertEquals(listOf(label), orderedCommits(session.dispatch(ImeCommand.SelectCandidate(0, second.ref(0)))))
        assertTrue(orderedCommits(session.dispatch(ImeCommand.SelectCandidate(0, second.ref(0)))).isEmpty())
    }
}
