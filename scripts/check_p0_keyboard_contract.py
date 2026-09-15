#!/usr/bin/env python3
"""Fail-fast source contract for P0 semantic keyboard dispatch and geometry."""

from pathlib import Path
import sys

layout = Path("app/src/main/java/com/example/androidkeyboard/input/KeyboardLayout.kt").read_text(encoding="utf-8")
shell = Path("app/src/main/java/com/example/androidkeyboard/input/KeyboardShell.kt").read_text(encoding="utf-8")
service = Path("app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt").read_text(encoding="utf-8")
view = Path("app/src/main/java/com/example/androidkeyboard/input/KeyboardView.kt").read_text(encoding="utf-8")

checks = {
    "Legacy KeyAction enum remains as layout data source": "enum class KeyAction" in layout,
    "Backspace remains semantic in layout data": "KeyAction.BACKSPACE" in layout,
    "Space remains semantic in layout data": "KeyAction.SPACE" in layout,
    "Dismiss remains semantic in layout data": "KeyAction.DISMISS" in layout,
    "v0.2 command model exists": "sealed interface ImeCommand" in shell,
    "v0.2 effect model exists": "sealed interface ImeEffect" in shell,
    "View dispatches semantic commands": "onCommand?.invoke" in view,
    "Service dispatches semantic commands": "dispatchCommand" in service,
    "Service executes semantic effects": "executeEffect" in service,
    "Service no longer branches on legacy key action": "when (key.action)" not in service,
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
