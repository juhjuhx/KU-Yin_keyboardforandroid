# CURRENT_STATE（目前事實）

## 2026-10-12：GitHub 整合與實驗性 Alpha 發布準備

使用者已授權 push／正常合併／發布。S1 分支 `65ec00b…` 已推送，PR #8；本輪只改 CI／發布文件，不改 `app/` 或候選引擎。短 TODO 與當前發布裁定以 `release/AIE_ALPHA_PROGRESS.md` 為準，續作入口是 `engineering/CODEX_CONTEXT_ANCHOR.md`。以下「沒有 push／merge」是原驗證時間點紀錄。Production release 仍 NOT_READY，完整實機仍 OPEN。

## 2026-10-11：AIE S0/S1 本地實作

- 工作分支 `feat/aie-s1-candidate-browser`，S0 commit `ea5f1853b3d4aba9f6c0deb1fbcd0e092b1cdac8`；Option1 已批准。最終 source/文件 SHA、APK 與審查以 `review/aie-s1-candidate-browser/06-handoff.md` 為準。
- S0 停用不完整 reset：明示 unsupported、移除 fallback-only 清除及假成功通知，零資料刪除。完整 native/fallback erase 仍 OPEN。
- S1 單列橫捲＋可展開網格／前後頁；preedit 保留、收合回原 keyboard，唯一 session owner；snapshot/provider/page/identity 校驗拒絕 stale tap。沿用 P0 scratch preview、原 orderedCommits/InputConnection ACK；未更動 ranking/native pins/learning/touch/symbols。
- fresh App54類265測、Host3類37測（實際 rerun）、Python104測、contracts10/10，零 failure/error/skip；真 pinned host JNI 分頁4案、真 native/service/EditText6案含第二頁選字一次→繼續輸入。不是裝置驗收。
- native bootstrap/probe、Debug與 app/host instrumentation build 成功；Release package 仍受既有缺 keystore 阻擋。新 Debug `20003 / 0.2.0-aie.1`；沒有簽 Release／push／merge。
- ADB inventory 無裝置：RUNTIME=NOT_TESTED，PHYSICAL_ACCEPTANCE=OPEN，RELEASE=NOT_READY，M4=NOT_VALIDATED。完整 native erase、English 既存跨prefix pointer、personal preview/ranking、TalkBack/OEM 等限制另記。
- 下列為歷史時間點，不與當前裁定混用；原 AIE review 的「UX待批准、reset未修」已由上述 S0/S1 更新。

## AIE S0 安全修正（2026-10-11）

S0 reset defect 採明確 unsupported/no-deletion 防護：停用誤導入口、移除fallback-only確認與成功訊息；native/fallback/設定全部保留。完整 erase 能力仍 OPEN。S0本地232測/50類0失敗與contracts10/10；真JNI僅證現有核心typing，非fullerase。S1 Option1已由使用者批准，S1將從通過S0的明確HEAD隔離繼續。

## 歷史狀態記錄

## 2026-10-11：AIE V1 審查／使用者 P0-R 復驗

- 使用者已在Android手機測P0-R，回報基本中文正常：`BASIC_TYPING=WORKS_ON_USER_DEVICE`。這更新先前「待使用者復驗」狀態，但沒有完整30-case／已裝base.apk byte核對。
- AIE審查工區 `docs/aie-v1-engineering-review`，base `3072affee95fa7fa8b0016ad85aac322272e4dea`，product `9f5db676a17b5770a738b5c20da937f9211e5a34`；本輪沒有產品修改或新APK。
- 已確認候選UI未接native paging/expand、preedit佔寬、preview不讀personal字典；既有「重設學習」只清fallback是HIGH，未修復、未刪真userdict。
- 深讀與issueledger：`research/AIE_V1_DEEP_UNDERSTANDING.md`、`research/AIE_V1_ISSUE_LEDGER.md`；roadmap：`roadmap/AIE_V1_ROADMAP.md`。
- UX待批准，`FIRST_APPROVED_SLICE=NONE`；architecture/ranking/privacy/touch/symbol皆提案，現有Compose/donation/community/credits/Dachen與libchewing保持。
- 三read-only獨立review回傳部分findings後usage-limit中斷，沒有finalapprove。OCR真health仍401；不聲稱完整獨立簽核或OCR PASS。
- full physical acceptance仍OPEN，production release NOT_READY，M4 NOT_VALIDATED；沒有push/merge/publish。下列P0與P1數據為歷史時間點證據，不能取代本輪使用者回報或新的device matrix。


## 2026-10-10：P0-R 優先於 P1-H／M4

