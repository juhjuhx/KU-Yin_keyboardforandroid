# R2.2 驗證矩陣

本輪結果；source checkpoint `3115002da5fbb5e0344e6bb3b2068dc9c145801b`，基線`2b8e8e4efff73741319fb00daf4cb4e58101e4e8`。本文件記本輪直接證據；historical R2/R2.1不覆寫。

外部證據根目錄：`/Users/huang/Desktop/KU-Yin-R2.2-Verification-2026-10-16/`；目錄日期是開始日，不代表所有收據都同一日。CI各自使用GitHub時間、裝置事件各自用epoch/uptime，不跨clock直接推先後。

| Gate | Fresh結果 | 證據與邊界 |
|---|---|---|
| Python全量 | 151 PASS／exit0 | 29-all-python-final.json/log，103.654s，六source檔snapshot28全程不變 |
| Regression RED | 有效assertion RED | 12原基線3案、13 package/primary、20b user、24 checkpoint、27 wrong-user cleanup；watchdog ERROR不算RED |
| Current protocol／proto | 8 focused PASS，含全量151 | current App5／Host33精確identity／32case owner；frozen26不改。fake不是runtime |
| Static contracts | 10 PASS／exit0 | 23-static-gates.json；final guard後30再核shell/YAML/docs/diff/source固定 |
| Protected Android sources | diff0 vs2b8 | app main/debug/androidTest、Host所有src、pins/build/signing/schema、frozen runner/manifest未改 |
| Original worktrees | 20／20 unchanged | 36-preservation-during-CI.json：HEAD/status/diff/cached；本輪新增隔離工區後總21 |
| 指定凍結資產 | 3／3 exacthash | 公開AIE APK、v2ZIP、v3ZIP；不是全部archive的重新盤點 |
| Independent source review | PASS_FOR_BOUNDED_EXPERIMENT | exact六source檔＋RED/GREEN；只准一次A/B，不是runtime／merge批准 |
| Android build | CI step6 SUCCESS | run39005467151 exact3115002一次建四APK；sharedguard與兩組phase metadata均相符 |
| A App runtime | 4 PASS／1 FAIL／0 SKIP | exact5，first visibility kt81失敗；完整protocol＋XML，Host未執行 |
| B App runtime | 4 PASS／1 FAIL／0 SKIP | exact5，完整protocol／terminal -1；raw shell0仍由strictparser判FAIL |
| B Host／B01 | NOT_TESTED | App前置FAIL，未安裝Host／未跑33或B01；32case全NOT_TESTED |
| Physical | NOT_TESTED | 本輪只有受控CI emulator，沒有實機安裝或資料讀寫 |

## 不得推論

CI step success不是JUnit全PASS；Host協定exit0也可能仍有INCONCLUSIVE；每個owner與完整scope必須分開核。A/B有installer、runner transport及順序負載confounds，即使AFAIL/BPASS也不能唯一證明replacement。full IMMS文字dump會呼叫client，關鍵時間窗只用server proto；proto沒有remote method/session欄位，不能把欄位缺席視為null。

原R2.1根因UNRESOLVED。本輪環境在install前已SystemUI ANR，windowfocus=false／IC0，與舊focus/IC1的remote失敗不同；分類EMULATOR_ENVIRONMENT。不再跑C或盲retry。237 source／252artifact hashes及original protocol由主agent回核，runtime獨立review因quota未完成。詳[逐界線裁定](R2_2_RUNTIME_FINDINGS.md)與[最終交接](R2_2_FINAL_HANDOFF.md)。PR12 Draft、PR10 HOLD，physical／production／M4／AIE-S2 gates未解除。
