package com.example.androidkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardPreferencesCodecTest {

    @Test
    fun defaultsMatchV02KeyboardShellContract() {
        val prefs = KeyboardPreferenceCodec.decode(emptyMap<String, Any?>())

        assertEquals(DefaultInputMode.ZHUYIN, prefs.defaultMode)
        assertFalse(prefs.rememberLastMode)
        assertEquals(BottomRowProfile.default(), prefs.bottomRowProfile)
        assertTrue(prefs.showSecondaryLabels)
        assertTrue(prefs.showLanguageKey)
        assertEquals(KeyboardHeight.STANDARD, prefs.keyboardHeight)
        assertTrue(prefs.hapticEnabled)
        assertEquals(0.15f, prefs.proximityTolerance, 0.0001f)
        assertFalse(prefs.candidateExpandedByDefault)
    }

    @Test
    fun legacyHapticPreferenceIsPreserved() {
        val prefs = KeyboardPreferenceCodec.decode(
            mapOf(KeyboardPreferenceKeys.HAPTIC_ENABLED to false),
        )

        assertFalse(prefs.hapticEnabled)
    }

    @Test
    fun legacyFloatProximityPreferenceIsPreserved() {
        val prefs = KeyboardPreferenceCodec.decode(
            mapOf(KeyboardPreferenceKeys.PROXIMITY_TOLERANCE to 0.30f),
        )

        assertEquals(0.30f, prefs.proximityTolerance, 0.0001f)
    }

    @Test
    fun currentStringProximityPreferenceIsPreserved() {
        val prefs = KeyboardPreferenceCodec.decode(
            mapOf(KeyboardPreferenceKeys.PROXIMITY_TOLERANCE to "0.05"),
        )

        assertEquals(0.05f, prefs.proximityTolerance, 0.0001f)
    }

    @Test
    fun malformedProximityFallsBackToSafeDefault() {
        val prefs = KeyboardPreferenceCodec.decode(
            mapOf(KeyboardPreferenceKeys.PROXIMITY_TOLERANCE to "not-a-number"),
        )

        assertEquals(0.15f, prefs.proximityTolerance, 0.0001f)
    }
}
