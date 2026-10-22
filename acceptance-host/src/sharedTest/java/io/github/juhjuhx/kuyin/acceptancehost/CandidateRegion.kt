package io.github.juhjuhx.kuyin.acceptancehost

/** Test-only copy of visible bounds; contains no AccessibilityNodeInfo owner. */
internal data class CandidateRegionBounds(val left: Int, val top: Int, val right: Int, val bottom: Int)

internal enum class CandidateKeyboardLayout { ZHUYIN, ENGLISH }

internal fun firstKeyboardRowTop(
    layout: CandidateKeyboardLayout?,
    visibleKeys: (String) -> List<CandidateRegionBounds>,
): Int? = when (layout) {
    CandidateKeyboardLayout.ZHUYIN -> listOf("ㄅ" to "ㄉ")
    CandidateKeyboardLayout.ENGLISH -> listOf("q" to "w", "Q" to "W")
    null -> emptyList()
}.mapNotNull { (first, second) ->
    val a = visibleKeys(first).singleOrNull() ?: return@mapNotNull null
    val b = visibleKeys(second).singleOrNull() ?: return@mapNotNull null
    if (a.top == b.top && a.bottom == b.bottom && a.right <= b.left) a.top else null
}.distinct().singleOrNull()

internal fun isInCandidateRegion(
    candidate: CandidateRegionBounds,
    clear: CandidateRegionBounds,
    keyboardTop: Int?,
): Boolean = keyboardTop != null && clear.bottom > clear.top && keyboardTop > clear.bottom &&
    candidate.right > candidate.left && candidate.bottom > candidate.top &&
    candidate.top >= clear.bottom && candidate.bottom <= keyboardTop
