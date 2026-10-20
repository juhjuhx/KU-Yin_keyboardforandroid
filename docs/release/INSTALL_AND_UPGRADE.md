# 公開 Alpha 安裝與保資料更新

僅適用 [v0.2.0-aie.1](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1) Experimental Debug Alpha；沒有新 RC 下載公告。

## 先核對檔案

官方檔名 `KU-Yin-AIE-S1-candidate-browser-debug.apk`，30,364,775 bytes，SHA256：

```text
acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634
```

applicationId `io.github.juhjuhx.kuyin`；versionName `0.2.0-aie.1`；versionCode `20003`；minSdk 24。Debug certificate SHA256：

```text
46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94
```

macOS 用 `shasum -a 256 <APK>`；Android SDK 用 `apksigner verify --verbose --print-certs <APK>`。本輪核對簽章 v2 有效；不是正式 key custody 審查。

## 安全安裝與回復

1. 先確認 Android 系統原有鍵盤可用，記下如何切回；不要卸載原有鍵盤。
2. 已有 KU-Yin 時先核對已安裝 package／version／certificate。相同 package 不代表相同簽章；版本也不得降級。
3. 手機所有者同意後才安裝並暫選 KU-Yin，用合成文字測試；先避免敏感資料。
4. 遇 signature mismatch、downgrade 或安裝錯誤立即停止並回報原錯誤；不以 uninstall、pm clear 或刪 userdict 解決。
5. 測試後從系統設定切回原鍵盤；不要更改其他使用者的 IME 設定。

**保資料更新尚未取得本輪實機證據。** 改用新 Release key 不會自動繼承 Debug 使用者資料。本專案尚無完整個人學習匯出／清除；不要宣稱已有通用備份還原。正式簽章遷移方案未批准前，不能提供保證無損更新的新 RC。

來源／relink、原 Release Notes 及 SHA256SUMS 一併保留在官方 Release，身份索引見 [資產清單](PUBLIC_ALPHA_ASSETS.json)。問題回報見 [FAQ](../community/FAQ.md)。
