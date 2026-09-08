# Wave 0 骨架審計（CHECKS / wave0）

> 檔名：`docs/CHECKS/wave0.md`
> 審計日期：2026-09-08
> 執行模式：Todo 4，tier **LIGHT**，docs-only 快照審計
> 審計對象：`D:\666\opencode\android-keyboard`（Wave 0 文件骨架）
> Scope：本審計之唯二改動為 **(a)** 新增本檔、(b) NOTICE L1 錯字修正；其餘任何文件內容未動、未 commit、未 push、未 clone。

---

## 0. 方法與基線

1. **Baseline first**：先以 `Get-ChildItem -Recurse` 列出目標樹（見 §B1 附錄），確認 12 個必需檔案全部存在，再逐檔讀取。
2. **連結解析**：`grep` 抓取 `](...)` markdown 連結，逐一以 `Test-Path -LiteralPath` 驗證相對路徑目標存在（絕對 URL 視為外鏈，僅驗格式）。
3. **授權比對**：NOTICE 逐項細讀 7 項；LICENSE 前 10 行與 LGPL-2.1 canonical header 逐字比對。
4. **一致性比對**：以 `grep` 全樹掃描三個釘選識別碼（上游 SHA / libchewing `a6a8fa4-dirty` / OpenCC `1.2.0`），確認每次出現之字串 100% 相同。
5. **範圍檢查**：審計前後比對目標樹 diff + 列出父目錄 `D:\666\opencode`。

---

## 1. 結構 — 全部存在（S-1 … S-12）

| # | 指定檔案 | 存在 | 證據 |
|---|----------|------|------|
| S-1 | `README.md` | ✅ PASS | 讀取成功，57 行 |
| S-2 | `LICENSE` | ✅ PASS | 存在，26,783 bytes |
| S-3 | `NOTICE` | ✅ PASS | 存在，5,068 bytes |
| S-4 | `docs/ARCHITECTURE.md` | ✅ PASS | 存在，1,988 bytes |
| S-5 | `docs/KEYMAP.md` | ✅ PASS | 存在，1,642 bytes |
| S-6 | `docs/IOS-NOTES.md` | ✅ PASS | 存在，1,143 bytes |
| S-7 | `docs/UPSTREAM.md` | ✅ PASS | 存在，9,596 bytes |
| S-8 | `docs/ROADMAP.md` | ✅ PASS | 存在，8,544 bytes |
| S-9 | `docs/AUDIT.md` | ✅ PASS | 存在，5,793 bytes |
| S-10 | `docs/OSS-NOTES.md` | ✅ PASS | 存在，2,983 bytes |
| S-11 | `docs/RELEASE.md` | ✅ PASS | 存在，6,008 bytes |
| S-12 | `docs/diagram/architecture.svg` | ✅ PASS | 存在，6,319 bytes（SVG 具 `<svg>` / `<text>` 內容，非空檔） |

**S-13 額外**：本審計新增 `docs/CHECKS/wave0.md`（本檔）。

---

## 2. 連結 — 全部解析成功（L-1 … L-4）

> 掃描全樹 `](...)` 型 markdown 連結，共 4 筆（README 3 筆、ARCHITECTURE 1 筆）。

| # | 位置（file:line） | 連結目標 | 型別 | 結果 | 證據 |
|---|--------------------|----------|------|------|------|
| L-1 | `README.md:3` | `https://github.com/fcitx5-android/fcitx5-android` | 絕對 URL | ✅ PASS | 外鏈格式完好 |
| L-2 | `README.md:5` | `docs/ARCHITECTURE.md` | 相對 | ✅ PASS | `Test-Path` → True |
| L-3 | `README.md:11` | `https://github.com/fcitx5-android/fcitx5-android` | 絕對 URL | ✅ PASS | 外鏈格式完好 |
| L-4 | `docs/ARCHITECTURE.md:9` | `https://github.com/fcitx5-android/fcitx5-android` | 絕對 URL | ✅ PASS | 外鏈格式完好 |

**L-5 README 的 NOTICE 引用（特別檢查）**：`README.md:17`「詳見 `NOTICE`」→ 目標檔 `NOTICE` 存在（`Test-Path` → True）→ ✅ **PASS**，無 dead link。

**L-6 INFO（非連結，僅路徑註記）**：
- `README.md:49` `D:\666\opencode\.omo\plans\android-keyboard.md` → 存在（True）。
- `README.md:50` `.omo/drafts/android-keyboard.md` → 以 repo root `D:\666\opencode` 解析存在（True）。
- `UPSTREAM.md:136` 相依文件 `docs/ARCHITECTURE.md`、`README.md` → 兩者皆存在。
- 上述均為純文字路徑（非 `](...)` markdown 連結），且實際目標都存在 → 不構成 dead link。

**結論**：所有 markdown 相對連結與 NOTICE 引用皆解析成功，**無 dead link**。

---

## 3. 授權（Li-1 … Li-3）

