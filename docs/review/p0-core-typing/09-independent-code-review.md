# P0-R 獨立最終程式碼審查

審查日期：2026-10-10。審查基線為 `85711bc8c749d488923954e8f4c173a493bf27eb`，目標工作樹為 `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p0-core-typing-recovery`。我重新讀取最終差異、輸入服務與 session、JNI／engine、政策與資產來源，並核對 Unicode 綠燈、Gradle XML、APK 身分及簽章。未修改產品碼、測試、Gradle 或裝置；本文件是唯一新增的審查產物。

## 結論

最終 source diff 未見尚未解決的程式碼阻擋項。前輪提出的候選身分錯配、未完成音節跨標點／模式切換，以及同游標外部移除 composing span 後重送舊音節等問題，已由實作與針對性測試覆蓋。審查期間另確認 JNI 用 `NewStringUTF` 讀取標準 UTF-8 會破壞補充平面字元；Unicode 修正的 production JNI 候選、索引讀取、buffer 與 commit 均通過精確字元比對。最後新增的設定頁版本／詞庫描述修正也已讀碼，更新 BuildConfig 版本顯示並移除沒有證據支持的 `1250+` 完整性宣稱；final delivery build 已重新通過。

這是程式碼審查結果，不是產品或裝置驗收結果。`PRODUCT_TYPING_ACCEPTANCE=FAIL_BY_USER_REPORT`、`DEVICE_RUNTIME=NOT_TESTED` 仍成立；Robolectric／Mac JNI 不代表 Android OEM、觸控或各 app 的實機行為。本審查不批准發佈或 M4。

## 發現與處置

| 項目 | 最終狀態 | 審查證據與邊界 |
|---|---|---|
| JNI 補充平面 UTF-8 | 已修正 | RED 重現：host-only fixture 將 `𠮷`／`ㄐㄧˊ` 注入 `:memory:` 字典，production JNI 輸入 `ru6` 後候選清單末項被解碼成 `ð`，找不到 `𠮷`。修正後以 `String(byte[], "UTF-8")` 解碼五個 CAPI 輸出入口；green 對候選清單、by-index、buffer、commit 精確斷言 `𠮷`。見 [RED](../../../build/p0r-evidence/unicode-red/production-adapter-unicode-red.log)、[最終 native green JSON](../../../build/p0r-evidence/unicode-final-green/production-adapter-unicode-final-green.json)、[最終 JNI](../../../app/src/main/cpp/chewing_jni.cpp)。此 fixture 編入 host probe，不在 Android CMake 產品 source。 |
| 候選身分與錯誤候選提交 | 已修正 | 預覽候選以文字與重複次數識別；即時 context 重新解析候選並核對完整 buffer，不把預覽 ordinal 直接用於個人字典。候選已變動或不能精確匹配時保留組字並回報不可用，避免悄悄提交另一個預設字。production adapter probe 覆蓋候選選取與單次 commit；UI／session 測試涵蓋候選點選寫入 editor。 |
| 未完成音節遇標點／模式切換 | 已修正 | commit 無可用轉換時保留 preedit 和錯誤狀態；不追加標點、不切換模式，也不把後續 Enter 當成 editor action。最終 app 測試包含 `PunctuationCommandTest`、`PunctuationTransitionTest` 與 `IncompleteModeSwitchUiTest`。 |
| 外部移除 composing span、游標未移動 | 已修正 | `EditorEffectTracker` 只認領本次連線／generation 中與 selection 及 composing 範圍完全相符的通知。`EditorCommitRecoveryTest` 的「external composing span removal at same cursor」案例確認 decoder reset 後再輸入不會把舊音節送出兩次。 |
| InputConnection false ACK | 以保守恢復處理 | false ACK 即使 editor 可能已變更，也將有序未確認文字保留在本地、停止後續輸入且不自動重送；新 editor 不會收到舊 editor 的待確認文字。敏感欄位的待確認內容會遮蔽。最終 `EditorCommitRecoveryTest` 共 8 案通過。這些測試使用 Robolectric `EditText`／`InputConnection`，不涵蓋 OEM 非同步回呼。 |
| native replay history | 有限探針已驗證 | 只在 pending key/backspace 回到相同 native checkpoint 時移除 inverse pair，不合成鍵或改寫 trace。最終 production adapter probe 對 `ji3`、`ji3v`、`ji3vu` 各重複 100 次，再比較後續按鍵、刪除、翻頁、選取、commit 與 clean context；結果相等。這證明列出的前綴與動作，不是 Rust decoder 所有隱藏狀態在任意序列上的形式等價。 |
| 英文建議與隱私政策 | 審查通過 | `NO_SUGGESTIONS`／`AUTO_COMPLETE` 會停用英文詞典建議，保留注音中文轉換；敏感／force-ASCII 欄位不保留英文 token 或 native 個人學習。預覽使用 `:memory:` 並停用 learning；host probe 記錄預覽前後 userdict SHA 相同，實際啟用 learning 的 commit 後 SHA 改變。最後兩者有分開的測試與政策入口。 |
| 設定頁版本與詞庫說明 | 已修正並通過最後 build | `UpstreamAndCommunityCards` 改從 `BuildConfig.VERSION_NAME`／`VERSION_CODE` 顯示本地版本，取代舊硬編碼 alpha 版本；詞庫文字改為「隨 App 提供的離線詞庫」，移除沒有來源證據支持的 `1250+ 詞組／完整性已校驗`。檢視的最終檔案 SHA-256 為 `ac750a72a557b41721eb18ad4f6f405e9b48ca020f4e83431b5fed279ff2e2b2`。 |

