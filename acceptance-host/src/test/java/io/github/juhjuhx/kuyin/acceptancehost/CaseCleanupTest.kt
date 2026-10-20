package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.Test

class CaseCleanupTest {
    @Test fun closeFailureDoesNotEraseEarlierClearFailure() {
        val clear = AssertionError("clear assertion")
        val close = IllegalStateException("close failure")
        val actual = runCatching { runCaseCleanup({ throw clear }, { throw close }) }.exceptionOrNull()
        assertSame(clear, actual)
        assertArrayEquals(arrayOf(close), checkNotNull(actual).suppressed)
    }
    @Test fun clearFailureCannotBecomeSkipWhenCloseIsInconclusive() {
        val clear = AssertionError("clear assertion")
        val actual = runCatching { runCaseCleanup({ throw clear }, { throw CaseInconclusive("gap") }) }.exceptionOrNull()
        assertFalse(checkNotNull(actual).isInconclusive())
        assertSame(clear, actual)
    }
    @Test fun inconclusiveClearCannotHideCloseFailure() {
        val clear = CaseInconclusive("gap")
        val close = IllegalStateException("close failure")
        val actual = runCatching { runCaseCleanup({ throw clear }, { throw close }) }.exceptionOrNull()
        assertSame(close, actual)
        assertArrayEquals(arrayOf(clear), checkNotNull(actual).suppressed)
    }
    @Test fun clearFailureStillAttemptsCloseExactlyOnce() {
        val clear = AssertionError("clear assertion")
        var calls = 0
        assertSame(clear, runCatching { runCaseCleanup({ throw clear }, { calls++ }) }.exceptionOrNull())
        assertEquals(1, calls)
    }
}
