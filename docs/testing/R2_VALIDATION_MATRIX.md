# R2 驗證矩陣

| 層 | 來源／狀態 | 本輪結果 |
|---|---|---|
| R1 archive | 569＋616 hashes | 1185一致，0mismatch |
| R2 baseline Python | main＋reviewed local Host | 113測，結果以external命令為準 |
| 歷史 App/Host CI | R1 raw XML/log | App5／Host15PASS9FAIL8SKIP；非R2 fresh |
| ea5歷史 | exact42fb9afb merge | App4PASS1FAIL／Host未跑 |
| debug observer | Robolectric real EditText／final EditorInfo | RED 4測3 assertion failures；GREEN 4/4 |
| failure boundary | diagnostic IOException／automation failure | RED 4測2 assertion failures；GREEN 4/4 |
| CI user／failure採證 | fake subprocess／ADB | 原script 3測3 assertion failures；新regressions逐項RED；10focused GREEN |
| Python | 新、舊runner／protocol／CI tests | 120 tests PASS，exit0；fake mutation，非裝置proof |
| contracts | 全部10個check_*.py | 10/10 PASS，exit0 |
| App JVM | 56 classes／273 tests | 273 PASS／0failure／0error／0skip，含真host-native10案 |
| Host JVM | 6 classes／97 tests | 強制rerun：97 PASS／0failure／0error／0skip |
| native bootstrap | 專用新cache；pin3587ba3 | PASS，exit0；非fresh Rust rebuild |
| Debug builds | App＋Host／兩個androidTest | 四APK assemble PASS；非frozen／release APK |
| source preservation | main protected paths | product／host main／pins／Gradle身份設定diff空 |
| shell／workflow | bash syntax／YAML／embedded Python | PASS；不是CI runtime proof |
| R2 A/B/C emulator | 未執行 | NOT_RUN |
| physical | 無授權實機session | OPEN／NOT_TESTED |

不能把UNIT_PASS／INTEGRATION_PASS／EMULATOR_RUNTIME_PASS／PHYSICAL_DEVICE_PASS混用；release signing／M4／S2均不在本輪。

## 原始證據

外部 `/Users/huang/Desktop/KU-Yin-R2-Verification-2026-10-13/`：`01b-*`有效observer RED、`04-*`GREEN、`05-*`boundary RED、`09-*`8測GREEN＋instrumentation compile、`11-full-local-gates.log`／unit-summary／local-artifacts、`14-host-fresh-unit.log`強制24tasks、`15-final-python.log`120測、`12-static-results.json`。後者Python119是中間版；final為120。

早期observer tests有一次NoSuchElement的不合格RED、兩次fake-process timeout；全數保留，不作有效assertion RED。修正oracle後的01b RED只含3 AssertionError。早期compile因SDK env缺失及尚未bootstrap失敗，06/06b日志保留，補實際SDK/pinned deps後09成功；不稱產品缺陷。Native JNI沿用已hash核對的R1 host binary，真JNI測試本輪執行，不能稱本輪重建該binary。

OpenCodeReview v1.12.13：本輪單次provider health exit1／HTTP401，BLOCKED_BY_PROVIDER，非APPROVE。
