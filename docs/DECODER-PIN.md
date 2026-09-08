# DECODER-PIN — chewing 解碼器釘選規格（Todo 5）

> 本文為 android-keyboard mirror 計畫 **Todo 5** 的檢驗規格：釘選 fcitx5-chewing
> 解碼器上游單元、鎖定鍵盤配置設定鍵與預設值、並定義 `chewing_Reset` 的
> **#836 不可回退規則** 與未來測試計畫。所有 SHA 皆為「ⓘ 擷取當下」的唯讀快照。

---

## 1. 上游單元與釘選版本對照表

| 單元 | 路徑（上游） | 釘選 | 用途 |
|---|---|---|---|
| fcitx5-android HEAD | `HEAD`（master） | `6998502528cd26efb6079556da92003638e4179c` | 鏡像底座的頂層快照 |
| **fcitx5-chewing** | `plugin/chewing/src/main/cpp/fcitx5-chewing`（gitlink） | `07eddb16961b18765e67cec538708b6964baa57c` | 注音解碼引擎（C++） |
| prebuilt libchewing | `lib/fcitx5/src/main/cpp/prebuilt`（gitlink, shallow） | `a6a8fa4-dirty`（git commit `a6a8fa4`，dirty build） | 預編譯靜態庫 `libchewing_capi.a` |

### 1.1 精確檔案路徑（上游鉚釘）

- **fcitx5-chewing submodule 釘選（gitlink）**
  `plugin/chewing/src/main/cpp/fcitx5-chewing` → commit `07eddb16…`
- **chewing 外掛 build 描述**
  `plugin/chewing/build.gradle.kts`（cmake target：`chewing`）
- **chewing 原生 CMake（引用 prebuilt 與 submodule）**
  `plugin/chewing/src/main/cpp/CMakeLists.txt`
  - 匯入 static lib：`"${PREBUILT_DIR}/libchewing/${ANDROID_ABI}/lib/libchewing_capi.a"`
  - include：`"${PREBUILT_DIR}/libchewing/${ANDROID_ABI}/include/chewing"`
  - `add_subdirectory(fcitx5-chewing)`
- **prebuilt libchewing 版本標記**
  `libchewing/…/lib/cmake/Chewing/ChewingConfigVersion.cmake`
  → `set(PACKAGE_VERSION "a6a8fa4-dirty")`

---

## 2. 鍵盤配置設定鍵與預設值（fcitx5-chewing 側）

設定鍵位於 fcitx5-chewing 的 **`src/eim.h`**（`FCITX_CONFIGURATION(ChewingConfig, …)`）
與 **`src/eim.cpp`**（套用處）。

### 2.1 鍵盤配置列舉（`ChewingLayout`）

`src/eim.h:60-77`，`builtin_keymaps[]` 對應 `src/eim.h:79-85`：

| 列舉值 | 索引 | libchewing keymap | 顯示名稱 |
|---|---|---|---|
| `Default` | 0 | `KB_DEFAULT` | Default Keyboard（標準大千/Dachen） |
| `Hsu` | 1 | `KB_HSU` | Hsu's Keyboard |
| `IBM` | 2 | `KB_IBM` | IBM Keyboard |
| `GinYieh` | 3 | `KB_GIN_YIEH` | Gin-Yieh Keyboard |
| `ETen` | 4 | `KB_ET` | ETen Keyboard |
| `ETen26` | 5 | `KB_ET26` | ETen26 Keyboard |
| `Dvorak` | 6 | `KB_DVORAK` | Dvorak Keyboard |
| `DvorakHsu` | 7 | `KB_DVORAK_HSU` | Dvorak Keyboard with Hsu's support |
| `DACHEN_CP26` | 8 | `KB_DACHEN_CP26` | DACHEN_CP26 Keyboard |
| `HanYuPinYin` | 9 | `KB_HANYU_PINYIN` | Han-Yu PinYin Keyboard |
| `ThlPinYin` | 10 | `KB_THL_PINYIN` | THL PinYin Keyboard |
| `Mps2PinYin` | 11 | `KB_MPS2_PINYIN` | MPS2 PinYin Keyboard |
| `Carpalx` | 12 | `KB_CARPALX` | Carpalx Keyboard |
| `Colemak` | 13 | `KB_COLEMAK` | Colemak Keyboard |
| `ColemakDH_ANSI` | 14 | `KB_COLEMAK_DH_ANSI` | Colemak-DH ANSI Keyboard |
| `ColemakDH_ORTH` | 15 | `KB_COLEMAK_DH_ORTH` | Colemak-DH Orth Keyboard |

### 2.2 設定鍵與預設值

- **`Layout`**（`ChewingLayout` enum；`eim.h:177-178`）
  - 鍵名：`"Layout"`
  - **預設：`ChewingLayout::Default`（= `KB_DEFAULT`，即「標準大千/Dachen」預設鍵盤）**
  - 顯示鍵：`_("Keyboard Layout")`
  - 套用：`eim.cpp:321-325`
    ```cpp
    chewing_set_KBType(ctx,
        chewing_KBStr2Num(builtin_keymaps[static_cast<int>(*config_.Layout)]));
    ```

