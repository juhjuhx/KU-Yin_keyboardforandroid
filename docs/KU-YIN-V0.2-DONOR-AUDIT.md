# KU-Yin v0.2 Donor Audit (M3: candidate presentation)

Policy (from spec §18): no code is copied without per-file license/provenance
review. Prefer adapting concepts over copying code. Gboard is visual-only.

KU-Yin repository license: LGPL-2.1 (matches fcitx5-android family).

## Donor matrix

| Project | License | Feature studied | Files inspected | Verdict | Reused in M3 |
|---|---|---|---|---|---|
| fcitx5-android (`fcitx5-android/fcitx5-android`, master) | LGPL-2.1 (repo LICENSE, verified via GitHub) | Expandable candidate view; horizontal strip + expanded grid sharing decoder-ordered data; expand affordance as explicit state (`ExpandButtonStateMachine`); content-width items (Flexbox) | `.../input/candidates/horizontal/HorizontalCandidateComponent.kt`, `.../horizontal/HorizontalCandidateViewAdapter.kt`, `.../candidates/expanded/window/BaseExpandedCandidateWindow.kt`, `.../expanded/window/FlexboxExpandedCandidateWindow.kt`, `.../expanded/window/GridExpandedCandidateWindow.kt`, `.../candidates/CandidateViewHolder.kt` (all via code search snippets + repo tree; no file content copied) | GREEN (concepts) | Concepts only: separate collapsed/expanded presentations, explicit expand state, decoder-owned ordering. Mechanism differs on purpose (KU-Yin keeps dependency-free custom `CandidateView`; no RecyclerView/Flexbox/splitties/paging deps added) |
| fcitx5-chewing (plugin inside fcitx5-android) | LGPL-2.1 (same repo) | Chewing candidate interaction: select-by-index → decoder snapshot → preedit/candidates/committed update | `plugin/chewing` tree (confirmed exists; matches KU-Yin's `selectCandidateUpdate → applyEngineUpdate` shape) | GREEN (concepts) | Concepts only: index-based selection, commit only decoder-reported text. No code copied |
| FlorisBoard | Apache-2.0 (LICENSE verified) | Suggestion strip / settings architecture (secondary reference) | Repo LICENSE only; no source files opened for M3 | GREEN (unused this round) | Nothing reused in M3 |
| HeliBoard (`Helium314/HeliBoard`) | GPL-3.0 (LICENSE verified) | None (UX ideas only, per spec) | No source files opened | RED (copying would require relicense decision) | Nothing reused, nothing copied |
| Trime / Rime Android | (various; not inspected this round) | Deferred to later milestone if needed | None | YELLOW (unverified) | Nothing reused |
| Gboard / AOSP LatinIME | Proprietary / AOSP | Visual/behavioral reference only | None (never copy) | Visual-only | Nothing reused |
| Google AI Studio prototype (user-provided) | User's own output; third-party deps inside unchecked | Candidate bar density, chip visuals (Phase D scope) | Not used in M3 | YELLOW (pending Phase D provenance pass) | Nothing reused in M3 |

## M3 provenance statement

M3 (`CandidateState`, `candidateStateOf`, `SelectCandidate` command/effect,
`CandidateView` collapsed/expanded rendering, engine `canPage*` queries) is
original KU-Yin code written against the v0.2 spec (§14) and the existing
`KeyboardController`/`ImeEffect` bridge. Zero donor files copied, zero new
dependencies added. fcitx5-android LGPL-2.1 compatibility was verified but not
exercised (no copy occurred).

## Open donor work (post-M3)

- Phase D visual audit of AI Studio prototype (KEEP/ADAPT/DROP per asset, with
  third-party icon/dependency license checks).
- If M4+ needs a donor file verbatim: record exact path + commit SHA + license
  header here BEFORE merging.
