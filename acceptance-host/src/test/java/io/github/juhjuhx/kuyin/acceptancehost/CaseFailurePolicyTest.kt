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

    @Test fun pureCaseInconclusiveStillRoutesToJUnitSkip() {
        val original = CaseInconclusive("BLOCKED_SELECTOR", "bounded observation unavailable")
        val terminal = terminalCaseFailure("B02", original)
        assertTrue(original.isInconclusive())
        assertTrue(terminal is AssumptionViolatedException)
        assertSame(original, terminal.cause)
    }

    @Test fun pureJUnitAssumptionKeepsOriginalSkipIdentity() {
        val original = AssumptionViolatedException("unsupported editor scope")
        assertTrue(original.isInconclusive())
        assertSame(original, terminalCaseFailure("D17", original))
    }

    @Test fun caseInconclusiveWithRestoreFailureMustBeFail() {
        val original = CaseInconclusive("BLOCKED_SELECTOR").apply {
            addSuppressed(IllegalStateException("restore failure"))
        }
        assertFalse(original.isInconclusive())
    }

    @Test fun compositeCaseInconclusiveTerminalKeepsPrimaryAndSuppressed() {
        val restore = IllegalStateException("restore failure")
        val original = CaseInconclusive("BLOCKED_SELECTOR").apply { addSuppressed(restore) }
        val terminal = terminalCaseFailure("B02", original)
        assertSame(original, terminal)
        assertArrayEquals(arrayOf(restore), terminal.suppressed)
        assertFalse(terminal is AssumptionViolatedException)
    }

    @Test fun junitAssumptionWithRestoreFailureMustBeFail() {
        val original = AssumptionViolatedException("scope unavailable").apply {
            addSuppressed(IllegalStateException("restore failure"))
        }
        assertFalse(original.isInconclusive())
    }

    @Test fun compositeJUnitAssumptionRoutesToAssertionWithOriginalCause() {
        val restore = IllegalStateException("restore failure")
        val original = AssumptionViolatedException("scope unavailable").apply { addSuppressed(restore) }
        val terminal = terminalCaseFailure("D17", original)
        assertTrue(terminal is AssertionError)
        assertSame(original, terminal.cause)
        assertArrayEquals(arrayOf(restore), checkNotNull(terminal.cause).suppressed)
        assertFalse(terminal is AssumptionViolatedException)
    }

    @Test fun nestedSuppressedRestoreFailureCannotHideUnderScopeLimitations() {
        val nested = CaseInconclusive("BLOCKED_SELECTOR").apply {
            addSuppressed(IllegalStateException("restore failure"))
        }
        val original = CaseInconclusive("LEARNING_OBSERVER_OPEN").apply { addSuppressed(nested) }
        assertFalse(original.isInconclusive())
    }

    @Test fun onlySuppressedScopeLimitationsRemainInconclusive() {
        val limitation = CaseInconclusive("BLOCKED_SELECTOR")
        val original = CaseInconclusive("HOST_FALSE_IPC_SCOPE").apply { addSuppressed(limitation) }
        assertTrue(original.isInconclusive())
        val terminal = terminalCaseFailure("D17", original)
        assertTrue(terminal is AssumptionViolatedException)
        assertSame(original, terminal.cause)
        assertArrayEquals(arrayOf(limitation), checkNotNull(terminal.cause).suppressed)
    }

    @Test fun cyclicSuppressedLimitationsFailClosed() {
        val first = CaseInconclusive("FIRST")
        val second = CaseInconclusive("SECOND")
        first.addSuppressed(second)
        second.addSuppressed(first)
        assertFalse(first.isInconclusive())
    }

    @Test fun compositePrimaryIsPreservedWhenCleanupAlsoFails() {
        val restore = IllegalStateException("restore failure")
        val original = CaseInconclusive("BLOCKED_SELECTOR").apply { addSuppressed(restore) }
        val cleanup = AssertionError("cleanup failure")
        assertSame(original, mergeCaseFailure(original, cleanup))
        assertFalse(original.isInconclusive())
        assertArrayEquals(arrayOf(restore, cleanup), original.suppressed)
    }

    @Test fun ordinaryFailureTerminalRoutingKeepsOriginalIdentity() {
        val original = AssertionError("actual editor mismatch")
        assertSame(original, terminalCaseFailure("B02", original))
        assertFalse(original.isInconclusive())
    }
}
