# iOS 複用筆記（IOS-NOTES）

> 狀態：**骨架（skeleton）** — 本檔僅為「筆記-only」標記，**不包含、也不承諾任何 iOS 實作**。
>
> 本期範圍（Todo 1 / Todo 22）：僅記錄 core/UI 切分與 `fcitx5-ios` 複用邊界，供後續評估。

---

## 標記：僅筆記，不實作

- 本期**不**實作 iOS App / extension。
- 本期**不**引入任何 iOS SDK / Xcode / Swift 建置。
- 承諾邊界：iOS 只「消費」`engines/core`（chewing / OpenCC / 配置 schema），不複製 Android UI。

## 複用來源（參考，占位）

- 參考專案：`fcitx-contrib/fcitx5-ios`（iOS ≥ 16.3、CMake、App Group、keyboard extension）。
- 約束（計畫 Todo 22 詳列）：extension 與 app 不同進程、記憶體/無網路限制、配置 schema 共用等。

---

## 待補（TODO，Todo 22 填充）

- [ ] 記錄 fcitx5-ios 三約束（App Group 共享目錄、extension 進程/記憶體、配置 schema 共用）。
- [ ] 寫死 core 複用邊界與緩解（無開發者帳號需 full access + Sync config）。

---

_本檔為 Todo 1 骨架；詳細複用筆記於 Todo 22 填寫。_
