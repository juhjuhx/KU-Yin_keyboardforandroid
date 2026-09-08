# 使用者字典維護：與 Linux 版對等（USERDICT）

> 目標：讓 `android-keyboard` 的使用者自訂詞庫（使用者字典）在**匯出／匯入**與**學習模型**上，與 Linux 上的 fcitx5 桌面版達到行為對等。
> 此文件為 **Todo 7（tier MEDIUM）** 之分析規格文件：**只做分析與規格，不改程式碼**。
> 上游參考來源（`HEAD`，僅供查閱）：
> - `fcitx5-android` @ `6998502`（含輸入法匯出／匯入機制）
> - `fcitx5-chewing` @ `60096e8`（fcitx5 的注音輸入法外掛）
> - `libchewing`（來源：Codeberg `chewing/libchewing`，Rust 重寫版）

---

## 1. 上游使用者資料匯出／匯入機制（fcitx5-android）

### 1.1 資料來源位置
`UserDataManager`（`data/UserDataManager.kt`）定義三個匯出來源 + 一份 metadata：

| 區塊 | Android 路徑 | 內容 |
| --- | --- | --- |
| `shared_prefs/` | `dataDir/shared_prefs/` | 應用偏好（含最近使用、UI 設定等） |
| `databases/` | `dataDir/databases/` | SQLite 資料庫（如適用） |
| `external/` | `getExternalFilesDir(null)` | **主要的輸入法資料**（含使用者字典） |
| `metadata.json` | —（由程式產生） | 匯出資訊 |

> `externalDir = appContext.getExternalFilesDir(null)`（`UserDataManager.kt:56`），通常解析為
> `/storage/emulated/0/Android/data/org.fcitx.fcitx5.android/files/`。
> 匯出時會遞迴走訪 `shared_prefs`、`databases`、`external` 三個目錄樹（`writeFileTree`），
> 再加上根層的 `metadata.json`。

### 1.2 `metadata.json` 結構
```kotlin
@Serializable
data class Metadata(
    val packageName: String,
    val versionCode: Int,
    val versionName: String,
    val exportTime: Long,        // epoch millis
    val isDataUserBuild: Boolean,
)
```
匯入時會檢查 `packageName != BuildConfig.APPLICATION_ID` 才允許（`UserDataManager.kt:100`），
並在成功後重啟 fcitx（`AdvancedSettingsFragment.kt:56,75`）。

### 1.3 匯出／匯入流程（UI 出口）
位於 `ui/main/settings/behavior/AdvancedSettingsFragment.kt`：
- **匯出**：`exportLauncher.launch("fcitx5-android_${iso8601UTCDateTime(timestamp)}.zip")` → `UserDataManager.export(...)`。
- **匯入**：`importLauncher.launch("application/zip")`（`confirm_import_user_data` 對話框確認）→ `UserDataManager.import(...)`。

**要點**：Android 端「匯出」**是整包 `.zip`**（含設定、資料庫、外部資料），並非僅字典。
任何「與 Linux 對等」的設計都必須釐清：使用者字典在 Linux 是獨立檔案，但 fcitx5-android 是整包備份。

---

## 2. libchewing 學習模型（注音使用者字典）

### 2.1 使用者字典檔案（`src/path.rs`）
| 平台 | 使用者資料目錄 | 使用者字典檔 |
| --- | --- | --- |
| Linux | `$XDG_DATA_HOME/chewing` 或 `~/.local/share/chewing` | `<資料目錄>/chewing.dat` |
| Windows（參考） | `%AppData%\Chewing` | `<資料目錄>\chewing.dat` |
| macOS（參考） | `~/Library/Application Support/im.chewing.Chewing` | 同上 |

- 可用 `CHEWING_USER_PATH` 環境變數覆寫資料目錄（`path.rs:170-175`）。
- 搜尋路徑為 `<user_datadir>`、`<user_datadir>/dictionary.d`、`<sys_dir>`（`search_path_from_env_var`）。
- 系統字典：`libchewing/tsi.dat`（fcitx5-chewing `getChewingContext` 用 `chewing_new2(...sysPath)` 指定）。

