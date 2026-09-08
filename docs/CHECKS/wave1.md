# Wave 1 交付檢驗清單（android-keyboard）

> **Todo 9** · tier LIGHT · docs-only 收尾稽核（closing audit of Wave 1）
> 檢驗對象：Wave 1 產出的四份規格文件（
> `docs/DECODER-PIN.md`、`docs/OPENCC-WIRING.md`、`docs/USERDICT.md`、`docs/AAR-FALLBACK.md`）。
> 本檔為**唯讀檢驗**；僅新增本檔，不修改其他任何檔案。
> 檢驗日期：2026-09-08。

---

## 0. 檢驗範圍與待測四檔

| # | 檔案（相對 docs/） | 狀態 | 實測行數 | 任務聲稱行數 | 判定 |
|---|---|---|---|---|---|
| 1 | `DECODER-PIN.md` | 存在 | 155 | 155 | ✅ PASS |
| 2 | `OPENCC-WIRING.md` | 存在 | 380 | 380 | ✅ PASS |
| 3 | `USERDICT.md` | 存在 | 155 | 155 | ✅ PASS |
| 4 | `AAR-FALLBACK.md` | 存在 | 261 | 261 | ✅ PASS |

> 四檔皆存在，且實測行數與任務聲稱一致（檔案結尾自帶 `total N lines` 標記，
> 與 `Get-Content` 行數一致）。

---

## 1. 存在性（Presence）

| 檢查項 | 結果 | 證據（file:line） |
|---|---|---|
| `docs/DECODER-PIN.md` 存在 | ✅ PASS | 存在（155 行） |
| `docs/OPENCC-WIRING.md` 存在 | ✅ PASS | 存在（380 行） |
| `docs/USERDICT.md` 存在 | ✅ PASS | 存在（155 行） |
| `docs/AAR-FALLBACK.md` 存在 | ✅ PASS | 存在（261 行） |

**結論：1 全部 PASS。**

---

## 2. 驗收閘（Acceptance Gates）

### 2.1 三鍵盤佈局鍵表 + Dachen 預設（DECODER-PIN §2）

| 檢查項 | 結果 | 證據（file:line） |
|---|---|---|
| 鍵盤配置鍵表存在（ChewingLayout 列舉 + 索引 + libchewing keymap + 顯示名） | ✅ PASS | DECODER-PIN.md:43-60（§2.1 表格，`Default`…`ColemakDH_ORTH`） |
| Dachen 預設：`Layout` 鍵預設 = `ChewingLayout::Default`（= `KB_DEFAULT`，標準大千/Dachen） | ✅ PASS | DECODER-PIN.md:64-66（§2.2「**預設：`ChewingLayout::Default`（= `KB_DEFAULT`，即「標準大千/Dachen」預設鍵盤）**」）；DECODER-PIN.md:45 |
| 名詞澄清（Dachen 預設 ≠ DACHEN_CP26） | ✅ PASS（附註） | DECODER-PIN.md:74-76（§2 警告框） |

> ⓘ INFO（誠實備註）：DECODER-PIN §2.1 的鍵盤配置表實為 **16 個** `ChewingLayout` 列舉
> （`Default`…`ColemakDH_ORTH`，DECODER-PIN.md:43-60），非「僅 3 個」。任務描述中的
> 「3-layout keys table」為簡稱；本表涵蓋多種佈局，其「Dachen 預設」驗收實質通過。

**結論：2.1 PASS。**

### 2.2 簡繁 profile：s2tw/tw2s 預設 + s2t/t2s fallback + 14-profile 列表（OPENCC-WIRING）

| 檢查項 | 結果 | 證據（file:line） |
|---|---|---|
| s2t 預設台灣 profile `s2tw.json`，fallback `s2t.json` | ✅ PASS | OPENCC-WIRING.md:101-109（`preferredS2TProfile = "s2tw.json"`；fallback `s2t.json`） |
| t2s 預設台灣 profile `tw2s.json`，fallback `t2s.json` | ✅ PASS | OPENCC-WIRING.md:111-113（`preferredT2SProfile = "tw2s.json"`；fallback `t2s.json`） |
| 內建可用 profile 列表（儲存於 §3.4 表格） | ✅ PASS | OPENCC-WIRING.md:230-242（§3.4 表格列舉 `s2tw`/`tw2s`/`s2t`/`t2s`/`s2twp`/`tw2sp`/`s2hk`/`hk2s`/`t2hk`/`t2tw`/`tw2t`/`t2jp`/`jp2t`） |
| 14-profile i18n 項宣告 | ✅ PASS（§4.3 明確記載） | OPENCC-WIRING.md:285（`NC_("OpenCC Profile", …)` 14 items） |
| 預設台灣 s2tw/tw2s 之來源（上游 updateConfig 一致） | ✅ PASS | OPENCC-WIRING.md:98-115、244 |

