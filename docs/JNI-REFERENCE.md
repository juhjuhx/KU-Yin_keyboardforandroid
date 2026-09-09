# libchewing C API Reference for JNI

## Overview
This document describes the JNI functions exposed by chewing_jni.cpp
to the Android Kotlin code.

## ABI Mapping

| libchewing C API | JNI function name | Kotlin external fun |
|-----------------|-------------------|---------------------|
| chewing_new() | chewing_1new | chewing_new(): Long |
| chewing_delete(ctx) | chewing_1delete | chewing_delete(ctx: Long) |
| chewing_Reset(ctx) | chewing_1reset | chewing_reset(ctx: Long): Int |
| chewing_Init(dp, hp) | chewing_1init | chewing_init(ctx: Long, dataPath: String, hashPath: String): Int |
| chewing_Terminate() | chewing_1terminate | chewing_terminate() |
| chewing_handle_Default(ctx, key) | chewing_1handle_1default | chewing_handle_default(ctx: Long, key: Int): Int |
| chewing_handle_Backspace(ctx) | chewing_1handle_1backspace | chewing_handle_backspace(ctx: Long): Int |
| chewing_handle_Space(ctx) | chewing_1handle_1space | chewing_handle_space(ctx: Long): Int |
| chewing_buffer_String_static(ctx) | chewing_1buffer_1string_1static | chewing_buffer_string_static(ctx: Long): String |
| chewing_buffer_Check(ctx) | chewing_1buffer_1check | chewing_buffer_check(ctx: Long): Int |
| chewing_buffer_Len(ctx) | chewing_1buffer_1len | chewing_buffer_len(ctx: Long): Int |
| chewing_cursor_Current(ctx) | chewing_1cursor_1current | chewing_cursor_current(ctx: Long): Int |
| chewing_cand_open(ctx) | chewing_1cand_1open | chewing_cand_open(ctx: Long): Int |
| chewing_cand_close(ctx) | chewing_1cand_1close | chewing_cand_close(ctx: Long): Int |
| chewing_cand_TotalChoice(ctx) | chewing_1cand_1total_1choice | chewing_cand_total_choice(ctx: Long): Int |
| chewing_cand_TotalPage(ctx) | chewing_1cand_1total_1page | chewing_cand_total_page(ctx: Long): Int |
| chewing_cand_CurrentPage(ctx) | chewing_1cand_1current_1page | chewing_cand_current_page(ctx: Long): Int |
| chewing_cand_ChoicePerPage(ctx) | chewing_1cand_1choice_1per_1page | chewing_cand_choice_per_page(ctx: Long): Int |
| chewing_cand_string_by_index_static(ctx, idx) | chewing_1cand_1string_1by_1index_1static | chewing_cand_string_by_index_static(ctx: Long, index: Int): String? |
| chewing_cand_choose_by_index(ctx, idx) | chewing_1cand_1choose_1by_1index | chewing_cand_choose_by_index(ctx: Long, index: Int): Int |
| chewing_commit_preedit_buf(ctx) | chewing_1commit_1preedit_1buf | chewing_commit_preedit_buf(ctx: Long): Int |
| chewing_commit_String_static(ctx) | chewing_1commit_1string_1static | chewing_commit_string_static(ctx: Long): String |
| chewing_commit_Check(ctx) | chewing_1commit_1check | chewing_commit_check(ctx: Long): Int |
| chewing_set_ChiEngMode(ctx, mode) | chewing_1set_1chi_1eng_1mode | chewing_set_chi_eng_mode(ctx: Long, mode: Int) |
| chewing_get_ChiEngMode(ctx) | chewing_1get_1chi_1eng_1mode | chewing_get_chi_eng_mode(ctx: Long): Int |
| chewing_set_ShapeMode(ctx, mode) | chewing_1set_1shape_1mode | chewing_set_shape_mode(ctx: Long, mode: Int) |
| chewing_get_ShapeMode(ctx) | chewing_1get_1shape_1mode | chewing_get_shape_mode(ctx: Long): Int |
| chewing_set_KBType(ctx, kbtype) | chewing_1set_1kb_1type | chewing_set_kb_type(ctx: Long, kbtype: Int): Int |
| chewing_get_KBType(ctx) | chewing_1get_1kb_1type | chewing_get_kb_type(ctx: Long): Int |
| chewing_KBStr2Num(str) | chewing_1kb_1str2num | chewing_kb_str2num(kbStr: String): Int |
| chewing_userphrase_add(ctx, phrase, bopomofo) | chewing_1userphrase_1add | chewing_userphrase_add(ctx: Long, phrase: String, bopomofo: String): Int |
| chewing_userphrase_lookup(ctx, phrase, bopomofo) | chewing_1userphrase_1lookup | chewing_userphrase_lookup(ctx: Long, phrase: String, bopomofo: String): Int |

## Constants

| Name | Value | Description |
|------|-------|-------------|
| KB_DEFAULT | 0 | DaChen layout |
| KB_HSU | 1 | Hsu layout |
| KB_ET26 | 5 | Eten26 layout |
| CHINESE_MODE | 1 | Chinese input mode |
| SYMBOL_MODE | 0 | Symbol input mode |
| FULLSHAPE_MODE | 1 | Full-width characters |
| HALFSHAPE_MODE | 0 | Half-width characters |
| KEYSTROKE_IGNORE | 1 | Key was ignored |
| KEYSTROKE_COMMIT | 2 | Key committed |
| KEYSTROKE_BELL | 4 | Bell sound |

## Build Steps

1. Build libchewing static library:
   ```powershell
   .\build_libchewing.ps1
   ```

2. Build Android project:
   ```bash
   ./gradlew :app:assembleDebug
   ```

## Notes

- The JNI library name is "chewing-jni" (matches System.loadLibrary("chewing-jni"))
- libchewing must be built first as a static library (.a) before building the JNI wrapper
- The C API is from Rust libchewing 0.12.x (not the legacy C version)
- Data paths should point to Android assets or external storage
