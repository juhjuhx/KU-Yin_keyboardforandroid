# INTEGRATION — 全案整合狀態（接管 × 審查 × 修復，一文交棒）

> 日期：2026-09-09。用途：任何新會話讀完本檔＋`RECONCILE-agnes.md`＋plan，即可接手。
> 原則：只收錄實字節驗證過的；推測一律標註。数字口径：29 todos＝25＋F1–F4。

## 1. 進度總覽（17 完成 / 1 取消 / 3 執行中 / 8 待命）

- 完成：T1–T10、T12–T17、T25（plan 已勾，逐條有 ledger 收據）。
- 取消：T11（worker 遺失、零交付；覆蓋併入真機驗證）。
- 執行中：T18（manifest/ids 查驗）、T21（core/UI 切分分析）、T22（iOS 筆記增補）。
- 待命：T19（需真機）、T20、T23、T24、F1–F4。
- R2 修復線：R2a（入口 3 件）✓、R2b（H1–H4 引擎）✓、R2c（去 BOM＋重建）✓、
  R2d（真設置頁）✓、R2e（重建含設置頁）✓、R2f（顯示表＋▼收起鍵＋重建）✓、
  R2g（乾淨重建＋版本化 `KU-Yin_0.1.0-alpha_20260909-1447.apk`）✓、
  R2h（重複鍵 `KeyDef.code` 直通＋`...-1453.apk`）✓、R2i（容器疊層修復＋`...-1514.apk` 構建中→見 ledger）。
- 當前可裝包：`outputs/apks/KU-Yin_0.1.0-alpha_20260909-1453.apk`
  （哈希 `15747CAF…`；`app-debug.apk` 是草稿箱，只認時間戳命名）。

## 2. TUI 架構圖（as-built 實況）

```
┌─ APP 層 ────────────────────────────────┐
│ SettingsActivity → PreferenceFragment   │  ← R2d 重建（真設置頁）
│ ChewingInputMethodService               │
│  ├─ onCreateInputView → FrameLayout     │  ← R2i 修復中：候選44dp置頂＋鍵盤下排
│  │    ├─ CandidateView (44dp TOP)       │     （修復前：全屏透明層吞觸控）
│  │    ├─ KeyboardView (自繪,下排)       │
│  │    └─ SymbolPicker (GONE)            │
│  ├─ handleKey → KeyDef.code 直通 (R2h)  │  ← 不再查易錯 mapOf
│  └─ updateCandidates 無條件刷新 (R2b)   │
│ KeyboardLayout 真大千顯示表 (R2f) ＋ ▼ 收起鍵
└───────────┬─────────────────────────────┘
            ▼
┌─ ENGINE 層 ─────────────────────────────┐
│ AndroidChewingEngine → JNI (REAL)       │
│  ├─ bitmask 謂詞 (R2b, 替代 != 比對)    │
│  └─ cand_open 按需開啟 (R2b)            │
│ OpenCCConverter → STUB（直通）          │  ← 待完整 C API（後期）
│ IMEConfig (SharedPreferences 8鍵)       │  ← R2d 设置页同键直写
│ KeyMapping → 標籤提示專用（去重後）      │
└───────────┬─────────────────────────────┘
            ▼
┌─ NATIVE 層 ─────────────────────────────┐
│ chewing_jni.cpp (319行, REAL) →         │
│ prebuilt libchewing.a (Rust, a6a8fa4)   │  ← 非 submodule pin
│ OpenCC 1.2.0 prebuilt                   │
└─────────────────────────────────────────┘
  构建：AGP 7.4.2 + JDK17 + NDK r27d + CMake 3.22.1（本機 D:\666）
  權限：VIBRATE + PLUGIN（零 INTERNET）；包名 com.example.* 待定稿（需用戶拍板）
```

## 3. 路線圖（waves＋狀態）

