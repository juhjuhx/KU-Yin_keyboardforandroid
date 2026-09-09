// Wave 4 T21b: JNI bridge for libchewing C API (0.12.x - Rust reimplementation)
// Build: ./gradlew :app:externalNativeBuildDebug
// Requires: Android NDK r27d, libchewing CAPI in include/chewing/
//
// libchewing source: https://github.com/chewing/libchewing (Rust rewrite)
// CAPI: capi/include/chewing.h

#include <jni.h>
#include <string>
#include <chewing/chewing.h>

extern "C" {

/* ── Context lifecycle ───────────────────────────────────────────── */

JNIEXPORT jlong JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1new
  (JNIEnv *env, jobject thiz) {
    ChewingContext *ctx = chewing_new();
    return (jlong) (intptr_t) ctx;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1delete
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (c) chewing_delete(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1reset
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_Reset(c);
}

/* ── Initialization ──────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1init
  (JNIEnv *env, jobject thiz, jlong ctx, jstring data_path, jstring hash_path) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return -1;
    const char *dp = env->GetStringUTFChars(data_path, nullptr);
    const char *hp = env->GetStringUTFChars(hash_path, nullptr);
    if (!dp || !hp) {
      if (dp) env->ReleaseStringUTFChars(data_path, dp);
      if (hp) env->ReleaseStringUTFChars(hash_path, hp);
      return -1;
    }
    jint result = chewing_Init(dp, hp);
    env->ReleaseStringUTFChars(data_path, dp);
    env->ReleaseStringUTFChars(hash_path, hp);
    return result;
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1terminate
  (JNIEnv *env, jobject thiz) {
    chewing_Terminate();
}

/* ── Key event ───────────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1default
  (JNIEnv *env, jobject thiz, jlong ctx, jint key) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_handle_Default(c, key);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1backspace
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_handle_Backspace(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1handle_1space
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_handle_Space(c);
}

/* ── Preedit ─────────────────────────────────────────────────────── */

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1buffer_1string_1static
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return env->NewStringUTF("");
    const char *s = chewing_buffer_String_static(c);
    return s ? env->NewStringUTF(s) : env->NewStringUTF("");
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1buffer_1check
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_buffer_Check(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1buffer_1len
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_buffer_Len(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cursor_1current
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_cursor_Current(c);
}

/* ── Candidates ──────────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1open
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return -1;
    return (jint) chewing_cand_open(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1close
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return -1;
    return (jint) chewing_cand_close(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1total_1choice
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_cand_TotalChoice(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1total_1page
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_cand_TotalPage(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1current_1page
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_cand_CurrentPage(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choice_1per_1page
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_cand_ChoicePerPage(c);
}

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1string_1by_1index_1static
  (JNIEnv *env, jobject thiz, jlong ctx, jint index) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return nullptr;
    const char *s = chewing_cand_string_by_index_static(c, index);
    return s ? env->NewStringUTF(s) : nullptr;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1cand_1choose_1by_1index
  (JNIEnv *env, jobject thiz, jlong ctx, jint index) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return -1;
    return (jint) chewing_cand_choose_by_index(c, index);
}

/* ── Commit ──────────────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1commit_1preedit_1buf
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return -1;
    return (jint) chewing_commit_preedit_buf(c);
}

JNIEXPORT jstring JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1commit_1string_1static
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return env->NewStringUTF("");
    const char *s = chewing_commit_String_static(c);
    return s ? env->NewStringUTF(s) : env->NewStringUTF("");
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1commit_1check
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    return (jint) chewing_commit_Check(c);
}

/* ── Mode ────────────────────────────────────────────────────────── */

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1set_1chi_1eng_1mode
  (JNIEnv *env, jobject thiz, jlong ctx, jint mode) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return;
    chewing_set_ChiEngMode(c, mode);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1get_1chi_1eng_1mode
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return CHINESE_MODE;
    return (jint) chewing_get_ChiEngMode(c);
}

JNIEXPORT void JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1set_1shape_1mode
  (JNIEnv *env, jobject thiz, jlong ctx, jint mode) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return;
    chewing_set_ShapeMode(c, mode);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1get_1shape_1mode
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return HALFSHAPE_MODE;
    return (jint) chewing_get_ShapeMode(c);
}

/* ── Layout ──────────────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1set_1kb_1type
  (JNIEnv *env, jobject thiz, jlong ctx, jint kbtype) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return -1;
    return (jint) chewing_set_KBType(c, kbtype);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1get_1kb_1type
  (JNIEnv *env, jobject thiz, jlong ctx) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return KB_DEFAULT;
    return (jint) chewing_get_KBType(c);
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1kb_1str2num
  (JNIEnv *env, jobject thiz, jstring kb_str) {
    const char *s = env->GetStringUTFChars(kb_str, nullptr);
    if (!s) return -1;
    jint result = chewing_KBStr2Num(s);
    env->ReleaseStringUTFChars(kb_str, s);
    return result;
}

/* ── User dictionary ─────────────────────────────────────────────── */

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1userphrase_1add
  (JNIEnv *env, jobject thiz, jlong ctx, jstring phrase, jstring bopomofo) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return -1;
    const char *p = env->GetStringUTFChars(phrase, nullptr);
    const char *b = env->GetStringUTFChars(bopomofo, nullptr);
    if (!p || !b) {
      if (p) env->ReleaseStringUTFChars(phrase, p);
      if (b) env->ReleaseStringUTFChars(bopomofo, b);
      return -1;
    }
    jint result = chewing_userphrase_add(c, p, b);
    env->ReleaseStringUTFChars(phrase, p);
    env->ReleaseStringUTFChars(bopomofo, b);
    return result;
}

JNIEXPORT jint JNICALL
Java_com_example_androidkeyboard_engines_android_AndroidChewingEngine_chewing_1userphrase_1lookup
  (JNIEnv *env, jobject thiz, jlong ctx, jstring phrase, jstring bopomofo) {
    auto *c = reinterpret_cast<ChewingContext *>(ctx);
    if (!c) return 0;
    const char *p = env->GetStringUTFChars(phrase, nullptr);
    const char *b = env->GetStringUTFChars(bopomofo, nullptr);
    if (!p || !b) {
      if (p) env->ReleaseStringUTFChars(phrase, p);
      if (b) env->ReleaseStringUTFChars(bopomofo, b);
      return 0;
    }
    jint result = chewing_userphrase_lookup(c, p, b);
    env->ReleaseStringUTFChars(phrase, p);
    env->ReleaseStringUTFChars(bopomofo, b);
    return result;
}

} /* extern C */
