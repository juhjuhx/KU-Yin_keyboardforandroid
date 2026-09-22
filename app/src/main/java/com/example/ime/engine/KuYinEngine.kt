package com.example.ime.engine

import android.content.Context
import com.example.androidkeyboard.input.EditorAction
import com.example.androidkeyboard.input.EditorPolicy
import com.example.ime.api.IKuYinEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class KeyboardMode {
    ZHUYIN,     // 酷音注音
    ENGLISH,    // 英文 QWERTY
    SYMBOLS,    // 標點符號與數字
    EMOJI       // 表情符號
}

enum class ShiftState {
    OFF,        // 小寫
    ON,         // 單次大寫
    CAPS_LOCK   // 大寫鎖定
}

class KuYinEngine(context: Context) : IKuYinEngine {

    val dictionary = ZhuyinDictionary(context)

    private var policy: EditorPolicy = EditorPolicy(
        isSensitive = false,
        allowPersonalizedLearning = true,
        allowCandidates = true,
        allowComposition = true,
        forceAscii = false,
        action = EditorAction.UNSPECIFIED
    )

    fun applyPolicy(policy: EditorPolicy) {
        this.policy = policy
    }

    // 當前模式
    private val _mode = MutableStateFlow(KeyboardMode.ZHUYIN)
    override val mode: StateFlow<KeyboardMode> = _mode.asStateFlow()

    // 英文 Shift 狀態
    private val _shiftState = MutableStateFlow(ShiftState.OFF)
    override val shiftState: StateFlow<ShiftState> = _shiftState.asStateFlow()

    // 注音拼寫暫存 (例如 "ㄋㄧˇ")
    private val _composingZhuyin = MutableStateFlow("")
    override val composingZhuyin: StateFlow<String> = _composingZhuyin.asStateFlow()

    // 候選字詞列表
    private val _candidates = MutableStateFlow<List<String>>(emptyList())
    override val candidates: StateFlow<List<String>> = _candidates.asStateFlow()

    override fun setMode(newMode: KeyboardMode) {
        if (newMode == KeyboardMode.ZHUYIN && !policy.allowComposition) return
        _mode.value = newMode
        if (newMode != KeyboardMode.ZHUYIN) {
            clearComposing()
        }
    }

    // 切換中英文/符號模式時，若有未上屏之暫存注音或候選字，自動提交上屏
    override fun switchMode(newMode: KeyboardMode, onCommit: (String) -> Unit) {
        if (newMode == KeyboardMode.ZHUYIN && !policy.allowComposition) {
            clearComposing()
            return
        }
        if (_composingZhuyin.value.isNotEmpty()) {
            val commitText = _candidates.value.firstOrNull() ?: _composingZhuyin.value
            onCommit(commitText)
            if (policy.allowPersonalizedLearning) dictionary.recordWordSelection(commitText)
            clearComposing()
        }
        _mode.value = newMode
    }

    override fun toggleShift() {
        _shiftState.value = when (_shiftState.value) {
            ShiftState.OFF -> ShiftState.ON
            ShiftState.ON -> ShiftState.CAPS_LOCK
            ShiftState.CAPS_LOCK -> ShiftState.OFF
        }
    }

    // 處理注音按鍵輸入 (支援新酷音 / RIME 首字母縮寫與長詞連打，如 ㄨㄕㄕ、ㄨㄛㄕㄕㄟ、ㄨㄛˇㄕˋㄕㄟˊ -> 我是誰)
    override fun onZhuyinKey(zhuyinChar: String, onCommit: (String) -> Unit) {
        if (!policy.allowComposition) return
        val current = _composingZhuyin.value

        if (ZhuyinConstants.isTone(zhuyinChar)) {
            // 聲調鍵 (ˊ ˇ ˋ ˙)
            if (current.isEmpty()) {
                // 無拼音時單獨輸入聲調符號
                onCommit(zhuyinChar)
                return
            }
            // 若最後一個已經是聲調，則替換聲調
            if (ZhuyinConstants.isTone(current.takeLast(1))) {
                _composingZhuyin.value = current.dropLast(1) + zhuyinChar
            } else {
                _composingZhuyin.value = current + zhuyinChar
            }
        } else {
            // 聲母、介音、韻母皆持續累積，完美支援 ㄨㄕㄕ、ㄋㄏ、ㄒㄒ、ㄉㄐ 等首字母縮寫連打
            _composingZhuyin.value = current + zhuyinChar
        }

        updateCandidates()
    }

    // 處理候選字選取
    override fun selectCandidate(candidate: String, onCommit: (String) -> Unit) {
        onCommit(candidate)
        if (policy.allowPersonalizedLearning) dictionary.recordWordSelection(candidate)
        clearComposing()
    }

    // 處理空白鍵
    override fun onSpace(onCommit: (String) -> Unit) {
        val current = _composingZhuyin.value
        if (current.isNotEmpty()) {
            val candidateList = _candidates.value
            if (candidateList.isNotEmpty()) {
                // 酷音習慣：有候選字時按 Space 預設送出第一候選字
                selectCandidate(candidateList.first(), onCommit)
            } else {
                // 無候選字時直接送出注音符號
                onCommit(current)
                clearComposing()
            }
        } else {
            // 無暫存注音，直接送出標準空格
            onCommit(" ")
        }
    }

    // 標點提交：有組字時先按空白鍵政策排空，再送標點，避免繞過 state machine 留下 stale composition
    fun commitPunctuation(punct: String, onCommit: (String) -> Unit) {
        if (_composingZhuyin.value.isNotEmpty()) {
            onSpace(onCommit)
        }
        onCommit(punct)
    }

    // 處理刪除鍵 (Backspace)
    override fun onBackspace(onDeleteSurrounding: () -> Unit) {
        val current = _composingZhuyin.value
        if (current.isNotEmpty()) {
            _composingZhuyin.value = current.dropLast(1)
            updateCandidates()
        } else {
            onDeleteSurrounding()
        }
    }

    // 更新候選字
    private fun updateCandidates() {
        val query = _composingZhuyin.value
        if (query.isBlank()) {
            _candidates.value = emptyList()
        } else {
            _candidates.value = dictionary.query(query)
        }
    }

    override fun clearComposing() {
        _composingZhuyin.value = ""
        _candidates.value = emptyList()
    }

    // 英文輸入處理
    override fun onEnglishKey(char: String, onCommit: (String) -> Unit) {
        val text = when (_shiftState.value) {
            ShiftState.OFF -> char.lowercase()
            ShiftState.ON -> {
                _shiftState.value = ShiftState.OFF // 單次大寫後恢復
                char.uppercase()
            }
            ShiftState.CAPS_LOCK -> char.uppercase()
        }
        onCommit(text)
    }
}
