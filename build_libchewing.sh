# Build libchewing as static library for Android
# Run this script to build libchewing for all Android ABIs

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LIBCHEWING_SRC="${SCRIPT_DIR}/app/src/main/cpp/libchewing-src/capi"
BUILD_DIR="${SCRIPT_DIR}/build/cargo"
NDK_DIR="${SCRIPT_DIR}/../../../android-ndk-r27d-windows"
ANDROID_ABI=${1:-"arm64-v8a"}
ANDROID_API=${2:-24}

echo "Building libchewing for ${ANDROID_ABI} API ${ANDROID_API}..."

# Source Rust environment
source "$HOME/.cargo/env" 2>/dev/null || true

# Set target
case ${ANDROID_ABI} in
    armeabi-v7a)
        RUST_TARGET="armv7-linux-androideabi"
        ;;
    arm64-v8a)
        RUST_TARGET="aarch64-linux-android"
        ;;
    x86)
        RUST_TARGET="i686-linux-android"
        ;;
    x86_64)
        RUST_TARGET="x86_64-linux-android"
        ;;
    *)
        echo "Unknown ABI: ${ANDROID_ABI}"
        exit 1
        ;;
esac

# Build
cd "${LIBCHEWING_SRC}"
cargo build --release \
    --target ${RUST_TARGET} \
    --target-dir ${BUILD_DIR} \
    2>&1

# Copy to jniLibs
LIB_PATH="${BUILD_DIR}/${RUST_TARGET}/release/libchewing_capi.a"
if [ -f "${LIB_PATH}" ]; then
    mkdir -p "${SCRIPT_DIR}/app/src/main/jniLibs/${ANDROID_ABI}"
    cp "${LIB_PATH}" "${SCRIPT_DIR}/app/src/main/jniLibs/${ANDROID_ABI}/libchewing.a"
    echo "Built: ${SCRIPT_DIR}/app/src/main/jniLibs/${ANDROID_ABI}/libchewing.a"
else
    echo "Error: Failed to build libchewing"
    exit 1
fi

echo "Build complete!"
