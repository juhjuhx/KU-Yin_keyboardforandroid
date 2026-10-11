package com.example.ime.session

import kotlinx.coroutines.flow.StateFlow
import com.example.androidkeyboard.engines.core.EngineCandidatePage

data class ComposeImeState(
    val preedit: String = "",
    val candidates: List<String> = emptyList(),
    val canPageBackward: Boolean = false,
    val canPageForward: Boolean = false,
    val candidatesExpanded: Boolean = false,
    val generation: Long = 0L,
    val unavailableReason: String? = null,
    val candidatePage: CandidatePageSnapshot? = null,
)

data class CandidateEntry(val label: String, val selectionIdentity: String)

data class CandidateRef(
    val snapshotGeneration: Long,
    val providerIdentity: String,
    val pageIndex: Int?,
    val selectionIdentity: String,
)

/** One immutable presentation/selection boundary. Unknown provider totals stay null. */
data class CandidatePageSnapshot(
    val compositionGeneration: Long,
    val snapshotGeneration: Long,
    val providerIdentity: String,
    val pageIndex: Int?,
    val pageSize: Int,
    val totalPages: Int?,
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val candidates: List<CandidateEntry>,
) {
    fun ref(index: Int): CandidateRef? = candidates.getOrNull(index)?.let {
        CandidateRef(snapshotGeneration, providerIdentity, pageIndex, it.selectionIdentity)
    }
}

internal fun candidatePageSnapshot(
    provider: String,
    generation: Long,
    compositionGeneration: Long,
    candidates: List<String>,
    metadata: EngineCandidatePage? = null,
    hasPrevious: Boolean = false,
    hasNext: Boolean = false,
) = CandidatePageSnapshot(
    compositionGeneration, generation, provider, metadata?.pageIndex,
    metadata?.pageSize ?: candidates.size, metadata?.totalPages,
    hasPrevious, hasNext,
    candidates.mapIndexed { index, label ->
        CandidateEntry(label, metadata?.selectionIds?.getOrNull(index) ?: "ordinal:$index")
    },
)

internal fun ComposeImeState.withExpanded(expanded: Boolean, invalidate: Boolean = false): ComposeImeState {
    val revision = generation + if (invalidate) 1 else 0
    return copy(candidatesExpanded = expanded, generation = revision,
        candidatePage = candidatePage?.copy(snapshotGeneration = revision))
}

sealed interface ImeCommand {
    data class TapZhuyinKey(val char: String) : ImeCommand
    data class SelectCandidate(val index: Int, val reference: CandidateRef? = null) : ImeCommand
    data object Backspace : ImeCommand
    data object Space : ImeCommand
    data object Enter : ImeCommand
    data class Punctuation(val text: String) : ImeCommand
    data object Complete : ImeCommand
    data object PageForward : ImeCommand
    data object PageBackward : ImeCommand
    data object ToggleCandidatesExpanded : ImeCommand
    data object OpenExpandedCandidates : ImeCommand
    data object CloseExpandedCandidates : ImeCommand
    data class NextCandidatePage(val snapshot: CandidatePageSnapshot) : ImeCommand
    data class PreviousCandidatePage(val snapshot: CandidatePageSnapshot) : ImeCommand
    data object ClearComposing : ImeCommand
    data object Reset : ImeCommand
}

data class DispatchResult(
    val commitText: String?,
    val consumed: Boolean,
    val additionalCommits: List<String> = emptyList()
)

/**
 * D3e: the single centralized editor-effect projection. Callers apply the
 * returned texts in list order, each exactly once: commitText first, then
 * additionalCommits. No command-specific application path may reorder,
 * merge, or duplicate these effects.
 */
fun orderedCommits(result: DispatchResult): List<String> =
    if (result.commitText != null) {
        listOf(result.commitText) + result.additionalCommits
    } else {
        result.additionalCommits
    }

interface ComposeDecoderSession {
    val state: StateFlow<ComposeImeState>
    fun dispatch(command: ImeCommand): DispatchResult
}
