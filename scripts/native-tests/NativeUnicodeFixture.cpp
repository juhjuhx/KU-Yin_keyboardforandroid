#include <jni.h>
#include <cstdint>
#include <chewing.h>

// Host-only synthetic fixture. Not compiled into the Android product library.
extern "C" JNIEXPORT jint JNICALL Java_NativeUnicodeFixture_addWord(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = reinterpret_cast<ChewingContext *>(static_cast<intptr_t>(handle));
    return chewing_userphrase_add(ctx, "𠮷", "ㄐㄧˊ");
}
