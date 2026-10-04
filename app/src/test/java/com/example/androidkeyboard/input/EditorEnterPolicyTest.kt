package com.example.androidkeyboard.input

import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 純JVM決策表；Android常數為編譯期int，不建立framework物件。 */
class EditorEnterPolicyTest {
    private fun assertNewline(policy: EditorEnterPolicy) {
        assertEquals("換行", policy.label)
        assertFalse(policy.isActionPresentation)
        assertEquals(EditorEnterPolicy.Behavior.InsertNewline, policy.behavior)
    }

    private fun assertStandard(code: Int, label: String) {
        val policy = EditorEnterPolicy.from(code)
        assertEquals(label, policy.label)
        assertTrue(policy.isActionPresentation)
        assertEquals(EditorEnterPolicy.Behavior.PerformStandardAction(code), policy.behavior)
    }

    @Test fun `NONE is newline`() = assertNewline(EditorEnterPolicy.from(EditorInfo.IME_ACTION_NONE))
    @Test fun `UNSPECIFIED is newline`() = assertNewline(EditorEnterPolicy.from(EditorInfo.IME_ACTION_UNSPECIFIED))
    @Test fun `GO pairs label and action`() = assertStandard(EditorInfo.IME_ACTION_GO, "前往")
    @Test fun `SEARCH pairs label and action`() = assertStandard(EditorInfo.IME_ACTION_SEARCH, "搜尋")
    @Test fun `SEND pairs label and action`() = assertStandard(EditorInfo.IME_ACTION_SEND, "傳送")
    @Test fun `NEXT pairs label and action`() = assertStandard(EditorInfo.IME_ACTION_NEXT, "下一步")
    @Test fun `DONE pairs label and action`() = assertStandard(EditorInfo.IME_ACTION_DONE, "完成")
    @Test fun `PREVIOUS pairs label and action`() = assertStandard(EditorInfo.IME_ACTION_PREVIOUS, "上一步")

    @Test fun `NO_ENTER_ACTION suppresses SEND label and behavior`() =
        assertNewline(EditorEnterPolicy.from(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION))

    @Test fun `NO_ENTER_ACTION suppresses GO label and behavior`() =
        assertNewline(EditorEnterPolicy.from(EditorInfo.IME_ACTION_GO or EditorInfo.IME_FLAG_NO_ENTER_ACTION))

    @Test fun `NO_ENTER_ACTION wins over every masked action`() {
        (0..255).forEach {
            assertNewline(EditorEnterPolicy.from(it or EditorInfo.IME_FLAG_NO_ENTER_ACTION))
        }
    }

    @Test fun `custom label pairs with custom id instead of standard SEND`() {
        val policy = EditorEnterPolicy.from(EditorInfo.IME_ACTION_SEND, "確認", 73)
        assertEquals("確認", policy.label)
        assertTrue(policy.isActionPresentation)
        assertEquals(EditorEnterPolicy.Behavior.PerformCustomAction(73), policy.behavior)
    }

    @Test fun `NO_ENTER_ACTION overrides custom label and id`() = assertNewline(
        EditorEnterPolicy.from(EditorInfo.IME_ACTION_GO or EditorInfo.IME_FLAG_NO_ENTER_ACTION, "確認", 73),
    )

    @Test fun `custom id zero remains eligible even with NONE`() {
        val policy = EditorEnterPolicy.from(EditorInfo.IME_ACTION_NONE, "確認", 0)
        assertTrue(policy.isActionPresentation)
        assertEquals(EditorEnterPolicy.Behavior.PerformCustomAction(0), policy.behavior)
    }

    @Test fun `custom newline label remains action presentation`() {
        val policy = EditorEnterPolicy.from(EditorInfo.IME_ACTION_SEND, "換行", 73)
        assertEquals("換行", policy.label)
        assertTrue(policy.isActionPresentation)
        assertEquals(EditorEnterPolicy.Behavior.PerformCustomAction(73), policy.behavior)
    }

    @Test fun `supplied empty custom label remains custom rather than silently choosing SEND`() {
        val policy = EditorEnterPolicy.from(EditorInfo.IME_ACTION_SEND, "", 73)
        assertEquals("", policy.label)
        assertTrue(policy.isActionPresentation)
        assertEquals(EditorEnterPolicy.Behavior.PerformCustomAction(73), policy.behavior)
    }

    @Test fun `unrelated high flags do not change standard action`() {
        val policy = EditorEnterPolicy.from(EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING)
        assertEquals("搜尋", policy.label)
        assertEquals(EditorEnterPolicy.Behavior.PerformStandardAction(EditorInfo.IME_ACTION_SEARCH), policy.behavior)
    }

    @Test fun `unknown action falls back like unspecified metadata`() = assertNewline(EditorEnterPolicy.from(255))
}
