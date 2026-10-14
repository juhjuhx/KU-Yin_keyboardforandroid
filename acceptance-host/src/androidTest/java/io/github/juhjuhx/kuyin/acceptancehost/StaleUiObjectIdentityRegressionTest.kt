package io.github.juhjuhx.kuyin.acceptancehost

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Test infrastructure only; not a P1 acceptance row. No key injection or IME settings mutation. */
@RunWith(AndroidJUnit4::class)
class StaleUiObjectIdentityRegressionTest {
    @Test fun destroyedEditorKeepsMapIdentityButCannotBeReadAsCurrentUi() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        val selector = By.res(AcceptanceDriver.HOST_PACKAGE, "editor")
        val intent = Intent(instrumentation.targetContext, AcceptanceEditorActivity::class.java)
            .putExtra("caseId", "R2-UIOBJECT-IDENTITY")
        val (node, existingMap) = ActivityScenario.launch<AcceptanceEditorActivity>(intent).use {
            assertTrue("synthetic editor must be visible", device.wait(Until.hasObject(selector), 5_000))
            val liveNode = checkNotNull(device.findObject(selector))
            liveNode to hashMapOf(liveNode to 23)
        }
        assertTrue("editor must actually disappear before testing stale identity", device.wait(Until.gone(selector), 5_000))
        val getterFailure = runCatching { node.visibleBounds }.exceptionOrNull()
        assertTrue("destroyed node must not count as a current UI observation", getterFailure is StaleObjectException)
        val lookup = runCatching { existingMap[node] }
        assertNull("identity hashing must not throw merely because the editor node is stale", lookup.exceptionOrNull())
        assertEquals(23, lookup.getOrNull())
    }
}
