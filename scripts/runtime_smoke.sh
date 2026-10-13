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

# TEST_ONLY: this orchestration owns a disposable, single-user CI emulator.
# Physical acceptance retains the separate P1-G preflight/restore runner.
ensure_ci_user() {
  local actual
  actual="$(adb shell am get-current-user | tr -d '\r')"
  if [[ "$actual" != "0" ]]; then
    echo "CI Android user changed or is unsupported: $actual; refusing mutation" >&2
    return 2
  fi
}
ensure_ci_user

R2_EVIDENCE_DIR="build/r2-runtime-evidence"
mkdir -p "$R2_EVIDENCE_DIR"
capture_r2() {
  local phase="$1" failed=0 snapshot_presence
  date -u '+%Y-%m-%dT%H:%M:%SZ' > "$R2_EVIDENCE_DIR/$phase-time.txt" || failed=1
  adb shell am get-current-user > "$R2_EVIDENCE_DIR/$phase-user.txt" 2>&1 || failed=1
  for property in ro.product.model ro.product.manufacturer ro.build.version.sdk ro.product.cpu.abi ro.product.cpu.abilist ro.build.fingerprint; do
    printf '%s=' "$property" >> "$R2_EVIDENCE_DIR/$phase-device.txt" || failed=1
    adb shell getprop "$property" >> "$R2_EVIDENCE_DIR/$phase-device.txt" 2>&1 || failed=1
  done
  adb shell dumpsys package io.github.juhjuhx.kuyin > "$R2_EVIDENCE_DIR/$phase-package.txt" 2>&1 || failed=1
  adb shell dumpsys -t 2 input_method > "$R2_EVIDENCE_DIR/$phase-input-method.txt" 2>&1 || failed=1
  adb shell dumpsys -t 2 window windows > "$R2_EVIDENCE_DIR/$phase-window.txt" 2>&1 || failed=1
  adb shell dumpsys -t 2 activity service io.github.juhjuhx.kuyin/com.example.ime.KuYinInputMethodService > "$R2_EVIDENCE_DIR/$phase-service.txt" 2>&1 || failed=1
  adb logcat -d -v threadtime -s KUYIN_R2_TEST_ONLY InputMethodManager InsetsController InputMethodService WindowManager ActivityTaskManager AndroidRuntime > "$R2_EVIDENCE_DIR/$phase-platform-logcat.txt" 2>&1 || failed=1
  snapshot_presence="$(adb shell 'if [ -d /data/local/tmp/kuyin-r2 ]; then echo PRESENT; else echo ABSENT; fi' | tr -d '\r')" || failed=1
  if [[ "$snapshot_presence" == "PRESENT" ]]; then
    adb pull /data/local/tmp/kuyin-r2 "$R2_EVIDENCE_DIR/live-failure" > "$R2_EVIDENCE_DIR/$phase-pull.txt" 2>&1 || failed=1
  elif [[ "$snapshot_presence" != "ABSENT" ]]; then
    echo "Unverified live snapshot probe: $snapshot_presence" >&2
    failed=1
  fi
  python3 - "$R2_EVIDENCE_DIR/$phase-artifacts.json" <<'PY' || failed=1
import hashlib, json, os, pathlib, subprocess, sys, zipfile
sdk = pathlib.Path(os.environ.get('ANDROID_HOME', os.environ.get('ANDROID_SDK_ROOT', '/nonexistent')))
signers = sorted((sdk / 'build-tools').glob('*/apksigner'))
result = []
for module in ['app', 'acceptance-host']:
    for apk in sorted(pathlib.Path(module, 'build/outputs/apk').rglob('*.apk')):
        with zipfile.ZipFile(apk) as archive:
            entries = {name: hashlib.sha256(archive.read(name)).hexdigest()
                       for name in archive.namelist() if name.startswith(('lib/', 'assets/'))}
        signer = subprocess.run([str(signers[-1]), 'verify', '--print-certs', str(apk)],
                                text=True, capture_output=True) if signers else None
        if signer is None or signer.returncode:
            raise RuntimeError('APK signer verification unavailable or failed: ' + str(apk))
        result.append({'path': str(apk), 'bytes': apk.stat().st_size,
                       'sha256': hashlib.sha256(apk.read_bytes()).hexdigest(),
                       'signer_verification': signer.stdout, 'native_and_assets': entries})
pathlib.Path(sys.argv[1]).write_text(json.dumps(result, indent=2))
PY
  return "$failed"
}
on_exit() {
  local original=$? evidence_status
  trap - EXIT
  set +e
  capture_r2 final
  evidence_status=$?
  printf 'original_exit=%s\nevidence_exit=%s\n' "$original" "$evidence_status" > "$R2_EVIDENCE_DIR/terminal.txt" || evidence_status=1
  # Diagnostics never turn a failed assertion into a successful command.
  if [[ "$original" == "0" && "$evidence_status" != "0" ]]; then exit 2; fi
  exit "$original"
}
trap on_exit EXIT

R2_TEST_ARGS=(--stacktrace)
if [[ "${KUYIN_R2_OBSERVER:-}" == "true" ]]; then
  R2_TEST_ARGS+=(-Pandroid.testInstrumentationRunnerArguments.r2Diagnostics=true)
fi

APP_PACKAGE="$(sed -n 's/.*applicationId = "\([^"]*\)".*/\1/p' app/build.gradle.kts | head -n1)"

bash scripts/bootstrap_native_deps.sh
select_product_ime() {
  ensure_ci_user
  ./gradlew -Pandroid.injected.device.serial="$KUYIN_CI_SERIAL" :app:installDebug --stacktrace
  ensure_ci_user

installed=""
for _ in $(seq 1 40); do
  installed="$(adb shell ime list -s -a --user 0 | tr -d '\r')"
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
ensure_ci_user
adb shell ime enable --user 0 "$IME_COMPONENT"

enabled=""
for _ in $(seq 1 40); do
  enabled="$(adb shell ime list -s --user 0 | tr -d '\r')"
  if printf '%s\n' "$enabled" | grep -Fxq "$IME_COMPONENT"; then
    break
  fi
  sleep 0.25
done
printf 'Enabled IMEs:\n%s\n' "$enabled"
printf '%s\n' "$enabled" | grep -Fxq "$IME_COMPONENT"

echo "Selecting $IME_COMPONENT"
ensure_ci_user
adb shell ime set --user 0 "$IME_COMPONENT"

selected=""
for _ in $(seq 1 40); do
  selected="$(adb shell settings --user 0 get secure default_input_method | tr -d '\r')"
  if [ "$selected" = "$IME_COMPONENT" ]; then
    break
  fi
  sleep 0.25
done
printf 'Selected IME: %s\n' "$selected"
[ "$selected" = "$IME_COMPONENT" ]

}

select_product_ime
ensure_ci_user
capture_r2 before-app
./gradlew -Pandroid.injected.device.serial="$KUYIN_CI_SERIAL" "${R2_TEST_ARGS[@]}" :app:connectedDebugAndroidTest

# Gradle's product-suite installation/cleanup can reset the default IME.
# Establish the real precondition again before the external editor suite.
select_product_ime
ensure_ci_user
capture_r2 before-host
./gradlew -Pandroid.injected.device.serial="$KUYIN_CI_SERIAL" :acceptance-host:connectedDebugAndroidTest --stacktrace
