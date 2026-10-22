package io.github.juhjuhx.kuyin.acceptancehost

/** Test-only copy of visible bounds; contains no AccessibilityNodeInfo owner. */
internal data class CandidateRegionBounds(val left: Int, val top: Int, val right: Int, val bottom: Int)

internal fun isInCandidateRegion(
    candidate: CandidateRegionBounds,
    clear: CandidateRegionBounds,
    keyboardTop: Int?,
): Boolean = keyboardTop != null && clear.bottom > clear.top && keyboardTop > clear.bottom &&
    candidate.right > candidate.left && candidate.bottom > candidate.top &&
    candidate.top >= clear.bottom && candidate.bottom <= keyboardTop
