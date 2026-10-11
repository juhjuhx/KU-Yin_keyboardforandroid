# P0-R 有限英文離線詞庫與詞緩衝

## 問題與結果

本輪檢視的舊 `KuYinEngine.onEnglishKey` 只套用 Shift 後逐鍵 `onCommit(text)`；英文 UI 的 Space、Backspace 與 Enter 也直接呼叫 editor callbacks。這條路徑沒有 word buffer、字首 query 或英文候選。新增 `EnglishLexicon` 與 `EnglishWordSession`，服務／UI 由共用流程 owner 整合；英文不再借用中文 decoder 的組字。

範圍是**目前單字的離線字首補全**，只有點候選才替換整詞。Space 與標點送出使用者原字，沒有自動拼字修正、下一詞預測、context retrieval 或個人化學習。這些是本輪產品決策，不是抄用上游實作。

## 真實詞頻資料

採用官方 AOSP LatinIME 固定 commit `127336e9f29d69607eab55982324b210279ae8c5` 的 [en_wordlist.combined.gz](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/127336e9f29d69607eab55982324b210279ae8c5/dictionaries/en_wordlist.combined.gz?format=TEXT)。Gitiles 回傳 base64；核對雜湊的對象是解碼後 gzip，非 HTTP response text。

| 項目 | 已核對資料 |
| --- | --- |
| gzip SHA256 | `07682388185c285d307e341d1733331af8699f735b4137e9f22571017fab69d2` |
| gzip／解壓大小 | 914,050／7,270,298 bytes；165,644 lines |
| header version/date | `54`／`1414726273`，固定舊字典，不稱為最新語料 |
| 格式／分數 | [sample.combined](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/127336e9f29d69607eab55982324b210279ae8c5/dictionaries/sample.combined)：單字的 `f` 為 0–255 logarithmic frequency；`f=0` 不應主動建議。不是 raw 次數。 |
| 本輪有效清單 | 僅第一層 unigram、`[a-z]+`、`f>0`、空 `flags`、非 `not_a_word=true`；同字取最大 `f`，依 `(-f,word)` 排序，前 10,000 詞 |
| TSV SHA256 | `e44d3b87e830de6e57cc493f3cdb090404a477781299648464b2824e3b488e97` |
| TSV 大小 | 123,878 bytes；這不代表 APK 增量或 runtime heap |

清單包含真實常用詞，不是 hello-only fixture：`hello=120`（全清單順位 3997）、`project=148`、`frequency=131`。`hel` 原 frequency 順序為 held、help、helped、helping、helps、helicopter、hello、helicopters；候選池最多 8 個，因此 hello 可以手動選取。沒有修改 frequency 讓 hello 虛構成第一名。

資產：`app/src/main/assets/english/word_frequency.tsv`；完整 attribution 與 Apache2 文本同目錄；[來源記錄](../../../app/src/main/assets/english/SOURCE.md)。`scripts/generate_english_lexicon.py --source <已解碼gzip>` 先驗來源 SHA，再驗輸出 count/SHA，才寫檔；執行時不查網路。

## 純 Kotlin seam

- `EnglishPrefixProvider.suggest(prefix, limit)`：只取得當前 normalized token；不接 Android、editor、JNI 或使用者歷史。
- `EnglishLexicon.fromTsv(reader)`：驗證小寫 ASCII、1–255 score、row/word 長度界限；同字去重。載入後只保留已排序單字 list，最多 10k entries。
- `EnglishWordSession.state`：`StateFlow<EnglishWordState>`，snapshot 包含 `wordBuffer` 和 `candidates`。
- 命令：`typeText`、`select(index)`、`space`、`complete`、`punctuation`、`backspace`、`reset`、`configureSuggestions(Boolean)`。
- 回傳沿用 `DispatchResult`／`orderedCommits`：所有 commit 按順序套用一次。現有 class 未由英文元件修改。

MVP 每次 query 走有限 list，依既有 frequency 順序查 `startsWith` 取 8 個；不建立高 overhead 的逐字物件 trie，也不引入完整 LatinIME/NLP JNI。這是有界實作選擇；latency、heap 和實機 smoothness 尚未量測。

