package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.Test

class ScopedIdleReadTest {
    private class Clock(var now: Long = 0)
    private class StaleRead : RuntimeException()
    private class Control(val clock: Clock, val events: MutableList<String>, var timeout: Long = 10_000) : IdleReadControl {
        var idleElapsed = 0L
        var idleError: Throwable? = null
        var disableError: Throwable? = null
        var restoreError: Throwable? = null
        override fun getWaitForIdleTimeout(): Long { events += "get:$timeout"; return timeout }
        override fun setWaitForIdleTimeout(timeout: Long) {
            events += "set:$timeout"
            this.timeout = timeout
            (if (timeout == 0L) disableError else restoreError)?.let { throw it }
        }
        override fun waitForIdle(timeout: Long) {
            events += "idle:$timeout"
            clock.now += idleElapsed
            idleError?.let { throw it }
        }
    }

    @Test fun oneExplicitIdlePrecedesZeroTimeoutCaptureAndOriginalRestore() {
        val clock = Clock()
        val events = mutableListOf<String>()
        val control = Control(clock, events)
        val result = scopedIdleRead(100, { clock.now }, control) { events += "read"; "complete" }
        assertEquals("complete", result)
        assertEquals(listOf("get:10000", "idle:100", "set:0", "read", "set:10000"), events)
    }

    @Test fun captureGettersObserveZeroIdleTimeout() {
        val clock = Clock()
        val control = Control(clock, mutableListOf())
        val observed = scopedIdleRead(100, { clock.now }, control) { control.timeout }
        assertEquals(0L, observed)
    }

    @Test fun captureRestoresNondefaultOriginalTimeout() {
        val clock = Clock()
        val events = mutableListOf<String>()
        val control = Control(clock, events, 700)
        scopedIdleRead(100, { clock.now }, control) { "complete" }
        assertEquals(700L, control.timeout)
        assertEquals(listOf("get:700", "idle:100", "set:0", "set:700"), events)
    }

    @Test fun idleThatConsumesDeadlineCannotStartCapture() {
        val clock = Clock()
        val events = mutableListOf<String>()
        val control = Control(clock, events).apply { idleElapsed = 100 }
        var reads = 0
        val failure = runCatching { scopedIdleRead(100, { clock.now }, control) { reads++; "late" } }.exceptionOrNull()
        assertTrue(failure is UiReadDeadline)
        assertEquals(0, reads)
        assertEquals(listOf("get:10000", "idle:100", "set:10000"), events)
    }

    @Test fun expiredEntryCannotWaitOrCapture() {
        val clock = Clock(100)
        val events = mutableListOf<String>()
        val control = Control(clock, events)
        var reads = 0
        val failure = runCatching { scopedIdleRead(100, { clock.now }, control) { reads++ } }.exceptionOrNull()
        assertTrue(failure is UiReadDeadline)
        assertEquals(0, reads)
        assertEquals(listOf("get:10000", "set:10000"), events)
    }

    @Test fun lateCompletedCaptureIsRejectedAfterRestore() {
        val clock = Clock()
        val events = mutableListOf<String>()
        val control = Control(clock, events)
        val failure = runCatching { scopedIdleRead(100, { clock.now }, control) { clock.now = 101; "late" } }.exceptionOrNull()
        assertTrue(failure is UiReadDeadline)
        assertEquals(10_000L, control.timeout)
        assertEquals("set:10000", events.last())
    }

    @Test fun runtimeFailurePropagatesAfterRestore() {
        assertFailureRestores(IllegalStateException("capture failure"))
    }

    @Test fun assertionFailurePropagatesAfterRestore() {
        assertFailureRestores(AssertionError("strict evidence failure"))
    }

    @Test fun staleFailurePropagatesAfterRestore() {
        assertFailureRestores(StaleRead())
    }

    private fun assertFailureRestores(expected: Throwable) {
        val clock = Clock()
        val events = mutableListOf<String>()
        val control = Control(clock, events)
        val failure = runCatching { scopedIdleRead(100, { clock.now }, control) { throw expected } }.exceptionOrNull()
        assertSame(expected, failure)
        assertEquals(listOf("get:10000", "idle:100", "set:0", "set:10000"), events)
    }

    @Test fun restorationFailureIsSuppressedOnPrimaryCaptureFailure() {
        val clock = Clock()
        val primary = AssertionError("capture failure")
        val restore = IllegalStateException("restore failure")
        val control = Control(clock, mutableListOf()).apply { restoreError = restore }
        val failure = runCatching { scopedIdleRead(100, { clock.now }, control) { throw primary } }.exceptionOrNull()
        assertSame(primary, failure)
        assertArrayEquals(arrayOf(restore), checkNotNull(failure).suppressed)
    }

    @Test fun restorationFailureAfterSuccessfulCapturePropagates() {
        val clock = Clock()
        val restore = IllegalStateException("restore failure")
        val control = Control(clock, mutableListOf()).apply { restoreError = restore }
        assertSame(restore, runCatching { scopedIdleRead(100, { clock.now }, control) { "complete" } }.exceptionOrNull())
    }

    @Test fun partiallyAppliedDisableFailureStillRestoresOriginal() {
        val clock = Clock()
        val events = mutableListOf<String>()
        val expected = IllegalStateException("disable failure")
        val control = Control(clock, events).apply { disableError = expected }
        var reads = 0
        assertSame(expected, runCatching { scopedIdleRead(100, { clock.now }, control) { reads++ } }.exceptionOrNull())
        assertEquals(0, reads)
        assertEquals(10_000L, control.timeout)
        assertEquals("set:10000", events.last())
    }

    @Test fun idleFailurePropagatesAndRestoresOriginal() {
        val clock = Clock()
        val events = mutableListOf<String>()
        val expected = IllegalStateException("idle failure")
        val control = Control(clock, events).apply { idleError = expected }
        assertSame(expected, runCatching { scopedIdleRead(100, { clock.now }, control) { fail("must not capture") } }.exceptionOrNull())
        assertEquals(listOf("get:10000", "idle:100", "set:10000"), events)
    }

    @Test fun staleRetryRestoresBeforeEachAttemptAndKeepsSameDeadline() {
        val clock = Clock()
        val events = mutableListOf<String>()
        val control = Control(clock, events, 700).apply { idleElapsed = 10 }
        var attempts = 0
        val result = freshUiRead(100, { clock.now }, { events += "pause:$it"; clock.now += it },
            { retryableScopedStale(it) { error -> error is StaleRead } }) {
            scopedIdleRead(100, { clock.now }, control) {
                events += "read:${++attempts}"
                if (attempts == 1) throw StaleRead()
                "complete"
            }
        }
        assertEquals("complete", result)
        assertEquals(listOf("get:700", "idle:100", "set:0", "read:1", "set:700", "pause:50",
            "get:700", "idle:40", "set:0", "read:2", "set:700"), events)
    }

    @Test fun staleWithRestorationFailureCannotBeSwallowedByRetry() {
        val clock = Clock()
        val primary = StaleRead()
        val restore = IllegalStateException("restore failure")
        val control = Control(clock, mutableListOf()).apply { restoreError = restore }
        var attempts = 0
        val failure = runCatching {
            freshUiRead(100, { clock.now }, { clock.now += it },
                { retryableScopedStale(it) { error -> error is StaleRead } }) {
                scopedIdleRead(100, { clock.now }, control) {
                    if (++attempts == 1) throw primary
                    "complete"
                }
            }
        }.exceptionOrNull()
        assertSame(primary, failure)
        assertArrayEquals(arrayOf(restore), checkNotNull(failure).suppressed)
        assertEquals(1, attempts)
    }
}
