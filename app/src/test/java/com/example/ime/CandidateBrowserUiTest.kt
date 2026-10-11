package com.example.ime

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.ime.engine.KuYinEngine
import com.example.ime.session.*
import com.example.ime.settings.KeyboardSettings
import com.example.ime.ui.KuYinKeyboardUi
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h800dp")
class CandidateBrowserUiTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var session: ChewingEngineSession
    private lateinit var decoder: BrowserTestEngine
    private val commits = mutableListOf<String>()
    private fun show(labels: List<String>? = null, fontScale: Float = 1f, fontSize: Int = 18,
        preedit: String = "ㄨㄛ", compose: Boolean = true) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        decoder = BrowserTestEngine(labels = labels, preedit = preedit)
        session = ChewingEngineSession(decoder)
        if (compose) session.dispatch(ImeCommand.TapZhuyinKey("ㄨ"))
        val presentation = KuYinEngine(context)
        val settings = KeyboardSettings(context).apply { candidateFontSizeSp = fontSize }
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                KuYinKeyboardUi(presentation, session, settings, "換行",
                    { commits += it }, {}, {}, {}, {})
            }
        }
    }
    @Test fun `horizontal drag does not select or commit candidate`() {
        val labels = (0..15).map { "候選字詞$it" }
        show(labels)
        val strip = rule.onNodeWithTag("candidate_strip")
        val before = strip.fetchSemanticsNode().config[SemanticsProperties.HorizontalScrollAxisRange].value()
        rule.onNodeWithText(labels.first()).performTouchInput {
            swipe(center, center.copy(x = -180f), durationMillis = 500)
        }
        val after = strip.fetchSemanticsNode().config[SemanticsProperties.HorizontalScrollAxisRange].value()
        assertTrue("A real overflow drag must scroll", after > before)
        rule.runOnIdle { assertTrue(commits.isEmpty()); assertEquals(0, decoder.selectionCalls) }
    }
    @Test fun `touch down from old composition cannot click reused current candidate`() {
        show()
        rule.onNodeWithText("我").performTouchInput { down(center) }
        rule.runOnIdle { session.dispatch(ImeCommand.TapZhuyinKey("ㄛ")) }
        rule.onRoot().performTouchInput { up() }
        rule.runOnIdle { assertTrue(commits.isEmpty()); assertEquals(0, decoder.selectionCalls) }
    }
    @Test
    @Config(qualifiers = "w640dp-h360dp-land")
    fun `expanded browser leaves landscape editor space`() {
        show()
        rule.onNodeWithContentDescription("展開候選").performClick()
        val density = ApplicationProvider.getApplicationContext<Context>().resources.displayMetrics.density
        assertTrue(rule.onNodeWithTag("candidate_browser").fetchSemanticsNode().boundsInRoot.height <= 162 * density + 1)
        rule.onNodeWithContentDescription("下一頁候選").assertIsDisplayed()
    }
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `large font single character remains visually readable`() {
        show(listOf("我"), fontScale = 2f, fontSize = 20)
        val layouts = mutableListOf<TextLayoutResult>()
        rule.onNodeWithText("我").performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
        val layout = layouts.single()
        // LazyRow's unbounded width is reused by semantics to reconstruct its paragraph.
        // didOverflowWidth compares that paragraph width, rather than glyph bounds.
        assertFalse("Single candidate must not be truncated by strip height", layout.didOverflowHeight)
        assertFalse("Single candidate must not be ellipsized", layout.isLineEllipsized(0))
        assertEquals("Complete candidate must remain visible", "我".length, layout.getLineEnd(0, visibleEnd = true))
        assertTrue("Glyph start must fit measured text", layout.getLineLeft(0) >= -1f)
        assertTrue("Glyph end must fit measured text", layout.getLineRight(0) <= layout.size.width + 1f)
    }
    @Test fun `expand replaces keyboard and collapse restores keys without composition loss`() {
        show()
        rule.onNodeWithContentDescription("展開候選").performClick()
        rule.onNodeWithTag("candidate_grid").assertIsDisplayed()
        rule.onNodeWithText("ㄅ").assertDoesNotExist()
        rule.onNodeWithText("ㄨㄛ").assertExists()
        rule.onNodeWithContentDescription("收合候選").performClick()
        rule.onNodeWithText("ㄅ").assertIsDisplayed()
        rule.runOnIdle { assertEquals("ㄨㄛ", session.state.value.preedit); assertTrue(commits.isEmpty()); assertEquals(1, decoder.queries) }
    }
    @Test fun `expanded page controls reach different decoder candidates and preserve preedit`() {
        show()
        rule.onNodeWithContentDescription("展開候選").performClick()
        rule.onNodeWithContentDescription("上一頁候選").assertIsNotEnabled()
        rule.onNodeWithContentDescription("下一頁候選").performClick()
        rule.onNodeWithText("握").assertIsDisplayed()
        rule.onNodeWithContentDescription("下一頁候選").assertIsNotEnabled()
        rule.onNodeWithContentDescription("上一頁候選").performClick()
        rule.onNodeWithText("我").assertIsDisplayed()
        rule.runOnIdle { assertTrue(commits.isEmpty()); assertEquals("ㄨㄛ", session.state.value.preedit) }
    }
    @Test fun `expanded candidate uses existing single commit path and closes`() {
        show()
        rule.onNodeWithContentDescription("展開候選").performClick()
        rule.onNodeWithContentDescription("下一頁候選").performClick()
        rule.onNodeWithText("握").performTouchInput { click() }
        rule.onNodeWithTag("candidate_grid").assertDoesNotExist()
        rule.onNodeWithText("ㄅ").assertIsDisplayed()
        rule.runOnIdle { assertEquals(listOf("握"), commits); assertEquals(1, decoder.selectionCalls); assertEquals(1, decoder.commits) }
    }
    @Test fun `grid overflow drag scrolls without selecting a candidate`() {
        val labels = (0..23).map { "候選$it" }
        show(labels)
        rule.onNodeWithContentDescription("展開候選").performClick()
        val grid = rule.onNodeWithTag("candidate_grid")
        val before = grid.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
        rule.onNodeWithText(labels.first()).performTouchInput {
            swipe(center, center.copy(y = -150f), durationMillis = 500)
        }
        val after = grid.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
        assertTrue(after > before)
        rule.runOnIdle { assertTrue(commits.isEmpty()); assertEquals(0, decoder.selectionCalls) }
    }
    @Test fun `grid cancels old pointer when composition changes`() {
        show()
        rule.onNodeWithContentDescription("展開候選").performClick()
        rule.onNodeWithText("我").performTouchInput { down(center) }
        rule.runOnIdle { session.dispatch(ImeCommand.TapZhuyinKey("ㄛ")) }
        rule.onRoot().performTouchInput { up() }
        rule.runOnIdle { assertTrue(commits.isEmpty()); assertEquals(0, decoder.selectionCalls) }
    }
    @Test fun `empty projected page retains navigation and empty composition hides browser`() {
        show(emptyList())
        rule.onNodeWithContentDescription("展開候選").performClick()
        rule.onNodeWithText("此頁沒有可顯示候選").assertIsDisplayed()
        rule.onNodeWithContentDescription("下一頁候選").performClick()
        rule.onNodeWithContentDescription("上一頁候選").assertIsEnabled()
        rule.runOnIdle { session.dispatch(ImeCommand.Reset) }
        rule.onNodeWithTag("candidate_grid").assertDoesNotExist()
        rule.onNodeWithContentDescription("展開候選").assertDoesNotExist()
        rule.onNodeWithText("ㄅ").assertIsDisplayed()
    }
    @Test
    @Config(qualifiers = "w600dp-h900dp")
    fun `wide editor with enlarged font can expand select and return to keyboard`() {
        show(listOf("我"), fontScale = 1.3f, fontSize = 20)
        rule.onNodeWithContentDescription("展開候選").assertWidthIsAtLeast(48.dp).performClick()
        rule.onNodeWithText("我").assertIsDisplayed().assertHasClickAction().performClick()
        rule.runOnIdle { assertEquals(listOf("我"), commits); assertEquals(1, decoder.selectionCalls) }
        rule.onNodeWithTag("candidate_grid").assertDoesNotExist()
        rule.onNodeWithText("ㄅ").assertIsDisplayed()
    }
    @Test
    @Config(qualifiers = "w320dp-h600dp")
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun `narrow large font long preedit keeps candidate width and accessible full label`() {
        val label = "這是一個很長而且完整可辨識的候選詞句"
        show(listOf(label), fontScale = 2f, fontSize = 20, preedit = "我現在正在測試很長的組字顯示".repeat(3))
        val density = ApplicationProvider.getApplicationContext<Context>().resources.displayMetrics.density
        assertTrue(rule.onNodeWithTag("candidate_strip").fetchSemanticsNode().boundsInRoot.width >= 240 * density)
        rule.onNodeWithContentDescription("展開候選").assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp).performClick()
        rule.onNodeWithText(label).assertHasClickAction().assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
        rule.onNodeWithContentDescription("收合候選").assertIsDisplayed()
    }
}
