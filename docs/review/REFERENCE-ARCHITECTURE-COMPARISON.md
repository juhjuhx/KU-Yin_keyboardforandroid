# Reference-architecture comparison (research refs, NOT dependencies)

| Project | Relevant pattern (borrowed as idea) | Irrelevant / warning |
|---|---|---|
| fcitx5-android | addon/plugin isolation; chewing+rime packaging; prebuilt static libs (our bootstrap mirrors this) | framework itself; do not import |
| HeliBoard | AOSP-descendant lattice: gesture/settings structure study target | code copy needs file-level license review first (not done) |
| FlorisBoard | smartbar concept; emojicon resource idea; Kotlin IME structure | gestures/LM scope beyond us; no copy |
| AOSP LatinIME | typing/scoring/weighting/proximity structure; suggestion strip; touch model | Java legacy paths; Apache-2.0 only with provenance |
| libchewing (pinned a6a8fa4) | THE backend: preedit/candidates/learn/userdict/symbols | desktop-only controls excluded from mobile UX |

Standing rule (AGENTS.md): no reference-keyboard copy without
license/provenance review + compatibility justification. Nothing copied to date
(COMPOSE-MAINLINE: "無 donor-copy 引入").
