package com.example.ime.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.EngineUpdate
import com.example.ime.session.ChewingEngineSession
import com.example.ime.session.ImeCommand
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class DachenKeyboardLayoutTest {
    @get:Rule val composeRule = createComposeRule()

    // 獨立預期值：libchewing Standard 配列；不由產品 KeyboardLayout 衍生。
    // https://github.com/chewing/libchewing/blob/3c4a93aa03d574c7f011ff84e8a2437c2f79b2cf/src/editor/zhuyin_layout/standard.rs
    private val expectedRows = listOf(
        "ㄅㄉˇˋㄓˊ˙ㄚㄞㄢㄦ" to "1234567890-",
        "ㄆㄊㄍㄐㄔㄗㄧㄛㄟㄣ" to "qwertyuiop",
        "ㄇㄋㄎㄑㄕㄘㄨㄜㄠㄤ" to "asdfghjkl;",
        "ㄈㄌㄏㄒㄖㄙㄩㄝㄡㄥ" to "zxcvbnm,./",
    )

    private fun showKeyboard(
        onZhuyin: (String) -> Unit = {},
        onAction: (String) -> Unit = {},
    ) {
        composeRule.setContent {
            ZhuyinKeyboardLayout(
                keyBgNormal = Color.DarkGray,
                keyBgAction = Color.Gray,
                keyBgAccent = Color.Blue,
                keyTextPrimary = Color.White,
                keyTextSub = Color.LightGray,
                actionLabel = "送出",
                onZhuyinPress = onZhuyin,
                onSpacePress = { onAction("space") },
                onBackspacePress = { onAction("backspace") },
                onEnterPress = { onAction("enter") },
                onSwitchToEnglish = { onAction("english") },
                onSwitchToSymbols = { onAction("symbols") },
                onSwitchToEmoji = { onAction("emoji") },
                onPunctuation = onAction,
            )
        }
    }

    @Test
    fun `all 37 phonetic symbols and four tones are visible exactly once`() {
        showKeyboard()
        expectedRows.forEach { (labels, _) ->
            labels.forEach { label ->
                composeRule.onAllNodesWithText(label.toString()).assertCountEquals(1)
                composeRule.onNodeWithText(label.toString()).assertIsDisplayed()
            }
        }
    }

    @Test
    fun `lower row hints match standard physical codes`() {
        showKeyboard()
        val (labels, codes) = expectedRows.last()
        labels.zip(codes).forEach { (label, code) ->
            // clickable 的合併語義節點同時包含主符號與鍵碼提示。
            composeRule.onNodeWithText(label.toString()).assert(hasText(code.toString()))
        }
    }

    @Test
    @Config(sdk = [36], qualifiers = "w320dp-h640dp")
    fun `narrow phone keeps every phonetic key and backspace visible`() {
        showKeyboard()
        expectedRows.forEach { (labels, _) ->
            labels.forEach { composeRule.onNodeWithText(it.toString()).assertIsDisplayed() }
        }
        composeRule.onNodeWithContentDescription("操作").assertIsDisplayed()
    }

    @Test
    fun `each visible phonetic tap dispatches its standard code to native session once`() {
        val engine = RecordingEngine()
        val session = ChewingEngineSession(engine)
        showKeyboard(onZhuyin = { session.dispatch(ImeCommand.TapZhuyinKey(it)) })
        expectedRows.forEach { (labels, _) ->
            labels.forEach { label ->
                composeRule.onNodeWithText(label.toString()).performClick()
            }
        }
        composeRule.runOnIdle {
            assertEquals(expectedRows.flatMap { it.second.map(Char::code) }, engine.codes)
        }
    }

    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `four phonetic rows keep key order with backspace at fourth row end`() {
        showKeyboard()
        var previousBottom = Float.NEGATIVE_INFINITY
        expectedRows.forEach { (labels, _) ->
            val bounds = labels.map {
                composeRule.onNodeWithText(it.toString()).fetchSemanticsNode().boundsInRoot
            }
            assertTrue(bounds.first().top >= previousBottom)
            bounds.forEach { assertEquals(bounds.first().top, it.top, 0.5f) }
            bounds.zipWithNext().forEach { (left, right) ->
                assertTrue(right.left >= left.right)
            }
            previousBottom = bounds.first().bottom
        }
        val lastKey = composeRule.onNodeWithText("ㄥ").fetchSemanticsNode().boundsInRoot
        val backspace = composeRule.onNodeWithContentDescription("操作")
            .fetchSemanticsNode().boundsInRoot
        assertEquals(lastKey.top, backspace.top, 0.5f)
        assertTrue(backspace.left >= lastKey.right)
        composeRule.onRoot().captureRoboImage("build/outputs/roborazzi/dachen-360.png")
    }

    @Test
    fun `function keys use dedicated callbacks without phonetic dispatch`() {
        val actions = mutableListOf<String>()
        val phonetics = mutableListOf<String>()
        showKeyboard(onZhuyin = { phonetics.add(it) }, onAction = { actions.add(it) })
        composeRule.onNodeWithContentDescription("操作").performClick()
        listOf("EN", "?123", "😊", "，", "空白 (一聲)", "。", "送出").forEach {
            composeRule.onNodeWithText(it).performClick()
        }
        composeRule.runOnIdle {
            assertEquals(
                listOf("backspace", "english", "symbols", "emoji", "，", "space", "。", "enter"),
                actions,
            )
            assertTrue(phonetics.isEmpty())
        }
    }

    private class RecordingEngine : ChewingEngine {
        val codes = mutableListOf<Int>()
        private val idle = EngineUpdate(true, "", emptyList(), "")
        override val isReady = true
        override val personalizedLearningEnabled = false
        override fun init(layout: ChewingEngine.Layout) {}
        override fun setPersonalizedLearningEnabled(enabled: Boolean) {}
        override fun reset() {}
        override fun handleKeyUpdate(keyCode: Int): EngineUpdate {
            codes.add(keyCode)
            return idle
        }
        override fun backspaceUpdate() = idle
        override fun selectCandidateUpdate(index: Int) = idle
        override fun commitUpdate() = idle
        override fun nextPageUpdate(): EngineUpdate? = null
        override fun prevPageUpdate(): EngineUpdate? = null
        override fun canPageCandidatesBackward() = false
        override fun canPageCandidatesForward() = false
        override fun close() {}
    }
}
