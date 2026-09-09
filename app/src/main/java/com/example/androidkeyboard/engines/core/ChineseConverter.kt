package com.example.androidkeyboard.engines.core

/**
 * Wave 4 T21: Chinese conversion interface.
 *
 * Used for Simplified ↔ Traditional conversion via OpenCC.
 */
interface ChineseConverter {

    enum class Profile {
        S2TW, TW2S, S2T, T2S
    }

    fun init(s2tProfile: Profile, t2sProfile: Profile)
    fun simplifyToTraditional(text: String): String
    fun traditionalToSimplify(text: String): String
    fun toggleDirection(): Pair<Profile, Profile>
    var enabled: Boolean
    val isReady: Boolean
}
