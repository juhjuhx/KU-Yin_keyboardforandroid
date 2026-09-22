#!/usr/bin/env python3
"""Fail-fast contract for the PRODUCTION Compose IME path.

Unlike the donor architecture contracts (which validate the reference
View/libchewing implementation under androidkeyboard/), every check here
reads the Manifest-registered production service, its engine, or its UI.
A green donor suite must never stand in for these.
"""

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
service = (ROOT / "app/src/main/java/com/example/ime/KuYinInputMethodService.kt").read_text(encoding="utf-8")
engine = (ROOT / "app/src/main/java/com/example/ime/engine/KuYinEngine.kt").read_text(encoding="utf-8")
ui = (ROOT / "app/src/main/java/com/example/ime/ui/KuYinKeyboardUi.kt").read_text(encoding="utf-8")

checks = {
    "manifest registers production Compose service": ".ime.KuYinInputMethodService" in manifest,
    "production service derives editor policy": "EditorPolicy.from(" in service,
    "production service applies policy to engine": "engine.applyPolicy(" in service,
    "production service falls back to ASCII surface": "KeyboardMode.ENGLISH" in service,
    "production service reconciles selection": "onUpdateSelection" in service
    and "CompositionResetPolicy" in service,
    "production backspace is code-point aware": "deleteSurroundingTextInCodePoints" in service,
    "engine gates learning on policy": "allowPersonalizedLearning" in engine,
    "punctuation routes through engine transition": "commitPunctuation" in engine
    and "engine.commitPunctuation" in ui,
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    print("Production Compose contract FAILED:", file=sys.stderr)
    for name in failed:
        print(f"  - {name}", file=sys.stderr)
    sys.exit(1)

print("Production Compose contract OK")
