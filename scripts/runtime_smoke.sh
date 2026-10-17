#!/usr/bin/env bash
set -euo pipefail

# This script owns only the disposable emulator created by GitHub Actions.
# Owner-authorized physical testing uses p1_acceptance_device.py instead.
if [[ "${CI:-}" != "true" ]]; then
  echo "runtime_smoke.sh requires an isolated CI emulator" >&2
  exit 2
fi
case "${KUYIN_R2_HOST_PROBE:-false}" in
  true|false|identity|geometry) ;;
  *) echo "Invalid TEST_ONLY Host probe mode; refusing device commands" >&2; exit 2 ;;
esac
case "${KUYIN_R22_BINDING_ARM:-}" in
  ''|legacy|preinstalled) ;;
  *) echo "Invalid binding experiment arm; refusing device commands" >&2; exit 2 ;;
esac
case "${KUYIN_R22_APP_ONLY:-false}" in
  true|false) ;;
  *) echo "Invalid App-only experiment flag" >&2; exit 2 ;;
esac
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
if [[ -n "${KUYIN_R22_BINDING_ARM:-}" ]]; then R2_EVIDENCE_DIR="build/r22-${KUYIN_R22_BINDING_ARM}"; fi
mkdir -p "$R2_EVIDENCE_DIR"
HOST_TEST_ARGS=(--stacktrace)
if [[ -n "${KUYIN_R22_BINDING_ARM:-}" ]]; then
  echo FINITE_BINDING_EXPERIMENT_NOT_ACCEPTANCE | tee "$R2_EVIDENCE_DIR/scope.txt"
