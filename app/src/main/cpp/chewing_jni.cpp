#include <jni.h>
#include <cstdint>
#include <cstring>
#include <limits>
#include <string>
#include <vector>
#include <chewing.h>

namespace {

inline ChewingContext *contextFrom(jlong handle) {
    return reinterpret_cast<ChewingContext *>(static_cast<intptr_t>(handle));
}

inline jlong handleFrom(ChewingContext *ctx) {
    return static_cast<jlong>(reinterpret_cast<intptr_t>(ctx));
}

// libchewing returns standard UTF-8. NewStringUTF expects JNI Modified UTF-8,
// which cannot represent a four-byte supplementary-plane character directly.
jstring utf8String(JNIEnv *env, const char *text) {
    const size_t size = text ? std::strlen(text) : 0;
    if (size > static_cast<size_t>(std::numeric_limits<jsize>::max())) {
        jclass error = env->FindClass("java/lang/IllegalArgumentException");
        if (error) {
            env->ThrowNew(error, "libchewing UTF-8 output exceeds Java array size");
            env->DeleteLocalRef(error);
        }
        return nullptr;
    }
    jclass string_class = env->FindClass("java/lang/String");
    if (!string_class) return nullptr;
    jmethodID constructor = env->GetMethodID(string_class, "<init>", "([BLjava/lang/String;)V");
    if (!constructor) {
        env->DeleteLocalRef(string_class);
        return nullptr;
    }
    jbyteArray bytes = env->NewByteArray(static_cast<jsize>(size));
    if (!bytes) {
        env->DeleteLocalRef(string_class);
        return nullptr;
    }
    if (size > 0) {
        env->SetByteArrayRegion(bytes, 0, static_cast<jsize>(size), reinterpret_cast<const jbyte *>(text));
    }
    if (env->ExceptionCheck()) {
        env->DeleteLocalRef(bytes);
        env->DeleteLocalRef(string_class);
        return nullptr;
    }
    jstring encoding = env->NewStringUTF("UTF-8"); // ASCII, valid Modified UTF-8.
    jstring result = encoding ? static_cast<jstring>(env->NewObject(string_class, constructor, bytes, encoding)) : nullptr;
    if (encoding) env->DeleteLocalRef(encoding);
    env->DeleteLocalRef(bytes);
    env->DeleteLocalRef(string_class);
    return result;
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
    if (!ctx) return utf8String(env, "");
    const char *text = chewing_buffer_String_static(ctx);
    return utf8String(env, text);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1buffer_1check(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_buffer_Check(ctx)) : 0;
}

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1bopomofo_1string_1static(
    JNIEnv *env, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    if (!ctx) return utf8String(env, "");
    const char *text = chewing_bopomofo_String_static(ctx);
    return utf8String(env, text);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1bopomofo_1check(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_bopomofo_Check(ctx)) : 0;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cursor_1current(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_cursor_Current(ctx)) : 0;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1open(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_cand_open(ctx)) : -1;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1close(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_cand_close(ctx)) : -1;
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
    return text ? utf8String(env, text) : nullptr;
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
    if (!ctx) return utf8String(env, "");
    const char *text = chewing_commit_String_static(ctx);
    return utf8String(env, text);
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

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1config_1set_1int(
    JNIEnv *env, jobject, jlong handle, jstring option, jint value) {
    auto *ctx = contextFrom(handle);
    if (!ctx || !option) return -1;
    const char *name = env->GetStringUTFChars(option, nullptr);
    if (!name) return -1;
    const int result = chewing_config_set_int(ctx, name, value);
    env->ReleaseStringUTFChars(option, name);
    return static_cast<jint>(result);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1clean_1bopomofo_1buf(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_clean_bopomofo_buf(ctx)) : -1;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1get_1phone_1seq_1len(
    JNIEnv *, jobject, jlong handle) {
    auto *ctx = contextFrom(handle);
    return ctx ? static_cast<jint>(chewing_get_phoneSeqLen(ctx)) : 0;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1ack(
    JNIEnv *, jobject, jlong handle) {
    if (auto *ctx = contextFrom(handle)) chewing_ack(ctx);
}

JNIEXPORT jobjectArray JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1strings(
    JNIEnv *env, jobject, jlong handle) {
    std::vector<std::string> candidates;
    if (auto *ctx = contextFrom(handle)) {
        // Enumerate once: by-index static recomputes the entire fuzzy lookup
        // for every index, which becomes quadratic for incomplete syllables.
        chewing_cand_Enumerate(ctx);
        while (chewing_cand_hasNext(ctx) > 0) {
            const char *text = chewing_cand_String_static(ctx);
            candidates.emplace_back(text ? text : "");
        }
    }
    jclass string_class = env->FindClass("java/lang/String");
    if (!string_class) return nullptr;
    jobjectArray result = env->NewObjectArray(
        static_cast<jsize>(candidates.size()), string_class, nullptr);
    env->DeleteLocalRef(string_class);
    if (!result) return nullptr;
    for (jsize i = 0; i < static_cast<jsize>(candidates.size()); ++i) {
        jstring text = utf8String(env, candidates[i].c_str());
        if (!text) return nullptr;
        env->SetObjectArrayElement(result, i, text);
        env->DeleteLocalRef(text);
        if (env->ExceptionCheck()) return nullptr;
    }
    return result;
}

}  // extern "C"
