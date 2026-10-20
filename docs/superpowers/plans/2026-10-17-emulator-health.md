# R2.3 Emulator Health Implementation Plan

> 執行：主agent使用executing-plans inline實作；獨立reviewer只讀，使用者已明確授權本輪執行，不重問選項。

**Goal:** 將失效emulator阻擋於APK install之前，並以真editor證據守住binding前置。
**Architecture:** 小型Python採證／分類器，由原runtime_smoke opt-in gate接入；重用existing host editor與原protocol，沒有新Kotlin／runner framework。
**Tech Stack:** Python unittest、Bash、GitHub Actions、API33平台診斷。
**Spec:** docs/testing/R2_3_HEALTH_SPEC.md（對應使用者R2.3 prompt）。

## Global Constraints

產品／JNI／pins／signing／四APK Android來源／原5+33 assertions不變。H0/H1非HEALTHY即停止；原archive/worktrees/data保留。無自動merge、release、S2、M4。

## Review Focus

1. hidden ANR不得假可見，歷史ANR不得無證判已恢復。
2. ADB transport/timeout/空輸出與不同user不得變healthPASS。
3. H1不能接受舊case XML、錯windowfocus或fakeIC；defaultIME不變。
4. 日誌／PNG失敗不得吞掉primary錯誤或繼續install。
5. A healthFAIL不得藉continue-on-error啟動B；raw outcome不得綠化。

### Task 1: H0 pure classification與採證

Create scripts/emulator_health.py、scripts/tests/test_emulator_health.py與最小fixtures。Modify scripts/runtime_smoke.sh opt-in guard；existing fake tests擴充純fixture回應。
Interfaces: assess_h0(captures: dict)->dict；collect_h0(serial: str, evidence_dir: Path)->dict；HEALTHY exit0，UNHEALTHY exit2，INCONCLUSIVE exit3。輸入真command records；output含status/reasons/evidence paths。
- [x] 寫舊runtime遇ANR仍install的實際regression，確認Assertion RED。
- [x] 寫visible/hidden/history／healthy／missing／timeout／user/target guard tests，再最小實作GREEN。
- [x] 套用真R22 A/B before-install raw離線分類（不是fresh runtime）。

### Task 2: H1及CI接線

Modify scripts/emulator_health.py、runtime_smoke.sh與既有r2-host-observation-probe.yml；不改原Android檔。
Interfaces: assess_h1(window, hierarchy, case_id)->dict；H1讀取H0同device/boot/user記錄，launch一次後bounded condition poll；wrapper只在HEALTHY執行原pipeline。
- [x] RED驗證錯焦點／舊XML／nullIC／hidden與visible對話框／IME漂移必須阻擋。
- [x] GREEN fake subprocess；source APK hash保護／user再驗／primary errors保留。
- [x] binding job補回原KVM grant，記錄actual env，A healthFAIL阻B。shell/YAML checks。

### Task 3: 驗證／獨立review／一次CI／交接

- [x] 全Python、10contracts、docs/shell/YAML/JSON、protected source/archives/worktrees驗證。
- [x] fresh-context reviewer讀actualdiff/RED/raw與環境對照；重大finding修正有RED/GREEN。
- [x] local atomic commits／正常測試分支push，一次受控CI。H0/H1不通則停止，H2 NOT_EXECUTED；不盲retry。
- [x] 若健康才讓同次source/四APK受控A/B，分開H0/H1/H2與Host結論。
- [ ] canonical anchor／最終收據／handoff，無自動merge或release。

實作判定：使用既有project plan與外部RUNNING_CONTEXT_ANCHOR保存狀態；沒有執行skill helper／私有workspace刪除。H1改用既有exported host，避免繞過debug Activity exported=false；代價是binding對照兩arm都新增相同host前置，須把時序confound明記，不能單因果宣稱。

最終：CI39047679642 H0 INCONCLUSIVE，故H1/H2 NOT_EXECUTED，B skipped。Runtime review NEEDS_REVISION（OPEN P2 frozen guard），依使用者STOP只記錄，不修碼或重跑；source-only批准不推廣。Local181/CI181與10contracts PASS，buildtasks完成；JVM個別XML未封存count UNKNOWN。後續只有docs／保存收據收束，不開始下一slice。
