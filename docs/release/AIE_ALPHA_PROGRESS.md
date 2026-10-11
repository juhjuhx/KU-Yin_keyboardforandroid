# AIE Alpha 發布進度與 TODO

更新：2026-10-12。任務限於既有 S0/S1 的 GitHub 整合與實驗性預發布。

## 已完成

- [x] 核對 S1 source `39acf96…`、文件 HEAD `65ec00b…`、APK hash／version／signer。
- [x] 推送 `feat/aie-s1-candidate-browser`，建立 PR #8；原 main 的 TAUI 檔案可無衝突保留。
- [x] 讀取 CI run `38144366396`：blocking build 成功；experimental runtime 失敗，尚未算裝置驗收。
- [x] 核對 libchewing source／prebuilt／prebuilder pins；詞庫 `word.csv` 與 `tsi.csv` 檔頭明示 LGPL-2.1-or-later。
- [x] 建立短版續作入口與元件 ownership，既有 AGENTS 不變。

## 當前任務（按依賴）

1. [x] 修正 CI 跨模組測試的 IME precondition；fake 工具驗 RED/GREEN，完整 suite 不刪測試。新 CI 結果另記，不能以 fake 測試聲稱 runtime PASS。
2. [x] 停止 main push 自動產生／發布不同 Debug signer 的 APK；手動發布需明確啟用。
3. [x] 補齊 pinned 原生／詞庫來源、build patch、license、JNI relink object 與重建說明；核對包內 hash，保留原 APK。
4. [x] 更新 README、CURRENT_STATE、release readiness、實際 skill routing；完成 diff review 及所需 contracts。
5. [x] 推送 PR 更新，blocking CI已通過；正常 merge，保留遠端 main 現有內容。
6. [x] 標記來源，發布 exact APK＋source/relink/notice/manifest；用GitHub server SHA256／size核對資產，記錄最終 URL／SHA。

## 發布限制與後續

- S1 實機、TalkBack/OEM、完整 learning erase 與 English 既存跨-prefix pointer case 尚待驗證／修正；不是正式版。
- CI runtime 初次失敗：app layer 開始前已選 KU-Yin，host layer `SETUP_IME_NOT_SELECTED`。跨模組安裝／清理可能重設 default IME；需在每層前建立並檢查 precondition，不能放寬 assertions。
- 下一個產品任務仍是實機復驗；AIE-S2、M4、privacy default、release signing 要另有決策。
- OpenCodeReview 最近實際回應 401，`BLOCKED_BY_PROVIDER`；不反覆重試，也不稱 OCR APPROVE。

## 驗收標準

GitHub main 含核對過的產品；公開 prerelease 的 APK 與原 hash 完全一致；source/relink materials 可取得；已知限制清楚；未宣称 physical acceptance 或 production readiness。

## 2026-10-12 本輪本地驗證

- 3 個 CI orchestration regression：原腳本 3 FAIL（aggregate precondition lost／未拒絕實體target／原 failure未傳遞），修改後 3 PASS。
- Python `python3 -m unittest discover -s scripts/tests -v`：107 tests，exit0。
- 10 個 `scripts/check_*.py`：全部 exit0；`bash -n`、`git diff --check` 通過。
- 對產品來源39acf96的 `app/` diff 為空；沒有重建或替換 APK，沒有實體裝置操作。
- 原生 source/relink 包四 ABI 真link exit0；`.text`／`.rodata` 比對APK全相同；完整ELF hash可因metadata不同。
- 新CI與正式GitHub merge/publish仍待執行，不能把fake工具測試當成裝置PASS。

## 已公開結果

PR #8正常merge至912b5cc，v0.2.0-aie.1公開experimental prerelease。exact APK與source/relink材料皆有server hash proof；詳細交接見 `AIE_ALPHA_HANDOFF.md`。

新CI host不是PASS：32 tests，15PASS、9FAIL、8skipped。已確認fixture stale-read及旧selector/新layout不相容，部分timeout仍未證明原因；不把未測scope算通過。下一步是bounded fixture adapter、保持exactly-once斷言、重跑32-test suite、再做手機復驗。M4/S2仍未開始。
