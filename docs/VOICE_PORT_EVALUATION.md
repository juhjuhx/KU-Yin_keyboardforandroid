# Voice input: Windows implementation and KU-Yin Android integration assessment

## Source & licensing

- Reference behaviour: https://github.com/xifan2333/fcitx5-vinput (GPL-3.0; Linux Fcitx5 addon, PipeWire capture, sherpa-onnx/cloud ASR, LLM command scenes, GUI/CLI).
- Windows baseline alternative: https://github.com/gavinkao98/ASR (MIT; Windows Traditional Chinese offline voice input; evaluate its tested hotkey lifecycle, VAD, and model support before replacing it).
- KU-Yin current base is LGPL-2.1 and an Android InputMethodService. Its `SECURITY.md` explicitly says no INTERNET and no clipboard scraping. Do not copy upstream GPL-3.0 code into the LGPL product without a reviewed licensing plan.
- Experimental `tools/windows-voice` uses a separate clean-room Python implementation and only requires `sounddevice`; `whisper.cpp` model/runtime is user-supplied. No GPL-3.0 upstream source has been embedded.

## Shared product contract (planned, not implemented on Android)

```
VoiceTrigger -> CaptureAdapter -> AudioFrame(PCM16/16kHz/mono)
             -> AsrBackend(local preferred) -> Transcript(text, language)
             -> TransformChain(offline punctuation, optional OpenCC, optional LLM)
             -> EditorCommitGuard -> PlatformCommit
```

Platform implementation:

| Capability | Windows | KU-Yin Android |
| --- | --- | --- |
| Trigger | RegisterHotKey Ctrl+Alt+Space | Compose microphone button (future) |
| Capture | PortAudio/sounddevice | AudioRecord with RECORD_AUDIO runtime permission (future) |
| ASR | whisper.cpp CLI offline (experimental implementation) | sherpa-onnx Android/JNI or tested Android-native offline ASR (future) |
| Commit | Win32 SendInput Unicode | Existing service `commitToEditor`, never UI-direct InputConnection |
| Safety | HWND focus and no clipboard | EditorPolicy sensitivity/forceAscii, focus epoch, lifecycle & composition ACK |

### Android stop gates

1. Explicit record permission with a dedicated activity; `InputMethodService` must not call Activity `requestPermissions` directly. Reject/disable voice for sensitive or password editors, FORCE_ASCII, missing input connection, `editorWriteFailure`, and when no valid focus/editor epoch exists.
2. Microphone use must be visible with a clear stop control; stop immediately on `onFinishInputView`, `onFinishInput`, `onDestroy` and permission revoke. Avoid a background or persistent recorder.
3. Do not run ASR or model IO on the main IME/UI thread. Keep audio in memory where possible; no audio/transcript logs, analytics, clipboard, external storage, or cloud by default.
4. `ComposeDecoderSession` remains the composition owner. Before a transcript is committed, require no pending composition (or explicit safe completion/ACK). Only `KuYinInputMethodService` executes editor effects; never auto-replay an unacknowledged commit.
5. Model licensing, Android ABI, disk footprint, CPU/RAM/battery benchmarks, 16k audio backend and reproducible model checksums require independent review. Do not add `INTERNET` solely to make voice easy.
6. New feature is opt-in behind a feature flag until JVM/host/device tests pass. Current Alpha should not advertise working voice input.

## Android acceptance plan

- Regression: ensure S0/S1 candidate browsing, native chewing, English suggestions, editor ACK, Dachen geometry, keyboard modes unchanged.
- New unit tests: deny sensitive/FORCE_ASCII contexts, obsolete editor epoch drop, cancellation/permission revocation, empty transcript, composition ACK and exactly-once submission policy.
- Instrumentation: mic permission grant/deny, onFinishInputView mid-record, editor switching during ASR, orientation rebuild, phone/OEM and TalkBack. Confirm on-device network is not used and no audio remnants survive.
- Release is blocked until those tests, APK size, offline model provisioning, model provenance, security review, physical-device verification, and signing succeed.

## Completion status

- Windows CLI **source prototype** + offline backend + deterministic unit tests: present. NOT Windows hardware-validated, no packaged EXE.
- KU-Yin Android adapter: **architecture assessed only**, no modified Android runtime/UI, no APK. This deliberately preserves the existing security and IME behaviour until the device gates are available.