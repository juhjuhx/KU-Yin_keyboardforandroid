"""KU-Yin Windows Voice (experimental): private offline dictation via whisper.cpp.

Clean-room implementation inspired by the *behaviour* of fcitx5-vinput.
No source code or binary from fcitx5-vinput is incorporated here.
"""
from __future__ import annotations

import argparse
import ctypes
from ctypes import wintypes
from dataclasses import dataclass
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import threading
import wave

HOTKEY_ID = 0x4B59
WM_HOTKEY = 0x0312
MOD_CONTROL = 0x0002
MOD_ALT = 0x0001
VK_SPACE = 0x20
KEYEVENTF_KEYUP = 0x0002
KEYEVENTF_UNICODE = 0x0004
INPUT_KEYBOARD = 1


@dataclass(frozen=True)
class Config:
    cli: Path
    model: Path
    language: str = "zh"
    sample_rate: int = 16000
    max_seconds: int = 30
    provider: str = "offline"
    api_url: str = ""
    api_model: str = "whisper-1"
    api_key_env: str = "KUYIN_VOICE_API_KEY"

    def validate(self) -> None:
        if self.provider == "api":
            from provider import validate_endpoint
            validate_endpoint(self.api_url)
            if not self.api_model:
                raise ValueError("ASR API model must not be empty")
            return
        if self.provider != "offline":
            raise ValueError("Unknown ASR provider")
        if not self.cli.is_file():
            raise FileNotFoundError(f"找不到 whisper-cli: {self.cli}")
        if not self.model.is_file():
            raise FileNotFoundError(f"找不到 Whisper 模型: {self.model}")
        if self.language not in {"zh", "auto", "en"}:
            raise ValueError("language 必須是 zh、auto 或 en")


def transcribe(wav_path: Path, config: Config, runner=subprocess.run) -> str:
    """Run a pinned local executable without invoking a shell or network."""
    config.validate()
    if config.provider == "api":
        from provider import ApiSettings, transcribe_api
        return transcribe_api(wav_path, ApiSettings(
            endpoint=config.api_url, model=config.api_model,
            api_key=os.environ.get(config.api_key_env, ""),
            language=config.language))
    with tempfile.TemporaryDirectory(prefix="kuyin-whisper-") as directory:
        stem = Path(directory) / "transcript"
        argv = [str(config.cli), "-m", str(config.model), "-f", str(wav_path),
                "-l", config.language, "-otxt", "-of", str(stem), "-np"]
        completed = runner(argv, shell=False, capture_output=True, timeout=180, check=False)
        if completed.returncode != 0:
            raise RuntimeError(f"whisper-cli 退出碼 {completed.returncode}（不記錄原始語音文字）")
        output = stem.with_suffix(".txt")
        if not output.is_file():
            raise RuntimeError("whisper-cli 未產生 transcript.txt")
        return output.read_text(encoding="utf-8-sig").strip()


def to_traditional(text: str, enabled: bool) -> str:
    if not enabled or not text:
        return text
    try:
        from opencc import OpenCC
    except ImportError as exc:
        raise RuntimeError("繁體轉換需要 pip install opencc-python-reimplemented") from exc
    return OpenCC("s2twp").convert(text)


class Recorder:
    """Records to a disposable 16 kHz mono signed-16-bit WAV."""
    def __init__(self, sample_rate: int = 16000):
        self.sample_rate = sample_rate
        self._stream = None
        self._writer = None
        self._lock = threading.Lock()

    def start(self, path: Path) -> None:
        import sounddevice as sd  # lazy: pure logic tests run without audio hardware
        with self._lock:
            if self._stream is not None:
                raise RuntimeError("錄音已開始")
            writer = wave.open(str(path), "wb")
            writer.setnchannels(1)
            writer.setsampwidth(2)
            writer.setframerate(self.sample_rate)
            self._writer = writer

        def callback(indata, frames, time_info, status):
            del frames, time_info
            if status:
                # No audio content is logged; callback remains intentionally minimal.
                pass
            with self._lock:
                if self._writer is not None:
                    self._writer.writeframesraw(bytes(indata))
        try:
            stream = sd.RawInputStream(samplerate=self.sample_rate, channels=1,
                                       dtype="int16", callback=callback)
            stream.start()
            with self._lock:
                self._stream = stream
        except BaseException:
            with self._lock:
                self._writer.close()
                self._writer = None
            raise

    def stop(self) -> None:
        with self._lock:
            stream = self._stream
        if stream is None:
            return
        try:
            stream.stop()
            stream.close()
        finally:
            with self._lock:
                self._stream = None
                writer = self._writer
                self._writer = None
                if writer is not None:
                    writer.close()


