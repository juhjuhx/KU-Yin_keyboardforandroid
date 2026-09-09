package com.example.androidkeyboard.engines.opencc

import com.example.androidkeyboard.engines.core.ChineseConverter
import com.example.androidkeyboard.engines.core.ChineseConverter.Profile

/**
 * Wave 4 T21: OpenCC wrapper for Traditional/Simplified conversion.
 *
 * STUB — actual OpenCC Java bindings will be wired when the prebuilt
 * OpenCC library is available (see OPENCC-WIRING.md for profile details).
 *
 * Fallback: returns text unchanged when no native library loaded.
 */
class OpenCCConverter : ChineseConverter {

    private var _s2tProfile = Profile.S2TW
    private var _t2sProfile = Profile.TW2S
    private var _enabled = true
    private var _ready = false

    override val isReady: Boolean get() = _ready

    override fun init(s2tProfile: Profile, t2sProfile: Profile) {
        _s2tProfile = s2tProfile
        _t2sProfile = t2sProfile
        _ready = true
        // OpenCC native library not yet available; stub returns pass-through.
        // When native lib is ready, call opencc_init(s2tProfile.name, t2sProfile.name)
        // and wire simplifyToTraditional/traditionalToSimplify accordingly.
        // See docs/OPENCC-WIRING.md for profile details.
    }

    override fun simplifyToTraditional(text: String): String {
        if (!_ready || !_enabled) return text
        // OpenCC native binding not yet linked; pass-through until available.
        // TODO: opencc_simple_to_traditional(text, _s2tProfile.name)
        return text
    }

    override fun traditionalToSimplify(text: String): String {
        if (!_ready || !_enabled) return text
        // OpenCC native binding not yet linked; pass-through until available.
        // TODO: opencc_traditional_to_simplified(text, _t2sProfile.name)
        return text
    }

    override fun toggleDirection(): Pair<Profile, Profile> {
        val tmp = _s2tProfile
        _s2tProfile = _t2sProfile
        _t2sProfile = tmp
        return Pair(_s2tProfile, _t2sProfile)
    }

    override var enabled: Boolean
        get() = _enabled
        set(value) { _enabled = value }
}
