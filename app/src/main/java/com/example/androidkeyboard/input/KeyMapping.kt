package com.example.androidkeyboard.input

/**
 * T18: libchewing Dachen 佈局 KeyEvent → Bopomofo 字元對照表
 *
 * Android KeyEvent 發出 → libchewing 解碼 → 候選字排序
 * 本表用於 UI 標籤顯示 (label) 與事件發送 (keyCode) 的雙向對照
 *
 * 佈局來源: libchewing src/data/kb-dachen.c (KB_DEFAULT)
 * Android KeyEvent 代碼參照: android.view.KeyEvent
 */
object KeyMapping {

    /**
     * Dachen 佈局：Bopomofo 字母行 + 符號行
     *
     * Row 1 (ㄍ-ㄬ): 基本聲母/韻母
     * Row 2 (ㄭ-ˇˊˋ): 聲調 + 數字 + 修飾
     * Row 3 (ㄧ-鿯): 更多韻母 + 標點
     * Row 4 (ㄫ-!): 特殊鍵 + 符號
     */
    val dachenMap: Map<String, Int> = mapOf(
        // Row 1: 基本注音 (對應 QWERTY A-J)
        "ㄅ" to android.view.KeyEvent.KEYCODE_A, // ㄅ
        "ㄆ" to android.view.KeyEvent.KEYCODE_S, // ㄆ
        "ㄇ" to android.view.KeyEvent.KEYCODE_D, // ㄇ
        "ㄈ" to android.view.KeyEvent.KEYCODE_F, // ㄈ
        "ㄉ" to android.view.KeyEvent.KEYCODE_G, // ㄉ
        "ㄊ" to android.view.KeyEvent.KEYCODE_H, // ㄊ
        "ㄋ" to android.view.KeyEvent.KEYCODE_J, // ㄋ
        "ㄌ" to android.view.KeyEvent.KEYCODE_K, // ㄌ
        "ㄍ" to android.view.KeyEvent.KEYCODE_L, // ㄒ (註: libchewing Dachen 此鍵為 ㄒ)
        "ㄎ" to android.view.KeyEvent.KEYCODE_SEMICOLON, // ㄓ

        // Row 2: 聲調 + 數字
        "ㄏ" to android.view.KeyEvent.KEYCODE_Z,   // ㄐ
        "ㄐ" to android.view.KeyEvent.KEYCODE_X,   // ㄑ
        "ㄑ" to android.view.KeyEvent.KEYCODE_C,   // ㄒ
        "ㄒ" to android.view.KeyEvent.KEYCODE_V,   // ㄓ
        "ㄓ" to android.view.KeyEvent.KEYCODE_1,   // 第聲 (無聲調)
        "ㄔ" to android.view.KeyEvent.KEYCODE_2,   // 第聲
        "ㄕ" to android.view.KeyEvent.KEYCODE_3,   // 第聲
        "ㄖ" to android.view.KeyEvent.KEYCODE_4,   // 第聲
        "ㄗ" to android.view.KeyEvent.KEYCODE_5,   // 第聲 / 數字 5
        "ㄘ" to android.view.KeyEvent.KEYCODE_6,   // 數字 6

        // Row 3: 更多韻母 + 標點
        "ㄙ" to android.view.KeyEvent.KEYCODE_W,   // ㄗ
        "ㄚ" to android.view.KeyEvent.KEYCODE_E,   // ㄘ
        "ㄛ" to android.view.KeyEvent.KEYCODE_R,   // ㄙ
        "ㄜ" to android.view.KeyEvent.KEYCODE_T,   // ㄚ
        "ㄝ" to android.view.KeyEvent.KEYCODE_Y,   // ㄛ
        "ㄞ" to android.view.KeyEvent.KEYCODE_U,   // ㄜ
        "ㄟ" to android.view.KeyEvent.KEYCODE_I,   // ㄝ
        "ㄠ" to android.view.KeyEvent.KEYCODE_O,   // ㄞ
        "ㄡ" to android.view.KeyEvent.KEYCODE_P,   // ㄟ
        "ㄢ" to android.view.KeyEvent.KEYCODE_MINUS, // ㄠ

        // Row 4: 特殊鍵
        "ㄣ" to android.view.KeyEvent.KEYCODE_Q,   // ㄡ
        "ㄤ" to android.view.KeyEvent.KEYCODE_7,   // 數字 7
        "ㄥ" to android.view.KeyEvent.KEYCODE_8,   // 數字 8
        "-" to android.view.KeyEvent.KEYCODE_9,        // 減號 → 數字 9 (shift)
        " " to android.view.KeyEvent.KEYCODE_SPACE,    // 空格
        "|" to android.view.KeyEvent.KEYCODE_0,        // 直線 → 數字 0
        "、" to android.view.KeyEvent.KEYCODE_COMMA,   // 、 (頓號)
        "。" to android.view.KeyEvent.KEYCODE_PERIOD,  // 。 (句號)
        "?" to android.view.KeyEvent.KEYCODE_QUESTION,   // ?
        "!" to android.view.KeyEvent.KEYCODE_EXCLAMATION,  // !
    )

    /**
     * 反向映射：KeyEvent → Bopomofo 字元 (用於 UI 顯示預覽)
     */
    val keyCodeToLabel: Map<Int, String> = dachenMap.entries.map { it.value to it.key }.toMap()

    /**
     * 將 Bopomofo 字元轉換為對應的 Android KeyEvent
     * 若找不到對應鍵，返回 -1 (表示非字母鍵，需特殊處理)
     */
    fun getKeyEventForChar(label: String): Int {
        return dachenMap[label] ?: -1
    }

    /**
     * 檢查字元是否為「操作鍵」(非注音字符)
     * 操作鍵直接提交，不走 libchewing 解碼
     */
    fun isSpecialKey(label: String): Boolean {
        return when (label) {
            " " -> true
            "back" -> true
            "|" -> true
            "-" -> true
            "?" -> true
            "!" -> true
            "、" -> true
            "。" -> true
            else -> false
        }
    }
}