### Li-1 NOTICE 7 項 + 版本 — ✅ PASS
逐項細讀 `NOTICE`（101 行），共 **7 項**，皆有「授權條款」欄位：

| # | 項目 | 授權條款 | 版本/提交 | 證據（NOTICE:line） |
|---|------|----------|-----------|----------------------|
| 1 | fcitx5-android | LGPL-2.1 | `6998502528cd26efb6079556da92003638e4179c` | `:7-14`（版本 `:10`） |
| 2 | fcitx5-chewing | LGPL-2.1+ | 參照公開版本（reference） | `:17-23` |
| 3 | libchewing | LGPL-2.1 | `a6a8fa4-dirty` | `:26-35`（版本 `:29`） |
| 4 | OpenCC | Apache-2.0 | `1.2.0` | `:38-45`（版本 `:41`） |
| 5 | FlorisBoard 主題構想 | Apache-2.0 | reference-only | `:48-55` |
| 6 | GuilelessBopomofo | GPL-2.0+ | reference-only | `:58-65` |
| 7 | HeliBoard | Apache-2.0 | reference-only | `:68-75` |

- ✅ 實際採用/參照並釘選版本的三項（fcitx5-android、libchewing、OpenCC）皆有明確版本識別碼。
- ⓘ INFO：項目 2、5、6、7 屬「參考/未整合」性質，依文件說明不釘版本（`NOTICE:22`「參照公開可得之最新版本」；`:53/:63/:73` 表未複製源碼）。此為設計決定而非缺失。

### Li-2 LICENSE canonical LGPL-2.1 — ✅ PASS
`LICENSE:1-10` 與 LGPL-2.1 canonical header 比對一致：
| 預期 | 實際（LICENSE:line） |
|------|---------------------|
| `GNU LESSER GENERAL PUBLIC LICENSE` | `:2` ✓ |
| `Version 2.1, February 1999` | `:3` ✓ |
| `Copyright (C) 1991, 1999 Free Software Foundation, Inc.` | `:5` ✓ |
| `51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA` | `:6` ✓ |
| `Everyone is permitted to copy and distribute verbatim copies` | `:7` ✓ |
| `of this license document, but changing it is not allowed.` | `:8` ✓ |
| `[This is the first released version of the Lesser GPL.` | `:10` ✓ |

檔案全長 516 行（完整官方文本規模）。僅有 FSF 地址內的空格數差異（`02110-1301  USA` vs `02110-1301 USA`），為無害排版差異，非內容偏離。

### Li-3 NOTICE L1 錯字修正（本審計唯一被授權之 NOTICE 編輯）— ✅ DONE
- 修正前（baseline read）：`NOTICE — 版面AttrIBUTIONS（逐項說明）`
- 修正後：`NOTICE — 逐項歸屬（逐項說明）`
- 僅替換子字串 `版面AttrIBUTIONS` → `逐項歸屬`，其餘 100 行未動（diff 見 §B3）。

---

## 4. 一致性 — 三個釘選識別碼全樹一致（C-1 … C-3）

> 以 `grep` 全樹掃描三組識別碼，每次出現皆逐字比對。

### C-1 上游 SHA `6998502528cd26efb6079556da92003638e4179c` — ✅ PASS
| 位置 | 值 |
|------|-----|
| `NOTICE:10` | `6998502528cd26efb6079556da92003638e4179c` ✓ |
| `UPSTREAM:15`（HEAD SHA 表） | 同 ✓ |
| `UPSTREAM:26`（`6998502528…` 縮寫，一致） | ✓ |
| `UPSTREAM:108`（ls-remote HEAD） | 同 ✓ |
| `UPSTREAM:109`（ls-remote master） | 同 ✓ |
| `UPSTREAM:117`（log 輸出） | 同 ✓ |

### C-2 libchewing `a6a8fa4-dirty` — ✅ PASS
| 位置 | 值 |
|------|-----|
| `NOTICE:29` | `a6a8fa4-dirty` ✓ |
| `UPSTREAM:76`（prebuilt 版本表） | `a6a8fa4-dirty` ✓ |
| `UPSTREAM:97`（緩解策略） | `a6a8fa4-dirty` ✓ |
| `UPSTREAM:123`（重現命令） | `a6a8fa4-dirty` ✓ |

### C-3 OpenCC `1.2.0` — ✅ PASS
| 位置 | 值 |
|------|-----|
| `NOTICE:38`（標題）| `1.2.0` ✓ |
| `NOTICE:41`（版本欄）| `1.2.0` ✓ |
| `UPSTREAM:77`（prebuilt 版本表）| `1.2.0` ✓ |
| `UPSTREAM:124`（重現命令）| `1.2.0` ✓ |

- ✅ 三識別碼在 **UPSTREAM 與 NOTICE** 間 100% 一致。
- ⓘ INFO：`README.md` 為 scaffold 聲明檔，未釘選 SHA/版本字串（其 `README.md:13` 僅稱 `OpenCC`），與 UPSTREAM/NOTICE **不互相矛盾**——釘選權威以 UPSTREAM/NOTICE 為準。
- ⓘ INFO：`OSS-NOTES:27` 的「最新 **0.13.1**」與 `ROADMAP:27` 的「libchewing 0.13.x」為「未來升級目標版本」，與 prebuilt 釘選 `a6a8fa4-dirty` 屬不同語意（release 版 vs 釘選 commit），不相衝突。