> ⓘ INFO（誠實備註）：§4.3 於 OPENCC-WIRING.md:285 記載 i18n profile 為 **14 items**；
> 而 §3.4 的可選 profile 表格（OPENCC-WIRING.md:230-242）具體列出 **13 個** `.json` 檔。
> 兩者計數略異（14 為 i18n 宣告項數、13 為 §3.4 表格列出之檔）。此為既有文件內「計數表述
> 差異」，非驗收失敗；本清單據實記錄，供後續 Todo 統一 profile 數量表述時參考。

**結論：2.2 PASS（含 INFO 記錄）。**

### 2.3 使用者字典：字典持久化規格 + 6 點 hash 計畫 + Guileless#11 防呆（USERDICT §4）

| 檢查項 | 結果 | 證據（file:line） |
|---|---|---|
| 字典持久化規格（路徑/檔案/載體對照） | ✅ PASS | USERDICT.md:93-110（§3 對照表：`chewing.dat`、`CHEWING_USER_PATH`、資料根、`.zip` 載體） |
| 6 點 hash 相符驗證計畫 | ✅ PASS | USERDICT.md:137-143（§4.3「明確驗證點」共 6 點：①Round-trip ②跨平台 ③排除清單 ④單字防呆 ⑤新詞優先 ⑥metadata 相容） |
| Guileless #11 單碼點回歸守衛（單字防呆） | ✅ PASS | USERDICT.md:141（§4.3 點 4「對應 prior lesson：`Guileless #11` 單碼點回歸守衛」）；另見點 4 正文 USERDICT.md:137-143 |
| 匯出—清除—匯入三步驟 hash 對比（H_ref/H_A/H_A'） | ✅ PASS | USERDICT.md:128-135（§4.2） |

**結論：2.3 PASS。**

### 2.4 AAR fallback：路由 + 版本 + NOT-RUN 誠實 + GPL-3.0 旗標（AAR-FALLBACK）

| 檢查項 | 結果 | 證據（file:line） |
|---|---|---|
| AAR 建置路由與觸發條件 | ✅ PASS | AAR-FALLBACK.md:10-20（§1 觸發條件）、§4 建置指令（:91-110） |
| 版本規格（Gradle/AGP/Kotlin/SDK/NDK） | ✅ PASS | AAR-FALLBACK.md:50-68（§3.1 Gradle 8.11.1 / AGP 8.9.0 / Kotlin 1.9.0 / compileSdk 35 / minSdk 23 / buildTools 35.0.0 / Java 17；§3.2 NDK 28.1.13356709）；MSRV 保守 1.88（§3.4,:87） |
| NOT-RUN 誠實標記（本機未建置） | ✅ PASS | AAR-FALLBACK.md:168-178（§8 本機建置阻斷器：Java 1.8.0_401 BLOCKED、CMake 未裝、SDK/NDK 未設；「標記為 NOT-RUN 的步驟已明確註記」:178） |
| GPL-3.0 授權旗標 | ✅ PASS | AAR-FALLBACK.md:206-211（§10：`libchewingAndroidAppModule` 為 GPL-3.0、見 `GPL-3.0.txt`；與底座 LGPL-2.1 之張力提醒） |

**結論：2.4 PASS。**

---

## 3. Pin 一致性（Pin Consistency）

### 3.1 各 pin 於四檔中出現處之核對

| pin | 應出現文件 | 實測值 | 判定 |
|---|---|---|---|
| 上游 HEAD `6998502528` | DECODER-PIN、OPENCC-WIRING、USERDICT | DECODER-PIN:13,14,15,152,155 = `6998502528cd26efb6079556da92003638e4179c`；OPENCC-WIRING:4,15 = 同全值；USERDICT:6 = 短寫 `6998502` | ✅ PASS（同值，USERDICT 為短寫，後綴一致） |
| fcitx5-chewing `07eddb16` | DECODER-PIN（userdict 有差異，見 §3.2） | DECODER-PIN:14 = `07eddb16961b18765e67cec538708b6964baa57c`、:20 = `07eddb16…` | ✅ PASS（DECODER-PIN 內一致）；⚠️ 對 USERDICT 需調和（見 §3.2） |
| fcitx5-chinese-addons `0d3fd040` | OPENCC-WIRING | OPENCC-WIRING:4,14 = `0d3fd0408d03abc63cdb700559f85a387b98735f` | ✅ PASS |
| libchewing prebuilt `a6a8fa4-dirty` | DECODER-PIN | DECODER-PIN:15 = `a6a8fa4-dirty`、:30（`ChewingConfigVersion.cmake` → `PACKAGE_VERSION "a6a8fa4-dirty"`） | ✅ PASS |
| OpenCC `1.2.0` | OPENCC-WIRING | OPENCC-WIRING:18、:158（`PACKAGE_VERSION "1.2.0"`） | ✅ PASS（凡提及處同值） |

