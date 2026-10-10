# AIE V1 深讀報告

審查日期：2026-10-10–11。狀態：REVIEW_READY；產品未修改，實作待決策。

## 1. Current source of truth

- 工作區 `.worktrees/aie-v1-engineering-review`，分支 `docs/aie-v1-engineering-review`；基線 HEAD `3072affee95fa7fa8b0016ad85aac322272e4dea`，產品 commit `9f5db676a17b5770a738b5c20da937f9211e5a34`。
- P0-R 工區起始乾淨；11 個既有工區已盤點。root、C4 forensic、clean-baseline、M4 的既有未提交資料全部保留，未匯入。
- 已重新核對 Desktop P0-R APK：32,276,066 bytes，SHA256 `14082a60527ef57542d62cfb2bfaac1ed53a99080fb5af7f0de2792862fe260f`；`20002 / 0.2.0-p0r.1`。
- 使用者本輪回報 `BASIC_TYPING=WORKS_ON_USER_DEVICE`；不是已核對手機 base.apk bytes 或完整 30-case PASS。本輪沒有裝置命令。
- P0-R 完整交接、P1-A/B/C/D 與 E/G 交接已讀；P1-F 外部執行報告、深讀與獨立裁定也已讀，原30案例全NOT_TESTED；位置為Desktop裝置驗收/2026-10-08-214315-P1F-盤點-abed8d1c。main／C4 不是最新產品。

## 2. Product capability inventory

| 能力 | 現況與來源 |
|---|---|
| 注音 | libchewing production；startup-only fallback，`session/ProductionDecoderResolver.kt:12` |
| 大千 | 37 注音＋4 顯式聲調；`ui/DachenKeyboardRows.kt:13` 衍生 canonical mapping；四排 11/10/10/10 |
| 無調／連句 | P0 同 pin FuzzyPartialPrefix＋scratch preview；真 JNI/Robolectric 證據在 P0 交接，未重跑 |
| 英文 | 10k 離線 frequency-score prefix completion，最多 8 建議、64 字元 token；沒有 autocorrect／n-gram／learning |
| 候選瀏覽 | LazyRow 只顯示 adapter 當前頁；產品未接翻頁／展開命令 |
| 學習 | native userdict 與 fallback prefs 為不同 store；不是同時雙 decoder 排名 |
| 符號 | 硬編碼四排 39 項（含數字），emoji 40 項；沒有 production search/catalog/kaomoji 分頁 |
| 發布 | Debug 身分可辨、同 signer 更新前提；正式 signing、CI mainline、裝置矩陣與 LGPL 散布義務仍未完成 |

## 3. Real Android interaction flow

`Compose clickable → TapZhuyinKey(label) → canonical ASCII → ChewingEngineSession → AndroidChewingEngine → JNI → pinned libchewing → EngineUpdate → ComposeImeState → UI`。
候選 callback 捕捉同一 immutable state，檢查 `session.state.value === sessionState`（`KuYinKeyboardUi.kt:227–234`）；再送當前頁 index。明確選字完成組字，`orderedCommits` 依序經 service `commitText`，並同步 `setComposingText`。Enter consumed 才不執行外部 editor action。

`KuYinInputMethodService.kt:279–315` 檢查 commit/composing Boolean ACK；失敗保留未確認尾段並停輸入，不自動重送。`EditorEffectTracker` 只保存 connection/generation 與 offset/range，不保存完整訊息；own callbacks 與外部 cursor/span 更新分開。ACK 不等於全 OEM 實際文字已驗收。

## 4. Candidate retrieval flow

`AndroidChewingEngine.kt:289–358`：建立 `:memory:`、停 learning 的 preview，重放 active actions，安全 finalize tail，再 `cand_open`。native `candidateIdentities` 枚舉可選池；加上 null identity 的完整轉換項；先按 native page 切片，再以各自 scratch context 投影完整 buffer，最後依 display 去重。

P0 manifest 綁定 source archive `29fdb2470a0a09133c1779c6d8058e394778a917c2c35da7f43a2e2869860816`；這是解壓 cache 而非 Git clone（沒有.git），不以git rev-parse核對。真 pin `a6a8fa4…` 的 `src/editor/mod.rs:92–101` 預設 page=10；CAPI `capi/src/io.rs:486–490` 拒絕0或>10，頁長上限10。所以第一頁最多十個 plans，完整轉換／同 display 重複或無效投影會讓顯示數更少；這不是全字典只有十字。`lastTotalChoices` 是去重／投影前 plan count，並非唯一完整可見候選數。

`nextPageUpdate/prevPageUpdate` 存在（adapter:196–214），session 也接線（:73–79）；`CandidateBar` 沒有翻頁入口。左右捲動僅抵達目前頁。不得為了讀候選對 live 執行破壞性 cand_open。

## 5. Candidate ranking flow

目前沒有 KU-Yin production CandidateRanker。首項為 preview 完整句 decode，其餘依 native 候選順序；native `sort_candidates_by_frequency` 預設 false（pin `editor/mod.rs:101`、`selection/phrase.rs:246`），兩種 context 未覆寫該設定。

