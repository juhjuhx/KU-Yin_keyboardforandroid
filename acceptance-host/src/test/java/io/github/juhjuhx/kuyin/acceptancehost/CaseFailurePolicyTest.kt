package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.AssumptionViolatedException
import org.junit.Test

class CaseFailurePolicyTest {
    @Test fun noFailureRemainsSuccess() { assertNull(mergeCaseFailure(null, null)) }
    @Test fun solePrimaryIsPreserved() {
        val primary = AssertionError("assertion")
        assertSame(primary, mergeCaseFailure(primary, null))
    }
    @Test fun soleCleanupFailureMustFail() {
        val cleanup = AssertionError("cleanup failed")
        assertSame(cleanup, mergeCaseFailure(null, cleanup))
        assertFalse(cleanup.isInconclusive())
    }
    @Test fun genuineCleanupOverridesInconclusiveAndKeepsScopeReason() {
        val primary = CaseInconclusive("LEARNING_OBSERVER_OPEN")
        val cleanup = AssertionError("cleanup failed")
        val final = mergeCaseFailure(primary, cleanup)
        assertSame(cleanup, final)
        assertFalse(final!!.isInconclusive())
        assertArrayEquals(arrayOf(primary), final.suppressed)
    }
    @Test fun genuineCleanupOverridesJUnitAssumption() {
        val primary = AssumptionViolatedException("selector unavailable")
        val cleanup = IllegalStateException("scenario close failed")
        assertSame(cleanup, mergeCaseFailure(primary, cleanup))
        assertArrayEquals(arrayOf(primary), cleanup.suppressed)
    }
    @Test fun genuinePrimaryIsNotDowngradedByInconclusiveCleanup() {
        val primary = AssertionError("wrong SEND id")
        val cleanup = CaseInconclusive("BLOCKED_SELECTOR")
        assertSame(primary, mergeCaseFailure(primary, cleanup))
        assertArrayEquals(arrayOf(cleanup), primary.suppressed)
    }
    @Test fun twoGenuineFailuresPreserveBoth() {
        val primary = AssertionError("wrong newline")
        val cleanup = IllegalStateException("close failure")
        assertSame(primary, mergeCaseFailure(primary, cleanup))
        assertArrayEquals(arrayOf(cleanup), primary.suppressed)
    }
    @Test fun twoScopeLimitationsRemainInconclusiveWithBothReasons() {
        val primary = CaseInconclusive("HOST_FALSE_IPC_SCOPE")
        val cleanup = CaseInconclusive("BLOCKED_SELECTOR")
        assertSame(primary, mergeCaseFailure(primary, cleanup))
        assertTrue(primary.isInconclusive())
        assertArrayEquals(arrayOf(cleanup), primary.suppressed)
    }
    @Test fun sameThrowableDoesNotCreateSelfSuppression() {
        val primary = AssertionError("same")
        assertSame(primary, mergeCaseFailure(primary, primary))
        assertTrue(primary.suppressed.isEmpty())
    }
}
