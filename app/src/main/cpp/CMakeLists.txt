cmake_minimum_required(VERSION 3.22.1)
project(chewing_jni LANGUAGES C CXX)

set(LIBCHEWING_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/.deps/libchewing")
set(LIBCHEWING_INCLUDE_DIR "${LIBCHEWING_ROOT}/include/chewing")
set(LIBCHEWING_ARCHIVE "${LIBCHEWING_ROOT}/${ANDROID_ABI}/lib/libchewing_capi.a")
set(LIBCHEWING_HEADER "${LIBCHEWING_INCLUDE_DIR}/chewing.h")

if(NOT EXISTS "${LIBCHEWING_HEADER}")
    message(FATAL_ERROR
        "Pinned libchewing headers are missing. Run scripts/bootstrap_native_deps.sh before Gradle/CMake. "
        "Expected: ${LIBCHEWING_HEADER}")
endif()

if(NOT EXISTS "${LIBCHEWING_ARCHIVE}")
    message(FATAL_ERROR
        "Pinned libchewing_capi.a is missing for ABI ${ANDROID_ABI}. "
        "Run scripts/bootstrap_native_deps.sh before Gradle/CMake. "
        "Expected: ${LIBCHEWING_ARCHIVE}")
endif()

add_library(libchewing_capi STATIC IMPORTED GLOBAL)
set_target_properties(libchewing_capi PROPERTIES
    IMPORTED_LOCATION "${LIBCHEWING_ARCHIVE}"
    INTERFACE_INCLUDE_DIRECTORIES "${LIBCHEWING_INCLUDE_DIR}"
)

add_library(chewing-jni SHARED chewing_jni.cpp)

target_compile_features(chewing-jni PRIVATE cxx_std_17)
target_compile_options(chewing-jni PRIVATE -Wall -Wextra -fPIC)
target_include_directories(chewing-jni PRIVATE "${LIBCHEWING_INCLUDE_DIR}")

find_library(LOG_LIB log REQUIRED)
find_library(ANDROID_LIB android REQUIRED)
find_library(DL_LIB dl REQUIRED)
find_library(M_LIB m REQUIRED)

target_link_libraries(
    chewing-jni
    PRIVATE
    libchewing_capi
    ${LOG_LIB}
    ${ANDROID_LIB}
    ${DL_LIB}
    ${M_LIB}
)
