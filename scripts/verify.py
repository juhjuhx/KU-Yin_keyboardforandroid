#!/usr/bin/env python3
"""Canonical developer entrypoint. Orchestrates existing scripts, reimplements nothing.

Modes: --static (contracts + docs) --jvm (unit tests) --build (debug APK)
       --runtime (smoke, needs adb/emulator) --all (static+jvm+build)
"""
import argparse
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONTRACTS = [
    "check_dachen_contract.py",
    "check_p0_keyboard_contract.py",
    "check_editor_sync_contract.py",
    "check_p1_build_contract.py",
    "check_p2_ui_contract.py",
    "check_p3_architecture_security_contract.py",
    "check_production_compose_contract.py",
    "check_ci_c4_trigger.py",
    "check_docs_consistency.py",
]


def run(cmd, **kw):
    print(f"$ {' '.join(cmd)}")
    return subprocess.run(cmd, cwd=ROOT, **kw).returncode


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--static", action="store_true")
    ap.add_argument("--jvm", action="store_true")
    ap.add_argument("--build", action="store_true")
    ap.add_argument("--runtime", action="store_true")
    ap.add_argument("--all", action="store_true")
    args = ap.parse_args()
    if args.all:
        args.static = args.jvm = args.build = True
    if not (args.static or args.jvm or args.build or args.runtime):
        ap.print_help()
        return 2
    if args.static:
        for name in CONTRACTS:
            if run([sys.executable, f"scripts/{name}"]) != 0:
                return 1
    if args.jvm:
        if run(["./gradlew", "testDebugUnitTest", "--stacktrace"]) != 0:
            return 1
    if args.build:
        if run(["./gradlew", "assembleDebug", "--stacktrace"]) != 0:
            return 1
    if args.runtime:
        if run(["bash", "scripts/runtime_smoke.sh"]) != 0:
            return 1
    print("verify OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
