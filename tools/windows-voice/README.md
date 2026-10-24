# KU-Yin Windows Voice (experimental, offline)

**Scope**: a separately runnable Windows 10/11 voice-to-text prototype. It does not modify the Android IME, install an input method, or depend on Fcitx5. This is a clean-room implementation of generic dictation behaviour; it does not copy GPL-3.0 fcitx5-vinput code.

## Set up on Windows 10 22H2

1. Install Python 3.11 and a current [whisper.cpp](https://github.com/ggml-org/whisper.cpp) Windows `whisper-cli.exe` build, plus a multilingual **ggml** model (for Chinese choose `small`, `medium`, etc.; not an English-only `.en` model). Keep downloaded binaries and their model license/provenance separately. All inference runs on the host machine. Windows AV and download provenance should be checked before running any executable.
2. In PowerShell, from this directory, set the paths (replace examples with actual paths):

   ```powershell
   $env:KUYIN_WHISPER_CLI='C:\tools\whisper.cpp\build\bin\Release\whisper-cli.exe'
   $env:KUYIN_WHISPER_MODEL='C:\models\ggml-small.bin'
   py -3.11 -m venv .venv
   .\.venv\Scripts\python.exe -m pip install -r requirements.txt
   .\.venv\Scripts\python.exe winvoice.py --traditional
   ```

   For subsequent runs in the same environment, launch `start-windows.cmd`.
3. Focus a **non-sensitive** text box. Press `Ctrl+Alt+Space` to begin capturing speech, then press it again to transcribe and inject text. Maximum recording length: 30 seconds. Press `Ctrl+C` in the terminal to exit.

## Behaviour and limits

- Default: local offline `whisper-cli` model, 16 kHz mono PCM16, no cloud, no API key, no conversation history. Temporary WAV/text files are deleted on completion. Console messages do not print recognized text when using global dictation.
- Windows injection uses `SendInput(KEYEVENTF_UNICODE)` without clipboard access. It works with many normal Windows edit fields, **but not all applications, elevated windows, secure fields or remote desktops**. It does not claim TSF/IME integration or guaranteed delivery. Keep the target window in focus until it finishes transcribing. The focused HWND is checked again before injection.
- This starter offers *toggle* recording rather than Linux vinput's modifier-key push-to-talk. No GUI/tray yet, and no LLM command mode. Do not bind to a modifier-only hotkey: `RegisterHotKey` needs a non-modifier key.
- `--traditional` runs optional OpenCC `s2twp` post-processing; without that flag the recognizer may produce Simplified Chinese. OpenCC conversion can alter named entities and technical words; proofread important text.
- `whisper.cpp` can run on CPU; Arc GPU offload needs separate backend benchmarking. Nothing here assumes CUDA. The model is not bundled; speed and precision are not validated on the user's A770 hardware.
- Audio is present in a user temp directory during transcription and is removed afterward. Abnormal termination may leave temp WAV files; inspect `%TEMP%\kuyin-voice-*` if necessary. This design is not yet approved for confidential voice dictation.

## Smoke checks

```powershell
.\.venv\Scripts\python.exe -m unittest discover -s tests -v
.\.venv\Scripts\python.exe winvoice.py --whisper-cli 'C:\path\whisper-cli.exe' --model 'C:\path\ggml-small.bin' --wav 'C:\path\16k.wav'
```

The Python tests exercise pure contracts; they cannot validate a live microphone or Windows key injection outside Windows. Validate with Notepad (Chinese, emoji), mixed Chinese/English, zero speech, denied microphone permission, focus changes, an elevated app, 30-second auto-stop, and no network connection before distribution.