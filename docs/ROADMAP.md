# Roadmap（設計方向與路線圖）

> 狀態：Wave 2 骨架完成（T1 done，T2 running，T3 queued）。本檔是 handoff 入口：新會話先讀本檔 + `ARCHITECTURE.md` + plan，即可接手。

## 1. 整體設計方向（一句話）

Fork 官方 `fcitx5-android`（LGPL-2.1），解碼沿用 Linux 同款 `libchewing`、
簡繁走內建 OpenCC 一鍵切換，前端走低延遲 Kotlin `View`/`Canvas` 自繪
（單容器 + 全量 `onDraw`，不做逐鍵 View、不拿 Compose 當輸入視窗），
預設大千 4x10，鍵尺寸對標並超過 Gboard，`engines/core` 與 UI 解耦預留 iOS。

## 2. TUI 架構圖

```
┌────────────────────────── APP 層 (Android UI) ──────────────────────────┐
│  InputMethodService                                                      │
│   ├─ KeyboardView (自繪 ViewGroup, 硬體加速, onDraw 全鍵 loop)            │
│   │    ├─ KeyData[] 命中走查 (getNearestKeys / ProximityInfo 思路)       │
│   │    └─ Popup 預覽 + 長按符號 (350ms) + 按壓高亮                        │
│   ├─ CandidateView (分頁/展開/浮窗/物理鍵盤)  ◄── 高度 44dp               │
│   ├─ Toolbar (簡繁一鍵 s2tw/tw2s, 語言切換, 剪貼簿入口)                   │
│   └─ Theme (M3 動態色 / 深色-OLED / 自訂背景 / key border 選項)           │
└──────────────────────────────┬──────────────────────────────────────────┘
                               │  唯一介面：engines/core API（UI 零直調 JNI）
┌──────────────────────────────▼──────────────────────────────────────────┐
│  CORE 層 engines/core (Android 與未來 iOS 共用)                           │
│   ├─ libchewing 0.13.x（Rust, Codeberg 主倉）                             │
│   │    ├─ DaChen(預設) / Hsu / Eten26（chewing_handle_KeyboardEvent）     │
│   │    ├─ sort_candidates_by_frequency / auto_snapshot_selections        │
│   │    └─ user dict + deleted-phrases exclusion（chewing-deleted.dat）   │
│   ├─ OpenCC（s2tw/tw2s 預設，s2t/t2s fallback）                           │
│   └─ 配置 schema + 使用者詞庫匯入匯出（.zip，Linux 同義）                  │
└──────────────────────────────┬──────────────────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────────────────┐
│  上游與參照                                                              │
│   ├─ fcitx5-android master（fork 底座，plugin/chewing 保留）              │
│   ├─ 渲染參照：Futo Keyboard（onDraw loop/PreviewChoreographer/高度數）   │
│   ├─ 佈局參照：HeliBoard（自訂佈局靈感）/ mzbgf JSON KeyConfigLoader      │
│   ├─ 輕量參照：Guileless（12MiB）/ WeType SelfDraw（單容器論證）          │
│   └─ iOS 參照：fcitx5-ios（僅筆記：App Group/extension 約束）             │
└─────────────────────────────────────────────────────────────────────────┘
```

## 3. 輸入資料流

```
觸控 → KeyboardView.hitTest → fcitx5-chewing → libchewing Editor
  → 候選 (frequency 排序) → CandidateView 上屏
  → OpenCC（若簡繁切換開）→ InputConnection.commitText
  → UserDict 記憶（頻率 + last-used）→ 下次排序更準
```

## 4. Wave 路線圖（時間軸）

```
##########█ T1[✓] T2[✓] T3[✓] T4[✓] ████░░░░░░ T1[✓] T2[~] T3[ ] T4[ ]
Wave1 解碼 ██████████ T5[✓] T6[✓] T7[✓] T8[✓] T9[✓] T5-T9（版本釘選/三佈局/OpenCC/詞庫/AAR備援/自檢）
Wave2 前端 ██████░░░░ T10[✓] T11[✓] T12-T15[ ] T10-T15（Service/View/候選/Emoji/主題/自檢）
Wave3 人體工學 ██████ T16-T20 DONE（KEYMAP 凍結/工具列/無衝突/Gboard對比/自檢）
Wave4 切分發版 ████▓▓ T21[✓] T22[✓] T23[✓] T24[~]（core/UI/iOS筆記/矩陣建置/封版）
終驗 ░░░░░░░░░░ F1-F4（合規/品質/矩陣/保真，全 APPROVE 才算完）
```

