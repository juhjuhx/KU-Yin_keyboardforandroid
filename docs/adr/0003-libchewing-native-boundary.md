# ADR 0003 — libchewing native boundary

- Status: accepted
- Context: native decoder must be reachable without leaking JNI into UI/policy code.
- Decision: only `AndroidChewingEngine` calls JNI symbols; service owns exactly one
  instance (init once, close once); UI never references native symbols.
- Consequences: D3 switch = service wiring change, not UI rewrite.
- Alternatives rejected: per-key/per-session native contexts (lifetime risk);
  engine-package JNI mirror (boundary violation).
- Evidence: 24 JNIEXPORTs ↔ 24 externals 1:1; guarded init/reset/close paths.
