# Repository Consolidation v0.1.1-alpha Design

## Goal

Turn KU-Yin from a recovery-stage repository into a clean, single-mainline open-source Android IME project without changing core input behavior.

## Decisions

1. `main` is the only long-lived branch after consolidation.
2. `master` is historical and already fully contained by `main`; it may be deleted after the cleanup merge.
3. `fix/p0-p4-ime-recovery` has the same final tree content already represented by the squash merge on `main`; it may be deleted after the cleanup merge.
4. No Git-history rewrite. Preserve commit/audit history.
5. Keep the production architecture Kotlin/View + JNI/C++ + libchewing C API. Do not add a Rust layer to KU-Yin merely because modern libchewing upstream is Rust-heavy.
6. The public documentation surface is intentionally small: `README.md`, `CONTRIBUTING.md`, `SECURITY.md`, `CHANGELOG.md`, `LICENSE`, `NOTICE`, plus a focused `docs/` index.
7. Historical recovery/audit material is consolidated into `docs/HISTORY.md`; old point-in-time reports are removed or archived so they cannot conflict with current documentation.
8. Upstream provenance is authoritative in `docs/UPSTREAM.md` and `scripts/bootstrap_native_deps.sh`.
9. Upstream updates are automated as detection + validation PRs, never direct unverified writes to `main`.
10. v0.1.1-alpha remains a prerelease. Debug APK is directly testable; release APK remains unsigned until signing is established.

## Public documentation structure

```text
README.md
CONTRIBUTING.md
SECURITY.md
CHANGELOG.md
LICENSE
NOTICE

docs/
├── README.md
├── ARCHITECTURE.md
├── DEVELOPMENT.md
├── HISTORY.md
├── ROADMAP.md
├── SECURITY_AUDIT.md
├── UPSTREAM.md
├── i18n/
│   ├── README.en.md
│   └── README.zh-CN.md
└── archive/
    └── historical design/recovery records
```

## Architecture baseline

```text
Android framework / EditorInfo / InputConnection
        │
        ▼
ChewingInputMethodService
        │
        ├── EditorPolicy
        ├── ImeSessionController
        ├── KeyboardView / CandidateView
        └── ChewingEngine contract
                │
                ▼
        AndroidChewingEngine
                │
                ▼
             JNI / C++
                │
                ▼
          libchewing C API
```

KU-Yin owns the Android/Kotlin and JNI adapter layers. libchewing remains an external upstream dependency.

## Upstream update model

A scheduled GitHub Actions workflow checks:
- `fcitx5-android/prebuilt` master SHA against the pinned SHA in `scripts/bootstrap_native_deps.sh`;
- current libchewing release information for visibility/audit.

If the prebuilt SHA changes, the workflow creates or refreshes an update issue/PR rather than modifying `main` directly. Any actual pin update must pass native bootstrap, JVM tests, Debug APK, and Release APK builds before merge.

## Security baseline

- No cloud/network typing path is required for current decoding.
- No plaintext keystroke logging is permitted.
- Android backup remains disabled.
- Native user data remains app-private.
- Sensitive editors and `IME_FLAG_NO_PERSONALIZED_LEARNING` remain policy-controlled.
- Runtime/device validation is separate from build success.

## Release baseline

`v0.1.1-alpha` increments `versionCode` to 2 and `versionName` to `0.1.1-alpha`. The prerelease includes Debug APK, unsigned Release APK, and SHA-256 checksums. Stable signing is explicitly deferred.

## About recommendation

Description:
`Local-first open-source Zhuyin (Bopomofo) IME for Android, powered by libchewing.`

Topics:
`android`, `ime`, `keyboard`, `zhuyin`, `bopomofo`, `traditional-chinese`, `libchewing`, `kotlin`, `jni`, `local-first`, `taiwan`, `open-source`
