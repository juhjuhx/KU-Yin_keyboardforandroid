package com.example.androidkeyboard.engines.core

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.androidkeyboard.engines.core.ChewingEngine.Layout
import com.example.androidkeyboard.engines.core.ChineseConverter.Profile

/**
 * Wave 4 T21: Centralized IME configuration persisted via SharedPreferences.
 * All UI settings flow through this class; no direct SP access in UI code.
 */
class IMEConfig(private val context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var layout: Layout
        get() = try {
            Layout.valueOf(prefs.getString(KEY_LAYOUT, Layout.DACHEN.name) ?: Layout.DACHEN.name)
        } catch (e: IllegalArgumentException) { Layout.DACHEN }
        set(value) = prefs.edit().putString(KEY_LAYOUT, value.name).apply()

    var fullHalf: Boolean
        get() = prefs.getBoolean(KEY_FULL_HALF, false)
        set(value) = prefs.edit().putBoolean(KEY_FULL_HALF, value).apply()

    var s2tProfile: Profile
        get() = try {
            Profile.valueOf(prefs.getString(KEY_S2T, Profile.S2TW.name) ?: Profile.S2TW.name)
        } catch (e: IllegalArgumentException) { Profile.S2TW }
        set(value) = prefs.edit().putString(KEY_S2T, value.name).apply()

    var t2sProfile: Profile
        get() = try {
            Profile.valueOf(prefs.getString(KEY_T2S, Profile.TW2S.name) ?: Profile.TW2S.name)
        } catch (e: IllegalArgumentException) { Profile.TW2S }
        set(value) = prefs.edit().putString(KEY_T2S, value.name).apply()

    var conversionEnabled: Boolean
        get() = prefs.getBoolean(KEY_CONVERSION, true)
        set(value) = prefs.edit().putBoolean(KEY_CONVERSION, value).apply()

    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var proximityTolerance: Float
        get() = prefs.getFloat(KEY_PROXIMITY, 0.15f).coerceIn(0f, 0.5f)
        set(value) = prefs.edit().putFloat(KEY_PROXIMITY, value).apply()

    var userDictPath: String
        get() = prefs.getString(KEY_USER_DICT_PATH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_DICT_PATH, value).apply()

    fun applyTo(engine: ChewingEngine) {
        engine.init(layout)
    }

    companion object {
        private const val KEY_LAYOUT = "layout"
        private const val KEY_FULL_HALF = "full_half"
        private const val KEY_S2T = "s2t_profile"
        private const val KEY_T2S = "t2s_profile"
        private const val KEY_CONVERSION = "conversion_enabled"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_PROXIMITY = "proximity_tolerance"
        private const val KEY_USER_DICT_PATH = "user_dict_path"
    }
}
