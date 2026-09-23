# ADR 0001 — Compose product mainline

- Status: accepted
- Context: View donor shell (PR #4) vs Compose rewrite.
- Decision: Compose UI is the product mainline; View shell stays donor-only reference.
- Consequences: all new UI work targets Compose; View code is never production.
- Alternatives rejected: dual-UI maintenance (ownership split risk).
- Evidence: `COMPOSE-MAINLINE.md`, product IME service renders Compose.
