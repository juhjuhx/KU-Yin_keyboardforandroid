# AIE S0/S1 Implementation Plan

> 執行：主代理串行修改與建置；獨立代理唯讀審查。使用 executing-plans / TDD，不重新詢問已批准的 UX。

**Goal:** 關閉誤報學習重設成功的 HIGH，接通安全候選瀏覽。
**Architecture:** session 唯一 owner；adapter scratch 查詢；UI immutable snapshot 與 callbacks；原 editor ACK 單一路徑。
**Tech Stack:** Kotlin/Compose/JUnit4/Robolectric/同 pin libchewing JNI。

## S0
- [x] 讀取設定 reset / fallback prefs / native owner 與寫入點，記錄 unsupported 安全分支。
- [ ] 在 LearningResetSafetyUiTest 對正式 KuYinSetupScreen 寫 RED：assertIsNotEnabled；無 confirm/success；人工 fixture 頻次/settings/files 保留。
- [ ] ./gradlew :app:testDebugUnitTest --tests com.example.LearningResetSafetyUiTest，保存 XML/exit（必須行為 assertion RED）。
- [ ] MainActivity 刪除 reset dialog/state/dict reference，disabled OutlinedButton＋明確說明（不碰 stores）。
- [ ] focused/全量 P0/native GREEN＋独立安全 review，local commit。

## S1（S0 通過之後）
- [ ] 在驗證過的 S0 HEAD 建立 feat/aie-s1-candidate-browser。
- [ ] Native/session pagination/snapshot/lifecycle RED；純 snapshot identities provider+generation+page，拒絕舊頁/舊generation。
- [ ] 現有 scratch 分頁提供 metadata，不呼叫 live 左右鍵讀候選、不重排。
- [ ] 正式 UI RED：expand/collapse、前後頁、同 preedit、不同 candidates、scroll 無 selection、selection exactly once。
- [ ] CandidatePanel 單列＋bounded grid 取代原主鍵區，preedit 可見、收合恢复、48dp targets，session route only。
- [ ] full app/host/native/Compose/contracts/bootstrap/Debug/androidTest compile、独立 final review。
- [ ] version 20003 / 0.2.0-aie.1，local source commit 後生成可追蹤 APK。無裝置則手冊 NOT_TESTED；保留舊 APK。
- [ ] canonical docs + local docs commit，clean，STOP；S2 未批准。
