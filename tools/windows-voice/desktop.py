"""KU-Yin Desktop Voice: Windows application frontend for real microphone dictation.

Global voice transcription intentionally lives outside Weasel: Weasel owns TSF.
"""
from __future__ import annotations

import json
import os
from pathlib import Path
import queue
import sys
import threading
import tkinter as tk
from tkinter import ttk
from tkinter import messagebox

from winvoice import Config, VoiceApp

PRODUCT = "KU-Yin Desktop Voice"
BASE = Path(sys.executable).resolve().parent if getattr(sys, "frozen", False) else Path(__file__).resolve().parent
DATA = Path(os.environ.get("LOCALAPPDATA", str(Path.home()))) / "KU-Yin" / "Voice"
SETTINGS_FILE = DATA / "settings.json"
ALLOWED_KEYS = {"provider", "api_url", "api_model", "language", "traditional", "cli", "model"}


def read_settings() -> dict:
    try:
        v = json.loads(SETTINGS_FILE.read_text(encoding="utf-8"))
        return {k: v[k] for k in ALLOWED_KEYS if k in v} if isinstance(v, dict) else {}
    except (OSError, ValueError):
        return {}


def save_settings(value: dict) -> None:
    DATA.mkdir(parents=True, exist_ok=True)
    # Do not persist the API key or recorded speech. Settings are non-sensitive.
    value = {k: value[k] for k in ALLOWED_KEYS if k in value}
    temporary = SETTINGS_FILE.with_suffix(".new")
    temporary.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding="utf-8")
    temporary.replace(SETTINGS_FILE)


