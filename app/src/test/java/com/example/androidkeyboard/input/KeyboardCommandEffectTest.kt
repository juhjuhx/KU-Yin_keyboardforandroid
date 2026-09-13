package com.example.androidkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardCommandEffectTest {

    private val controller = KeyboardController()

    @Test
    fun zhuyinInputRoutesPhysicalCodeToChewing() {
        val result = controller.reduce(
            state = KeyboardRuntimeState.defaultZhuyin(),
            command = ImeCommand.Input('1'.code),
            context = ControllerContext(
                hasActiveComposition = false,
                allowComposition = true,
            ),
        )

        assertEquals(listOf(ImeEffect.SendChewingKey('1'.code)), result.effects)
    }

    @Test
    fun englishInputCommitsLowercaseText() {
        val state = KeyboardRuntimeState.defaultZhuyin().copy(inputMode = InputMode.ENGLISH)

        val result = controller.reduce(
            state = state,
            command = ImeCommand.Input('q'.code),
            context = ControllerContext(
                hasActiveComposition = false,
                allowComposition = true,
            ),
        )

        assertEquals(listOf(ImeEffect.CommitText("q")), result.effects)
        assertFalse(result.state.shifted)
    }

    @Test
    fun shiftedEnglishLetterCommitsUppercaseAndResetsShift() {
        val state = KeyboardRuntimeState.defaultZhuyin().copy(
            inputMode = InputMode.ENGLISH,
            shifted = true,
        )

        val result = controller.reduce(
            state = state,
            command = ImeCommand.Input('q'.code),
            context = ControllerContext(
                hasActiveComposition = false,
                allowComposition = true,
            ),
        )

        assertEquals(listOf(ImeEffect.CommitText("Q")), result.effects)
        assertFalse(result.state.shifted)
    }

    @Test
    fun secureEditorCannotToggleBackIntoZhuyin() {
        val state = KeyboardRuntimeState.defaultZhuyin().copy(inputMode = InputMode.ENGLISH)

        val result = controller.reduce(
            state = state,
            command = ImeCommand.ToggleLanguage,
            context = ControllerContext(
                hasActiveComposition = false,
                allowComposition = false,
            ),
        )

        assertEquals(InputMode.ENGLISH, result.state.inputMode)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun secureEditorNormalizesStaleZhuyinStateBeforeInput() {
        val result = controller.reduce(
            state = KeyboardRuntimeState.defaultZhuyin(),
            command = ImeCommand.Input('q'.code),
            context = ControllerContext(
                hasActiveComposition = false,
                allowComposition = false,
            ),
        )

        assertEquals(InputMode.ENGLISH, result.state.inputMode)
        assertEquals(listOf(ImeEffect.CommitText("q")), result.effects)
    }

    @Test
    fun englishBackspaceDeletesFromEditorInsteadOfCallingChewing() {
        val state = KeyboardRuntimeState.defaultZhuyin().copy(inputMode = InputMode.ENGLISH)

        val result = controller.reduce(
            state = state,
            command = ImeCommand.Backspace,
            context = ControllerContext(
                hasActiveComposition = false,
                allowComposition = true,
            ),
        )

        assertEquals(listOf(ImeEffect.DeleteBackward), result.effects)
    }

    @Test
    fun zhuyinBackspaceDelegatesToChewing() {
        val result = controller.reduce(
            state = KeyboardRuntimeState.defaultZhuyin(),
            command = ImeCommand.Backspace,
            context = ControllerContext(
                hasActiveComposition = true,
                allowComposition = true,
            ),
        )

        assertEquals(listOf(ImeEffect.BackspaceChewing), result.effects)
    }

    @Test
    fun englishSpaceCommitsLiteralSpace() {
        val state = KeyboardRuntimeState.defaultZhuyin().copy(inputMode = InputMode.ENGLISH)

        val result = controller.reduce(
            state = state,
            command = ImeCommand.Space,
            context = ControllerContext(
                hasActiveComposition = false,
                allowComposition = true,
            ),
        )

        assertEquals(listOf(ImeEffect.CommitText(" ")), result.effects)
    }

    @Test
    fun zhuyinSpaceRoutesSpaceKeyThroughChewing() {
        val result = controller.reduce(
            state = KeyboardRuntimeState.defaultZhuyin(),
            command = ImeCommand.Space,
            context = ControllerContext(
                hasActiveComposition = true,
                allowComposition = true,
            ),
        )

        assertEquals(listOf(ImeEffect.SendChewingKey(' '.code)), result.effects)
    }

    @Test
    fun enterCommitsActiveZhuyinCompositionBeforeEditorAction() {
        val result = controller.reduce(
            state = KeyboardRuntimeState.defaultZhuyin(),
            command = ImeCommand.Enter,
            context = ControllerContext(
                hasActiveComposition = true,
                allowComposition = true,
            ),
        )

        assertEquals(
            listOf(ImeEffect.CommitComposition, ImeEffect.PerformEditorAction),
            result.effects,
        )
    }

    @Test
    fun dismissCommitsCompositionBeforeHidingKeyboard() {
        val result = controller.reduce(
            state = KeyboardRuntimeState.defaultZhuyin(),
            command = ImeCommand.Dismiss,
            context = ControllerContext(
                hasActiveComposition = true,
                allowComposition = true,
            ),
        )

        assertEquals(
            listOf(ImeEffect.CommitComposition, ImeEffect.HideKeyboard),
            result.effects,
        )
    }

    @Test
    fun nextInputMethodProducesSystemEffect() {
        val result = controller.reduce(
            state = KeyboardRuntimeState.defaultZhuyin().copy(inputMode = InputMode.ENGLISH),
            command = ImeCommand.NextInputMethod,
            context = ControllerContext(
                hasActiveComposition = false,
                allowComposition = true,
            ),
        )

        assertEquals(listOf(ImeEffect.ShowNextInputMethod), result.effects)
    }
}
