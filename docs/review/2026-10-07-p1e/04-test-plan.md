# P1-E 執行與驗證計畫

採 executing-plans。唯一 Gradle writer 由 root 序列建置；固定 primitive 契約後，裝置測試葉檔與執行器分工撰寫，root 整合並新鮮驗證。

## 1. 純契約 TDD

- [x] 獨立斷言 defaults、typed extras、TYPE_NULL、null／empty label、ID=0、true／false action 計數、真正文字 LF 與不可變 snapshot。
- [x] 寫可編譯、尚無行為的 scaffold，保存 assertion RED：28 測／25 failures；不把初次 source-set 編譯錯誤算 RED。
- [x] 最小實作 parser／recorder／snapshot／test-only matrix，取得 GREEN 28 測／0 失敗。
- [x] 加入 RecordingEditText／Intent adapter／Activity diagnostics 後，再跑 host unit＋Debug build。

## 2. 跨程序 editor oracle 與矩陣

- [x] super → platform snapshot → 真正 outAttrs 四欄控制 → final snapshot；真 null IC 原樣返回。
- [x] action 記錄並消費；文字／composition／deletion delegate 平台；換行從真 Editable 文字計算。
- [x] D01–D18、C01–C05、B01–B03 UIAutomator 測試整合；只使用真可見軟鍵。
- [x] 固定 serial 的裝置狀態執行器；四 APK 核對、capture／install／select／run／restore，失敗時還原且不 uninstall／clear。

## 3. 新鮮 gates

所有 Gradle 使用 JDK21、--no-build-cache --console=plain；不並行共用 build outputs。

- [x] :acceptance-host:testDebugUnitTest。
- [x] :acceptance-host:assembleDebug。
- [x] :acceptance-host:assembleDebugAndroidTest。
- [x] :app:assembleDebugAndroidTest。
- [x] :app:testDebugUnitTest，40 類／190 測／0 失敗。
- [x] :app:assembleDebug。
- [x] python3 scripts/verify.py --static，10 合約 exit0。
- [x] 固定 SHA native bootstrap，exit0；在產品 native build 前完成。
- [x] 必須的 :app:assembleRelease：既有缺 keystore，packageRelease exit1，誠實分類。
- [x] Runner 純邏輯／模擬 subprocess 測試與 script compile/help。

已通過且未改來源的 gate 不無故重跑；新變更、失敗或未解疑慮才補受影響檢查。

## 4. 審查、封裝與本機歷史

- [x] Host oracle 新鮮唯讀獨立審查：限定範圍 APPROVE。
- [x] 最終裝置矩陣／執行器／整體差異獨立審查；修正確認問題並驗受影響 gates。
- [x] apksigner／merged manifest 核對四 APK 的身分、簽章、test target、host isolation。
- [x] 新 v2 ZIP 含原凍結兩 APK、新 host／test APK、操作手冊、矩陣、artifact manifest／hash／signer、架構報告；不得覆寫 v1。
- [x] 比對所有 app 來源與 base，核對原 8 工區 HEAD／status／dirty bytes、v1 三 binary 不變。
- [x] 只建立本機 commits，保留分支／工區，git status clean；不 push。
- [x] 寫完整 07-handoff，fixture READY_FOR_DEVICE、runtime NOT_TESTED，STOP。
