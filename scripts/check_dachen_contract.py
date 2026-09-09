#!/usr/bin/env python3
"""Fast source-level contract for the Standard/Dachen key table.

This runs before Android/Gradle setup so physical-key regressions fail in
seconds. It intentionally accepts both numeric ASCII literals and Kotlin
character expressions such as `'x'.code`.
"""

from pathlib import Path
import re
import sys

SOURCE = Path("app/src/main/java/com/example/androidkeyboard/input/KeyboardLayout.kt")
text = SOURCE.read_text(encoding="utf-8")

entries: dict[int, str] = {}
pattern = re.compile(
    r'KeyDef\("([^"]*)"[^\n]*?code\s*=\s*(?:(\d+)|\'(.?)\'\.code)'
)
for match in pattern.finditer(text):
    label = match.group(1)
    numeric = match.group(2)
    character = match.group(3)
    code = int(numeric) if numeric is not None else ord(character)
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
