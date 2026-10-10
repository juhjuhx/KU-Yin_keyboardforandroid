#!/usr/bin/env python3
"""以鎖定 libchewing 原始碼與字典驗證真 production JNI；不執行 Gradle。"""
from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import time

SOURCE_COMMIT = "a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c"
PREBUILT_COMMIT = "3587ba3355711f0aca50136e787719f6562676b8"
ROOT = Path(__file__).resolve().parents[1]


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-dir", type=Path, required=True,
                        help="官方 immutable archive 解開的 libchewing 0.11.0 目錄")
    parser.add_argument("--prebuilt-dir", type=Path, required=True,
                        help="鎖定 fcitx5-android/prebuilt checkout（只讀）")
    parser.add_argument("--jdk-home", type=Path, required=True)
    parser.add_argument("--output-dir", type=Path, default=ROOT / "build/p0r-evidence")
    parser.add_argument("--phase", default="final")
    args = parser.parse_args()
    output = args.output_dir.resolve()
    output.mkdir(parents=True, exist_ok=True)
    source = args.source_dir.resolve()
    prebuilt = args.prebuilt_dir.resolve()
    checkout = subprocess.check_output(
        ["git", "-C", str(prebuilt), "rev-parse", "HEAD"], text=True).strip()
    if checkout != PREBUILT_COMMIT:
        raise ValueError("字典 checkout 必須是 production 鎖定 revision")
    provenance = json.loads((source.parent / "source-provenance.json").read_text())
    if provenance["source_commit"] != SOURCE_COMMIT:
        raise ValueError("libchewing 原始碼必須是 production 鎖定 revision")
    archive = source.parent / "libchewing-source.tar.gz"
    if provenance["archive_sha256"] != sha256(archive):
        raise ValueError("immutable source archive hash 不符")
    if 'version = "0.11.0"' not in (source / "capi/Cargo.toml").read_text():
        raise ValueError("CAPI version 必須是 0.11.0")
    dictionary = prebuilt / "chewing-dict"
    dictionary_hashes = {name: sha256(dictionary / name)
                         for name in ("word.dat", "tsi.dat", "swkb.dat", "symbols.dat")}

    commands: list[dict[str, object]] = []

    def run(command: list[str], label: str, cwd: Path = ROOT, timeout: int = 180):
        start = time.monotonic()
        result = subprocess.run(command, cwd=cwd, capture_output=True, text=True, timeout=timeout)
        (output / f"{label}.log").write_text(result.stdout + result.stderr)
        commands.append({"label": label, "command": command, "cwd": str(cwd),
                         "exit_code": result.returncode,
                         "elapsed_seconds": round(time.monotonic() - start, 3)})
        if result.returncode:
            raise RuntimeError(f"{label} failed; 見 {output / (label + '.log')}")
        return result

    try:
        run(["cargo", "rustc", "--offline", "-p", "chewing_capi", "--release", "--",
             "--crate-type", "cdylib"], "host-capi-build", cwd=source)
        jdk = args.jdk_home.resolve()
        headers = prebuilt / "libchewing/arm64-v8a/include/chewing"
        cpp = ROOT / "app/src/main/cpp/chewing_jni.cpp"
        cpp_flags = ["-std=c++17", "-Wall", "-Wextra", "-Werror",
                     f"-I{jdk / 'include'}", f"-I{jdk / 'include/darwin'}", f"-I{headers}"]
        run(["clang++", "-fsyntax-only", *cpp_flags, str(cpp)], "jni-diagnostics")
        run(["clang++", "-dynamiclib", *cpp_flags, str(cpp),
             str(ROOT / "scripts/native-tests/NativeUnicodeFixture.cpp"),
             str(source / "target/release/libchewing_capi.a"),
             "-o", str(output / "libchewing-jni.dylib")], "host-jni-build")

        cache = Path.home() / ".gradle/caches/modules-2/files-2.1"

        def jar(group: str, artifact: str, version: str) -> Path:
            return next((cache / group / artifact / version).glob("*/*.jar"))

        stdlib = jar("org.jetbrains.kotlin", "kotlin-stdlib", "2.2.10")
        jars = [jar("org.jetbrains.kotlin", "kotlin-compiler-embeddable", "2.2.10"), stdlib,
                jar("org.jetbrains.kotlin", "kotlin-script-runtime", "2.2.10"),
                jar("org.jetbrains.kotlin", "kotlin-reflect", "2.2.10"),
                jar("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm", "1.8.0"),
                jar("org.jetbrains", "annotations", "13.0")]
        java = str(jdk / "bin/java")
        test_source = ROOT / "scripts/native-tests/P0RNativeHostProbe.kt"
        # Probe keeps its dictionary source explicit; substituting this constant
        # changes only the generated test copy, never production assets/source.
        generated = output / "P0RNativeHostProbe.kt"
        generated.write_text(test_source.read_text().replace(
            "/private/tmp/kuyin-p1g-native-20261009/fcitx5-android-prebuilt/chewing-dict", str(dictionary)))
        sources = [ROOT / "app/src/main/java/com/example/androidkeyboard/engines/core/ChewingEngine.kt",
                   ROOT / "app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt",
                   ROOT / "scripts/native-tests/HostLog.kt",
                   ROOT / "scripts/native-tests/NativeUnicodeFixture.kt", generated]
        run([java, "-cp", os.pathsep.join(map(str, jars)), "org.jetbrains.kotlin.cli.jvm.K2JVMCompiler",
             "-no-stdlib", "-no-reflect", "-classpath", str(stdlib), "-jvm-target", "21",
             "-d", str(output / "adapter-probe.jar"), *map(str, sources)], "kotlin-diagnostics")
        result = run([java, f"-Djava.library.path={output}", f"-Dp0r.evidence={output}",
                      "-cp", os.pathsep.join([str(output / "adapter-probe.jar"), str(stdlib)]),
                      "P0RNativeHostProbeKt"], f"production-adapter-{args.phase}")
        (output / f"production-adapter-{args.phase}.json").write_text(result.stdout)
        document = json.loads(result.stdout)
        if document["failures"]:
            raise RuntimeError("真 native probe 有未滿足 acceptance")
        after = {name: sha256(dictionary / name) for name in dictionary_hashes}
        if after != dictionary_hashes:
            raise RuntimeError("鎖定字典被修改")
        print("PASS：真 pinned libchewing + production JNI / adapter；裝置仍 NOT_TESTED。")
        return 0
    finally:
        (output / "host-probe-manifest.json").write_text(json.dumps({
            "source_commit": SOURCE_COMMIT, "prebuilt_commit": PREBUILT_COMMIT,
            "source_archive_sha256": provenance["archive_sha256"],
            "dictionary_sha256": dictionary_hashes,
            "source_files_sha256": {str(path.relative_to(ROOT)): sha256(path) for path in (
                ROOT / "app/src/main/cpp/chewing_jni.cpp",
                ROOT / "app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt")},
            "commands": commands, "device_runtime": "NOT_TESTED"}, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    raise SystemExit(main())
