package com.example.androidkeyboard.engines.android

import android.util.Log
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.ChewingEngine.Layout

/** A single, immutable result of one decoder transition. */
data class EngineUpdate(
    val consumed: Boolean,
    val preedit: String,
    val candidates: List<String>,
    val committedText: String,
)

/** Android libchewing adapter. JNI calls stay behind this boundary. */
class AndroidChewingEngine : ChewingEngine {

    private var nativeCtx: Long = 0L
    private var nativeLibraryLoaded = false
    private var _layout = Layout.DACHEN
    private var _preedit = ""
    private var _candidates = emptyList<String>()
    private var _ready = false
    private var _candPage = 0
    private var _candidatePageSize = 0
    private var _chiEngMode = CHINESE_MODE
    private var _fullHalfMode = HALFSHAPE_MODE

    override val isReady: Boolean get() = _ready

    init {
        nativeLibraryLoaded = try {
            System.loadLibrary("chewing-jni")
            true
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "Unable to load chewing-jni", error)
            false
        }
    }

    override fun init(layout: Layout) {
        _layout = layout
        if (!nativeLibraryLoaded) {
            clearCachedState()
            _ready = false
            return
        }

        nativeCtx = try {
            if (nativeCtx == 0L) chewing_new() else nativeCtx.also { chewing_reset(it) }
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "libchewing JNI surface is incomplete", error)
            0L
        }

        if (nativeCtx == 0L) {
            clearCachedState()
            _ready = false
            return
        }

        val kbType = when (layout) {
            Layout.DACHEN -> KB_DEFAULT
            Layout.HSU -> KB_HSU
            Layout.Eten26 -> KB_ET26
        }
        chewing_set_kb_type(nativeCtx, kbType)
        _chiEngMode = CHINESE_MODE
        _fullHalfMode = HALFSHAPE_MODE
        chewing_set_chi_eng_mode(nativeCtx, _chiEngMode)
        chewing_set_shape_mode(nativeCtx, _fullHalfMode)
        clearCachedState()
        _ready = true
    }

    override fun reset() {
        if (nativeCtx != 0L) chewing_reset(nativeCtx)
        clearCachedState()
    }

    fun handleKeyUpdate(keyCode: Int): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val result = chewing_handle_default(nativeCtx, keyCode)
        _candPage = 0
        return snapshot(consumed = !isIgnored(result))
    }

    override fun handleKeyEvent(keyCode: Int): Boolean = handleKeyUpdate(keyCode).consumed

    fun backspaceUpdate(): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val hadComposition = chewing_buffer_check(nativeCtx) != 0 || _preedit.isNotEmpty()
        if (!hadComposition) return snapshot(consumed = false)
        val result = chewing_handle_backspace(nativeCtx)
        _candPage = 0
        return snapshot(consumed = !isIgnored(result) || hadComposition)
    }

    override fun backspace(): Boolean = backspaceUpdate().consumed

    fun selectCandidateUpdate(index: Int): EngineUpdate {
        if (!isReady || nativeCtx == 0L || index < 0) return emptyUpdate(false)
        val pageSize = _candidatePageSize.takeIf { it > 0 } ?: chewing_cand_choice_per_page(nativeCtx).coerceAtLeast(1)
        val globalIndex = (_candPage * pageSize) + index
        val result = chewing_cand_choose_by_index(nativeCtx, globalIndex)
        return snapshot(consumed = result == 0)
    }

    override fun selectCandidate(index: Int) {
        selectCandidateUpdate(index)
    }

    fun nextPageUpdate(): EngineUpdate? {
        if (!isReady || nativeCtx == 0L) return null
        val totalPages = chewing_cand_total_page(nativeCtx)
        if (_candPage >= totalPages - 1) return null
        _candPage++
        return snapshot(consumed = true)
    }

    override fun nextPage(): Boolean = nextPageUpdate() != null

    fun prevPageUpdate(): EngineUpdate? {
        if (!isReady || nativeCtx == 0L || _candPage <= 0) return null
        _candPage--
        return snapshot(consumed = true)
    }

    override fun prevPage(): Boolean = prevPageUpdate() != null

    fun commitUpdate(): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val result = chewing_commit_preedit_buf(nativeCtx)
        _candPage = 0
        return snapshot(consumed = result == 0)
    }

    override fun commit() {
        commitUpdate()
    }

    override fun getPreedit(): String = _preedit

    override fun getCandidates(): List<String> = _candidates

    override fun toggleFullHalf(): Boolean {
        if (!isReady || nativeCtx == 0L) return false
        _fullHalfMode = if (_fullHalfMode == FULLSHAPE_MODE) HALFSHAPE_MODE else FULLSHAPE_MODE
        chewing_set_shape_mode(nativeCtx, _fullHalfMode)
        return true
    }

    override fun toggleChiEng(): Boolean {
        if (!isReady || nativeCtx == 0L) return false
        _chiEngMode = if (_chiEngMode == CHINESE_MODE) SYMBOL_MODE else CHINESE_MODE
        chewing_set_chi_eng_mode(nativeCtx, _chiEngMode)
        return true
    }

    override fun loadUserDict(path: String): Boolean = isReady && nativeCtx != 0L

    override fun saveUserDict(path: String) = Unit

    fun close() {
        if (nativeCtx != 0L) {
            chewing_delete(nativeCtx)
            nativeCtx = 0L
        }
        _ready = false
        clearCachedState()
    }

    private fun snapshot(consumed: Boolean): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(consumed)

        val committed = if (chewing_commit_check(nativeCtx) != 0) {
            chewing_commit_string_static(nativeCtx).orEmpty()
        } else {
            ""
        }

        _preedit = chewing_buffer_string_static(nativeCtx).orEmpty()
        _candidates = buildCandidates()
        return EngineUpdate(
            consumed = consumed,
            preedit = _preedit,
            candidates = _candidates,
            committedText = committed,
        )
    }

    private fun buildCandidates(): List<String> {
        if (!isReady || nativeCtx == 0L) return emptyList()
        if (chewing_cand_open(nativeCtx) != 0) return emptyList()

        val total = chewing_cand_total_choice(nativeCtx)
        if (total <= 0) {
            _candidatePageSize = 0
            return emptyList()
        }

        _candidatePageSize = chewing_cand_choice_per_page(nativeCtx).coerceAtLeast(1)
        val start = _candPage * _candidatePageSize
        if (start >= total) return emptyList()
        val end = minOf(start + _candidatePageSize, total)
        return (start until end)
            .mapNotNull { index -> chewing_cand_string_by_index_static(nativeCtx, index)?.takeIf(String::isNotEmpty) }
    }

    private fun clearCachedState() {
        _preedit = ""
        _candidates = emptyList()
        _candPage = 0
        _candidatePageSize = 0
    }

    private fun emptyUpdate(consumed: Boolean) = EngineUpdate(
        consumed = consumed,
        preedit = "",
        candidates = emptyList(),
        committedText = "",
    )

    companion object {
        private const val TAG = "AndroidChewingEngine"

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

        fun isIgnored(rtn: Int): Boolean = (rtn and KEYSTROKE_IGNORE) != 0
        fun isCommitted(rtn: Int): Boolean = (rtn and KEYSTROKE_COMMIT) != 0
    }

    private external fun chewing_new(): Long
    private external fun chewing_delete(ctx: Long)
    private external fun chewing_reset(ctx: Long): Int
    private external fun chewing_handle_default(ctx: Long, key: Int): Int
    private external fun chewing_handle_backspace(ctx: Long): Int
    private external fun chewing_buffer_string_static(ctx: Long): String?
    private external fun chewing_buffer_check(ctx: Long): Int
    private external fun chewing_cand_open(ctx: Long): Int
    private external fun chewing_cand_total_choice(ctx: Long): Int
    private external fun chewing_cand_total_page(ctx: Long): Int
    private external fun chewing_cand_choice_per_page(ctx: Long): Int
    private external fun chewing_cand_string_by_index_static(ctx: Long, index: Int): String?
    private external fun chewing_cand_choose_by_index(ctx: Long, index: Int): Int
    private external fun chewing_commit_preedit_buf(ctx: Long): Int
    private external fun chewing_commit_string_static(ctx: Long): String?
    private external fun chewing_commit_check(ctx: Long): Int
    private external fun chewing_set_chi_eng_mode(ctx: Long, mode: Int)
    private external fun chewing_set_shape_mode(ctx: Long, mode: Int)
    private external fun chewing_set_kb_type(ctx: Long, kbtype: Int): Int
}
