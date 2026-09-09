package com.example.androidkeyboard.engines.android

import android.util.Log
import com.example.androidkeyboard.engines.core.ChewingEngine
import com.example.androidkeyboard.engines.core.ChewingEngine.Layout
import com.example.androidkeyboard.engines.core.EngineUpdate

/** Android libchewing adapter. JNI calls stay behind this boundary. */
class AndroidChewingEngine(
    private val systemDataPath: String,
    private val userDataPath: String,
) : ChewingEngine {

    private var nativeCtx: Long = 0L
    private var nativeLibraryLoaded = false
    private var preedit = ""
    private var candidates = emptyList<String>()
    private var ready = false
    private var candidatePage = 0
    private var candidatePageSize = 0

    override val isReady: Boolean get() = ready

    override val personalizedLearningEnabled: Boolean
        get() = nativeCtx != 0L && chewing_get_auto_learn(nativeCtx) == AUTOLEARN_ENABLED

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
        if (!nativeLibraryLoaded) {
            clearCachedState()
            ready = false
            return
        }

        closeNativeContext()
        nativeCtx = try {
            chewing_new2(systemDataPath, userDataPath)
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "libchewing JNI surface is incomplete", error)
            0L
        }

        if (nativeCtx == 0L) {
            Log.e(TAG, "libchewing failed to initialize with app-private dictionary paths")
            clearCachedState()
            ready = false
            return
        }

        // Dachen is the only decoder layout currently shipped, so the native
        // decoder and rendered Chinese keyboard cannot drift apart.
        chewing_set_kb_type(nativeCtx, KB_DEFAULT)
        chewing_set_chi_eng_mode(nativeCtx, CHINESE_MODE)
        chewing_set_shape_mode(nativeCtx, HALFSHAPE_MODE)
        clearCachedState()
        ready = true
    }

    override fun setPersonalizedLearningEnabled(enabled: Boolean) {
        if (nativeCtx == 0L) return
        chewing_set_auto_learn(
            nativeCtx,
            if (enabled) AUTOLEARN_ENABLED else AUTOLEARN_DISABLED,
        )
    }

    override fun reset() {
        if (nativeCtx != 0L) chewing_reset(nativeCtx)
        clearCachedState()
    }

    override fun handleKeyUpdate(keyCode: Int): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val result = chewing_handle_default(nativeCtx, keyCode)
        candidatePage = 0
        return snapshot(consumed = !isIgnored(result))
    }

    override fun backspaceUpdate(): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val hadComposition = chewing_buffer_check(nativeCtx) != 0 || preedit.isNotEmpty()
        if (!hadComposition) return snapshot(consumed = false)
        val result = chewing_handle_backspace(nativeCtx)
        candidatePage = 0
        return snapshot(consumed = !isIgnored(result) || hadComposition)
    }

    override fun selectCandidateUpdate(index: Int): EngineUpdate {
        if (!isReady || nativeCtx == 0L || index < 0) return emptyUpdate(false)
        val pageSize = candidatePageSize.takeIf { it > 0 }
            ?: chewing_cand_choice_per_page(nativeCtx).coerceAtLeast(1)
        val globalIndex = (candidatePage * pageSize) + index
        val result = chewing_cand_choose_by_index(nativeCtx, globalIndex)
        return snapshot(consumed = result == 0)
    }

    override fun nextPageUpdate(): EngineUpdate? {
        if (!isReady || nativeCtx == 0L) return null
        val totalPages = chewing_cand_total_page(nativeCtx)
        if (candidatePage >= totalPages - 1) return null
        candidatePage++
        return snapshot(consumed = true)
    }

    override fun prevPageUpdate(): EngineUpdate? {
        if (!isReady || nativeCtx == 0L || candidatePage <= 0) return null
        candidatePage--
        return snapshot(consumed = true)
    }

    override fun commitUpdate(): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val result = chewing_commit_preedit_buf(nativeCtx)
        candidatePage = 0
        return snapshot(consumed = result == 0)
    }

    override fun close() {
        closeNativeContext()
        ready = false
        clearCachedState()
    }

    private fun closeNativeContext() {
        if (nativeCtx != 0L) {
            chewing_delete(nativeCtx)
            nativeCtx = 0L
        }
    }

    private fun snapshot(consumed: Boolean): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(consumed)

        val committed = if (chewing_commit_check(nativeCtx) != 0) {
            chewing_commit_string_static(nativeCtx).orEmpty()
        } else {
            ""
        }

        preedit = chewing_buffer_string_static(nativeCtx).orEmpty()
        candidates = buildCandidates()
        return EngineUpdate(
            consumed = consumed,
            preedit = preedit,
            candidates = candidates,
            committedText = committed,
        )
    }

    private fun buildCandidates(): List<String> {
        if (!isReady || nativeCtx == 0L) return emptyList()
        if (chewing_cand_open(nativeCtx) != 0) return emptyList()

        val total = chewing_cand_total_choice(nativeCtx)
        if (total <= 0) {
            candidatePageSize = 0
            return emptyList()
        }

        candidatePageSize = chewing_cand_choice_per_page(nativeCtx).coerceAtLeast(1)
        val start = candidatePage * candidatePageSize
        if (start >= total) return emptyList()
        val end = minOf(start + candidatePageSize, total)
        return (start until end)
            .mapNotNull { index ->
                chewing_cand_string_by_index_static(nativeCtx, index)?.takeIf(String::isNotEmpty)
            }
    }

    private fun clearCachedState() {
        preedit = ""
        candidates = emptyList()
        candidatePage = 0
        candidatePageSize = 0
    }

    private fun emptyUpdate(consumed: Boolean) = EngineUpdate(
        consumed = consumed,
        preedit = "",
        candidates = emptyList(),
        committedText = "",
    )

    companion object {
        private const val TAG = "AndroidChewingEngine"
        private const val KB_DEFAULT = 0
        private const val CHINESE_MODE = 1
        private const val HALFSHAPE_MODE = 0
        // Pinned libchewing a6a8fa4: 0 = enabled, 1 = disabled.
        private const val AUTOLEARN_ENABLED = 0
        private const val AUTOLEARN_DISABLED = 1
        private const val KEYSTROKE_IGNORE = 1

        fun isIgnored(rtn: Int): Boolean = (rtn and KEYSTROKE_IGNORE) != 0
    }

    private external fun chewing_new2(systemDataPath: String, userDataPath: String): Long
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
    private external fun chewing_set_auto_learn(ctx: Long, mode: Int)
    private external fun chewing_get_auto_learn(ctx: Long): Int
}
