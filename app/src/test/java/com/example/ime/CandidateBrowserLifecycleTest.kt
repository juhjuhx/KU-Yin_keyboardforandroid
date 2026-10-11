package com.example.ime

import android.view.inputmethod.EditorInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ime.session.*
import com.example.ime.engine.KeyboardMode
import org.junit.Assert.*
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class CandidateBrowserLifecycleTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var harness: EnterServiceHarness
    private lateinit var session: ChewingEngineSession
    @After fun close() { if (::harness.isInitialized) harness.close() }
    private fun show() {
        harness = EnterServiceHarness()
        session = ChewingEngineSession(BrowserTestEngine())
        harness.setField("session", session)
        harness.start(EditorInfo.IME_ACTION_DONE)
        session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        rule.setContent { AndroidView(factory = { harness.service.onCreateInputView() }) }
    }
    @Test fun `hide request blocks captured candidate before finish callback`() {
        show()
        val tap = rule.onNode(hasText("我") and hasClickAction()).fetchSemanticsNode()
            .config[SemanticsActions.OnClick].action!!
        rule.onNodeWithContentDescription("收起鍵盤").performClick()
        rule.runOnIdle { tap(); assertTrue(harness.connection.effects.isEmpty()) }
    }
    @Test fun `editor switch rejects prior candidate and closes browser`() {
        show()
        rule.onNodeWithContentDescription("展開候選").performClick()
        val tap = rule.onNode(hasText("我") and hasClickAction()).fetchSemanticsNode()
            .config[SemanticsActions.OnClick].action!!
        rule.runOnIdle { harness.start(EditorInfo.IME_ACTION_SEND); tap(); assertTrue(harness.connection.effects.isEmpty()) }
        rule.onNodeWithTag("candidate_grid").assertDoesNotExist()
        rule.onNodeWithText("ㄅ").assertIsDisplayed()
    }
    @Test fun `hidden symbol action cannot dispatch editor effect before finish callback`() {
        show()
        rule.runOnIdle { harness.engine.setMode(KeyboardMode.SYMBOLS) }
        val enter = rule.onNodeWithText("完成").fetchSemanticsNode().config[SemanticsActions.OnClick].action!!
        rule.onNodeWithContentDescription("收起鍵盤").performClick()
        rule.runOnIdle { enter(); assertTrue(harness.connection.effects.isEmpty()) }
    }
    @Test fun `editor start blocks old key until the input view starts`() {
        show()
        val key = rule.onNodeWithText("ㄨ").fetchSemanticsNode().config[SemanticsActions.OnClick].action!!
        val info = EditorInfo().apply { inputType = android.text.InputType.TYPE_CLASS_TEXT }
        rule.runOnIdle {
            harness.service.onStartInput(info, false)
            key()
            assertEquals("", session.state.value.preedit)
            assertTrue(harness.connection.effects.isEmpty())
            harness.service.onStartInputView(info, false)
        }
        rule.onNodeWithText("ㄨ").performClick()
        rule.runOnIdle { assertEquals("ㄨㄛ", session.state.value.preedit) }
    }
}
