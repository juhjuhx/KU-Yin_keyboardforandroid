package io.github.juhjuhx.kuyin.acceptancehost

import android.graphics.Rect
import android.os.SystemClock
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import org.json.JSONObject

/** Live nodes survive only to perform one gesture; every property comes from the completed capture. */
internal class VisibleTarget(val node: VisibleUiNode, val tree: VisibleUiSnapshot, private val handle: UiObject2) {
    val text get() = node.text
    val isClickable get() = node.clickable
    val visibleBounds get() = node.bounds.toRect()
    fun texts(): List<String> = (listOf(node) + tree.descendants(node)).mapNotNull { it.text }
    fun click() { handle.click() } // Never inside a retry boundary.
}

internal class CapturedUi(val tree: VisibleUiSnapshot, private val handles: Map<Int, UiObject2>) {
    fun targets(nodes: List<VisibleUiNode>) = nodes.map { VisibleTarget(it, tree, checkNotNull(handles[it.id])) }
}

internal class ExternalUiObservation(private val device: UiDevice) {
    private var captureCount = 0
    private var attemptCount = 0
    private var completedCount = 0
    private var elapsedMs = 0L
    private var maxElapsedMs = 0L
    private var lastDeadline = 0L
    private var lastComplete = false
    private var lastNodeCount = 0
    private val idleControl = object : IdleReadControl {
        override fun getWaitForIdleTimeout() = Configurator.getInstance().getWaitForIdleTimeout()
        override fun setWaitForIdleTimeout(timeout: Long) { Configurator.getInstance().setWaitForIdleTimeout(timeout) }
        override fun waitForIdle(timeout: Long) { device.waitForIdle(timeout) }
    }
    fun capture(deadline: Long): CapturedUi {
        val started = SystemClock.uptimeMillis()
        captureCount++
        lastDeadline = deadline
        lastComplete = false
        lastNodeCount = 0
        try {
            val captured = freshUiRead(deadline, SystemClock::uptimeMillis, SystemClock::sleep,
                { retryableScopedStale(it) { error -> error is StaleObjectException } }) {
                attemptCount++
                scopedIdleRead(deadline, SystemClock::uptimeMillis, idleControl, ::captureOnce)
            }
            completedCount++
            lastComplete = true
            lastNodeCount = captured.tree.nodes.size
            return captured
        } finally {
            val elapsed = SystemClock.uptimeMillis() - started
            elapsedMs += elapsed
            maxElapsedMs = maxOf(maxElapsedMs, elapsed)
        }
    }

    /** Numeric observation diagnostics only; no tree, text or keystroke data. */
    fun diagnostics(): JSONObject = JSONObject().put("capture_count", captureCount)
        .put("attempt_count", attemptCount).put("completed_count", completedCount)
        .put("elapsed_ms", elapsedMs).put("max_elapsed_ms", maxElapsedMs)
        .put("last_deadline_ms", lastDeadline).put("last_complete", lastComplete)
        .put("last_node_count", lastNodeCount)

    private fun captureOnce(): CapturedUi {
        val handles = mutableListOf<UiObject2>()
        val ids = LinkedHashMap<UiObject2, Int>()
        fun register(handle: UiObject2): Int = ids.getOrPut(handle) {
            if (handles.size >= 1_000) throw CaseInconclusive("BLOCKED_SELECTOR", "完整 UI hierarchy 超過 fixture 的有界上限；不使用部分樹")
            handles.add(handle)
            handles.lastIndex
        }
        device.findObjects(By.pkg(FIXTURE_PRODUCT_PACKAGE)).forEach(::register)
        val nodes = mutableListOf<VisibleUiNode>()
        var index = 0
        while (index < handles.size) {
            val handle = handles[index]
            val parent = handle.parent?.takeIf { it.applicationPackage == FIXTURE_PRODUCT_PACKAGE }
            val parentId = parent?.let(::register)
            // Traverse the reported children. A surfaced stale read invalidates the whole
            // capture; UIAutomator may omit unavailable nodes, so this is not an atomic tree.
            handle.children.forEach(::register)
            val rect = handle.visibleBounds
            nodes += VisibleUiNode(index, parentId, handle.applicationPackage, handle.text,
                handle.contentDescription, handle.isClickable, UiBounds(rect.left, rect.top, rect.right, rect.bottom))
            index++
        }
        return CapturedUi(VisibleUiSnapshot(nodes.toList()), handles.withIndex().associate { it.index to it.value })
    }
}

internal fun UiBounds.toRect() = Rect(left, top, right, bottom)
