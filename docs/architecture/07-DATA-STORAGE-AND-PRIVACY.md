# 資料儲存與隱私

狀態：NORMATIVE／IMPLEMENTED（文內 PROPOSED/OPEN 例外）。目的：說明此邊界的實際責任、資料與故障處理。來源基線 `a6a4c654845338521700121ac06586887d1ec085`；R1工具／test-adapter不改產品。

## 實際 source files / evidence

- [app/src/main/java/com/example/feedback/data/AppDatabase.kt](../../app/src/main/java/com/example/feedback/data/AppDatabase.kt)
- [app/src/main/java/com/example/feedback/data/FeedbackEntity.kt](../../app/src/main/java/com/example/feedback/data/FeedbackEntity.kt)
- [app/src/main/java/com/example/feedback/data/FeedbackDao.kt](../../app/src/main/java/com/example/feedback/data/FeedbackDao.kt)
- [app/src/main/java/com/example/feedback/ui/FeedbackViewModel.kt](../../app/src/main/java/com/example/feedback/ui/FeedbackViewModel.kt)
- [app/src/main/java/com/example/ime/settings/KeyboardSettings.kt](../../app/src/main/java/com/example/ime/settings/KeyboardSettings.kt)
- [app/src/main/java/com/example/ime/service/NativeLearningPolicy.kt](../../app/src/main/java/com/example/ime/service/NativeLearningPolicy.kt)
- [app/src/main/res/xml/data_extraction_rules.xml](../../app/src/main/res/xml/data_extraction_rules.xml)

## Owner / data flows

資料全在本機；native lexicon由libchewing；fallbackfreq由ZhuyinDictionary，IMEfallback及MainActivity內嵌試打都可寫；設定由KeyboardSettings；Room只存使用者主動feedback，不是ranking/touch/learning DB。英文TSV唯讀、不學習。

```mermaid
flowchart LR
  EditorPolicy --> NativeLearningPolicy
  NativeLearningPolicy --> Live[Live native learning]
  Live --> Userdict[noBackup userdict.dat]
  Preview[Scratch preview learning off] --> Memory[Memory-only dictionary]
  Demo[MainActivity trial and startup fallback] --> Freq[kuyin_dict_prefs]
  Settings[Settings UI] --> Prefs[kuyin_settings]
  Feedback[Explicit user feedback] --> VM[ViewModel and repository]
  VM --> Room[kuyin_app_database v1]
  Feedback -->|explicit share or issue draft| External[External application]
```

EditorPolicy password/forceASCII/nontext/NO_PERSONALIZED_LEARNING禁native learning；英文另尊重NO_SUGGESTIONS/AUTO_COMPLETE。沒有通用private mode、personal export/import或全learning erase。S0reset disabled、明示unsupported且零刪除。

Room v1/exportSchema=false、沒有migration/destructive fallback；feedback含description/email/device metadata，手動copy/share/Issue draft可能傳外部app，普通save只local insert。DAO有手動delete/clear，沒有TTL或quota；insert成功callback在Room完成後呼叫；main PR40 的 delete/clear 成功 callback 在 suspend DAO 完成後才觸發；失敗顯示錯誤，重入時回報 busy，取消仍傳遞。原先立即顯示成功是歷史缺口，公開 Alpha 尚未包含修復。nativelearning先於editor ACK，非成功commit後才learn。

## Backup / upgrade / limitations

allowBackup=false与native noBackup store不同保证；Android12+部分OEM的D2D不能仅以allowBackup=false排除，现行XML沒有有效device-transfer excludes。Room/prefs移轉狀態待另安全slice/OEM驗收，不宣稱所有資料皆不可搬移。相同簽章及較高 versionCode 是正常更新的必要條件，並不證明實際資料保存；本輪沒有 device DB inspection／migration／deletion 或保資料升級證據。PR41 的偏好限定政策仍 Draft，未當作 main 行為。

完整data schema/retention/rollback見[資料清單](../maintenance/DATA_STORAGE_INVENTORY.md)與[政策](../maintenance/DATA_RETENTION_POLICY.md)。Tests：LearningResetSafetyUiTest、EditorPolicy/NativeLearningPolicy；不是native erase或OEM D2Dproof。
