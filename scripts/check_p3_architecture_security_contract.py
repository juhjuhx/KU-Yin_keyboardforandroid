#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    p = ROOT / path
    return p.read_text(encoding="utf-8") if p.exists() else ""


config = read("app/src/main/java/com/example/androidkeyboard/engines/core/IMEConfig.kt")
engine = read("app/src/main/java/com/example/androidkeyboard/engines/core/ChewingEngine.kt")
android_engine = read("app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt")
service = read("app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt")
settings = read("app/src/main/java/com/example/androidkeyboard/SettingsActivity.kt")
layout = read("app/src/main/java/com/example/androidkeyboard/input/KeyboardLayout.kt")
manifest = read("app/src/main/AndroidManifest.xml")
symbol_picker = read("app/src/main/java/com/example/androidkeyboard/input/SymbolPicker.kt")

checks = [
    ("config is data-only and does not initialize engine", "fun applyTo(" not in config),
    ("legacy user dictionary path preference removed", "userDictPath" not in config and "user_dict_path" not in settings),
    ("settings expose only implemented Dachen layout", "Hsu" not in settings and "Eten26" not in settings),
    ("dead full-half setting is not exposed", 'key = "full_half"' not in settings and "var fullHalf" not in config),
    ("service depends on core ChewingEngine contract", "private lateinit var chewing: ChewingEngine" in service),
    ("service no longer calls config.applyTo", "config.applyTo(" not in service),
    ("service only reinitializes decoder when needed", "activeLayout" in service and "!chewing.isReady" in service),
    ("EngineUpdate lives in core contract", "data class EngineUpdate" in engine and "data class EngineUpdate" not in android_engine),
    ("core contract exposes update transitions", "fun handleKeyUpdate(" in engine and "fun backspaceUpdate(" in engine and "fun selectCandidateUpdate(" in engine),
    ("dead user dictionary API removed", "loadUserDict" not in engine and "saveUserDict" not in engine and "loadUserDict" not in android_engine and "saveUserDict" not in android_engine),
    ("dead query/toggle API removed from core", "getPreedit" not in engine and "getCandidates" not in engine and "toggleChiEng" not in engine and "toggleFullHalf" not in engine),
    ("manifest disables Android backup", 'android:allowBackup="false"' in manifest),
    ("manifest has no Internet permission", "android.permission.INTERNET" not in manifest),
    ("View haptics need no VIBRATE permission", "android.permission.VIBRATE" not in manifest),
    ("symbol picker no longer carries unused anchor API", "anchorX" not in symbol_picker),
    ("ASCII surface provides explicit shift action", "KeyAction.SHIFT" in layout and "asciiRows(shifted" in layout),
    ("service creates editor policy", "EditorPolicy.from(attribute.inputType, attribute.imeOptions)" in service),
    ("service owns session controller", "ImeSessionController()" in service and "sessionController.begin(" in service),
    ("service applies personalized-learning policy", "setPersonalizedLearningEnabled(activeSession.personalizedLearningEnabled)" in service),
    ("service switches keyboard surface by session", "rowsForActiveSession" in service and "SessionKeyboard.ASCII" in service),
    ("service handles semantic enter", "KeyAction.ENTER" in service and "performEditorAction" in service),
    ("service handles ASCII shift", "KeyAction.SHIFT" in service and "toggleAsciiShift" in service),
    ("service reconciles editor selection", "override fun onUpdateSelection" in service and "shouldResetComposition" in service),
]

failures = [name for name, ok in checks if not ok]
if failures:
    print("P3 architecture/security contract FAILED:")
    for failure in failures:
        print(f"  - {failure}")
    raise SystemExit(1)

print("P3 architecture/security contract OK")
