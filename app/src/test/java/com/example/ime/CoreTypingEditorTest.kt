package com.example.ime

import android.app.Activity
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ime.session.ChewingEngineSession
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class CoreTypingEditorTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var harness: EnterServiceHarness
    @After fun close() { harness.close() }

    @Test fun `visible candidate tap inserts Chinese into real EditText and can continue`() {
        harness = EnterServiceHarness()
        harness.setField("session", ChewingEngineSession(TypingContractEngine()))
        harness.start(EditorInfo.IME_ACTION_DONE)
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val editor = EditText(activity).apply { inputType = InputType.TYPE_CLASS_TEXT }
        activity.setContentView(editor)
        editor.requestFocus()
        val connection = editor.onCreateInputConnection(EditorInfo())!!
        ReflectionHelpers.setField(harness.service, "mStartedInputConnection", connection)
        rule.setContent { AndroidView(factory = { harness.service.onCreateInputView() }) }
        rule.onNodeWithText("ㄨ").performClick()
        rule.onNodeWithText("我").performClick()
        rule.runOnIdle { assertEquals("我", editor.text.toString()) }
        rule.onNodeWithText("ㄨ").performClick()
        rule.onNodeWithText("我").performClick()
        rule.runOnIdle { assertEquals("我我", editor.text.toString()) }
        activity.finish()
    }
}
