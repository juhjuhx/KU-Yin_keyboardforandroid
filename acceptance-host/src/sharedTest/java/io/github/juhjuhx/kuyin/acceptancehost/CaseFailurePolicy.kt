package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.AssumptionViolatedException

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
    this is CaseInconclusive || this is AssumptionViolatedException
