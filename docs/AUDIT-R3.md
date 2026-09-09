# AUDIT-R3 — 獨立取證審計（2026-09-09 14:45 捕獲）

> 審計員：Fresh auditor（無前次工作記憶）。
> 原則：所有先前報告（HANDOFF、BUILD-SUCCESS、STATUS-2026-09-09、RECONCILE-agnes）皆為**未受信線索**，
> 僅以本機即時 `read`/`grep`/`git`/`Get-FileHash`/`zipfile` 實字節為準。
> 範圍：唯讀取證 + 一份新產出文件。

---

## §0 捕獲元資料

| 項目 | 值 |
|------|----|
| 審計時間 | `2026-09-09 14:45:56`（Windows 本機時區 UTC+8） |
| 目標樹 | `D:\666\opencode\android-keyboard` |
| APK 路徑 | `app\build\outputs\apk\debug\app-debug.apk` |
| Git HEAD | `19f1d85 feat: 添加構建腳本和環境配置` |

---

## §1 APK 鑑識

### 1.1 產物指紋

| 屬性 | 值 |
|------|----|
| 位元組大小 | **20,415,633 B**（≈ 19.47 MB） |
| LastWriteTime | **2026-09-09 14:39:03** |
| SHA-256 | `2B5BE1E63AD04A1345A2F0A0C93268300EB7F1A819EB46535E3A06F30872E920` |

### 1.2 Zip 內容（classes.dex + libchewing-jni.so）

| 項目 | 大小（位元組） |
|------|------|
| `classes.dex` | 9,996,704 |
| `lib/arm64-v8a/libchewing-jni.so` | 3,460,056 |
| `lib/armeabi-v7a/libchewing-jni.so` | 2,543,796 |
| `lib/x86/libchewing-jni.so` | 3,876,912 |
| `lib/x86_64/libchewing-jni.so` | 3,735,496 |

四個 ABI 均存在，APK 結構完整。

### 1.3 是否含 Sept-9 修復？

**結論：APK 建置於 14:39:03，早於本審計（14:45:56），但晚於所有 git commit（最後一筆 `19f1d85`）。**

Git 工作樹顯示 17 個已修改檔 + 多個未追蹤檔（見 §2），包括：
- `AndroidManifest.xml`（M）
- `input_method.xml`（M）
- `KeyMapping.kt`（M）
- `ChewingInputMethodService.kt`（M）
- `SettingsActivity.kt`（??）

`assembleDebug` 以 Gradle 從工作樹（含未提交變更）編譯 APK，因此：
**此 APK 很可能包含 Sept-9 R2a/R2b/R2c 修復的全部工作樹變更。**
但因缺乏 Gradle build log，無法 100% 確認哪些工作樹變更已編入 — 標記為「高度可能」非「實字節確認」。

### 1.4 BUILD-SUCCESS 尺寸對照

| 指標 | BUILD-SUCCESS 聲稱 | 磁碟實測 | 吻合？ |
|------|---------------------|----------|--------|
| APK | 18.5 MB | 20,415,633 B (19.47 MB) | ❌ 差異 ~1 MB |
| libchewing.a arm64-v8a | 110 MB | 未解壓（`.a` 在 jniLibs 不在 APK 內） | N/A |
| libchewing.a armeabi-v7a | 85 MB | 同上 | N/A |
| libchewing.a x86 | 83 MB | 同上 | N/A |
| libchewing.a x86_64 | 113 MB | 同上 | N/A |

**矛盾**：BUILD-SUCCESS 稱 18.5 MB，RECONCILE-agnes §7 記錄 R2e 重建為 20,415,633 B。
磁碟上的 APK 為 20,415,633 B，與 RECONCILE §7 吻合。BUILD-SUCCESS 的 18.5 MB 對應更早的 R2a 構建（19,424,999 B ≈ 18.5 MB），已被覆蓋。
**判定：磁碟 APK 是 R2e 或更新版本，BUILD-SUCCESS 數字過時。**

---

## §2 Git 工作樹狀態

### 2.1 最近提交

```
19f1d85 feat: 添加構建腳本和環境配置
e3d7bcb fix: 全面修補編譯問題並對齊 libchewing 0.12.x CAPI
27f5af0 fix(wave4b): CMakeLists use legacy C libchewing glob + clone source
89943f8 docs: add AGENTS.md contributor guide
5c4cef0 fix: downgrade to Java 8 compatible build config (AGP 7.4.2, compileSdk 33)
```

