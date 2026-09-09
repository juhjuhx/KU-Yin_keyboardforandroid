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

- [x] 記錄 fcitx5-ios 三約束（App Group 共享目錄、extension 進程/記憶體、配置 schema 共用）。
- [x] 寫死 core 複用邊界與緩解（無開發者帳號需 full access + Sync config）。

---

## fcitx5-ios 複用約束（Todo 22 填充）

> 參考專案：[`fcitx-contrib/fcitx5-ios`](https://github.com/fcitx-contrib/fcitx5-ios)（iOS ≥ 16.3、CMake + Ninja、developer beta）
> 建置檔：`fcitx5-ios-prebuilder`（https://github.com/fcitx-contrib/fcitx5-ios-prebuilder）

### 1. App Group 共享目錄機制

- iOS keyboard extension 與 container app 是**兩個獨立進程**，透過 App Group entitlement 共享一個目錄（`~/Library/Developer/CoreSimulator/Devices/UUID/data/Containers/Shared/AppGroup/<group-id>`）。
- 配置（`fcitx5` config schema INI 檔）與用戶詞庫均存放於此共享目錄，由 container app 寫入、keyboard extension 讀取。
- **無開發者帳號時 App Group 不可用**：keyboard extension 必須開啟 Full Access，且每次修改配置後需手動點擊 Sync config。container app 的寫入可能靜默失敗（extension 端無寫入權限），需以 timestamp 比較新舊版本並做 merge（參考 fcitx5-ios 的 `activeLanguage` stamp 機制）。

### 2. Extension / Container 進程拆分

| 項目 | Container App | Keyboard Extension |
|------|---------------|-------------------|
| 進程 | 主進程，無記憶體特殊限制 | 獨立進程，受 jetsam 嚴格監控 |
| 網路 | ✅ 有 | ❌ 預設無（需 Full Access） |
| 剪貼簿 | ✅ 系統剪貼簿 | ❌ 預設無（需 Full Access） |
| App Group 寫入 | ✅ 可靠 | ⚠️ 無 Full Access 時可能靜默失敗 |
| 音效/震動 | 正常 AudioSession | ❌ 無 AudioSession，需在按鍵路徑上以低預算產生 |

- Keyboard extension 的 `UIInputViewController` 子類及其 view 不會被 iOS 自動釋放（每切換一個 host app 就建立新 controller，但保留舊的 view），導致記憶體逐 host 累積。fcitx5-ios 以此為已知陷阱並做了專門處理。

### 3. 記憶體與無網路限制

- **記憶體上限**：fcitx5-ios 文件記載 **77 MB**（`memorystatus: keyboard exceeded mem limit: ActiveHard 77 MB`）。第三方分析指出實際 `phys_footprint` 上限約 **60 MB**，且無 crash log — jetsam 直接終止 process，使用者看到「鍵盤自己消失」。
- **無網路**：keyboard extension 預設無網路 entitlement，不可發 HTTP 請求。詞庫更新、雲同步等功能必須在 container app 中完成，再透過 App Group 共享結果。
- **無音訊 session**：按鍵音效/震動必須在 extension 內以极低預算（無 AudioSession）即時產生。

### 4. iOS 可消費的 Engine / Core API 表面

fcitx5-ios 以 CMake 建置，依賴以下模組（透過 `.gitmodules` submodule 拉取）：

| 模組 | 用途 | 本專案是否需要 |
|------|------|---------------|
| `fcitx5`（core） | Fcitx5Utils + Fcitx5Config + Fcitx5Core：事件迴圈、配置 INI 解析、InputMethodEngine 介面 | ✅ 需要（配置 schema 共用） |
| `fcitx5-chewing` | libchewing 橋接（Bopomofo 注音引擎） | ✅ 需要（核心引擎） |
| `libchewing`（prebuilt `.a`） | C 語言注音解碼庫 | ✅ 需要（與 Android 同源） |
| `fcitx5-chinese-addons` | 簡繁轉換（OpenCC）、拼音、倉頡等 | ⚠️ 僅 chttrans（OpenCC wrapper）需要 |
| `libime` | 詞庫格式（kenlm n-gram） | ⚠️ 視詞庫格式而定 |
| `fcitx5-rime` / `fcitx5-hallelujah` / `fcitx5-lua` / `fcitx5-mozc` | 其他引擎 | ❌ 不需要 |
| `ios-cmake` | iOS 交叉編譯 toolchain | N/A（Android 用 NDK） |

**本專案 iOS 可消費的最小 API 表面**：
1. **chewing engine**（`fcitx5-chewing` + `libchewing`）— 注音解碼核心，與 Android 同源 C 原始碼。
2. **chttrans / OpenCC**（`fcitx5-chinese-addons` 的 chttrans 模組）— 簡繁轉換，配置 schema 共用。
3. **Fcitx5Config INI schema** — 配置檔格式（`~/.config/fcitx5/conf/` 下的 `.conf` 檔），container app 與 extension 共用同一 schema。

**零 Android UI 複製**：iOS 端需全新 Swift/UIKit 鍵盤視圖，不消費 Android 的 `KeyboardView`、`CandidateView`、`ChewingInputMethodService` 等任何 Kotlin UI 層。

### 5. 明確 NON-Goals（本期不實作）

- ❌ 不建置 iOS App / keyboard extension。
- ❌ 不引入 Xcode project / Swift Package Manager / iOS SDK。
- ❌ 不複製 Android UI 程式碼到 iOS。
- ❌ 不處理 Apple Developer 帳號 / App Group provisioning / 簽名。
- 本節僅為**評估筆記**，供後續 iOS port 計畫使用。

---

_本檔為 Todo 1 骨架；Todo 22 fcitx5-ios 複用約束已填充。_
