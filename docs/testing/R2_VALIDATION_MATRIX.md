# R2 驗證矩陣

## 最新checkpoint：3895625／2026-10-15

| 證據 | 實際結果 | 界線 |
|---|---|---|
| 51/51b→52e focus TDD | 8個Robolectric GREEN；2 initial＋destroy有效RED | lose-focus舊oracle先show後失焦，不計有效RED；新oracle明確排隊失焦在前 |
| 53→54 raw shell TDD | 5測4有效AssertionError RED→5GREEN；連同focus/boundary共17GREEN | 真host shell process，非AndroidRW證明 |
| 55 full gate | App282中3native FAIL；其餘App/Host及四APKassemble完成 | 舊host data目錄空，保留FAIL；不改assertions |
| 57 bootstrap | 新獨立cache pin3587ba3、4nonempty詞庫、14publichashes unchanged | hostJNI仍R1已hashbinary，非fresh JNI rebuild |
| 59 premature gate | FAIL，cache尚未完成 | 編排錯誤保留；不是產品診斷 |
| 59b full gate | App57classes282真執行，0fail/error/skip，含native integration10 | Host107 UP-TO-DATE；四APKassemble UP-TO-DATE，真正新selfcheck compile見55 |
| 56 Python／58 contracts | 130PASS／10PASS，exit0 | fake ADB不代替device runtime |
| source reviews | focus＋rawdump＋59b前提獨立窄批准，無未解High | 不批准完整runtime／ea5根因／PR10 |
| 38660489452 | exact3895625 App5PASS→ZH06/ZH01皆PASS；0skip | AndroidRW fixedpayload驗證；只有限scope，fullHost待跑 |

以下為各切片當時結果；不得把舊PASS當最新source重跑，或把以前RUNNING當現在狀態。

## 歷史切片

| 層 | 來源／狀態 | 本輪結果 |
|---|---|---|
| R1 archive | 569＋616 hashes | 1185一致，0mismatch |
| R2 baseline Python | main＋reviewed local Host | 113測，結果以external命令為準 |
| 歷史 App/Host CI | R1 raw XML/log | App5／Host15PASS9FAIL8SKIP；非R2 fresh |
| ea5歷史 | exact42fb9afb merge | App4PASS1FAIL／Host未跑 |
| debug observer | Robolectric real EditText／final EditorInfo | RED 4測3 assertion failures；GREEN 4/4 |
| failure boundary | diagnostic IOException／automation failure | RED 4測2 assertion failures；GREEN 4/4 |
| CI user／failure採證 | fake subprocess／ADB | 原script 3測3 assertion failures；新regressions逐項RED；12focused GREEN |
| Python | 最新49全量／真guard／runner/protocol | 130 tests PASS，exit0；fake mutation，非裝置proof |
| contracts | 全部10個check_*.py | 10/10 PASS，exit0 |
| App JVM | 45 fresh --rerun-tasks／56classes273 | 273 PASS／0failure／0error／0skip，含真host-native10案 |
| Host JVM | 45 fresh --rerun-tasks／8classes107 | 107 PASS／0failure／0error／0skip |
| native bootstrap | 專用新cache；pin3587ba3 | PASS，exit0；非fresh Rust rebuild |
| Debug builds | App＋Host／兩個androidTest | 四APK assemble PASS；非frozen／release APK |
| source preservation | main protected paths | product／Host main／pins／身份diff空；唯一test-only UiAutomator scalar2.3→2.4 |
| shell／workflow | bash syntax／YAML／embedded Python | PASS；不是CI runtime proof |
| R2 A/B/C emulator | source7f／synthetic698d；run38459179007 | App各5PASS；Host A/B15PASS9FAIL8SKIP，C15PASS17SKIP；workflow FAIL |
| 普通CI emulator | source7f／synthetic698d；run38459178799 | App5PASS；Host15PASS17SKIP；不是完整acceptance |
| 9f有限probe／普通Build | 38524583127／38524582991 | probe App5／ZH01PASS／C04INCONCLUSIVE；full App5PASS／Host15PASS17SKIP |
| physical | 無授權實機session | OPEN／NOT_TESTED |

