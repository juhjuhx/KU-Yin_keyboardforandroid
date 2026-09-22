package com.example.ime.api

import com.example.ime.engine.KeyboardMode
import com.example.ime.engine.ShiftState
import kotlinx.coroutines.flow.StateFlow

/**
 * 酷音輸入法核心模組開放接口 (Open Architecture Interface)
 * 供社群開發者 Fork 本專案時擴充、客製化或替換核心引擎
 */
interface IKuYinEngine {
    val mode: StateFlow<KeyboardMode>
    val shiftState: StateFlow<ShiftState>
    val composingZhuyin: StateFlow<String>
    val candidates: StateFlow<List<String>>

    fun setMode(newMode: KeyboardMode)
    fun switchMode(newMode: KeyboardMode, onCommit: (String) -> Unit)
    fun toggleShift()
    fun onZhuyinKey(zhuyinChar: String, onCommit: (String) -> Unit)
    fun onSpace(onCommit: (String) -> Unit)
    fun selectCandidate(candidate: String, onCommit: (String) -> Unit)
    fun onBackspace(onDeleteSurrounding: () -> Unit)
    fun clearComposing()
    fun onEnglishKey(char: String, onCommit: (String) -> Unit)
}

/**
 * 外部自訂詞庫擴充接口 (Custom Lexicon / Plugin Extension)
 * 允許擴充專業領域詞庫、方言詞彙或社群整理字詞
 */
interface IKuYinLexiconProvider {
    val providerId: String
    val providerName: String
    val lexiconVersion: String
    fun lookup(zhuyinKey: String): List<String>
    fun searchPrefix(prefix: String): List<String>
}
