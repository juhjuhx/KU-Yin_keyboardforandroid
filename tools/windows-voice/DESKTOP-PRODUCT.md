# KU-Yin Desktop (Windows 10/11 x64)

## Real offline product package
The CI artifact is a self-contained, extracted-once Windows package:
- KU-Yin-Voice.exe: standalone Windows voice input with GUI, tray and system-wide Ctrl+Alt+Space hotkey.
- whisper-cli.exe, its dynamic libraries, and an offline ggml-base multilingual model, compiled/downloading from pinned upstream during GitHub Actions packaging.
- Weasel 0.17.4.0 upstream installer: authentic Windows TSF zhuyin keyboard. Weasel is maintained and licensed separately; this is not a fork or modification of Weasel.
- One-click user-profile installer. No external Python runtime, token or online transcription required after download.

Unzip all files and run Install.cmd. It installs the voice app to your Windows user profile, offers to run the **official Weasel installer** and launches voice settings. In Weasel's installer select Chinese (Taiwan); switch using Win+Space. For dictation in Notepad/most normal editor windows press Ctrl+Alt+Space, speak, press again, and wait for local transcription. If the focused window changed, the transcript is discarded.

## Provider routing
Default: offline whisper.cpp CPU backend, packaged. CPU is the guaranteed build target; optional A770 Vulkan acceleration remains a separate measured build gate, NOT yet shipped.
Optional API: select api in the GUI, enter HTTPS OpenAI-compatible /v1/audio/transcriptions endpoint, API model, and an optional session-only API token. Clear-text HTTP is allowed only to loopback addresses. An endpoint configured for LSS or a locally running transcription service can be used, but LSS code integration is NOT part of this branch.
The API key is never stored in settings or transmitted in telemetry. It is held in process environment memory until exit. The API path transfers your recording and should only be used with a provider you trust.

## Important boundaries
This voice app injects Unicode using Windows SendInput; it is a companion to the **genuine Weasel TSF keyboard**, not itself a COM text-service DLL. Global shortcuts cannot reliably detect a password field. Do not use it in passwords, banking fields or elevated applications; Windows UIPI also blocks elevated app injection. It does not read the clipboard or persist transcriptions. No speech leaves the computer unless you explicitly choose API mode.

## Provenance
- rime/weasel 0.17.4 installer SHA256 cf509534a8f5f8af9c98ed7cbb8f135439f145a8cbe7e50ede42bb5b5ab45c29, GPLv3: https://github.com/rime/weasel/releases/tag/0.17.4 ; corresponding source https://github.com/rime/weasel/tree/0.17.4
- ggml-org/whisper.cpp v1.9.5, MIT: https://github.com/ggml-org/whisper.cpp/tree/v1.9.5
- base multilingual model source: https://huggingface.co/ggerganov/whisper.cpp ; sha1 465707469ff3a37a2b9b8d8f89f2f99de7299dac (published in whisper.cpp models/README.md).
- fcitx5-vinput, GPLv3: https://github.com/xifan2333/fcitx5-vinput ; design reference only, no copied C++ files or executable.
- KU-Yin Android, LGPLv2.1: unchanged by this product branch.

Windows code signing, physical mic/target-app tests, RDP cases, native TSF voice commit and independent security audit remain open. CI package and contract tests cannot certify all of these.
