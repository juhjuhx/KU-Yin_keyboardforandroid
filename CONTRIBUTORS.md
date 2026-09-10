# Contributors and Acknowledgements

KU-Yin follows a simple attribution rule: material contribution is credited even when later work corrects, replaces, or substantially refactors it. Credit is a record of participation; it is not a statement that every earlier implementation was correct, shippable, or retained unchanged.

## Project owner and maintainer

- **juhjuhx** — project owner, product direction, testing, repository stewardship, integration decisions, open-source release decisions, and final acceptance.

## AI-assisted development ledger

The project has used multiple AI coding systems during development. They are listed here as development tools/models, not as human GitHub identities, legal authors, or copyright owners.

| Tool / orchestration | Model | Contribution area | Status |
|---|---|---|---|
| **OpenCode** | **Mules Spark 1.3** | Early project scaffolding, implementation exploration and documentation | Credited for early-stage contribution; portions were later corrected/reworked |
| **Hermes** | **Agnes 2.0 Dash Flash** | Early architecture, Android/JNI implementation work, audit/status documents and build attempts | Credited for early-stage contribution; the resulting early APK baseline was not usable and is being recovered |
| **ChatGPT** | **GPT-5.6 Sol** | Forensic review, P0–P4 recovery design, TDD regression work, Android IME repair, native reproducibility, CI/security hardening, architecture/roadmap/README recovery | Recovery pass begun 2026-09-09 |

### Attribution notes

- AI systems are credited because their output materially influenced the repository.
- This ledger intentionally preserves credit for work that was later found incorrect or incomplete.
- A model/tool entry does not imply endorsement by that model provider.
- No AI system is represented as a GitHub account or human maintainer.
- Commit history remains the authoritative record of repository changes.

## Upstream projects and technical foundations

KU-Yin is possible because of the following open-source projects and references. Exact versions/pins and reuse boundaries are tracked in [`docs/UPSTREAM.md`](docs/UPSTREAM.md) and `NOTICE`.

- **libchewing** — Chewing intelligent Zhuyin engine and C API; core decoding and candidate logic.
- **libchewing-data** — system dictionary/source data used to build Chewing dictionary assets.
- **fcitx5-chewing** — reference integration for Chewing behavior, layout handling and lifecycle expectations.
- **fcitx5-android** — Android input-method architecture and integration reference used during early research/mirroring.
- **OpenCC** — intended Traditional/Simplified conversion foundation. KU-Yin does not claim working OpenCC conversion until a real tested backend is present.
- **Android Open Source Project / Android Developers documentation** — `InputMethodService`, `InputConnection`, IME lifecycle and Android platform behavior references.
- **Gradle / Android Gradle Plugin / Kotlin** — build toolchain.

## How to add a contributor

A contribution can be code, testing, documentation, architecture/research, design, release engineering, localization, accessibility work, or reproducible bug investigation. Add human contributors only with an identity they use publicly for this project. Add AI-assisted work under the AI ledger with the tool/model actually used and a bounded description of its contribution.
