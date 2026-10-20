package io.github.juhjuhx.kuyin.acceptancehost

import org.junit.Assert.*
import org.junit.Test

class VisibleUiAdapterTest {
    @Test fun semanticTokenRecognizesSpaceSeparatedHint() {
        assertTrue(semanticToken("ㄅ 1", "ㄅ"))
        assertTrue(semanticToken("ㄅ 1", "1"))
    }
    @Test fun semanticTokenRecognizesTabSeparatedHint() {
        assertTrue(semanticToken("ㄅ\t1", "ㄅ"))
        assertTrue(semanticToken("ㄅ\t1", "1"))
    }
    private fun node(id: Int, parent: Int? = null, text: String? = null, description: String? = null,
        clickable: Boolean = false, left: Int = 0, top: Int = 0, right: Int = 100, bottom: Int = 48,
        pkg: String = FIXTURE_PRODUCT_PACKAGE) = VisibleUiNode(id, parent, pkg, text, description,
        clickable, UiBounds(left, top, right, bottom))

    private fun currentStrip(extra: List<VisibleUiNode> = emptyList()) = VisibleUiSnapshot(listOf(
        node(0, right = 360, bottom = 400),
        node(1, 0, right = 360),
        node(2, 1, text = "ㄨㄛ", right = 170),
        node(3, 1, description = "清除", clickable = true, left = 180, right = 228),
        node(4, 0, text = "我", clickable = true, top = 52, bottom = 100),
        node(5, 0, text = "ㄅ\n1", clickable = true, top = 112, bottom = 160),
    ) + extra)

