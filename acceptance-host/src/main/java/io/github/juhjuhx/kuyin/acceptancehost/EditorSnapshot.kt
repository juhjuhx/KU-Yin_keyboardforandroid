package io.github.juhjuhx.kuyin.acceptancehost

data class EditorInfoSnapshot(
    val inputType: Int,
    val imeOptions: Int,
    val actionLabel: String?,
    val actionId: Int,
    val inputConnectionPresent: Boolean,
)

data class EditorSnapshot(
    val caseId: String,
    val platformMetadata: EditorInfoSnapshot? = null,
    val finalMetadata: EditorInfoSnapshot? = null,
    val currentText: String = "",
    val editorActionCount: Int = 0,
    val lastEditorActionId: Int? = null,
    val lastActionReturnValue: Boolean? = null,
    val commitCount: Int = 0,
    val lastCommitText: String? = null,
    val revision: Int = 0,
    val composingStart: Int = -1,
    val composingEnd: Int = -1,
    val lastCommitAccepted: Boolean? = null,
) {
    val newlineCount: Int get() = currentText.count { it == '\n' }
}
