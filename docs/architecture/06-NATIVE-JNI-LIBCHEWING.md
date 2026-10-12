# Native/JNI/libchewing

狀態：NORMATIVE／IMPLEMENTED（文內 PROPOSED/OPEN 例外）。目的：說明此邊界的實際責任、資料與故障處理。來源基線 `a6a4c654845338521700121ac06586887d1ec085`；R1工具／test-adapter不改產品。

## 實際 source files / evidence

- [app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt](../../app/src/main/java/com/example/androidkeyboard/engines/android/AndroidChewingEngine.kt)
- [app/src/main/java/com/example/ime/service/NativeChewingEngineOwner.kt](../../app/src/main/java/com/example/ime/service/NativeChewingEngineOwner.kt)
- [app/src/main/cpp/chewing_jni.cpp](../../app/src/main/cpp/chewing_jni.cpp)
- [app/src/main/cpp/CMakeLists.txt](../../app/src/main/cpp/CMakeLists.txt)
- [scripts/bootstrap_native_deps.sh](../../scripts/bootstrap_native_deps.sh)
- [NOTICE](../../NOTICE)

## Interface / ownership

AndroidChewingEngine是唯一產品JNI caller；external functions以Long handle傳ctx。JNI C++17連pinned C API，負責string ownership釋放、UTF-8 bytes→Java String（非直接ModifiedUTF8，避免補充平面損壞）。service owner持續live engine init-once/close-once；adapter disposable scratch各自finally delete，不能說所有查詢只用一個ctx。

bootstrap fetch prebuilt `3587ba3355711f0aca50136e787719f6562676b8`，associatedsource `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c` 是provenance，不由腳本重新編譯Rust/source。檢gitHEAD与各ABI archive/dictionary非空；沒有expected per-filehash gate。stage到app/src/main/cpp/.deps/libchewing及assets/libchewing；CMake imported static capi→自有chewing-jni shared。四ABI、API24、NDK28.2/CMake3.22.1不變。

## Storage / errors

installer將system assets寫到noBackupFilesDir/libchewing/system，live user=user/userdict.dat；native自行manage format／learning，不假定.dat是SQLite。system既存非空即跳過，無hash/revision replacement，pin更新需另設migration。startup failure走dictionaryfallback；scratch failure可能empty候選。記錄少量native load/init錯誤、不記按鍵／內容。

## Build and licensing evidence

Published v0.2.0-aie.1另外提供source/data/vendor/patch/JNI relink objects/NDK/Rust notices；四ABI relink .text/.rodata matching是歷史證據，不等於Rustarchives從源重編等價或全面LGPL法律簽核。不能只因bootstrap成功稱義務完成。

Tests：NativeCandidatePagingTest、NativeTypingEditorIntegrationTest及scripts/native probe。host integration缺property會Assume skip，fresh gates須明确啟用。ASAN、真device learning/reset、完整runtime依賴license inventory仍OPEN。
