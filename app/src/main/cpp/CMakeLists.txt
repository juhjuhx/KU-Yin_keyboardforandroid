cmake_minimum_required(VERSION 3.18)
project(chewing-jni)

# Clone libchewing source first:
#   cd app/src/main/cpp
#   git clone --depth=1 https://codeberg.org/chenyf/libchewing.git libchewing-src

set(LIBCHEWING_SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libchewing-src)

if(EXISTS ${LIBCHEWING_SRC_DIR})
    message(STATUS "Found libchewing source at ${LIBCHEWING_SRC_DIR}")
    add_subdirectory(${LIBCHEWING_SRC_DIR} ${CMAKE_CURRENT_BINARY_DIR}/libchewing-build)
    set(LIBCHEWING_LIB chewing_static)
    set(LIBCHEWING_INCLUDE_DIRS ${LIBCHEWING_SRC_DIR}/src)
else()
    message(WARNING "libchewing source not found")
    set(LIBCHEWING_LIB)
    set(LIBCHEWING_INCLUDE_DIRS)
endif()

add_library(chewing-jni SHARED chewing_jni.cpp)
set_target_properties(chewing-jni PROPERTIES LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
target_include_directories(chewing-jni PRIVATE ${LIBCHEWING_INCLUDE_DIRS})
target_link_libraries(chewing-jni PRIVATE ${LIBCHEWING_LIB} log)
target_compile_options(chewing-jni PRIVATE -Wall -Wextra -fPIC -std=c99)
