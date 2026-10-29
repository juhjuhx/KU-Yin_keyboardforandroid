package io.github.juhjuhx.kuyin.acceptancehost

import android.content.Intent
import android.view.inputmethod.EditorInfo
import org.robolectric.Robolectric
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.NodeGenerationDevice
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], shadows = [NodeGenerationDevice.DeviceShadow::class, NodeGenerationDevice.GestureShadow::class])
class HostObserverGenerationTest {
    private val product = AcceptanceDriver.PRODUCT_PACKAGE
    private lateinit var device: UiDevice
    private var generation = 0

    @Before fun setup() {
        NodeGenerationDevice.rootSupplier = null
        NodeGenerationDevice.gestures = 0
        NodeGenerationDevice.onGesture = Runnable {}
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
    @After fun cleanup() {
        NodeGenerationDevice.rootSupplier = null
        NodeGenerationDevice.roots = emptyArray()
        NodeGenerationDevice.onGesture = Runnable {}
    }

    private fun node(text: String? = null, description: String? = null,
                     clickable: Boolean = false, bounds: Rect = Rect(0, 1400, 1080, 2400)): AccessibilityNodeInfo =
        AccessibilityNodeInfo.obtain().apply {
            packageName = product
            className = "android.view.View"
            this.text = text
            contentDescription = description
            isClickable = clickable
            isVisibleToUser = true
            setBoundsInScreen(bounds)
        }

    private fun tree(preedit: String, staleClear: Boolean = false): AccessibilityNodeInfo {
        val root = node()
        val row = node(bounds = Rect(0, 1450, 1080, 1500))
        val leaf = node(preedit, bounds = Rect(8, 1450, 850, 1500))
        val clear = node(clickable = true, bounds = Rect(900, 1450, 948, 1500))
        val icon = node(description = "清除", bounds = Rect(900, 1450, 948, 1500))
        if (staleClear) shadowOf(icon).setRefreshReturnValue(false)
        shadowOf(clear).addChild(icon)
        shadowOf(row).addChild(leaf)
        shadowOf(row).addChild(clear)
        shadowOf(root).addChild(row)
        shadowOf(root).addChild(node("ㄛ", clickable = true, bounds = Rect(200, 1600, 280, 1680)))
        return root
    }

    @Test fun canaryUsesRealUiObject2RefreshToThrowStaleAndFreshQueryFindsNewNode() {
        NodeGenerationDevice.roots = arrayOf(tree("ㄨ"))
        val old = device.findObject(By.pkg(product).text("ㄨ"))
        assertNotNull(old)
        assertEquals(Rect(8, 1450, 850, 1500), old.visibleBounds)
        val cached: AccessibilityNodeInfo = ReflectionHelpers.getField(old, "mCachedNode")
        shadowOf(cached).setRefreshReturnValue(false)
        generation++
        NodeGenerationDevice.roots = arrayOf(tree("ㄨㄛ"))
        assertThrows(StaleObjectException::class.java) { old.visibleBounds }
        val fresh = device.findObject(By.pkg(product).text("ㄨㄛ"))
        assertEquals("ㄨㄛ", fresh.text)
        assertEquals(1, generation)
    }

    @Test fun originalStrictPreeditReadsTheNonClickableLeafFromRealNodeTree() {
        NodeGenerationDevice.roots = arrayOf(tree("ㄨ"))
        val preedit = AcceptanceDriver("synthetic").awaitPreedit()
        assertEquals("ㄨ", preedit.text)
        assertEquals(Rect(8, 1450, 850, 1500), preedit.bounds)
        assertEquals(Rect(900, 1450, 948, 1500), preedit.clearBounds)
    }
    private fun generationChangeWithUnrelatedStaleNode(before: String, after: String, id: String) {
        val old = tree(before)
        val retired = node("retired diagnostics", bounds = Rect(0, 2200, 1080, 2300))
        shadowOf(old).addChild(retired)
        NodeGenerationDevice.roots = arrayOf(old)
        val oldHandle = device.findObject(By.pkg(product).text("retired diagnostics"))
        assertEquals("retired diagnostics", oldHandle.text)
        shadowOf(retired).setRefreshReturnValue(false)
        val cached: AccessibilityNodeInfo = ReflectionHelpers.getField(oldHandle, "mCachedNode")
        shadowOf(cached).setRefreshReturnValue(false)
        assertThrows(StaleObjectException::class.java) { oldHandle.visibleBounds }
        NodeGenerationDevice.onGesture = Runnable {
            generation++
            val current = tree(after)
            // A separate stale diagnostic node is not the preedit or key target.
            shadowOf(current).addChild(retired)
            NodeGenerationDevice.roots = arrayOf(current)
        }
        val driver = AcceptanceDriver(id)
        driver.tapPhonetic("ㄛ")
        val observed = driver.awaitPreedit()
        assertEquals(after, observed.text)
        assertEquals(1, generation)
        assertEquals("Never retry a click", 1, NodeGenerationDevice.gestures)
    }

    @Test fun zh01ObserverRequeriesAfterGenerationChange() = generationChangeWithUnrelatedStaleNode("ㄨ", "ㄨㄛ", "P0-ZH01")
    @Test fun zh02ObserverRequeriesAfterGenerationChange() = generationChangeWithUnrelatedStaleNode("ㄨ", "ㄨㄛ", "P0-ZH02")
    @Test fun zh04ObserverRequeriesAfterGenerationChange() = generationChangeWithUnrelatedStaleNode("ㄨ", "ㄨㄛ", "P0-ZH04")
    @Test fun zh05ObserverRequeriesAfterGenerationChange() = generationChangeWithUnrelatedStaleNode("ㄨ", "ㄨㄛ", "P0-ZH05")
    @Test fun b02ObserverRequeriesAfterGenerationChange() = generationChangeWithUnrelatedStaleNode("ㄋ", "ㄋㄛ", "B02")
    @Test fun b03ObserverRequeriesAfterGenerationChange() = generationChangeWithUnrelatedStaleNode("ㄋ", "ㄋㄛ", "B03")
    @Test fun d13ObserverRequeriesAfterGenerationChange() = generationChangeWithUnrelatedStaleNode("ㄋ", "ㄋㄛ", "D13")
    @Test fun d14ObserverRequeriesAfterGenerationChange() = generationChangeWithUnrelatedStaleNode("ㄋ", "ㄋㄛ", "D14")

    private fun staleClearTree(): AccessibilityNodeInfo = tree("", staleClear = true)

    @Test fun persistentStaleIsFailureWithCauseRatherThanInconclusive() {
        NodeGenerationDevice.roots = arrayOf(staleClearTree())
        val failure = assertThrows(AssertionError::class.java) { AcceptanceDriver("STALE").awaitPreedit() }
        assertTrue(failure.cause is StaleObjectException)
        assertEquals(0, NodeGenerationDevice.gestures)
    }

    @Test fun en02MissingBodyLeafAndPersistentStaleCleanupBothRemainVisible() {
        val driver = AcceptanceDriver("P0-EN02")
        NodeGenerationDevice.roots = arrayOf(tree(""))
        val body = assertThrows(CaseInconclusive::class.java) { driver.awaitPreedit() }
        NodeGenerationDevice.roots = arrayOf(staleClearTree())
        val cleanup = assertThrows(AssertionError::class.java) { driver.clearComposition() }
        val combined = checkNotNull(mergeCaseFailure(body, cleanup))
        assertSame(cleanup, combined)
        assertSame(body, combined.suppressed.single())
        assertTrue(combined.cause is StaleObjectException)
        assertFalse(combined.isInconclusive())
        assertEquals(0, NodeGenerationDevice.gestures)
    }

    @Test fun freshQueryCanRecoverTargetStaleWithoutReusingOldBounds() {
        var reads = 0
        NodeGenerationDevice.rootSupplier = java.util.function.Supplier {
            reads++
            arrayOf(if (reads == 1) tree("ㄨ", staleClear = true) else tree("ㄨㄛ"))
        }
        assertEquals("ㄨㄛ", AcceptanceDriver("TRANSIENT").awaitPreedit().text)
        assertTrue(reads >= 2)
        assertEquals(0, NodeGenerationDevice.gestures)
    }

    @Test fun tapBaselineRequeriesTransientStaleTargetBeforeExactlyOneGesture() {
        var reads = 0
        NodeGenerationDevice.rootSupplier = java.util.function.Supplier {
            reads++
            arrayOf(tree("ㄨ", staleClear = reads == 1))
        }
        NodeGenerationDevice.onGesture = Runnable {
            NodeGenerationDevice.rootSupplier = null
            NodeGenerationDevice.roots = arrayOf(tree("ㄨㄛ"))
        }
        val driver = AcceptanceDriver("BASELINE-TRANSIENT")
        driver.tapPhonetic("ㄛ")
        assertEquals("ㄨㄛ", driver.awaitPreedit().text)
        assertTrue(reads >= 2)
        assertEquals(1, NodeGenerationDevice.gestures)
    }

    @Test fun clearBaselineRequeriesTransientStaleTargetBeforeExactlyOneGesture() {
        var reads = 0
        NodeGenerationDevice.rootSupplier = java.util.function.Supplier {
            reads++
            arrayOf(tree("ㄨ", staleClear = reads == 1))
        }
        NodeGenerationDevice.onGesture = Runnable {
            NodeGenerationDevice.rootSupplier = null
            NodeGenerationDevice.roots = arrayOf(node())
        }
        AcceptanceDriver("CLEAR-TRANSIENT").clearComposition()
        assertTrue(reads >= 2)
        assertEquals(1, NodeGenerationDevice.gestures)
    }

    @Test fun tapRecoversPostClickTargetGenerationWithFreshQueryAndExactlyOneGesture() {
        NodeGenerationDevice.roots = arrayOf(tree("ㄨ"))
        var postReads = 0
        NodeGenerationDevice.onGesture = Runnable {
            NodeGenerationDevice.rootSupplier = java.util.function.Supplier {
                postReads++
                arrayOf(if (postReads == 1) tree("ㄨㄛ", staleClear = true) else tree("ㄨㄛ"))
            }
        }
        val driver = AcceptanceDriver("POST-TAP-TRANSIENT")
        driver.tapPhonetic("ㄛ")
        assertEquals("ㄨㄛ", driver.awaitPreedit().text)
        assertTrue(postReads >= 2)
        assertEquals(1, NodeGenerationDevice.gestures)
    }

    @Test fun postClickPersistentTargetStaleFailsWithOriginalCauseAndOneGesture() {
        NodeGenerationDevice.roots = arrayOf(tree("ㄨ"))
        NodeGenerationDevice.onGesture = Runnable { NodeGenerationDevice.roots = arrayOf(tree("ㄨㄛ", staleClear = true)) }
        val failure = assertThrows(AssertionError::class.java) { AcceptanceDriver("POST-TAP-PERSISTENT").tapPhonetic("ㄛ") }
        assertTrue(failure.cause is StaleObjectException)
        assertEquals(1, NodeGenerationDevice.gestures)
    }

    @Test fun ambiguousClickableOrZeroBoundsPreeditIsNeverAccepted() {
        for (variant in listOf("ambiguous", "clickable", "zero")) {
            // Shadow getChild returns a copy: replace the row with original fixture nodes.
            val row = node(bounds = Rect(0, 1450, 1080, 1500))
            val text = node("ㄨ", clickable = variant == "clickable",
                bounds = if (variant == "zero") Rect() else Rect(8, 1450, 850, 1500))
            val clear = node(description = "清除", clickable = true, bounds = Rect(900, 1450, 948, 1500))
            shadowOf(row).addChild(text); shadowOf(row).addChild(clear)
            if (variant == "ambiguous") shadowOf(row).addChild(node("ㄛ", bounds = Rect(8, 1450, 850, 1500)))
            val replacement = node(); shadowOf(replacement).addChild(row)
            NodeGenerationDevice.roots = arrayOf(replacement)
            assertThrows("variant=$variant", CaseInconclusive::class.java) { AcceptanceDriver(variant).awaitPreedit() }
            assertEquals(0, NodeGenerationDevice.gestures)
        }
    }

    @Test fun unchangedPreeditStillFailsAndNeverRetriesTheTap() {
        NodeGenerationDevice.roots = arrayOf(tree("ㄨ"))
        val failure = assertThrows(AssertionError::class.java) { AcceptanceDriver("UNCHANGED").tapPhonetic("ㄛ") }
        assertTrue(failure.message.orEmpty().contains("未更新 preedit"))
        assertEquals(1, NodeGenerationDevice.gestures)
    }

    @Test fun realHostEditTextFocusConnectionAndRecorderRemainIndependentOfNodeFixture() {
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, AcceptanceEditorActivity::class.java)
            .putExtra("caseId", "SYNTHETIC-EDITOR")
        val controller = Robolectric.buildActivity(AcceptanceEditorActivity::class.java, intent).setup()
        try {
            val activity = controller.get()
            val editor = activity.findViewById<RecordingEditText>(R.id.editor)
            editor.requestFocus()
            assertTrue(editor.hasFocus())
            val connection = checkNotNull(editor.onCreateInputConnection(EditorInfo()))
            assertTrue(activity.editorSnapshot.finalMetadata?.inputConnectionPresent == true)
            assertEquals("", activity.editorSnapshot.currentText)
            assertEquals(0, activity.editorSnapshot.commitCount)
            assertTrue(connection.commitText("我", 1))
            assertEquals("我", editor.text.toString())
            assertEquals("我", activity.editorSnapshot.currentText)
            assertEquals(1, activity.editorSnapshot.commitCount)
            assertEquals(0, activity.editorSnapshot.editorActionCount)
            // Robolectric EditText boundary only; not WMS focus or a served IME session.
        } finally { controller.pause().stop().destroy() }
    }