elif [[ "${KUYIN_R2_HOST_PROBE:-false}" == "geometry" ]]; then
  HOST_TEST_ARGS+=(-Pandroid.testInstrumentationRunnerArguments.class=io.github.juhjuhx.kuyin.acceptancehost.DachenAcceptanceTest#b01CanonicalKeysAndShiRiHaveStrictPreeditEvidence)
  echo FOCUSED_DIAGNOSTIC_NOT_ACCEPTANCE | tee "$R2_EVIDENCE_DIR/scope.txt"
elif [[ "${KUYIN_R2_HOST_PROBE:-false}" == "identity" ]]; then
  HOST_TEST_ARGS+=(-Pandroid.testInstrumentationRunnerArguments.class=io.github.juhjuhx.kuyin.acceptancehost.StaleUiObjectIdentityRegressionTest)
  echo FOCUSED_DIAGNOSTIC_NOT_ACCEPTANCE | tee "$R2_EVIDENCE_DIR/scope.txt"
elif [[ "${KUYIN_R2_HOST_PROBE:-false}" == "true" ]]; then
  # Two source-verified identities only. App prerequisite remains the complete suite.
  HOST_TEST_ARGS+=(-Pandroid.testInstrumentationRunnerArguments.class=io.github.juhjuhx.kuyin.acceptancehost.CoreTypingAcceptanceTest#zh06CandidateThenContinuedPhrasePreservesFirstCommit,io.github.juhjuhx.kuyin.acceptancehost.CoreTypingAcceptanceTest#zh01NoToneWoIsSelectableAndCommitted)
  echo FOCUSED_DIAGNOSTIC_NOT_ACCEPTANCE | tee "$R2_EVIDENCE_DIR/scope.txt"
else
  echo FULL_SUITE_EXECUTION | tee "$R2_EVIDENCE_DIR/scope.txt"
fi
capture_r2() {
  local phase="$1" failed=0 snapshot_presence
  date -u '+%Y-%m-%dT%H:%M:%SZ' > "$R2_EVIDENCE_DIR/$phase-time.txt" || failed=1
  adb shell am get-current-user > "$R2_EVIDENCE_DIR/$phase-user.txt" 2>&1 || failed=1
  for property in ro.product.model ro.product.manufacturer ro.build.version.sdk ro.product.cpu.abi ro.product.cpu.abilist ro.build.fingerprint; do
    printf '%s=' "$property" >> "$R2_EVIDENCE_DIR/$phase-device.txt" || failed=1
    adb shell getprop "$property" >> "$R2_EVIDENCE_DIR/$phase-device.txt" 2>&1 || failed=1
  done
  adb shell dumpsys package io.github.juhjuhx.kuyin > "$R2_EVIDENCE_DIR/$phase-package.txt" 2>&1 || failed=1
  if [[ -n "${KUYIN_R22_BINDING_ARM:-}" && "$phase" != after-app && "$phase" != after-host && "$phase" != after-b01 && "$phase" != final ]]; then
    # Full text IMMS dumps call client IMM.isActive/checkFocus on API33.
    # Until the first observation completes, collect only system-server proto.
    adb shell dumpsys -t 2 input_method --proto > "$R2_EVIDENCE_DIR/$phase-input-method.pb" 2> "$R2_EVIDENCE_DIR/$phase-input-method-error.txt" || failed=1
    python3 scripts/r2_ci_protocol.py server-proto "$R2_EVIDENCE_DIR/$phase-input-method.pb" "$R2_EVIDENCE_DIR/$phase-server-state.json" || failed=1
  else
    adb shell dumpsys -t 2 input_method > "$R2_EVIDENCE_DIR/$phase-input-method.txt" 2>&1 || failed=1
  fi
  adb shell dumpsys -t 2 window windows > "$R2_EVIDENCE_DIR/$phase-window.txt" 2>&1 || failed=1
  adb shell dumpsys -t 2 activity service io.github.juhjuhx.kuyin/com.example.ime.KuYinInputMethodService > "$R2_EVIDENCE_DIR/$phase-service.txt" 2>&1 || failed=1
  adb logcat -d -v threadtime -s KUYIN_R2_TEST_ONLY InputMethodManager InsetsController InputMethodService WindowManager ActivityTaskManager AndroidRuntime > "$R2_EVIDENCE_DIR/$phase-platform-logcat.txt" 2>&1 || failed=1
  if [[ -n "${KUYIN_R22_BINDING_ARM:-}" ]]; then
    adb shell log -p i -t KUYIN_R22_TEST_ONLY "$phase" || failed=1
    adb shell cat /proc/uptime > "$R2_EVIDENCE_DIR/$phase-uptime.txt" 2>&1 || failed=1
    adb shell settings --user 0 get secure default_input_method > "$R2_EVIDENCE_DIR/$phase-selected-ime.txt" 2>&1 || failed=1
    adb shell ime list -s --user 0 > "$R2_EVIDENCE_DIR/$phase-enabled-ime.txt" 2>&1 || failed=1
    adb shell pidof io.github.juhjuhx.kuyin > "$R2_EVIDENCE_DIR/$phase-pid.txt" 2>&1 || true
    local package_exit=0
    adb shell pm path --user 0 io.github.juhjuhx.kuyin > "$R2_EVIDENCE_DIR/$phase-package-path.txt" 2>&1 || package_exit=$?
    local absence_allowed=false
    if [[ "$phase" == before-install || ( "$phase" == final && "${KUYIN_R22_BINDING_ARM:-}" == legacy ) ]]; then absence_allowed=true; fi
    if [[ "$absence_allowed" == true && "$package_exit" == "1" && ! -s "$R2_EVIDENCE_DIR/$phase-package-path.txt" ]]; then
      echo "ABSENT_AT_$phase" > "$R2_EVIDENCE_DIR/$phase-package-state.txt"
    elif [[ "$package_exit" != "0" ]]; then
      failed=1
    fi
    adb logcat -d -v epoch -b events -s am_proc_start am_proc_died am_kill am_crash am_force_stop am_create_service am_destroy_service am_schedule_service_restart am_process_start_timeout > "$R2_EVIDENCE_DIR/$phase-process-events.txt" 2>&1 || failed=1
    adb logcat -d -v epoch -s ActivityManager ActivityTaskManager InputMethodManagerService InputMethodBindingController InputMethodService InputMethodManager KUYIN_R2_TEST_ONLY KUYIN_R22_TEST_ONLY AndroidRuntime > "$R2_EVIDENCE_DIR/$phase-binding-logcat.txt" 2>&1 || failed=1
    # Only the disposable emulator's known package; no dictionary/data inspection.
    local base_apk
    base_apk="$(tr -d '\r' < "$R2_EVIDENCE_DIR/$phase-package-path.txt" | sed -n 's/^package:\(\/data\/app\/[^[:space:]]*\/base\.apk\)$/\1/p')"
    if [[ -n "$base_apk" && "$base_apk" != *$'\n'* ]]; then
      adb shell sha256sum "$base_apk" > "$R2_EVIDENCE_DIR/$phase-installed-apk-sha256.txt" 2>&1 || failed=1
      python3 - "$R2_EVIDENCE_DIR/$phase-installed-apk-sha256.txt" <<'PY' || failed=1
import hashlib, pathlib, re, sys
actual = pathlib.Path(sys.argv[1]).read_text().split()
expected = hashlib.sha256(pathlib.Path('app/build/outputs/apk/debug/app-debug.apk').read_bytes()).hexdigest()
if len(actual) != 2 or not re.fullmatch('[0-9a-f]{64}', actual[0]) or actual[0] != expected:
    raise RuntimeError('Installed target APK does not match the shared build')
PY
    elif [[ "$absence_allowed" != true || "$package_exit" != "1" ]]; then
      echo "Expected one installed base.apk path at $phase" >&2
      failed=1
    fi
  fi
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
  if [[ -n "${KUYIN_R22_BINDING_ARM:-}" ]] && ! ensure_ci_user; then
    evidence_status=2
    echo USER_IDENTITY_CHANGED_FINAL_CAPTURE_SKIPPED > "$R2_EVIDENCE_DIR/final-capture-refused.txt"
  else
    capture_r2 final
    evidence_status=$?
  fi
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
if [[ -n "${KUYIN_R22_BINDING_ARM:-}" ]]; then
  capture_r2 before-install
fi
select_product_ime() {
  local selection_phase="${1:-installed-before-select}"
  ensure_ci_user
  if [[ "${KUYIN_R22_BINDING_ARM:-}" != "preinstalled" ]]; then
    ./gradlew -Pandroid.injected.device.serial="$KUYIN_CI_SERIAL" :app:installDebug --stacktrace
  fi
  ensure_ci_user
  if [[ -n "${KUYIN_R22_BINDING_ARM:-}" ]]; then capture_r2 "$selection_phase"; fi

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

install_exact() {
  ensure_ci_user
  adb install --user 0 -r -t "$1"
  ensure_ci_user
  capture_r2 "$2"
}

direct_instrumentation() {
  local layer="$1" runner="$2" suffix="$3" raw_exit=0 protocol_exit=0 capture_exit=0 identity_exit=0
  shift 3
  ensure_ci_user
  local extras=(--user 0 -w -r)
  if [[ "${KUYIN_R2_OBSERVER:-}" == "true" ]]; then extras+=(-e r2Diagnostics true); fi
  adb shell am instrument "${extras[@]}" "$@" "$runner" > "$R2_EVIDENCE_DIR/$suffix-instrumentation.txt" 2>&1 || raw_exit=$?
  local protocol_args=(--adb-exit "$raw_exit")
  if [[ "$suffix" == "b01" ]]; then protocol_args+=(--b01); fi
  python3 scripts/r2_ci_protocol.py "$layer" "$R2_EVIDENCE_DIR/$suffix-instrumentation.txt" "$R2_EVIDENCE_DIR/$suffix-protocol.json" "${protocol_args[@]}" || protocol_exit=$?
  ensure_ci_user || identity_exit=$?
  if [[ "$identity_exit" == "0" ]]; then capture_r2 "after-$suffix" || capture_exit=$?; fi
  printf 'raw_exit=%s\nprotocol_exit=%s\ncapture_exit=%s\nidentity_exit=%s\n' "$raw_exit" "$protocol_exit" "$capture_exit" "$identity_exit" > "$R2_EVIDENCE_DIR/$suffix-boundary.txt"
  if [[ "$protocol_exit" != "0" ]]; then return "$protocol_exit"; fi
  if [[ "$identity_exit" != "0" ]]; then return "$identity_exit"; fi
  if [[ "$capture_exit" != "0" ]]; then return 2; fi
}

if [[ "${KUYIN_R22_BINDING_ARM:-}" == "preinstalled" ]]; then
  install_exact app/build/outputs/apk/debug/app-debug.apk after-app-install
  install_exact app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk after-app-test-install
fi
select_product_ime app-before-select
ensure_ci_user
capture_r2 before-app
if [[ "${KUYIN_R22_BINDING_ARM:-}" == "preinstalled" ]]; then
  direct_instrumentation app io.github.juhjuhx.kuyin.test/androidx.test.runner.AndroidJUnitRunner app
else
  ./gradlew -Pandroid.injected.device.serial="$KUYIN_CI_SERIAL" "${R2_TEST_ARGS[@]}" :app:connectedDebugAndroidTest
fi
ensure_ci_user
if [[ "${KUYIN_R22_APP_ONLY:-false}" == "true" ]]; then
  echo APP_ONLY_CONTROL_HOST_NOT_REQUESTED > "$R2_EVIDENCE_DIR/execution-boundary.txt"
  exit 0
fi

if [[ "${KUYIN_R22_BINDING_ARM:-}" == "preinstalled" ]]; then
  install_exact acceptance-host/build/outputs/apk/debug/acceptance-host-debug.apk after-host-install
  install_exact acceptance-host/build/outputs/apk/androidTest/debug/acceptance-host-debug-androidTest.apk after-host-test-install
  select_product_ime host-before-select
  capture_r2 before-host
  direct_instrumentation host io.github.juhjuhx.kuyin.acceptancehost.test/androidx.test.runner.AndroidJUnitRunner host
  capture_r2 before-b01
  direct_instrumentation host io.github.juhjuhx.kuyin.acceptancehost.test/androidx.test.runner.AndroidJUnitRunner b01 -e class io.github.juhjuhx.kuyin.acceptancehost.DachenAcceptanceTest#b01CanonicalKeysAndShiRiHaveStrictPreeditEvidence
  exit 0
fi

# Gradle's product-suite installation/cleanup can reset the default IME.
# Establish the real precondition again before the external editor suite.
select_product_ime host-before-select
ensure_ci_user
capture_r2 before-host
./gradlew -Pandroid.injected.device.serial="$KUYIN_CI_SERIAL" :acceptance-host:connectedDebugAndroidTest "${HOST_TEST_ARGS[@]}"
