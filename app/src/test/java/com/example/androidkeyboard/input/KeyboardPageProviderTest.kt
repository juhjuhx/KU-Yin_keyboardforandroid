package com.example.androidkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardPageProviderTest {

    @Test
    fun dachenProviderPreservesPinnedZhuyinMapping() {
        val layout = DachenPageProvider().resolve(KeyboardRuntimeState.defaultZhuyin())

        val first = layout.rows.first().keys.first()
        assertEquals("ㄅ", first.label)
        assertEquals(ImeCommand.Input('1'.code), first.command)

        val tail = layout.rows.last().keys.first { it.label == "ㄥ" }
        assertEquals(ImeCommand.Input('/'.code), tail.command)
    }

    @Test
    fun dachenProviderExposesStableIdentitySecondaryLegendAndCharacterRole() {
        val layout = DachenPageProvider().resolve(KeyboardRuntimeState.defaultZhuyin())

        val first = layout.rows.first().keys.first()
        assertEquals("dachen:input:49", first.id)
        assertEquals("1", first.secondaryLabel)
        assertEquals(KeyRole.CHARACTER, first.role)

        val tail = layout.rows.last().keys.first { it.label == "ㄥ" }
        assertEquals("dachen:input:47", tail.id)
        assertEquals("/", tail.secondaryLabel)
        assertEquals(KeyRole.CHARACTER, tail.role)
    }

    @Test
    fun dachenProviderConvertsSpecialKeysToCommands() {
        val layout = DachenPageProvider().resolve(KeyboardRuntimeState.defaultZhuyin())
        val commands = layout.rows.flatMap { it.keys }.map { it.command }

        assertTrue(commands.contains(ImeCommand.Backspace))
        assertTrue(commands.contains(ImeCommand.Space))
        assertTrue(commands.contains(ImeCommand.Enter))
        assertTrue(commands.contains(ImeCommand.Dismiss))
    }

    @Test
    fun englishProviderUsesLowercaseLabelsWhenShiftIsOff() {
        val state = KeyboardRuntimeState.defaultZhuyin().copy(
            inputMode = InputMode.ENGLISH,
            shifted = false,
        )

        val layout = EnglishPageProvider().resolve(state)
        val q = layout.rows[1].keys.first()

        assertEquals("q", q.label)
        assertEquals(ImeCommand.Input('q'.code), q.command)
        assertEquals("english:input:113", q.id)
        assertNull(q.secondaryLabel)
        assertEquals(KeyRole.CHARACTER, q.role)
    }

    @Test
    fun englishProviderUsesUppercaseLabelsWithoutChangingPhysicalCodeOrStableIdentity() {
        val state = KeyboardRuntimeState.defaultZhuyin().copy(
            inputMode = InputMode.ENGLISH,
            shifted = true,
        )

        val layout = EnglishPageProvider().resolve(state)
        val q = layout.rows[1].keys.first()

        assertEquals("Q", q.label)
        assertEquals(ImeCommand.Input('q'.code), q.command)
        assertEquals("english:input:113", q.id)
        assertEquals(KeyRole.CHARACTER, q.role)
    }

    @Test
    fun resolvedLayoutKeepsLegacySizingMetadataForIncrementalUiMigration() {
        val layout = EnglishPageProvider().resolve(
            KeyboardRuntimeState.defaultZhuyin().copy(inputMode = InputMode.ENGLISH),
        )
        val shift = layout.rows[3].keys.first()

        assertEquals("⇧", shift.label)
        assertEquals(15f, shift.widthPct, 0.0001f)
        assertEquals("english:shift", shift.id)
        assertEquals(KeyRole.FUNCTION, shift.role)
        assertNull(shift.secondaryLabel)
        assertEquals(ImeCommand.Shift, shift.command)
    }
}