| 輸入／事件 | 行為契約 |
| --- | --- |
| ASCII 字母 | 原大小寫保留在 buffer；lookup 用 `Locale.ROOT` lower-case。最多 64 字母；超限時送出原字並清空。Shift 本身由 presentation engine 處理。 |
| 候選大小寫 | `hel→hello`、`Hel→Hello`、`HEL→HELLO`；mixed case 保留已輸入 prefix，再接 lowercase suffix。單個 uppercase prefix 無法區分 Shift 與 CAPS_LOCK，按全 uppercase prefix 投影。 |
| 點選候選 | 送出整詞，不預加空白，清空 buffer/candidates。UI 的 stale candidate revision 檢查由共用 snapshot owner 負責。 |
| Space | buffer 原字先送出，再送出一個空格；點選候選後 Space 仍只送一個空格。 |
| 標點、非字母 literal | 先送原字，再送原符號，清空；不讀 surrounding text，也不做 smart punctuation。 |
| Enter／Complete | 有 buffer 先送原字且 `consumed=true` 一次；空 buffer `consumed=false`，由 service 套既有 editor action 政策。 |
| Backspace | 有 buffer 只刪 buffer；空 buffer 回傳 unconsumed，交由 service 刪 editor 文字。 |
| mode／editor／lifecycle | mode completion 與 clear/reset 用同一 owner；新 editor、選區移動、finish input 的 reset 清空暫存，避免跨欄位 stale 候選。 |

大小寫限制：資產只收小寫 ASCII，排除 `I`、專名、apostrophe contractions、連字號與非 ASCII 字詞；使用者仍可原樣輸入並 flush。詞庫缺字不能截斷或改寫原字。

## 敏感欄位

既有 `EditorPolicy.from` 是政策來源，未另造 `sensitiveEditorPolicy`。`isSensitive`、`forceAscii` 或 `!allowComposition` 時由 service 設 `configureSuggestions(false)`：當場清 buffer/candidates，後續 `typeText` 原樣直接 commit，**連靜態 lexicon query 都不執行**。重新開啟不復原前欄位文字。

`IME_FLAG_NO_PERSONALIZED_LEARNING` 是禁止個人化學習的宣告，不是全面禁止靜態建議；這個 provider 本來沒有 learning／persistence。參照 [EditorInfo 官方定義](https://developer.android.com/reference/android/view/inputmethod/EditorInfo#IME_FLAG_NO_PERSONALIZED_LEARNING)。Service 的 composing effects 遵循 [InputConnection](https://developer.android.com/reference/android/view/inputmethod/InputConnection#setComposingText(java.lang.CharSequence,%20int))，不能只保存內部 buffer 而遺漏 editor composing region。

## 隔離驗證

- 新增 17 個純 JUnit，使用正式 `DispatchResult`／`orderedCommits`、Kotlin compiler 2.2.10、JDK21、coroutines 1.10.2；standalone compile + JUnit **17/17 PASS**，未占 Gradle 執行槽。
- 有效候選池 RED：真實 10k + `hel` 的 hello membership，在 5 candidate 上限時正常 assertion failure（17 tests／1 failure），改上限為 8 後 GREEN。未改資料 frequency。
- 初次新 class 尚不存在的 compile failure 只標 **IMPLEMENTATION_SCAFFOLD**，不是有效 behavior RED；既有 UI/服務回歸由整合 owner 保存。
- Python 資料契約 **3/3 PASS**：來源 hash gate 不覆寫既有輸出、排除 offensive/nonword/nested/uppercase、固定 TSV count/SHA 與常用詞內容。
- 沒有可用 `lsp_diagnostics` 工具；Kotlin 編譯與 Python 編譯／資料契約作為此隔離元件的實際診斷。完整 Gradle、APK 與 UI 接線結果以本輪統合報告為準；裝置 runtime **NOT_TESTED**。

## 已讀技能記錄

- `~/.agents/skills/addyosmani-source-driven-development/SKILL.md`：官方、固定 revision 的程式／資料／授權證據。
- `~/.agents/skills/addyosmani-api-and-interface-design/SKILL.md`：provider/session 與 service/UI 的穩定邊界。
- `~/.agents/skills/addyosmani-test-driven-development/SKILL.md`：候選池 assertion RED → GREEN；初次 missing-class 編譯錯誤沒有列為有效 RED。
- `~/.codex/plugins/cache/openai-curated-remote/codex-engineering-guardrails/1.1.1/skills/code-work/SKILL.md`：新檔 ownership、固定資料 provenance、與共用 Gradle/裝置 gate 隔離。
