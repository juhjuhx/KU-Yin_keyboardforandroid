cmake_minimum_required(VERSION 3.22.1)
project(chewing-jni)

set(LIBCHEWING_SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libchewing-src)

# Check if we have the Rust-based libchewing with CAPI
if(EXISTS ${LIBCHEWING_SRC_DIR}/capi/include/chewing.h)
    message(STATUS "Using Rust libchewing CAPI (version 0.12.x)")
    
    # The Rust libchewing needs to be built via cargo first
    # For now, we'll just include the header for JNI binding development
    set(LIBCHEWING_INCLUDE_DIRS ${LIBCHEWING_SRC_DIR}/capi/include)
    set(LIBCHEWING_FOUND FALSE)
elseif(EXISTS ${LIBCHEWING_SRC_DIR}/src)
    message(STATUS "Using legacy C libchewing")
    set(LIBCHEWING_INCLUDE_DIRS ${LIBCHEWING_SRC_DIR}/src)
    file(GLOB LIBCHEWING_SOURCES
        ${LIBCHEWING_SRC_DIR}/src/*.c
        ${LIBCHEWING_SRC_DIR}/src/bopomofo/*.c
        ${LIBCHEWING_SRC_DIR}/src/zhuyin/*.c
        ${LIBCHEWING_SRC_DIR}/src/datetime/*.c
        ${LIBCHEWING_SRC_DIR}/src/zuyin2/*.c
    )
    add_library(chewing_static STATIC ${LIBCHEWING_SOURCES})
    set(LIBCHEWING_LIB chewing_static)
    set(LIBCHEWING_FOUND TRUE)
else()
    message(WARNING "libchewing source not found at ${LIBCHEWING_SRC_DIR}")
    set(LIBCHEWING_INCLUDE_DIRS)
    set(LIBCHEWING_LIB)
    set(LIBCHEWING_FOUND FALSE)
endif()

add_library(chewing-jni SHARED chewing_jni.cpp)

if(LIBCHEWING_FOUND)
    target_include_directories(chewing-jni PRIVATE ${LIBCHEWING_INCLUDE_DIRS})
    target_link_libraries(chewing-jni PRIVATE ${LIBCHEWING_LIB} log)
    target_compile_options(chewing-jni PRIVATE -Wall -Wextra -fPIC -std=c99)
else()
    # When using Rust libchewing, we need the pre-built .so or build it separately
    # For now, just include headers and link against liblog
    target_include_directories(chewing-jni PRIVATE ${LIBCHEWING_INCLUDE_DIRS})
    target_link_libraries(chewing-jni PRIVATE log)
    message(WARNING "libchewing not pre-built. Build with: cd capi && cargo build --release --target-dir ../build/cargo")
endif()

# Set output directory for .so files
set_target_properties(chewing-jni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}
)