> ⓘ AAR-FALLBACK.md 並非上述 pin 的出現位置：其記錄的是**另一獨立上游 repo**
> `libchewingAndroidAppModule`（AAR-FALLBACK.md:5，repo SHA `a3511a8...`，Release 0.9.1.8）。
> 此為不同 repo 的快照，不在本波 pin 對照範圍內，與上述四項 pin 無衝突。

### 3.2 ⚠️ 需調和事項（INFO + 指導）：USERDICT 檢視 fcitx5-chewing@60096e8

USERDICT.md **沒有**使用本波釘選的 fcitx5-chewing `07eddb16`，而是記錄了：

- USERDICT.md:7 — `fcitx5-chewing @ 60096e8`（檔案標註「上游參考來源（HEAD）」）

調和判定：

| 項目 | 內容 |
|---|---|
| 性質 | **INFO（非 FAIL）** |
| 為何非 FAIL | USERDICT 為「與 Linux 版行為對等」的**分析規格**，其 `60096e8` 是撰寫當下瀏覽的**上游 master 較新快照**；它不會覆寫或宣告本波 pin。pin 一致性規則要求「凡 pin 出現處須一致」，而 USERDICT 從未聲稱其 fcitx5-chewing 即為釘選值——故不構成 pin 衝突。 |
| 差異實況 | `60096e8`（USERDICT 檢視）vs `07eddb16`（DECODER-PIN 釘選）＝上游 master 較新 HEAD vs 本波鎖定 gitlink。 |
| 指導（guidance） | ① **規格內容有效**：USERDICT 的持久化／hash／學習模型規格以 libchewing 核心語意為準，不因 fcitx5-chewing commit 新舊而失效。② **落地（landing）必須用 pin**：任何實際 `fetch`/submodule/建置作業，一律以 DECODER-PIN.md:14 之釘選 `07eddb16961b18765e67cec538708b6964baa57c` 為準，**不得**以 USERDICT 記錄的 `60096e8` 落地。③ 建議於 USERDICT 加註「此為檢視當下 master 快照，落地釘選見 DECODER-PIN §1」以消除歧義（本波僅記錄建議，不改檔）。 |

**結論：3 PASS（含 §3.2 之 INFO 調和記錄與落地指導）。**

---

## 4. 連結（Links）

| 檢查項 | 結果 | 證據 |
|---|---|---|
| 四檔內所有 Markdown 相對連結 `[text](url)` 可解析 | ✅ PASS（vacuous） | 對四檔執行 markdown-link 掃描（regex `\[[^\]]+\]\([^\)]+\)`）皆為 **0 筆**：DECODER-PIN=0、OPENCC-WIRING=0、USERDICT=0、AAR-FALLBACK=0 |
| 既有「引用式」路徑（backtick 內文）指向之文件是否存在 | ✅ PASS（附註） | OPENCC-WIRING.md:351 引 `docs/ARCHITECTURE.md`、:352 引 `docs/UPSTREAM.md`；AAR-FALLBACK.md:224,235 引 `docs/UPSTREAM.md`，皆為 backtick 內文（非可點選連結）。實測：`docs/ARCHITECTURE.md` 存在、`docs/UPSTREAM.md` 存在、`android-keyboard/README.md` 存在。 |

> 四檔**沒有任何**真正可點選的相對 Markdown 連結，故「所有相對連結皆可解析」為
> **空洞通過（vacuous PASS）**；同時以內文引用的文件亦實際存在，無斷鏈。

**結論：4 PASS。**

---

## 5. 範圍（Scope）

