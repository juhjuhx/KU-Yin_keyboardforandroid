package io.github.juhjuhx.kuyin.acceptancehost

class EditorRecorder(
    private val config: CaseConfig,
    private val onSnapshotChanged: () -> Unit = {},
) {
    private var state = EditorSnapshot(caseId = config.caseId, currentText = config.initialText)
    val snapshot: EditorSnapshot get() = synchronized(this) { state }

    fun recordConnection(platform: EditorInfoSnapshot, final: EditorInfoSnapshot) = update {
        it.copy(platformMetadata = platform, finalMetadata = final)
    }
    fun recordText(text: String) = update { it.copy(currentText = text) }
    fun recordCommit(text: String) = update {
        it.copy(commitCount = it.commitCount + 1, lastCommitText = text)
    }
    fun recordAction(actionId: Int): Boolean {
        update {
            it.copy(editorActionCount = it.editorActionCount + 1,
                lastEditorActionId = actionId, lastActionReturnValue = config.performEditorActionReturnValue)
        }
        return config.performEditorActionReturnValue
    }

    private fun update(transform: (EditorSnapshot) -> EditorSnapshot) {
        synchronized(this) { state = transform(state).copy(revision = state.revision + 1) }
        onSnapshotChanged()
    }
}
