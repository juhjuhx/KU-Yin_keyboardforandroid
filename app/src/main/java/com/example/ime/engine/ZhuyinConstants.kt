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
