package com.example.ime.engine

object ZhuyinConstants {
    // 37 注音符號
    // 聲母 (21)
    val INITIALS = listOf(
        "ㄅ", "ㄆ", "ㄇ", "ㄈ", "ㄉ", "ㄊ", "ㄋ", "ㄌ",
        "ㄍ", "ㄎ", "ㄏ", "ㄐ", "ㄑ", "ㄒ", "ㄓ", "ㄔ",
        "ㄕ", "ㄖ", "ㄗ", "ㄘ", "ㄙ"
    )

    // 介音 (3)
    val MEDIALS = listOf("ㄧ", "ㄨ", "ㄩ")

    // 韻母 (13)
    val RHYMES = listOf(
        "ㄚ", "ㄛ", "ㄜ", "ㄝ", "ㄞ", "ㄟ", "ㄠ", "ㄡ",
        "ㄢ", "ㄣ", "ㄤ", "ㄥ", "ㄦ"
    )

    // 聲調 (5)
    const val TONE_1 = ""      // 一聲 (陰平，預設或空白)
    const val TONE_2 = "ˊ"     // 二聲 (陽平)
    const val TONE_3 = "ˇ"     // 三聲 (上聲)
    const val TONE_4 = "ˋ"     // 四聲 (去聲)
    const val TONE_5 = "˙"     // 輕聲

    val TONES = listOf(TONE_2, TONE_3, TONE_4, TONE_5)

    fun isInitial(s: String): Boolean = INITIALS.contains(s)
    fun isMedial(s: String): Boolean = MEDIALS.contains(s)
    fun isRhyme(s: String): Boolean = RHYMES.contains(s)
    fun isTone(s: String): Boolean = s in listOf("ˊ", "ˇ", "ˋ", "˙")

    // 標準大千式鍵盤配列 (DaQian Layout)
    // 第一排 (對應 1 2 3 4 5 6 7 8 9 0 -)
    val ROW_1 = listOf(
        KeyDef("ㄅ", "1"),
        KeyDef("ㄉ", "2"),
        KeyDef("ˇ", "3"),
        KeyDef("ˋ", "4"),
        KeyDef("ㄓ", "5"),
        KeyDef("ˊ", "6"),
        KeyDef("˙", "7"),
        KeyDef("ㄚ", "8"),
        KeyDef("ㄞ", "9"),
        KeyDef("ㄢ", "0"),
        KeyDef("ㄦ", "-")
    )

    // 第二排 (對應 q w e r t y u i o p)
    val ROW_2 = listOf(
        KeyDef("ㄆ", "q"),
        KeyDef("ㄊ", "w"),
        KeyDef("ㄍ", "e"),
        KeyDef("ㄐ", "r"),
        KeyDef("ㄔ", "t"),
        KeyDef("ㄗ", "y"),
        KeyDef("ㄧ", "u"),
        KeyDef("ㄛ", "i"),
        KeyDef("ㄟ", "o"),
        KeyDef("ㄣ", "p")
    )

    // 第三排 (對應 a s d f g h j k l ;)
    val ROW_3 = listOf(
        KeyDef("ㄇ", "a"),
        KeyDef("ㄋ", "s"),
        KeyDef("ㄎ", "d"),
        KeyDef("ㄑ", "f"),
        KeyDef("ㄒ", "g"),
        KeyDef("ㄘ", "h"),
        KeyDef("ㄨ", "j"),
        KeyDef("ㄜ", "k"),
        KeyDef("ㄠ", "l"),
        KeyDef("ㄤ", ";")
    )

    // 第四排 (對應 z x c v b n m ,)
    val ROW_4 = listOf(
        KeyDef("ㄈ", "z"),
        KeyDef("ㄌ", "x"),
        KeyDef("ㄏ", "c"),
        KeyDef("ㄙ", "v"),
        KeyDef("ㄩ", "b"),
        KeyDef("ㄝ", "n"),
        KeyDef("ㄡ", "m"),
        KeyDef("ㄥ", ",")
    )

    // 常用中文標點符號
    val CHINESE_PUNCTUATION_1 = listOf(
        "，", "。", "！", "？", "：", "；", "、", "～"
    )

    val CHINESE_PUNCTUATION_2 = listOf(
        "「", "」", "『", "』", "（", "）", "【", "】",
        "《", "》", "…", "—", "、", "／", "％", "＠"
    )

    // 常用表情符號
    val EMOJI_LIST = listOf(
        "😊", "😂", "🤣", "😍", "🥰", "😘", "😎", "🥳",
        "👍", "👏", "🙏", "❤️", "🔥", "✨", "🎉", "💯",
        "🤔", "😅", "😭", "🥺", "😳", "😴", "💪", "🙌",
        "🤩", "😋", "🤗", "👋", "👌", "✌️", "💖", "🌟",
        "☕", "🍻", "🍔", "🍕", "🍜", "🍰", "🍎", "🌸"
    )
}

data class KeyDef(
    val main: String,
    val sub: String = ""
)
