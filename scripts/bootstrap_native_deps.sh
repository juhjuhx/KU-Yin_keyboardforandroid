#!/usr/bin/env bash
set -euo pipefail

# P1 bridge: consume the same Android libchewing artifacts produced by the
# fcitx5-android prebuilder, pinned to an immutable repository commit.
# Provenance:
#   prebuilt: 3587ba3355711f0aca50136e787719f6562676b8
#   libchewing source used by prebuilder: a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c
PREBUILT_REPO="https://github.com/fcitx5-android/prebuilt.git"
PREBUILT_COMMIT="3587ba3355711f0aca50136e787719f6562676b8"
LIBCHEWING_SOURCE_COMMIT="a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c"
ABIS=(armeabi-v7a arm64-v8a x86 x86_64)
DICT_FILES=(tsi.dat word.dat swkb.dat symbols.dat)

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CACHE_ROOT="${KUYIN_DEPS_CACHE:-${RUNNER_TEMP:-${TMPDIR:-/tmp}}/kuyin-native-deps}"
CHECKOUT="$CACHE_ROOT/fcitx5-android-prebuilt"
NATIVE_DEST="$ROOT/app/src/main/cpp/.deps/libchewing"
ASSET_DEST="$ROOT/app/src/main/assets/libchewing"

rm -rf "$CHECKOUT"
mkdir -p "$CHECKOUT" "$NATIVE_DEST" "$ASSET_DEST"

git -C "$CHECKOUT" init -q
git -C "$CHECKOUT" remote add origin "$PREBUILT_REPO"
git -C "$CHECKOUT" fetch --depth=1 origin "$PREBUILT_COMMIT"
git -C "$CHECKOUT" checkout --detach -q FETCH_HEAD

actual_commit="$(git -C "$CHECKOUT" rev-parse HEAD)"
if [[ "$actual_commit" != "$PREBUILT_COMMIT" ]]; then
  echo "ERROR: expected prebuilt commit $PREBUILT_COMMIT, got $actual_commit" >&2
  exit 1
fi

rm -rf "$NATIVE_DEST" "$ASSET_DEST"
mkdir -p "$NATIVE_DEST/include" "$ASSET_DEST"

# The C API headers are ABI-independent. Keep one canonical copy next to the
# staged archives consumed by CMake.
cp -R "$CHECKOUT/libchewing/arm64-v8a/include/chewing" "$NATIVE_DEST/include/"

for abi in "${ABIS[@]}"; do
  src="$CHECKOUT/libchewing/$abi/lib/libchewing_capi.a"
  dst="$NATIVE_DEST/$abi/lib/libchewing_capi.a"
  if [[ ! -s "$src" ]]; then
    echo "ERROR: missing libchewing_capi.a for $abi at pinned prebuilt commit" >&2
    exit 1
  fi
  mkdir -p "$(dirname "$dst")"
  cp "$src" "$dst"
done

for file in "${DICT_FILES[@]}"; do
  src="$CHECKOUT/chewing-dict/$file"
  if [[ ! -s "$src" ]]; then
    echo "ERROR: missing dictionary $file at pinned prebuilt commit" >&2
    exit 1
  fi
  cp "$src" "$ASSET_DEST/$file"
done

cat > "$NATIVE_DEST/PROVENANCE.txt" <<EOF
fcitx5-android/prebuilt=$PREBUILT_COMMIT
chewing/libchewing=$LIBCHEWING_SOURCE_COMMIT
EOF

echo "KU-Yin native dependencies ready: $PREBUILT_COMMIT"