不能把UNIT_PASS／INTEGRATION_PASS／EMULATOR_RUNTIME_PASS／PHYSICAL_DEVICE_PASS混用；release signing／M4／S2均不在本輪。

## 原始證據

外部 `/Users/huang/Desktop/KU-Yin-R2-Verification-2026-10-13/`：`01b-*`有效observer RED、`04-*`GREEN、`05-*`boundary RED、`09-*`8測GREEN＋instrumentation compile、`11-full-local-gates.log`／unit-summary／local-artifacts、`14-host-fresh-unit.log`強制24tasks、`15-final-python.log`120測、`12-static-results.json`。後者Python119是中間版；final為120。

早期observer tests有一次NoSuchElement的不合格RED、兩次fake-process timeout；全數保留，不作有效assertion RED。修正oracle後的01b RED只含3 AssertionError。早期compile因SDK env缺失及尚未bootstrap失敗，06/06b日志保留，補實際SDK/pinned deps後09成功；不稱產品缺陷。Native JNI沿用已hash核對的R1 host binary，真JNI測試本輪執行，不能稱本輪重建該binary。

OpenCodeReview v1.12.13：本輪單次provider health exit1／HTTP401，BLOCKED_BY_PROVIDER，非APPROVE。

## 最新來源9f6d2e8的device-free gate

Python122PASS（22 log）、Host107PASS（26 XML；8classes，0fail/error/skip）、26b最後close-order instrumentation compilePASS、27全contracts10PASS、diff/shell/YAML PASS。App273含host-native10先前本輪在7f source執行；App code在9f相同，不能把它寫為9f重跑。CI7f App JVM273中263PASS／10SKIP（Linux未開host-native）；Host97PASS。每一層均保留source與skip差異。

19b trace RED中3個AssertionError為有效RED，1個NoSuchElement oracle錯誤不算；23 owner retention1 assertion RED；21 probe2 assertion RED；25 import compile錯誤不算RED，25b雙cleanup4測3 assertion RED。最後26 trace6＋cleanup4全部GREEN。診斷本身的unit PASS不證明Host typing runtime。

## 最新test-only依賴修復

| 層 | Exact source／證據 | 結果 |
|---|---|---|
| origin probe | 74193b2／38529968625 | ZH06→ZH01均INCONCLUSIVE；BODY／CLEANUP QUERY_REGISTER stale |
| identity RED | 8c7d318／38561946857／2.3.0 | App5PASS，infra1有效assertion FAIL（existing map查找StaleObjectException） |
| identity GREEN | 3fc2c89／38563163760／2.4.0 | App5PASS，same infra1PASS；getter-stale原assert保留，181source hashes一致 |
| production dependency graph | 41b→42 App；42c→42b Host | runtime graph前後完全相同；2.4僅Host androidTest |
| test dependency compile | 42／48 | Host instrumentation真正compile；四APKassemble PASS |
| fresh JVM/native | 45 exactXML／R1 hostJNI hash核對 | App273＋Host107，0fail/error/skip；非本輪重建JNI |
| Python/contracts | 49／43 | 130PASS／10PASS；真bootstrap guard46 RED→47七測GREEN |
| soft-key sequence | 3fc2c89／38563975932 | App4PASS1firstVisibilityFAIL；Host未執行；焦點前置缺陷及raw缺口另立修復 |
| 當時full new CI | 3106139 source尚未推PR12 | 該checkpoint未執行；現在應以3895625 lineage的新normalCI為準 |

Host現在33個JUnit identities＝原32 case methods＋1infra regression；case envelopes仍32；frozen P1-E26不改。41首次JAVA env缺失、42首份empty-parser、36首份import錯誤均保留且不當有效證據。Local與CI APK bytes不同，各自manifest，不冒稱binary-identical。
