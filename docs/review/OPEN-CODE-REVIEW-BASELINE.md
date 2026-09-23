# OpenCodeReview baseline (RC2)

- OCR version: from `ocr --version` at runtime (record here on SDK box; this box: CLI present, LLM 401).
- Execution mode: **delegation** (`ocr delegate preview --format json --from e72acb3 --to 34a758b` + `ocr delegate rule --format json <8 paths>`). Host (agent) performed the review; OCR side made zero LLM calls.
- Reviewed range: e72acb3..34a758b, merge_base e72acb3. Reviewable 8 files (+568/-45 tracked by preview; git numstat says +568/-45 over 13 incl. tests/docs).
- Excluded by OCR default rules: 4 test files (default_path) + 1 md (unsupported_ext) — reviewed manually instead (R10: behavior-testing, negative cases, counts present).
- Rule groups applied: Kotlin group 1–9 (null-safety, conciseness, collections, coroutines, design, resources, perf, interop, immutability) + Python precision group.
- Full `ocr review`: BLOCKED — Anthropic 401 invalid x-api-key (proven twice: `ocr llm test`, full run session 9b0ed68f). Remediation: valid key via `ocr config provider`, then rerun or `--resume`.
- `ocr scan` full-repo: NOT RUN (same blocker; scan also requires LLM).
- Findings (host verdicts): Critical 0, High 0, Medium 0 open (MEDIUM-1 triplication fixed in 34a758b; R10 raw-branch gap closed with 2 tests). Accepted: none pending. Rejected: none. Unresolved: JVM/BUILD-gated items (need SDK).
- False positives filtered: none raised (no findings to filter).
