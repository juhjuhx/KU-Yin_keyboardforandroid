# KU-Yin 續作短入口

2026-10-15 R2 active。先讀[CURRENT_STATE](../CURRENT_STATE.md)、[規範ARCHITECTURE](../ARCHITECTURE.md)、[R2交接](../testing/R2_FINAL_HANDOFF.md)與[驗證矩陣](../testing/R2_VALIDATION_MATRIX.md)，再讀必要 source/raw evidence。

- main `63c9e4ab8ad7e277013acb5d7cce99228ec1f827`；PR10 remote ea5c2b5 OPEN/HOLD。Draft PR12 remote9f6d2e8；R2 source checkpoint `3895625df8a70647de599652b58c268a10b5613b`，branch `fix/r2-ime-visibility-runtime`。只推新實驗分支 `test/r2-focus-dump-preconditions`，未合併。
- UiAutomator2.3 existing-map stale lookup有真 emulator RED38561946857；same regression2.4 GREEN38563163760。只變Host androidTest依賴，product source/runtime graphs不變。currentHost33JUnit＝32case＋1infra；frozen26不改。
- same3fc run38563975932 App4PASS／1firstVisibilityFAIL，Host未跑。show在windowFocus=false/IC0時遭平台not-served，11–17ms後focus/IC才到。confirmed test precondition缺陷；debug after-focus one-shot33b3ac8，8個Robolectric GREEN及獨立窄review批准。舊ea5曾served，完整歷史IMMS根因仍UNRESOLVED。
- raw dump舊markers沒有txt，Runtime.exec不解析`>`。3895625改API31+RW stdin＋實檔非空/exit驗證，5個JVM shell測試有4有效RED→GREEN。transport自檢只在recreated visibility已判true後執行，不影響首次/recreate觀察。Android RW固定payload roundtrip已驗證；5個真failure dumps仍未驗。
- 59b App57classes282真執行PASS，含native integration10；Host107此次UP-TO-DATE，先前45有fresh107。56 Python130、58 contracts10、57 fresh pinned bootstrap、四APKassemble PASS。55 native3FAIL因舊host data目錄空；59曾過早啟動亦FAIL，保留兩者。新cache `/private/tmp/kuyin-r2-native-20261015-587736954b` pin3587ba3，14publicassets及4詞庫hash不變；hostJNI沿用R1已hashbinary，不稱freshrebuild。
- 新有限CI [38660489452](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/38660489452) exact3895625 已完成：App原5PASS→ZH06/ZH01皆PASS，無skip。最新full仍15PASS／17SKIP，FAIL→SKIP不算修復。先判實際raw/XML，再決定是否完整33測CI。
- source窄切片已獨立批准，未有full runtime／merge批准；OCR單次401 BLOCKED。外部證據 `/Users/huang/Desktop/KU-Yin-R2-Verification-2026-10-13/`，RUNNING_CONTEXT_ANCHOR.json機器入口。
- 1185archive／536materials／18原工區保留，R2後共19；root userdirty、M4不動；DB/字典/prefs/keys不動。產品39acf96、tagv0.2.0-aie.1→2ce6810、exact公開APKacdbacdf…不變。physicalOPEN、productionNOT_READY、S2NOT_STARTED、M4NOT_VALIDATED；不自動merge/release。
