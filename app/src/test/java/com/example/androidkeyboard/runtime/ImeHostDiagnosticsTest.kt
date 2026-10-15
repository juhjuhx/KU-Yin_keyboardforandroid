package com.example.androidkeyboard.runtime

import android.content.Intent
import android.os.Looper
import android.view.inputmethod.EditorInfo
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLog
import org.robolectric.shadows.ShadowViewRootImpl
import org.robolectric.shadow.api.Shadow
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImeHostDiagnosticsTest {
    @Before fun clearLogs() = ShadowLog.clear()

    private fun activity(enabled: Boolean = true): ImeHostActivity {
        val intent = Intent(RuntimeEnvironment.getApplication(), ImeHostActivity::class.java)
            .putExtra("KUYIN_R2_DIAGNOSTICS", enabled)
        return Robolectric.buildActivity(ImeHostActivity::class.java, intent).setup().windowFocusChanged(true).get()
    }

    private fun events() = ShadowLog.getLogsForTag("KUYIN_R2_TEST_ONLY")
        .map { JSONObject(it.msg) }

    @Test fun recordsExistingShowResultWithoutAddingAnotherShowRequest() {
        val host = activity()
        host.requestIme()
        shadowOf(Looper.getMainLooper()).idle()
        val events = events()
        assertEquals(1, events.count { it.getString("event") == "INSETS_SHOW_REQUEST" })
        val result = events.single { it.getString("event") == "IMM_SHOW_RESULT" }
        assertTrue(result.has("accepted"))
        assertTrue(result.has("window_focused"))
        assertTrue(result.has("attached"))
        assertTrue(result.has("token_present"))
    }

    @Test fun observesFinalEditorInfoAfterRealEditTextConnectionCreation() {
        val host = activity()
        val info = EditorInfo()
        val connection = host.editor.onCreateInputConnection(info)
        assertNotNull(connection)
        val connections = events().filter { it.getString("event") == "INPUT_CONNECTION_CREATED" }
        assertFalse("final EditorInfo observation must actually execute", connections.isEmpty())
        val event = connections.last()
        assertTrue(event.getBoolean("connection_present"))
        assertEquals(info.inputType, event.getInt("input_type"))
        assertEquals(info.imeOptions, event.getInt("ime_options"))
    }

    @Test fun diagnosticsDoNotContainEditorContent() {
        val host = activity()
        host.editor.setText("R2_SYNTHETIC_PRIVACY_CANARY")
        host.requestIme()
        shadowOf(Looper.getMainLooper()).idle()
        val rows = events()
        assertTrue("diagnostics should actually execute", rows.isNotEmpty())
        assertFalse(rows.joinToString().contains("R2_SYNTHETIC_PRIVACY_CANARY"))
        assertFalse(rows.any { it.has("text") || it.has("hint") })
    }

    @Test fun ordinaryDebugHostDoesNotEmitOptInDiagnostics() {
        val host = activity(enabled = false)
        host.requestIme()
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue(events().isEmpty())
    }

    @Test fun showWaitsForRealWindowFocusThenDispatchesOriginalSequenceOnce() {
        val intent = Intent(RuntimeEnvironment.getApplication(), ImeHostActivity::class.java)
            .putExtra("KUYIN_R2_DIAGNOSTICS", true)
        Robolectric.buildActivity(ImeHostActivity::class.java, intent).setup().windowFocusChanged(false).use { controller ->
            val host = controller.get()
            assertFalse(host.editor.hasWindowFocus())
            host.requestIme()
            shadowOf(Looper.getMainLooper()).idle()
            assertFalse("an unconnected window must not issue an ineffective show", events().any {
                it.getString("event") in setOf("INSETS_SHOW_REQUEST", "IMM_SHOW_RESULT")
            })
            controller.windowFocusChanged(true)
            shadowOf(Looper.getMainLooper()).idle()
            val shows = events().filter { it.getString("event") in setOf("INSETS_SHOW_REQUEST", "IMM_SHOW_RESULT") }
            assertEquals(listOf("INSETS_SHOW_REQUEST", "IMM_SHOW_RESULT"), shows.map { it.getString("event") })
            assertTrue(shows.all { it.getBoolean("window_focused") && it.getBoolean("attached") })
        }
    }

    @Test fun laterWindowFocusChangesDoNotRepeatAnAlreadyDispatchedRequest() {
        val intent = Intent(RuntimeEnvironment.getApplication(), ImeHostActivity::class.java)
            .putExtra("KUYIN_R2_DIAGNOSTICS", true)
        Robolectric.buildActivity(ImeHostActivity::class.java, intent).setup().windowFocusChanged(false).use { controller ->
            val host = controller.get()
            host.requestIme()
            controller.windowFocusChanged(true)
            shadowOf(Looper.getMainLooper()).idle()
            controller.windowFocusChanged(false).windowFocusChanged(true)
            shadowOf(Looper.getMainLooper()).idle()
            val shows = events().filter { it.getString("event") in setOf("INSETS_SHOW_REQUEST", "IMM_SHOW_RESULT") }
            assertEquals(2, shows.size)
            assertTrue("show calls must actually occur after focus", shows.all { it.getBoolean("window_focused") })
        }
    }

    @Test fun losingFocusBeforePostedDispatchKeepsRequestPending() {
        val intent = Intent(RuntimeEnvironment.getApplication(), ImeHostActivity::class.java)
            .putExtra("KUYIN_R2_DIAGNOSTICS", true)
        Robolectric.buildActivity(ImeHostActivity::class.java, intent).setup().windowFocusChanged(true).use { controller ->
            val host = controller.get()
            val root = ReflectionHelpers.callInstanceMethod<Any>(host.editor, "getViewRootImpl")
            Shadow.extract<ShadowViewRootImpl>(root).callWindowFocusChanged(false)
            assertTrue("focus-loss message must still be queued", host.editor.hasWindowFocus())
            host.requestIme()
            shadowOf(Looper.getMainLooper()).idle()
            assertFalse("focus-loss message must run before the show dispatch", host.editor.hasWindowFocus())
            assertFalse(events().any { it.getString("event") in setOf("INSETS_SHOW_REQUEST", "IMM_SHOW_RESULT") })
            controller.windowFocusChanged(true)
            shadowOf(Looper.getMainLooper()).idle()
            assertEquals(1, events().count { it.getString("event") == "INSETS_SHOW_REQUEST" })
            assertEquals(1, events().count { it.getString("event") == "IMM_SHOW_RESULT" })
        }
    }

    @Test fun destroyedActivityCancelsQueuedShow() {
        val intent = Intent(RuntimeEnvironment.getApplication(), ImeHostActivity::class.java)
            .putExtra("KUYIN_R2_DIAGNOSTICS", true)
        val controller = Robolectric.buildActivity(ImeHostActivity::class.java, intent).setup().windowFocusChanged(true)
        val host = controller.get()
        host.requestIme()
        controller.pause().stop().destroy()
        ShadowLog.clear()
        shadowOf(Looper.getMainLooper()).idle()
        assertFalse("a destroyed fixture must not issue its queued show", events().any {
            it.getString("event") in setOf("INSETS_SHOW_REQUEST", "IMM_SHOW_RESULT")
        })
    }
}
