#!/usr/bin/env python3
"""Fail-fast P1 contract for clean-clone native Android builds."""

from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
workflow = (ROOT / ".github/workflows/build.yml").read_text(encoding="utf-8")
cmake = (ROOT / "app/src/main/cpp/CMakeLists.txt").read_text(encoding="utf-8")
jni = (ROOT / "app/src/main/cpp/chewing_jni.cpp").read_text(encoding="utf-8")
engine = (ROOT / "app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt").read_text(encoding="utf-8")
service = (ROOT / "app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt").read_text(encoding="utf-8")
bootstrap_path = ROOT / "scripts/bootstrap_native_deps.sh"
bootstrap = bootstrap_path.read_text(encoding="utf-8") if bootstrap_path.exists() else ""
installer_path = ROOT / "app/src/main/java/com/example/androidkeyboard/engines/android/LibChewingDataInstaller.kt"
installer = installer_path.read_text(encoding="utf-8") if installer_path.exists() else ""

PREBUILT_COMMIT = "3587ba3355711f0aca50136e787719f6562676b8"
SOURCE_COMMIT = "a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c"
ABIS = ("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
DICT_FILES = ("tsi.dat", "word.dat", "swkb.dat", "symbols.dat")

checks = {
    "workflow pins Gradle 7.6.4": 'gradle-version: "7.6.4"' in workflow,
    "workflow bootstraps native dependencies": "scripts/bootstrap_native_deps.sh" in workflow,
    "workflow runs clean Gradle unit tests": "gradle testDebugUnitTest" in workflow,
    "workflow builds debug APK": "gradle assembleDebug" in workflow,
    "native bootstrap script exists": bootstrap_path.exists(),
    "native bootstrap pins immutable fcitx prebuilt commit": PREBUILT_COMMIT in bootstrap,
    "native bootstrap records exact libchewing source commit": SOURCE_COMMIT in bootstrap,
    "CMake links the pinned chewing C API archive": "libchewing_capi.a" in cmake,
    "CMake fails when native dependency is missing": "FATAL_ERROR" in cmake,
    "CMake no longer warning-continues without libchewing": "without libchewing" not in cmake.lower(),
    "JNI exposes explicit data/user path constructor": "chewing_new2" in jni,
    "Android engine supplies data and user paths": "chewing_new2" in engine,
    "app installs libchewing dictionary assets": "LibChewingDataInstaller" in service and installer_path.exists(),
    "installer uses app-private no-backup storage": "noBackupFilesDir" in installer,
}

for abi in ABIS:
    checks[f"bootstrap includes ABI {abi}"] = abi in bootstrap
for name in DICT_FILES:
    checks[f"bootstrap packages dictionary {name}"] = name in bootstrap
    checks[f"installer requires dictionary {name}"] = name in installer

failed = [name for name, ok in checks.items() if not ok]
if failed:
    print("P1 build contract FAILED:", file=sys.stderr)
    for name in failed:
        print(f"  - {name}", file=sys.stderr)
    sys.exit(1)

print("P1 build contract OK")
