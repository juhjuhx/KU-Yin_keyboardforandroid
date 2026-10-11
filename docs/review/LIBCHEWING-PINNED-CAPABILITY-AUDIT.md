# libchewing pinned-capability audit (pin a6a8fa4)

Basis: 24 JNIEXPORTs in `app/src/main/cpp/chewing_jni.cpp` + every call site in
`AndroidChewingEngine.kt` (both read in full). Upstream headers are NOT in this
checkout (`.deps` absent, no network fetch performed) — anything about upstream
beyond the wrapped surface is marked NOT VERIFIED.

## ALREADY WIRED (Kotlin → JNI 1:1)

lifecycle: chewing_new2 / chewing_delete / chewing_reset · input:
chewing_handle_default / chewing_handle_backspace · read: buffer_string_static,
buffer_check, bopomofo_string_static, bopomofo_check, cursor_current,
commit_check, commit_string_static · candidates: cand_open, cand_total_choice,
cand_total_page, cand_choice_per_page, cand_string_by_index_static,
cand_choose_by_index · commit: commit_preedit_buf · config: set_kb_type (Dachen
only), set_chi_eng_mode, set_shape_mode, set/get_auto_learn.

## JNI AVAILABLE BUT NOT PRODUCTION

None — every wrapped symbol is reachable from session paths or guards
(isReady/ctx==0 fallbacks on all 12 update paths).

## UPSTREAM AVAILABLE / JNI MISSING (verify against .deps headers on SDK box)

- Symbol selection / Easy Symbol Input: NOT VERIFIED (no wrapper; G1 research item).
- Cursor-position set / segment cursor correction: NOT VERIFIED.
- Phrase memorization beyond auto-learn flag: NOT VERIFIED.
- Anything else: NOT VERIFIED — do not assume from libchewing HEAD; the repo
  pins a6a8fa4, audit that tree only (`scripts/bootstrap_native_deps.sh` fetches it).

## NOT SUPPORTED (by policy, not capability)

Desktop-only interactions (Tab-break, physical-keyboard-only flows) are out of
scope for the Android mobile UX regardless of upstream support.

## D3 NATIVE RISK REGISTER

| Risk | Trigger | Current mitigation | Required D3 test |
|---|---|---|---|
| `chewing_commit_string_static` lifetime | commit read after further calls | read immediately in snapshot() | CASE-style commit-then-continue test on real engine |
| Page index drift | native page count changes mid-session | candidatePage reset on key/commit/backspace | paging-vs-native-count alignment test |
| `cand_choose_by_index` global vs page index | stale page click | globalIndex computed; stale index → unconsumed | stale-click-on-page-2 test |
| auto-learn double ranking | Zhuyin prefs + libchewing learn both on | none yet (B8 item) | single-ranking invariant test |
| symbols.dat has no wrapper | M4 symbols want native data | none (research item) | G1 decision record first |
