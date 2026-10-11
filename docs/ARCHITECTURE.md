# KU-Yin Architecture (normative, Compose/C4 line)

> Supersedes all View-era descriptions. For history see `docs/HISTORY.md` and
> `docs/archive/`. If any older doc contradicts this file, this file wins.

```
Android editor
    │  EditorInfo / InputConnection
    ▼
KuYinInputMethodService
    ├── EditorPolicy.from() ............ sensitive/ASCII/composition/learning normalizer
    ├── EditorEnterPolicy.from() ....... inline Enter label/presentation/behavior
    ├── userPreferredMode/effectiveMode . forced editors override temporarily
    ├── KuYinEngine .................... presentation only: KeyboardMode,
    │                                     ShiftState, editor policy
    ├── EditorEffectTracker ........... own composing offsets / acknowledged writes
    ├── EnglishWordSession ............ bounded word buffer + offline EnglishLexicon
    └── ComposeDecoderSession .......... ALL Chinese state: preedit,
            │                             candidates, paging, selection,
            │                             space/enter/backspace/punct/reset
            ├── ChewingEngineSession (PRODUCTION when native startup succeeds)
            │       └── AndroidChewingEngine .. sole JNI caller; service-owned
            │               │                   successful startup initializes once
            │               └── JNI chewing_jni.cpp (C++17)
            │                       │  pinned: prebuilt 3587ba33 / src a6a8fa4
            │                       └── libchewing C API (system .dat + userdict.dat,
            │                                            app-private)
            └── ZhuyinDictionarySession (startup-failure fallback)
```

## Boundaries (normative)

- Framework: service owns lifecycle + InputConnection side effects only.
- Policy: `EditorPolicy` + `EffectiveModePolicy`; forced editors can never
  permanently change `userPreferredMode` and can never reach Zhuyin composition.
- Presentation: `KuYinEngine` owns mode/shift/policy. UI reads `engine.mode`
  and `session.state`; UI/service never read engine composition/candidates.
- Decoder: `ComposeDecoderSession` + `DispatchResult(commitText, consumed,
  additionalCommits=[])`. Apply order is always commitText then
  additionalCommits (single shared UI path per action).
- Enter: service snapshots `EditorInfo` primitives into one immutable
  `EditorEnterPolicy`; UI receives only label/presentation. Zhuyin Enter applies
  ordered session commits first and consults the editor policy only when unconsumed.
  `NO_ENTER_ACTION` suppresses standard/custom inline actions; custom labels dispatch
  `actionId`. `performEditorAction(false)` does not trigger a second newline effect.
- Native: only `AndroidChewingEngine` touches JNI symbols. UI never does.
- Storage: system dicts + `userdict.dat` under `noBackupFilesDir/libchewing`;
  `allowBackup=false`. No network, no telemetry, no cloud candidates.
- Fallback: `ZhuyinDictionarySession` stays as startup-failure fallback and
  test/comparison harness; never a user-facing decoder toggle.
