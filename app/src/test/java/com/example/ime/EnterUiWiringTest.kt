package com.example.ime

import android.view.inputmethod.EditorInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ime.EnterServiceHarness.Effect.Action
import com.example.ime.EnterServiceHarness.Effect.Commit
import com.example.ime.engine.KeyboardMode
import com.example.ime.session.ComposeDecoderSession
import com.example.ime.session.ComposeImeState
import com.example.ime.session.DispatchResult
import com.example.ime.session.ImeCommand
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class EnterUiWiringTest {
    @get:Rule val composeRule = createComposeRule()
    private lateinit var harness: EnterServiceHarness
    @Before fun setUp() { harness = EnterServiceHarness() }
    @After fun tearDown() { harness.close() }

    private fun show() {
        // 掛載service實際的ComposeView，驗證onCreateInputView的label與callback接線。
        composeRule.setContent { AndroidView(factory = { harness.service.onCreateInputView() }) }
    }

    @Test fun `NO_ENTER_ACTION suppresses SEND presentation and dispatch`() {
        harness.start(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        show()
        composeRule.onNodeWithText("傳送").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("換行").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(listOf(Commit("\n")), harness.connection.effects) }
    }

    @Test fun `NO_ENTER_ACTION suppresses GO presentation and dispatch`() {
        harness.start(EditorInfo.IME_ACTION_GO or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        show()
        composeRule.onNodeWithText("前往").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("換行").performClick()
        composeRule.runOnIdle { assertEquals(listOf(Commit("\n")), harness.connection.effects) }
    }

    @Test fun `custom label is shown and dispatches its own id`() {
        harness.start(EditorInfo.IME_ACTION_SEND, "確認", 73)
        show()
        composeRule.onNodeWithText("確認").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(listOf(Action(73)), harness.connection.effects) }
    }

    @Test fun `custom label cannot bypass NO_ENTER_ACTION presentation`() {
        harness.start(EditorInfo.IME_ACTION_GO or EditorInfo.IME_FLAG_NO_ENTER_ACTION, "確認", 73)
        show()
        composeRule.onNodeWithText("確認").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("換行").performClick()
        composeRule.runOnIdle { assertEquals(listOf(Commit("\n")), harness.connection.effects) }
    }

    @Test fun `PREVIOUS has matching Traditional Chinese presentation`() {
        harness.start(EditorInfo.IME_ACTION_PREVIOUS)
        show()
        composeRule.onNodeWithText("上一步").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(listOf(Action(EditorInfo.IME_ACTION_PREVIOUS)), harness.connection.effects) }
    }

    @Test fun `custom newline label remains text rather than newline icon`() {
        harness.start(EditorInfo.IME_ACTION_SEND, "換行", 73)
        show()
        composeRule.onNodeWithText("換行").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(listOf(Action(73)), harness.connection.effects) }
    }

    @Test fun `active composition consumes first SEND Enter and second press sends once`() {
        harness.start(EditorInfo.IME_ACTION_SEND)
        show()
        composeRule.onNodeWithText("ㄋ").performClick()
        composeRule.runOnIdle { assertEquals("ㄋ", harness.session.state.value.preedit) }
        composeRule.onNodeWithText("傳送").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf(Commit("你")), harness.connection.effects)
            assertEquals(1, harness.native.commitCalls)
            assertEquals("", harness.session.state.value.preedit)
        }
        composeRule.onNodeWithText("傳送").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf(Commit("你"), Action(EditorInfo.IME_ACTION_SEND)), harness.connection.effects)
            assertEquals(1, harness.native.commitCalls)
        }
    }

    @Test fun `active composition consumes Enter without also inserting newline`() {
        harness.start(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        show()
        composeRule.onNodeWithText("ㄋ").performClick()
        composeRule.runOnIdle { assertTrue(harness.session.state.value.preedit.isNotEmpty()) }
        composeRule.onNodeWithContentDescription("換行").performClick()
        composeRule.runOnIdle { assertEquals(listOf(Commit("你")), harness.connection.effects) }
        composeRule.onNodeWithContentDescription("換行").performClick()
        composeRule.runOnIdle { assertEquals(listOf(Commit("你"), Commit("\n")), harness.connection.effects) }
    }

    @Test fun `empty composition delegates exactly once after session Enter`() {
        harness.start(EditorInfo.IME_ACTION_SEND)
        show()
        composeRule.onNodeWithText("傳送").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf(ImeCommand.Enter), harness.commands)
            assertEquals(listOf(Action(EditorInfo.IME_ACTION_SEND)), harness.connection.effects)
            assertEquals(0, harness.native.commitCalls)
        }
    }

    @Test fun `English and symbols receive the same normalized custom presentation`() {
        harness.start(EditorInfo.IME_ACTION_SEND, "確認", 73)
        harness.engine.setMode(KeyboardMode.ENGLISH)
        show()
        composeRule.onNodeWithText("確認").performClick()
        composeRule.onNodeWithText("?123").performClick()
        composeRule.onNodeWithText("確認").performClick()
        composeRule.runOnIdle { assertEquals(listOf(Action(73), Action(73)), harness.connection.effects) }
    }

    @Test fun `consumed Enter applies ordered session outputs without external editor action`() {
        harness.start(EditorInfo.IME_ACTION_SEND)
        val session = object : ComposeDecoderSession {
            override val state = MutableStateFlow(ComposeImeState(preedit = "組字"))
            override fun dispatch(command: ImeCommand): DispatchResult {
                assertEquals(ImeCommand.Enter, command)
                state.value = ComposeImeState()
                return DispatchResult("先", true, listOf("後"))
            }
        }
        harness.setField("session", session)
        show()
        composeRule.onNodeWithText("傳送").performClick()
        composeRule.runOnIdle { assertEquals(listOf(Commit("先"), Commit("後")), harness.connection.effects) }
    }
}