| 檢查項 | 結果 | 證據 |
|---|---|---|
| 本作業**僅新增** `docs/CHECKS/wave1.md` | ✅ PASS | 檢驗前基準樹（`D:\666\opencode\android-keyboard`）僅含 `docs/CHECKS/wave0.md`，**無** `wave1.md`；本作業唯一寫入 = `docs/CHECKS/wave1.md` |
| 未修改其他檔案 | ✅ PASS | 本作業對四份待測檔僅做唯讀讀取；未寫入 README/LICENSE/NOTICE/ARCHITECTURE/USERDICT/OPENCC-WIRING/DECODER-PIN/AAR-FALLBACK/ROADMAP/AUDIT/RELEASE/UPSTREAM/OSS-NOTES/KEYMAP/IOS-NOTES 任一檔案 |
| 父層 `D:\666\opencode` 列示未受本作業影響 | ✅ PASS | 檢驗紀錄父層既存項目：`.codegraph/`、`.omo/`、`addy-skills/`、`android-keyboard/`、`kiss-translator/`、`videos/`、`addy-skills.tar`、`AGENTS.md`、多份 `kiss-translator-*.md`、`README.md`、`ultrawork-hussite-remotion.md`；本作業不在此層新增/刪除任何項目 |
| 未 commit / push / clone | ✅ PASS | 本目錄非 git repo（AGENTS.md 明示「No git repo」）；本作業無任何 git 操作、無任何 clone |

**結論：5 PASS。**

---

## 6. Manual-QA（逐項 PASS/FAIL 摘要）

| Gate | 判定 | 關鍵證據（file:line） |
|---|---|---|
| (1) Presence | ✅ PASS | 四檔存在且行數 155/380/155/261 相符 |
| (2a) Dachen 預設鍵表 | ✅ PASS | DECODER-PIN.md:43-60、64-66 |
| (2b) s2tw/tw2s + s2t/t2s fallback + profile 列表 | ✅ PASS | OPENCC-WIRING.md:101-113、230-242、285 |
| (2c) 字典持久化 + 6 點 hash + Guileless#11 | ✅ PASS | USERDICT.md:93-110、137-143（6 點）、:141 |
| (2d) AAR 路由 + 版本 + NOT-RUN + GPL-3.0 | ✅ PASS | AAR-FALLBACK.md:50-68、168-178、206-211 |
| (3) Pin 一致（含 60096e8 調和） | ✅ PASS（含 INFO/指導） | §3.1 表；§3.2 |
| (4) 相對連結解析 | ✅ PASS（vacuous） | §4（0 筆 markdown link；backtick 引用文件實存） |
| (5) Scope（僅新增 wave1.md） | ✅ PASS | §5 |

**全數 PASS，無任何 gate 為 FAIL；不需回報 needs-fix。**

---

## 7. Adversarial Probes

| Probe | 判定 | 說明／證據 |
|---|---|---|
| **stale_state** | N/A | 本作業為**靜態快照稽核**：不 fetch、不 clone、不驗證遠端 git 狀態；四份 pin 皆以文件內既有靜態快照為準，與實際遠端 HEAD 是否已前進無關（OPENCC-WIRING.md:21 亦自帶 staleness 提醒）。 |
| **dirty_worktree** | PROBE → ✅ 通過 | 寫入 `wave1.md` 前後比對基準樹：寫入前無 `wave1.md`、寫入後僅多 `docs/CHECKS/wave1.md`；四份待測檔與其餘檔案內容未經修改（見 §5 樹狀對照與父層列示）。 |
| **misleading_success_output** | PROBE → ✅ 通過 | 所有 PASS 皆附 file:line 引用與實測行數（§0、§6）；誠實記錄 3 處 INFO：①DECODER-PIN 鍵表為 16 佈局非 3（§2.1）②§3.4 列出 13 檔 vs §4.3 宣告 14 items（§2.2）③USERDICT `60096e8` vs pin `07eddb16` 之調和（§3.2）。無虛構 PASS、無省略已知差異。 |

---

## 8. 結論（DoneClaim 摘要）

- **Verification 結果**：`docs/CHECKS/wave1.md` 已建立並回讀（本檔即為其內容）；Manual-QA 五個 gate（presence / acceptance / pin-consistency / links / scope）**全部 PASS**，含 3 處誠實 INFO 記錄。
- **無 FAIL**：不需回報 needs-fix，此為綠色收尾（wave1 closing audit）。
- **Adversarial**：stale_state=N/A（快照稽核）；dirty_worktree / misleading_success_output 皆通過（附以上證據）。
- **交付**：唯一新增檔案 `docs/CHECKS/wave1.md`；無其他修改、無 commit、無 push、無 clone、無程式碼。
