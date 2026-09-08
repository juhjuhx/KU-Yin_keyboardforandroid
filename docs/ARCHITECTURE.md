# 架構說明（ARCHITECTURE）

> 狀態：**骨架（skeleton）** — 本檔描述 `android-keyboard` fork-mirror 的目標架構與切分原則；實碼與依賴圖於後續 waves（Todo 10 之後）落地。

---

## 1. 定位與上游

- 底座：[fcitx5-android](https://github.com/fcitx5-android/fcitx5-android)（`master`，LGPL-2.1）。
- 本專案為 fork-mirror，目標是「可長期迭代、可直接推送 GitHub、含上游宣告與授權合規」。

## 2. 三大支柱（解碼 / 簡繁 / 前端）

| 支柱 | 技術 | 說明 |
|------|------|------|
| 解碼 | `libchewing` 經 `fcitx5-chewing` | 預設大千（DaChen）4x10，附 Hsu、Eten26 選項 |
| 簡繁 | `OpenCC` | 一鍵切換；預設台灣 `s2tw` / `tw2s`，fallback `s2t` / `t2s` |
| 前端 | Kotlin `View` / `Canvas` 自繪 | AOSP LatinIME 思路；**非 Compose 主渲染** |

## 3. core / UI 切分（iOS 預留）

- `engines/core`：chewing / OpenCC / 配置 schema（與平台無關，iOS 可複用）。
- `app/`（Android UI）：`InputMethodService` + 自繪 Keyboard `View`/`Canvas` + 候選欄 + 主題。
- 原則：UI 層**不直接呼叫 JNI 解碼**，一律經 `core` 介面（依賴走查於 Todo 21 驗證）。

## 4. 前端渲染策略（低延遲）

- `InputMethodService` 生命週期：`onCreateInputView` / `onStartInput` / batch edit。
- 自繪 Keyboard View（硬體加速開啟）、鄰鍵 `getNearestKeys` / `ProximityInfo` 誤觸處理。
- 候選欄分頁/展開、按壓 popup、長按 350ms 符號、Emoji/符號選擇器、純文字剪貼簿、Material You 3 動態色。
- 做法對標 AOSP LatinIME；不拿 Compose 當輸入視窗主渲染。

## 5. 依賴圖與越層規則（佔位）

> 依賴圖於 Todo 21 落地後於此補圖。

- UI → core：唯讀介面呼叫。
- core → JNI：唯一允許 JNI 的層。
- 禁止：UI 層直調 JNI、core 層依賴 Android UI。

---

_本檔為 Todo 1 骨架；後續 waves 將填充實作細節與依賴圖。_
