# UI 元件責任

狀態：NORMATIVE／IMPLEMENTED（文內 PROPOSED/OPEN 例外）。目的：說明此邊界的實際責任、資料與故障處理。來源基線 `a6a4c654845338521700121ac06586887d1ec085`；R1工具／test-adapter不改產品。

## 實際 source files / evidence

- [app/src/main/java/com/example/ime/ui/KuYinKeyboardUi.kt](../../app/src/main/java/com/example/ime/ui/KuYinKeyboardUi.kt)
- [app/src/main/java/com/example/ime/ui/CandidatePanel.kt](../../app/src/main/java/com/example/ime/ui/CandidatePanel.kt)
- [app/src/main/java/com/example/ime/ui/DachenKeyboardRows.kt](../../app/src/main/java/com/example/ime/ui/DachenKeyboardRows.kt)
- [app/src/main/java/com/example/MainActivity.kt](../../app/src/main/java/com/example/MainActivity.kt)
- [app/src/main/java/com/example/ime/ui/OpenSourceDonationCard.kt](../../app/src/main/java/com/example/ime/ui/OpenSourceDonationCard.kt)
- [app/src/main/java/com/example/ime/ui/UpstreamAndCommunityCards.kt](../../app/src/main/java/com/example/ime/ui/UpstreamAndCommunityCards.kt)

## Component matrix

| Component | Responsibility / input-output | State owner |
|---|---|---|
| KuYinKeyboardUi | mode/preedit/suggestions呈現、typed callbacks、ordered commits | observes engine/session/English；不JNI |
| CandidatePanel/Strip/ExpandedGrid | immutable page、ref、scroll/grid/accessibility callbacks | session owns expanded/page/generation；Compose local scroll only |
| DachenKeyboardRows | canonical41 glyph/code來源 | KeyboardLayout.Dachen |
| KuYinEngine | mode/shift/policy；fallback另有組字 | nativeChinese不讀它的candidates |
| MainActivity | setup/settings/trial/feedback/community | trial dictionary engine與UIlocalstate |
| Donation/Community cards | existing links/credits | presentation，不typingnetwork |

header preedit與candidate strip上下分列；展開暫替主鍵區，保留header/收合/上一下一頁。horizontal swipe不選字；candidate tap需same-generation ref。中文lazy key帶snapshot generation，pointer在update後取消；English無等效CandidateRef，已知crossprefixpointer待產品slice。

同一IME窗口有界高度，長字完整semantic保留。actionLabel/icon由normalized EditorEnterPolicy，不讀EditorInfo位元。鍵帽geometry、美術與社群捐贈不更改。Symbols是現行inline/簡單emoji surface；SymbolRepository/搜尋/CLDR大型catalog及TouchResolver未實作。

## Error / tests / limitations

source unavailable與editorfailure明示；sensitive error遮蔽。CandidateBrowserUiTest涵蓋scroll/expand/close/longtext/fontScale/width、CandidateIdentityUiTest檢indexdispatch、Dachen rendering tests。Compose testTag目前不等於UIAutomator resourceID；device fixture须匹配真实可見hierarchy、不能新增production randomsemantics換green。TalkBack/OEM/實機未完整驗收。
