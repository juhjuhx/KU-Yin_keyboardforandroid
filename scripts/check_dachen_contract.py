#!/usr/bin/env python3
"""Fast source-level contract for the Standard/Dachen key table.

This intentionally runs before Android/Gradle setup in CI so a broken physical
key map fails in seconds. JVM tests remain the second, semantic layer.
"""

from pathlib import Path
import re
import sys

SOURCE = Path("app/src/main/java/com/example/androidkeyboard/input/KeyboardLayout.kt")
text = SOURCE.read_text(encoding="utf-8")

# Parse KeyDef("label", ... code = N) declarations without depending on Kotlin tooling.
entries: dict[int, str] = {}
for match in re.finditer(r'KeyDef\("([^"]*)"[^\n]*?code\s*=\s*(\d+)', text):
    label = match.group(1)
    code = int(match.group(2))
    entries[code] = label

expected = {
    ord("x"): "ㄌ",
    ord("c"): "ㄏ",
    ord("v"): "ㄒ",
    ord("b"): "ㄖ",
    ord("n"): "ㄙ",
    ord("m"): "ㄩ",
    ord(","): "ㄝ",
    ord("."): "ㄡ",
    ord("/"): "ㄥ",
    ord(" "): " ",
}

errors: list[str] = []
for code, label in expected.items():
    actual = entries.get(code)
    if actual != label:
        errors.append(
            f"ASCII {code} ({chr(code)!r}) expected {label!r}, got {actual!r}"
        )

if errors:
    print("Dachen contract FAILED:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    sys.exit(1)

print("Dachen contract OK")
