package io.github.juhjuhx.kuyin.acceptancehost

internal const val FIXTURE_PRODUCT_PACKAGE = "io.github.juhjuhx.kuyin"

internal data class UiBounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val visible: Boolean get() = right > left && bottom > top
    fun overlapsVertically(other: UiBounds) = top < other.bottom && other.top < bottom
}

/** Complete immutable observation; never keep live accessibility reads in selectors. */
internal data class VisibleUiNode(val id: Int, val parentId: Int?, val packageName: String?,
    val text: String?, val description: String?, val clickable: Boolean, val bounds: UiBounds)
internal data class PreeditObservation(val text: String, val bounds: UiBounds, val clearBounds: UiBounds)

internal class VisibleUiSnapshot(val nodes: List<VisibleUiNode>) {
    private val byId = nodes.associateBy { it.id }
    private fun children(node: VisibleUiNode) = nodes.filter { it.parentId == node.id }
    fun descendants(node: VisibleUiNode): List<VisibleUiNode> {
        val result = mutableListOf<VisibleUiNode>()
        val visited = mutableSetOf(node.id)
        fun visit(parent: VisibleUiNode) {
            children(parent).forEach {
                check(visited.add(it.id)) { "cyclic accessibility observation" }
                result += it
                visit(it)
            }
        }
        visit(node)
        return result
    }
    private fun clickableAncestor(node: VisibleUiNode): VisibleUiNode? {
        var current: VisibleUiNode? = node
        repeat(12) {
            val value = current ?: return null
            if (value.packageName != FIXTURE_PRODUCT_PACKAGE) return null
            if (value.clickable && value.bounds.visible) return value
            current = byId[value.parentId]
        }
        return null
    }
    private fun targets(nodes: List<VisibleUiNode>) = nodes.mapNotNull(::clickableAncestor)
        .distinctBy { it.id }
    fun textTargets(label: String, tokenMatch: Boolean = false) = targets(nodes.filter {
        it.bounds.visible && (it.text == label || (tokenMatch && it.text?.let { text -> semanticToken(text, label) } == true))
    })
    fun descriptionTargets(label: String) = targets(nodes.filter { it.bounds.visible && it.description == label })
    fun targetText(target: VisibleUiNode): String? = target.text
        ?: descendants(target).mapNotNull { it.text }.distinct().singleOrNull()
    fun keyboardTop(): Int? = textTargets("ㄅ", tokenMatch = true).singleOrNull()?.bounds?.top
        ?: (textTargets("q") + textTargets("Q")).maxOfOrNull { it.bounds.top }

    /** Preserves the frozen strict-preedit identity; keyboard/candidate text is never evidence. */
    fun strictPreedit(): PreeditObservation? {
        val clear = descriptionTargets("清除").singleOrNull() ?: return null
        var ancestor = byId[clear.parentId]
        repeat(5) {
            val subtree = ancestor ?: return null
            if (subtree.packageName != FIXTURE_PRODUCT_PACKAGE) return null
            val texts = descendants(subtree).filter {
                it.packageName == FIXTURE_PRODUCT_PACKAGE && !it.text.isNullOrBlank() &&
                    children(it).isEmpty() && clickableAncestor(it) == null &&
                    it.bounds.visible && it.bounds.right <= clear.bounds.left + 2 &&
                    it.bounds.overlapsVertically(clear.bounds)
            }.distinctBy { it.id }
            if (texts.size == 1) {
                val preedit = texts.single()
                return PreeditObservation(checkNotNull(preedit.text).trim(), preedit.bounds, clear.bounds)
            }
            if (texts.size > 1) return null
            ancestor = byId[subtree.parentId]
        }
        return null
    }

    /** S1: the candidate strip is below the header, bounded above the actual keyboard. */
    fun candidates(preedit: PreeditObservation): List<VisibleUiNode> {
        val expanded = descriptionTargets("收合候選").size == 1
        val lowerBoundary = if (expanded) {
            nodes.filter { it.bounds.visible && it.packageName == FIXTURE_PRODUCT_PACKAGE &&
                it.description in listOf("上一頁候選", "下一頁候選") }.minOfOrNull { it.bounds.top }
        } else {
            // A visible keyboard anchor is mandatory. English candidates may equal letters;
            // the lowest q/Q is the actual first keyboard row, not the suggestion above it.
            keyboardTop()
        } ?: return emptyList()
        return targets(nodes.filter { it.bounds.visible && !it.text.isNullOrBlank() }).filter {
            it.bounds.top >= preedit.clearBounds.bottom && it.bounds.bottom <= lowerBoundary
        }
    }
}

internal fun semanticToken(text: String, token: String): Boolean =
    text == token || text.split(Regex("[\\s,]+"), limit = 0).any { it == token } ||
        text.lineSequence().any { it.trim() == token }

internal class UiReadDeadline : RuntimeException("fresh UI observation exceeded its bounded deadline")

/** Read-only boundary. Gesture injection is deliberately outside this function. */
internal fun <T> freshUiRead(deadline: Long, now: () -> Long, pause: (Long) -> Unit,
    isStale: (Throwable) -> Boolean, read: () -> T): T {
    while (now() < deadline) {
        try {
            val result = read()
            if (now() > deadline) throw UiReadDeadline()
            return result
        } catch (error: Throwable) { if (!isStale(error)) throw error }
        val remaining = deadline - now()
        if (remaining <= 0) throw UiReadDeadline()
        pause(minOf(50L, remaining))
    }
    throw UiReadDeadline()
}

/** Apply to the settled actual editor snapshot, never only to the first matching effect. */
internal fun assertCandidateCompletion(before: EditorSnapshot, after: EditorSnapshot,
    selectedLabel: String, expectedEditorText: String) {
    if (after.commitCount != before.commitCount + 1) throw AssertionError("candidate must commit exactly once")
    if (after.currentText != expectedEditorText) throw AssertionError("actual editor text mismatch")
    if (after.lastCommitText != selectedLabel) throw AssertionError("candidate commit differs from visible selected label")
    if (after.editorActionCount != before.editorActionCount) throw AssertionError("candidate dispatched editor action")
    if (after.newlineCount != before.newlineCount) throw AssertionError("candidate inserted a newline")
}