class MOUSEINPUT(ctypes.Structure):
    _fields_ = [("dx", wintypes.LONG), ("dy", wintypes.LONG),
                ("mouseData", wintypes.DWORD), ("dwFlags", wintypes.DWORD),
                ("time", wintypes.DWORD), ("dwExtraInfo", ctypes.c_size_t)]


class KEYBDINPUT(ctypes.Structure):
    _fields_ = [("wVk", wintypes.WORD), ("wScan", wintypes.WORD),
                ("dwFlags", wintypes.DWORD), ("time", wintypes.DWORD),
                ("dwExtraInfo", ctypes.c_size_t)]


class HARDWAREINPUT(ctypes.Structure):
    _fields_ = [("uMsg", wintypes.DWORD), ("wParamL", wintypes.WORD),
                ("wParamH", wintypes.WORD)]


class INPUT_UNION(ctypes.Union):
    _fields_ = [("mi", MOUSEINPUT), ("ki", KEYBDINPUT), ("hi", HARDWAREINPUT)]


class INPUT(ctypes.Structure):
    _fields_ = [("type", wintypes.DWORD), ("data", INPUT_UNION)]


def unicode_inputs(text: str):
    """One Win32 KEYEVENTF_UNICODE press/release per UTF-16 code unit."""
    result = []
    utf16 = text.encode("utf-16-le", errors="surrogatepass")
    for i in range(0, len(utf16), 2):
        code = int.from_bytes(utf16[i:i + 2], "little")
        for flags in (KEYEVENTF_UNICODE, KEYEVENTF_UNICODE | KEYEVENTF_KEYUP):
            result.append(INPUT(type=INPUT_KEYBOARD, data=INPUT_UNION(
                ki=KEYBDINPUT(wVk=0, wScan=code, dwFlags=flags, time=0, dwExtraInfo=0))))
    return result


def type_unicode(text: str) -> None:
    if sys.platform != "win32":
        raise OSError("文字注入僅支援 Windows")
    if not text:
        return
    # SendInput is scoped by Windows UIPI; it cannot inject into elevated apps.
    user32 = ctypes.WinDLL("user32", use_last_error=True)
    user32.SendInput.argtypes = (wintypes.UINT, ctypes.POINTER(INPUT), ctypes.c_int)
    user32.SendInput.restype = wintypes.UINT
    events = unicode_inputs(text)
    values = (INPUT * len(events))(*events)
    sent = user32.SendInput(len(events), values, ctypes.sizeof(INPUT))
    if sent != len(events):
        raise OSError("無法送入文字：確認目標視窗權限與焦點（UIPI）")


