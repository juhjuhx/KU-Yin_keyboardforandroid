package io.github.juhjuhx.kuyin.acceptancehost

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/** Explicit P0-only suite. Frozen P1-G runner/manifest must not be used for this APK/test suite. */
@RunWith(AndroidJUnit4::class)
class CoreTypingAcceptanceTest {
    @Test fun zh01NoToneWoIsSelectableAndCommitted() = acceptanceCase("P0-ZH01") {
        launch(CaseConfig(caseId = "P0-ZH01", imeOptions = 6))
        selectMode(VisibleKeyboardMode.ZHUYIN)
        compose(listOf("ㄨ", "ㄛ"))
        tapCompletionCandidate("我", "我")
    }
    @Test fun zh02ThirdToneWoIsSelectableAndCommitted() = acceptanceCase("P0-ZH02") {
        launch(CaseConfig(caseId = "P0-ZH02", imeOptions = 6))
        selectMode(VisibleKeyboardMode.ZHUYIN)
        compose(listOf("ㄨ", "ㄛ", "ˇ"))
        tapCompletionCandidate("我", "我")
    }
    @Test fun zh04ContinuousNoTonePhraseCommitsExactlyOnce() = acceptanceCase("P0-ZH04") {
        launch(CaseConfig(caseId = "P0-ZH04", imeOptions = 6))
        selectMode(VisibleKeyboardMode.ZHUYIN)
        compose(listOf("ㄨ", "ㄛ", "ㄒ", "ㄧ", "ㄢ", "ㄗ", "ㄞ"))
        tapCompletionCandidate("我現在", "我現在")
    }
    @Test fun zh05ContinuousTonedPhraseCommitsExactlyOnce() = acceptanceCase("P0-ZH05") {
        launch(CaseConfig(caseId = "P0-ZH05", imeOptions = 6))
        selectMode(VisibleKeyboardMode.ZHUYIN)
        compose(listOf("ㄨ", "ㄛ", "ˇ", "ㄒ", "ㄧ", "ㄢ", "ˋ", "ㄗ", "ㄞ", "ˋ"))
        tapCompletionCandidate("我現在", "我現在")
    }
    @Test fun zh06CandidateThenContinuedPhrasePreservesFirstCommit() = acceptanceCase("P0-ZH06") {
        launch(CaseConfig(caseId = "P0-ZH06", imeOptions = 6))
        selectMode(VisibleKeyboardMode.ZHUYIN)
        compose(listOf("ㄨ", "ㄛ"))
        tapCompletionCandidate("我", "我")
        compose(listOf("ㄒ", "ㄧ", "ㄢ", "ˋ", "ㄗ", "ㄞ", "ˋ"))
        tapCompletionCandidate("現在", "我現在")
    }
    @Test fun en02EnglishDictionaryCompletionCommitsActualEditorWord() = acceptanceCase("P0-EN02") {
        launch(CaseConfig(caseId = "P0-EN02", imeOptions = 6))
        selectMode(VisibleKeyboardMode.ENGLISH)
        listOf("h", "e", "l", "l").forEach(::tapEnglishSoftKey)
        tapCompletionCandidate("hello", "hello")
    }
}
