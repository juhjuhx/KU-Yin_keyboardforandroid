# R2.3.1 Health Probe Correction

規格：2026-10-18 使用者補充 R3，僅修 R2.3 F1/P2；產品／JNI／App visibility assertions 不變。不執行 H1/H2、不合併、不發布。

## 來源與界線

- Base：`9622586acaa7e0b1335acee6b35d3b857bde7c20`，runtime baseline `5ef8a979850d1588d6be68aca103e12cb51f4a6d`。
- 舊 CI：39047679642，initial WMS frozen=true/focus=null，final frozen=false/Launcher focus；真 run 先被其他條件擋住，沒有假 PASS。
- 原始 evidence：`/Users/huang/Desktop/KU-Yin-R2.3-Verification-2026-10-17-233234/33-CI-artifacts/r22-binding-isolation/r22-legacy/h0/`。
- Tool Master：實際 canonical `~/.agents/skills/using-superpowers/SKILL.md`；不是 MCP plugin。路由／skill hashes 在外部新 session `00-baseline-and-route.json`、`01-skill-routing.json`。

## 有界 readiness policy

H0 不等待重試：一次 initial WMS pair，診斷後一次 final pair；每個命令原 timeout 保留。所有觀測必須有且僅有一個明確 `mDisplayFrozen=false`，並滿足原 inventory/focus/boot/service 守護。true／缺失／unknown／重複矛盾都不放行；final 正常不能覆蓋 initial failure。若第一讀失敗，本次 session 永不升格 HEALTHY。

每個健康判定 ADB 命令保存 UTC 開始時間、monotonic 起訖、stdout/stderr/exit/timeout；derived pair 明列原始命令與非 atomic。HEALTHY 只表示這兩個有界觀測相容，不能保證觀測間／未來持續健康。沒有 fixed sleep、poll-until-green、重新開 emulator 或 dismiss dialog。

時間範圍不含既有 `environment.json` host helper（uname/lscpu/free/emulator version/accel-check/ps）：它們仍為原有 command/exit/stdout/stderr 或 error，不參與 WMS unfrozen oracle。host helper timeout 未完整保存 partial stdout 與時間屬既有限制，不宣稱全流程所有 subprocess 均有時序證明；本輪不擴成環境採證框架重構。

## TODO／gates

1. [x] 真 raw fixtures + hash/provenance；frozen counterexample assertion RED 已記錄於外部 `02-frozen-red.log`。
2. [x] 最小 frozen guard、時間與 readiness evidence；focused36、修正後全Python192、10contracts PASS。review另發現空值重複欄位，`10-review-empty-field-red.log`兩個subcase RED後，以欄位總數=1修正。
3. [x] Fresh-context source reviewer 重跑 F1 與負面 fixtures；無未解重大 finding 才提交。
4. [x] 一次 health-only CI39080190653 exactd26fb1b、H0 HEALTHY／exit0：同 API33/default/x86_64/pixel7、現有 KVM/setup，不 build/install APK，不啟動 App/Host/binding。保留 exact SHA、raw/hash、review、最終三態；H1/H2 NOT_EXECUTED。

共享介面：task1 raw windows → task2 `complete_window` → task3/4 actual collector。原 H1 使用同 parser，因此補 fixture 明確 false，不改真 editor assertions。使用已註冊 workflow ID `384024174` 的新 health scope；main 沒有該檔，既有 branch run 已建立註冊。舊 run 的 event=workflow_dispatch、source=5ef8a97；API確認workflow active。官方規則與路由獨立審查另列報告。

本輪僅 push 新 branch 並手動 dispatch 一次 `host_scope=health`，**不建立 PR**：既有 PR workflow／Build 會觸發 App/Host runtime，超出本次 health-only 邊界。未來 PR 需另訂執行 gate，不能將 manual health scope 安全視為整體 PR workflow 批准。

真 WMS 原檔有一個 trailing space；fixture 子目錄 `.gitattributes` 只豁免該類原始 stdout 的 blank-at-eol 檢查，保留 provenance SHA，程式碼與其他檔仍正常檢查。沒有刪改 raw evidence。

## Review focus

Spec：true/unknown/missing/conflicting 絕無 HEALTHY、first failure durable、時間可追溯、沒有 atomic 宣稱、health-only 沒有 install/test。Quality：raw/derived metadata不覆寫、bounded/no retry、privacy/Android source preservation。WMS 讀取期間狀態可能變動、一次健康不能證明 binding，這些仍是限制。

## Rollback

不動舊工區／main／Release。放棄新 branch 或正常 revert 本切片；歷史 evidence 不改寫，CI 失敗不重試洗綠。

5. [ ] 最後actual CI raw獨立review：reviewer遇額度限制，REVIEW_INCOMPLETE；作者重建已完成，不替代批准。H1/H2仍NOT_EXECUTED。
