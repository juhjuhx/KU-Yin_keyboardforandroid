cmake_minimum_required(VERSION 3.18)
project(chewing-jni)

# ─── libchewing source build ───────────────────────────────────────
# Download libchewing source into this directory:
#   git clone --depth=1 https://github.com/chenyf/libchewing.git /libchewing-src
# Then uncomment the lines below.
#
# option(LIBCHEWING_BUILD_FROM_SOURCE  Build libchewing from source ON)
# if(LIBCHEWING_BUILD_FROM_SOURCE)
#     # Build libchewing as a static library
#     set(LIBCHEWING_SRC_DIR /libchewing-src)
#     add_subdirectory( /libchewing-build)
#     set(LIBCHEWING_LIB chewing_static)
#     set(LIBCHEWING_INCLUDE_DIRS /src)
# else()
#     # Use prebuilt static library (download from releases)
#     set(LIBCHEWING_LIB /prebuilt/libchewing_capi.a)
#     set(LIBCHEWING_INCLUDE_DIRS /prebuilt/include)
# endif()

# For now: stub mode — no native library built
# The Kotlin external functions will throw UnsatisfiedLinkError at runtime
# until NDK is configured and CMake runs.

# ─── JNI shared library ────────────────────────────────────────────
add_library(chewing-jni SHARED
    chewing_jni.cpp
)

# Target ABIs
set_target_properties(chewing-jni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY /../jni/
)

# Include dirs for chewing.h
target_include_directories(chewing-jni PRIVATE
    /include
)

# Link against libchewing when available
# target_link_libraries(chewing-jni PRIVATE )

# NDK flags
target_compile_options(chewing-jni PRIVATE
    -Wall -Wextra
    -fPIC
)