## 來源、測試與 APK 身分

final Unicode manifest 將 production adapter 與本工作樹來源雜湊綁定：`chewing_jni.cpp=9e2760c17fa7f6211e0b33e0d4f21d06ae108f0a946a476b8151d97633819076`，`AndroidChewingEngine.kt=60cb45cd22799025f604f8a0e23a869aec27ebfaeae20537ab822d010f086c18`。manifest 記載 libchewing source commit `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`、prebuilt commit `3587ba3355711f0aca50136e787719f6562676b8`、字典檔雜湊，native 編譯與 production-adapter Unicode probe 皆 exit 0，`failures=[]`；裝置欄位明列 `NOT_TESTED`。見 [manifest](../../../build/p0r-evidence/unicode-final-green/host-probe-manifest.json)。

最後 delivery Gradle 建置 exit 0（48 秒）；四 ABI native library 已重新連結，app／host androidTest APK assemble、Debug APK 與 `compileReleaseKotlin` 成功。fresh 測試 XML 合計 app 49 類／229 案、acceptance-host 3 類／37 案，失敗、錯誤、略過皆為 0；host 單元測試任務在此輪為 UP-TO-DATE。`NativeTypingEditorIntegrationTest` 的 5 案使用 pinned JNI、production service UI 與真正 `EditText`，但仍執行於 Robolectric。見 [最後 delivery 建置記錄](../../../build/p0r-evidence/final-delivery-build.log)、[測試摘要](../../../build/p0r-evidence/final-test-summary.json) 及 [產物清冊](../../../build/p0r-evidence/new-build-artifacts.json)。最終 APK 實際雜湊為 `14082a60527ef57542d62cfb2bfaac1ed53a99080fb5af7f0de2792862fe260f`，32,276,066 bytes；aapt 確認 package `io.github.juhjuhx.kuyin`、`versionCode=20002`、`versionName=0.2.0-p0r.1`，沒有 INTERNET 權限；apksigner 驗證成功，簽署者為 Debug certificate SHA-256 `46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94`。這不是 production 簽章或實機安裝證據。

離線英文詞頻資產固定於 AOSP LatinIME commit `127336e9f29d69607eab55982324b210279ae8c5`，輸出 `word_frequency.tsv` SHA-256 為 `e44d3b87e830de6e57cc493f3cdb090404a477781299648464b2824e3b488e97`。資產附完整 Apache-2.0 license、上游 NOTICE 與可重建來源說明；本輪沒有新增 LatinIME 實作碼。既有靜態 libchewing 的 LGPL-2.1-or-later 來源／重連結義務仍應在實際散布前由發佈負責人核對，APK 建置本身不構成授權義務完成證明。凍結 P1 四 APK 與 v3 ZIP 的最終核對記錄為 unchanged，見 [凍結產物清冊](../../../build/p0r-evidence/frozen-artifacts-final.json)。

## 保留限制

候選預覽使用無個人字典的記憶體 context，所以個人學習造成的候選排序可能與 live context 不同；即時選取會重新解析並核對完整結果，遇到不一致時會失敗保護，但預覽順位仍不是個人化順位。編輯器 offset／span tracker 只觀察通知與本機 write ACK，不讀取內容，也無法證明所有 OEM 對非同步 selection callback 的排列方式。需在實機完成注音、候選觸控、標點、Enter、游標移動、密碼欄位與英文輸入矩陣後，才能改變 `NOT_TESTED`／`FAIL_BY_USER_REPORT` 裁定。
