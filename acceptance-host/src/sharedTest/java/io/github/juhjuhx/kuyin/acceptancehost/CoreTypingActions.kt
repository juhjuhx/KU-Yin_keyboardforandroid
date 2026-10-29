package io.github.juhjuhx.kuyin.acceptancehost

import android.os.SystemClock
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import org.junit.Assert.assertEquals

// P0 adapters share the real Host oracle/lifecycle between JVM and instrumentation.
// Frozen P1 binaries and manifest remain immutable; current sources have a separate ledger.
internal fun AcceptanceDriver.tapCompletionCandidate(label: String, expectedEditorText: String) {
    val before = snapshot()
    val target = candidateTarget(label)
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
internal fun AcceptanceDriver.tapEnglishSoftKey(letter: String) {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val target = p0SoftTarget(letter) { key ->
        // Requery the header in the same iteration; never retain a handle across polls.
        val headers = device.findObjects(By.pkg(P0_PRODUCT_PACKAGE).desc("收起鍵盤"))
        headers.size == 1 && key.visibleBounds.top >= headers.single().visibleBounds.bottom
    }
    val before = snapshot().currentText
    target.click()
    if (!p0Await { snapshot().currentText != before })
        throw AssertionError("visible English key $letter did not update editor composition")
}
internal fun AcceptanceDriver.p0SoftTarget(label: String, eligible: (UiObject2) -> Boolean): UiObject2 {
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
    var lastStale: androidx.test.uiautomator.StaleObjectException? = null
    do {
        try {
            val matched = condition()
            lastStale = null
            if (matched) return true
        } catch (error: androidx.test.uiautomator.StaleObjectException) {
            lastStale = error // Discard the whole query, never retry the gesture.
        }
        SystemClock.sleep(50)
    } while (SystemClock.uptimeMillis() < deadline)
    lastStale?.let { throw AssertionError("Accessibility node remained stale during bounded soft-target observation", it) }
    return false
}
private const val P0_PRODUCT_PACKAGE = "io.github.juhjuhx.kuyin"
