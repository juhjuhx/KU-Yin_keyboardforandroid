package com.example.ime

import android.view.inputmethod.EditorInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ime.engine.KeyboardMode
import com.example.ime.session.ChewingEngineSession
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class IncompleteModeSwitchUiTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var harness: EnterServiceHarness
    @After fun close() { harness.close() }
    @Test fun `mode change cannot replace unresolved Chinese phonetics with English composing`() {
        harness = EnterServiceHarness()
        harness.setField("session", ChewingEngineSession(TypingContractEngine().apply {
            commitUnavailable = "這組注音尚無可用候選"
        }))
        harness.start(EditorInfo.IME_ACTION_DONE)
        rule.setContent { AndroidView(factory = { harness.service.onCreateInputView() }) }
        rule.onNodeWithText("ㄨ").performClick()
        rule.onNodeWithText("EN").performClick()
        rule.runOnIdle {
            assertEquals(KeyboardMode.ZHUYIN, harness.engine.mode.value)
        }
    }
}