### 2.2 學習模型的資料模型（`src/dictionary/`)
- 詞條 = `Phrase { text: String, freq: u32, last_used: Option<u64> }`。
- 使用者字典（`TrieBuf`）內部以 `BTreeMap<PhraseKey, (u32 freq, u64 last_used)>` 維護，另有一份 `graveyard`（已刪除集合）。
- **分層字典**（`Layered`）合併多層：`built-in`（系統）、`extension`、`custom`、`user`（讀寫）、`exclude-list`（排除）。
  - 使用者詞頻 > 內建詞頻時**覆寫**內建；否則保留較高者（`layered.rs:161-178`）。
  - 移除多字詞 → 寫入 **exclude-list**（排除清單），同時從使用者字典移除；**單字元**移除只從使用者字典移除（`layered.rs:265-281`）——這是防呆：避免單字（詞頻極高）被誤排除。

### 2.3 頻率增量規則（`editor/estimate.rs`，`LaxUserFreqEstimate`）
以 `lifetime`（鬆散計時，不落盤，取使用者字典中最大 `last_used` 當起點）計算 `delta_time = now - last_used`：

| 條件（delta_time） | 新詞頻增幅 |
| --- | --- |
| `== 0`（新詞） | `max_freq + 10`（新詞直接拉最高，優先選用） |
| `< 4000` | `freq + 10`（短增量） |
| `< 50000` | `freq + 5`（中增量） |
| 其餘 | `freq + 1`（長增量，避免冷門詞暴衝） |
| 上限 | `MAX_USER_FREQ = 9_999_999` |

> 每個使用者輸入「交互」呼叫一次 `tick()`（`UserFreqEstimate::tick`）。`last_used` 為給定 epoch 之 tick 值。

### 2.4 檔案格式快取／落盤
- `TrieBuf::open` 若目標檔不存在會自動建立空字典（`name="我的詞庫"`）。
- `flush()` / `checkpoint()` 在**背景執行緒**把 `btree + graveyard` 快照寫回 `.dat`（`trie_buf.rs:300-345`）；`Drop` 時 `wait + sync + flush`。
- 字典檔頭含軟體版本資訊（`software_version()`）。

---

## 3. 檔案格式與儲存路徑對照表

> 下表對照 **Linux 桌面版 (fcitx5 + libchewing)** 與 **Android (fcitx5-android + libchewing)** 的使用者字典相關路徑與檔案。

| 項目 | Linux (fcitx5+libchewing) | Android (fcitx5-android) | 備註 |
| --- | --- | --- | --- |
| 使用者資料根 | `~/.local/share/chewing`（或 `$XDG_DATA_HOME/chewing`） | `.../Android/data/org.fcitx.fcitx5.android/files/`（`getExternalFilesDir(null)`） | 可被 `CHEWING_USER_PATH` 覆寫 |
| 使用者字典檔 | `<根>/chewing.dat` | 同上（`chewing.dat`，位於 external 目錄） | 由 `UserDataManager` 以 `external/` 區塊備份 |
| 搜尋路徑 | `<根>`、`<根>/dictionary.d`、`<sys>` | 相同（libchewing 內部決定，含 `CHEWING_USER_PATH`） | `search_path_from_env_var` |
| 系統字典 | `/usr/share/libchewing/tsi.dat` | `<sys>/libchewing/tsi.dat`（由 `getChewingContext` 定位） | fcitx5-chewing `eim.cpp:295-300` |
| 匯出／匯入 | 無原生（檔案直接複製） | `.zip` 整包備份（`external/`+`shared_prefs/`+`databases/`+`metadata.json`） | 見 §1 |
| 詞條格式 | `Phrase { text, freq, last_used }`（`.dat` 內） | 相同（同 libchewing 核心） | — |
| 排除清單 | `.dat` 內 `graveyard` / exclude-list | 相同 | 單字不移除 |
| 學習計時 | `LaxUserFreqEstimate`（鬆散 tick，不落盤） | 相同 | — |

