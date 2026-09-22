package com.example.androidkeyboard.input

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ime.engine.KeyDef
import com.example.ime.ui.ZhuyinKeyView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class KeyboardHitboxContractTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun `dachen rows keep proportional weights`() {
        val sums = KeyboardLayout.Dachen.rows.map { row -> row.keys.sumOf { it.widthPct.toDouble() } }
        assertEquals(listOf(110.0, 100.0, 100.0, 70.0, 96.0), sums)
    }

    @Test
    fun `space dominates bottom row`() {
        val bottom = KeyboardLayout.Dachen.rows.last()
        val space = bottom.keys.single { it.action == KeyAction.SPACE }
        val total = bottom.keys.sumOf { it.widthPct.toDouble() }
        assertTrue(space.widthPct / total > 0.25)
    }

    @Test
    fun `backspace and enter are flagged special`() {
        val bottom = KeyboardLayout.Dachen.rows.last()
        val specials = bottom.keys.filter { it.isSpecial }.map { it.action }
        assertTrue(specials.contains(KeyAction.BACKSPACE))
        assertTrue(specials.contains(KeyAction.ENTER))
    }

    @Test
    fun `row labels are unique for stable hit identity`() {
        for (row in KeyboardLayout.Dachen.rows) {
            val labels = row.keys.map { it.label }
            assertEquals(labels.toSet().size, labels.size)
        }
    }

    @Test
    fun `single tap fires exactly one callback`() {
        var calls = 0
        composeRule.setContent {
            ZhuyinKeyView(
                keyDef = KeyDef("ㄅ", "1"),
                bg = Color.DarkGray,
                primaryColor = Color.White,
                subColor = Color.Gray,
                onClick = { calls++ }
            )
        }
        composeRule.onNodeWithText("ㄅ").performClick()
        composeRule.waitForIdle()
        assertEquals(1, calls)
    }
}
