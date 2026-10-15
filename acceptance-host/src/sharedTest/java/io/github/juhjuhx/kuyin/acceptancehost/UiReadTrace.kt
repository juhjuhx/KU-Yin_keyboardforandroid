package io.github.juhjuhx.kuyin.acceptancehost

internal enum class UiReadOwner { BODY, CLEANUP }
internal enum class UiReadPhase { IDLE, DISABLE_IDLE, QUERY, REGISTER, QUERY_REGISTER, PARENT_REGISTER, CHILD_REGISTER, PARENT, PARENT_PACKAGE, CHILDREN, BOUNDS, PACKAGE, TEXT, DESCRIPTION, CLICKABLE, RESTORE_IDLE }
internal data class UiReadFailure(val owner: UiReadOwner, val capture: Int, val attempt: Int,
    val phase: UiReadPhase, val node: Int, val elapsedMs: Long, val stale: Boolean)

/** TEST_ONLY: fixed labels and numeric metadata; never store node content or exception messages. */
internal class UiReadTrace(private val now: () -> Long, private val isStale: (Throwable) -> Boolean) {
    var owner = UiReadOwner.BODY
    var capture = 0
    var attempt = 0
    private val recent = ArrayDeque<UiReadFailure>()
    private val ownerFailures = linkedMapOf<UiReadOwner, UiReadFailure>()
    val failures: List<UiReadFailure> get() = recent.toList()
    val lastFailuresByOwner: List<UiReadFailure> get() = ownerFailures.values.toList()
    var staleCount: Int = 0
        private set
    fun <T> read(phase: UiReadPhase, node: Int = -1, block: () -> T): T {
        val started = now()
        try {
            return block()
        } catch (error: Throwable) {
            val stale = isStale(error)
            if (stale) staleCount++
            if (recent.size == 8) recent.removeFirst()
            val failure = UiReadFailure(owner, capture, attempt, phase, node, now() - started, stale)
            recent.addLast(failure)
            ownerFailures[owner] = failure
            throw error
        }
    }
}
