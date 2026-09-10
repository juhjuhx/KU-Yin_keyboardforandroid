package com.example.androidkeyboard.engines.core

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.androidkeyboard.engines.core.ChewingEngine.Layout
import com.example.androidkeyboard.engines.core.ChineseConverter.Profile

/** Persistent user-facing IME preferences. Decoder lifecycle is owned by the service. */
class IMEConfig(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

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

    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    /**
     * ListPreference stores strings. Reading through prefs.all also migrates older
     * builds that stored this value as a Float under the same key.
     */
    var proximityTolerance: Float
        get() = when (val stored = prefs.all[KEY_PROXIMITY_TOLERANCE]) {
            is Number -> stored.toFloat()
            is String -> stored.toFloatOrNull() ?: DEFAULT_PROXIMITY_TOLERANCE
            else -> DEFAULT_PROXIMITY_TOLERANCE
        }.coerceIn(0f, 0.5f)
        set(value) = prefs.edit()
            .putString(KEY_PROXIMITY_TOLERANCE, value.coerceIn(0f, 0.5f).toString())
            .apply()

    private inline fun <reified T : Enum<T>> enumValue(key: String, default: T): T {
        val raw = prefs.getString(key, default.name) ?: default.name
        return enumValues<T>().firstOrNull { it.name == raw } ?: default
    }

    companion object {
        const val KEY_S2T_PROFILE = "s2t_profile"
        const val KEY_T2S_PROFILE = "t2s_profile"
        const val KEY_CONVERSION_ENABLED = "conversion_enabled"
        const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        const val KEY_PROXIMITY_TOLERANCE = "proximity_tolerance"
        private const val DEFAULT_PROXIMITY_TOLERANCE = 0.15f
    }
}
