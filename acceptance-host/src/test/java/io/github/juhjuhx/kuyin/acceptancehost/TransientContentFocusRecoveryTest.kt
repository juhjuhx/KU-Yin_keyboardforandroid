package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.Test

class TransientContentFocusRecoveryTest {
    private fun arm(p: TransientContentFocusRecovery) {
        p.focusChanged(true, true, true, true)
        p.focusChanged(false, false, true, true)
    }
    @Test fun restoresOnceOnlyAfterFormerFocusedContentRegainsArea() {
        val p = TransientContentFocusRecovery(); arm(p)
        assertFalse(p.layout(false, false, true, true, false, false))
        assertTrue(p.layout(true, true, true, true, false, false))
        assertFalse(p.layout(true, true, true, true, false, false))
    }
    @Test fun ordinaryFocusLossIsNotReversed() {
        val p = TransientContentFocusRecovery()
        p.focusChanged(true, true, true, true); p.focusChanged(false, true, true, true)
        assertFalse(p.layout(true, true, true, true, false, false))
    }
    @Test fun pauseOrWindowLossCancelsPendingRecovery() {
        val p = TransientContentFocusRecovery(); arm(p); p.cancel()
        assertFalse(p.layout(true, true, true, true, false, false))
        arm(p); assertFalse(p.layout(true, true, true, false, false, false))
        assertFalse(p.layout(true, true, true, true, false, false))
    }
    @Test fun neverStealsFocusAndDoesNotRetryLater() {
        val p = TransientContentFocusRecovery(); arm(p)
        assertFalse(p.layout(true, true, true, true, true, false))
        assertFalse(p.layout(true, true, true, true, false, false))
    }
    @Test fun noPreviousEditorFocusMeansNoRecovery() {
        val p = TransientContentFocusRecovery();p.focusChanged(false, false, true, true)
        assertFalse(p.layout(true, true, true, true, false, false))
    }
    @Test fun frameworkRecoveryConsumesPendingWork() {
        val p = TransientContentFocusRecovery();arm(p)
        p.focusChanged(true, true, true, true)
        assertFalse(p.layout(true, true, true, true, false, true))
    }
}
