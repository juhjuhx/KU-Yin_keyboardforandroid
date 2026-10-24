# KU-Yin Architecture

NORMATIVE；R3.8校正（產品架構未改）。唯一現行架構入口，依main實作校正。歴史D2.5/C4與futureAIEproposal不能覆蓋此入口。產品source `39acf96c6cfc058106315f3553e1c8b88438f4f7` 已在main；本次深讀基線 `a6a4c654845338521700121ac06586887d1ec085`。

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

## 工程與發行控制面（R3.8）

acceptance-host只作真Views editor／immutable recorder和test-only observer；canonical Driver在JVM中同源staging，非第二個IME owner。離線H0規格僅解析合成/保存觀察，不是Android初始化完成API。GitHub CI的unit/build、手動Runtime及公開Release互相分層；TEST_ONLY artifact不通過手機或簽章Gate。

現行技術責任：Kotlin/Compose呈現與session；C++ JNI管理native handle；libchewing提供現有C ABI decoder；Gradle/NDK/CMake固定建置鏈。上游Rust實作經prebuilt靜態連結，不代表KU-Yin另交付通用Rust runtime、ASR或新framework。Rime/Fcitx5 framework/Whisper屬研究/未來，不是production dependencies。

已知D2D、Room刪除ACK與完整erase限制見FINAL_SECURITY_AUDIT及資料庫章；本輪不改schema、storage或輸入核心。
