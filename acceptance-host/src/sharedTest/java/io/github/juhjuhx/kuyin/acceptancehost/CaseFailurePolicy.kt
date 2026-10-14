package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.AssumptionViolatedException

internal fun runCaseCleanup(clear: () -> Unit, close: () -> Unit) {
    var failure: Throwable? = null
    try { clear() } catch (error: Throwable) { failure = error }
    try { close() } catch (error: Throwable) { failure = mergeCaseFailure(failure, error) }
    failure?.let { throw it }
}

internal class CaseInconclusive(val reason: String, detail: String = reason) : RuntimeException(detail)

/** 測試結果不得把真正 cleanup failure 降成 assumption。只供 test/androidTest。 */
internal fun mergeCaseFailure(primary: Throwable?, cleanup: Throwable?): Throwable? {
    if (primary == null) return cleanup
    if (cleanup == null || primary === cleanup) return primary
    if (primary.isInconclusive() && !cleanup.isInconclusive()) {
        cleanup.addSuppressed(primary)
        return cleanup
    }
    primary.addSuppressed(cleanup)
    return primary
}

internal fun Throwable.isInconclusive(): Boolean =
    isInconclusive(mutableSetOf())

private fun Throwable.isInconclusive(visiting: MutableSet<Throwable>): Boolean {
    if (this !is CaseInconclusive && this !is AssumptionViolatedException) return false
    if (!visiting.add(this)) return false
    val pure = suppressed.all { it.isInconclusive(visiting) }
    visiting.remove(this)
    return pure
}

/** Extracted terminal routing shared by the JVM regression and actual instrumentation. */
internal fun terminalCaseFailure(caseId: String, error: Throwable): Throwable = when (error) {
    is CaseInconclusive -> if (error.isInconclusive()) AssumptionViolatedException(
        "$caseId RESULT=INCONCLUSIVE REASON=${error.reason}: ${error.message}", error) else error
    is AssumptionViolatedException -> if (error.isInconclusive()) error else
        AssertionError("$caseId RESULT=FAIL: assumption contains a real failure", error)
    else -> error
}
