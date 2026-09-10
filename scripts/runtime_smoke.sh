#!/usr/bin/env bash
set -euo pipefail

APP_PACKAGE="com.example.androidkeyboard"

bash scripts/bootstrap_native_deps.sh
gradle installDebug --stacktrace

installed=""
for _ in $(seq 1 40); do
  installed="$(adb shell ime list -s -a | tr -d '\r')"
  if printf '%s\n' "$installed" | grep -Fq "$APP_PACKAGE/"; then
    break
  fi
  sleep 0.25
done
printf 'Installed IMEs:\n%s\n' "$installed"
printf '%s\n' "$installed" | grep -Fq "$APP_PACKAGE/"

IME_COMPONENT="$(printf '%s\n' "$installed" | grep -F "$APP_PACKAGE/" | head -n 1)"
if [ -z "$IME_COMPONENT" ]; then
  echo "Failed to resolve KU-Yin canonical IME id" >&2
  exit 1
fi
printf 'Canonical KU-Yin IME: %s\n' "$IME_COMPONENT"

echo "Enabling $IME_COMPONENT"
adb shell ime enable "$IME_COMPONENT"

enabled=""
for _ in $(seq 1 40); do
  enabled="$(adb shell ime list -s | tr -d '\r')"
  if printf '%s\n' "$enabled" | grep -Fxq "$IME_COMPONENT"; then
    break
  fi
  sleep 0.25
done
printf 'Enabled IMEs:\n%s\n' "$enabled"
printf '%s\n' "$enabled" | grep -Fxq "$IME_COMPONENT"

echo "Selecting $IME_COMPONENT"
adb shell ime set "$IME_COMPONENT"

selected=""
for _ in $(seq 1 40); do
  selected="$(adb shell settings get secure default_input_method | tr -d '\r')"
  if [ "$selected" = "$IME_COMPONENT" ]; then
    break
  fi
  sleep 0.25
done
printf 'Selected IME: %s\n' "$selected"
[ "$selected" = "$IME_COMPONENT" ]

gradle connectedDebugAndroidTest --stacktrace