- Donor: old View shell (PR #4) is reference only, not a product path.
- UI (M4 target, not this doc's mandate): candidate strip, canonical Dachen
  from `KeyboardLayout.Dachen`, row-inset geometry, unified touch surface,
  SymbolCatalog, long-press model, emoji categories, compact toolbar.

## Test boundaries

Python contracts pin static invariants; JVM tests pin behavior (fresh counts in
`docs/CURRENT_STATE.md`);
instrumentation/runtime smoke covers lifecycle+JNI on device. Runtime-only
gates (touch geometry, rotation, OEM matrix, QR scan art) are never claimed
from static evidence.

P1-E 新增獨立 test-only `:acceptance-host`，使用 platform Views。
RecordingEditText 保留真實 editor InputConnection、消費並記錄 editor action，
文字操作仍 delegate；平台 EditorInfo 與最終受控宣告分別保存。Host instrumentation
以可見 UIAutomator 軟鍵 selector 控制外部 KU-Yin IME 視窗；host 不依賴產品、
decoder、Compose 或 native。裝置 IME 狀態由固定 serial 的操作手冊／執行器管理，
instrumentation 不自行接管預設輸入法。

## P0-R 核心輸入（2026-10-10）

- 原生pin維持3587ba33/a6a8fa4；conversion_engine=2啟用既有FuzzyPartialPrefix。production仍是libchewing，未接librime/Fcitx framework。完整研究與授權比較見 `review/p0-core-typing/03-decoder-comparison.md`。
- live context擁有實際解碼；active replay history去除已auto-commit的prefix。候選查詢只操作不學習的derived preview context，讀出完整native buffer；scratch index不能盲用於live選字。已證實還原的pending key/Backspace逆操作可從active history消除，實際事件由synthetic test trace獨立保留。
- EngineUpdate／ComposeImeState的unavailableReason使失敗可見；拒絕選字不能接著commit預設字。未完成的音節不能越過標點或模式切換。
- candidate tap完成組字並依orderedCommits送出一次。Space/Enter先完成Chinese composition；English Space完成原typedword加一個separator，Enter先完成word。只有unconsumed Enter才呼叫normalized editor policy。
- 服務擁有setComposingText/commitText/finishComposingText。EditorEffectTracker只保存offset/range，以connection+editor generation與bounded pendingtuple分辨自身通知；外部span失效或游標移動重置decoder。這不是通用OEM callback證明。
- commit false/null connection保存未確認文字／ordered tail供手動核對，不automaticretry。普通欄位顯示錯誤；敏感欄位遮蔽，記憶體狀態不寫檔或log。AndroidIPC送入是否成功與實際editor文字是不同證據層。
- EnglishLexicon是AOSP固定commit的10k frequency-score字表，獨立Kotlin prefix lookup，不含network/learning/autocorrect。資料及Apache2 notice隨asset分發，見 `assets/english/SOURCE.md` 及 `review/p0-core-typing/08-license-review.md`。
- diagnostics僅synthetic test source/native host probe，記錄event、glyph/ASCII、provider、preedit/candidate identity、commit request/result及actualEditText delta；不編入產品／release，不記錄日常輸入。沒有雲端telemetry。

English suggestions 額外尊重 `NO_SUGGESTIONS`／`AUTO_COMPLETE`；editor 自有完成與英文詞庫預測分開。這兩個旗標不禁用必要的中文 phonetic conversion；password／非文字／forceASCII composition policy 沿用 P1-C。

## AIE V1（2026-10-11，提案邊界）

現有P0-R runtime架構保留。新的candidate presentation/retrieval/ranking、learning、touchresolver與symbolrepo圖是PROPOSED，見 `engineering/AIE_V1_ARCHITECTURE.md`，並非已存在runtime。ADR0006尚待UX答案，不能據舊M4視覺授權重設品牌。native live為persistent唯一owner，derived scratch只是查詢投影；舊ADR0003的per-key禁止不應誤解為移除安全preview。

現行學習是productionnative或startupfallback各自store；設定頁reset未涵蓋native為已確認HIGH。任何futureexternallearning需避免對nativeauto-learn重複計次；見privacy matrix與issueledger。

## AIE S0/S1 implemented boundary（2026-10-11）

候選現由 EngineCandidatePage → CandidatePageSnapshot/Ref → CandidatePanel 呈現單列與展開網格；typed page/selection events 經唯一 session 校驗。翻頁在既有 scratch plan projection 邊界內，沒有 live native 方向鍵／候選 refresh mutation。composition 與 snapshot generation 分開，editor/hide/close 失效 prior refs，view-start gate 恢復輸入。原 orderedCommits 與 InputConnection ACK/recovery 不變。

設定的完整學習 reset 暫不支援且停用，不作局部刪除或假成功。native learning owner/存放/政策未改。新 ranking、personal preview、adaptive touch、SymbolCatalog 仍為未來提案；S1 不等同 M4 或 release acceptance。詳 `review/aie-s1-candidate-browser/01-native-and-state-contract.md`。
