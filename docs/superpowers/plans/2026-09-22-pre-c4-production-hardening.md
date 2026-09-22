# C3.5 — Pre-C4 Production Hardening（計劃）

- Branch: `fix/pre-c4-production-hardening`（基於 `origin/main@278bcc2`，獨立 worktree）
- 目標：在 C4 切換前修掉 Compose production path 的 editor-policy、lifecycle、語義與假綠燈。
  完成前不做 C4 switch；做完不自動開始 C4。
- 生產路徑（唯一）：`KuYinInputMethodService → KuYinEngine → ZhuyinDictionary → Compose UI → InputConnection`
- Donor（唯讀參考，不合併 UI）：`ChewingInputMethodService`（`onStartInput` 建會話／`onUpdateSelection` 重置／effect 執行三段式）、`ImeSessionController.shouldResetComposition`、`EditorPolicy.from`

## Task 1 — Production EditorPolicy 接線
- 接縫：`KuYinEngine.applyPolicy(policy: EditorPolicy)`（新增，預設 permissive 前向相容）＋ service `onStartInputView` 計算政策並傳入＋切 ASCII surface。
- RED（`ProductionEditorPolicyTest`，Robolectric＋真 `KuYinEngine`／`ZhuyinDictionary`）：
  normal editor allows learning；no-learning 下選字不動 `freq_*`；text／visible／web／numeric password 禁學習；force ASCII 回不去注音組字。
- 驗收：7 測全綠；`recordWordSelection` 只在 `allowPersonalizedLearning` 時發生；敏感欄 UI 走 ASCII。
- 風險：`switchMode` 會先提交組字——policy 拒絕時必須先擋，順序寫進測試。

## Task 2 — Composition lifecycle／selection reconciliation
- 接縫：service `onStartInput`＋`onUpdateSelection`，重用 `ImeSessionController.shouldResetComposition` 語義（donor L171-200 為範本，Compose 版只清 engine＋candidates，不碰 View）。
- RED：cursor moved／selection changed during composition → reset；無組字 no-op；普通 commit 回調不誤觸二次 reset。
- 陷阱：自家 commit 後的 selection 回調不得觸發 reset（以 composing 是否為空＋selection 是否對應為判據，先寫測鎖住）。

## Task 3 — Punctuation stale composition
- 接縫：兩條標點路徑（CandidateBar quick／Zhuyin `onPunctuation`）統一經 engine 語義：有組字→先按既有 policy 完成或清理→commit 標點→歸零；無組字→直送。
- RED：`ㄋㄧˇ`＋候選存在時按 `，`→ composing/candidates 歸零且只提交一次標點。

## Task 4 — Backspace editor 語義
- 接縫：小型 `EditorDeletion` helper（Compose UI 不碰 InputConnection）：無組字時 BMP 用 `deleteSurroundingTextInCodePoints(1,0)`；supplementary 不留半個 surrogate；有 selection 先刪 selection；無 IC 安全 no-op。
- RED：BMP／emoji／selection／null-IC 四測。

## Task 5 — Dictionary ranking edge cases
- 接縫：`ZhuyinDictionary.query()` 內。tone-aware continuation：已含 tone 的 query 只接受 tone-compatible 延續；tone-only 回空；toneless partial 照舊。
- RED：tone-incompatible 排除、tone-only 空集合（現行回整表）、EXACT 不被高頻超車（已有測鎖住，不得回退）。
- 不重寫字典、不搭 trie。

## Task 6 — Runtime smoke identity
- 接縫：`ImeRuntimeSmokeTest` 的 `APP_PACKAGE`／service 常數改由 `targetContext.packageName`＋manifest 解析取得；保留實驗性質。
- 驗收：跑到 registered／enabled／selected／visible／recreate 後仍指向 production service；headless 卡住時明確區分 identity failure vs window limitation。

## Task 7 — Contract 去假綠燈
- 拆分：donor contracts 改名誠實化（`donor architecture`），新增 production Compose contracts（manifest service／policy 接線／lifecycle／candidate-learning／punctuation／delete 語義／session seam）。
- P3/security gate 直接讀 production service。舊 contracts 保留作 C4 reference。

## Task 8 — CI／release 誠實化
- JVM/contracts／Debug 維持；runtime-smoke 續 experimental；輸出分 JVM／APK／smoke 三欄；prerelease 只跑 production critical contracts；第三方 Action SHA pin 記 hardening backlog（本輪可順手、不擋 C4；branch protection 同）。

## Task 9 — Production integration tests
- 最小 service 邊界 harness（Robolectric service 影子或 session＋fake IC）：normal／password／no-learning／force-ASCII／composition＋cursor／composition＋punctuation／emoji backspace／selection backspace。行為描述命名，禁 grep 式斷言。

## C4 Gate（重申，完成定義）
全部 PASS 才開 `docs/superpowers/plans/2026-09-22-c4-compose-libchewing-switch.md`：sensitive-policy 接線、no-learning 零持久化、stale lifecycle 修復、punctuation 歸零、production contract 指對 service、smoke identity 修正、JVM 綠、APK 綠。Backspace 與 ranking edge 原則收完。Device retest 仍獨立保留。

## 非目標
C4 切換、刪 ZhuyinDictionarySession、刪 donor、合 PR#6、重做 UI、16dp stagger、release key/AAB/F-Droid、網路權限、telemetry、新大型依賴。
