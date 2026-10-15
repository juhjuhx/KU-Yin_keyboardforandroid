# KU-Yin 續作短入口

2026-10-15，R2已完成本輪test-infrastructure修正與完整emulator evidence audit；驗收仍PARTIAL／PR10 HOLD。先讀[CURRENT_STATE](../CURRENT_STATE.md)、[規範ARCHITECTURE](../ARCHITECTURE.md)、[R2交接](../testing/R2_FINAL_HANDOFF.md)，再讀必要source及精確raw證據。

- main `63c9e4ab8ad7e277013acb5d7cce99228ec1f827`；PR10 remote `ea5c2b5b3404508c8ab6b0a69642b3a54a8cb565`。Draft PR12已驗證checkpoint `03ac3264b4178afccb5781fac6445920bf41dc3a`；後續doc-only HEAD以PR／外部收據核對。R2工區 `.worktrees/r2-ime-visibility-runtime`，branch `fix/r2-ime-visibility-runtime`；後續local doc-only提交與tested source分開記錄。
- 測試source checkpoint3895625：Host test UiAutomator2.4（same primitive真RED→GREEN）、debug after-focus one-shot33b3ac8（8測GREEN）、raw dump stdin3895625（5測GREEN）。main／Host main／pins／身份／signing／schema不變，未改鍵盤產品行為。
- confirmed local precondition：same3fc失敗38563975932在windowFocus=false/IC0時show→not-served；修後有限38660489452與PR有限38661678095均App5＋ZH06/ZH01 PASS。首次IMM仍false後才visible，local focus/IC不等同remote ready；完整historical ea5 served失敗因果仍UNRESOLVED。
- 正常CI [38661678107](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/38661678107)：actual synthetic merge `89f98a93b66a630327415ece25c3f4a9cb8b0c52`，完整tree＝PR03ac326。App5PASS；Host33＝24PASS／0FAIL／9SKIP（matrix32＝23PASS／9INCONCLUSIVE＋infra1PASS）。exact starts/ends/identities／32owners／terminal均核對；不是fullacceptance。
- 舊9FAIL有7案全PASS，B02/B03可見assertions完成但nativecontext仍gap；舊8SKIP只有ZH06轉PASS。B01 width/orientation、C nativelearning、D17 Binder仍OPEN；C04真nullIC後空tree，不能推temporary modePASS。詳[R2逐案裁定](../testing/R2_HOST_CASE_DISPOSITION.md)。不放寬assertions、延timeout或加production observer強行過關。
- 最新local59b App282含native10真PASS，Host107當時cached；正常CI App272PASS10nativeSKIP、Host107真PASS；Python130／contracts10／bootstrap／Debug buildPASS。CI仍有既有KSP/AWT error annotation，pin2.3.5未改，原因未在R2修復。55/59缺詞庫FAIL保留，新獨立cache pin3587ba3；非fresh JNI rebuild。
- API33 RW固定payload一個檔／一次typed事件已驗證（兩pull副本）；五個真失敗dump修後未執行，不當已驗。OCR單次401BLOCKED。source／有限runtime已獨立覆核，完整runtime裁定見交接；CI reviewer額度中斷無完整批准，KSP漂移疑點已直接diff證偽。
- 外部 `/Users/huang/Desktop/KU-Yin-R2-Verification-2026-10-13/`，RUNNING_CONTEXT_ANCHOR.json機器入口。65保存再驗18原工區HEAD/status/diff全相同，現19；公開APKacdbacdf…與tag2ce6810不變。DB／prefs／字典／keys不動，實機資料未檢查。
- NEXT：限定驗收缺口／historical disposition審查及授權實機觀察；PR10 HOLD、PR12 Draft，不自動merge/release；physicalOPEN、productionNOT_READY、M4NOT_VALIDATED、S2NOT_STARTED。沒有init/compact/LSP API，使用此短入口與compiler/static證據。
