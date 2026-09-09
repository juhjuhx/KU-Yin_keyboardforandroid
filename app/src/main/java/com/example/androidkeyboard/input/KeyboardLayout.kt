package com.example.androidkeyboard.input

/**
 * Keyboard surfaces used by the IME.
 *
 * Dachen physical ASCII codes are aligned with pinned libchewing
 * `src/editor/zhuyin_layout/standard.rs` at
 * `3c4a93aa03d574c7f011ff84e8a2437c2f79b2cf`.
 */
enum class KeyAction {
    INPUT,
    SPACE,
    BACKSPACE,
    ENTER,
    SHIFT,
    DISMISS,
}

data class KeyDef(
    val label: String,
    val widthPct: Float = 10f,
    val isSpecial: Boolean = false,
    val code: Int = -1,
    val action: KeyAction = KeyAction.INPUT,
)

data class KeyboardRow(val keys: List<KeyDef>)

private fun asciiKeys(labels: String, shifted: Boolean): List<KeyDef> = labels.map { character ->
    val rendered = if (shifted && character.isLetter()) character.uppercaseChar() else character
    KeyDef(rendered.toString(), code = character.code)
}

private fun createAsciiRows(shifted: Boolean): List<KeyboardRow> = listOf(
    KeyboardRow(asciiKeys("1234567890", shifted = false)),
    KeyboardRow(asciiKeys("qwertyuiop", shifted)),
    KeyboardRow(asciiKeys("asdfghjkl", shifted)),
    KeyboardRow(
        listOf(
            KeyDef("⇧", widthPct = 15f, isSpecial = true, action = KeyAction.SHIFT),
        ) +
            asciiKeys("zxcvbnm", shifted) +
            KeyDef("⌫", widthPct = 15f, isSpecial = true, action = KeyAction.BACKSPACE)
    ),
    KeyboardRow(listOf(
        KeyDef(".", widthPct = 12f, code = '.'.code),
        KeyDef(",", widthPct = 12f, code = ','.code),
        KeyDef(" ", widthPct = 40f, code = ' '.code, action = KeyAction.SPACE),
        KeyDef("↵", widthPct = 20f, isSpecial = true, action = KeyAction.ENTER),
        KeyDef("▼", widthPct = 16f, isSpecial = true, action = KeyAction.DISMISS),
    )),
)

enum class KeyboardLayout(val rows: List<KeyboardRow>) {
    Dachen(
        rows = listOf(
            KeyboardRow(listOf(
                KeyDef("ㄅ", code = '1'.code),
                KeyDef("ㄉ", code = '2'.code),
                KeyDef("ˇ", code = '3'.code),
                KeyDef("ˋ", code = '4'.code),
                KeyDef("ㄓ", code = '5'.code),
                KeyDef("ˊ", code = '6'.code),
                KeyDef("˙", code = '7'.code),
                KeyDef("ㄚ", code = '8'.code),
                KeyDef("ㄞ", code = '9'.code),
                KeyDef("ㄢ", code = '0'.code),
                KeyDef("ㄦ", code = '-'.code),
            )),
            KeyboardRow(listOf(
                KeyDef("ㄆ", code = 'q'.code),
                KeyDef("ㄊ", code = 'w'.code),
                KeyDef("ㄍ", code = 'e'.code),
                KeyDef("ㄐ", code = 'r'.code),
                KeyDef("ㄔ", code = 't'.code),
                KeyDef("ㄗ", code = 'y'.code),
                KeyDef("ㄧ", code = 'u'.code),
                KeyDef("ㄛ", code = 'i'.code),
                KeyDef("ㄟ", code = 'o'.code),
                KeyDef("ㄣ", code = 'p'.code),
            )),
            KeyboardRow(listOf(
                KeyDef("ㄇ", code = 'a'.code),
                KeyDef("ㄋ", code = 's'.code),
                KeyDef("ㄎ", code = 'd'.code),
                KeyDef("ㄑ", code = 'f'.code),
                KeyDef("ㄕ", code = 'g'.code),
                KeyDef("ㄘ", code = 'h'.code),
                KeyDef("ㄨ", code = 'j'.code),
                KeyDef("ㄜ", code = 'k'.code),
                KeyDef("ㄠ", code = 'l'.code),
                KeyDef("ㄤ", code = ';'.code),
            )),
            KeyboardRow(listOf(
                KeyDef("ㄈ", code = 'z'.code),
                KeyDef("ㄌ", code = 'x'.code),
                KeyDef("ㄏ", code = 'c'.code),
                KeyDef("ㄒ", code = 'v'.code),
                KeyDef("ㄖ", code = 'b'.code),
                KeyDef("ㄙ", code = 'n'.code),
                KeyDef("ㄩ", code = 'm'.code),
            )),
            KeyboardRow(listOf(
                KeyDef("ㄝ", code = ','.code),
                KeyDef("ㄡ", code = '.'.code),
                KeyDef("ㄥ", code = '/'.code),
                KeyDef("⌫", widthPct = 12f, isSpecial = true, action = KeyAction.BACKSPACE),
                KeyDef(" ", widthPct = 28f, code = ' '.code, action = KeyAction.SPACE),
                KeyDef("↵", widthPct = 14f, isSpecial = true, action = KeyAction.ENTER),
                KeyDef("▼", widthPct = 12f, isSpecial = true, action = KeyAction.DISMISS),
            ))
        )
    ),

    Ascii(rows = createAsciiRows(shifted = false));

    val allKeys: List<KeyDef>
        get() = rows.flatMap { row -> row.keys }

    val rowCount: Int get() = rows.size

    companion object {
        fun asciiRows(shifted: Boolean): List<KeyboardRow> = createAsciiRows(shifted)
    }
}
