package com.example.androidkeyboard.engines.core

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.androidkeyboard.engines.core.ChewingEngine.Layout
import com.example.androidkeyboard.engines.core.ChineseConverter.Profile
import com.example.androidkeyboard.input.KeyboardPreferenceKeys
import com.example.androidkeyboard.input.SharedPreferencesKeyboardPreferencesRepository

/** Persistent user-facing IME preferences. Decoder lifecycle is owned by the service. */
class IMEConfig(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    private val keyboardPreferences = SharedPreferencesKeyboardPreferencesRepository(prefs)

    /** Only Dachen has a complete visual keyboard in the current release. */
    val layout: Layout
        get() = Layout.DACHEN

    /** Reserved for the future OpenCC backend; not exposed while conversion is unavailable. */
    var s2tProfile: Profile
        get() = enumValue(KEY_S2T_PROFILE, Profile.S2TW)
        set(value) = prefs.edit().putString(KEY_S2T_PROFILE, value.name).apply()

    /** Reserved for the future OpenCC backend; not exposed while conversion is unavailable. */
    var t2sProfile: Profile
        get() = enumValue(KEY_T2S_PROFILE, Profile.TW2S)
        set(value) = prefs.edit().putString(KEY_T2S_PROFILE, value.name).apply()

    /**
     * Legacy preference retained for forward compatibility. The current build has no
     * OpenCC backend, so the default is deliberately disabled and the converter still
     * enforces its own availability check.
     */
    var conversionEnabled: Boolean
        get() = prefs.getBoolean(KEY_CONVERSION_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_CONVERSION_ENABLED, value).apply()

    /** Delegates to the typed keyboard repository while preserving the released key. */
    var hapticEnabled: Boolean
        get() = keyboardPreferences.load().hapticEnabled
        set(value) = keyboardPreferences.setHapticEnabled(value)

    /**
     * Delegates legacy Float/String migration and range validation to the typed keyboard
     * repository. Writes remain ListPreference-compatible strings under the released key.
     */
    var proximityTolerance: Float
        get() = keyboardPreferences.load().proximityTolerance
        set(value) = keyboardPreferences.setProximityTolerance(value)

    private inline fun <reified T : Enum<T>> enumValue(key: String, default: T): T {
        val raw = prefs.getString(key, default.name) ?: default.name
        return enumValues<T>().firstOrNull { it.name == raw } ?: default
    }

    companion object {
        const val KEY_S2T_PROFILE = "s2t_profile"
        const val KEY_T2S_PROFILE = "t2s_profile"
        const val KEY_CONVERSION_ENABLED = "conversion_enabled"
        const val KEY_HAPTIC_ENABLED = KeyboardPreferenceKeys.HAPTIC_ENABLED
        const val KEY_PROXIMITY_TOLERANCE = KeyboardPreferenceKeys.PROXIMITY_TOLERANCE
    }
}
