#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BOOTSTRAP="$ROOT/scripts/bootstrap_native_deps.sh"
pinned_prebuilt="$(sed -n 's/^PREBUILT_COMMIT="\([^"]*\)"/\1/p' "$BOOTSTRAP")"
pinned_libchewing="$(sed -n 's/^LIBCHEWING_SOURCE_COMMIT="\([^"]*\)"/\1/p' "$BOOTSTRAP")"
latest_prebuilt="$(git ls-remote https://github.com/fcitx5-android/prebuilt.git refs/heads/master | awk '{print $1}')"
tracked_libchewing_release="0.13.1"
release_json="$(curl -fsSL https://codeberg.org/api/v1/repos/chewing/libchewing/releases/latest || true)"
latest_libchewing_release="$(printf '%s' "$release_json" | python3 -c 'import json,sys; s=sys.stdin.read().strip(); print((json.loads(s).get("tag_name") or "unknown") if s else "unknown")' 2>/dev/null || echo unknown)"
latest_libchewing_release="${latest_libchewing_release#v}"
prebuilt_changed=false; libchewing_changed=false
[[ -n "$latest_prebuilt" && "$latest_prebuilt" != "$pinned_prebuilt" ]] && prebuilt_changed=true
[[ "$latest_libchewing_release" != "unknown" && "$latest_libchewing_release" != "$tracked_libchewing_release" ]] && libchewing_changed=true
changed=false; if [[ "$prebuilt_changed" == true || "$libchewing_changed" == true ]]; then changed=true; fi
printf 'KU-Yin upstream check\npinned_prebuilt=%s\nlatest_prebuilt=%s\npinned_libchewing_source=%s\ntracked_libchewing_release=%s\nlatest_libchewing_release=%s\n' "$pinned_prebuilt" "$latest_prebuilt" "$pinned_libchewing" "$tracked_libchewing_release" "$latest_libchewing_release"
if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
  { echo "pinned_prebuilt=$pinned_prebuilt"; echo "latest_prebuilt=$latest_prebuilt"; echo "pinned_libchewing=$pinned_libchewing"; echo "tracked_libchewing_release=$tracked_libchewing_release"; echo "latest_libchewing_release=$latest_libchewing_release"; echo "prebuilt_changed=$prebuilt_changed"; echo "libchewing_changed=$libchewing_changed"; echo "changed=$changed"; } >> "$GITHUB_OUTPUT"
fi