    private fun candidateTree(staleCandidate: Boolean = false): AccessibilityNodeInfo {
        val root = tree("你")
        // Shipped CandidateBar is a Column: preedit then a separate CandidateStrip row.
        val chip = node("你", clickable = true, bounds = Rect(12, 1510, 110, 1570))
        if (staleCandidate) shadowOf(chip).setRefreshReturnValue(false)
        shadowOf(root).addChild(chip)
        shadowOf(root).addChild(node("ㄅ", clickable = true, bounds = Rect(0, 1600, 100, 1680)))
        return root
    }

    @Test fun firstCandidateUsesShippedSeparateRowAndRequeriesBeforeOneTap() {
        var reads = 0
        NodeGenerationDevice.rootSupplier = java.util.function.Supplier {
            reads++
            arrayOf(candidateTree(staleCandidate = reads == 5))
        }
        NodeGenerationDevice.onGesture = Runnable {
            NodeGenerationDevice.rootSupplier = null
            NodeGenerationDevice.roots = arrayOf(candidateTree())
        }
        val driver = AcceptanceDriver("B02-CANDIDATE-ROW")
        // This action also observes the real synthetic Editor; no fake InputConnection.
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, AcceptanceEditorActivity::class.java)
            .putExtra("caseId", "B02-CANDIDATE-ROW")
        val scenario = androidx.test.core.app.ActivityScenario.launch<AcceptanceEditorActivity>(intent)
        ReflectionHelpers.setField(driver, "scenario", scenario)
        try {
            driver.tapFirstVisibleCandidate()
            assertTrue("candidate scan must discard its stale iteration", reads > 5)
            assertEquals("selection must never retry a gesture", 1, NodeGenerationDevice.gestures)
        } finally { scenario.close() }
    }

    @Test fun completionSelectorDiscardsStaleQueryAndUsesExactLabelInSeparateRow() {
        var reads = 0
        NodeGenerationDevice.rootSupplier = java.util.function.Supplier {
            reads++
            arrayOf(candidateTree(staleCandidate = reads == 1))
        }
        val target = AcceptanceDriver("P0-ZH01").p0SoftTarget("你") { it.visibleBounds.top >= 1500 }
        assertEquals("你", target.text)
        assertTrue("stale generation must be discarded", reads >= 2)
        assertEquals("observation must not tap", 0, NodeGenerationDevice.gestures)
    }

    @Test fun candidateTextInsideBandCannotAuthorizeAnAncestorHitboxOutsideBand() {
        val root = tree("你")
        shadowOf(root).addChild(node("ㄅ", clickable = true, bounds = Rect(0, 1600, 100, 1680)))
        val target = node(clickable = true, bounds = Rect(0, 1500, 1080, 1700))
        shadowOf(target).addChild(node("你", bounds = Rect(12, 1510, 110, 1570)))
        shadowOf(root).addChild(target)
        NodeGenerationDevice.roots = arrayOf(root)
        assertThrows(CaseInconclusive::class.java) { AcceptanceDriver("OUTSIDE-HITBOX").candidateTarget("你") }
        assertEquals(0, NodeGenerationDevice.gestures)
    }

    @Test fun persistentCandidateStaleFailsWithoutGesture() {
        NodeGenerationDevice.roots = arrayOf(candidateTree(staleCandidate = true))
        val error = assertThrows(AssertionError::class.java) { AcceptanceDriver("PERSISTENT").candidateTarget("你") }
        assertTrue(error.cause is StaleObjectException)
        assertEquals(0, NodeGenerationDevice.gestures)
    }

    @Test fun duplicateCandidateLabelDoesNotChooseAnArbitraryHitbox() {
        val root = candidateTree()
        shadowOf(root).addChild(node("你", clickable = true, bounds = Rect(130, 1510, 230, 1570)))
        NodeGenerationDevice.roots = arrayOf(root)
        assertThrows(CaseInconclusive::class.java) { AcceptanceDriver("AMBIGUOUS").candidateTarget("你") }
        assertEquals(0, NodeGenerationDevice.gestures)
    }

    @Test fun compositionEnterReplacesRealComposingRangeAndKeepsSurroundingText() = compositionCommitFixture(1)

    @Test fun compositionEnterRejectsDoubleCommit() = compositionCommitFixture(2)

    private fun compositionCommitFixture(commits: Int) {
        val intent = Intent(InstrumentationRegistry.getInstrumentation().targetContext, AcceptanceEditorActivity::class.java)
            .putExtra("caseId", "D13-COMPOSING").putExtra("initialText", "前後").putExtra("imeOptions", 4)
        val scenario = androidx.test.core.app.ActivityScenario.launch<AcceptanceEditorActivity>(intent)
        lateinit var connection: android.view.inputmethod.InputConnection
        scenario.onActivity {
            val editor = it.findViewById<RecordingEditText>(R.id.editor)
            editor.requestFocus()
            editor.setSelection(1)
            connection = checkNotNull(editor.onCreateInputConnection(EditorInfo()))
            assertTrue(connection.setComposingText("你", 1))
            assertEquals("前你後", it.editorSnapshot.currentText)
        }
        val root = tree("你")
        shadowOf(root).addChild(node("傳送", clickable = true, bounds = Rect(900, 2200, 1050, 2300)))
        NodeGenerationDevice.roots = arrayOf(root)
        NodeGenerationDevice.onGesture = Runnable {
            scenario.onActivity {
                repeat(commits) { index ->
                    if (index > 0) assertTrue(connection.setComposingRegion(1, 2))
                    assertTrue(connection.commitText("你", 1))
                }
            }
            NodeGenerationDevice.roots = arrayOf(node())
        }
        val driver = AcceptanceDriver("D13-COMPOSING")
        ReflectionHelpers.setField(driver, "scenario", scenario)
        try {
            val verify = { driver.assertCompositionEnter(EnterCase(CaseConfig(caseId = "D13-COMPOSING", imeOptions = 4), "傳送", 4), false) }
            if (commits == 1) verify() else assertThrows(AssertionError::class.java) { verify() }
            scenario.onActivity {
                assertEquals("前你後", it.editorSnapshot.currentText)
                assertEquals(commits, it.editorSnapshot.commitCount)
                assertEquals(0, it.editorSnapshot.editorActionCount)
            }
            assertEquals(1, NodeGenerationDevice.gestures)
        } finally { scenario.close() }
    }

}
