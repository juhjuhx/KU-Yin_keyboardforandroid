package io.github.juhjuhx.kuyin.acceptancehost

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import org.junit.Assert.assertEquals
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

// P0 adapters reuse the host oracle and existing lifecycle/mode driver. Keeping
// these outside AcceptanceDriver preserves the frozen P1 instrumentation manifest.
private fun AcceptanceDriver.tapCompletionCandidate(label: String, expectedEditorText: String) {
    val before = snapshot()
    val preedit = awaitPreedit()
    val target = p0SoftTarget(label) {
        val rect = it.visibleBounds
        isInCandidateRegion(rect.regionBounds(), preedit.clearBounds.regionBounds(), observedKeyboardTop())
    }
    target.click()
    if (!p0Await {
            val after = snapshot()
            after.currentText == expectedEditorText && after.commitCount == before.commitCount + 1
        }) throw AssertionError("completion $label did not commit exactly once: ${snapshot()}")
    assertEmptyComposition()
    assertEquals("completion cannot also dispatch an editor action", before.editorActionCount,
        snapshot().editorActionCount)
    observe("completion-$label", snapshot())
}
private fun AcceptanceDriver.tapEnglishSoftKey(letter: String) {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val headers = device.findObjects(By.pkg(P0_PRODUCT_PACKAGE).desc("收起鍵盤"))
    if (headers.size != 1) inconclusive("BLOCKED_SELECTOR", "keyboard header identity is ambiguous")
    val target = p0SoftTarget(letter) { it.visibleBounds.top >= headers.single().visibleBounds.bottom }
    val before = snapshot().currentText
    target.click()
    if (!p0Await { snapshot().currentText != before })
        throw AssertionError("visible English key $letter did not update editor composition")
}
private fun AcceptanceDriver.p0SoftTarget(label: String, eligible: (UiObject2) -> Boolean): UiObject2 {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    var matches = emptyList<UiObject2>()
    if (!p0Await {
            matches = device.findObjects(By.pkg(P0_PRODUCT_PACKAGE).text(label)).mapNotNull { leaf ->
                var target: UiObject2? = leaf
                while (target != null && target.applicationPackage == P0_PRODUCT_PACKAGE && !target.isClickable)
                    target = target.parent
                target?.takeIf { it.applicationPackage == P0_PRODUCT_PACKAGE && it.isClickable && eligible(it) }
            }.distinctBy { it.visibleBounds.toShortString() }
            matches.size == 1
        }) inconclusive("BLOCKED_SELECTOR", "visible soft target $label is absent/ambiguous")
    return matches.single()
}
private fun p0Await(condition: () -> Boolean): Boolean {
    val deadline = SystemClock.uptimeMillis() + 8_000
    do {
        if (condition()) return true
        SystemClock.sleep(50)
    } while (SystemClock.uptimeMillis() < deadline)
    return false
}
private const val P0_PRODUCT_PACKAGE = "io.github.juhjuhx.kuyin"
