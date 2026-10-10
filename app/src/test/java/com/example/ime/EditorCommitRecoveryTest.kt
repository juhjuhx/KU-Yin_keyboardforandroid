package com.example.ime

import android.app.Activity
import android.text.InputType
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ime.session.ComposeDecoderSession
import com.example.ime.session.ComposeImeState
import com.example.ime.session.DispatchResult
import com.example.ime.session.ImeCommand
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

/** JVM editor-boundary tests: real EditText/InputConnection, decoder contract double; no device/JNI. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class EditorCommitRecoveryTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var harness: EnterServiceHarness
    private lateinit var editor: EditText
    private lateinit var decoder: EditorSession
    private lateinit var activity: Activity

    @After fun close() {
        if (::harness.isInitialized) harness.close()
        if (::activity.isInitialized) activity.finish()
    }

    private fun setup(connection: (InputConnection) -> InputConnection = { it }) {
        harness = EnterServiceHarness()
        decoder = EditorSession()
        harness.setField("session", decoder)
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        editor = EditText(activity).apply { inputType = InputType.TYPE_CLASS_TEXT }
        activity.setContentView(editor)
        editor.requestFocus()
        val actual = editor.onCreateInputConnection(EditorInfo())!!
        ReflectionHelpers.setField(harness.service, "mStartedInputConnection", connection(actual))
        harness.start(EditorInfo.IME_ACTION_DONE)
        rule.setContent { AndroidView(factory = { harness.service.onCreateInputView() }) }
    }

    @Test fun `phonetic preedit is an actual composing span in EditText`() {
        setup()
        rule.onNodeWithText("ㄨ").performClick()
        rule.runOnIdle {
            assertEquals("ㄨ", editor.text.toString())
            assertEquals(0, BaseInputConnection.getComposingSpanStart(editor.text))
            assertEquals(1, BaseInputConnection.getComposingSpanEnd(editor.text))
        }
    }

    @Test fun `own composing selection notification preserves decoder candidates`() {
        setup()
        rule.onNodeWithText("ㄨ").performClick()
        rule.runOnIdle { harness.service.onUpdateSelection(0, 0, 1, 1, 0, 1) }
        rule.runOnIdle {
            assertEquals("ㄨ", decoder.state.value.preedit)
            assertEquals(listOf("我"), decoder.state.value.candidates)
        }
        rule.onNodeWithText("我").assertExists()
    }

    @Test fun `external cursor move resets decoder without moving chosen cursor`() {
        setup()
        rule.onNodeWithText("ㄨ").performClick()
        rule.runOnIdle {
            editor.setSelection(0)
            harness.service.onUpdateSelection(1, 1, 0, 0, 0, 1)
            assertEquals("", decoder.state.value.preedit)
            assertEquals(0, editor.selectionStart)
            assertEquals(-1, BaseInputConnection.getComposingSpanStart(editor.text))
        }
    }

    @Test fun `external composing span removal at same cursor cannot duplicate old phonetics`() {
        setup()
        rule.onNodeWithText("ㄨ").performClick()
        rule.runOnIdle {
            BaseInputConnection.removeComposingSpans(editor.text)
            harness.service.onUpdateSelection(1, 1, 1, 1, -1, -1)
            assertEquals("", decoder.state.value.preedit)
        }
        rule.onNodeWithText("ㄛ").performClick()
        rule.runOnIdle { assertEquals("ㄨㄛ", editor.text.toString()) }
    }

    @Test fun `unsupported extracted text uses declared initial selection for own composing update`() {
        setup { actual -> object : InputConnectionWrapper(actual, false) {
            override fun getExtractedText(request: android.view.inputmethod.ExtractedTextRequest?, flags: Int): android.view.inputmethod.ExtractedText? = null
        } }
        rule.runOnIdle {
            val info = EditorInfo().apply {
                inputType = InputType.TYPE_CLASS_TEXT
                initialSelStart = 0; initialSelEnd = 0
            }
            harness.service.onStartInput(info, false)
        }
        rule.onNodeWithText("ㄨ").performClick()
        rule.runOnIdle {
            harness.service.onUpdateSelection(0, 0, 1, 1, 0, 1)
            assertEquals("ㄨ", decoder.state.value.preedit)
        }
    }

    @Test fun `false ACK retains ordered unsent tail and blocks another key without replay`() {
        val attempted = mutableListOf<String>()
        setup { actual ->
            object : InputConnectionWrapper(actual, false) {
                override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                    attempted.add(text.toString())
                    // A false reply is ambiguous: the editor may already have changed its text.
                    super.commitText(text, newCursorPosition)
                    return false
                }
            }
        }
        rule.onNodeWithText("ㄨ").performClick()
        rule.onNodeWithText("空白 (一聲)").performClick()
        rule.runOnIdle {
            assertEquals(listOf("我"), attempted)
            assertEquals("我", editor.text.toString())
        }
        rule.onNodeWithText("編輯器未確認送出").assertExists()
        rule.onNodeWithText("我 ").assertExists()
        rule.onNodeWithText("ㄨ").performClick()
        rule.runOnIdle {
            assertEquals(listOf("我"), attempted)
            assertEquals("", decoder.state.value.preedit)
            assertEquals("我", editor.text.toString())
        }
    }

    @Test fun `unconfirmed commit is not sent into a newly focused editor`() {
        val attempted = mutableListOf<String>()
        setup { actual ->
            object : InputConnectionWrapper(actual, false) {
                override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                    attempted.add(text.toString())
                    return false
                }
            }
        }
        rule.onNodeWithText("ㄨ").performClick()
        rule.onNodeWithText("我").performClick()
        val next = EditText(activity).apply { inputType = InputType.TYPE_CLASS_TEXT }
        rule.runOnIdle {
            ReflectionHelpers.setField(harness.service, "mStartedInputConnection",
                next.onCreateInputConnection(EditorInfo())!!)
            harness.start(EditorInfo.IME_ACTION_DONE)
        }
        rule.onNodeWithText("編輯器未確認送出").assertExists()
        rule.onNodeWithText("ㄨ").performClick()
        rule.runOnIdle {
            assertEquals(listOf("我"), attempted)
            assertEquals("", next.text.toString())
            assertEquals("", decoder.state.value.preedit)
        }
    }

    @Test fun `starting another editor never deletes its existing selection`() {
        setup()
        rule.runOnIdle {
            editor.setText("既有文字")
            editor.setSelection(0, 2)
            harness.start(EditorInfo.IME_ACTION_DONE)
            assertEquals("既有文字", editor.text.toString())
            assertTrue(editor.selectionEnd >= editor.selectionStart)
        }
    }

    private class EditorSession : ComposeDecoderSession {
        override val state = MutableStateFlow(ComposeImeState())
        override fun dispatch(command: ImeCommand): DispatchResult = when (command) {
            is ImeCommand.TapZhuyinKey -> {
                state.value = ComposeImeState(state.value.preedit + command.char, listOf("我"))
                DispatchResult(null, true)
            }
            is ImeCommand.SelectCandidate, ImeCommand.Enter, ImeCommand.Complete -> {
                val composed = state.value.preedit.isNotEmpty()
                state.value = ComposeImeState()
                DispatchResult("我".takeIf { composed }, composed)
            }
            ImeCommand.Space -> {
                state.value = ComposeImeState()
                DispatchResult("我", true, listOf(" "))
            }
            ImeCommand.Backspace, ImeCommand.ClearComposing, ImeCommand.Reset -> {
                state.value = ComposeImeState()
                DispatchResult(null, true)
            }
            else -> DispatchResult(null, false)
        }
    }
}
