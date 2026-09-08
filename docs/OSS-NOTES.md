# OSS 參照筆記（可抄襲清單，皆附授權）

> 原則：先借上游，再借參照，最後自寫。自寫需在 AUDIT 留「為何不借」一行理由。
> T2 執行中、T3 排隊中：本檔只新增，不碰 `UPSTREAM.md` / `LICENSE` / `NOTICE` / `RELEASE.md`。

## 採用（可直接借，標註授權）

| 項目 | 來源 | 授權 | 用途（落點） |
|---|---|---|---|
| fork 底座全套 | fcitx5-android/fcitx5-android master | LGPL-2.1 | 全部（T2 釘選） |
| JSON 配置化佈局 `KeyConfigLoader`（+73 行，asset 讀 `keyboard_layout.json`，失敗回退預設） | mzbgf/fcitx5-android commit f5e7947 | LGPL-2.1（上游 fork） | T11/T16 佈局配置化 |
| key border stroke（E-ink 友善） | 上游 PR #799（plateaukao，2025-10 已合併） | LGPL-2.1 | T14 主題選項 |
| 候選分頁按需加載 | 上游 0.0.6 | LGPL-2.1 | T12 沿用 |
| 高度 RealSize 基線 / 旋轉不閃 | 上游 0.1.3 | LGPL-2.1 | T10/T14 |
| 主題設計器 | fcitx5-android.github.io/theme-designer | 工具（输出自有） | T14 出主題 |
| 自訂佈局靈感 | HeliBoard（上游 #377 討論引用） | Apache-2.0（借思路，需標註） | T16 follow-up |
| 27 節 IME 指南（API 30–36/M3/Baseline Profile/Perfetto/FTL） | Made-in-Jurgistan/android-keyboard-design-guide | 看其 LICENSE 後標註 | T10-T15/T23 方法論 |

## 論證（不抄碼，抄結論）

- **WeType SelfDraw**：單 `ViewGroup` + 手繪 `SelfTextView`，省 `measure/layout` 與 `DisplayList` → 本案單容器自繪的第三方背書（AUDIT§6）。
- **Futo Keyboard 渲染**：`onDraw` 全鍵 loop + `KeyPreviewChoreographer`（preview 高 80dp/上偏 -8dp/linger 70ms）→ T11/T12 參數起點；其裝置高度表（直 205.6 / 橫 176 / sw600 302.4 / sw768 365.4dp）與本案 KEYMAP（直 252–280 含 44dp 候選欄）交叉驗算一致。
- **risingsun-ai/android-ime**：Compose Canvas 零逐鍵 composable + `LruCache(200)` 預測 + Room → 證明「零逐鍵單元」與詞庫兩級快取思路通用；本案仍選 View 主渲染（輸入視窗生命週期），只借快取思路（T7）。

## 引擎新訊（已同步進 Scope/draft）

- libchewing 主倉已遷 **Codeberg**（`codeberg.org/chewing/libchewing`），GitHub 為鏡像；最新 **0.13.1**（2026-07-15）。
- 0.11→0.13 關鍵：`chewing_handle_KeyboardEvent`（T5 優先）、`chewing_new3`、頻率排序候選、auto_snapshot、deleted-phrases 排除字典、SQLite 預設關、Swift package（iOS 利好，T22 備註）。
- Rust MSRV 1.88：T23 NDK/Rust 工具鏈版本對照時注意。

## 不採用（明確拒絕，理由）

- Compose 主渲染輸入視窗（risingsun 路線）：輸入視窗生命週期 + 延遲風險，本案只借其快取思路。
- RIME/Trime 主引擎：與「Linux 同款 chewing」目標衝突，僅借 OpenCC/詞庫維護思路。
- 九宮格/日語十二鍵/引擎控佈局（上游 #377/#109/#375 open）：本期不承諾，列 follow-up。
