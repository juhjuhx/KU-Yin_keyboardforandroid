package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.Test

class CompositionCompletionOracleTest {
    private val before = EditorSnapshot("D13", currentText = "seedㄋㄧˇtail", commitCount = 2)
    private val boundary = EditorCompositionBoundary(before, 4, 7)
    private val completed = before.copy(currentText = "seed你tail", commitCount = 3, lastCommitText = "你")

    @Test fun completionReplacesActualComposingIntervalAndPreservesBothSides() {
        val result = runCatching { assertCompositionCompletion(boundary, completed) }
        assertTrue("projection must be replaced, not appended: ${result.exceptionOrNull()}", result.isSuccess)
    }
    @Test fun emptyEditorProjectionIsReplacedOnce() {
        val projected = EditorSnapshot("D14", currentText = "ㄋㄧˇ")
        val after = projected.copy(currentText = "你", commitCount = 1, lastCommitText = "你")
        assertTrue(runCatching { assertCompositionCompletion(EditorCompositionBoundary(projected, 0, 3), after) }.isSuccess)
    }
    @Test fun alreadyConvertedProjectionCanCompleteWithoutChangingActualText() {
        val projected = before.copy(currentText = "seed你tail")
        assertTrue(runCatching { assertCompositionCompletion(EditorCompositionBoundary(projected, 4, 5), completed) }.isSuccess)
    }
    @Test fun oldAppendArtifactCannotPassReplacementOracle() {
        val wronglyAppended = completed.copy(currentText = "seedㄋㄧˇtail你")
        assertTrue(runCatching { assertCompositionCompletion(boundary, wronglyAppended) }.exceptionOrNull() is AssertionError)
    }
    @Test fun hostUtf16SpanBoundaryPreservesSupplementaryPrefix() {
        val projected = EditorSnapshot("D13", currentText = "𠮷ㄋㄧˇ")
        val after = projected.copy(currentText = "𠮷你", commitCount = 1, lastCommitText = "你")
        assertTrue(runCatching { assertCompositionCompletion(EditorCompositionBoundary(projected, 2, 5), after) }.isSuccess)
    }
    @Test fun missingRealHostSpanCannotPassAsAppendFallback() {
        val appended = completed.copy(currentText = "seedㄋㄧˇtail你")
        assertTrue(runCatching { assertCompositionCompletion(EditorCompositionBoundary(before, -1, -1), appended) }
            .exceptionOrNull() is AssertionError)
    }
    @Test fun replacementStillRequiresExactlyOneCommit() {
        for (count in listOf(2, 4)) {
            assertTrue(runCatching { assertCompositionCompletion(boundary, completed.copy(commitCount = count)) }
                .exceptionOrNull() is AssertionError)
        }
    }
    @Test fun firstCompositionEnterCannotAlsoDispatchActionOrNewline() {
        for (after in listOf(completed.copy(editorActionCount = 1), completed.copy(currentText = "seed你tail\n"))) {
            assertTrue(runCatching { assertCompositionCompletion(boundary, after) }.exceptionOrNull() is AssertionError)
        }
    }
    @Test fun compositionCompletionRequiresNonEmptyCommitText() {
        assertTrue(runCatching { assertCompositionCompletion(boundary, completed.copy(lastCommitText = "")) }
            .exceptionOrNull() is AssertionError)
    }
}
