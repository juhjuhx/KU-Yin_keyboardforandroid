package com.example.androidkeyboard.input

/**
 * libchewing 標準布局（Dachen/大千）鍵盤布局
 *
 * 對齊 libchewing src/editor/zhuyin_layout/standard.rs 的映射
 *
 * Row 1 (數字行): 1=ㄅ 2=ㄉ 3=ˇ 4=ˋ 5=ㄓ 6=ˊ 7=˙ 8=ㄚ 9=ㄞ 0=ㄢ -=ㄦ
 * Row 2 (Q-P): Q=ㄆ W=ㄊ E=ㄍ R=ㄐ T=ㄔ Y=ㄗ U=ㄧ I=ㄛ O=ㄟ P=ㄣ
 * Row 3 (A-L): A=ㄇ S=ㄋ D=ㄎ F=ㄑ G=ㄕ H=ㄘ J=ㄨ K=ㄜ L=ㄠ ;=ㄤ
 * Row 4 (Z-M): Z=ㄈ X=ㄉ C=ㄊ V=ㄋ B=ㄌ N=ㄙ M=ㄡ
 * Row 5 (符號): ,=ㄝ /=ㄥ 空白=確認 退格=刪除 收起鍵盤=▼
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
            // Row 1: 數字鍵行 (1-0, -) → ㄅㄉ等聲母/韻母
            KeyboardRow(listOf(
                KeyDef("ㄅ", code = 49),  // KEY_1
                KeyDef("ㄉ", code = 50),  // KEY_2
                KeyDef("ˇ", code = 51),  // KEY_3 (聲調3)
                KeyDef("ˋ", code = 52),  // KEY_4 (聲調4)
                KeyDef("ㄓ", code = 53),  // KEY_5
                KeyDef("ˊ", code = 54),  // KEY_6 (聲調2)
                KeyDef("˙", code = 55),  // KEY_7 (聲調5)
                KeyDef("ㄚ", code = 56),  // KEY_8
                KeyDef("ㄞ", code = 57),  // KEY_9
                KeyDef("ㄢ", code = 48),  // KEY_0
                KeyDef("ㄦ", code = 45),  // KEY_MINUS
            )),
            // Row 2: Q-P → ㄆㄊ等聲母/韻母
            KeyboardRow(listOf(
                KeyDef("ㄆ", code = 113),  // KEY_Q
                KeyDef("ㄊ", code = 119),  // KEY_W
                KeyDef("ㄍ", code = 101),  // KEY_E
                KeyDef("ㄐ", code = 114),  // KEY_R
                KeyDef("ㄔ", code = 116),  // KEY_T
                KeyDef("ㄗ", code = 121),  // KEY_Y
                KeyDef("ㄧ", code = 117),  // KEY_U
                KeyDef("ㄛ", code = 105),  // KEY_I
                KeyDef("ㄟ", code = 111),  // KEY_O
                KeyDef("ㄣ", code = 112),  // KEY_P
            )),
            // Row 3: A-L → ㄇㄋ等聲母/韻母
            KeyboardRow(listOf(
                KeyDef("ㄇ", code = 97),  // KEY_A
                KeyDef("ㄋ", code = 115),  // KEY_S
                KeyDef("ㄎ", code = 100),  // KEY_D
                KeyDef("ㄑ", code = 102),  // KEY_F
                KeyDef("ㄕ", code = 103),  // KEY_G
                KeyDef("ㄘ", code = 104),  // KEY_H
                KeyDef("ㄨ", code = 106),  // KEY_J
                KeyDef("ㄜ", code = 107),  // KEY_K
                KeyDef("ㄠ", code = 108),  // KEY_L
                KeyDef("ㄤ", code = 59),  // KEY_SEMICOLON
            )),
            // Row 4: Z-M → ㄈㄉ等聲母/韻母
            KeyboardRow(listOf(
                KeyDef("ㄈ", code = 122),  // KEY_Z
                KeyDef("ㄉ", code = 120),  // KEY_X
                KeyDef("ㄊ", code = 99),  // KEY_C
                KeyDef("ㄋ", code = 118),  // KEY_V
                KeyDef("ㄌ", code = 98),  // KEY_B
                KeyDef("ㄙ", code = 110),  // KEY_N
                KeyDef("ㄡ", code = 109),  // KEY_M
            )),
            // Row 5: 符號鍵 + 特殊鍵
            KeyboardRow(listOf(
                KeyDef("ㄝ", code = 44),  // KEY_COMMA
                KeyDef("ㄥ", code = 47),  // KEY_SLASH
                KeyDef("⌫", isSpecial = true),  // KEY_BACKSPACE
                KeyDef(" ", code = 65),  // KEY_SPACE (確認/音節分隔)
                KeyDef("▼", isSpecial = true),  // KEY_DISMISS (收起鍵盤)
            ))
        )
    );

    val allKeys: List<KeyDef>
        get() = rows.flatMap { row -> row.keys }

    val rowCount: Int get() = rows.size
}
