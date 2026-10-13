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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ImeHostDiagnosticsTest {
    @Before fun clearLogs() = ShadowLog.clear()

    private fun activity(enabled: Boolean = true): ImeHostActivity {
        val intent = Intent(RuntimeEnvironment.getApplication(), ImeHostActivity::class.java)
            .putExtra("KUYIN_R2_DIAGNOSTICS", enabled)
        return Robolectric.buildActivity(ImeHostActivity::class.java, intent).setup().get()
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
}