preview 不讀 live 個人字典；因此 native 學習結果不能直接改變顯示排序。fallback 的 tier/frequency query（`ZhuyinDictionary.kt:1315–1356`）不是 native 排名。排序先要測池 recall、身份與頁面邊界，不能只改畫面或盲加 frequency flag。

## 6. Composition and selection ownership

live context 擁有實際 decoder 狀態；session 只投影 preedit/candidates/generation；UI 擁有呈現與按鍵 callbacks；service 擁有 editor effects。scratch 是衍生查詢 context，非第二個組字 owner。

adapter:163–193 以 `(text, occurrence)` 在 live 重找候選，核對完整 buffer，錯配保留組字。不得把 UI 重排後 index 當 native ordinal。`generation` 已存在，但 command 只有 index；未來 CandidateRef 必須攜 generation/provider/page/selection identity，先小 slice 再擴契約。

## 7. Current learning behavior

native `userdict.dat` 位於 `noBackupFilesDir/libchewing/user/`（`LibChewingDataInstaller.kt:27–39`）；政策只允許普通文字且非 FORCE_ASCII／NO_PERSONALIZED_LEARNING。preview 永不寫 learning。

已確認 HIGH：`MainActivity.kt:694–701` 重設只呼叫 fallback `clearLearnedFrequencies`，`ZhuyinDictionary.kt:1369` 只清 SharedPreferences，native store 未重設卻顯示成功。

pin `editor/mod.rs:711–722` 在 native commit 內先 auto_learn；Android ACK 在後。這是既有 native 時點，不代表 ACK 後 KU-Yin 可再重複增頻。新增學習需 commit event/ACK ownership 設計；失敗不得新增外部 learning record。

## 8. Current touch hit testing

`ZhuyinKeyView.kt` 實際位於 `KuYinKeyboardUi.kt:1110–1132`：48dp cell 的 clickable 在 visual padding 外，並非全部 padding 都是死區。没有座標 resolver、鄰鍵機率、滑移修正、長按／repeat 共用模型。

既有 hitbox/Dachen tests 用 semantics `performClick`＋bounds，沒有座標 touch event；不能證明中央／邊界／gap／TalkBack hit 仲裁。[Compose 官方文件](https://developer.android.com/develop/ui/compose/accessibility/api-defaults) 指 clickable 可自動擴小目標，因此不能把 16dp clear icon 直接宣稱為 16dp 實際 hitbox。

## 9. Current symbol catalog

`SymbolsKeyboardLayout:922–925` 四排 inline strings；`EmojiKeyboardLayout:1062` 使用 `ZhuyinConstants.EMOJI_LIST`。沒有 source ID、search、categories、recents/favorites 或 grapheme validation。View `SymbolPicker` 是 donor，不能算產品功能。

Desktop AI Studio donor 的 CandidateBar 仍以 display String selection（:347/437），比 P0 indexed snapshot 舊；不回拷。TAUI/web scaffolds 不屬 Android runtime。捐款／社群卡片保留；QR Canvas:123–144 是 URL hash 圖樣，不是標準 QR encoder，暫不能聲稱可掃描。

## 10. Privacy/security boundaries

無 INTERNET，allowBackup=false；英文字表僅 bundled data。Sensitive/非文字/FORCE_ASCII 禁 composition/learning；NO_SUGGESTIONS/AUTO_COMPLETE 禁英文建議而保留必要中文轉換；NO_PERSONALIZED_LEARNING 禁學習而非禁普通候選。

目前沒有 user private mode、統一 personal-store erase 或 touch consent。OTP 未必有可辨平台 flag，不能憑欄位名稱猜測；需 privacy matrix 與使用者決策。不得持久化完整句子、raw key/touch log 或輸出 private replay。

## 11. Known evidence limitations

P0 歷史證據 app229/host37/Python104/contracts10，僅證各自層；本輪只重新執行 static contracts。沒有新 Gradle/native/device usability/latency 測量或 APK。31 舊 instrumentation／30 full scopes 不因使用者 basic typing 可用而全 PASS；新 P0 host 32 tests 不能用 v3 舊 manifest 執行。

三個獨立 read-only reviewers 回傳以上候選／reset／touch具體發現，隨後遇服務 usage limit，均沒有 final approval。主代理逐項核對源碼；完整獨立簽核仍 OPEN。OCR v1.12.13 provider401，無 OCR PASS。

## 12. Architectural contradictions

AGENTS 的舊 core paragraph 仍稱 dictionary production；實際 resolver 為 native。AGENTS 保留不改，CURRENT_STATE/ARCHITECTURE 本輪補最新事實與使用者報告。ADR0003 的「不可 per-key context」舊文未區分 persistent owner 與 P0 derived query contexts；不能藉此刪除安全 preview。舊 ROADMAP 的 D3 尚未切換／M4 visual mandate 不再是 AIE 的目前授權。

下一步請讀 [issue ledger](AIE_V1_ISSUE_LEDGER.md)、[候選 UX 待決策](../adr/0006-aie-candidate-ux.md) 及 [分期 roadmap](../roadmap/AIE_V1_ROADMAP.md)。沒有產品實作批准。
