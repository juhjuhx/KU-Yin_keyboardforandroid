# R3.6 Bounded Health Protocol

Base diagnostic source `9ac52b5699aad874a3daa6d6ee950dabc4ce22e9`；main `2a2181e023e0dee0ddff98681e374df10cd5fa8f`另為產品基線。只改TEST_ONLY evidence與手動workflow，不改產品/JNI/原App/Host assertions。

## 單一假說

CI39734667334 initial/final rotation端點皆0，但appBounds上緣0→136、final NavigationBar0/StatusBar orientationChanging，可能是boot後display配置更新使WMS等待redraw。相同rotation端點不證明區間從未旋轉。相關變化不等於cutout/截圖已證實觸發；guest事件時刻缺失，UNRESOLVED。

官方機制參考：[AOSP Android13 DisplayContent.reconfigureDisplayLocked](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/services/core/java/com/android/server/wm/DisplayContent.java)可在配置變動時啟動freeze；僅說明可能路徑，不等於CI根因。沒有複製其實作。

## 單次有界實驗

新增workflow `host_scope=display-health`，與舊editor-health互斥。新的disposable API33/default/x86_64/pixel7、2cores/1536M、emulator build16428233、既有flags、同一Actionrevision a421e438。不安裝APK、不選IME、不H1/H2、不Binding。

wrapper先限定唯一emulator/user0/boot；記guest date/uptime與overlaylist，只執行一次原collect_h0，再保留原status/exit/reasons/hash，補記guest epochs/events、framework logs、WMS與identity。補充觀測不能回寫首錯或以恢復代替失敗，現有oracle/timeout/argv不變。host command bounds與guest timestamps分開，WMS讀取非atomic。最多4000行/每buffer尾端log是有界資料，缺事件不能推論事件沒發生。

若frozen/unknown/missing/矛盾或identity變更，繼續INCONCLUSIVE/UNHEALTHY。若直接H0與完整補充資料皆有效，HEALTHY只涵蓋此boot有界觀測；未重現失敗不等於修復根因。環境/image properties漂移須記錄，不能與原CI作單變數因果對照。只安排後續H1，不在本health-only run偷偷執行。

## Offline evidence

實際raw4files及SHA見scripts/tests/fixtures/r36_wms/provenance.json。新增診斷缺失RED為2tests/3assertion failures；保留unknown與非因果GREEN。作者160 Python及10contracts PASS；Reviewer獨立42聚焦PASS（nativePTY權限）。sandbox原有H1 PTY fixture失敗有原始9ac/native對照，不弱化測試。

## Independent gate

`/root/r36_health_protocol_review` 審actual diff/untracked/fixtures，AST確認collect_h0、assess_h0、complete_window、window_unfrozen、result、environment不變；Spec PASS/Engineering PASS、ALLOW_ONE_HEALTH_ONLY。非blocking gaps：wrapper原UNHEALTHY/supplement timeout/boot-change/result bytes尚未逐項測全。沒有產品/Release批准。Runtime SHA／run ID由實跑source-sha與外部receipt記錄，不拿本文件日期代替。
