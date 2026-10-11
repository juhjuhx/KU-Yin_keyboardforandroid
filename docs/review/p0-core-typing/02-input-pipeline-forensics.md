# 輸入管線與第一個失真邊界

基線產品 75b93a3b68867fd6ec4c6adadd3e279d71e98104；P0-R 工區基線 85711bc8c749d488923954e8f4c173a493bf27eb，繼承產品檔 diff 為空。

| 邊界 | 輸入／輸出 | owner／發現 |
|---|---|---|
| Compose key | KeyDef.main → TapZhuyinKey(label) | UI；canonical Dachen來源 |
| Session | label → KeyboardLayout.Dachen code | ChewingEngineSession；不重新正規化音節 |
| Resolver | ready engine → ChewingEngineSession | native成功不使用fallback詞典 |
| Android adapter | key → JNI → pinned libchewing | 原生 owns decode；snapshot會進 cand_open |
| Native candidate refresh | 已轉中文字+pending音節 → start_selecting | 會清 pending syl；第二音節首次失真在這裡 |
| First unfinished syllable | buffer_check==0 → candidates=[] | 原生候選顯示被 adapter guard 隱藏，fallback query不補此路徑 |
| Candidate tap | selectCandidateUpdate → replacement preedit | 原生替換不是 editor commit，session 未再complete |
| UI候選 | label → indexOf(label) | 重複文字選錯identity |
| Editor | orderedCommits → currentInputConnection.commitText | boolean未檢查；沒有setComposingText投影 |
| Selection update | caret moved+preedit → reset | 未分離自身 composing/write callback與外部游標移動 |
| English | onEnglishKey → character commit | 沒有wordbuffer/prefix provider |

真 pin CAPI/JNI 逐鍵證據見 [native spike](02-native-spike.md)，不是 fake decoder 測試。舊靜態editor-sync合約只讀 donor View service，不證明 Manifest-registered Compose service 正常。
