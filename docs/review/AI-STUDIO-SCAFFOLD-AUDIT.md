# AI-Studio scaffold audit (2026-09-23, verified against build file + history)

Method: every commented dependency in `app/build.gradle.kts` classified by
provenance (AI-Studio scaffold era per HISTORY.md §0) vs roadmap need.

| Commented dep | Verdict | Reason |
|---|---|---|
| accompanist.permissions | REMOVE (dead) | unused; zero-permission policy forbids its domain; roadmap has no permission feature |
| camera camera2/core/lifecycle/view (×4) | REMOVE (dead) | scaffold residue; no camera feature anywhere |
| datastore.preferences | REMOVE (dead) | prefs go via SharedPreferences/Room; no migration planned |
| navigation.compose | REMOVE (dead) | single-IME, no nav graph |
| coil.compose | REMOVE (dead) | no remote/local image loading (donation QR is hand-drawn Canvas) |
| play.services.location | REMOVE (dead) | location contradicts zero-data policy; roadmap absent |

Still present and intentional: accompanist none; `material`/`appcompat`/`preference`
active deps stay (settings UI). No React/Node/web scaffold remains in-tree
(metadata.json deleted earlier per COMPOSE-MAINLINE).

Current AI Studio relevance to KU-Yin: native Android + Kotlin + Compose track
only. Web/React/Node server tracks are explicitly irrelevant — no import.
