# R2.3.1 Emulator Health Probe Correction

狀態：test-infrastructure source 修正，runtime 結果另見 final handoff；不是 IME binding／產品修復。

## 根因與修正

Base `9622586acaa7e0b1335acee6b35d3b857bde7c20` 延續 runtime source `5ef8a979850d1588d6be68aca103e12cb51f4a6d`。舊 `complete_window()` 驗 inventory、focus 與 boot footer，但沒驗 `mDisplayFrozen`。CI39047679642 實際 initial focus=null 而先被阻擋；將真 final inventory **僅 frozen boolean 改 true**，舊版卻回 HEALTHY。這是離線反例，不是該次 CI 曾假 PASS。

新 `window_unfrozen()` 要求欄位總數恰為一、值恰為 false；缺失、true、未知、空值及重複（包括空白）都不通過。原 inventory/focus/visibility assertions 保留。initial 與 final 都走此 guard；`finish()` 保留首次 assessment，final 恢復不能洗掉理由。

## 有界觀測

- 初始一組 `window windows`／`window displays`，原診斷後 final 一組；沒有 H0 polling、sleep、重啟、dismiss 或「retry until green」。
- 每個 ADB capture 有 UTC 開始、monotonic 起訖、exit/timeout、獨立 stdout/stderr 與 command JSON。derived observation 標記 `atomic_snapshot=false` 並以兩命令起訖為時間界。
- readiness 區塊明列 initial/final references 與初次 verdict。兩次獨立 WMS reads 不是 atomic snapshot；兩組觀測間持續健康沒有證明。
- 既有 host helper inventory 時序與 timeout partial-output 限制仍存在；不能宣稱每個 host subprocess 都有完整時間證據。

## 測試／證據對照

| 要求 | 直接證據 |
| --- | --- |
| F1 反例 | `CapturedWmsReadinessTest`：真 raw + 單一 boolean 變化，舊版 assertion RED；修正後非 HEALTHY |
| 原始 frozen + 有效 focus | 初次真 raw 只補同 inventory 的 focus；不放行 |
| missing/unknown/conflicting/duplicate/empty | raw-derived subcases；獨立 review 找到空欄位漏失，再 RED→GREEN 修正 |
| 首次失敗保留 | 真 `Capture.finish()` + fake ADB final recovery；status/reasons/first_assessment 保留 |
| final 轉 frozen | 初始正常仍不得通過 |
| 原始檔身份 | 四 stdout byte-for-byte fixtures、`provenance.json` SHA/size，來源 API33 disposable CI 無私人輸入 |
| 時間與原始 exit | `EmulatorHealthCollectorTest`；兩命令各自 metadata，不以 derived 值覆寫 raw |
| health-only 邊界 | `test_r231_health_workflow.py` source contract；實際 CI job/command artifacts 才是 runtime 證據 |

外部 session：`/Users/huang/Desktop/KU-Yin-R2.3.1-Health-Correction-2026-10-18-073541/`。含 02/04/05/10 RED、focused GREEN、全量 Python、contracts、獨立原始報告。import/KeyError 等早期測試架構失誤不作 F1 RED 證據。

## CI 與來源隔離

既有 workflow ID384024174 新增 `host_scope=health`，獨立 job：source guard、Python/contracts、同 API33/default/x86_64/pixel7/KVM 設定、H0、always artifact。**不 build/install APK，不啟動 H1/H2/App/Host**。唯一手動 run，三態 exit 原樣傳遞，拒絕仍保存證據。

只 push 新隔離 branch，不開 PR：目前既有 PR trigger 會執行其他 Runtime workflow。未來 PR 需另行 gate，不將此 scope 的批准延伸到一般 PR。原 R2.3／R2.2 工區、Android/JNI/native pins、release、signing 與使用者資料不變。

## 官方參考與適用界線

[AOSP Android 13 WindowManagerService](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/services/core/java/com/android/server/wm/WindowManagerService.java) 提供此版本 WMS 欄位來源；不是跨 API/OEM 通用 parser 保證。
[emulator-runner action inputs](https://github.com/ReactiveCircus/android-emulator-runner/blob/main/action.yml) 定義 emulator-port 等設定；existing action 啟動時的 keyevent/動畫設定不屬 H0 唯讀命令，也不是軟鍵輸入證據。

Rollback：保留舊分支／工區與 evidence；正常 revert 新 source commit 即可。H0 有可信 PASS 前，H1/H2 始終 NOT_EXECUTED；即使 H0 PASS，也要另明確階段才恢復 binding。正式 release 仍 NOT_READY。
