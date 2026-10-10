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
    private var lastTotalChoices = 0

    // Replay contains active decoder transitions. A pending key/backspace
    // inverse pair may be removed only after the live decoder proves equality.
    // Synthetic test traces keep physical events independently of this projection.
    private data class NativeSnapshot(
        val buffer: String, val bopomofo: String, val cursor: Int, val phones: Int,
    )
    private sealed interface NativeAction {
        data class Key(val code: Int, val pendingBefore: NativeSnapshot? = null) : NativeAction
        data object Backspace : NativeAction
        data object FinalizeTail : NativeAction
        data class Select(val identity: CandidateIdentity) : NativeAction
    }
    private data class CandidateIdentity(val text: String, val occurrence: Int)
    private data class PreviewCandidate(val display: String, val identity: CandidateIdentity?)
    private val nativeActions = mutableListOf<NativeAction>()
    // Action offsets ending each completed native syllable. Auto-committed
    // prefixes are removed at these real input boundaries, never replayed.
    private val completedSyllableEnds = mutableListOf<Int>()
    private var visibleCandidatePlans = emptyList<PreviewCandidate>()

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
        if (chewing_config_set_int(nativeCtx, CONVERSION_ENGINE, FUZZY_ENGINE) != 0) {
            closeNativeContext()
            clearCachedState()
            ready = false
            return
        }
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
        nativeActions.clear()
        completedSyllableEnds.clear()
        clearCachedState()
    }

    override fun handleKeyUpdate(keyCode: Int): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val before = nativeSnapshot()
        val phonesBefore = before.phones
        val result = chewing_handle_default(nativeCtx, keyCode)
        val committed = if (chewing_commit_check(nativeCtx) != 0) {
            chewing_commit_string_static(nativeCtx).orEmpty()
        } else ""
        val committedCharacters = committed.codePointCount(0, committed.length)
        val phonesAfter = chewing_get_phone_seq_len(nativeCtx)
        val checkpoint = before.takeIf {
            committed.isEmpty() && phonesBefore == phonesAfter && chewing_bopomofo_check(nativeCtx) != 0
        }
        nativeActions += NativeAction.Key(keyCode, checkpoint)
        if (phonesAfter + committedCharacters == phonesBefore + 1) {
            // A tone completes this key's syllable; a fuzzy boundary completes
            // the prior syllable while this actual key starts the next one.
            completedSyllableEnds += nativeActions.size -
                if (chewing_bopomofo_check(nativeCtx) != 0) 1 else 0
        }
        trimAutoCommittedPrefix(committedCharacters)
        candidatePage = 0
        return snapshot(consumed = !isIgnored(result))
    }

    override fun backspaceUpdate(): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val hadComposition = chewing_buffer_check(nativeCtx) != 0 ||
            chewing_bopomofo_check(nativeCtx) != 0 ||
            preedit.isNotEmpty()
        if (!hadComposition) return snapshot(consumed = false)
        val phonesBefore = chewing_get_phone_seq_len(nativeCtx)
        val result = chewing_handle_backspace(nativeCtx)
        val lastKey = nativeActions.lastOrNull() as? NativeAction.Key
        if (lastKey?.pendingBefore != null && lastKey.pendingBefore == nativeSnapshot()) {
            // This key affected only an unfinished syllable, not the phone
            // buffer or committed text, and Backspace restored its exact state.
            nativeActions.removeAt(nativeActions.lastIndex)
        } else {
            nativeActions += NativeAction.Backspace
        }
        val removed = phonesBefore - chewing_get_phone_seq_len(nativeCtx)
        repeat(removed.coerceAtMost(completedSyllableEnds.size).coerceAtLeast(0)) {
            completedSyllableEnds.removeAt(completedSyllableEnds.lastIndex)
        }
        if (chewing_buffer_check(nativeCtx) == 0 && chewing_bopomofo_check(nativeCtx) == 0) {
            nativeActions.clear()
            completedSyllableEnds.clear()
        }
        candidatePage = 0
        return snapshot(consumed = !isIgnored(result) || hadComposition)
    }

    override fun selectCandidateUpdate(index: Int): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val plan = visibleCandidatePlans.getOrNull(index) ?: return snapshot(consumed = false)
        val history = nativeActions.toList()
        val hadTail = chewing_bopomofo_check(nativeCtx) != 0
        if (!finalizeTail(nativeCtx)) return restoreAfterUnavailableSelection(history)

        // Only an explicit selection mutates live selection state. The preview
        // user dictionary is :memory:, so its ordinal must never be reused here.
        if (chewing_cand_open(nativeCtx) != 0) return restoreAfterUnavailableSelection(history)
        val result = if (plan.identity == null) {
            chewing_cand_close(nativeCtx)
            0
        } else {
            val globalIndex = candidateIndex(nativeCtx, plan.identity)
            if (globalIndex < 0) -1 else chewing_cand_choose_by_index(nativeCtx, globalIndex)
        }
        val actual = chewing_buffer_string_static(nativeCtx).orEmpty()
        if (result != 0 || actual != plan.display || chewing_bopomofo_check(nativeCtx) != 0) {
            return restoreAfterUnavailableSelection(history)
        }
        if (hadTail) {
            nativeActions += NativeAction.FinalizeTail
            completedSyllableEnds += nativeActions.size
        }
        plan.identity?.let { nativeActions += NativeAction.Select(it) }
        if (completedSyllableEnds.isNotEmpty()) {
            completedSyllableEnds[completedSyllableEnds.lastIndex] = nativeActions.size
        }
        candidatePage = 0
        return snapshot(consumed = true)
    }

    override fun nextPageUpdate(): EngineUpdate? {
        if (!canPageCandidatesForward()) return null
        candidatePage++
        return snapshot(consumed = true)
    }

    override fun prevPageUpdate(): EngineUpdate? {
        if (!canPageCandidatesBackward()) return null
        candidatePage--
        return snapshot(consumed = true)
    }

    override fun canPageCandidatesBackward(): Boolean =
        isReady && nativeCtx != 0L && candidatePage > 0 && lastTotalChoices > 0

    override fun canPageCandidatesForward(): Boolean {
        if (!isReady || nativeCtx == 0L) return false
        val pageSize = candidatePageSize.coerceAtLeast(1)
        return lastTotalChoices > 0 && (candidatePage + 1) * pageSize < lastTotalChoices
    }

    override fun commitUpdate(): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(false)
        val hadComposition = chewing_buffer_check(nativeCtx) != 0 ||
            chewing_bopomofo_check(nativeCtx) != 0
        if (!hadComposition) return snapshot(consumed = false)
        val history = nativeActions.toList()
        if (!finalizeTail(nativeCtx)) return restoreAfterUnavailableSelection(history, NO_CONVERSION)
        if (!isConvertedDisplay(chewing_buffer_string_static(nativeCtx).orEmpty())) {
            return restoreAfterUnavailableSelection(history, NO_CONVERSION)
        }
        val result = chewing_commit_preedit_buf(nativeCtx)
        candidatePage = 0
        if (result == 0) {
            nativeActions.clear()
            completedSyllableEnds.clear()
        } else {
            return restoreAfterUnavailableSelection(history, NO_CONVERSION)
        }
        // An incomplete/invalid tail stays consumed and visible instead of
        // falling through to an editor action or dropping part of the input.
        return snapshot(consumed = true)
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
        nativeActions.clear()
        completedSyllableEnds.clear()
    }

    private fun snapshot(consumed: Boolean, unavailableReason: String? = null): EngineUpdate {
        if (!isReady || nativeCtx == 0L) return emptyUpdate(consumed)

        val committed = if (chewing_commit_check(nativeCtx) != 0) {
            chewing_commit_string_static(nativeCtx).orEmpty().also {
                // The CAPI commit getter does not consume its buffer.
                chewing_ack(nativeCtx)
            }
        } else {
            ""
        }

        val compositionBuffer = chewing_buffer_string_static(nativeCtx).orEmpty()
        val currentBopomofo = if (chewing_bopomofo_check(nativeCtx) != 0) {
            chewing_bopomofo_string_static(nativeCtx).orEmpty()
        } else {
            ""
        }
        val cursor = chewing_cursor_current(nativeCtx)
        preedit = composeVisiblePreedit(compositionBuffer, currentBopomofo, cursor)
        if (preedit.isEmpty()) {
            nativeActions.clear()
            completedSyllableEnds.clear()
        }
        candidates = buildCandidates()
        return EngineUpdate(
            consumed = consumed,
            preedit = preedit,
            candidates = candidates,
            committedText = committed,
            unavailableReason = unavailableReason,
        )
    }

    private fun buildCandidates(): List<String> {
        visibleCandidatePlans = emptyList()
        if (!isReady || nativeCtx == 0L || nativeActions.isEmpty()) {
            lastTotalChoices = 0
            return emptyList()
        }
        val preview = newPreviewContext()
        if (preview == 0L) {
            lastTotalChoices = 0
            return emptyList()
        }
        try {
            if (!replay(preview, nativeActions) || !finalizeTail(preview)) {
                lastTotalChoices = 0
                return emptyList()
            }
            val decoded = chewing_buffer_string_static(preview).orEmpty()
            if (decoded.isEmpty() || chewing_cand_open(preview) != 0) {
                lastTotalChoices = 0
                return emptyList()
            }
            candidatePageSize = chewing_cand_choice_per_page(preview).coerceAtLeast(1)
            val identities = candidateIdentities(preview)
            // The first item is the authentic full native conversion. All other
            // displays are read AFTER native selection in independent replay
            // contexts, because selecting a tail can re-convert earlier words.
            val plans = listOf<CandidateIdentity?>(null) + identities
            lastTotalChoices = plans.size
            val start = candidatePage * candidatePageSize
            visibleCandidatePlans = plans.drop(start).take(candidatePageSize)
                .mapNotNull { identity ->
                    if (identity == null) decoded.takeIf(::isConvertedDisplay)?.let {
                        PreviewCandidate(it, null)
                    }
                    else projectCandidate(identity)
                }
                .distinctBy { it.display }
            return visibleCandidatePlans.map { it.display }
        } finally {
            chewing_delete(preview)
        }
    }

    private fun projectCandidate(identity: CandidateIdentity): PreviewCandidate? {
        val preview = newPreviewContext()
        if (preview == 0L) return null
        try {
            if (!replay(preview, nativeActions) || !finalizeTail(preview) ||
                chewing_cand_open(preview) != 0) return null
            val index = candidateIndex(preview, identity)
            if (index < 0 || chewing_cand_choose_by_index(preview, index) != 0) return null
            val display = chewing_buffer_string_static(preview).orEmpty()
            return display.takeIf(::isConvertedDisplay)?.let { PreviewCandidate(it, identity) }
        } finally {
            chewing_delete(preview)
        }
    }

    private fun newPreviewContext(): Long {
        val ctx = chewing_new2(systemDataPath, MEMORY_USER_DICTIONARY)
        if (ctx == 0L) return 0L
        chewing_set_kb_type(ctx, KB_DEFAULT)
        chewing_set_chi_eng_mode(ctx, CHINESE_MODE)
        chewing_set_shape_mode(ctx, HALFSHAPE_MODE)
        chewing_set_auto_learn(ctx, AUTOLEARN_DISABLED)
        if (chewing_config_set_int(ctx, CONVERSION_ENGINE, FUZZY_ENGINE) != 0) {
            chewing_delete(ctx)
            return 0L
        }
        return ctx
    }

    /**
     * a6a8fa4 fuzzy_key_press inserts the prior non-toned syllable on a new
     * initial. Only this disposable preview, or an explicit commit/selection,
     * receives that boundary. Check the phone count so an invalid tail cannot
     * silently disappear. The synthetic initial is cleared before reading.
     */
    private fun finalizeTail(ctx: Long): Boolean {
        if (chewing_bopomofo_check(ctx) == 0) return true
        val before = chewing_get_phone_seq_len(ctx)
        chewing_handle_default(ctx, PREVIEW_BOUNDARY_INITIAL)
        chewing_clean_bopomofo_buf(ctx)
        if (chewing_get_phone_seq_len(ctx) != before + 1) return false
        // clean_bopomofo_buf leaves the state EnteringSyllable; a safe open/close
        // after finalization returns it to Entering without discarding input.
        if (chewing_cand_open(ctx) != 0) return false
        chewing_cand_close(ctx)
        return true
    }

    private fun candidateIdentities(ctx: Long): List<CandidateIdentity> {
        val occurrences = mutableMapOf<String, Int>()
        return chewing_cand_strings(ctx).mapNotNull { candidate ->
            candidate.takeIf { it.isNotEmpty() }?.let { text ->
                val occurrence = occurrences.getOrDefault(text, 0)
                occurrences[text] = occurrence + 1
                CandidateIdentity(text, occurrence)
            }
        }
    }

    private fun candidateIndex(ctx: Long, identity: CandidateIdentity): Int =
        candidateIdentities(ctx).indexOf(identity)

    private fun isConvertedDisplay(text: String): Boolean = text.isNotEmpty() && text.none {
        it in '\u3100'..'\u312f' || it in '\u31a0'..'\u31bf' ||
            it in "ˇˉˊˋ˙"
    }

    private fun trimAutoCommittedPrefix(characters: Int) {
        val count = characters.coerceAtMost(completedSyllableEnds.size)
        if (count <= 0) return
        val cut = completedSyllableEnds[count - 1]
        nativeActions.subList(0, cut).clear()
        completedSyllableEnds.subList(0, count).clear()
        for (index in completedSyllableEnds.indices) completedSyllableEnds[index] -= cut
    }

    private fun replay(ctx: Long, actions: List<NativeAction>): Boolean {
        for (action in actions) {
            when (action) {
                is NativeAction.Key -> chewing_handle_default(ctx, action.code)
                NativeAction.Backspace -> chewing_handle_backspace(ctx)
                NativeAction.FinalizeTail -> if (!finalizeTail(ctx)) return false
                is NativeAction.Select -> {
                    if (chewing_cand_open(ctx) != 0) return false
                    val index = candidateIndex(ctx, action.identity)
                    if (index < 0 || chewing_cand_choose_by_index(ctx, index) != 0) return false
                }
            }
            // Historical auto-commits are already owned by InputConnection;
            // replay is a state projection and must never emit them again.
            chewing_ack(ctx)
        }
        return true
    }

    private fun restoreAfterUnavailableSelection(
        history: List<NativeAction>,
        reason: String = CANDIDATE_CHANGED,
    ): EngineUpdate {
        val learning = personalizedLearningEnabled
        chewing_set_auto_learn(nativeCtx, AUTOLEARN_DISABLED)
        chewing_reset(nativeCtx)
        replay(nativeCtx, history)
        chewing_set_auto_learn(nativeCtx, if (learning) AUTOLEARN_ENABLED else AUTOLEARN_DISABLED)
        return snapshot(consumed = true, unavailableReason = reason)
    }

    private fun clearCachedState() {
        preedit = ""
        candidates = emptyList()
        candidatePage = 0
        candidatePageSize = 0
        lastTotalChoices = 0
        visibleCandidatePlans = emptyList()
    }

    private fun nativeSnapshot() = NativeSnapshot(
        chewing_buffer_string_static(nativeCtx).orEmpty(),
        chewing_bopomofo_string_static(nativeCtx).orEmpty(),
        chewing_cursor_current(nativeCtx),
        chewing_get_phone_seq_len(nativeCtx),
    )

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
        private const val CONVERSION_ENGINE = "chewing.conversion_engine"
        private const val FUZZY_ENGINE = 2
        private const val MEMORY_USER_DICTIONARY = ":memory:"
        private const val PREVIEW_BOUNDARY_INITIAL = 115 // Dachen s = ㄋ
        private const val NO_CONVERSION = "這組注音尚無可用候選"
        private const val CANDIDATE_CHANGED = "候選已更新，請重新選擇"

        fun isIgnored(rtn: Int): Boolean = (rtn and KEYSTROKE_IGNORE) != 0
    }

    private external fun chewing_new2(systemDataPath: String, userDataPath: String): Long
    private external fun chewing_delete(ctx: Long)
    private external fun chewing_reset(ctx: Long): Int
    private external fun chewing_handle_default(ctx: Long, key: Int): Int
    private external fun chewing_handle_backspace(ctx: Long): Int
    private external fun chewing_buffer_string_static(ctx: Long): String?
    private external fun chewing_buffer_check(ctx: Long): Int
    private external fun chewing_bopomofo_string_static(ctx: Long): String?
    private external fun chewing_bopomofo_check(ctx: Long): Int
    private external fun chewing_cursor_current(ctx: Long): Int
    private external fun chewing_cand_open(ctx: Long): Int
    private external fun chewing_cand_close(ctx: Long): Int
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
    private external fun chewing_config_set_int(ctx: Long, option: String, value: Int): Int
    private external fun chewing_clean_bopomofo_buf(ctx: Long): Int
    private external fun chewing_get_phone_seq_len(ctx: Long): Int
    private external fun chewing_ack(ctx: Long)
    private external fun chewing_cand_strings(ctx: Long): Array<String>
}

/**
 * Build the visible Android preedit from libchewing's two composition surfaces.
 * libchewing keeps converted/preedit text and the currently-entered Bopomofo syllable
 * separately; the Bopomofo string belongs at the decoder cursor position.
 */
internal fun composeVisiblePreedit(
    compositionBuffer: String,
    currentBopomofo: String,
    cursorCodePointIndex: Int,
): String {
    if (currentBopomofo.isEmpty()) return compositionBuffer
    if (compositionBuffer.isEmpty()) return currentBopomofo

    val codePointCount = compositionBuffer.codePointCount(0, compositionBuffer.length)
    val safeCursor = cursorCodePointIndex.coerceIn(0, codePointCount)
    val insertionOffset = compositionBuffer.offsetByCodePoints(0, safeCursor)
    return buildString(compositionBuffer.length + currentBopomofo.length) {
        append(compositionBuffer, 0, insertionOffset)
        append(currentBopomofo)
        append(compositionBuffer, insertionOffset, compositionBuffer.length)
    }
}
