#!/usr/bin/env python3
"""Contract: blocking Build CI runs on the C4 development branch.

The c4-compose-libchewing-switch branch carries native production work that
must have blocking CI evidence. A main-only trigger leaves it unverified.
"""

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
workflow = (ROOT / ".github/workflows/build.yml").read_text(encoding="utf-8")

checks = {
    "blocking build triggers on C4 branch push": "c4-compose-libchewing-switch" in workflow,
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    print("CI C4 trigger contract FAILED:", file=sys.stderr)
    for name in failed:
        print(f"  - {name}", file=sys.stderr)
    sys.exit(1)

print("CI C4 trigger contract OK")
