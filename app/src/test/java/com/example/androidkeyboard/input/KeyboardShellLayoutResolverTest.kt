package com.example.androidkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardShellLayoutResolverTest {

    private val resolver = KeyboardShellLayoutResolver()
    private val controller = KeyboardController()

    @Test
    fun defaultZhuyinSurfaceHasDachenAndShellControls() {
        val layout = resolver.resolve(
            state = KeyboardRuntimeState.defaultZhuyin(),
            preferences = KeyboardPreferences(),
        )
        val keys = layout.rows.flatMap { it.keys }

        assertTrue(keys.any { it.label == "ㄅ" })
        assertEquals(1, keys.count { it.command == ImeCommand.Space })
        assertTrue(keys.any { it.command == ImeCommand.ToggleLanguage })
        assertTrue(keys.any { it.command == ImeCommand.Enter })
    }

    @Test
    fun secondaryLabelsPreferenceOnlyChangesPresentationMetadata() {
        val visible = resolver.resolve(
            state = KeyboardRuntimeState.defaultZhuyin(),
            preferences = KeyboardPreferences(showSecondaryLabels = true),
        ).rows.flatMap { it.keys }.first { it.label == "ㄅ" }
        val hidden = resolver.resolve(
            state = KeyboardRuntimeState.defaultZhuyin(),
            preferences = KeyboardPreferences(showSecondaryLabels = false),
        ).rows.flatMap { it.keys }.first { it.label == "ㄅ" }

        assertEquals("1", visible.secondaryLabel)
        assertNull(hidden.secondaryLabel)
        assertEquals(visible.id, hidden.id)
        assertEquals(visible.command, hidden.command)
        assertEquals(visible.role, hidden.role)
    }

    @Test
    fun englishSurfaceIsQwertyAndKeepsLanguageToggle() {
        val layout = resolver.resolve(
            state = KeyboardRuntimeState.defaultZhuyin().copy(inputMode = InputMode.ENGLISH),
            preferences = KeyboardPreferences(),
        )
        val keys = layout.rows.flatMap { it.keys }
        val labels = keys.map { it.label }

        assertTrue(labels.containsAll(listOf("q", "w", "e", "a", "s", "d")))
        assertTrue(keys.any { it.command == ImeCommand.Shift })
        assertTrue(keys.any { it.command == ImeCommand.ToggleLanguage })
        assertEquals(1, keys.count { it.command == ImeCommand.Space })
    }

    @Test
    fun symbolsSurfaceCommitsLiteralTextAndCanReturnToLetters() {
        val layout = resolver.resolve(
            state = KeyboardRuntimeState.defaultZhuyin().copy(page = KeyboardPage.SYMBOLS_PRIMARY),
            preferences = KeyboardPreferences(),
        )
        val keys = layout.rows.flatMap { it.keys }
        val inserts = keys.mapNotNull { it.command as? ImeCommand.InsertText }

        assertTrue(inserts.any { it.text == "1" })
        assertTrue(inserts.any { it.text == "?" })
        assertTrue(keys.any { it.command == ImeCommand.ReturnToLetters })
    }

    @Test
    fun primaryAndSecondarySymbolPagesUseRealStateTransitions() {
        val primaryState = KeyboardRuntimeState.defaultZhuyin().copy(
            page = KeyboardPage.SYMBOLS_PRIMARY,
        )
        val primaryLayout = resolver.resolve(primaryState, KeyboardPreferences())
        val primaryKeys = primaryLayout.rows.flatMap { it.keys }

        assertTrue(
            primaryKeys.any {
                it.label == "#+=" && it.command == ImeCommand.OpenSymbolsSecondary
            },
        )

        val secondaryResult = controller.reduce(
            state = primaryState,
            command = ImeCommand.OpenSymbolsSecondary,
            context = ControllerContext(hasActiveComposition = false),
        )
        assertEquals(KeyboardPage.SYMBOLS_SECONDARY, secondaryResult.state.page)
        assertTrue(secondaryResult.effects.isEmpty())

        val secondaryLayout = resolver.resolve(secondaryResult.state, KeyboardPreferences())
        val secondaryKeys = secondaryLayout.rows.flatMap { it.keys }
        assertTrue(
            secondaryKeys.any {
                it.label == "?123" && it.command == ImeCommand.OpenSymbolsPrimary
            },
        )

        val primaryResult = controller.reduce(
            state = secondaryResult.state,
            command = ImeCommand.OpenSymbolsPrimary,
            context = ControllerContext(hasActiveComposition = false),
        )
        assertEquals(KeyboardPage.SYMBOLS_PRIMARY, primaryResult.state.page)
        assertTrue(primaryResult.effects.isEmpty())
    }

    @Test
    fun emojiSurfaceCommitsLiteralEmojiAndCanReturnToLetters() {
        val layout = resolver.resolve(
            state = KeyboardRuntimeState.defaultZhuyin().copy(page = KeyboardPage.EMOJI),
            preferences = KeyboardPreferences(),
        )
        val keys = layout.rows.flatMap { it.keys }
        val inserts = keys.mapNotNull { it.command as? ImeCommand.InsertText }

        assertTrue(inserts.any { it.text == "😀" })
        assertTrue(keys.any { it.command == ImeCommand.ReturnToLetters })
    }

    @Test
    fun disabledLanguageKeyIsActuallyRemovedFromSurface() {
        val layout = resolver.resolve(
            state = KeyboardRuntimeState.defaultZhuyin(),
            preferences = KeyboardPreferences(showLanguageKey = false),
        )
        val keys = layout.rows.flatMap { it.keys }

        assertFalse(keys.any { it.command == ImeCommand.ToggleLanguage })
    }
}
