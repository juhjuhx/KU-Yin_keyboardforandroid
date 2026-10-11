# AIE S0 學習重設安全

BASE_SHA: 84dcf9a85d43b079a053b5b78e6fdcaa90a5bd2f
PRODUCT_SHA: 9f5db676a17b5770a738b5c20da937f9211e5a34
BRANCH: fix/aie-s0-learning-reset-safety

## 已核對的缺陷
MainActivity.kt:699–701 只呼叫 fallback clearLearnedFrequencies，無條件成功；ZhuyinDictionary.kt:1369 只清 kuyin_dict_prefs。Native stores 是 noBackupFilesDir/libchewing/user/userdict.dat，context 由 service 的 NativeChewingEngineOwner 持有。allowBackup=false；重設 composing 不等於刪除個人詞庫。

## 安全處置（本輪明確允許的 unsupported 分支）
完整 reset 暫時不提供。停用原按鈕，旁邊明示「尚未支援安全重設原生與備援學習紀錄；目前不會刪除任何資料」。移除 confirm、fallback-only 清除與成功訊息路徑。沒有新檔案刪除、沒有 prefs clear、沒有 native close/reset/reopen。此處不宣稱 native reset success。

現有 native user dictionary 與 fallback prefs 無跨 store transaction；目前沒有 service 排他 close/flush/reset/reopen 協定。不能假定刪掉仍開啟的 userdict.dat 可以完整清除，亦不能用模擬測試冒稱 native erase。完整重設另片，必須審查 scope/active context/rollback/驗證。

## 測試與範圍
先對正式設定頁測試 disabled、unsupported 說明、原 confirm flow 不可到達、fallback/native synthetic bytes/keyboard prefs/其他檔案保留。另外既有 fallback clear API 與 NativeLearningPolicy 測試保留。真 native 初始化及 P0-R typing gate 需重新執行，不代表 native reset 已支援。實機 reset 不執行。

OCR: v1.12.13；health exit1，Anthropic401 invalid x-api-key；request req_011CfukguqdyTTrfSRGwUSxd；BLOCKED_BY_PROVIDER。沒有 OCR approval。
