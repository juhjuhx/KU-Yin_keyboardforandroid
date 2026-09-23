# ADR 0002 — Decoder session state ownership

- Status: accepted
- Context: composition/candidates were owned by engine fields and read directly by UI/service.
- Decision: `ComposeDecoderSession` owns all Chinese state; `KuYinEngine` keeps
  mode/shift/policy; UI/service apply `DispatchResult` only.
- Consequences: Enter/Punctuation/Complete/mode/lifecycle all flow through session;
  enforced by `check_production_compose_contract.py`.
- Alternatives rejected: dual-source reads (ghost-state risk, proven by A0 audit).
- Evidence: `EnterCommandContractTest`, `PunctuationCommandContractTest`,
  `ModeSwitchContractTest` (16 tests).
