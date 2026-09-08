// Wave 4 T21: JNI bridge for libchewing C API
// Build with: ./gradlew :app:externalNativeBuildDebug
// Requires: Android NDK r25+, libchewing source in libchewing-src/

#include <jni.h>
#include <string>
#include <chewing.h>

extern  C {

// ─── Context lifecycle ─────────────────────────────────────────────

JNIEXPORT jlong JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1new
  (JNIEnv *env, jobject thiz) {
    ChewingContext *ctx = chewing_new();
    return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1delete
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (ctx) chewing_delete(reinterpret_cast<ChewingContext *>(ctx));
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1reset
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (ctx) chewing_Reset(reinterpret_cast<ChewingContext *>(ctx));
}

// ─── Key event handling ────────────────────────────────────────────

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1default
  (JNIEnv *env, jobject thiz, jlong ctx, jint key) {
    if (!ctx) return 0;
    return chewing_handle_Default(reinterpret_cast<ChewingContext *>(ctx), key);
}

// ─── Preedit ───────────────────────────────────────────────────────

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1get_1composing_1str
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return env->NewStringUTF(");
    gchar *s = chewing_get_composing_str_ptr(reinterpret_cast<ChewingContext *>(ctx));
    return s ? env->NewStringUTF(s) : env->NewStringUTF(");
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1get_1cursor_1rest_1pos
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return 0;
    return chewing_get_cursor_rest_pos(reinterpret_cast<ChewingContext *>(ctx));
}

// ─── Candidates ────────────────────────────────────────────────────

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choice_1count
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return 0;
    return chewing_cand_ChoiceCount(reinterpret_cast<ChewingContext *>(ctx));
}

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choice_1string
  (JNIEnv *env, jobject thiz, jlong ctx, jint index) {
    if (!ctx) return nullptr;
    gchar **arr = chewing_cand_choiceString(reinterpret_cast<ChewingContext *>(ctx));
    if (!arr || index < 0) return nullptr;
    gchar *s = arr[index];
    return s ? env->NewStringUTF(s) : nullptr;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choice_1by_1index
  (JNIEnv *env, jobject thiz, jlong ctx, jint index) {
    if (!ctx) return;
    chewing_cand_ChoiceByIndex(reinterpret_cast<ChewingContext *>(ctx), index);
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1close
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return;
    chewing_cand_close(reinterpret_cast<ChewingContext *>(ctx));
}

// ─── Commit ────────────────────────────────────────────────────────

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1commit_1str
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return env->NewStringUTF(");
    gchar *s = chewing_commit_str_ptr(reinterpret_cast<ChewingContext *>(ctx));
    return s ? env->NewStringUTF(s) : env->NewStringUTF(");
}

// ─── Backspace ─────────────────────────────────────────────────────

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1backspace
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return;
    chewing_handle_Backspace(reinterpret_cast<ChewingContext *>(ctx));
}

// ─── Full/Half toggle ──────────────────────────────────────────────

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1full_1half
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return;
    chewing_handle_FullHalf(reinterpret_cast<ChewingContext *>(ctx));
}

// ─── Layout ────────────────────────────────────────────────────────

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1set_1kb_1type
  (JNIEnv *env, jobject thiz, jlong ctx, jint kbtype) {
    if (!ctx) return;
    chewing_set_KBType(reinterpret_cast<ChewingContext *>(ctx), kbtype);
}

// ─── User dictionary ───────────────────────────────────────────────

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1load_1userphrase
  (JNIEnv *env, jobject thiz, jlong ctx, jstring pathJ) {
    if (!ctx) return -1;
    const char *path = env->GetStringUTFChars(pathJ, nullptr);
    if (!path) return -1;
    int result = chewing_load_userphrase(reinterpret_cast<ChewingContext *>(ctx), path);
    env->ReleaseStringUTFChars(pathJ, path);
    return result;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1store_1userphrase
  (JNIEnv *env, jobject thiz, jlong ctx, jstring pathJ) {
    if (!ctx) return;
    const char *path = env->GetStringUTFChars(pathJ, nullptr);
    if (!path) return;
    chewing_store_userphrase(reinterpret_cast<ChewingContext *>(ctx), path);
    env->ReleaseStringUTFChars(pathJ, path);
}

} // extern C