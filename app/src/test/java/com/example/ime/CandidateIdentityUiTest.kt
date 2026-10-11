package com.example.ime

import android.view.inputmethod.EditorInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ime.session.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class CandidateIdentityUiTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var harness: EnterServiceHarness
    @After fun close() { harness.close() }
    @Test fun `equal candidate labels retain their selection identity`() {
        harness = EnterServiceHarness()
        harness.start(EditorInfo.IME_ACTION_DONE)
        var selected = -1
        val session = object : ComposeDecoderSession {
            override val state = MutableStateFlow(ComposeImeState("組字", listOf("我", "我")))
            override fun dispatch(command: ImeCommand): DispatchResult {
                selected = (command as ImeCommand.SelectCandidate).index
                return DispatchResult(null, true)
            }
        }
        harness.setField("session", session)
        rule.setContent { AndroidView(factory = { harness.service.onCreateInputView() }) }
        rule.onAllNodesWithText("我")[1].performClick()
        rule.runOnIdle { assertEquals(1, selected) }
    }
}
