#include <jni.h>
#include <cstdint>
#include <chewing.h>

namespace {

inline ChewingContext *contextFrom(jlong handle) {
    return reinterpret_cast<ChewingContext *>(static_cast<intptr_t>(handle));
}

inline jlong handleFrom(ChewingContext *ctx) {
    return static_cast<jlong>(reinterpret_cast<intptr_t>(ctx));
}

}  // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1new2(
    JNIEnv *env,
    jobject,
    jstring system_data_path,
    jstring user_data_path) {
    if (system_data_path == nullptr || user_data_path == nullptr) return 0;

    const char *system_path = env->GetStringUTFChars(system_data_path, nullptr);
    if (system_path == nullptr) return 0;

    const char *user_path = env->GetStringUTFChars(user_data_path, nullptr);
    if (user_path == nullptr) {
        env->ReleaseStringUTFChars(system_data_path, system_path);
        return 0;
    }

    ChewingContext *ctx = chewing_new2(system_path, user_path, nullptr, nullptr);

    env->ReleaseStringUTFChars(user_data_path, user_path);
    env->ReleaseStringUTFChars(system_data_path, system_path);
    return handleFrom(ctx);
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1delete(
    JNIEnv *, jobject, jlong handle) {
    if (auto *ctx = contextFrom(handle)) chewing_delete(ctx);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1reset(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_Reset(ctx)) : 0;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1default(
    JNIEnv *, jobject, jlong handle, jint key) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_handle_Default(ctx, key)) : 0;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1backspace(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_handle_Backspace(ctx)) : 0;
}

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1buffer_1string_1static(
    JNIEnv *env, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    if (!ctx) return env->NewStringUTF("");
    const char *text = chewing_buffer_String_static(ctx);
    return env->NewStringUTF(text ? text : "");
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1buffer_1check(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_buffer_Check(ctx)) : 0;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1open(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_cand_open(ctx)) : -1;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1total_1choice(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_cand_TotalChoice(ctx)) : 0;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1total_1page(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_cand_TotalPage(ctx)) : 0;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choice_1per_1page(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_cand_ChoicePerPage(ctx)) : 0;
}

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1string_1by_1index_1static(
    JNIEnv *env, jobject, jlong handle, jint index) {
    auto *ctx = contextFrom(handle);
    if (!ctx) return nullptr;
    const char *text = chewing_cand_string_by_index_static(ctx, index);
    return text ? env->NewStringUTF(text) : nullptr;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choose_1by_1index(
    JNIEnv *, jobject, jlong handle, jint index) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_cand_choose_by_index(ctx, index)) : -1;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1commit_1preedit_1buf(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_commit_preedit_buf(ctx)) : -1;
}

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1commit_1string_1static(
    JNIEnv *env, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    if (!ctx) return env->NewStringUTF("");
    const char *text = chewing_commit_String_static(ctx);
    return env->NewStringUTF(text ? text : "");
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1commit_1check(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_commit_Check(ctx)) : 0;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1set_1chi_1eng_1mode(
    JNIEnv *, jobject, jlong handle, jint mode) {
    if (auto *ctx = contextFrom(handle)) chewing_set_ChiEngMode(ctx, mode);
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1set_1shape_1mode(
    JNIEnv *, jobject, jlong handle, jint mode) {
    if (auto *ctx = contextFrom(handle)) chewing_set_ShapeMode(ctx, mode);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1set_1kb_1type(
    JNIEnv *, jobject, jlong handle, jint kb_type) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_set_KBType(ctx, kb_type)) : -1;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1set_1auto_1learn(
    JNIEnv *, jobject, jlong handle, jint mode) {
    if (auto *ctx = contextFrom(handle)) chewing_set_autoLearn(ctx, mode);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1get_1auto_1learn(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_get_autoLearn(ctx)) : -1;
}

}  // extern "C"
