#!/usr/bin/env python3
"""Contract: every NDK pin in the repo derives from the app production pin.

app/build.gradle.kts `ndkVersion` is the single source of truth. The CI
build-job packages and the runtime-smoke NDK input must equal it; otherwise
native artifacts and smoke evidence are built against different toolchains.
"""

from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent.parent
build_gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
workflow = (ROOT / ".github/workflows/build.yml").read_text(encoding="utf-8")

match = re.search(r'ndkVersion\s*=\s*"([^"]+)"', build_gradle)
app_ndk = match.group(1) if match else ""

checks = {
    "app declares an NDK pin": bool(app_ndk),
    "runtime smoke uses the production NDK pin": f"ndk: {app_ndk}" in workflow,
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    print("NDK pin contract FAILED:", file=sys.stderr)
    for name in failed:
        print(f"  - {name}", file=sys.stderr)
    if app_ndk:
        print(f"  app ndkVersion: {app_ndk}", file=sys.stderr)
    sys.exit(1)

print("NDK pin contract OK")
