// Wave 4 T21b: JNI bridge for libchewing C API (0.13.x)
// Build: ./gradlew :app:externalNativeBuildDebug
// Requires: Android NDK r25+, libchewing source in libchewing-src/
//
// libchewing source: https://codeberg.org/chenyf/libchewing

#include <jni.h>
#include <string>
#include <chewing.h>

extern  C {

/* ── Context lifecycle ───────────────────────────────────────────── */

JNIEXPORT jlong JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1new
  (JNIEnv *env, jobject thiz) {
    ChewingContext *ctx = chewing_new();
    return (jlong) ctx;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1delete
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (ctx) chewing_delete((ChewingContext *) ctx);
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1reset
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (ctx) chewing_Reset((ChewingContext *) ctx);
}

/* ── Key event ───────────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1default
  (JNIEnv *env, jobject thiz, jlong ctx, jint key) {
    if (!ctx) return 0;
    return (jint) chewing_handle_Default((ChewingContext *) ctx, (gint) key);
}

/* ── Preedit ─────────────────────────────────────────────────────── */

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1get_1composing_1str
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return env->NewStringUTF("");
    const gchar *s = chewing_get_composing_str_ptr((ChewingContext *) ctx);
    return s ? env->NewStringUTF(s) : env->NewStringUTF("");
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1get_1cursor_1rest_1pos
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return 0;
    return (jint) chewing_get_cursor_rest_pos((ChewingContext *) ctx);
}

/* ── Candidates ──────────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choice_1count
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return 0;
    return (jint) chewing_cand_ChoiceCount((ChewingContext *) ctx);
}

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choice_1string
  (JNIEnv *env, jobject thiz, jlong ctx, jint index) {
    if (!ctx) return nullptr;
    gchar **arr = chewing_cand_choiceString((ChewingContext *) ctx);
    if (!arr || index < 0) return nullptr;
    gchar *s = arr[index];
    return s ? env->NewStringUTF(s) : nullptr;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choice_1by_1index
  (JNIEnv *env, jobject thiz, jlong ctx, jint index) {
    if (!ctx) return;
    chewing_cand_ChoiceByIndex((ChewingContext *) ctx, (gint) index);
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1close
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return;
    chewing_cand_close((ChewingContext *) ctx);
}

/* ── Commit ──────────────────────────────────────────────────────── */

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1commit_1str
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return env->NewStringUTF("");
    const gchar *s = chewing_commit_str_ptr((ChewingContext *) ctx);
    return s ? env->NewStringUTF(s) : env->NewStringUTF("");
}

/* ── Backspace ───────────────────────────────────────────────────── */

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1backspace
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return;
    chewing_handle_Backspace((ChewingContext *) ctx);
}

/* ── Full/Half ───────────────────────────────────────────────────── */

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1full_1half
  (JNIEnv *env, jobject thiz, jlong ctx) {
    if (!ctx) return;
    chewing_handle_FullHalf((ChewingContext *) ctx);
}

/* ── Layout ──────────────────────────────────────────────────────── */

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1set_1kb_1type
  (JNIEnv *env, jobject thiz, jlong ctx, jint kbtype) {
    if (!ctx) return;
    chewing_set_KBType((ChewingContext *) ctx, (gint) kbtype);
}

/* ── User dictionary ─────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1load_1userphrase
  (JNIEnv *env, jobject thiz, jlong ctx, jstring pathJ) {
    if (!ctx) return -1;
    const char *path = env->GetStringUTFChars(pathJ, nullptr);
    if (!path) return -1;
    gint result = chewing_load_userphrase((ChewingContext *) ctx, path);
    env->ReleaseStringUTFChars(pathJ, path);
    return (jint) result;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1store_1userphrase
  (JNIEnv *env, jobject thiz, jlong ctx, jstring pathJ) {
    if (!ctx) return;
    const char *path = env->GetStringUTFChars(pathJ, nullptr);
    if (!path) return;
    chewing_store_userphrase((ChewingContext *) ctx, path);
    env->ReleaseStringUTFChars(pathJ, path);
}

} /* extern C */