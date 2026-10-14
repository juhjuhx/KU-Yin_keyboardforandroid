# KU-Yin Desktop Voice V1 design (2026-10-14)

## Product contract
Deliver a Windows 10/11 x64 usable voice-input package, with Windows-native UI, global hotkey, offline Whisper, opt-in OpenAI-compatible transcription, Taiwan Traditional Chinese conversion, and downloadable CI build. Deliver actual TSF keyboard IME through separately maintained Rime Weasel. Do not describe the voice service itself as TSF.

## Separation
- Weasel rime/weasel (GPL-3.0): installed TSF text service and zhuyin composing/candidate UI.
- KU-Yin Voice: local microphone recording, local whisper.cpp or explicit opt-in HTTP transcription, foreground HWND guard and Win32 Unicode SendInput.
- LSS: match AsrProvider conceptual contract; local LSS code was not accessible, so no direct source reuse.
- KU-Yin Android LGPL-2.1: unchanged in this branch; future microphone gate via IME editor policy and AudioRecord, offline default.

## Acceptance
- CI publishes Windows x64 executable bundle built with source-pinned whisper.cpp, complete runtime and offline ggml-base.bin model.
- Default transcription and microphone stay local. API audio upload requires explicit provider selection. No telemetry, clipboard harvesting or persisted transcripts.
- No silent installation of Weasel or COM registration. Preserve upstream licensing and corresponding source availability.
- Foreground HWND mismatch suppresses typing, hotkey collision errors clearly, UIPI is respected. Password fields cannot be reliably detected by this global helper: never use speech in sensitive fields until proper TSF integration adds editor-aware policy.
- Unit/contract tests, Windows CI build, package smoke test. Hardware mic/RDP/editor acceptance remains open until demonstrated.

## Safety & upstream
Track Weasel and fcitx5-vinput version/commit in an upstream manifest. Pinned source ref and checksums. Review upstream changes before advancing pins. Avoid copying GPL-3.0 source into LGPL KU-Yin Android without legal review. Direct GitHub fork API is unavailable through the current connector; an upstream tracking manifest is not an actual fork.

## Stages
G0 provenance; G1 executable package; G2 real mic and editor runtime; G3 TSF native direct integration; G4 Android isolated offline voice; G5 shared LSS provider interface and repeatable CPU/Vulkan measurements.
