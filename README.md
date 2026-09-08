# android-keyboard

> 本倉庫是 **fork-mirror 骨架**，以官方 [fcitx5-android](https://github.com/fcitx5-android/fcitx5-android)（`master`，**LGPL-2.1**）為底座的長期可迭代 Android 注音輸入法（IME）專案鏡像。

> ⚠️ **處於骨架（scaffold）階段**：本目錄目前僅有文件骨架，尚未執行 `git clone` / `git init` / submodule 實抓。請參考 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) 與規劃文件。

---

## 專案定位

- **上游來源**：[fcitx5-android/fcitx5-android](https://github.com/fcitx5-android/fcitx5-android)（`master` 分支，**LGPL-2.1** 授權）。本專案為其 fork-mirror，保留上游合規宣告與借用來源標註。
- **解碼器**：`libchewing`，經 `fcitx5-chewing` 橋接（與 Linux 桌面端同款引擎）。
- **簡繁轉換**：`OpenCC`（一鍵切換，預設台灣 `s2tw` / `tw2s`）。
- **前端渲染**：Kotlin `View` / `Canvas` 自繪鍵盤（低延遲），**不**以 Compose 作為輸入視窗主渲染。
- **預設注音佈局**：大千（DaChen）4x10；另備 Hsu、Eten26 選項。

> 以 SwiftFloris 授權同理，所有借用代碼與品牌素材皆標註來源與授權，詳見 `NOTICE`（沿用計畫 Todo 3，尚未落地）。

---

## 目錄結構（骨架）

```
android-keyboard/
├── README.md              # 本檔：fork 聲明 + 專案定位
└── docs/
    ├── ARCHITECTURE.md    # 架構說明（core / UI 切分、解碼、前端）
    ├── KEYMAP.md          # 鍵盤佈局與尺寸規格表（人體工學，超 Gboard 目標）
    └── IOS-NOTES.md       # iOS 複用筆記（僅筆記，不實作）
```

---

## 授權與上游聲明（Todo 1 範圍）

| 項目 | 值 |
|------|----|
| 上游 | https://github.com/fcitx5-android/fcitx5-android |
| 上游分支 | `master` |
| 上游授權 | LGPL-2.1 |
| 本專案定位 | fork-mirror 骨架 |

> `LICENSE` / `NOTICE` / `docs/RELEASE.md` / `docs/UPSTREAM.md` 屬計畫 Todo 3–4 範圍，尚未於本體落地（參見 Todo 1 Accept 範圍）。

---

## 規劃引用

- 完整工作計畫：`D:\666\opencode\.omo\plans\android-keyboard.md`
- 草稿：`.omo/drafts/android-keyboard.md`

## 後續文檔（Wave 0 研究產物）

- `docs/ROADMAP.md` — 設計方向、TUI 架構圖、路線圖、Addy 路由表、handoff 清單
- `docs/AUDIT.md` — 七維審計（內容/資料/美觀/接口/架構/效能/安全）+ 效能開銷分析
- `docs/OSS-NOTES.md` — 可借用開源清單（mzbgf/HeliBoard/Futo/WeType/0.13 新訊）
- `docs/diagram/architecture.svg` — 架構圖（深色模板）
