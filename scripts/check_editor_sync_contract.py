#!/usr/bin/env python3
"""Fail-fast contract for Android editor synchronization and candidate selection."""

from pathlib import Path
import sys

service = Path("app/src/main/java/com/example/androidkeyboard/input/ChewingInputMethodService.kt").read_text(encoding="utf-8")
engine = Path("app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt").read_text(encoding="utf-8")
core_engine = Path("app/src/main/java/com/example/androidkeyboard/engines/core/ChewingEngine.kt").read_text(encoding="utf-8")
candidate = Path("app/src/main/java/com/example/androidkeyboard/ui/CandidateView.kt").read_text(encoding="utf-8")

checks = {
    "engine exposes immutable update state": "data class EngineUpdate" in core_engine,
    "service sets composing text": "setComposingText" in service,
    "service finishes composing text": "finishComposingText" in service,
    "service applies committed native text": "committedText" in service and "commitText" in service,
    "candidate callback carries index": "((Int, String) -> Unit)?" in candidate,
    "service selects native candidate before editor update": (
        "selectCandidateUpdate(index)" in service and "applyEngineUpdate" in service
    ),
    "engine maps page-local candidate index": (
        "globalIndex" in engine and "chewing_cand_choose_by_index(nativeCtx, globalIndex)" in engine
    ),
    "service does not commit rendered candidate directly": "commitCandidate(candidate: String)" not in service,
}

failed = [name for name, ok in checks.items() if not ok]
if failed:
    print("Editor sync contract FAILED:", file=sys.stderr)
    for name in failed:
        print(f"  - {name}", file=sys.stderr)
    sys.exit(1)

print("Editor sync contract OK")
