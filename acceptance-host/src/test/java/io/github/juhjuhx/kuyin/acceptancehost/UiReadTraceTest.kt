package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.Test

class UiReadTraceTest {
    private class Stale : RuntimeException()
    @Test fun recordsFailingGetterBeforeRestoreAndPreservesExceptionIdentity() {
        var now = 10L
        val trace = UiReadTrace({ now }) { it is Stale }.apply { capture = 3; attempt = 7 }
        val original = Stale()
        assertSame(original, runCatching { trace.read(UiReadPhase.TEXT, 23) { now = 15; throw original } }.exceptionOrNull())
        trace.read(UiReadPhase.RESTORE_IDLE) { now = 20 }
        assertEquals(listOf(UiReadFailure(UiReadOwner.BODY, 3, 7, UiReadPhase.TEXT, 23, 5, true)), trace.failures)
        assertEquals(1, trace.staleCount)
    }
    @Test fun cleanupFailureDoesNotErasePrimaryGetterEvidence() {
        val trace = UiReadTrace({ 0L }) { it is Stale }
        runCatching { trace.read(UiReadPhase.REGISTER, 4) { throw Stale() } }
        trace.owner = UiReadOwner.CLEANUP
        runCatching { trace.read(UiReadPhase.PARENT, 6) { throw Stale() } }
        assertEquals(listOf(UiReadOwner.BODY, UiReadOwner.CLEANUP), trace.failures.map { it.owner })
        assertEquals(listOf(UiReadPhase.REGISTER, UiReadPhase.PARENT), trace.failures.map { it.phase })
    }
    @Test fun retainedFailuresAreBoundedButStaleTotalIsNotTruncated() {
        val trace = UiReadTrace({ 0L }) { it is Stale }
        repeat(20) { index -> runCatching { trace.read(UiReadPhase.CHILDREN, index) { throw Stale() } } }
        assertEquals(8, trace.failures.size)
        assertEquals((12..19).toList(), trace.failures.map { it.node })
        assertEquals(20, trace.staleCount)
    }
    @Test fun nonStaleFailureIsRecordedAndNeverReclassified() {
        val trace = UiReadTrace({ 0L }) { it is Stale }
        val original = AssertionError("not UI content")
        assertSame(original, runCatching { trace.read(UiReadPhase.IDLE) { throw original } }.exceptionOrNull())
        assertEquals(false, trace.failures.single().stale)
        assertEquals(0, trace.staleCount)
    }
    @Test fun successfulReadKeepsResultAndNoFailureEvidence() {
        val trace = UiReadTrace({ 0L }) { it is Stale }
        assertEquals("complete", trace.read(UiReadPhase.QUERY) { "complete" })
        assertTrue(trace.failures.isEmpty())
    }
    @Test fun repeatedCleanupReadsCannotEvictLastBodyFailure() {
        val trace = UiReadTrace({ 0L }) { it is Stale }
        runCatching { trace.read(UiReadPhase.BOUNDS, 44) { throw Stale() } }
        trace.owner = UiReadOwner.CLEANUP
        repeat(20) { index -> runCatching { trace.read(UiReadPhase.PARENT, index) { throw Stale() } } }
        assertEquals(listOf(UiReadOwner.BODY, UiReadOwner.CLEANUP), trace.lastFailuresByOwner.map { it.owner })
        assertEquals(listOf(44, 19), trace.lastFailuresByOwner.map { it.node })
    }
}
