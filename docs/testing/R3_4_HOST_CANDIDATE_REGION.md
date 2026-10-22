# R3.4 Host candidate region — Draft infrastructure slice

## Verified problem and scope

Base main `5c7b22c2e58a08c6a7f18633aa2e0378b68ef418`. The product's
`KuYinKeyboardUi.kt` CandidateBar is a Column: preedit/clear header, then
CandidateStrip, then the keyboard. AcceptanceDriver and CoreTypingAcceptanceTest
incorrectly required candidates to overlap the header and be to its right.

This patch only changes those two geometric eligibility predicates. A shared
test-only bounds function accepts complete visible targets below the clear
header and above the observed first keyboard row. Two unique first-row anchors
(ㄅ/ㄉ or q/w, Q/W) establish that boundary; missing/ambiguous anchors fail closed.
No fixed screen coordinates or dp height substitutes for real target identity.
Existing exact label, package, clickable, uniqueness, strict preedit, editor
text/commit/action, B02/B03 and D13/D14 final assertions remain unchanged.

## Evidence and limits

- Extracted legacy predicate (identical same-row rule) against eight literal,
  hand-checked geometry cases: 6 FAIL / 2 PASS / 0 errors. New region: 8 PASS.
- Full Host JVM: 45 PASS / 0 FAIL / 0 SKIP. Host Debug and instrumentation APK
  compile PASS. This is not Android execution.
- Full Python initially: 109 tests, 1 failure from comparing current Host bytes
  against the frozen P1-E source hash. Original failure is retained externally.
  Reuse the existing R2 frozen-source pack, validating its eight exact hashes
  against the unchanged manifest; require missing/tampered copies to fail.
  Final full Python:111 PASS / 0 FAIL / 0 SKIP; all10 contracts PASS.
- Nine historical StaleObjectException FAIL and eight SKIP are NOT repaired by
  geometry unit tests. Main's broad live-node getters remain unchanged. In
  particular EN02 primary BLOCKED_SELECTOR and its suppressed cleanup failure
  remain separate evidence. Do not call this a stale fix or product PASS.
- Latest permitted health run39734667334 stopped at H0 final frozen WMS;
  H1/H2/Host were not executed. No new runtime may proceed without a separately
  healthy authorized precondition. No timeout/retry/skip/oracle relaxation here.

## Review and integration

Independent review and exact branch CI must be attached to the Draft PR. No
automatic merge: candidate location and original assertions still require real
Android execution once health prerequisites exist. The old P1-G runner remains
for its frozen four APKs only; these rebuilt Host APKs are not substitutes.
Product source, JNI/pins, dictionaries, settings, signing and published Alpha
are unchanged. Rollback is normal revert of this isolated test-only slice.

Fresh commands: `:acceptance-host:testDebugUnitTest`,
`:acceptance-host:assembleDebug`, `:acceptance-host:assembleDebugAndroidTest`,
`python3 -m unittest discover -s scripts/tests -v`, all `scripts/check_*.py`,
`git diff --check`. Exact logs/XML stay in the local R3.4 evidence session.
