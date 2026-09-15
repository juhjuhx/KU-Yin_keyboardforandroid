package com.example.androidkeyboard.input

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardPreferencesRepositoryTest {

    @Test
    fun loadDecodesLegacyValuesThroughSingleRepositoryContract() {
        val store = FakeKeyboardPreferenceStore(
            mapOf(
                KeyboardPreferenceKeys.HAPTIC_ENABLED to false,
                KeyboardPreferenceKeys.PROXIMITY_TOLERANCE to 0.30f,
            ),
        )
        val repository = KeyboardPreferencesRepository(store)

        val prefs = repository.load()

        assertFalse(prefs.hapticEnabled)
        assertEquals(0.30f, prefs.proximityTolerance, 0.0001f)
    }

    @Test
    fun hapticWriteUsesExistingPreferenceKey() {
        val store = FakeKeyboardPreferenceStore()
        val repository = KeyboardPreferencesRepository(store)

        repository.setHapticEnabled(false)

        assertEquals(false, store.values[KeyboardPreferenceKeys.HAPTIC_ENABLED])
    }

    @Test
    fun proximityWriteUsesListPreferenceCompatibleString() {
        val store = FakeKeyboardPreferenceStore()
        val repository = KeyboardPreferencesRepository(store)

        repository.setProximityTolerance(0.30f)

        assertEquals("0.3", store.values[KeyboardPreferenceKeys.PROXIMITY_TOLERANCE])
        assertEquals(0.30f, repository.load().proximityTolerance, 0.0001f)
    }

    @Test
    fun proximityWriteIsClampedToSupportedRange() {
        val store = FakeKeyboardPreferenceStore()
        val repository = KeyboardPreferencesRepository(store)

        repository.setProximityTolerance(9f)

        assertEquals("0.5", store.values[KeyboardPreferenceKeys.PROXIMITY_TOLERANCE])
    }

    @Test
    fun resetKeyboardShellPreferencesPreservesLegacyTypingPreferences() {
        val store = FakeKeyboardPreferenceStore(
            mapOf(
                KeyboardPreferenceKeys.HAPTIC_ENABLED to false,
                KeyboardPreferenceKeys.PROXIMITY_TOLERANCE to "0.30",
                KeyboardPreferenceKeys.DEFAULT_INPUT_MODE to DefaultInputMode.ENGLISH.name,
                KeyboardPreferenceKeys.REMEMBER_LAST_MODE to true,
                KeyboardPreferenceKeys.SHOW_LANGUAGE_KEY to false,
            ),
        )
        val repository = KeyboardPreferencesRepository(store)

        repository.resetKeyboardShellPreferences()

        assertEquals(false, store.values[KeyboardPreferenceKeys.HAPTIC_ENABLED])
        assertEquals("0.30", store.values[KeyboardPreferenceKeys.PROXIMITY_TOLERANCE])
        assertFalse(store.values.containsKey(KeyboardPreferenceKeys.DEFAULT_INPUT_MODE))
        assertFalse(store.values.containsKey(KeyboardPreferenceKeys.REMEMBER_LAST_MODE))
        assertFalse(store.values.containsKey(KeyboardPreferenceKeys.SHOW_LANGUAGE_KEY))
        assertTrue(repository.load().showLanguageKey)
    }

    private class FakeKeyboardPreferenceStore(
        initial: Map<String, Any?> = emptyMap(),
    ) : KeyboardPreferenceStore {
        val values = initial.toMutableMap()

        override fun snapshot(): Map<String, *> = values.toMap()

        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        override fun putString(key: String, value: String) {
            values[key] = value
        }

        override fun remove(keys: Set<String>) {
            keys.forEach(values::remove)
        }
    }
}
