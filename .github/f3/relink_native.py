#!/usr/bin/env python3
"""Relink KU-Yin's JNI object with a replaceable libchewing static library."""
import argparse
from pathlib import Path
import platform
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument("--ndk", type=Path, required=True)
parser.add_argument("--abi", choices=["armeabi-v7a", "arm64-v8a", "x86", "x86_64"], required=True)
parser.add_argument("--archive", type=Path, help="Your modified libchewing_capi.a; default uses shipped archive")
parser.add_argument("--output", type=Path, required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parent
host = "darwin-x86_64" if platform.system() == "Darwin" else "linux-x86_64"
llvm = args.ndk / "toolchains/llvm/prebuilt" / host
triple = {"armeabi-v7a": "armv7-none-linux-androideabi", "arm64-v8a": "aarch64-none-linux-android",
          "x86": "i686-none-linux-android", "x86_64": "x86_64-none-linux-android"}[args.abi]
inputs = root / "relink-inputs" / args.abi
archive = args.archive or inputs / "libchewing_capi.a"
if args.output.exists():
    parser.error("Refusing to overwrite an existing output")
args.output.parent.mkdir(parents=True, exist_ok=True)
command = [str(llvm / "bin/clang++"), "--target=" + triple + "24", "--sysroot=" + str(llvm / "sysroot"),
           "-fPIC", "-g", "-DANDROID", "-fdata-sections", "-ffunction-sections", "-funwind-tables",
           "-fstack-protector-strong", "-no-canonical-prefixes", "-D_FORTIFY_SOURCE=2",
           "-Wformat", "-Werror=format-security", "-std=c++17", "-fno-limit-debug-info",
           "-Qunused-arguments", "-shared", "-static-libstdc++",
           "-Wl,--no-undefined", "-Wl,--no-undefined-version", "-Wl,--fatal-warnings",
           "-Wl,--build-id=sha1", "-Wl,--no-rosegment", "-Wl,-soname,libchewing-jni.so",
           str(inputs / "chewing_jni.cpp.o"), str(archive),
           "-llog", "-landroid", "-ldl", "-lm", "-latomic", "-o", str(args.output)]
if args.abi == "armeabi-v7a":
    command[1:1] = ["-march=armv7-a", "-mthumb"]
subprocess.run(command, check=True)
print(args.output)
