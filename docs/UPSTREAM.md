# Upstream and Provenance

## Production dependencies

### libchewing

Official project: https://codeberg.org/chewing/libchewing

Role: Zhuyin decoding, composition, candidates, user dictionary and learning behavior. KU-Yin uses the C API through its own JNI adapter.

The current Android artifacts are not built from upstream HEAD at every build. They are staged from the pinned `fcitx5-android/prebuilt` repository below. The prebuilder records libchewing source commit:

`a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`

Modern official libchewing has moved to Codeberg and the 2026 0.12/0.13 series contains newer APIs. That does not make KU-Yin's pin silently float.

### fcitx5-android/prebuilt

Repository: https://github.com/fcitx5-android/prebuilt

Pinned commit:

`3587ba3355711f0aca50136e787719f6562676b8`

As of the 2026-09-10 audit this equals the repository's current master commit. KU-Yin consumes four ABI `libchewing_capi.a` archives, headers and Chewing dictionary assets from this immutable revision via `scripts/bootstrap_native_deps.sh`.

## Platform/toolchain foundations

- Android Open Source Project / Android Developers: `InputMethodService`, `InputConnection`, `EditorInfo` and IME lifecycle contracts.
- Android Gradle Plugin / Gradle / Kotlin: application and JVM build toolchain.
- Android NDK / CMake: JNI/native build.

## Reference-only research

The following projects may inform behavior, architecture or UX research. They are **not** automatically production dependencies and their source must not be copied without file-level license/provenance review:

- fcitx5-android: Android IME architecture and Chewing integration research.
- fcitx5-chewing: Chewing behavior/integration reference.
- FlorisBoard: Android keyboard state/theme/accessibility reference.
- HeliBoard: keyboard/editor-action/IME-switching behavior reference; treat source licensing carefully.
- AOSP LatinIME / platform IME code: Android behavior reference under its applicable upstream license.
- OpenCC: potential Traditional/Simplified conversion backend; KU-Yin currently does not claim a production OpenCC feature.

## Update policy

`.github/workflows/upstream-watch.yml` runs a detection workflow. It compares the pinned prebuilt revision with upstream and queries the official Codeberg libchewing release API. Changes create/update an issue containing exact refs and a validation checklist.

No automated watcher may update `main` directly. A dependency update PR must pass native bootstrap, JVM tests and both APK builds; ABI/API changes require explicit review.

## Licensing

KU-Yin repository license is LGPL-2.1. Each upstream keeps its own license and notices. `NOTICE` records provenance; it does not replace upstream license texts or legal review.

### P1-E 測試基礎設施

AndroidX UIAutomator `2.3.0`（Apache-2.0）僅供 acceptance-host instrumentation。
未複製外部鍵盤或 CTS 實作；依官方 platform API 與 CTS helper／polling 模式獨立撰寫。
精確上游 revision、逐檔授權與引用分類記錄於
`docs/review/2026-10-07-p1e/02-upstream-research.md`。

### AIE V1 研究（2026-10-11；沒有新增production依賴）

固定上游revision/逐檔license/dependency/cost/classification見 `research/UPSTREAM_REUSE_MATRIX.md`。Qingjian/HeliBoard GPL實作僅參考；librime BSD不代表其predict/schema/資料同授權；rime-prelude LGPL3另核。CLDR所讀annotations為Unicode-3.0，LDML出版物terms不同。DIRECT_REUSE_CANDIDATE仍需批准，沒有複製外部source/data或修改libchewing pin。