> ⚠️ 名詞澄清：`DACHEN_CP26`（大千 26 鍵直結）是**另一種**配置；「Dachen 預設」
> 指的是 `ChewingLayout::Default` → `KB_DEFAULT`，即 libchewing 標準注音鍵盤
> （standard 大千）。本鏡像預設值 = `Default`，非 `DACHEN_CP26`。

---

## 3. `chewing_Reset` 呼叫點與 #836 不可回退規則

### 3.1 呼叫點（fcitx5-chewing `src/eim.cpp`）

| 行號 | 函式 | 情境 |
|---|---|---|
| `354` | `ChewingEngine::doReset(InputContextEvent&)` | **唯一**直接呼叫 `chewing_Reset(ctx)` 的位置 |
| `344-347` | `ChewingEngine::reset(InputMethodEntry&, InputContextEvent&)` | 呼叫 `doReset(event)`；由引擎介面觸發 |
| `372` | `activate(...)` | input context 切換時 `doReset(event)` |
| `687` | `deactivate(...)` | 切離（含 `CommitDefault` 後）`doReset(event)` |
| `503` / `517` | keyEvent 處理（Backspace / Delete 清空後） | `reset(entry, keyEvent)` |

`doReset` 內容（`eim.cpp:349-356`）：
```cpp
void ChewingEngine::doReset(InputContextEvent &event) {
    ChewingContext *ctx = context_.get();
    chewing_cand_close(ctx);
    chewing_clean_preedit_buf(ctx);
    chewing_clean_bopomofo_buf(ctx);
    chewing_Reset(ctx);
    updateUI(event.inputContext());
}
```

### 3.2 歷史與 #836 成因（保留脈絡）

- `cfa4143`（2013）：「layout seems to be need reload after reset … Closes Issue #5」
  → 指出每次 reset 後 **layout 需要重新載入**。
- `c8b4dcd`（2022）：「Fix chewing layout is not applied before the first reset」
  → 修正首次 reset 前 layout 未套用。
- 上游 `fcitx5-android` issue **#836** 的背景風險：`chewing_Reset` 會清除
  libchewing 內部狀態（cows/注音緩衝/tone），**若在設定項目存檔流程後被誤觸發**，
  可能導致使用者已儲存之設定（特別是 `Layout`）在保留(retention)上出錯，
  或與「reset 後 layout 需重載」交互而產生回退。

### 3.3 不可回退規則（NO-FALLBACK RULE）

本鏡像對 `chewing_Reset` 的處理：

1. **不得**為了解決 #836 而在 `doReset` 之外新增「回退到 `ChewingLayout::Default`」
   的兜底邏輯（例如：偵測到 layout 異常就強制 `KB_DEFAULT`）。這會抹掉使用者已選的
   Hsu / ETen26 / Dvorak 等配置。
2. layout 的「重載」責任在**引擎端於每次 reset 後重新套用 `config_.Layout`**
   （見 §2.2 套用處），而非「回退到預設」。
3. 任何對 `chewing_Reset` 或 layout 應用的改動，都必須維持：
   - 不改變已儲存 `Layout`/`SelectionKey` 等設定鍵的 persistence；
   - 不隱式 fallback；有異常時明確回報而非靜默回退。

### 3.4 未來測試計畫：Reset 後之設定保留（settings-retention after Reset）

驗證「呼叫 `chewing_Reset`**不**抹除使用者設定、且 `Layout` 於 reset 後正確重載」：

1. **設定寫入**：設 `Layout = Hsu`、`SelectionKey = CSK_Digit`，存檔。
2. **觸發 reset**：
   - 切換 input method（`activate` 換 context → `doReset`）；
   - Backspace/Delete 清空緩衝後（`reset`）；
   - 切離 input method（`deactivate` → `doReset`）。
3. **斷言**：
   - `config_.Layout` 於 reset 前後皆為 `Hsu`（未回退成 `Default`）；
   - 重新 activate 後實際 `chewing_set_KBType` 套用的是 `KB_HSU`（非 `KB_DEFAULT`）；
   - `SelectionKey`/`PageSize` 等其餘設定鍵維持原值（retention 通過）。
4. **負向**：故意把 `Layout` 設成引擎 `supportedLayouts()` 之外的索引，確認
   不會因此觸發 fallback 到 `Default`，而是維持既有行為或顯式報錯。

**自動化形式**：優先以 fcitx5-chewing 既有 `test/testchewing.cpp` 的
`config.setValueByPath("Layout", …)` 模式擴充一個「設定保留」測試個案；否則於
android-keyboard 階段用原生單元測試覆蓋上述步驟 1–4。

---

## 4. 附註

- 所有路徑皆為「上游 fcitx5-android `6998502528…`」快照下的路徑；本機 mirror 尚未
  實抓 submodule 內容（僅記錄 gitlink 釘選）。
- prebuilt libchewing 版本 `a6a8fa4-dirty` 依 `ChewingConfigVersion.cmake` 擷取。
- 本文由 Todo 5 檢驗流程產生，截至 2026-09-08 與上游 HEAD `6998502528…` 一致。
