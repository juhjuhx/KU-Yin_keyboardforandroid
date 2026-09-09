cmake_minimum_required(VERSION 3.22.1)
project(chewing-jni)

set(LIBCHEWING_SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libchewing-src)

# Check if we have the Rust-based libchewing with CAPI
if(EXISTS ${LIBCHEWING_SRC_DIR}/capi/include/chewing.h)
    message(STATUS "Using Rust libchewing CAPI (version 0.12.x)")
    set(LIBCHEWING_INCLUDE_DIRS ${LIBCHEWING_SRC_DIR}/capi/include)
    set(LIBCHEWING_FOUND TRUE)
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
    target_include_directories(chewing_static PRIVATE ${LIBCHEWING_INCLUDE_DIRS})
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
    # Link against log library
    target_link_libraries(chewing-jni PRIVATE log)
    
    # For Rust libchewing CAPI, we need to either:
    # 1. Build as static lib using cargo and link
    # 2. Or use pre-built .a/.so
    
    # Try to find pre-built static lib
    set(RUST_LIB_PATH ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libchewing.a)
    if(EXISTS ${RUST_LIB_PATH})
        message(STATUS "Found pre-built libchewing.a: ${RUST_LIB_PATH}")
        add_library(chewing_rust STATIC IMPORTED)
        set_target_properties(chewing_rust PROPERTIES
            IMPORTED_LOCATION ${RUST_LIB_PATH}
        )
        target_link_libraries(chewing-jni PRIVATE chewing_rust)
    else()
        message(WARNING "Pre-built libchewing.a not found. JNI will compile but may fail at runtime.")
        message(WARNING "To build: cd ${LIBCHEWING_SRC_DIR}/capi && cargo build --release --target-dir ../../../build/cargo")
    endif()
    
    target_compile_options(chewing-jni PRIVATE -Wall -Wextra -fPIC -std=c++11)
else()
    message(WARNING "libchewing not found!")
endif()

# Set output directory for .so files
set_target_properties(chewing-jni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}
)
