package io.github.juhjuhx.kuyin.acceptancehost

internal interface IdleReadControl {
    fun getWaitForIdleTimeout(): Long
    fun setWaitForIdleTimeout(timeout: Long)
    fun waitForIdle(timeout: Long)
}

private val scopedIdleReadLock = Any()

/** Configurator is process-wide. Only observation belongs in this serialized scope. */
internal fun <T> scopedIdleRead(deadline: Long, now: () -> Long,
    control: IdleReadControl, read: () -> T): T = synchronized(scopedIdleReadLock) {
    val original = control.getWaitForIdleTimeout()
    var primary: Throwable? = null
    val result = try {
        val remaining = deadline - now()
        if (remaining <= 0) throw UiReadDeadline()
        control.waitForIdle(remaining)
        // UIAutomator logs and swallows its idle timeout, so returning is not idle success.
        if (now() >= deadline) throw UiReadDeadline()
        control.setWaitForIdleTimeout(0)
        val captured = read()
        if (now() > deadline) throw UiReadDeadline()
        captured
    } catch (error: Throwable) {
        primary = error
        throw error
    } finally {
        try {
            control.setWaitForIdleTimeout(original)
        } catch (restoreError: Throwable) {
            val failure = primary
            if (failure == null) throw restoreError
            if (failure !== restoreError) failure.addSuppressed(restoreError)
        }
    }
    if (now() > deadline) throw UiReadDeadline()
    result
}

internal fun retryableScopedStale(error: Throwable, isStale: (Throwable) -> Boolean): Boolean =
    isStale(error) && error.suppressed.isEmpty()
