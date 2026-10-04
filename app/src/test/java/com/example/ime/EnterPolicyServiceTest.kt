package com.example.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.example.ime.EnterServiceHarness.Effect.Action
import com.example.ime.EnterServiceHarness.Effect.Commit
import com.example.ime.engine.KeyboardMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EnterPolicyServiceTest {
    private lateinit var harness: EnterServiceHarness
    @Before fun setUp() { harness = EnterServiceHarness() }
    @After fun tearDown() { harness.close() }

    @Test fun `NONE inserts one newline`() {
        harness.start(EditorInfo.IME_ACTION_NONE)
        harness.enterEditor()
        assertEquals(listOf(Commit("\n")), harness.connection.effects)
    }

    @Test fun `UNSPECIFIED inserts one newline`() {
        harness.start(EditorInfo.IME_ACTION_UNSPECIFIED)
        harness.enterEditor()
        assertEquals(listOf(Commit("\n")), harness.connection.effects)
    }

    @Test fun `six standard actions dispatch their advertised code exactly once`() {
        listOf(EditorInfo.IME_ACTION_GO, EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT, EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_PREVIOUS)
            .forEach { action ->
                harness.start(action)
                harness.enterEditor()
                assertEquals(listOf(Action(action)), harness.connection.effects)
            }
    }

    @Test fun `NO_ENTER_ACTION suppresses SEND dispatch`() {
        harness.start(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        harness.enterEditor()
        assertEquals(listOf(Commit("\n")), harness.connection.effects)
    }

    @Test fun `NO_ENTER_ACTION suppresses GO dispatch`() {
        harness.start(EditorInfo.IME_ACTION_GO or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        harness.enterEditor()
        assertEquals(listOf(Commit("\n")), harness.connection.effects)
    }

    @Test fun `custom action dispatches custom id instead of masked standard action`() {
        harness.start(EditorInfo.IME_ACTION_SEND, "確認", 73)
        harness.enterEditor()
        assertEquals(listOf(Action(73)), harness.connection.effects)
    }

    @Test fun `custom action with NO_ENTER_ACTION inserts newline`() {
        harness.start(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION, "確認", 73)
        harness.enterEditor()
        assertEquals(listOf(Commit("\n")), harness.connection.effects)
    }

    @Test fun `custom label with NONE still dispatches custom id zero`() {
        harness.start(EditorInfo.IME_ACTION_NONE, "確認", 0)
        harness.enterEditor()
        assertEquals(listOf(Action(0)), harness.connection.effects)
    }

    @Test fun `false action return does not inject a second newline effect`() {
        harness.start(EditorInfo.IME_ACTION_SEND)
        harness.connection.actionResult = false
        harness.enterEditor()
        assertEquals(listOf(Action(EditorInfo.IME_ACTION_SEND)), harness.connection.effects)
    }

    @Test fun `explicit multiline NO_ENTER_ACTION retains newline`() {
        harness.start(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION,
            type = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        harness.enterEditor()
        assertEquals(listOf(Commit("\n")), harness.connection.effects)
    }

    @Test fun `dispatch uses the same snapshot as presentation until next callback`() {
        val info = harness.start(EditorInfo.IME_ACTION_SEND)
        info.imeOptions = EditorInfo.IME_ACTION_GO or EditorInfo.IME_FLAG_NO_ENTER_ACTION
        info.actionLabel = "新動作"
        info.actionId = 99
        harness.enterEditor()
        assertEquals(listOf(Action(EditorInfo.IME_ACTION_SEND)), harness.connection.effects)
        harness.start(info.imeOptions, info.actionLabel, info.actionId)
        harness.enterEditor()
        assertEquals(listOf(Commit("\n")), harness.connection.effects)
    }

    @Test fun `non text Enter plumbing preserves English and learning restriction`() {
        listOf(InputType.TYPE_NULL, InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME).forEach { type ->
            harness.start(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION, type = type)
            assertEquals(KeyboardMode.ENGLISH, harness.engine.mode.value)
            assertFalse(harness.native.personalizedLearningEnabled)
            harness.enterEditor()
            assertEquals(listOf(Commit("\n")), harness.connection.effects)
        }
    }
}
