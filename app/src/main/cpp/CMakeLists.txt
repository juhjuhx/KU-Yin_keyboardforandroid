cmake_minimum_required(VERSION 3.18)
project(chewing-jni)

set(LIBCHEWING_SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR}/libchewing-src)

if(EXISTS ${LIBCHEWING_SRC_DIR}/src)
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
else()
    message(WARNING "Legacy C libchewing not found at ${LIBCHEWING_SRC_DIR}")
    message(WARNING "Run: git clone --depth=1 --branch v0.13.0 https://github.com/chenyf/libchewing.git libchewing-src")
    set(LIBCHEWING_LIB)
    set(LIBCHEWING_INCLUDE_DIRS)
endif()

add_library(chewing-jni SHARED chewing_jni.cpp)
set_target_properties(chewing-jni PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}
)
target_include_directories(chewing-jni PRIVATE ${LIBCHEWING_INCLUDE_DIRS})
target_link_libraries(chewing-jni PRIVATE ${LIBCHEWING_LIB} log)
target_compile_options(chewing-jni PRIVATE -Wall -Wextra -fPIC -std=c99)
