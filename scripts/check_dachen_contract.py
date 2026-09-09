#!/usr/bin/env python3
"""Fast source-level contract for the Standard/Dachen key table.

This runs before Android/Gradle setup so physical-key regressions fail in
seconds. It intentionally inspects only the Dachen enum entry; ASCII fallback
keys may reuse the same character codes with different labels and must not
pollute the Dachen mapping.
"""

from pathlib import Path
import re
import sys

SOURCE = Path("app/src/main/java/com/example/androidkeyboard/input/KeyboardLayout.kt")
text = SOURCE.read_text(encoding="utf-8")

start_marker = "    Dachen("
end_marker = "\n\n    Ascii("
start = text.find(start_marker)
end = text.find(end_marker, start + len(start_marker)) if start >= 0 else -1
if start < 0 or end < 0:
    print("Dachen contract FAILED:", file=sys.stderr)
    print("  - unable to isolate KeyboardLayout.Dachen source block", file=sys.stderr)
    sys.exit(1)

dachen_source = text[start:end]
entries: dict[int, str] = {}
pattern = re.compile(
    r'KeyDef\("([^"]*)"[^\n]*?code\s*=\s*(?:(\d+)|\'(.?)\'\.code)'
)
for match in pattern.finditer(dachen_source):
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
