# KU-Yin 續作短入口

2026-10-12 R1。只需先讀[CURRENT_STATE](../CURRENT_STATE.md)、[規範ARCHITECTURE](../ARCHITECTURE.md)、[FINAL_HANDOFF](../maintenance/FINAL_HANDOFF.md)與[VALIDATION_MATRIX](../maintenance/VALIDATION_MATRIX.md)，再按最小任務讀source；不重讀數百review。

R1文件／doctor／CI PR #9正常merge `bf7c5d1d912d77ae8c6c7179d26d72bf835232bc`。文件交付後精確main在GitHub與外部`R1_FINAL_RECEIPT.json`；canonical本地checkout `.worktrees/mainline` branch main。原root與15原工區保持，新增共18；禁止reset／clean／prune。

外部archive `/Users/huang/Desktop/KU-Yin-Engineering-Archive-2026-10-12/`：569baselinehash、4dirtyclone restore PASS，536refs／470copies；private data／keys excluded，DB不改，removed0。產品39acf96、tagv0.2.0-aie.1 source2ce6810、exactAPKacdbacdf…不變。

Host PR #10 **HOLD／未merge**。baseline fresh emulator App5PASS／Host15PASS9FAIL8SKIP；087adapter Host15PASS17SKIP；ea5c2b5 CI38254806233 App4PASS1FAIL（IME visibility :72），Host未跑。來源不改App，rootcauseUNKNOWN，不能用skips或workflowSUCCESS宣稱修好。新source測試詳見fixturebranch交接。

NEXT：bounded test-infra定位IME visibility／完整capture成本，保留strict preedit與exact commit/action/newline，取得完整runtime再裁定PR10；不得盲retry、改assertions、清字典或新增decoder。True產品defect另isolatedfix。physical／OEM／TalkBack／erase／license／signing仍OPEN；productionNOT_READY、M4NOT_VALIDATED、S2未開始。

Native production＋startupfallback；MainActivitytrial另dictionary；Englishoffline10k。scratch snapshotplan/generation/ref；nativeACK早於editorACK；fullresetunsupported；Roomfeedback v1。Supplemental native stress180s TIMEOUT不當PASS。TAUI／oldbranches KEEP待owner決策。無init/compact/LSP API；不改globalCodexconfig。
