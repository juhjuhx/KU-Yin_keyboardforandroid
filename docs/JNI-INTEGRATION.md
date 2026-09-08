# JNI 接入指南 — libchewing C API 綁定

> Wave 4 T21b | 2026-09-08 | **執行時激活，當前為 stub 模式**
> 狀態：待 NDK 環境啟用

---

## 1. 前置條件

| 項目 | 版本要求 | 獲取方式 |
|------|---------|---------|
| Android NDK | r25+ | SDK Manager > SDK Tools > NDK (Side by side) |
| CMake | 3.18+ | NDK 內建，无需单独安装 |
| libchewing 源碼 | 0.13.x | git clone https://github.com/chenyf/libchewing.git |

---

## 2. 下載 libchewing 源碼

`ash
cd app/src/main/cpp
git clone --depth=1 https://github.com/chenyf/libchewing.git libchewing-src
cd libchewing-src
git checkout v0.13.0  # 或使用最新穩定版
`

---

## 3. 修改 CMakeLists.txt 激活源碼構建

取消註解以下區塊（見 pp/src/main/cpp/CMakeLists.txt）：

`cmake
option(LIBCHEWING_BUILD_FROM_SOURCE  Build libchewing from source ON)
if(LIBCHEWING_BUILD_FROM_SOURCE)
    set(LIBCHEWING_SRC_DIR /libchewing-src)
    add_subdirectory( /libchewing-build)
    set(LIBCHEWING_LIB chewing_static)
    set(LIBCHEWING_INCLUDE_DIRS /src)
else()
    set(LIBCHEWING_LIB /prebuilt/libchewing_capi.a)
    set(LIBCHEWING_INCLUDE_DIRS /prebuilt/include)
endif()
`

並取消註解：
`cmake
target_link_libraries(chewing-jni PRIVATE )
target_include_directories(chewing-jni PRIVATE )
`

---

## 4. 修改 build.gradle.kts

在 ndroid { defaultConfig { } } 中加入：

`kotlin
defaultConfig {
    // ... 既有設定 ...
    ndkVersion = 25.2.9519653  // 與你的 NDK 版本一致
    externalNativeBuild {
        cmake {
            cppFlags += -std=c++17
            abiFilters += armeabi-v7a
            abiFilters += arm64-v8a
            abiFilters += x86
            abiFilters += x86_64
        }
    }
}

externalNativeBuild {
    cmake {
        path = file(src/main/cpp/CMakeLists.txt)
        version = 3.18.0
    }
}
`

---

## 5. 編譯與驗證

`ash
# Debug build（包含符号，便於除錯）
./gradlew :app:externalNativeBuildDebug

# 驗證 .so 已生成
ls app/build/intermediates/cmake/debug/obj/
# 應看到：arm64-v8a/libchewing-jni.so 等

# 完整 APK build
./gradlew :app:assembleDebug
`

---

## 6. libchewing C API 對照表

| Kotlin external | C 函式 | 說明 |
|----------------|--------|------|
| chewing_new() | chewing_new() | 建立上下文 |
| chewing_delete(ctx) | chewing_delete(ctx) | 釋放上下文 |
| chewing_reset(ctx) | chewing_Reset(ctx) | 重置所有狀態 |
| chewing_handle_default(ctx, key) | chewing_handle_Default(ctx, key) | 處理 KeyEvent |
| chewing_get_composing_str(ctx) | chewing_get_composing_str_ptr(ctx) | 取得拼字字串 |
| chewing_cand_choice_count(ctx) | chewing_cand_ChoiceCount(ctx) | 候選字數量 |
| chewing_cand_choice_string(ctx, i) | chewing_cand_choiceString(ctx, i) | 取得第 i 個候選 |
| chewing_cand_choice_by_index(ctx, i) | chewing_cand_ChoiceByIndex(ctx, i) | 選擇第 i 個候選 |
| chewing_commit_str(ctx) | chewing_commit_str_ptr(ctx) | 提交最終結果 |
| chewing_handle_backspace(ctx) | chewing_handle_Backspace(ctx) | 倒退鍵 |
| chewing_handle_full_half(ctx) | chewing_handle_FullHalf(ctx) | 全形/半形切換 |
| chewing_set_kb_type(ctx, type) | chewing_set_KBType(ctx, type) | 設定鍵盤佈局 |
| chewing_load_userphrase(ctx, path) | chewing_load_userphrase(ctx, path) | 載入使用者詞庫 |
| chewing_store_userphrase(ctx, path) | chewing_store_userphrase(ctx, path) | 儲存使用者詞庫 |

---

## 7. KBType 常數對照

| Kotlin Layout enum | libchewing KBType | 數值 |
|---|---|---|
| DACHEN | KB_DEFAULT | 1 |
| HSU | KB_HSU | 2 |
| Eten26 | KB_ET26 | 6 |

---

## 8. 已知限制

1. **libchewing 0.13.x** 是當前最新穩定版；較新版本可能改變 C API
2. **chewing_handle_Default** 的返回值在 0.13.x 中為 enum，非 int；需要 cast
3. **user dict 路徑** 必須是 App 可寫目錄（如 getFilesDir().absolutePath + /user_phrase.dat）

---

_本文件為 T21b 交付物，提供完整的 JNI 接入路徑。當前 stub 模式下所有 native 調用均無效，輸入行為等同普通鍵盤。_