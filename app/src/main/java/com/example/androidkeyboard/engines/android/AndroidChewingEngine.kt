package com.example.androidkeyboard.engines.android

import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.ChewingEngine.Layout
import android.util.Log

/**
 * Wave 4 T21: Android ChewingEngine implementation with libchewing JNI bindings.
 *
 * Uses libchewing 0.12.x (Rust reimplementation) via CAPI.
 * JNI functions match the libchewing C API naming.
 */
class AndroidChewingEngine : ChewingEngine {

    private var nativeCtx: Long = 0L
    private var _layout = Layout.DACHEN
    private var _preedit = ""
    private var _candidates = emptyList<String>()
    private var _ready = false
    private var _candPage = 0
    private var _chiEngMode = CHINESE_MODE
    private var _fullHalfMode = HALFSHAPE_MODE

    override val isReady: Boolean get() = _ready
    private val TAG = "AndroidChewingEngine"

    init { loadNativeLibrary() }

    private fun loadNativeLibrary() {
        try {
            System.loadLibrary("chewing-jni")
            Log.d(TAG, "libchewing-jni loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "libchewing-jni not loaded: ${e.message}. Running in stub mode.")
        }
    }

    override fun init(layout: Layout) {
        _layout = layout
        if (nativeCtx == 0L) {
            nativeCtx = chewing_new()
        } else {
            chewing_reset(nativeCtx)
        }
        if (nativeCtx != 0L) {
            val kbType = when (layout) {
                Layout.DACHEN -> KB_DEFAULT
                Layout.HSU -> KB_HSU
                Layout.Eten26 -> KB_ET26
                else -> KB_DEFAULT
            }
            chewing_set_kb_type(nativeCtx, kbType)
            _ready = true
        }
        _preedit = ""
        _candidates = emptyList()
        _candPage = 0
        _chiEngMode = CHINESE_MODE
        _fullHalfMode = HALFSHAPE_MODE
    }

    override fun reset() {
        if (nativeCtx != 0L) {
            chewing_reset(nativeCtx)
        }
        _ready = false
        _preedit = ""
        _candidates = emptyList()
        _candPage = 0
    }

    override fun handleKeyEvent(keyCode: Int): Boolean {
        if (nativeCtx == 0L) return false
        val result = chewing_handle_default(nativeCtx, keyCode)
        _preedit = getPreedit()
        _candidates = buildCandidates()
        _candPage = 0
        return !isIgnored(result)
    }

    override fun getPreedit(): String {
        if (nativeCtx == 0L) return _preedit
        val s = chewing_buffer_string_static(nativeCtx)
        _preedit = s
        return s
    }

    override fun getCandidates(): List<String> = _candidates

    override fun selectCandidate(index: Int) {
        if (nativeCtx == 0L) return
        chewing_cand_choose_by_index(nativeCtx, index)
        _preedit = getPreedit()
        _candidates = buildCandidates()
    }

    override fun commit() {
        if (nativeCtx == 0L) {
            _preedit = ""
            _candidates = emptyList()
            return
        }
        chewing_commit_preedit_buf(nativeCtx)
        _preedit = ""
        _candidates = emptyList()
        _candPage = 0
    }

    override fun backspace(): Boolean {
        if (nativeCtx == 0L) return false
        chewing_handle_backspace(nativeCtx)
        _preedit = getPreedit()
        _candidates = buildCandidates()
        return _preedit.isNotEmpty() || chewing_buffer_check(nativeCtx) != 0
    }

    override fun toggleFullHalf(): Boolean {
        if (nativeCtx == 0L) return false
        _fullHalfMode = if (_fullHalfMode == FULLSHAPE_MODE) SYMBOL_MODE else FULLSHAPE_MODE
        chewing_set_shape_mode(nativeCtx, _fullHalfMode)
        return true
    }

    override fun toggleChiEng(): Boolean {
        if (nativeCtx == 0L) return false
        _chiEngMode = if (_chiEngMode == CHINESE_MODE) SYMBOL_MODE else CHINESE_MODE
        chewing_set_chi_eng_mode(nativeCtx, _chiEngMode)
        return true
    }

    override fun loadUserDict(path: String): Boolean {
        if (nativeCtx == 0L) return false
        // TODO: implement proper loading from file
        return true
    }

    override fun saveUserDict(path: String) {
        // TODO: implement saving to file
    }

    private fun buildCandidates(): List<String> {
        if (nativeCtx == 0L) return emptyList()
        // Ensure the candidate path is open before reading choices.
        chewing_cand_open(nativeCtx)
        val total = chewing_cand_total_choice(nativeCtx)
        if (total <= 0) return emptyList()
        val pageSize = chewing_cand_choice_per_page(nativeCtx)
        val start = _candPage * pageSize
        return (start until minOf(start + pageSize, total))
            .map { i -> chewing_cand_string_by_index_static(nativeCtx, i) ?: "" }
            .filter { it.isNotEmpty() }
    }

    override fun nextPage(): Boolean {
        if (nativeCtx == 0L) return false
        val totalPages = chewing_cand_total_page(nativeCtx)
        if (_candPage < totalPages - 1) {
            _candPage++
            _candidates = buildCandidates()
            return true
        }
        return false
    }

    override fun prevPage(): Boolean {
        if (nativeCtx == 0L) return false
        if (_candPage > 0) {
            _candPage--
            _candidates = buildCandidates()
            return true
        }
        return false
    }

    private fun releaseNative() {
        if (nativeCtx != 0L) {
            chewing_delete(nativeCtx)
            nativeCtx = 0L
        }
    }

    companion object {
        // libchewing constants
        const val KB_DEFAULT = 0
        const val KB_HSU = 1
        const val KB_ET26 = 5
        const val CHINESE_MODE = 1
        const val SYMBOL_MODE = 0
        const val FULLSHAPE_MODE = 1
        const val HALFSHAPE_MODE = 0
        const val KEYSTROKE_IGNORE = 1
        const val KEYSTROKE_COMMIT = 2
        const val KEYSTROKE_BELL = 4

        // Bitmask predicates mirroring native chewing_keystroke_CheckIgnore /
        // chewing_commit_Check. ignore iff (rtn & 1) != 0; committed iff (rtn & 2) != 0.
        fun isIgnored(rtn: Int): Boolean = (rtn and KEYSTROKE_IGNORE) != 0
        fun isCommitted(rtn: Int): Boolean = (rtn and KEYSTROKE_COMMIT) != 0
    }

    // JNI native declarations - matching chewing_jni.cpp
    private external fun chewing_new(): Long
    private external fun chewing_delete(ctx: Long)
    private external fun chewing_reset(ctx: Long): Int
    private external fun chewing_init(ctx: Long, dataPath: String, hashPath: String): Int
    private external fun chewing_terminate()
    private external fun chewing_handle_default(ctx: Long, key: Int): Int
    private external fun chewing_handle_backspace(ctx: Long): Int
    private external fun chewing_handle_space(ctx: Long): Int
    private external fun chewing_buffer_string_static(ctx: Long): String
    private external fun chewing_buffer_check(ctx: Long): Int
    private external fun chewing_buffer_len(ctx: Long): Int
    private external fun chewing_cursor_current(ctx: Long): Int
    private external fun chewing_cand_open(ctx: Long): Int
    private external fun chewing_cand_close(ctx: Long): Int
    private external fun chewing_cand_total_choice(ctx: Long): Int
    private external fun chewing_cand_total_page(ctx: Long): Int
    private external fun chewing_cand_current_page(ctx: Long): Int
    private external fun chewing_cand_choice_per_page(ctx: Long): Int
    private external fun chewing_cand_string_by_index_static(ctx: Long, index: Int): String?
    private external fun chewing_cand_choose_by_index(ctx: Long, index: Int): Int
    private external fun chewing_commit_preedit_buf(ctx: Long): Int
    private external fun chewing_commit_string_static(ctx: Long): String
    private external fun chewing_commit_check(ctx: Long): Int
    private external fun chewing_set_chi_eng_mode(ctx: Long, mode: Int)
    private external fun chewing_get_chi_eng_mode(ctx: Long): Int
    private external fun chewing_set_shape_mode(ctx: Long, mode: Int)
    private external fun chewing_get_shape_mode(ctx: Long): Int
    private external fun chewing_set_kb_type(ctx: Long, kbtype: Int): Int
    private external fun chewing_get_kb_type(ctx: Long): Int
    private external fun chewing_kb_str2num(kbStr: String): Int
    private external fun chewing_userphrase_add(ctx: Long, phrase: String, bopomofo: String): Int
    private external fun chewing_userphrase_lookup(ctx: Long, phrase: String, bopomofo: String): Int
}
