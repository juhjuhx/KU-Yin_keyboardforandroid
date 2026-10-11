package com.example.ime

import android.view.inputmethod.EditorInfo
import android.text.InputType
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ime.engine.KeyboardMode
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class EnglishTypingUiTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var harness: EnterServiceHarness
    @After fun close() { harness.close() }
    @Test fun `NO_SUGGESTIONS English editor has literal input and no dictionary completions`() {
        restrictedEnglish(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
    }
    @Test fun `AUTO_COMPLETE English editor does not override editor owned completions`() {
        restrictedEnglish(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
    }
    private fun restrictedEnglish(flag: Int) {
        harness = EnterServiceHarness()
        harness.start(EditorInfo.IME_ACTION_DONE, type = InputType.TYPE_CLASS_TEXT or flag)
        harness.engine.setMode(KeyboardMode.ENGLISH)
        rule.setContent { AndroidView(factory = { harness.service.onCreateInputView() }) }
        listOf("h", "e", "l", "l").forEach { rule.onNodeWithText(it).performClick() }
        rule.onNodeWithText("hello").assertDoesNotExist()
        rule.runOnIdle {
            assertEquals(listOf("h", "e", "l", "l").map { EnterServiceHarness.Effect.Commit(it) },
                harness.connection.effects)
        }
    }
    @Test fun `English visible keys offer dictionary completion and send one completed word`() {
        harness = EnterServiceHarness()
        harness.start(EditorInfo.IME_ACTION_DONE)
        harness.engine.setMode(KeyboardMode.ENGLISH)
        rule.setContent { AndroidView(factory = { harness.service.onCreateInputView() }) }
        listOf("h", "e", "l", "l").forEach { rule.onNodeWithText(it).performClick() }
        rule.onNodeWithText("hello").assertExists().performClick()
        rule.runOnIdle {
            assertEquals(listOf(EnterServiceHarness.Effect.Commit("hello")), harness.connection.effects)
        }
    }
}
