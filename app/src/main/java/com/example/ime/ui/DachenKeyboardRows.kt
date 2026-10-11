package com.example.ime.ui

import com.example.androidkeyboard.input.KeyAction
import com.example.androidkeyboard.input.KeyboardLayout
import com.example.ime.engine.KeyDef

/**
 * Compose 的四排大千鍵位，符號與 ASCII 提示皆衍生自 [KeyboardLayout.Dachen]。
 * 前三排沿用 canonical 排序；最後兩排的 INPUT 鍵合併為第四排。
 * 退格、空白、Enter 與切換鍵仍由 UI 的專用 callback 處理。
 */
object DachenKeyboardRows {
    private val inputRows = KeyboardLayout.Dachen.rows.map { row ->
        row.keys.filter { it.action == KeyAction.INPUT }.map { key ->
            KeyDef(main = key.label, sub = key.code.toChar().toString())
        }
    }

    val rows: List<List<KeyDef>> = inputRows.take(3) + listOf(inputRows.drop(3).flatten())
}
