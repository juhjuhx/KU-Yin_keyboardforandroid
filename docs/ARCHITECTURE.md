# KU-Yin Architecture

NORMATIVE；更新2026-10-12 R1。唯一現行架構入口，依main實作校正。歴史D2.5/C4與futureAIEproposal不能覆蓋此入口。產品source `39acf96c6cfc058106315f3553e1c8b88438f4f7` 已在main；本次深讀基線 `a6a4c654845338521700121ac06586887d1ec085`。

```text
Android Editor / EditorInfo / InputConnection
  ↕ KuYinInputMethodService [lifecycle, policy, editor effects]
      ├─ EditorPolicy → EffectiveModePolicy / NativeLearningPolicy
      ├─ EditorEnterPolicy [one presentation + behavior]
      ├─ EditorEffectTracker [offset/span ACK notifications]
      ├─ ComposeDecoderSession [Chinese command/state owner]
      │   ├─ ChewingEngineSession → AndroidChewingEngine → JNI/libchewing
      │   │   ├─ service-owned live context [decode + personal store]
      │   │   └─ disposable scratch contexts [candidate plan projection]
      │   └─ ZhuyinDictionarySession [startup-only IME fallback]
      ├─ EnglishWordSession → EnglishLexicon [offline 10k, no learning]
      └─ Compose UI / CandidatePanel [immutable snapshots and callbacks]
```

MainActivity試打另用dictionary session，亦可寫fallbackprefs；未註冊的舊View shell並非production。所有Editor effects由service執行，native ACK与editor BooleanACK是不同層；不保證跨IPC exactly-once delivery。候選page totals是projection plans，不是全部unique字庫數。S0停用不完整reset，沒有完整erase。新ranking、TouchResolver、SymbolRepository、Rime及M4皆未實作。

R3未發布分支b982062僅產品捐贈卡改標準離線QR、版本20004及test-only CI保全；上述typing/native/state ownership與Dachen geometry不變。ZXing僅JVM test，OpenCV僅離線asset tool，四APK不包含這兩者runtime。H0/H1健康probe及acceptance-host皆test infrastructure，不能畫成產品功能；實際runtime gate見[驗證矩陣](release/R3_RELEASE_VALIDATION_MATRIX.md)。

## 可導航的規範章節

- [系統總覽](architecture/01-SYSTEM-OVERVIEW.md)
- [Android IME 生命週期](architecture/02-ANDROID-IME-LIFECYCLE.md)
- [輸入與解碼管線](architecture/03-INPUT-DECODER-PIPELINE.md)
- [候選瀏覽與分頁](architecture/04-CANDIDATE-BROWSING.md)
- [Editor effects 與確認](architecture/05-EDITOR-EFFECTS-AND-ACK.md)
- [Native/JNI/libchewing](architecture/06-NATIVE-JNI-LIBCHEWING.md)
- [資料儲存與隱私](architecture/07-DATA-STORAGE-AND-PRIVACY.md)
- [UI 元件責任](architecture/08-UI-COMPONENT-OWNERSHIP.md)
- [建置、測試、CI、發布](architecture/09-BUILD-TEST-CI-RELEASE.md)
- [模組依賴及擴充](architecture/10-MODULE-DEPENDENCY-MAP.md)

## 證據與限制

Source-grounded owner/介面/資料/error/storage/tests逐章列出；fresh檢查在[Validation matrix](maintenance/VALIDATION_MATRIX.md)，資料政策在[Storage inventory](maintenance/DATA_STORAGE_INVENTORY.md)，future只看[Roadmap](roadmap/AIE_V1_ROADMAP.md)。本轮文檔與診斷收束不能解除實機、OEM、native erase、production signing/release gates。
