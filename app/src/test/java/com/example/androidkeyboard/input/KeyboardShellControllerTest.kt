package com.example.androidkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardShellControllerTest {

    private val controller = KeyboardController()

    @Test
    fun toggleLanguageFromZhuyinCommitsActiveCompositionAndSwitchesToEnglish() {
        val initial = KeyboardRuntimeState(
            inputMode = InputMode.ZHUYIN,
            page = KeyboardPage.LETTERS,
            shifted = false,
            candidateExpanded = false,
        )

        val result = controller.reduce(
            state = initial,
            command = ImeCommand.ToggleLanguage,
            context = ControllerContext(hasActiveComposition = true),
        )

        assertEquals(InputMode.ENGLISH, result.state.inputMode)
        assertEquals(KeyboardPage.LETTERS, result.state.page)
        assertEquals(listOf(ImeEffect.CommitComposition), result.effects)
    }

    @Test
    fun toggleLanguageFromEnglishReturnsToZhuyinWithoutExternalEffect() {
        val initial = KeyboardRuntimeState(
            inputMode = InputMode.ENGLISH,
            page = KeyboardPage.LETTERS,
            shifted = true,
            candidateExpanded = true,
        )

        val result = controller.reduce(
            state = initial,
            command = ImeCommand.ToggleLanguage,
            context = ControllerContext(hasActiveComposition = false),
        )

        assertEquals(InputMode.ZHUYIN, result.state.inputMode)
        assertEquals(KeyboardPage.LETTERS, result.state.page)
        assertEquals(false, result.state.shifted)
        assertEquals(false, result.state.candidateExpanded)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun openingSymbolsIsPureRuntimeStateTransition() {
        val initial = KeyboardRuntimeState.defaultZhuyin()

        val result = controller.reduce(
            state = initial,
            command = ImeCommand.OpenSymbols,
            context = ControllerContext(hasActiveComposition = false),
        )

        assertEquals(KeyboardPage.SYMBOLS_PRIMARY, result.state.page)
        assertTrue(result.effects.isEmpty())
    }

    @Test
    fun defaultBottomRowContainsSingleSpaceLanguageAndEnter() {
        val profile = BottomRowProfile.default()
        val all = profile.left + profile.center + profile.right

        assertEquals(1, all.count { it == BottomKey.SPACE })
        assertEquals(1, all.count { it == BottomKey.LANGUAGE })
        assertEquals(1, all.count { it == BottomKey.ENTER })
    }

    @Test
    fun invalidBottomRowFallsBackToBuiltInDefault() {
        val invalid = BottomRowProfile(
            left = listOf(BottomKey.SPACE, BottomKey.SPACE),
            center = BottomKey.SPACE,
            right = listOf(BottomKey.ENTER),
        )

        assertEquals(BottomRowProfile.default(), BottomRowProfileValidator.sanitize(invalid))
    }
}
