# R2 階段交接

目前為diagnostic preparation checkpoint，任務尚未完成。下列事實以本輪source、command logs及XML為準。

- MAIN_SHA：63c9e4ab8ad7e277013acb5d7cce99228ec1f827。
- PR10 remote SHA：ea5c2b5b3404508c8ab6b0a69642b3a54a8cb565，OPEN／HOLD。
- reviewed local Host SHA：4fc43ffe29a361ea7916abf0e319bdf42dea8eb6。
- R2 worktree：`/Users/huang/KU-Yin_keyboardforandroid/.worktrees/r2-ime-visibility-runtime`，branch `fix/r2-ime-visibility-runtime`；baseline63c9＋local fixture正常merge fa0518f815dea4271bd03423de41fbd9220b8de5。
- ROOT_CAUSE：UNRESOLVED，不能稱flaky；historical ea5 first visibilityfalse，recreate／Host未執行。
- PRODUCT_CHANGED：NO；app/host main、pins、Gradle身份、release/signing不改。18原工區heads/status/diffhash重新核對一致，R2新增後19工區。
- fresh local：Python120、contracts10、App JVM273含真host-native10、Host JVM97強制rerun、bootstrap／四APK編譯PASS。細分證據见[validation](R2_VALIDATION_MATRIX.md)。
- emulator：A/B/C尚未執行；physical OPEN；production NOT_READY；M4 NOT_VALIDATED；S2 NOT_STARTED。
- release v0.2.0-aie.1與exact assets保存；本地四個新test APK不能冒稱frozen release binaries。

## 下一個依賴順序

1. 診斷source independent review及draft CI source checkpoint。
2. 同API33/default/x86_64/pixel7、fresh emulator/user0的A原main、B main＋observer、C local PR10＋相同observer；save source/APK/signer/raw XML/现场trace。
3. 找first divergence並支持／反證hypotheses；必要test-only repair先RED→GREEN。若真product defect則另隔離產品修正，不在此偷改。
4. App5 prerequisite後Host32；逐案更新[case dispositions](R2_HOST_CASE_DISPOSITION.md)，原9FAIL→SKIP不能算repaired，保留原8SKIP observer gaps。
5. 最後actual-evidence獨立review及PR10裁定；不自動merge／release／M4。

外部evidence：`/Users/huang/Desktop/KU-Yin-R2-Verification-2026-10-13/`。讀本檔＋[root cause](R2_VISIBILITY_ROOT_CAUSE.md)＋validation即可續作，必要時才讀exactsource／raw evidence，不重讀全repository歷史。
