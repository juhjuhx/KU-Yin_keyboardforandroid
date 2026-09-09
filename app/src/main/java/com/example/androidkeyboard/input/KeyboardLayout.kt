package com.example.androidkeyboard.input

/**
 * libchewing 標準布局（Dachen/大千）鍵盤布局
 *
 * 對齊 pinned libchewing
 * src/editor/zhuyin_layout/standard.rs (3c4a93aa03d574c7f011ff84e8a2437c2f79b2cf)。
 *
 * Row 1 (數字行): 1=ㄅ 2=ㄉ 3=ˇ 4=ˋ 5=ㄓ 6=ˊ 7=˙ 8=ㄚ 9=ㄞ 0=ㄢ -=ㄦ
 * Row 2 (Q-P): Q=ㄆ W=ㄊ E=ㄍ R=ㄐ T=ㄔ Y=ㄗ U=ㄧ I=ㄛ O=ㄟ P=ㄣ
 * Row 3 (A-L): A=ㄇ S=ㄋ D=ㄎ F=ㄑ G=ㄕ H=ㄘ J=ㄨ K=ㄜ L=ㄠ ;=ㄤ
 * Row 4 (Z-M): Z=ㄈ X=ㄌ C=ㄏ V=ㄒ B=ㄖ N=ㄙ M=ㄩ
 * Row 5 (符號): ,=ㄝ .=ㄡ /=ㄥ 空白=第一聲/確認 退格=刪除 收起鍵盤=▼
 */
data class KeyDef(
    val label: String,
    val widthPct: Float = 10f,
    val isSpecial: Boolean = false,
    val code: Int = -1,
)

data class KeyboardRow(val keys: List<KeyDef>)

enum class KeyboardLayout(val rows: List<KeyboardRow>) {

    Dachen(
        rows = listOf(
            KeyboardRow(listOf(
                KeyDef("ㄅ", code = 49),
                KeyDef("ㄉ", code = 50),
                KeyDef("ˇ", code = 51),
                KeyDef("ˋ", code = 52),
                KeyDef("ㄓ", code = 53),
                KeyDef("ˊ", code = 54),
                KeyDef("˙", code = 55),
                KeyDef("ㄚ", code = 56),
                KeyDef("ㄞ", code = 57),
                KeyDef("ㄢ", code = 48),
                KeyDef("ㄦ", code = 45),
            )),
            KeyboardRow(listOf(
                KeyDef("ㄆ", code = 113),
                KeyDef("ㄊ", code = 119),
                KeyDef("ㄍ", code = 101),
                KeyDef("ㄐ", code = 114),
                KeyDef("ㄔ", code = 116),
                KeyDef("ㄗ", code = 121),
                KeyDef("ㄧ", code = 117),
                KeyDef("ㄛ", code = 105),
                KeyDef("ㄟ", code = 111),
                KeyDef("ㄣ", code = 112),
            )),
            KeyboardRow(listOf(
                KeyDef("ㄇ", code = 97),
                KeyDef("ㄋ", code = 115),
                KeyDef("ㄎ", code = 100),
                KeyDef("ㄑ", code = 102),
                KeyDef("ㄕ", code = 103),
                KeyDef("ㄘ", code = 104),
                KeyDef("ㄨ", code = 106),
                KeyDef("ㄜ", code = 107),
                KeyDef("ㄠ", code = 108),
                KeyDef("ㄤ", code = 59),
            )),
            KeyboardRow(listOf(
                KeyDef("ㄈ", code = 122),
                KeyDef("ㄌ", code = 120),
                KeyDef("ㄏ", code = 99),
                KeyDef("ㄒ", code = 118),
                KeyDef("ㄖ", code = 98),
                KeyDef("ㄙ", code = 110),
                KeyDef("ㄩ", code = 109),
            )),
            KeyboardRow(listOf(
                KeyDef("ㄝ", code = 44),
                KeyDef("ㄡ", code = 46),
                KeyDef("ㄥ", code = 47),
                KeyDef("⌫", isSpecial = true),
                KeyDef(" ", code = 32),
                KeyDef("▼", isSpecial = true),
            ))
        )
    );

    val allKeys: List<KeyDef>
        get() = rows.flatMap { row -> row.keys }

    val rowCount: Int get() = rows.size
}