**對等性結論**：
- 兩端共用**同一套 libchewing 核心**，因此「學習模型」「詞條格式」「頻率規則」完全一致。
- **唯一差異**在「備份／搬遷載體」：Linux 直接複製 `chewing.dat`；Android 透過 fcitx5-android 的 `.zip` 匯出／匯入，且該 `.zip` 同時含設定與其他輸入法資料。

---

## 4. 匯出－清除－匯入 hash 相符測試計畫

> 目的：驗證「Android 使用者字典」與「Linux 使用者字典」在匯出、清除、匯入三步驟後，**詞庫內容一致（hash 相符）**。
> 由於開發環境為 Windows（無 Linux GUI），此計畫以**檔案層級 hash 比對**為主，輔以可選的 Linux 實機對照。

### 4.1 前置條件
1. 產生一組**已知詞庫**（含自訂多字詞、單字、被移除的多字詞），確保學習模型有資料：
   - 新增多字詞：`測試一下`（`ㄘㄜˋ ㄕˋ ㄧ ㄒㄧㄚˋ`）
   - 新增單字：`測`
   - 移除多字詞：`舊詞`（應進入排除清單，不再出現）
2. 計算**基準 hash**：
   - Linux（或同核心之參考實作）產出 `chewing.dat` → `SHA-256(chewing.dat) = H_ref`。
   - Android 端匯出 `.zip`，解壓 `external/.../chewing.dat` → 記錄檔路徑。

### 4.2 測試步驟（三步驟循環）
| 步驟 | 動作 | 預期 |
| --- | --- | --- |
| **A. 匯出** | Android 設定 → 匯出使用者資料 → 保存 `fcitx5-android_*.zip` | `.zip` 內含 `external/.../chewing.dat`；解出後 `SHA-256 = H_A` |
| **B. 清除** | 使用者手動清除/重置詞庫（或使用全新的 `CHEWING_USER_PATH`） | 輸入時不再出現自訂詞 |
| **C. 匯入** | 匯入步驟 A 的 `.zip` → 重啟 fcitx | 輸出自訂詞恢復；解出 `chewing.dat` → `SHA-256 = H_A'` |

**相符判定**：`H_ref == H_A == H_A'`（跨平台一致）且 `H_A == H_A'`（匯出→清除→匯入 round-trip 安定）。

### 4.3 明確驗證點
1. **Round-trip 安定性**：`H_A == H_A'` —— 匯出再匯入不得改變詞庫二進位內容。
2. **跨平台一致**：`H_ref == H_A` —— Android 與 Linux 產出相同 `chewing.dat`（需 libchewing 版本一致）。
3. **排除清單**：被移除的多字詞在匯入後**不得**重新出現（`graveyard`/exclude-list 被還原）。
4. **單字防呆**（對應 prior lesson：`Guileless #11` 單碼點回歸守衛）：移除過的單字仍可依內建詞頻正常出現（未被排除清單遮蔽）。
5. **新詞優先**：匯入後首次輸入新詞仍會被拉高到最高頻（`delta_time==0` 規則）。
6. **metadata 相容**：匯入檔的 `packageName` 必須相符，否則拒絕匯入（`UserDataManager.kt:100`）。

### 4.4 測試環境註記（Windows 開發機）
- 無法直接跑 Linux `fcitx5`；以 **Windows 上的 libchewing 建置** 或 **Docker/Container fcitx5** 產出 `H_ref`。
- Android 端需能讀出 `getExternalFilesDir` 下的原始 `chewing.dat`（可用 `adb` 或檔案管理）。
- 若比對位元組級內容有困難（如 zip 內路徑差異），退而採用「條目層級」比對：以 `libchewing` 的 `entries()` 列舉並正規化排序後比對詞條集合。

---

## 附註：本次分析之限制與後續
- 本文件為規格；**未改動任何程式碼**。
- `libchewing` 官方倉從 GitHub 遷至 Codeberg（`https://codeberg.org/chewing/libchewing`）。
- 若要實作「僅字典對等」的匯出／匯入，可考慮在 fcitx5-android 之外**另提供 `chewing.dat` 層級的單檔搬移**，而不綁在整包 `.zip` 上——這部分留待後續 Todo 實作時決定。