class VoiceApp:
    def __init__(self, config: Config, traditional: bool, on_status=None):
        self.config = config
        self.traditional = traditional
        self.on_status = on_status
        self._message_thread_id = None
        self.recorder = Recorder(config.sample_rate)
        self.tmp = None
        self.wav = None
        self.recording = False
        self.processing = False
        self._lock = threading.Lock()
        self._timer = None
        self.user32 = ctypes.WinDLL("user32", use_last_error=True)
        self.user32.GetForegroundWindow.restype = wintypes.HWND

    def _emit(self, status: str):
        if self.on_status is not None:
            self.on_status(status)
        else:
            print(status)

    def toggle(self):
        with self._lock:
            if self.processing:
                self._emit("辨識中，暫不接受下一次錄音")
                return
            if self.recording:
                self.recording = False
                self.processing = True
                is_stop = True
            else:
                self.recording = True
                is_stop = False
        if is_stop:
            self._finish_async()
        else:
            self.tmp = tempfile.TemporaryDirectory(prefix="kuyin-voice-")
            self.wav = Path(self.tmp.name) / "capture.wav"
            try:
                self.recorder.start(self.wav)
                self._emit("🎙 錄音中。再次按 Ctrl+Alt+Space 轉錄")
                self._timer = threading.Timer(self.config.max_seconds, self._auto_stop)
                self._timer.daemon = True
                self._timer.start()
            except BaseException as exc:
                self.tmp.cleanup()
                self.tmp = None
                with self._lock:
                    self.recording = False
                self._emit(f"錄音失敗: {type(exc).__name__}: {exc}")

    def _auto_stop(self):
        with self._lock:
            should_stop = self.recording
        if should_stop:
            self.toggle()

    def _finish_async(self):
        if self._timer is not None:
            self._timer.cancel()
        # Remember the editor at the end of recording; never send to a new focus target.
        target = self.user32.GetForegroundWindow()
        def worker():
            try:
                self.recorder.stop()
                text = transcribe(self.wav, self.config)
                text = to_traditional(text, self.traditional)
                if not text:
                    self._emit("沒有辨識到文字")
                elif target and self.user32.GetForegroundWindow() == target:
                    type_unicode(text)
                    self._emit("✓ 已輸入（無文字紀錄）")
                else:
                    self._emit("焦點已變更：為避免輸入到錯誤視窗，已捨棄結果")
            except Exception as exc:
                self._emit(f"處理失敗: {type(exc).__name__}: {exc}")
            finally:
                self.tmp.cleanup()
                self.tmp = None
                with self._lock:
                    self.processing = False
        threading.Thread(target=worker, daemon=True).start()

    def stop(self):
        if self._message_thread_id:
            self.user32.PostThreadMessageW(self._message_thread_id, 0x0012, 0, 0)

    def run(self):
        self.config.validate()
        self._message_thread_id = ctypes.windll.kernel32.GetCurrentThreadId()
        self.user32.RegisterHotKey.argtypes = (wintypes.HWND, ctypes.c_int,
                                                wintypes.UINT, wintypes.UINT)
        self.user32.RegisterHotKey.restype = wintypes.BOOL
        if not self.user32.RegisterHotKey(None, HOTKEY_ID, MOD_CONTROL | MOD_ALT, VK_SPACE):
            raise OSError("Ctrl+Alt+Space 熱鍵註冊失敗，可能已被其他程式占用")
        self._emit("KU-Yin Voice for Windows：Ctrl+Alt+Space 開始／停止")
        msg = wintypes.MSG()
        try:
            while self.user32.GetMessageW(ctypes.byref(msg), None, 0, 0) > 0:
                if msg.message == WM_HOTKEY and msg.wParam == HOTKEY_ID:
                    self.toggle()
        finally:
            self.user32.UnregisterHotKey(None, HOTKEY_ID)
            if self._timer:
                self._timer.cancel()
            self.recorder.stop()
            if self.tmp is not None:
                self.tmp.cleanup()


def main(argv=None):
    parser = argparse.ArgumentParser(description="KU-Yin Windows Offline Voice (experimental)")
    parser.add_argument("--whisper-cli", default=os.environ.get("KUYIN_WHISPER_CLI", "whisper-cli.exe"))
    parser.add_argument("--model", default=os.environ.get("KUYIN_WHISPER_MODEL", ""))
    parser.add_argument("--language", default="zh", choices=["zh", "en", "auto"])
    parser.add_argument("--traditional", action="store_true", help="使用 OpenCC 轉臺灣繁體中文")
    parser.add_argument("--provider", choices=["offline", "api"], default="offline")
    parser.add_argument("--api-url", default="", help="Explicit opt-in transcription endpoint")
    parser.add_argument("--api-model", default="whisper-1")
    parser.add_argument("--wav", type=Path, help="僅轉錄現有 16k/16bit/mono WAV，不啟用熱鍵")
    args = parser.parse_args(argv)
    config = Config(cli=Path(args.whisper_cli).expanduser().resolve(),
                    model=Path(args.model).expanduser().resolve(), language=args.language,
                    provider=args.provider, api_url=args.api_url, api_model=args.api_model)
    if args.wav:
        print(to_traditional(transcribe(args.wav, config), args.traditional))
        return
    if sys.platform != "win32":
        parser.error("全域錄音與文字輸入只支援 Windows；可在其他系統使用 --wav 驗證")
    VoiceApp(config, args.traditional).run()


if __name__ == "__main__":
    main()
