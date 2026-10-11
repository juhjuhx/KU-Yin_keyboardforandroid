# ADR 0004 — Document source of truth

- Status: accepted
- Context: 10+ docs claimed overlapping authority; toolchain/version/architecture drifted.
- Decision: hierarchy AGENTS.md > CURRENT_STATE > ARCHITECTURE > DEVELOPMENT >
  SECURITY.md > SECURITY_AUDIT > UPSTREAM > ROADMAP(future) > HISTORY > COMPOSE-MAINLINE.
- Consequences: conflicts resolve by order; `check_docs_consistency.py` pins it.
- Alternatives rejected: README-as-authority (landing page, not spec).
- Evidence: provenance audit `docs/review/ARCHITECTURE-DOCUMENT-PROVENANCE.md`.
