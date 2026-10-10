# AIE V1 架構選項

狀態：PROPOSED／未批准。現況以 ../ARCHITECTURE.md 與深讀報告為準。本輪不新增 module／database／decoder／runtime owner。

## 漸進目標

```text
Android InputMethodService [lifecycle / normalized policy / editor effects]
    └─ Compose Presentation [既有 themes / Dachen / community]
         ├─ CandidatePresentation [snapshot + viewport + expanded page]
         ├─ Interaction callbacks [literal keys initially]
         │    └─ TouchResolver [future, opt-in decision pending]
         └─ ComposeDecoderSession [唯一 Chinese state owner]
              ├─ ChewingEngineSession → AndroidChewingEngine → pinned JNI/libchewing
              │    └─ Derived CandidateProvider [scratch, read-only projection]
              └─ Startup fallback ZhuyinDictionarySession

EnglishWordSession → EnglishPrefixProvider [existing 10k lexicon]
CandidateProvider → CandidateRanker [future deterministic / identity-preserving]
                      ↑ UserLearning [future, only supplemental owned data]
SymbolRepository [future attributable catalog] → literal editor effects
DispatchResult / orderedCommits → service → InputConnection ACK
                                           └─ confirmed-effect observer [future]
```

## 模組責任與邊界

| 邊界 | 初始實作／未來條件 |
|---|---|
| CandidateProvider | 起初仍在adapter；維持scratch query，回page與identity，不新增第二個decoderstate |
| CandidatePresentation | 可先抽 `ime/ui/CandidatePanel.kt`，只收immutable page、UIcallbacks；不讀JNI/rawEditorInfo |
| CandidateRanker | 先identity排序seam與replay，再評估nativefrequency flag/personalpreview；不變rawinput或pool |
| CandidateRef | 未來provider＋compositionGeneration＋pageToken＋selection identity；display/index只為呈現，不是native身份 |
| Editor effects | service單一路徑；future event `(editorGeneration,eventId,ackStatus)` 防雙記；不得把ACK視實機文字驗收 |
| UserLearning | native保有已有Chinese phrase learning；KU-Yin若增choice/context/touch/English各明定owner與去重；不能每次預覽增頻 |
| TouchResolver | 先純geometry/literal座標oracle；再optional context prior。central/keyaction護欄、accessibility語義不動 |
| SymbolRepository | 不依賴decoder；unicode完整String、stableID/source/license/searchkeys；近期與收藏為獨立最小metadata |

## S1 首片：建議 single strip＋展開當前頁網格

保留主題、字體設定、四排大千、底列與community。改善候選列表可用寬度；preedit有界顯示、展開時可讀完整。提供明確展開／收合與前後頁控制。

建議展開時暫時用網格取代主鍵區，保留preedit、pagecontrols與返回，避免永久佔兩排或改鍵位；收合恢復原布局。此是待批准方案，不是已畫完或已實作UI。字級／clear/expand target須測320/360/600dp及fontScale，而非硬塞縮字。

首片只顯示同一backend page（≤10 plans），翻頁替換該snapshot；不拼接多頁全pool或引入ranker。expand/collapse不能造成native refresh/commit；scroll不click；候選點選仍綁同snapshot，decoder變更後舊ref拒絕。

## 替代方案

1. 單列＋展開網格（推薦）：日常高度低，選字時完整瀏覽；展開時鍵區暫隱，操作可逆。
2. 常駐兩排＋展開：更多候選立即可見，IME長期佔高，需另驗editor可視範圍。
3. 可配置區域：彈性大，但settings/state/layout/test組合多；不宜首片。

## 不可跳過的研究

- 對personal-preview reading做同pin隔離spike：可否讀取一致user lexicon而零寫入／不開live selection？未證明不落實。
- rawpool/page/display去重分層，禁止把plan count稱unique candidate count。
- native auto-learn在ACK前；若無安全CAPI延後機制，完整重構另作ADR，不偷偷disable既有learning。
- reset native owner排他性、pendingcomposition、失敗恢復與reset驗證需獨立slice，絕不在review直接刪userdict。
- SQLite：現有Room dependencies可評估；尚未建立AIEschema。storage選擇／migration／export需privacy批准。

## 授權與rollback

維持現行LGPL-2.1，不copy GPLsource；upstream僅研究。每片localcommit可逆，engine/pins/data/mappings不漂移。排名／觸控改善若降低correctness則撤回該實驗；個人資料migration不可用gitrevert替代recovery設計。