---

## 5. 範圍 — 目標樹外零改動（Sc-1）

- ✅ SC-1：本次唯二檔案變動 = **(a)** 新增 `docs/CHECKS/wave0.md`、(b) `NOTICE` L1 錯字修正。審計前後目標樹 byte-level diff 確認無其他變動（§B3）。
- ✅ SC-2：父目錄 `D:\666\opencode` 內容列出於 §B2——僅含 `.codegraph / .omo / addy-skills / android-keyboard / kiss-translator / videos` 目錄與既有檔案，本次作業未於父目錄新增或刪除任何項目。
- ✅ SC-3：未 commit、未 push、未 clone、未寫任何程式碼；`docs/CHECKS/` 為本審計唯一新增目錄。
- ⓘ INFO：`ROADMAP.md:57`（Wave0 進度條）中 T4 仍標 `[ ]`。依本次 scope（「MUST NOT 變更其他內容」）**不**更新該格，交由排程確認後勾選；非缺陷。

---

## 6. 總結

| 檢查項 | 結果 | 阻擋項 |
|--------|------|--------|
| 1. 結構（12 檔齊備） | ✅ 全 PASS | 無 |
| 2. 連結（4 markdown 連結 + NOTICE 引用） | ✅ 全 PASS，無 dead link | 無 |
| 3. 授權（NOTICE 7 項 / LICENSE canonical / L1 錯字已修） | ✅ PASS | 無 |
| 4. 一致性（SHA / a6a8fa4-dirty / 1.2.0） | ✅ PASS | 無 |
| 5. 範圍（僅 2 處變動，父目錄零改動） | ✅ PASS | 無 |

**結論：Wave 0 骨架 5 大項全綠，無 needs-fix；唯一瑕疵（NOTICE L1 錯字）已依授權於本次修正。**

---

## 附錄

### B1 — 審計前 Baseline 目標樹
```
android-keyboard\
├── LICENSE                        26,783 B
├── NOTICE                          5,068 B
├── README.md                       2,616 B
└── docs\
    ├── ARCHITECTURE.md             1,988 B
    ├── AUDIT.md                    5,793 B
    ├── IOS-NOTES.md                1,143 B
    ├── KEYMAP.md                   1,642 B
    ├── OSS-NOTES.md                2,983 B
    ├── RELEASE.md                  6,008 B
    ├── ROADMAP.md                  8,544 B
    ├── UPSTREAM.md                 9,596 B
    ├── diagram\architecture.svg    6,319 B
    └── (CHECKS\wave0.md — 本次新增)
```

### B2 — 父目錄 `D:\666\opencode`（審計期間列出）
```
.codegraph / .omo / addy-skills / android-keyboard / kiss-translator / videos  （目錄）
addy-skills.tar / AGENTS.md / kiss-translator-rust-rewrite.md / kiss-translator-subs-rewrite.md / kiss-translator-subtitle-customization.md / kiss-translator-subtitle-plan-P1P2.md / README.md / ultrawork-hussite-remotion.md  （檔案）
```

### B3 — 本次變動 diff（摘要）
- `docs/CHECKS/wave0.md`：新增（本檔，對 wave0 自身）。單行：`D:\666\opencode\android-keyboard\docs\CHECKS\wave0.md | w+`
- `NOTICE`：L1 單行替換 `版面AttrIBUTIONS` → `逐項歸屬`；其餘 100 行 byte-identical。

### Manual-QA（逐項）
- **S-1…S-12**：每項 `Test-Path`/Read 回傳真實內容與 size → PASS（見 §1）。
- **L-1…L-5**：`grep '\]\('` 全樹僅 4 筆 markdown 連結，相對者 `Test-Path` True；`NOTICE` 存在 → PASS（見 §2）。
- **Li-1**：NOTICE 7 項逐一讀取，版本欄位核對 → PASS。
- **Li-2**：LICENSE:1-10 逐行比對 → PASS。
- **Li-3**：edit 前後 Read 對照，確認唯 L1 變動 → PASS。
- **C-1…C-3**：grep 全樹輸出核對一致 → PASS。
- **SC-1…SC-3**：審計前後樹比對 + 父目錄列出 → PASS。
- **Adversarial — stale_state**：N/A。快照審計，所有識別碼均與當日文件一致、無需外部即時狀態。
- **Adversarial — dirty_worktree PROBE**：目標樹 baseline（B1）vs 審計後僅多 `docs/CHECKS/wave0.md`、`NOTICE` L1 一行文字變動；父目錄（B2）無新增/刪除 → 乾淨。
- **Adversarial — misleading_success_output PROBE**：上述每項皆附 file:line 證據與 byte 數，成功宣稱均有可稽輸出，無空話。