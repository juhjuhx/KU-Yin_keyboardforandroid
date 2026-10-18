package io.github.juhjuhx.kuyin.acceptancehost

internal data class DachenGeometryFrame(
    val orientation: Int,
    val rotation: Int,
    val display: UiBounds,
    val ime: UiBounds,
    val keys: List<UiBounds>,
)

/** Test-only coverage oracle; completed captures, never requested orientation values. */
internal fun requireDachenGeometryCoverage(frames: List<DachenGeometryFrame>) {
    frames.forEach(::requireDachenGeometryFrame)
    fun requireCoverage(ok: Boolean, message: String) {
        if (!ok) throw CaseInconclusive("GEOMETRY_COVERAGE_INCOMPLETE", message)
    }
    requireCoverage(frames.map { it.orientation } == listOf(1, 2, 1), "actual portrait → landscape → portrait is required")
    val first = frames.first()
    requireCoverage(frames[1].rotation != first.rotation && frames.last().rotation == first.rotation,
        "actual display rotation and return are required")
    requireCoverage(frames[1].ime.right - frames[1].ime.left != first.ime.right - first.ime.left,
        "two distinct actual IME widths are required")
    requireCoverage(frames.last().ime.right - frames.last().ime.left == first.ime.right - first.ime.left,
        "return to the original actual IME width is required")
}

/** Validate completed measurements immediately, before any later observation can be inconclusive. */
internal fun requireDachenGeometryFrame(frame: DachenGeometryFrame) {
    fun requireEvidence(ok: Boolean, message: String) {
        if (!ok) throw AssertionError(message)
    }
    requireEvidence(frame.display.contains(frame.ime) && frame.ime.visible, "IME must be inside actual display")
    requireEvidence(frame.keys.size == 41 && frame.keys.distinct().size == 41, "41 distinct canonical targets are required")
    frame.keys.forEach { key -> requireEvidence(key.visible && frame.ime.contains(key), "key must be inside actual IME bounds") }
    for ((index, key) in frame.keys.withIndex()) {
        frame.keys.drop(index + 1).forEach { other ->
            requireEvidence(!(key.left < other.right && other.left < key.right &&
                key.top < other.bottom && other.top < key.bottom), "canonical targets must not overlap")
        }
    }
}

private fun UiBounds.contains(other: UiBounds) = visible && other.left >= left && other.top >= top &&
    other.right <= right && other.bottom <= bottom

internal fun requireNoRotationEffects(before: EditorSnapshot, after: EditorSnapshot) {
    if (before.caseId != after.caseId || before.currentText != after.currentText ||
        before.commitCount != after.commitCount || before.editorActionCount != after.editorActionCount ||
        before.newlineCount != after.newlineCount || before.lastCommitText != after.lastCommitText ||
        before.lastEditorActionId != after.lastEditorActionId) {
        throw AssertionError("orientation transition changed editor text, commit or action state")
    }
}
