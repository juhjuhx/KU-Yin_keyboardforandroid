package com.example.ime

import android.app.Activity
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.viewinterop.AndroidView
import com.example.androidkeyboard.engines.android.AndroidChewingEngine
import com.example.ime.session.ChewingEngineSession
import com.example.ime.session.ComposeDecoderSession
import com.example.androidkeyboard.input.KeyboardLayout
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

/** Real pinned JNI + real service UI + real EditText, still not physical-device evidence. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class NativeTypingEditorIntegrationTest {
    @get:Rule val rule = createComposeRule()
    private var harness: EnterServiceHarness? = null
    private var native: AndroidChewingEngine? = null
    private lateinit var editor: EditText
    private var activity: Activity? = null
    private var eventId = 0
    private var commitCalls = 0

    @Before fun start() {
        val data = System.getProperty("kuyin.native.host.data").orEmpty()
        Assume.assumeTrue("Explicit host-native library/data gate is required", data.isNotEmpty())
        val h = EnterServiceHarness().also { harness = it }
        val engine = AndroidChewingEngine(data, ":memory:").also { native = it; it.init() }
        assertTrue("Real pinned JNI must initialize", engine.isReady)
        h.setField("nativeEngine", engine)
        h.setField("session", ChewingEngineSession(engine))
        h.start(EditorInfo.IME_ACTION_DONE)
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        editor = EditText(activity!!).apply { inputType = InputType.TYPE_CLASS_TEXT }
        activity!!.setContentView(editor)
        editor.requestFocus()
        val recordingConnection = object : InputConnectionWrapper(editor.onCreateInputConnection(EditorInfo())!!, false) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                commitCalls++
                val before = editor.text.toString()
                val result = super.commitText(text, newCursorPosition)
                println("P0R_SYNTHETIC_TRACE=" + JSONObject().put("event_id", ++eventId)
                    .put("provider", "AndroidChewingEngine").put("commit_requested", text)
                    .put("commit_result", result).put("editor_before", before)
                    .put("editor_after", editor.text.toString()))
                return result
            }
        }
        ReflectionHelpers.setField(h.service, "mStartedInputConnection", recordingConnection)
        rule.setContent { AndroidView(factory = { h.service.onCreateInputView() }) }
    }
    @After fun close() { harness?.close(); native?.close(); activity?.finish() }
    private fun type(vararg glyphs: String) { glyphs.forEach { glyph ->
        rule.onNodeWithText(glyph).performClick()
        rule.runOnIdle {
            val state = ReflectionHelpers.getField<ComposeDecoderSession>(harness!!.service, "session").state.value
            println("P0R_SYNTHETIC_TRACE=" + JSONObject().put("event_id", ++eventId)
                .put("provider", "AndroidChewingEngine").put("mode", "ZHUYIN")
                .put("visible_key", glyph).put("dachen_ascii", KeyboardLayout.Dachen.allKeys.single { it.label == glyph }.code)
                .put("preedit", state.preedit).put("candidate_count", state.candidates.size)
                .put("candidate_generation", state.generation).put("unavailable_reason", state.unavailableReason))
        }
    } }
    private fun choose(text: String) {
        rule.onAllNodes(hasText(text) and hasClickAction())[0].performClick()
    }
    @Test fun `no tone candidate tap writes actual Chinese text`() {
        type("ㄨ", "ㄛ")
        choose("我")
        rule.runOnIdle { assertEquals("我", editor.text.toString()) }
    }
    @Test fun `tone three candidate remains selectable`() {
        type("ㄨ", "ㄛ", "ˇ")
        choose("我")
        rule.runOnIdle { assertEquals("我", editor.text.toString()) }
    }
    @Test fun `continuous no tone phrase commits whole decoder phrase`() {
        type("ㄨ", "ㄛ", "ㄒ", "ㄧ", "ㄢ", "ㄗ", "ㄞ")
        choose("我現在")
        rule.runOnIdle { assertEquals("我現在", editor.text.toString()) }
    }
    @Test fun `continuous toned phrase commits whole decoder phrase`() {
        type("ㄨ", "ㄛ", "ˇ", "ㄒ", "ㄧ", "ㄢ", "ˋ", "ㄗ", "ㄞ", "ˋ")
        choose("我現在")
        rule.runOnIdle { assertEquals("我現在", editor.text.toString()) }
    }
    @Test fun `choose first word and continue composing commits remaining phrase once`() {
        type("ㄨ", "ㄛ")
        choose("我")
        type("ㄒ", "ㄧ", "ㄢ", "ˋ", "ㄗ", "ㄞ", "ˋ")
        choose("現在")
        rule.runOnIdle { assertEquals("我現在", editor.text.toString()) }
    }
    @Test fun `expanded second native page selection writes exact displayed text once`() {
        type("ㄨ", "ㄛ")
        rule.onNodeWithContentDescription("展開候選").performClick()
        rule.onNodeWithContentDescription("下一頁候選").performClick()
        var label = ""
        rule.runOnIdle {
            label = ReflectionHelpers.getField<ComposeDecoderSession>(harness!!.service, "session")
                .state.value.candidates.first()
        }
        choose(label)
        rule.runOnIdle { assertEquals(label, editor.text.toString()); assertEquals(1, commitCalls) }
        rule.onNodeWithTag("candidate_grid").assertDoesNotExist()
        // Continue with real soft-key callbacks after the grid disappears.
        type("ㄨ", "ㄛ")
        choose("我")
        rule.runOnIdle { assertEquals(label + "我", editor.text.toString()) }
    }
}
