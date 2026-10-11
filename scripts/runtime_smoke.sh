#!/usr/bin/env bash
set -euo pipefail

# This script owns only the disposable emulator created by GitHub Actions.
# Owner-authorized physical testing uses p1_acceptance_device.py instead.
if [[ "${CI:-}" != "true" ]]; then
  echo "runtime_smoke.sh requires an isolated CI emulator" >&2
  exit 2
fi
KUYIN_CI_SERIAL="$(command adb devices -l | awk 'NR > 1 && $2 == "device" {print $1}')"
if [[ -z "$KUYIN_CI_SERIAL" || "$KUYIN_CI_SERIAL" == *$'\n'* || "$KUYIN_CI_SERIAL" != emulator-* ]]; then
  echo "Expected exactly one CI emulator; refusing ambiguous or physical target" >&2
  exit 2
fi
if [[ "$(command adb -s "$KUYIN_CI_SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')" != "1" ]]; then
  echo "Target is not an emulator; refusing device mutation" >&2
  exit 2
fi
export ANDROID_SERIAL="$KUYIN_CI_SERIAL"
adb() { command adb -s "$KUYIN_CI_SERIAL" "$@"; }

APP_PACKAGE="$(sed -n 's/.*applicationId = "\([^"]*\)".*/\1/p' app/build.gradle.kts | head -n1)"

bash scripts/bootstrap_native_deps.sh
select_product_ime() {
  ./gradlew -Pandroid.injected.device.serial="$KUYIN_CI_SERIAL" :app:installDebug --stacktrace

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

}

select_product_ime
./gradlew -Pandroid.injected.device.serial="$KUYIN_CI_SERIAL" :app:connectedDebugAndroidTest --stacktrace

# Gradle's product-suite installation/cleanup can reset the default IME.
# Establish the real precondition again before the external editor suite.
select_product_ime
./gradlew -Pandroid.injected.device.serial="$KUYIN_CI_SERIAL" :acceptance-host:connectedDebugAndroidTest --stacktrace
