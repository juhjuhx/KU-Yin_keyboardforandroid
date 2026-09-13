package com.example.androidkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardShellLayoutResolverTest {

    private val resolver = KeyboardShellLayoutResolver()

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
