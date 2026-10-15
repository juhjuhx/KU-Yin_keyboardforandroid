package io.github.juhjuhx.kuyin.acceptancehost

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DachenAcceptanceTest {
    @Test fun b01CanonicalKeysAndShiRiHaveStrictPreeditEvidence() = acceptanceCase("B01") {
        launch(CaseConfig(caseId = "B01", imeOptions = 4))
        selectMode(VisibleKeyboardMode.ZHUYIN)
        assertEmptyComposition()
        assertCanonicalDachenKeys()
        for (glyph in listOf("ㄕ", "ㄖ")) {
            val before = snapshot()
            tapPhonetic(glyph)
            assertEquals("實際 preedit 必須符合點擊的注音", glyph, awaitPreedit().text)
            clearComposition()
            val after = settledSnapshot()
            assertEquals("clear 不得送出文字", before.currentText, after.currentText)
            assertEquals("clear 不得呼叫 commitText", before.commitCount, after.commitCount)
            assertEquals("clear 不得派送 action", before.editorActionCount, after.editorActionCount)
            observe("tap-$glyph-clear", after)
        }
        scope = "已測當前可見寬度／方向的 41 canonical keys 與 ㄕ／ㄖ真點擊、strict preedit、clear；第二寬度與實體方向仍需人工觀察。"
        inconclusive("ADDITIONAL_WIDTH_AND_ORIENTATION_OBSERVATIONS_REQUIRED")
    }

    @Test fun b02RealCandidateSelectionThenContinuedComposition() = acceptanceCase("B02") {
        launch(CaseConfig(caseId = "B02", imeOptions = 4))
        selectMode(VisibleKeyboardMode.ZHUYIN)
        val start = snapshot()
        compose(listOf("ㄋ", "ㄧ", "ˇ"))
        val selected = tapFirstVisibleCandidate(start.currentText)
        assertEmptyComposition()
        assertEquals("candidate completion 必須恰好 commit 一次", start.commitCount + 1, settledSnapshot().commitCount)
        tapPhonetic("ㄅ")
        val continued = awaitPreedit().text
        assertEquals("後續真按鍵必須建立新組字", "ㄅ", continued)
        val after = settledSnapshot()
        assertEquals("後續組字不得遺失已提交 prefix", start.currentText + selected + "ㄅ", after.currentText)
        assertEquals("continue 不得重複 commit", start.commitCount + 1, after.commitCount)
        assertEquals("continue 不得派送 action", start.editorActionCount, after.editorActionCount)
        observe("candidate-then-continue", after)
        scope = "已測候選點擊實際提交一次、清空舊組字、後續 ㄅ建立新組字並保留 editor 已提交 prefix；external host 無法識別 native context/reset 身分。"
        inconclusive("NATIVE_CONTEXT_IDENTITY_UNOBSERVABLE")
    }

    @Test fun b03RealContinuousSentenceCandidateAndSingleFinalCommit() = acceptanceCase("B03") {
        val case = EnterCase(CaseConfig(caseId = "B03", imeOptions = 4), "傳送", 4)
        launch(case.config)
        selectMode(VisibleKeyboardMode.ZHUYIN)
        val start = snapshot()
        compose(listOf("ㄋ", "ㄧ", "ˇ"))
        val first = awaitPreedit().text
        compose(listOf("ㄏ", "ㄠ", "ˇ"))
        val sentence = awaitPreedit().text
        assertTrue("完整兩音節序列後可見 preedit 至少有兩個字元", sentence.length >= 2)
        note("two-syllable-observation", "soft sequence=ㄋ/ㄧ/ˇ/ㄏ/ㄠ/ˇ；first=$first；after=$sentence；不固定轉字或排名。")
        assertEquals("兩音節不得各自 commit", start.commitCount, settledSnapshot().commitCount)
        val selected = tapFirstVisibleCandidate(start.currentText)
        assertEmptyComposition()
        val completed = settledSnapshot()
        assertEquals("整句候選 completion 必須提交一次", start.commitCount + 1, completed.commitCount)
        assertEquals("整句候選不得派送 SEND", start.editorActionCount, completed.editorActionCount)
        tapPhonetic("ㄅ")
        assertEquals("完成句子後須可再次組字", "ㄅ", awaitPreedit().text)
        val after = settledSnapshot()
        assertEquals("下一次組字必須保留整句候選", start.currentText + selected + "ㄅ", after.currentText)
        assertEquals("下一次組字不得重複提交句子", start.commitCount + 1, after.commitCount)
        observe("continuous-sentence-and-next-composition", after)
        scope = "已觀察兩音節序列 strict preedit、候選點擊提交一次、下一次軟鍵組字保留 editor prefix；composition Enter 由 D13/D14 另測，未證明 native context 身分或整句語義／排名正確。"
        inconclusive("NATIVE_CONTEXT_AND_SENTENCE_SEMANTICS_UNOBSERVABLE")
    }
}
