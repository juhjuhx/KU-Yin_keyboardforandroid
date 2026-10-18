# R2.2 Runtime 逐界線裁定

2026-10-17；run [39005467151](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39005467151)，attempt1，exact source `3115002da5fbb5e0344e6bb3b2068dc9c145801b`。job117316921972執行38m43s，最終FAIL。兩個arm使用fresh API33/default/x86_64/pixel7,user0，同job建同四APK。外部根目錄`/Users/huang/Desktop/KU-Yin-R2.2-Verification-2026-10-16/`保留252原始artifact檔、237檔source manifest、原CI log及45-artifactmanifest；44-analysis是主agent回核，不冒稱獨立runtime批准。

## 結果與來源

| 項目 | A：原Gradle順序 | B：先裝App/AppTest再select/direct |
|---|---|---|
| App JUnit | exact5，4PASS／1FAIL／0SKIP | exact5，4PASS／1FAIL／0SKIP |
| 失敗 | ImeRuntimeSmokeTest#imeBindsAndSurvivesEditorRecreate，kt81 first visibility | 同一identity／同一原assertion |
| terminal | Gradle1；instrumentation -1；evidence0 | shell0不等於testPASS；protocol2／capture0／identity0；instrumentation -1 |
| Host33／matrix32／B01 | App前置FAIL，NOT_TESTED | App前置FAIL，NOT_TESTED |
| before-install | KU-Yin未安裝，已有可見SystemUI ANR window | 相同 |
| timeout前local state | attached/token=true，window_focused=false，input_connection_count=0 | 相同 |
| timeout後IMMS | selectedKU，mCurId/null／remote/null／session/null，haveConnection=false | selectedKU，但bound ID/remote仍是LatinIME；session=null，haveConnection=true |
| final | connected cleanup移除app與test、default回LatinIME | app路徑／SHA保留，default仍KU |

A完整protocol：`41-controlled-AB-artifacts/r22-legacy/app-connected-results/debug/emulator-5554 - 13/testlog/test-results.log`；XML為同debug根下`TEST-emulator-5554 - 13-_app-.xml`。B raw：`r22-preinstalled/app-instrumentation.txt`，新44-A/B reparse皆保留FAIL及五真test identities。XML與protocol的時間格式／統計區間不同，不能互換為精確wallclock。兩個原assertion沒有放寬，recreate後的第二visibility分支未到達。

## 第一分歧與證據

**本輪首先違反的是emulator/editor前提，不是已證明的product remote binding回歸。**

- A `before-install-window.txt:195`、B同檔`:170`的ANR window均顯示 `Application Not Responding: com.android.systemui`、`isVisible=true`／shown surface，flags含ALT_FOCUSABLE_IM。此時package-state明記ABSENT；系統故障早於KU-Yin安裝／selection／instrumentation。
- A `final-binding-logcat.txt:280–282`、B `after-app-binding-logcat.txt:368–370`：SystemUI ANR原因皆為執行`.keyguard.KeyguardService`超時。其他system ANR也存在；更深的boot／資源原因UNRESOLVED，不把load值推成確定CPU／GPU根因。
- A同log1540、B同log1761：FIRST_VISIBILITY_OBSERVED時window_focused=false、ICcount0。ACTIVITY_CREATED→EDITOR_ATTACHED→VIEW_FOCUS_REQUEST後沒有EDITOR_WINDOW_FOCUS／INPUT_CONNECTION_CREATED／INSETS_SHOW_REQUEST／IMM_SHOW_RESULT。原after-focus one-shot尚未觸發，不是原show被remote拒絕的同一前提。
- 兩組live-failure/first-screen.png直接顯示SystemUI isn't responding覆蓋synthetic editor；first-window與原始ANR事件相符，不能把View.focus=true當window focus。沒有自動按Wait／Close app、遮蔽dialog、延長timeout或重試。
- R2.1 run38825146067 attempt2原log的events3–8則具windowfocus、實際IC1、show request與IMM false，first-screen沒有此ANR。其firstIMMS為KU binding/remote null。兩組新失敗不能拼成舊remote failure的因果證明。

B的haveConnection=true是LatinIME binding，不能冒稱KU已連上；A proto未序列化bool仍是unknown，不將null summary當false。full IMMS文字dump在原false已觀察後取得，還會呼叫client，不是原子快照或零干擾trace。

## 安裝／程序事件

A原connected在select後有`installPackageLI`（1792215057.664）、`pkg removed`及`start instr`（1792215088.414）；結束有`finished inst`與`deletePackageX`。這證明本次有package操作，不證它造成first failure。B select後log只有`start instr`（1792216196.044），沒有A那次target replacement路徑；但同樣在既存ANR下FAIL。普通am instrument也restart target，前置PID均空，不能描述為「存活的KU IME被殺」；結束kill有明確finished-inst reason。

A/B的installed base SHA皆`08ba8741c01015185c9b92e3e297dc6283432189bd72d900c25acc254e545cfb`；全部採證phase的四APK metadata與shared manifest一致。它們是新CI Debug signer `5ebb93bed6c42bdcaa6bc74ce6237b3444bf6e78ed0fafe96a3e1d8020d47d2d`，不是公開release APK或舊P1 frozen signer。沒有將CI APK提供為更新包；raw artifact不含四APK本體，hash/signer是CI採證，不能稱本機重算了CI APK bytes。

## 假說裁定

| 假說 | 支持／反證 | 裁定 |
|---|---|---|
| Gradle覆裝唯一根因 | A有覆裝；B無該覆裝仍在相同ANR前提下FAIL | 本輪不支持唯一歸因；不能排除歷史情境 |
| selected即已bound/session | B selectedKU、bound仍Latin；兩組沒有KU service match | 明確不成立 |
| 原R2.1錯在show過早 | 舊focus/IC/show已具備，新環境根本未到該點 | 不能沿用此診斷 |
| 本輪環境前提不成立 | before-install ANR、失敗圖／window／localfocus/IC0交叉相符 | EMULATOR_ENVIRONMENT，信心HIGH |
| SystemUI ANR的更深成因／舊remote root | 尚缺健康環境受控觀察及service lifecycle直接證據 | UNRESOLVED |

C不啟動：在已失效環境再加replacement不會隔離原remote問題，沒有因果價值；不是略過一個已PASS的前置。沒有原問題的root-fix RED/GREEN；本輪151 GREEN只證experiment/parser/fake safety，兩個真App仍FAIL。

## 下一最小切片

先規範disposable emulator health precondition：可見system ANR／crash／不明focus owner須fail closed、保存raw並停止install；不要自動dismiss／clear／重跑直到綠。取得健康環境與明確independent review後，再對同APK、user、image比較install與instrumentation binding。健康檢查及環境修復尚未實作；不在缺review時加入未審的shell workaround。原R2.1 remote root仍UNRESOLVED，PR12 Draft／PR10HOLD，無product／JNI修補或實機PASS。