- 使用者回報正常中文輸入失敗，`PRODUCT_TYPING_ACCEPTANCE=FAIL_BY_USER_REPORT`；不得用歷史測試解除實機驗收。
- 工作分支 `fix/p0-core-typing-recovery`，基於 `85711bc8c749d488923954e8f4c173a493bf27eb`；繼承產品來源 `75b93a3b68867fd6ec4c6adadd3e279d71e98104`。舊工區與凍結 v1/v2/v3 未修改。
- 已找到 native live `cand_open` 清除未完成尾音、首次未完成音節隱藏候選、candidate tap只替換preedit未commit、重讀native commit、英文沒有wordbuffer等根因。
- P0-R 使用同一 pin 的 libchewing FuzzyPartialPrefix；候選預覽在不學習的 disposable context，明確選字才操作live並核對native identity/完整buffer。UI按同snapshot index選取，不以label indexOf。
- service以InputConnection投影組字、檢查commit acknowledgment、追蹤自身selection；寫入未確認時保存待核對文字並停止後續輸入，不盲目重送。敏感文字在錯誤區遮蔽。
- EnglishWordSession＋AOSP固定來源10k離線詞頻提供prefix completion；沒有network suggestion／autocorrect／0.9B模型。敏感、forceASCII與非文字surface直接輸入，不做詞典查詢或學習。
- 新Debug版本 `20002 / 0.2.0-p0r.1`；來源／APK hashes與最終實測以 `review/p0-core-typing/11-handoff.md` 為準。
- 本輪 fresh App49類229測（真JNI editor五案0skip）、Host37測、Python104測、contracts10/10及Debug/build gates通過；獨立review以09為準。
- 真pin host JNI與Robolectric真EditText分層驗證；fresh ADB無裝置，`RUNTIME=NOT_TESTED`、`PHYSICAL_ACCEPTANCE=OPEN`、`PRODUCTION_RELEASE=NOT_READY`、`M4=NOT_VALIDATED`。沒有push或Release簽章。
- 新CoreTypingAcceptanceTest六案待實機；凍結P1-G runner僅適用原凍結APK，不能拿舊hash/舊B02/B03候選契約驗新P0 APK。

## 下列為 P1 歷史基線證據（不代表目前 P0-R 已獲產品驗收）

- Release: `v0.2.0-alpha.1` = old commit `278bcc2` (pre-C3.5; NOT this branch).
- main: `73e810f` = old Android main + unrelated `taui-workspace/` web files.
- C4 branch: `c4-compose-libchewing-switch` (D3a–D3h landed, pushed; see ledger).
- Production decoder: `ChewingEngineSession` (native, D3d cutover).
  Native owner: service-owned `NativeChewingEngineOwner` (init-once/close-once).
  Fallback: `ZhuyinDictionarySession`, startup-only via `NativeStartupGate`.
  Learning: `NativeLearningPolicy` maps EditorPolicy to native (single source).
- Tests: local P1-A/B/C/D verification has 40 JVM classes / 190 tests, all green;
  P1-B includes rendered Dachen coverage at 320dp/360dp. Existing Android instrumentation
  has 4 classes / 5 test methods (native smoke, IME recreate, CASE-4, continuous-sentence
  structural, package identity). Compilation/package success does not prove device execution.
  Contracts: 10/10 exit 0 (incl. CI-trigger + NDK pins).
- Build: local P1-D assembleDebug green (~29MB). Historical C4 blocking CI was reported
  green; the local P1 series / Alpha preparation has not been pushed and has no fresh CI run.
- Runtime: historical C4 emulator smoke included native CASE-4/continuous-sentence
  (experimental job, continue-on-error); it does not validate local P1-A/B/C/D.
  Local P1-series runtime: NOT_TESTED (no current Android target). QR donation art: decorative
  until scanned.
- Open gates: exact P1-A/B/C/D physical-device acceptance, controlled editor matrix
  coverage, production signing, and device QA incl. wider D3h corpus.
- P1-B local slice: Compose Dachen rows derive from `KeyboardLayout.Dachen`;
  missing ㄕ/ㄖ and lower-row hint drift fixed; 11/10/10/10 phonetic/tone keys,
  backspace after row 4. Device runtime for this slice: NOT_TESTED (no device).
- P1-C local slice: editors without composition use temporary English mode and
  disable personalized learning; text return restores the user's preferred mode.
  Verified via policy + actual onStartInput + Compose tests; runtime NOT_TESTED.
- P1-D local slice: one immutable `EditorEnterPolicy` supplies inline Enter label,
  icon presentation and dispatch. `NO_ENTER_ACTION` yields newline; custom labels
  dispatch their custom IDs; PREVIOUS uses 上一步. Consumed composition Enter never
  also dispatches an editor action/newline. Debug and instrumentation compilation pass;
  Release packaging remains blocked by the existing missing keystore. Runtime NOT_TESTED.
  Review/evidence: `docs/review/2026-10-04-p1d/06-handoff.md`; local commits only, no push.
- Known debt: main contains TAUI files (recorded, revert separately, no rewrite);
  remaining M4 work NOT_VALIDATED; PR #4 = donor, PR #6 = docs-only.

- P1 Alpha preparation (2026-10-07): isolated `test/p1-alpha-acceptance` from P1-D docs
  `94e9191`; byte-identical P1-D Debug APK plus existing instrumentation APK prepared.
  Fresh ADB inventory has no target; runtime stays NOT_TESTED. Full P1 editor/action
  matrix has 30 unexecuted cases and requires a controlled debug host/action recorder.
  Review/runbook: `docs/review/2026-10-07-p1-alpha/05-交接.md`. No product source change,
  no release signing, no push, no M4 work.

- P1-E 驗收工具（2026-10-07）：隔離分支 `test/p1-alpha-acceptance-fixture`，
  基於精確 Alpha 準備 commit `5c6bcad34c4048e40ce4e871e70e176c10195bcf`。
  獨立 test-only `:acceptance-host` Views app 記錄最終受控 EditorInfo、editor
  action ID／次數及實際文字／換行差量；UIAutomator 點擊可見 KU-Yin 軟鍵。
  產品來源與凍結 v1 artifacts 保留；建置證據與限制見
  `review/2026-10-07-p1e/07-handoff.md`。裝置案例仍 NOT_TESTED，native learning
  observer 與安全 startup-fault 裝置工具仍 OPEN；未簽 Release、push 或開始 M4。
