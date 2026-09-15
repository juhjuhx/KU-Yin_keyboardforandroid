# KU-Yin Privacy Policy (draft for review)

> Draft status: this document describes the behavior of the current KU-Yin
> source tree as verified by code inspection. It is intended as the future
> canonical privacy-policy page (e.g. GitHub Pages) and as the factual basis
> for a Google Play Data Safety declaration. Do not publish it as final until
> physical-device acceptance and a privacy review of the release build pass.

## Scope

KU-Yin Keyboard for Android (`feat/keyboard-shell-v0.2` line; application ID
under review, see release-readiness notes). This policy covers the IME
application itself. It does not cover the Android operating system, the
system keyboard switcher, or any other application.

## Data processed on device

To convert keystrokes into text, the running IME necessarily processes, in
 volatile memory only:

- keys pressed while the KU-Yin input view is active;
- the active composing (preedit) text and the current candidate list;
- the surrounding editor text exposed by Android through `InputConnection`
  (required to place composing spans and to reconcile cursor/selection
  changes in `onUpdateSelection`).

This processing happens entirely on the device. Verified in
`ChewingInputMethodService.kt` (`applyEngineUpdate`, `commitCandidate`,
`onUpdateSelection`) and `AndroidChewingEngine.kt` (libchewing JNI calls).

## Data stored on device

- **libchewing system dictionaries** (`tsi.dat`, `word.dat`, `swkb.dat`,
  `symbols.dat`): read-only assets copied at first run into the
  app-private, non-backed-up directory (`noBackupFilesDir/libchewing`),
  see `LibChewingDataInstaller.kt`. Identical for every installation.
- **libchewing user dictionary** (`userdict.dat`): learned phrases stored in
  the same app-private, non-backed-up directory. Never leaves the device
  through any KU-Yin code path.
- **Keyboard preferences** (input mode, haptics, keyboard height, candidate
  presentation, bottom-row profile): stored in the app-private default
  `SharedPreferences` file, see `KeyboardPreferencesRepository.kt`. Contains
  no keystroke or text content.
- **Emoji recents** (planned, M4): small bounded list of selected emoji
  sequences in app-private preferences. Local only.

Android backup is explicitly disabled (`android:allowBackup="false"` in
`AndroidManifest.xml`), so none of the above is uploaded to cloud backup by
the OS on KU-Yin's behalf.

## Data transmitted off device

**None.** Verified:

- `AndroidManifest.xml` requests no `android.permission.INTERNET` (only the
  mandatory `android.permission.BIND_INPUT_METHOD` for the IME service).
- No networking libraries exist in `app/build.gradle.kts` (AndroidX
  core/appcompat/material/constraintlayout/preference and test libraries
  only) and no network code paths exist in `app/src/main/`.
- No telemetry, analytics, crash-reporting, Firebase, or update-check code
  exists in the source tree.
- No clipboard collection feature exists.

## Data shared with third parties

**None.** There is no third-party SDK in the application. Upstream
components (libchewing dictionaries, AndroidX libraries) are bundled at
build time and perform no runtime communication.

## Sensitive editors

For password fields, `FORCE_ASCII` editors, and editors that declare
`IME_FLAG_NO_PERSONALIZED_LEARNING`, KU-Yin switches to a safe ASCII/English
session: no Chinese composition, no candidate strip from the previous editor,
and personalized learning is disabled for the session. See `EditorPolicy.kt`
and `ImeSessionController.kt`.

## Data Safety mapping (Google Play, draft)

| Data Safety question | Draft answer | Code evidence |
|---|---|---|
| Does the app collect or share any user data? | No data collected, no data shared | No INTERNET permission; no network/telemetry/clipboard code; app-private storage only |
| Is all user data encrypted in transit? | Not applicable (no transmission) | Same as above |
| Account deletion / data deletion | In-app data (user dictionary, preferences, recents) is removed when the app is uninstalled; reset-to-default clears shell preferences without touching the user dictionary | `KeyboardPreferencesRepository.resetKeyboardShellPreferences()`; Android uninstall semantics |

## Limitations of this draft

- Statements about *absence* of behavior are bounded by the reviewed source
  revision; a release build should re-verify (including the signed release
  artifact, ProGuard/R8 mapping if ever enabled, and any new dependency).
- Rooted or compromised devices are outside this threat model.
- The user dictionary is app-private but not additionally encrypted at the
  application layer.
- This document is not legal advice.
