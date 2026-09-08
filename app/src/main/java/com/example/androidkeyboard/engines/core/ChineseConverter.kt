package com.example.androidkeyboard.engines.core

/**
 * Wave 4 T21: Platform-agnostic Traditional/Simplified Chinese converter interface.
 * Wraps OpenCC (or any future converter).
 */
interface ChineseConverter {

    enum class Profile(val name: String) {
        S2TW(\"s2tw\"),
        TW2S(\"tw2s\"),
        S2T(\"s2t\"),
        T2S(\"t2s\"),
        S2TWP(\"s2twp\"),
        TW2SP(\"tw2sp\"),
    }

    fun init(s2tProfile: Profile = Profile.S2TW, t2sProfile: Profile = Profile.TW2S)
    fun simplifyToTraditional(text: String): String
    fun traditionalToSimplify(text: String): String
    fun toggleDirection(): Pair<Profile, Profile>
    var enabled: Boolean
    val isReady: Boolean
}
