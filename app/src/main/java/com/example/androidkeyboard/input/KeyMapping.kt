package com.example.androidkeyboard.input

/**
 * libchewing Standard/Dachen label → ASCII fallback hints.
 *
 * Physical key input must use [KeyDef.code] from [KeyboardLayout]. This table is
 * only for code paths that have a Bopomofo label but no physical-key event.
 * Values are aligned with pinned libchewing
 * `src/editor/zhuyin_layout/standard.rs` at
 * `3c4a93aa03d574c7f011ff84e8a2437c2f79b2cf`.
 */
object KeyMapping {
    val charToLibchewingKeyCode: Map<String, Int> = mapOf(
        "ㄅ" to '1'.code,
        "ㄉ" to '2'.code,
        "ˇ" to '3'.code,
        "ˋ" to '4'.code,
        "ㄓ" to '5'.code,
        "ˊ" to '6'.code,
        "˙" to '7'.code,
        "ㄚ" to '8'.code,
        "ㄞ" to '9'.code,
        "ㄢ" to '0'.code,
        "ㄦ" to '-'.code,

        "ㄆ" to 'q'.code,
        "ㄊ" to 'w'.code,
        "ㄍ" to 'e'.code,
        "ㄐ" to 'r'.code,
        "ㄔ" to 't'.code,
        "ㄗ" to 'y'.code,
        "ㄧ" to 'u'.code,
        "ㄛ" to 'i'.code,
        "ㄟ" to 'o'.code,
        "ㄣ" to 'p'.code,

        "ㄇ" to 'a'.code,
        "ㄋ" to 's'.code,
        "ㄎ" to 'd'.code,
        "ㄑ" to 'f'.code,
        "ㄕ" to 'g'.code,
        "ㄘ" to 'h'.code,
        "ㄨ" to 'j'.code,
        "ㄜ" to 'k'.code,
        "ㄠ" to 'l'.code,
        "ㄤ" to ';'.code,

        "ㄈ" to 'z'.code,
        "ㄌ" to 'x'.code,
        "ㄏ" to 'c'.code,
        "ㄒ" to 'v'.code,
        "ㄖ" to 'b'.code,
        "ㄙ" to 'n'.code,
        "ㄩ" to 'm'.code,
        "ㄝ" to ','.code,
        "ㄡ" to '.'.code,
        "ㄥ" to '/'.code,
    )

    fun getLibchewingKeyCode(char: String): Int =
        charToLibchewingKeyCode[char] ?: -1
}