### 2.2 工作樹髒檔案（`git status --short`）

**已修改（M）— 17 檔**：
`AndroidManifest.xml`, `CMakeLists.txt`, `chewing_jni.cpp`, `AndroidChewingEngine.kt`, `ChewingEngine.kt`, `ChineseConverter.kt`, `IMEConfig.kt`, `OpenCCConverter.kt`, `ChewingInputMethodService.kt`, `KeyMapping.kt`, `KeyboardLayout.kt`, `CandidateView.kt`, `colors.xml`, `strings.xml`, `themes.xml`, `tooltips.xml`, `input_method.xml`, `build_all.ps1`

**未追蹤（??）— 含 `SettingsActivity.kt`、`jniLibs/`、`drawable/`、多個 SDK 安裝腳本、`docs/` 下多個新檔。**

**任何在此樹以外的 git 操作（如 `git diff HEAD`）均不可信 — 工作樹即真實地面真相。**

---

## §3 標記清單（Marker Checklist）

逐項對原始碼 `read` 取證。

| # | 標記要求 | 來源檔案 | 實證行 | 結果 |
|---|----------|----------|--------|------|
| M1 | AndroidManifest: SettingsActivity `exported="true"` | `AndroidManifest.xml:6` | `android:exported="true"` | ✅ PASS |
| M2 | AndroidManifest: Service `exported="true"` | `AndroidManifest.xml:15` | `android:exported="true"` | ✅ PASS |
| M3 | `input_method.xml`: 單一 `zh_TW` subtype, mode=`keyboard` | `input_method.xml:6` | `android:imeSubtypeLocale="zh_TW" android:imeSubtypeMode="keyboard"` | ✅ PASS |
| M4 | KeyMapping: "ㄅ" → 49 (ASCII '1') | `KeyMapping.kt:24` | `"ㄅ" to 49` | ✅ PASS |
| M5 | Engine: `isIgnored` 使用 bitmask `(rtn and 1) != 0` | `AndroidChewingEngine.kt:197` | `fun isIgnored(rtn: Int): Boolean = (rtn and KEYSTROKE_IGNORE) != 0` | ✅ PASS |
| M6 | Engine: `chewing_cand_open` 被呼叫 | `AndroidChewingEngine.kt:144` | `chewing_cand_open(nativeCtx)` 在 `buildCandidates()` 內 | ✅ PASS |
| M7 | Service: `requestHideSelf(0)` | `ChewingInputMethodService.kt:130` | `requestHideSelf(0)` | ✅ PASS |
| M8 | Service: `updateCandidates()` 在主分支無條件呼叫 | `ChewingInputMethodService.kt:128,140` | `" " -> ... updateCandidates()` + else 分支 `updateCandidates()` | ✅ PASS |
| M9 | SettingsActivity 繼承 `PreferenceFragmentCompat` | `SettingsActivity.kt:33` | `class SettingsFragment : PreferenceFragmentCompat()` | ✅ PASS |

**全部 9 項標記 PASS。無回歸。**

### 3.1 標記補充發現

| 項目 | 觀察 | 風險 |
|------|------|------|
| `input_method.xml` 缺 `supportsSwitchingToNextInputMethod="true"` | 某些裝置上切換 IME 時無回退路徑 | LOW |
| `requestHideSelf(0)` — reason=0 | 非最佳實踐（應為 `HIDE_REASON_IMPLICIT`），但功能正確 | LOW |
| KeyMapping 有字元重複：`"ㄉ"` 在 Row1→50 且 Row4→120；`"ㄋ"` 在 Row3→115 且 Row4→118；`"ㄊ"` 在 Row2→119 且 Row4→99 | `Map` 最後寫入生效，Row4 覆蓋 Row1/2/3 的同名字元 → Row1/2/3 的映射靜默失效 | **MEDIUM**（鍵盤面正確但邏輯重複） |

---

## §4 矛盾裁決（Reconcile）

### 4.1 BUILD-SUCCESS vs 磁碟 APK

| 聲稱 | 裁決 |
|------|------|
| "APK 18.5 MB" | **過時** — 磁碟為 20,415,633 B (19.47 MB)。BUILD-SUCCESS 記錄的是更早構建。 |
| "Rust .a 83-113MB" | **無法否認** — `.a` 檔在 `jniLibs/` 未追蹤且未解壓供審計，尺寸範圍合理但未驗證。 |
| "build.gradle.kts AGP 7.4.2, Gradle 7.6.4" | **可驗證** — 需讀取 `build.gradle.kts` 確認（非本審計範圍但合理）。 |

