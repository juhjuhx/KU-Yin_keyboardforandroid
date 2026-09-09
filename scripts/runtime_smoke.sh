#!/usr/bin/env bash
set -euo pipefail

IME_COMPONENT="com.example.androidkeyboard/com.example.androidkeyboard.input.ChewingInputMethodService"
APP_PACKAGE="com.example.androidkeyboard"

bash scripts/bootstrap_native_deps.sh
gradle installDebug --stacktrace

installed=""
for _ in $(seq 1 40); do
  installed="$(adb shell ime list -s -a | tr -d '\r')"
  if printf '%s\n' "$installed" | grep -Fq "$APP_PACKAGE"; then
    break
  fi
  sleep 0.25
done
printf 'Installed IMEs:\n%s\n' "$installed"
printf '%s\n' "$installed" | grep -Fq "$APP_PACKAGE"

echo "Enabling $IME_COMPONENT"
adb shell ime enable "$IME_COMPONENT"

enabled=""
for _ in $(seq 1 40); do
  enabled="$(adb shell ime list -s | tr -d '\r')"
  if printf '%s\n' "$enabled" | grep -Fq "$APP_PACKAGE"; then
    break
  fi
  sleep 0.25
done
printf 'Enabled IMEs:\n%s\n' "$enabled"
printf '%s\n' "$enabled" | grep -Fq "$APP_PACKAGE"

echo "Selecting $IME_COMPONENT"
adb shell ime set "$IME_COMPONENT"

selected=""
for _ in $(seq 1 40); do
  selected="$(adb shell settings get secure default_input_method | tr -d '\r')"
  if printf '%s\n' "$selected" | grep -Fq "$APP_PACKAGE"; then
    break
  fi
  sleep 0.25
done
printf 'Selected IME: %s\n' "$selected"
printf '%s\n' "$selected" | grep -Fq "$APP_PACKAGE"

gradle connectedDebugAndroidTest --stacktrace
