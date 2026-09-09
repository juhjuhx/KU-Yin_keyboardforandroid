package com.example.androidkeyboard.engines.opencc

import com.example.androidkeyboard.engines.core.ChineseConverter
import com.example.androidkeyboard.engines.core.ChineseConverter.Profile

/**
 * Placeholder boundary for a future OpenCC backend.
 *
 * The current build does not ship OpenCC bindings. This adapter therefore
 * reports itself unavailable and always returns text unchanged. Keeping the
 * boundary explicit prevents Settings or the IME from advertising a feature
 * that is not actually present while preserving a stable integration point.
 */
class OpenCCConverter : ChineseConverter {

    private var _s2tProfile = Profile.S2TW
    private var _t2sProfile = Profile.TW2S
    private var _enabled = false

    /** No OpenCC backend is packaged in the current build. */
    override val isReady: Boolean get() = false

    override fun init(s2tProfile: Profile, t2sProfile: Profile) {
        _s2tProfile = s2tProfile
        _t2sProfile = t2sProfile
        _enabled = false
    }

    override fun simplifyToTraditional(text: String): String = text

    override fun traditionalToSimplify(text: String): String = text

    override fun toggleDirection(): Pair<Profile, Profile> {
        val tmp = _s2tProfile
        _s2tProfile = _t2sProfile
        _t2sProfile = tmp
        return Pair(_s2tProfile, _t2sProfile)
    }

    override var enabled: Boolean
        get() = _enabled && isReady
        set(value) {
            _enabled = value && isReady
        }
}
