#!/usr/bin/env python3
"""Fail-fast source contract for P0 keyboard dispatch and geometry."""

from pathlib import Path
import sys

layout = Path("app/src/main/java/com/example/androidkeyboard/input/KeyboardLayout.kt").read_text(encoding="utf-8")
service = Path("app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt").read_text(encoding="utf-8")
view = Path("app/src/main/java/com/example/androidkeyboard/input/KeyboardView.kt").read_text(encoding="utf-8")

checks = {
    "KeyAction enum exists": "enum class KeyAction" in layout,
    "Backspace is semantic action": "KeyAction.BACKSPACE" in layout,
    "Space is semantic action": "KeyAction.SPACE" in layout,
    "Dismiss is semantic action": "KeyAction.DISMISS" in layout,
    "Service dispatches by action": "when (key.action)" in service,
    "Service no longer checks backspace label": 'key.label == "back"' not in service,
    "Keyboard rebuilds after size change": "override fun onSizeChanged" in view,
    "Zero-width geometry is guarded": "width <= 0" in view or "w <= 0" in view,
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    print("P0 keyboard contract FAILED:", file=sys.stderr)
    for name in failed:
        print(f"  - {name}", file=sys.stderr)
    sys.exit(1)

print("P0 keyboard contract OK")
