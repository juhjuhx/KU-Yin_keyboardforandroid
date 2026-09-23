#!/usr/bin/env python3
"""Anti-drift checker: derives facts from project files, fails on stale docs.

Deterministic: pure text extraction, no network, no build. Exit 0 = consistent.
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
failures = []


def check(name, cond, hint=""):
    print(("PASS " if cond else "FAIL ") + name + (f" ({hint})" if hint and not cond else ""))
    if not cond:
        failures.append(name)


def main():
    wrapper = (ROOT / "gradle/wrapper/gradle-wrapper.properties").read_text()
    catalog = (ROOT / "gradle/libs.versions.toml").read_text()
    build = (ROOT / "app/build.gradle.kts").read_text()
    dev = (ROOT / "docs/DEVELOPMENT.md").read_text()
    arch = (ROOT / "docs/ARCHITECTURE.md").read_text()
    roadmap = (ROOT / "docs/ROADMAP.md").read_text()
    audit = (ROOT / "docs/SECURITY_AUDIT.md").read_text()

    m = re.search(r"gradle-(\d+\.\d+\.\d+)-bin", wrapper)
    check("dev-doc gradle wrapper version", m and m.group(1) in dev, "wrapper has " + (m.group(1) if m else "?"))
    pairs = [
        ("AGP", r'agp\s*=\s*"([^"]+)"', catalog, r"(?m)^\|\s*AGP\s*\|[^|\n]*\|\s*([\d.]+)", dev),
        ("Kotlin", r'kotlin\s*=\s*"([^"]+)"', catalog, r"Kotlin[^\d]*([\d.]+)", dev),
        ("compileSdk", r"release\((\d+)\)", build, r"compileSdk[^\d]*(\d+)", dev),
        ("targetSdk", r"targetSdk\s*=\s*(\d+)", build, r"targetSdk[^\d]*(\d+)", dev),
        ("minSdk", r"minSdk\s*=\s*(\d+)", build, r"minSdk[^\d]*(\d+)", dev),
        ("NDK", r"ndkVersion\s*=\s*\"([^\"]+)\"", build, r"NDK[^\d]*([\d.]+)", dev),
        ("CMake", r'cmake\s*\{\s*path[^}]*?version\s*=\s*"([^"]+)"', build, r"CMake[^\d]*([\d.]+)", dev),
    ]
    for label, src_pat, src_text, doc_pat, doc_text in pairs:
        sm = re.search(src_pat, src_text)
        dm = re.search(doc_pat, doc_text)
        ok = bool(sm and dm and sm.group(1) == dm.group(1))
        hint = f"src={sm.group(1) if sm else '?'} doc={dm.group(1) if dm else '?'}"
        check(f"dev-doc {label} version", ok, hint)
    check("ARCHITECTURE is Compose-normative", "ComposeDecoderSession" in arch)
    check("ARCHITECTURE not View-production", "ChewingInputMethodService" not in arch)
    check("ROADMAP keeps done items done", "## DONE" in roadmap
          and "Gradle 7" not in roadmap and "API 33" not in roadmap
          and "1.9.22" not in roadmap)
    check("SECURITY_AUDIT not API-33-current", "target/compile API 33" not in audit)
    check("SECURITY_AUDIT current platform", "36" in audit)

    if failures:
        print(f"\n{len(failures)} drift check(s) FAILED")
        return 1
    print("\ndocs consistency OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
