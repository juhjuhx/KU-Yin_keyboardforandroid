# 七維審計報告（內容 / 資料 / 美觀 / 接口 / 架構 / 效能 / 安全）

> 審計對象：plan + 現有 4 份骨架文檔 + 上游/參照專案（本倉尚無產品碼，碼級審計延至各 Todo 落地時執行）。
> 審計日期：2026-09-08。結論先行：**架構成立，3 處需修正（已修 2，1 待 worker），無阻擋項。**

## §1 內容審計（文檔完整性）

- PASS：README/ARCHITECTURE/KEYMAP/IOS-NOTES 四檔齊，聲明（fork/LGPL-2.1/三支柱/大千預設）一致。
- FIND-01（已修）：plan 引用的 libchewing 版本（0.11/0.12）落後，實地已發 **0.13.1** 且主倉遷至 **Codeberg** → Scope 與 draft 已改推 0.13.x，T2 以實抓 SHA 為準。
- FIND-02（待 worker）：`docs/UPSTREAM.md`、`NOTICE` 缺失為已知排程（T2/T3），非缺陷；T4 自檢負責收口。

## §2 資料審計（詞庫與配置資料流）

- 詞庫三層：內建（libchewing-data 構建）→ 使用者（自動學頻率 + last-used）→ 排除（0.12+ `chewing-deleted.dat`，刪詞不再復活）。
- 0.13 新增 `chewing_handle_KeyboardEvent`（任意佈局免升級 libchewing）與 `chewing_new3`（按名啟用字典）：T5 應優先採用，可省 Hsu/Eten26 特判代碼。
- 風險：SQLite 預設關閉（0.11+），若上游 plugin 仍假設 SQLite 在，T5 需驗 `sqlite-bundled` 開關；已記入 T5 驗收（頻率/排序抽查）。
- 匯入匯出沿用上游 `.zip`（0.0.7 能力），Direct Boot 下行為與上游一致，不自創格式。

## §3 美觀審計（鍵盤視覺）

- 基線 = 上游已合併能力：動態色（0.1.2）、候選圓角/標點隱藏/key border stroke（#799，E-ink 友善）、emoji 過濾（#747）+ 膚色。
- 本案加碼：OLED 純黑態、自訂背景不擋鍵帽字、三態截圖對比（T14）。不另造主題引擎，直接吃上游 Theme 體系。
- 文檔圖表配色鎖定 `baoyu-diagram` 語義色（ROADMAP§6），避免多套色板。

## §4 前後端接口審計（UI ↔ core 邊界）

- 唯一合法邊界：`app/ui` → `engines/core` API；UI 零直調 JNI（T21 走查）。
- 事件流：觸控 → hitTest → chewing Editor → 候選 → OpenCC → `commitText`；`beginBatchEdit` 包裹多步提交（T10）。
- 上游已知坑（已納入）：OTP 多格重建鍵盤（禁）、密碼框上屏候選（禁）、`chewing_Reset` 語義變更（T2 註記 + T5 不回落驗收）。
- 待補（上游 open #377）：引擎控制鍵盤佈局（注音符號鍵、九宮格）上游尚未支援；本案 T16 先做靜態大千 4x10，動態佈局列為 follow-up，不在本期承諾。

## §5 架構審計（切分程度）

- 三層（APP/UI → CORE → 上游/參照）職責清晰；iOS 只消費 core（配置 schema + 字典能力），約束已記（App Group/extension/無網路）。
- 精簡點：mzbgf 的 `KeyConfigLoader`（JSON 驅動 `TextKeyboard` 佈局）證明「配置化佈局」可行且僅 +73 行；T11/T16 可借其思路，授權標註後抄（對方為上游 fork，LGPL-2.1 相容）。
- 反模式守衛：禁逐鍵 View（WeType SelfDraw + Futo + risingsun 三方一致：單容器自繪）、禁 Compose 主渲染（輸入視窗生命週期風險）、禁 INTERNET。

## §6 效能審計 + 開銷分析（會不會吃性能）

結論：**本架構天然低開銷；風險集中在三處，皆有預算與走查。**

| 開銷源 | 分析 | 預算 / 緩解（Todo） |
|---|---|---|
| 按鍵繪製 | 單容器 `onDraw` 全鍵 loop；40 鍵 × 簡單 rect+text，硬體加速下 <8ms/幀；WeType/Futo 同構已驗證 | T11 走查 50 邊緣點 + T15 旋轉/主題 |
| 解碼計算 | libchewing 0.13 conversion 路徑上限收斂到 10（`max output paths`），條件概率評分；逐鍵增量計算，ms 級 | T5 三佈局抽查 + T9 自檢 |
| 字典體積/記憶體 | trie + 使用者 dict；SQLite 預設關 → 常駐更小； APK 參照：上游 ~43MiB、Guileless 12MiB | T23 ABI/體積記錄；常駐增量目標 ≤ 上游 +30MB（T23 驗收加記） |
| 候選分頁 | 上游 0.0.6 已做按需加載（paging），不一次全取 | T12 沿用，不自造 |
| 主題/動態色 | Monet 取色一次性；旋轉不重建（RealSize 高度基線，0.1.3 新增） | T14 三態截圖 + T10 旋轉不丟 preedit |
| 電量 | 無輪詢、無網路、事件驅動；震動 10–20ms 可關 | T13/T18 |

- 效能預算（凍結）：鍵盤彈出 <200ms 體感；`onDraw` 全量 <8ms；常駐增量 ≤ +30MB；APK 體積記錄（不設硬頂，先量測，T23）。
- 吃性能開銷？答：不會是吃電怪獸；最大頭是主題背景大圖與候選無限加載，兩者本案都已封（自訂背景壓縮 + 按需分頁）。

## §7 安全審計

- 權限最小化：vibrate + plugin 權限；**零 INTERNET**（F2/T18 雙鎖）；剪貼簿僅純文字（防富文本外帶）。
- 鍵擊數據不出進程：無雲同步、無帳號、無 AI 外傳；詞庫只落本地 + 使用者自主管 `.zip` 匯出。
- Direct Boot：沿上游行為（0.0.8），不自行放寬鎖屏前可用面。
- 供應鏈：submodule SHA 釘選（T2）+ 每次構建記錄；libchewing 遷 Codeberg 後 URL 更新已同步（FIND-01）。
- 待 worker：T18 manifest 走查 + T23 權限表對照，F2 收口。

## 審計→Todos 追蹤表

| 發現 | 落點 | 狀態 |
|---|---|---|
| libchewing 0.13.x + Codeberg | Scope/T5/T2 | 已修（推版本）、T2 實抓 |
| `chewing_handle_KeyboardEvent` 優先 | T5 | worker 到站執行 |
| JSON 配置化佈局（mzbgf） | T11/T16 借鑒 | 已記 OSS-NOTES |
| 效能預算四條 | T11/T15/T23/F3 | 已寫入本節，T23 加記體積 |
| #377 動態佈局 follow-up | 範圍外 | 明確不承諾 |
| 九宮格/日語十二鍵 | 範圍外 | 上游 open issue，不承諾 |
