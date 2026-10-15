# R2 階段交接

2026-10-15 active；Host recovery尚未完成。必要入口：本檔、[驗證矩陣](R2_VALIDATION_MATRIX.md)、[根因](R2_VISIBILITY_ROOT_CAUSE.md)、[lifecycle](R2_ANDROID_IME_LIFECYCLE_TRACE.md)、[依賴窄修復](R2_UIAUTOMATOR_DEPENDENCY_REPAIR.md)。歷史R1文件不改。

## 來源

| 用途 | 精確來源 |
|---|---|
| main | 63c9e4ab8ad7e277013acb5d7cce99228ec1f827 |
| PR10 OPEN/HOLD | ea5c2b5b3404508c8ab6b0a69642b3a54a8cb565 |
| local reviewed Host | 4fc43ffe29a361ea7916abf0e319bdf42dea8eb6，已正常merge進R2，未merge main |
| R2 source checkpoint | 3895625df8a70647de599652b58c268a10b5613b |
| R2 branch/worktree | fix/r2-ime-visibility-runtime；/Users/huang/KU-Yin_keyboardforandroid/.worktrees/r2-ime-visibility-runtime |
| Draft PR12 remote | 9f6d2e8bb4de0604a378120c1ffb6c6465eb8fb9；尚未更新新修正 |
| 有限實驗 branch | test/r2-focus-dump-preconditions，exact3895625 |
| 產品／tag | 39acf96c6cfc058106315f3553e1c8b88438f4f7／v0.2.0-aie.1→2ce6810684fd65a65a3006a224dc786b1f3a5f62，不變 |

checkpoint不是包含本交接文件的最終commit，不混稱。後續doc-only SHA由Git取得。

## 已證明與未證明

1. 受控A/B/C38459179007：三組App5PASS；Host A/B15PASS9FAIL8SKIP、C15PASS17SKIP；workflow正確FAIL。三組product source及signer相同，但APK/native ELF bytes不同，不是binary-identical；不同run的簽章須另核對。
2. 9f full38524582991仍App5PASS／Host15PASS17SKIP。741 origin probe將失敗定位QUERY_REGISTER；FAIL→SKIP不算修復。
3. 真Activity destroyed-node identity regression：2.3 run38561946857有效StaleObjectException RED；same test2.4 run38563163760 App5＋infra1PASS。getter-stale assertion保留，只證明此primitive。App/Host production graph相同；只Host test依賴改2.4。
4. same3fc run38563975932已重現App4PASS／1firstVisibilityFAIL，Host未執行。request在windowFocus=false／IC0，IMM not-served；11–17ms後focus/IC到，原10秒到期仍false。confirmed TEST_PRECONDITION_DEFECT；舊ea5曾served，完整歷史IMMS根因仍UNRESOLVED。
5. debug one-shot pending/posted after-focus修正33b3ac8：8個Robolectric GREEN，獨立source審查批准。原40×250ms期限與visibility assertion不改；兩個原show有意移到同一after-focus post，不能稱原請求時序不變。
6. OBSERVATION_DEFECT：舊LIVE_FAILURE_DUMP markers沒有raw txt，只有PNG。3895625用API31+RW stdin、shell exit＋nonempty驗證；5個JVM shell測試4有效RED→GREEN。typed transport自檢只在recreated已visible後，不能替代5個真失敗dump的runtime驗證。

## 本地最新gate

- 59b：App57classes／282 tests，0fail/error/skip，task真執行；其中NativeCandidatePaging4＋NativeTypingEditorIntegration6真JNI。Host107為UP-TO-DATE，不能稱本次fresh；45曾fresh107。
- 56全Python130PASS；58 contracts10/10PASS；57新獨立cache bootstrap pin3587ba3，14publicassets與4nonempty詞庫hash不變；59b四Debug/testAPK assemble PASS。
- 55三個native FAIL因舊host data目錄空而落入minimumdictionary；59過早啟動時cache未完成，亦FAIL。兩份失敗保留；59b只在bootstrap exit0和詞庫前置檢查通過後執行。沒有改tests／decoder迎合GREEN。hostJNI沿用已hashR1 binary，非本輪重建。
- XML、命令、錯誤與SHA在外部同目錄59b收據；instrumentation compile只代表COMPILE_READY。

## 當前最小待辦

- [x] debug focus RED/GREEN＋獨立source review；raw dump RED/GREEN＋API31+contract與source review。
- [x] pinned data cache前置恢復、full App282／contracts／Python／四APKgate。
- [x] [38660489452](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/38660489452) exact3895625：App原5PASS→有限ZH06/ZH01兩案PASS，無skip。per-test可見延後到focus/IC後show；首次IMM仍false而後WindowInsets visible，recreate亦true。typed自檢在recreated=true之後，兩份25byte檔皆exact payload。這不證明remote session當刻已ready或完整historical root。
- [ ] 有限scope實際通過後才完整Host33JUnit／32case envelopes；逐案處置舊9FAIL／8SKIP，保持strict preedit／candidate identity／exactly-once assertions。
- [ ] 對exact source CI與runtime evidence獨立覆核，再決定是否更新Draft PR12及merge建議；不自動merge PR10/main。

## 審查與保存

source-only批准與runtime批准分開。獨立報告：視窗焦點fixture修正與時序oracle獨立覆核.md（a310ab04…）、原始dump傳輸修復與自檢時序獨立覆核.md（ca740f53…）；59b額外覆核另存同外部目錄。它們都未批准fullHost／historicalroot／PR10合併。OCR v1.12.13單次401=BLOCKED_BY_PROVIDER；官方指定review harness不可用。

PRODUCT_CHANGED=NO：app/Host main、native pin、身份、signing、schema不變；唯一catalog差異Host test UiAutomator2.3→2.4。1185archive hashes／536materials／18原工區及8份frozen來源檔雜湊已獨立覆核，R2後19工區；root userdirty／M4不動。未刪字典／DB／prefs或Release，採證限fresh disposable synthetic CI emulator。

PR10 HOLD、PR12 Draft；physicalOPEN/NOT_TESTED、productionNOT_READY、M4NOT_VALIDATED、S2NOT_STARTED。尚無完成Host recovery或正式發布的裁定。

外部證據：`/Users/huang/Desktop/KU-Yin-R2-Verification-2026-10-13/`；`RUNNING_CONTEXT_ANCHOR.json`機器入口。CI與local APK/source/signer各自manifest，不稱binary-identical。