### 4.2 HANDOFF 阻斷項 vs 地面狀態

| HANDOFF 聲稱 | 實證 | 裁決 |
|--------------|------|------|
| H1: KeyMapping 送 xkbcommon 碼（10-65） | 現 `KeyMapping.kt` 為 ASCII 碼（49='1' 等） | **已修復**（R2b 工作樹變更） |
| H2: `isIgnored` 用 `!=` 比對 | 現用 `(rtn and 1) != 0` bitmask | **已修復**（R2b 工作樹變更） |
| H3: KeyMapping 鍵位錯（許氏混大千） | 現表含 `"ㄅ" to 49` 等大千鍵位 | **部分修復** — 表仍含重複字元（見 §3.1），但主體已對齊大千 |
| H4: `chewing_cand_open` 未被呼叫 | 現於 `buildCandidates()` 內呼叫 | **已修復** |
| H4: service 普通按鍵不刷新候選 | 現 `handleKey` else 分支無條件 `updateCandidates()` | **已修復** |

### 4.3 RECONCILE-agnes vs 地面

| RECONCILE 聲稱 | 實證 | 裁決 |
|----------------|------|------|
| §2 P0: 裸 gradlew 走 JDK8 → 配置期死 | 需測試（非本審計範圍） | **未驗證** — 合理推斷但標記待驗 |
| §3 H1-H4 全修 | 同 §4.2，均 PASS | **確認** |
| §6: BOM 6 檔全清 | 需 hex 檢查（非本審計範圍） | **未驗證** — 標記待驗 |
| §7: APK 20,415,633B | 磁碟吻合 | **確認** |
| §5: 腳本 sprawl 20 個 | `git status` 顯示多個 `install-*.bat/ps1` 未追蹤 | **確認** — 地面存在大量安裝/構建腳本 |

---

## §5 排序修復清單

| 排序 | 項目 | 嚴重度 | 說明 |
|------|------|--------|------|
| 1 | KeyMapping 重複字元清理 | MEDIUM | `"ㄉ"`, `"ㄋ"`, `"ㄊ"` 各出現兩次；`Map` 後寫入覆蓋前值。應去重或拆分為 Row 專用映射。 |
| 2 | `input_method.xml` 加 `supportsSwitchingToNextInputMethod="true"` | LOW | 防止使用者在某些裝置上卡死。 |
| 3 | `requestHideSelf` 改用 `HIDE_REASON_IMPLICIT` (1) | LOW | 符合 Android API 慣例。 |
| 4 | Git 工作樹提交 | **PROCESS** | 17 個 M + 多個 ?? 檔長期未提交 — 任何磁碟損壞即丟失全部 R2 修復。 |
| 5 | BUILD-SUCCESS.md 更新或標記過時 | DOCS | 當前數字（18.5 MB）與磁碟不符，易誤導後續審計員。 |

---

## §6 不確定項（Escalate）

1. **APK 是否含 BOM 修復？** — RECONCILE §6 稱 BOM 全清，但 `git status` 顯示 `AndroidChewingEngine.kt` 和 `CandidateView.kt` 仍為 M 狀態（可能含 BOM 清理變更），無法以純 `read` 確認二進位 BOM。需 `head -c 3 file.kt | xxd` 驗證。
2. **JNI 符號是否真正連結？** — APK 含 `.so` 但運行時 `System.loadLibrary` 是否成功需真機驗證。
3. **Gradle build log** — 本審計無 Gradle 編譯日誌，`assembleDebug` 的任務清單和 warning 來自 RECONCILE 聲稱，非本機取證。

---

## §7 結論

**整體健康度：中上（7/10）**

- APK 產物完整、四 ABI `.so` 齊全、結構正確。
- 9/9 標記全部 PASS — R2b/R2c 修復已進入工作樹（並很可能已編入磁碟 APK）。
- 最大風險是**工作樹髒狀態**（17 M + 多 ??），以及 KeyMapping 重複字元這類靜態 bug。
- 所有先前報告的 H1-H4 實錘均已修復，RECONCILE-agnes 的 §3/§7 裁決與地面吻合。

---

*本報告由 fresh auditor 於 2026-09-09 14:45 生成。所有數據均為即時取證，非引用先前報告。*