class Desktop(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title(PRODUCT)
        self.geometry("615x490")
        self.minsize(540, 440)
        self.log_queue = queue.Queue()
        self.voice = None
        self.voice_thread = None
        self.tray = None
        self.closing = False
        saved = read_settings()
        self.provider = tk.StringVar(value=saved.get("provider", "offline"))
        self.cli = tk.StringVar(value=saved.get("cli", str(BASE / "whisper-cli.exe")))
        self.model = tk.StringVar(value=saved.get("model", str(BASE / "models" / "ggml-base.bin")))
        self.api_url = tk.StringVar(value=saved.get("api_url", "http://127.0.0.1:8080/v1/audio/transcriptions"))
        self.api_model = tk.StringVar(value=saved.get("api_model", "whisper-1"))
        self.api_key = tk.StringVar(value="")
        self.language = tk.StringVar(value=saved.get("language", "zh"))
        self.traditional = tk.BooleanVar(value=bool(saved.get("traditional", True)))
        self.status = tk.StringVar(value="未啟動；可設定模型後啟用")
        self._build()
        self.protocol("WM_DELETE_WINDOW", self._hide)
        self.after(150, self._poll)
        self._create_tray()
        if Path(self.cli.get()).exists() and Path(self.model.get()).exists():
            self.after(350, self.start)

    def _build(self):
        frame = ttk.Frame(self, padding=15)
        frame.pack(fill="both", expand=True)
        ttk.Label(frame, text="KU-Yin · Windows 語音輸入", font=("Segoe UI", 16, "bold")).grid(
            row=0, column=0, columnspan=3, sticky="w", pady=(0, 6))
        ttk.Label(frame, text="Ctrl + Alt + Space 錄音，再按一次辨識並輸入；預設完全離線。").grid(
            row=1, column=0, columnspan=3, sticky="w", pady=(0, 14))
        rows = [
            ("辨識路由", self.provider, ("offline", "api")),
            ("本機 whisper-cli", self.cli, None),
            ("本機 ggml 模型", self.model, None),
            ("API Endpoint", self.api_url, None),
            ("API Model", self.api_model, None),
            ("API Key（僅本次執行，不儲存）", self.api_key, "password"),
            ("語言", self.language, ("zh", "auto", "en")),
        ]
        for idx, (name, variable, options) in enumerate(rows, start=2):
            ttk.Label(frame, text=name).grid(row=idx, column=0, sticky="w", pady=5)
            if isinstance(options, tuple):
                widget = ttk.Combobox(frame, textvariable=variable, values=options, state="readonly")
            elif options == "password":
                widget = ttk.Entry(frame, textvariable=variable, show="•")
            else:
                widget = ttk.Entry(frame, textvariable=variable)
            widget.grid(row=idx, column=1, columnspan=2, sticky="ew", padx=(8, 0), pady=4)
        ttk.Checkbutton(frame, text="轉換成臺灣繁體中文（OpenCC）",
                        variable=self.traditional).grid(row=9, column=0, columnspan=3,
                                                        sticky="w", pady=7)
        btn = ttk.Frame(frame)
        btn.grid(row=10, column=0, columnspan=3, sticky="ew", pady=8)
        ttk.Button(btn, text="套用並啟動", command=self.start).pack(side="left", padx=(0, 6))
        ttk.Button(btn, text="停止語音服務", command=self.stop).pack(side="left", padx=(0, 6))
        ttk.Button(btn, text="關閉視窗到系統匣", command=self._hide).pack(side="left")
        ttk.Label(frame, textvariable=self.status, wraplength=555,
                  foreground="#12652b").grid(row=11, column=0, columnspan=3, sticky="w")
        ttk.Label(frame, text="隱私：API 模式才傳送音訊；不記錄逐字稿。敏感欄位請勿使用語音熱鍵。",
                  wraplength=560).grid(row=12, column=0, columnspan=3, sticky="w", pady=9)
        frame.columnconfigure(1, weight=1)
        frame.columnconfigure(2, weight=1)

    def _config(self):
        return Config(cli=Path(self.cli.get()).expanduser(),
                      model=Path(self.model.get()).expanduser(),
                      provider=self.provider.get(), api_url=self.api_url.get().strip(),
                      api_model=self.api_model.get().strip(), language=self.language.get())

    def start(self):
        if self.voice_thread and self.voice_thread.is_alive():
            self.status.set("服務已啟動。請先停止後再套用新設定。")
            return
        try:
            config = self._config()
            config.validate()
            save_settings({
                "provider": config.provider, "api_url": config.api_url,
                "api_model": config.api_model, "cli": str(config.cli),
                "model": str(config.model), "language": config.language,
                "traditional": self.traditional.get(),
            })
            # The key only lives in process memory and is never persisted.
            if self.api_key.get():
                os.environ["KUYIN_VOICE_API_KEY"] = self.api_key.get()
            self.voice = VoiceApp(config, self.traditional.get(), on_status=self.log_queue.put)
        except Exception as exc:
            messagebox.showerror(PRODUCT, f"設定錯誤：{exc}")
            return

        def worker():
            try:
                self.voice.run()
            except Exception as exc:
                self.log_queue.put(f"熱鍵服務停止：{type(exc).__name__}：{exc}")
        self.voice_thread = threading.Thread(target=worker, name="ku-yin-hotkey", daemon=True)
        self.voice_thread.start()
        self.status.set("啟動中；切換到文字編輯器後，按 Ctrl+Alt+Space")

    def stop(self):
        if self.voice is not None:
            self.voice.stop()
        self.status.set("語音服務停止中")
        # WndProc and microphone shutdown happen on the hotkey worker thread.

    def _poll(self):
        try:
            while True:
                self.status.set(self.log_queue.get_nowait())
        except queue.Empty:
            pass
        if not self.closing:
            self.after(150, self._poll)

    def _create_tray(self):
        try:
            import pystray
            from PIL import Image, ImageDraw
            image = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
            pen = ImageDraw.Draw(image)
            pen.ellipse((5, 5, 59, 59), fill=(13, 108, 170, 255))
            pen.rounded_rectangle((27, 14, 37, 41), radius=5, fill="white")
            pen.arc((19, 20, 45, 47), 0, 180, fill="white", width=4)
            pen.line((32, 45, 32, 52), fill="white", width=4)
            self.tray = pystray.Icon("kuyin-voice", image, PRODUCT,
                menu=pystray.Menu(
                    pystray.MenuItem("開啟設定", lambda _icon, _item: self.after(0, self._show)),
                    pystray.MenuItem("結束程式", lambda _icon, _item: self.after(0, self._exit))))
            threading.Thread(target=self.tray.run, daemon=True).start()
        except (ImportError, OSError, RuntimeError) as exc:
            self.status.set(f"系統匣不可用，可保持視窗開啟：{type(exc).__name__}")

    def _show(self):
        self.deiconify()
        self.lift()

    def _hide(self):
        if self.tray:
            self.withdraw()
        else:
            self.iconify()

    def _exit(self):
        self.closing = True
        if self.voice is not None:
            self.voice.stop()
        if self.tray:
            self.tray.stop()
        self.destroy()


if __name__ == "__main__":
    if sys.platform != "win32":
        raise SystemExit("KU-Yin Desktop Voice UI runs on Windows")
    Desktop().mainloop()
