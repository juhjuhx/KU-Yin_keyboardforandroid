# R2.2 IME remote binding 深讀

基線2b8e8e4efff73741319fb00daf4cb4e58101e4e8；main63c9e4ab不改。已依AGENTS讀current/architecture/development/security/upstream與R2/R2.1交接、兩ADR，原失敗38825146067 attempt2：App4PASS1FAIL、Host/B01未執行。

## 已定位的控制邊界

`scripts/runtime_smoke.sh`：CI/emulator/qemu/user0 fence → native bootstrap → `:app:installDebug` → ime enable/set → before-app採證 → `:app:connectedDebugAndroidTest` → 重裝/重選產品 → Host connected。Gradle connected另外擁有target/test APK install與cleanup；單靠腳本不能宣稱實際每次replacement或根因。App test與IME在同一產品package/process，Android instrumentation啟動的process lifecycle也是獨立可疑變數。

`ImeRuntimeSmokeTest.kt:69–91`：selected assertion →真ActivityScenario →原一次requestIme →40×250ms visibility oracle→原first assertion→recreate→原第二assertion。debug Activity one-shot已等windowfocus/attachment/token，但這不保證remote method/session已ready。原failed live dump已證focus/IC/served，不能套用舊early-show原因。

`KuYinInputMethodService.kt`：production onCreate/live engine、onStartInput/editor policy、onStartInputView/session state與onDestroy owner不改；不得新增production/JNI observer。現有平台dump可以觀察bound/service/inputStarted狀態，不等於逐callback tracing。缺callback證據必須明列。

## 受控比較

A：原Gradle install/select/connected順序。B：同一次建置之exact4APK先adb install，再select，以am instrument原App5執行、沒有後續package replacement。各用fresh API33 default/x86_64/pixel7 emulator/user0，observer同true、source/assertion/timeout相同。A與B runner/install orchestration共同改變，單憑AFAIL/BPASS只能支持orchestration，不足唯一歸因replacement。必要C：fresh B setup/select後explicit reinstall同appAPK，再同B instrumentation；不重跑直到綠。

採證：每安裝/選IME/測試前後epoch、pm path/dumpsys package、pidof、installed base SHA、IMMS/window/service、events與ActivityManager/InputMethodBindingController logs。只fresh CI synthetic，無private device，無userdict/DB/prefs。原失敗仍保存。

Root cause目前UNRESOLVED；本階段只新增test-only experiment routing/metadata，不當作fix。全Host33/32matrix與B01僅在當arm App5/0/0確實通過後執行。無SKIP→PASS、無newobserver、無main/PR merge/release。

## 獨立平台研究校正（2026-10-17）

外部 `R2_2_INDEPENDENT_SEQUENCE_RESEARCH.md` 與 `R2_2_ANDROID_PRIMARY_SOURCE_RECEIPT.json` 保存exact API33的12份來源片段/Git blob/SHA與AGP9.1.1 cached bytecode。已證normal `am instrument`本身有target restart語義，並非Gradle獨有；不偷加`--no-restart`改warm/cold owner契約。connected cleanup預設允許uninstall，不能由final package消失推回firstvisibility原因。原研究當時尚未取得exact worker；現已由官方Google Maven取得32.1.1 worker並獨立核實配置接線，仍不把設定層冒充實際事件。

新增完整IMMS文字dump會走client TransferPipe→IMM.isActive→checkFocus，會干擾採證。兩arm關鍵時間窗改server-only API33 proto；完整文字只在App既有觀測完成後/terminal，仍不假設零IPC耗時。proto是outer trace field3的inner IMMS，selectedID/bindingID/haveConnection/bound/inputShown等有界metadata；**沒有remote method/session/served IC/currentuser欄位**，不從缺欄位造null/false，currentuser另明讀。CRITICAL文字服務端早退已證，但exact nativeCLI未取證，本次不採用。

A App階段與B均只安App/AppTest，Host兩APK在B App通過後另裝；同job先建四APK只是共用bytes。A App-only是陰性/比較控制範圍，不替代完整Host；B同arm App5全通才接Host33/32 envelopes，再單獨B01。額外的安裝/runner transport與順序/hostload是confounds，AFAIL/BPASS不能唯一歸因replacement。C仍為依A/B結果條件啟用的授權，不盲重試。

### Exact worker 配置補充

官方 `com.android.tools.utp:gradle-work-action:32.1.1` JAR（82,663 bytes，SHA256 `d204ea49a6958bde0c457f98d854b3439a04defef992fd7dc5b74f9510d2d817`）只做javap靜態讀取，未執行。ServiceLoader實際delegate為UtpActionImpl；target App與AppTest皆送入apk installer配置，`uninstallAfterTest`與`forceReinstallBeforeTest`獨立。AGP connected接線對後者傳false；不能說所有connected都強制先uninstall，也不能說false代表不會overwrite。

外部 `R2_2_EXACT_WORKER_CONFIG_INDEPENDENT_ADDENDUM.md/json` 逐行核對bytecode與輸入hash，關閉worker配置缺口。實際host installer是否skip相同APK、device replacement與IMMS因果仍待run39005467151原始事件。Python TLS失敗收據保留；成功下載用system curl正常TLS，未停用憑證驗證。
