# P0-R 新 Debug APK 來源

唯一交付檔：`/Users/huang/Desktop/KU-Yin-P0R-2026-10-10/KU-Yin-P0R-core-typing-debug.apk`。

```text
APPLICATION_ID=io.github.juhjuhx.kuyin
VERSION_CODE=20002
VERSION_NAME=0.2.0-p0r.1
APK_BYTES=32276066
APK_SHA256=14082a60527ef57542d62cfb2bfaac1ed53a99080fb5af7f0de2792862fe260f
PRODUCT_SOURCE_SHA=9f5db676a17b5770a738b5c20da937f9211e5a34
BASE_SHA=85711bc8c749d488923954e8f4c173a493bf27eb
DEBUG_SIGNER_SHA256=46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94
BUILD_COMPLETED_UTC=2026-10-10T13:51:01.099951+00:00
```

來源本機提交 `fix: restore core typing and offline English completion`。APK 是最後 review 的 working-tree bytes 建置，之後才建立本機 code commit；95 個 build source files 逐 byte 等於上述 commit，外部 manifest 保存 hashes，**沒有聲稱 source SHA 嵌入 APK**。文件提交另列，不能將其 SHA 假裝為 product 編譯來源。原始 Gradle 輸出 `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p0-core-typing-recovery/app/build/outputs/apk/debug/app-debug.apk`；Desktop 副本 SHA 相等，不依檔名或 mtime 判斷版本。

Desktop 同目錄的 `來源與雜湊.json`、`SHA256SUMS`、`安裝與測試.md`；工區 `build/p0r-evidence/delivery-manifest.json` 保存同一份 manifest。後續文件 HEAD 登錄在外部 manifest，避免文件自我 hash 的循環。

## 身分與更新條件

aapt／apksigner exit 0，minSdk24/targetSdk36，signer V2 成功且憑證等於凍結 b223 包，version20002高於原20001；系統正常更新的靜態前提成立。**手機實際舊安裝身分仍 UNKNOWN，安裝未執行**；若手機有另一個簽章或更高版本，必須保留 package-manager error，不能 uninstall／pm clear。

設定頁現在從 BuildConfig 顯示 `v0.2.0-p0r.1 (20002)`，不再硬編碼舊 alpha。APK 無 INTERNET permission，沒有新增 telemetry、雲端候選或診斷日常輸入。此有效 Debug signer 不是 production release signing。

## Native／字典

- libchewing source pin `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`，prebuilt pin `3587ba3355711f0aca50136e787719f6562676b8`；未浮動 pins。
- 四 ABI armeabi-v7a／arm64-v8a／x86／x86_64 均新 link 正式 JNI；host-only Unicode fixture 不在 Android CMake source。
- 中文 word/tsi/swkb/symbols 四 assets hashes 等於凍結版本，完整逐-entry hashes 在 manifest。
- 英文 AOSP fixed commit `127336e9f29d69607eab55982324b210279ae8c5` 的 10k詞頻TSV，123878bytes，SHA `e44d3b87e830de6e57cc493f3cdb090404a477781299648464b2824e3b488e97`，附 SOURCE／NOTICE／Apache2 license。
- Unicode JNI final hash `9e2760c17fa7f6211e0b33e0d4f21d06ae108f0a946a476b8151d97633819076`；adapter final hash `60cb45cd22799025f604f8a0e23a869aec27ebfaeae20537ab822d010f086c18`。

## 同次建置產物

下列是新 P0 工區產物，不覆寫舊 P1 archive。一般使用者只安裝 Desktop 的產品 APK；其餘是未執行的 instrumentation 支援。

| 產物 | bytes | SHA256 | 原始路徑 |
|---|---:|---|---|
| app | 32276066 | `14082a60527ef57542d62cfb2bfaac1ed53a99080fb5af7f0de2792862fe260f` | `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p0-core-typing-recovery/app/build/outputs/apk/debug/app-debug.apk` |
| app-test | 965496 | `3938b3e3c1fb711ed6141d491a3841839e098dfb2c2ae81924b94f0d6106b8bd` | `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p0-core-typing-recovery/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk` |
| host | 861628 | `8e53e8933e1b436b3192dab4319a7da1fe58e98afba038447924f51e1481d829` | `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p0-core-typing-recovery/acceptance-host/build/outputs/apk/debug/acceptance-host-debug.apk` |
| host-test | 1037043 | `14ed0f16b2627b88ed34b70c6c44fc7433d52a0034349584a1768325109e48ee` | `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p0-core-typing-recovery/acceptance-host/build/outputs/apk/androidTest/debug/acceptance-host-debug-androidTest.apk` |

凍結 b223 與原四 APK／v3 ZIP 原 hashes 不變、ZIP CRC PASS；其他10工區 dirty bytes 未變，見 `frozen-artifacts-final.json`／`other-worktrees-final.json`。沒有 signed Release APK、push 或公開發布。
