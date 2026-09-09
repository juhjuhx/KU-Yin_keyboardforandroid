package com.example.androidkeyboard.input

/**
 * KeyMapping: libchewing 標準布局（Dachen）鍵位對照
 *
 * Maps bopomofo characters to ASCII key codes matching physical key positions
 * on a standard US keyboard (QWERTY). These ASCII codes are passed directly
 * to libchewing's chewing_handle_default().
 */
object KeyMapping {

    /**
     * Bopomofo character → ASCII keycode (LABEL-HINT ONLY)
     *
     * ⚠️ NOTE: This map is NOT the authoritative source for the Dachen layout.
     * True Dachen assigns the SAME bopomofo to TWO physical keys (ㄉ on '2'/X,
     * ㄋ on S/V, ㄊ on W/C), and a Map<String, Int> cannot represent that — later
     * entries always win, so duplicate keys silently collapse.
     *
     * The authoritative per-physical-key ASCII codes now live on each KeyDef.code
     * in KeyboardLayout.kt. KeyboardView passes the pressed KeyDef through to
     * ChewingInputMethodService.handleKey, which uses KeyDef.code directly.
     *
     * This map is retained ONLY as a fallback hint for label-only code paths
     * (e.g. programmatic commits not tied to a physical key). Duplicate bopomofo
     * entries have been REMOVED so no value here silently overrides another; each
     * bopomofo maps to a single canonical entry.
     *
     * Dachen (大千) layout: number row, QWERTY rows, symbol row.
     * ASCII codes: '1'=49, '2'=50, ..., '0'=48, '-'=45,
     *              'q'=113, 'w'=119, ..., 'p'=112,
     *              'a'=97, 's'=115, ..., ';'=59,
     *              'z'=122, 'x'=120, ..., 'm'=109,
     *              ','=44, '/'=47.
     *
     * Canonical hint values (one per bopomofo, no duplicates):
     *   ㄉ→50 ('2' row), ㄋ→115 ('S' row), ㄊ→119 ('W' row)
     *   (the X/V/C duplicates are intentionally absent — use KeyDef.code
     *    for the physical key being pressed).
     */
    val charToLibchewingKeyCode: Map<String, Int> = mapOf(
        // Row 1 (number row)
        "ㄅ" to 49,   // '1'
        "ㄉ" to 50,   // '2' (canonical hint; X=120 lives on KeyDef.code)
        "ˇ" to 51,   // '3' (tone 3)
        "ˋ" to 52,   // '4' (tone 4)
        "ㄓ" to 53,   // '5'
        "ˊ" to 54,   // '6' (tone 2)
        "˙" to 55,   // '7' (tone 5)
        "ㄚ" to 56,   // '8'
        "ㄞ" to 57,   // '9'
        "ㄢ" to 48,   // '0'
        "ㄦ" to 45,   // '-'

        // Row 2 (QWERTY)
        "ㄆ" to 113,  // 'q'
        "ㄊ" to 119,  // 'w' (canonical hint; C=99 lives on KeyDef.code)
        "ㄍ" to 101,  // 'e'
        "ㄐ" to 114,  // 'r'
        "ㄔ" to 116,  // 't'
        "ㄗ" to 121,  // 'y'
        "ㄧ" to 117,  // 'u'
        "ㄛ" to 105,  // 'i'
        "ㄟ" to 111,  // 'o'
        "ㄣ" to 112,  // 'p'

        // Row 3 (ASDF)
        "ㄇ" to 97,   // 'a'
        "ㄋ" to 115,  // 's' (canonical hint; V=118 lives on KeyDef.code)
        "ㄎ" to 100,  // 'd'
        "ㄑ" to 102,  // 'f'
        "ㄕ" to 103,  // 'g'
        "ㄘ" to 104,  // 'h'
        "ㄨ" to 106,  // 'j'
        "ㄜ" to 107,  // 'k'
        "ㄠ" to 108,  // 'l'
        "ㄤ" to 59,   // ';'

        // Row 4 (ZXCVBNM) — X/X vs 2, C vs W, V vs S duplicates removed
        "ㄈ" to 122,  // 'z'
        "ㄌ" to 98,   // 'b'
        "ㄙ" to 110,  // 'n'
        "ㄡ" to 109,  // 'm'

        // Row 5 (symbols)
        "ㄝ" to 44,   // ','
        "ㄥ" to 47,   // '/'
    )

    /**
     * Get ASCII keycode for a bopomofo character
     */
    fun getLibchewingKeyCode(char: String): Int {
        return charToLibchewingKeyCode[char] ?: -1
    }
}