## 5. Addy 24 技能 → 本環境等價 → Todos 路由表

`addy-*` 系列在此 runtime 未安裝；下表為等價映射，worker 按 Todo 到站時自行載入對應技能。
「深理解」要求：每個 Todo 動工前，worker 按路由載入 ≥1 技能並在 DoneClaim 註記。

| # | Addy 原技能 | 本環境等價 | 路由 Todos |
|---|-------------|-----------|-----------|
| 1 | idea-refine | brainstorming | 全波（需求澄清時） |
| 2 | interview-me | question 工具 + grill-me |  plan 階段（已用） |
| 3 | spec-driven-development | writing-plans + plan | T4/T9/T15/T20/T24 自檢 |
| 4 | planning-and-task-breakdown | writing-plans + orchestrate | 全波（本表即產物） |
| 5 | source-driven-development | 官方文件优先（AOSP/LatinIME/上游 README） | T10/T11/T16 |
| 6 | test-driven-development | tdd / test-driven-development | 全波 QA（本案用 tests-after，見 plan） |
| 7 | incremental-implementation | executing-plans（小步 commit） | 全波（每 todo 一 commit） |
| 8 | api-and-interface-design | engines/core API 邊界（ARCHITECTURE） | T21/T10 |
| 9 | frontend-ui-engineering | frontend-design + ui-ux-pro-max + baseline-ui | T11-T14/T16-T17 |
| 10 | code-review-and-quality | brooks-review + code-review-excellence + official-code-review | F1/F2 |
| 11 | code-simplification | simplify-code + deslop + complexity-cuts | T21/全波收尾 |
| 12 | debugging-and-error-recovery | diagnose + systematic-debugging + debugging-code | 缺陷回路（#836 類） |
| 13 | performance-optimization | complexity-cuts + fixing-motion-performance + lemmaly | T11/T15/T23 + AUDIT§6 |
| 14 | observability-and-instrumentation | 日誌/量測表（CHECKS/*.md） | T15/T19/T20/F3 |
| 15 | security-and-hardening | security-review + safe-mode | T18/F2 + AUDIT§7 |
| 16 | context-engineering | compact-guard + context-optimizer | handoff（本檔） |
| 17 | using-agent-skills | using-superpowers（本回合） | 全波 |
| 18 | git-workflow-and-versioning | using-git-worktrees + smart-commit | T2/T24/F4 |
| 19 | ci-cd-and-automation | release-skills（F-Droid/Play 雙通道） | T23 |
| 20 | browser-testing-with-devtools | 真機矩陣 + 錄影（替代） | F3 |
| 21 | deprecation-and-migration | chewing_Reset 語義變更管理（UPSTREAM） | T2/T5 |
| 22 | documentation-and-adrs | 本檔 + AUDIT.md + OSS-NOTES.md（ADR 散記） | T1-T4/T24 |
| 23 | shipping-and-launch | release-skills + finishing-a-development-branch | T23/T24 |
| 24 | doubt-driven-development | plan-interrogate + llm-council（重大決策前） | T16/T21 |

## 6. 模板與主題聲明

- 模板：深色紙面 `baoyu-diagram` 系（slate-900 `#0f172a` + cyan/emerald/violet 語義色），架構圖見 `docs/diagram/architecture.svg`。
- 主題：鍵盤本體跟隨 M3 動態色 + OLED 純黑 + 自訂背景（T14）；文檔配色沿用上表，不另起爐灶。
- 精簡原則：先借上游（fork），再抽共用（core/UI），最後才自寫；任何自寫模組需在 AUDIT 留下「為何不借」的一行理由。

## 7. Handoff 清單（給下一個會話）

1. 讀：本檔 → `ARCHITECTURE.md` → `.omo/plans/android-keyboard.md` → `.omo/drafts/android-keyboard.md`（記憶）→ ledger 尾 5 行（進度）。
2. 查：`background_output` 只在收到 `<system-reminder>` 後調用；`writing` 分類模型缺失，一律走 `general` 通道逐條派單。
3. 禁：UPSTREAM.md（T2 執行中）、LICENSE/NOTICE/RELEASE（T3 排隊）當前不可碰；不推 push；`videos/*` 等他區零改動。
4. 續：T2 完成通知 → 驗收 → 勾 plan → 派 T3 → Wave0 自檢 T4 → Wave1。