    @Test fun candidateBelowPreeditIsSelectable() {
        val ui = currentStrip()
        assertEquals(listOf("我"), ui.candidates(checkNotNull(ui.strictPreedit())).map { it.text })
    }
    @Test fun candidateRegionDoesNotSelectTheEnglishKeyboardLetter() {
        val ui = VisibleUiSnapshot(listOf(node(0, right = 360, bottom = 400), node(1, 0, right = 360),
            node(2, 1, text = "h", right = 170),
            node(3, 1, description = "清除", clickable = true, left = 180, right = 228),
            node(4, 0, text = "h", clickable = true, top = 52, bottom = 100),
            node(5, 0, text = "q", clickable = true, top = 112, bottom = 160),
            node(6, 0, text = "h", clickable = true, top = 164, bottom = 212)))
        assertEquals(listOf(4), ui.candidates(checkNotNull(ui.strictPreedit())).map { it.id })
    }
    @Test fun supplementaryHanCandidateIsNotFilteredByBmpRegex() {
        val ui = currentStrip(listOf(node(6, 0, text = "𠮷", clickable = true, left = 110, right = 160,
            top = 52, bottom = 100)))
        assertTrue(ui.candidates(checkNotNull(ui.strictPreedit())).any { it.text == "𠮷" })
    }
    @Test fun gridUsesPageControlsAsLowerBoundary() {
        val ui = VisibleUiSnapshot(listOf(node(0, right = 360, bottom = 400), node(1, 0, right = 360),
            node(2, 1, text = "ㄨㄛ", right = 170),
            node(3, 1, description = "清除", clickable = true, left = 180, right = 228),
            node(4, 1, description = "收合候選", clickable = true, left = 230, right = 278),
            node(5, 0, text = "握", clickable = true, top = 60, bottom = 108),
            node(6, 0, description = "上一頁候選", top = 260, bottom = 284)))
        assertEquals(listOf("握"), ui.candidates(checkNotNull(ui.strictPreedit())).map { it.text })
    }
    @Test fun missingKeyboardAndPagerDoesNotGuessACandidateRegion() {
        val ui = VisibleUiSnapshot(currentStrip().nodes.filter { it.id != 5 })
        assertTrue(ui.candidates(checkNotNull(ui.strictPreedit())).isEmpty())
    }
    @Test fun strictPreeditIsTheNonClickableLeafBesideClear() {
        assertEquals("ㄨㄛ", currentStrip().strictPreedit()?.text)
    }
    @Test fun keyboardGlyphAloneIsNotPreedit() {
        assertNull(VisibleUiSnapshot(currentStrip().nodes.filter { it.id != 2 }).strictPreedit())
    }
    @Test fun twoPreeditLeavesAreAmbiguous() {
        assertNull(currentStrip(listOf(node(6, 1, text = "另一文字", right = 170))).strictPreedit())
    }
    @Test fun clickablePreeditLeafIsRejected() {
        assertNull(VisibleUiSnapshot(currentStrip().nodes.map { if (it.id == 2) it.copy(clickable = true) else it }).strictPreedit())
    }
    @Test fun foreignPackageClearCannotEstablishPreedit() {
        assertNull(VisibleUiSnapshot(currentStrip().nodes.map { if (it.id == 3) it.copy(packageName = "foreign") else it }).strictPreedit())
    }
    @Test fun foreignPackageTextCannotEstablishPreedit() {
        assertNull(VisibleUiSnapshot(currentStrip().nodes.map { if (it.id == 2) it.copy(packageName = "foreign") else it }).strictPreedit())
    }
    @Test fun distinctOverlappingTargetsRemainAmbiguous() {
        val ui = currentStrip(listOf(node(6, 0, text = "我", clickable = true, top = 52, bottom = 100)))
        assertEquals(2, ui.candidates(checkNotNull(ui.strictPreedit())).size)
    }
    @Test fun equalLabelsKeepSeparateClickableTargets() {
        val ui = currentStrip(listOf(node(6, 0, text = "我", clickable = true, left = 110, right = 160,
            top = 52, bottom = 100)))
        assertEquals(2, ui.candidates(checkNotNull(ui.strictPreedit())).size)
    }
    private class StaleRead : RuntimeException()
    private class Clock { var now = 0L; fun pause(ms: Long) { now += ms } }
    @Test fun readRetriesTheWholeObservationAfterStale() {
        val clock = Clock()
        var attempts = 0
        val result = runCatching {
            freshUiRead(100, { clock.now }, clock::pause, { it is StaleRead }) {
                if (++attempts == 1) throw StaleRead()
                currentStrip().strictPreedit()?.text
            }
        }
        assertEquals("stale observation must be discarded and read afresh", "ㄨㄛ", result.getOrNull())
        assertEquals(2, attempts)
    }
    @Test fun continuouslyStaleObservationStopsAtTheSameDeadline() {
        val clock = Clock()
        val failure = runCatching {
            freshUiRead(100, { clock.now }, clock::pause, { it is StaleRead }) { throw StaleRead() }
        }.exceptionOrNull()
        assertTrue("bounded stale exhaustion must have a distinct read failure", failure is UiReadDeadline)
        assertEquals(100, clock.now)
    }
    @Test fun incompleteStaleReadCannotProveClearIsAbsent() {
        val clock = Clock()
        var attempt = 0
        val result = runCatching {
            freshUiRead(100, { clock.now }, clock::pause, { it is StaleRead }) {
                if (++attempt == 1) throw StaleRead()
                currentStrip().descriptionTargets("清除").isEmpty()
            }
        }
        assertEquals(false, result.getOrNull())
    }
    @Test fun observationDoesNotSwallowAssertionFailure() {
        val expected = AssertionError("actual editor mismatch")
        val clock = Clock()
        assertSame(expected, runCatching {
            freshUiRead(100, { clock.now }, clock::pause, { it is StaleRead }) { throw expected }
        }.exceptionOrNull())
    }
    @Test fun readThatFinishesAfterDeadlineIsNotAccepted() {
        val clock = Clock()
        val failure = runCatching {
            freshUiRead(100, { clock.now }, clock::pause, { it is StaleRead }) {
                clock.now = 101
                "late observation"
            }
        }.exceptionOrNull()
        assertTrue(failure is UiReadDeadline)
    }
    @Test fun visibleCandidateCompletionRequiresOneActualCommit() {
        val before = EditorSnapshot("B02", currentText = "ㄋㄧˇ")
        val after = before.copy(currentText = "你", commitCount = 1, lastCommitText = "你")
        val result = runCatching { assertCandidateCompletion(before, after, "你", "你") }
        assertTrue("current candidate tap completes once: ${result.exceptionOrNull()}", result.isSuccess)
    }
    @Test fun noCommitCannotPassAsCandidateCompletion() {
        val before = EditorSnapshot("B02", currentText = "ㄋㄧˇ")
        assertTrue(runCatching { assertCandidateCompletion(before, before.copy(currentText = "你"), "你", "你") }
            .exceptionOrNull() is AssertionError)
    }
    @Test fun lateDuplicateCommitFailsTheSettledCompletionOracle() {
        val before = EditorSnapshot("P0-ZH01")
        val after = before.copy(currentText = "我", commitCount = 2, lastCommitText = "我")
        assertTrue(runCatching { assertCandidateCompletion(before, after, "我", "我") }
            .exceptionOrNull() is AssertionError)
    }
    @Test fun candidateCompletionCannotDispatchAnEditorAction() {
        val before = EditorSnapshot("P0-ZH01")
        val after = before.copy(currentText = "我", commitCount = 1, lastCommitText = "我", editorActionCount = 1)
        assertTrue(runCatching { assertCandidateCompletion(before, after, "我", "我") }
            .exceptionOrNull() is AssertionError)
    }
    @Test fun completionPreservesPreviouslyCommittedPrefix() {
        val before = EditorSnapshot("P0-ZH06", currentText = "我ㄒㄧㄢˋㄗㄞˋ", commitCount = 1)
        val after = before.copy(currentText = "我現在", commitCount = 2, lastCommitText = "現在")
        assertTrue(runCatching { assertCandidateCompletion(before, after, "現在", "我現在") }.isSuccess)
    }
}
