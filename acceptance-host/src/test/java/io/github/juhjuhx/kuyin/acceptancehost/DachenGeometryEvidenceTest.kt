package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.assertThrows
import org.junit.Test

class DachenGeometryEvidenceTest {
    private val editor = EditorSnapshot(caseId = "B01", currentText = "我", commitCount = 1)
    private fun frame(orientation: Int = 1, rotation: Int = 0, width: Int = 500): DachenGeometryFrame {
        val keys = (0 until 41).map { i ->
            val row = if (i < 11) 0 else 1 + (i - 11) / 10
            val col = if (i < 11) i else (i - 11) % 10
            UiBounds(col * 40, 100 + row * 50, col * 40 + 35, 145 + row * 50)
        }
        return DachenGeometryFrame(orientation, rotation, UiBounds(0, 0, width, 1000),
            UiBounds(0, 90, width, 400), keys)
    }
    private fun valid() = listOf(frame(), frame(2, 1, 900), frame())
    private fun rejects(frames: List<DachenGeometryFrame>) {
        assertThrows(AssertionError::class.java) { requireDachenGeometryCoverage(frames) }
    }
    private fun incomplete(frames: List<DachenGeometryFrame>) {
        assertThrows(CaseInconclusive::class.java) { requireDachenGeometryCoverage(frames) }
    }

    @Test fun portraitLandscapeAndReturnWithRealDifferentWidthsIsComplete() {
        requireDachenGeometryCoverage(valid())
    }
    @Test fun singleLayoutCannotCloseTheOriginalGap() = incomplete(listOf(frame()))
    @Test fun sameWidthIsNotASecondKeyboardWidth() = incomplete(listOf(frame(), frame(2, 1), frame()))
    @Test fun orientationRequestWithoutActualRotationIsNotEvidence() = incomplete(listOf(frame(), frame(2, 0, 900), frame()))
    @Test fun missingPortraitReturnIsIncomplete() = incomplete(valid().take(2))
    @Test fun returningPortraitMustObserveTheOriginalWidth() = incomplete(listOf(frame(), frame(2, 1, 900), frame(width = 600)))
    @Test fun missingCanonicalKeyIsRejected() = rejects(valid().map { it.copy(keys = it.keys.dropLast(1)) })
    @Test fun duplicatedHitboxIsRejected() = rejects(valid().map { it.copy(keys = it.keys.dropLast(1) + it.keys.first()) })
    @Test fun keyOutsideActualImeWindowIsRejected() = rejects(valid().map { it.copy(ime = UiBounds(0, 90, 400, 400)) })
    @Test fun overlappingTargetsAreRejected() = rejects(valid().map {
        it.copy(keys = it.keys.toMutableList().apply { this[1] = UiBounds(30, 100, 75, 145) })
    })
    @Test fun imeOutsideDisplayIsRejected() = rejects(valid().map { it.copy(ime = UiBounds(-1, 90, 500, 400)) })
    @Test fun rotationCannotCommitAgain() {
        assertThrows(AssertionError::class.java) { requireNoRotationEffects(editor, editor.copy(commitCount = 2)) }
    }
    @Test fun rotationCannotRewriteEditorText() {
        assertThrows(AssertionError::class.java) { requireNoRotationEffects(editor, editor.copy(currentText = "我我")) }
    }
    @Test fun rotationCannotDispatchAnAction() {
        assertThrows(AssertionError::class.java) { requireNoRotationEffects(editor, editor.copy(editorActionCount = 1)) }
    }
    @Test fun rotationCannotReplaceTheHostRecorder() {
        assertThrows(AssertionError::class.java) { requireNoRotationEffects(editor, editor.copy(caseId = "OTHER")) }
    }
    @Test fun metadataRevisionCanChangeWithoutAnEditorEffect() {
        requireNoRotationEffects(editor, editor.copy(revision = 10))
    }
}
