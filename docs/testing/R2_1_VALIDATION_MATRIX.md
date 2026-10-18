# R2.1 驗證矩陣

2026-10-16。資料以external exact receipts為準；本輪fresh核R2 finalCI，不沿用03ac數字。

| gate／source | 結果 | 證據層／限制 |
|---|---|---|
| R2 d6／actual merge3b5e084 Build38761725033 | blocking build PASS；Python130/10contracts/bootstrap/Debug；App JVM272PASS10nativeSKIP、Host107PASS | exactfinalR2 CI，非R2.1新碼 |
| R2 d6 normal App runtime | 5PASS／0FAIL／0SKIP | API33 emulator；原first/recreate斷言執行，observer OFF |
| R2 d6 normal Host runtime | 24PASS／0FAIL／9SKIP；matrix23PASS9INCONCLUSIVE＋infra1PASS | exact33 identities/protocol，不是full acceptance |
| R2 d6 finiteprobe38761725004 | App5PASS＋Host2PASS | ZH06→ZH01、source183hash一致；非Host33；一次25byte canary兩副本 |
| R2.1 new geometry oracle03b | 10tests：9AssertionError RED／1PASS | no-op新oracle缺保護；不是宣稱舊產品decoder缺陷；03sandbox socket失敗不算RED |
| R2.1 finite geometry routing04→05 | 1 assertion RED→focused suite14PASS | fake subprocess，App全前置不被filter；131全Python另列 |
| review regression10 | 16tests：9AssertionError RED | missingcoverage分類與rotationeffect反例，不是syntax/import |
| earlyGREEN11／12 | 各2FAIL | 真clip檢查被coverage不足遮蔽；保留FAIL，修正先驗frame，不弱化expectation |
| 12b Host | 123PASS／0fail/error/skip；instrumentation compile PASS | 真執行；13僅UP-TO-DATE recheck，有持久exit0 |
| final17 Host | 123PASS／0fail/error/skip；instrumentation compile PASS | 最後frame helper exactbytes真執行，9原XML外凍結 |
| local16 App＋fourassemble | App282PASS／0skip（含10host-native）；Debug/Host/AppTest/HostTest assemble PASS | JNI沿用R1已hash binary，非本輪重建；詞庫新public cache pin3587ba3 |
| local07 Python／08 contracts | 131PASS／10PASS | 全量fresh，未和Gradle同時跑fake-process tests |
| local09 bootstrap | PASS，pin3587ba3355711f0aca50136e787719f6562676b8 | 新獨立cache，只public assets；未碰私人字典 |
| source／frozen assets18 | protected diff0；公開AIE APK、v2/v3 ZIP exactSHA不變 | 真hash上述3資產，不聲稱本輪重算1185 archive |
| B01有限38825146067／b1b5bf6 | attempt1 SDK FAIL；attempt2 App4PASS1FAIL，Host/B01 NOT_TESTED | 已核185source／App5完整protocol；原first visibility kt81 FAIL，不再retry／不跳前置 |
| physical | NOT_TESTED | read-only inventory無裝置；沒有實機install/IME selection |
| OCR1.13.0 | BLOCKED_BY_APPROVAL_REVIEW | llm test未執行；歷史401不是本輪provider結果 |
| official-code-review | BLOCKED／NOT_EXECUTED_PIPELINE | specified model harness不可用、PR12 Draft；不假稱approval |

R2 CI仍有既有KSP/AWT背景NullPointerException annotation，後續BUILD SUCCESS且XML0failure；pin2.3.5未變，不隱藏、不在此片升級。最終R2 CI／新B01 CI／local環境不同Debug signer與APKbytes各自記identity，不假稱同一binary或可更新公開APK。

LSP diagnostics API未提供；使用Kotlin compiler／unit／static／diff替代，但不虛構LSP執行。文件links/JSON/YAML/diff待本輪文件定稿後記錄。
