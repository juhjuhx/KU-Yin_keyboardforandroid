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
        tapFirstVisibleCandidate()
        val selected = awaitPreedit().text
        assertEquals("candidate select 不得自行 commit", start.commitCount, settledSnapshot().commitCount)
        tapPhonetic("ㄅ")
        val continued = awaitPreedit().text
        assertTrue("後續 preedit 必須可見保留已選內容", continued.contains(selected))
        assertTrue("後續真按鍵必須出現在 preedit", continued.contains("ㄅ"))
        assertEquals("continue 不得重複 commit", start.commitCount, settledSnapshot().commitCount)
        observe("candidate-then-continue", snapshot())
        scope = "已測真候選 UI 點擊後 preedit 與 ㄅ共存、editor 無重複 commit；external host 無法識別 native context/reset 身分。"
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
        tapFirstVisibleCandidate()
        awaitPreedit()
        assertEquals("修正不得提前 commit", start.commitCount, settledSnapshot().commitCount)
        assertCompositionEnter(case, performSecondEnter = false)
        tapPhonetic("ㄅ")
        assertEquals("完成句子後須可再次組字", "ㄅ", awaitPreedit().text)
        observe("continuous-sentence-and-next-composition", snapshot())
        scope = "已點擊完整兩音節序列並觀察 preedit、候選修正、單次完成 commit、下一次軟鍵組字；未證明完整 syllable coexist、native context 身分或整句語義／排名正確。"
        inconclusive("NATIVE_CONTEXT_AND_SENTENCE_SEMANTICS_UNOBSERVABLE")
    }
}
