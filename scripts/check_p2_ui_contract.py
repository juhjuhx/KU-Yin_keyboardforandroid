#!/usr/bin/env python3
"""P2 contract: Android IME view lifecycle, touch semantics, and readable adaptive UI."""

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
service = (ROOT / "app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt").read_text(encoding="utf-8")
keyboard = (ROOT / "app/src/main/java/com/example/androidkeyboard/input/KeyboardView.kt").read_text(encoding="utf-8")
candidate = (ROOT / "app/src/main/java/com/example/androidkeyboard/ui/CandidateView.kt").read_text(encoding="utf-8")
symbol = (ROOT / "app/src/main/java/com/example/androidkeyboard/input/SymbolPicker.kt").read_text(encoding="utf-8")
palette_path = ROOT / "app/src/main/java/com/example/androidkeyboard/ui/ImePalette.kt"
palette = palette_path.read_text(encoding="utf-8") if palette_path.exists() else ""


def action_block(source: str, action: str, next_actions: tuple[str, ...]) -> str:
    marker = f"MotionEvent.{action}"
    start = source.find(marker)
    if start < 0:
        return ""
    end = len(source)
    for next_action in next_actions:
        pos = source.find(f"MotionEvent.{next_action}", start + len(marker))
        if pos >= 0:
            end = min(end, pos)
    return source[start:end]


down = action_block(keyboard, "ACTION_DOWN", ("ACTION_MOVE", "ACTION_UP", "ACTION_CANCEL"))
up = action_block(keyboard, "ACTION_UP", ("ACTION_CANCEL",))

checks = {
    "IME handles input-view start lifecycle": "override fun onStartInputView" in service,
    "IME handles input-view finish lifecycle": "override fun onFinishInputView" in service,
    "candidate and keyboard use natural vertical layout": "LinearLayout" in service and "topMargin = candidateHeight" not in service,
    "keyboard orientation uses Configuration instead of defaultDisplay": "resources.configuration.orientation" in keyboard and "defaultDisplay" not in keyboard,
    "keyboard tracks move/cancel before committing": "MotionEvent.ACTION_MOVE" in keyboard,
    "keyboard does not commit on ACTION_DOWN": "onKeyPress?.invoke" not in down,
    "keyboard commits on ACTION_UP": "onKeyPress?.invoke" in up,
    "keyboard exposes click semantics": "performClick()" in keyboard,
    "keyboard haptics respect system settings": "FLAG_IGNORE_GLOBAL_SETTING" not in keyboard,
    "candidate view exposes click semantics": "performClick()" in candidate,
    "symbol picker exposes click semantics": "performClick()" in symbol,
    "symbol picker guards empty symbol hit-testing": "symbols.isEmpty()" in symbol and "return false" in symbol,
    "adaptive IME palette exists": palette_path.exists() and "UI_MODE_NIGHT_MASK" in palette,
    "keyboard uses adaptive IME palette": "ImePalette" in keyboard,
    "candidate view uses adaptive IME palette": "ImePalette" in candidate,
    "symbol picker uses adaptive IME palette": "ImePalette" in symbol,
    "custom-view text respects font scale": "scaledDensity" in keyboard and "scaledDensity" in candidate and "scaledDensity" in symbol,
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    print("P2 UI contract FAILED:", file=sys.stderr)
    for name in failed:
        print(f"  - {name}", file=sys.stderr)
    sys.exit(1)

print("P2 UI contract OK")