```
Wave0 骨架 ████████ T1-T4 ✓ (CHECKS/wave0)
Wave1 解碼 ████████ T5-T9 ✓ (CHECKS/wave1)
Wave0.5 發版門面 ██ T25 ✓ (+T24封版待)
Wave2 前端 ░░░░░░░░ T10✓ T12-T16✓ T11✗(取消) T15✓ T17✓ | T18…ing
Wave3 人體工學 ░░░░ T19(需真機) T20(待T16-T19)
Wave4 切分發版 ░░░░ T21…ing T22…ing T23 T24
終驗 F1-F4 ░░░░░░ 待 Wave3-4
R2 修復線 ██████░ R2a-i ✓ (R2j? 按需) → 真機三驗（用戶側：圖標/勾選/彈出打字/收回）
```

## 4. 技能矩陣（每階段 7–15，僅列本機實裝；缺裝標等價）

**R1 審查（已執行，9）：** `code-review-excellence`（Kotlin 主鏡）＋
`official-code-review`（HIGH-SIGNAL 準則；其 gh/haiku/opus/MCP 機制本機無，用法已轉為人工三段：獨立審查→逐條驗證→過濾未驗證）＋
`pr-review-toolkit`（silent-failure 視角）＋ `brooks-audit`＋`brooks-review` ＋
`understand-domain` ＋ `codebase-inspection` ＋ `systematic-debugging` ＋ `diagnose`。
（`addy-24` 當時缺裝→ROADMAP 映射表；現已同步，有需要按表直調。）

**R2 修正（執行中，10）：** `systematic-debugging`（主鏡，先根因）＋ `diagnose` ＋
`code-review-excellence` ＋ `pr-review-toolkit` ＋ `brooks-review` ＋
`test-driven-development`（OTP/旋轉/密碼框先紅後綠；本機無設備時記 NOT-RUN）＋
`complexity-cuts` ＋ `requesting-code-review` ＋ `verification-before-completion`
（真機證據才算完）＋ `caveman`（長程 token 節流）。
UI 階段待命：`ui-ux-pro-max`、`fixing-motion-performance`、`stitch-design-taste`；
收尾：`release-skills`、`using-git-worktrees`、`finishing-a-development-branch`。
（`visual-atelier v2`、`taiwan-*`、`fb-marketplace`、`pro-*` 大部、`OpenCodeReview`、
阿里 opencodereview 均缺裝；`addyosmani-*` 僅佔位符——見 ROADMAP §5。）

**R3 驗收（待啟，8）：** `verification-before-completion` ＋ `brooks-test` ＋
`brooks-sweep` ＋ `receiving-code-review` ＋ `release-skills` ＋
`systematic-debugging`（殘留 PARTIAL：旋轉無 onConfigurationChanged）＋
`improve-codebase-architecture`（core/UI 切分落子）＋ `deslop`（腳本瘦身，`pro-deslop` 缺裝用此代）。

## 5. Debug／審查流程（自動化約定）

1. **派單**：單 lane 單文件（`subagent_type` 單傳，禁 `category` 混傳）；背景跑，
   憑 `<system-reminder>` 收成，絕不輪詢 `background_output`。
2. **DoneClaim**：每個 worker 回傳 changed_files＋commands＋manual_qa＋cleanup＋risks；
   缺任一項打回。
3. **獨立驗收**：root 讀實字節（頭＋尾＋目錄），與聲稱逐項對照；不符→`needs-fix`
   原 session 重派；`background_output` 取不到＋文件不在＝判死（T11 判例）。
4. **證據鐵律**：構建聲稱需 exit code＋log 尾＋產物哈希三件套；同字節不同碼＝陳舊產物
   直接判假（R2e/R2f 教訓）；真機三驗（圖標/勾選/彈打收）未過不算完。
5. **併發紀律**：同文件同時一 lane；改前 fresh-read，改後讀回；錨點命中中部即停手重讀。
6. **記憶體**：ledger 只 append（錨定 consolidation 行）；讀檔去重鍵 `(event,task)`；
   todos＋plan＋實檔為權威，ledger 僅收據。

## 6. 待決策（需用戶拍板，不代判）

- 包名 `com.example.*`＋通用名定稿（改包名＝重裝）。
- libchewing 路線鎖定（現狀 prebuilt；legacy C／Rust 自編為備援）。
- OpenCC 完整 C API vs HashMap 過渡（現 stub）。
- 真機三驗結果（圖標/勾選/彈打收＋logcat）—— 全案下一刀的唯一依據。
