package com.example.ime.service

import android.view.inputmethod.InputConnection

/**
 * Tracks only offsets from acknowledged IME writes, scoped to one editor generation/connection.
 * A matching pending tuple may be the editor's own selection notification; arbitrary offsets
 * are never blanket-ignored. This is not an acknowledgement of the editor's text contents.
 */
internal class EditorEffectTracker {
    data class Selection(
        val start: Int,
        val end: Int,
        val composingStart: Int = -1,
        val composingEnd: Int = -1,
    )

    private data class Expected(val generation: Long, val selection: Selection)
    private var generation = 0L
    private var connection: InputConnection? = null
    private var projected = Selection(-1, -1)
    private var ownsComposing = false
    private val expected = ArrayDeque<Expected>()

    fun startEditor(ic: InputConnection?, initialStart: Int, initialEnd: Int) {
        generation++
        connection = ic
        projected = Selection(initialStart, initialEnd)
        ownsComposing = false
        expected.clear()
    }

    fun ownsComposition(ic: InputConnection?): Boolean =
        ic != null && ic === connection && ownsComposing

    fun recordWrite(
        ic: InputConnection,
        textLength: Int,
        composing: Boolean,
        before: Selection?,
        after: Selection?,
    ) {
        if (ic !== connection) startEditor(ic, before?.start ?: -1, before?.end ?: -1)
        val prior = before ?: projected
        val replacementStart = if (ownsComposing && projected.composingStart >= 0) {
            projected.composingStart
        } else if (prior.start >= 0 && prior.end >= 0) minOf(prior.start, prior.end) else -1
        val cursor = if (replacementStart >= 0) replacementStart + textLength else -1
        val selectionStart = after?.start ?: cursor
        val selectionEnd = after?.end ?: cursor
        val composingStart = when {
            !composing || textLength == 0 -> -1
            after != null && after.composingStart >= 0 -> after.composingStart
            else -> replacementStart
        }
        val composingEnd = when {
            composingStart < 0 -> -1
            after != null && after.composingEnd >= 0 -> after.composingEnd
            after != null && after.start == after.end -> after.end
            else -> composingStart + textLength
        }
        projected = Selection(selectionStart, selectionEnd, composingStart, composingEnd)
        ownsComposing = composing && textLength > 0
        if (selectionStart >= 0 && selectionEnd >= 0) {
            expected.addLast(Expected(generation, projected))
            while (expected.size > MAX_PENDING_NOTIFICATIONS) expected.removeFirst()
        }
    }

    fun consumeOwnSelection(ic: InputConnection?, selection: Selection): Boolean {
        if (ic == null || ic !== connection) return false
        val match = expected.indexOfFirst { it.generation == generation && it.selection == selection }
        if (match < 0) return false
        repeat(match + 1) { expected.removeFirst() }
        return true
    }

    fun compositionWasReplaced(ic: InputConnection?, selection: Selection): Boolean =
        ownsComposition(ic) && (selection.composingStart != projected.composingStart ||
            selection.composingEnd != projected.composingEnd)

    fun finishComposition() {
        ownsComposing = false
        projected = projected.copy(composingStart = -1, composingEnd = -1)
        expected.clear()
    }

    fun observeExternalSelection(start: Int, end: Int) {
        projected = Selection(start, end)
        ownsComposing = false
        expected.clear()
    }

    /** A live styled range, when available, must still be the one owned by this bridge. */
    fun ownsLiveRange(ic: InputConnection, live: Selection?): Boolean =
        ownsComposition(ic) && (live == null ||
            live.composingStart >= 0 && live.composingStart == projected.composingStart &&
                live.composingEnd == projected.composingEnd)

    private companion object {
        const val MAX_PENDING_NOTIFICATIONS = 32
    }
}
