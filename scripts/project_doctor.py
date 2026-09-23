#!/usr/bin/env python3
"""Project doctor: report environment status. Never installs anything.

Each line: OK / MISSING / MISMATCH / OPTIONAL.
"""
import os
import shutil
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def row(name, status, detail=""):
    print(f"{status:9} {name}" + (f" — {detail}" if detail else ""))


def main():
    row("git", "OK" if shutil.which("git") else "MISSING")
    java = shutil.which("java")
    if not java:
        row("java/JDK21", "MISSING", "no java on PATH")
    else:
        try:
            out = subprocess.run([java, "-version"], capture_output=True, text=True, timeout=30)
            ver = (out.stderr + out.stdout).splitlines()[0] if (out.stderr + out.stdout) else "?"
            row("java/JDK21", "OK" if "21" in ver else "MISMATCH", ver[:80])
        except Exception as e:  # noqa: BLE001 - diagnostic only
            row("java/JDK21", "MISMATCH", str(e)[:80])
    sdk = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT") or ""
    row("ANDROID_HOME/SDK_ROOT", "OK" if sdk else "MISSING", sdk or "unset")
    plat = Path(sdk) / "platforms" / "android-36" if sdk else None
    row("SDK platform 36", "OK" if plat and plat.is_dir() else "MISSING")
    ndk = Path(sdk) / "ndk" / "28.2.13676358" if sdk else None
    row("NDK 28.2.13676358", "OK" if ndk and ndk.is_dir() else "MISSING")
    row("CMake 3.22.1", "OPTIONAL", "verified at build time")
    row("gradle wrapper", "OK" if (ROOT / "gradlew").exists() else "MISSING")
    deps = ROOT / "app" / ".deps"
    row("native .deps", "OK" if deps.is_dir() else "MISSING", "run scripts/bootstrap_native_deps.sh")
    row("adb", "OK" if shutil.which("adb") else "MISSING", "runtime smoke needs it")
    row("ocr", "OK" if shutil.which("ocr") else "MISSING", "review scans need it (needs valid LLM key)")


if __name__ == "__main__":
    main()
