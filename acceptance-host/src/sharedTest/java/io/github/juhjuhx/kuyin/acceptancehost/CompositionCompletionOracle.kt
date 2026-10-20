package io.github.juhjuhx.kuyin.acceptancehost

/** Actual host Editable span offsets, captured with its immutable editor snapshot on the UI thread. */
internal data class EditorCompositionBoundary(val editor: EditorSnapshot, val start: Int, val end: Int)

/** A commit replaces the actual composing interval; the projected preedit is not committed prefix. */
internal fun assertCompositionCompletion(boundary: EditorCompositionBoundary, after: EditorSnapshot) {
    val before = boundary.editor
    if (boundary.start < 0 || boundary.end <= boundary.start || boundary.end > before.currentText.length)
        throw AssertionError("actual host composing interval is missing or invalid")
    if (after.editorActionCount != before.editorActionCount) throw AssertionError("composition Enter dispatched action")
    if (after.newlineCount != before.newlineCount) throw AssertionError("composition Enter inserted newline")
    if (after.commitCount != before.commitCount + 1) throw AssertionError("composition must commit exactly once")
    if (after.lastCommitText.isNullOrEmpty()) throw AssertionError("composition completion must commit nonempty text")
    val expected = before.currentText.replaceRange(boundary.start, boundary.end, checkNotNull(after.lastCommitText))
    if (after.currentText != expected) throw AssertionError("actual editor composing replacement mismatch")
}
