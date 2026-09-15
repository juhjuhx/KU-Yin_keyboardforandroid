#!/usr/bin/env python3
"""Deterministic 16 KB page-size gate for release APK/AAB native libraries.

Unpacks arm64-v8a/x86_64 `.so` files from the given APK (or AAB-as-ZIP) and
checks every PT_LOAD segment's p_align. Prints ALIGNED/UNALIGNED per ABI and
exits non-zero on any violation.

Not yet wired into blocking CI: the current NDK r27 build is 4K-aligned by
design, so wiring now would fail. Wire it when the NDK r28+ (or r27 link-flag)
migration lands, then require green.

Usage:
    python3 scripts/check_elf_alignment.py app/build/outputs/apk/release/app-release-unsigned.apk
"""

import struct
import sys
import tempfile
import zipfile
from pathlib import Path

REQUIRED_ALIGN = 16384
CHECKED_ABIS = ("arm64-v8a", "x86_64")
PT_LOAD = 1


def load_alignments(so_path: Path) -> list[int]:
    data = so_path.read_bytes()
    if data[:4] != b"\x7fELF":
        raise ValueError(f"not an ELF file: {so_path}")
    little = data[5] == 1
    endian = "<" if little else ">"
    is64 = data[4] == 2
    if is64:
        phoff = struct.unpack_from(endian + "Q", data, 0x20)[0]
        phentsize = struct.unpack_from(endian + "H", data, 0x36)[0]
        phnum = struct.unpack_from(endian + "H", data, 0x38)[0]
        entry = struct.Struct(endian + "IIQQQQQQ")
    else:
        phoff = struct.unpack_from(endian + "I", data, 0x1C)[0]
        phentsize = struct.unpack_from(endian + "H", data, 0x2A)[0]
        phnum = struct.unpack_from(endian + "H", data, 0x2C)[0]
        entry = struct.Struct(endian + "IIIIIIII")
    aligns = []
    for i in range(phnum):
        seg = entry.unpack_from(data, phoff + i * phentsize)
        ptype = seg[0]
        align = seg[-1]
        if ptype == PT_LOAD:
            aligns.append(align)
    if not aligns:
        raise ValueError(f"no PT_LOAD segments in {so_path}")
    return aligns


def main() -> int:
    if len(sys.argv) != 2:
        print(f"usage: {sys.argv[0]} <apk-or-aab>", file=sys.stderr)
        return 2
    archive = Path(sys.argv[1])
    if not archive.is_file():
        print(f"missing archive: {archive}", file=sys.stderr)
        return 2
    failures = []
    with tempfile.TemporaryDirectory() as tmp:
        with zipfile.ZipFile(archive) as zf:
            sos = [n for n in zf.namelist() if n.endswith(".so")]
            targets = [n for n in sos if any(f"lib/{abi}/" in n for abi in CHECKED_ABIS)]
            if not targets:
                print("no arm64-v8a/x86_64 .so found; nothing to check")
                return 0
            for name in sorted(targets):
                out = Path(tmp) / Path(name).name
                out.write_bytes(zf.read(name))
                aligns = load_alignments(out)
                ok = all(a % REQUIRED_ALIGN == 0 and a >= REQUIRED_ALIGN for a in aligns)
                status = "ALIGNED" if ok else "UNALIGNED"
                print(f"{status}: {name} (LOAD aligns: {sorted(set(aligns))})")
                if not ok:
                    failures.append(name)
    if failures:
        print(f"16K check FAILED for {len(failures)} librar(ies)", file=sys.stderr)
        return 1
    print("16K check PASSED")
    return 0


if __name__ == "__main__":
    sys.exit(main())
