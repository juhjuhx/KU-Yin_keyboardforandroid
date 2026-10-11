# P1-E 30-case 裝置驗收矩陣

`DEVICE_SEQUENCE=NOT_EXECUTED`。本輪無裝置，以下30個 case 的實際結果全部 `NOT_TESTED`。已編譯／打包／pure test 數量不能代填任何一列 PASS。PHYSICAL 與 EMULATOR 分別保存；完整 physical acceptance 仍 OPEN。

結果只有 `PASS / FAIL / INCONCLUSIVE / NOT_TESTED`。metadata readiness、soft-key UI observation、editor side-effect 與 native learning scope 各自留證。runner 的 `observable_result` 可表示某個可觀察 surface 通過；`full_acceptance_result` 必須保留該 case 完整契約仍缺的範圍。scope 缺口不是 mode test 自動 FAIL，也不是 learning physical PASS。

| ID | Case／配置 | 預期與必要證據 | 自動／人工／已知界線 | 實際結果 |
| --- | --- | --- | --- | --- |
| A01 | 冷啟動、正常 dict／ABI、新 service lifecycle | 鍵盤出現、可真 soft-key 組字、無 crash；另證 production native readiness | 既有 native smoke 是 direct native；bind test 不完整證 cold startup。不能將5/5挪作本列完整 PASS | NOT_TESTED |
| A02 | editor recreate 後再次 focus/show | host recreated 後 IME 可見／再輸入；記 API、可見性證據 | 既有 IME recreate smoke。API24–29 isAcceptingText 只證接受 full text edits，另需 visible UI | NOT_TESTED |
| A03 | 真實 physical rotation；組字中／完成後各轉向、再輸入 | actual display rotation 與 host orientation；無 crash／卡死／重複 commit/action/newline；可繼續輸入 | 人工實機 actual rotation。recreate 不等同；組字保留／清除僅 observation，未新增 preservation 契約 | NOT_TESTED |
| A04 | 受控 startup factory／init failure | fallback 可輸入、cleanup/retry 合 P1-A | OPEN：本輪沒有 test-only fault injection fixture；不改 frozen product 注入 fault | NOT_TESTED |
| B01 | 大千 portrait／landscape，至少兩種寬度；真 tap ㄕ／ㄖ | 41 phonetic/tone keys 合 canonical；hitbox 可點且 preedit 確實對應 | UIAutomator／人工 layout evidence；preedit chip 須與鍵面文字分辨，不能只 By.text(ㄕ/ㄖ) | NOT_TESTED |
| B02 | s/u/3 真 soft keys → candidate tap → key1 | selection 後繼續 composition；無 reset／context 更換、無額外 commit | 真 UI observable 可記；native context identity／reset count 未由 host 暴露，direct native 證據另層 | NOT_TESTED |
| B03 | s/u/3/c/l/3 真 soft keys → candidate 修正 → Enter | 同句 composition、conversion／correction；editor commit count；final清空與續輸入正常 | 記所有 editor deltas；不以 direct native 結構非空或方法名稱代「恰好一次完整 commit」 | NOT_TESTED |
| C01 | inputType=TYPE_CLASS_NUMBER(2) | final metadata、temporary ENGLISH、不可注音組字；native learning disabled | observable mode/composition 可測；native learning observer OPEN，不能只憑英文畫面宣稱learning已證 | NOT_TESTED |
| C02 | inputType=TYPE_CLASS_PHONE(3) | 同 C01，記實際 action／mode／metadata | native learning observer OPEN | NOT_TESTED |
| C03 | inputType=TYPE_CLASS_DATETIME(4) | 同 C01 | native learning observer OPEN | NOT_TESTED |
| C04 | inputType=TYPE_NULL(0) | super 真 null IC、final metadata與presence；forced editor行為另觀察 | null connection 不造假；系統 raw/dummy／show/bind 前提可能不同，不能量測時 INCONCLUSIVE；native learning OPEN | NOT_TESTED |
| C05 | preferred ZHUYIN → C01–C04 →普通text；另測 preferred ENGLISH | 返回 text 恢復使用者偏好；ENGLISH 偏好不被強制改 ZHUYIN | UI preferred/effective mode 可觀察；native learning restore scope observer OPEN | NOT_TESTED |
| D01 | IME_ACTION_NONE(1)，empty composition | newline delta=1、action count delta=0 | final controlled metadata 確認 action=1；真當前 Enter | NOT_TESTED |
| D02 | IME_ACTION_UNSPECIFIED(0)，empty composition | newline delta=1、action delta=0 | super platform可能改DONE/NEXT；必須 final outAttrs 真覆寫0，不能只 diagnostic 顯示0 | NOT_TESTED |
| D03 | GO(2) | 標示前往；action ID=2、count delta=1、newline delta=0 | primitive final metadata＋真 inline action＋host recorder | NOT_TESTED |
| D04 | SEARCH(3) | 搜尋；ID3/count1/newline0 | 同 D03 | NOT_TESTED |
| D05 | SEND(4) | 傳送；ID4/count1/newline0 | 同 D03 | NOT_TESTED |
| D06 | NEXT(5) | 下一步；ID5/count1/newline0 | host自己消費 action，不讓 TextView 預設focus跳轉污染量測 | NOT_TESTED |
| D07 | DONE(6) | 完成；ID6/count1/newline0 | host自己消費 action，不以鍵盤消失作 action 正確證據 | NOT_TESTED |
| D08 | PREVIOUS(7) | 上一步；ID7/count1/newline0 | 不只看是否focus移動 | NOT_TESTED |
| D09 | SEND＋NO_ENTER_ACTION(0x40000000) | newline1/action0，不派送SEND | 同時記 masked action 和 flags，避免誤把SEND單欄當契約 | NOT_TESTED |
| D10 | GO＋NO_ENTER_ACTION | newline1/action0，不派送GO | 同 D09 | NOT_TESTED |
| D11 | custom label確認／ID73；masked SEND | 顯示確認，ID73/count1/newline0，不送4 | setImeActionLabel＋finalcontrolled label/id；只一個action recorder，不delegate action | NOT_TESTED |
| D12 | 同 D11＋NO_ENTER_ACTION | newline1/action0，不派73/4 | custom label不能繞過 NO_ENTER_ACTION | NOT_TESTED |
| D13 | 真非空注音 preedit＋SEND | 第一次Enter只完成組字、action0/newline0；第二次Enter才ID4/count1/newline0 | 按Enter前須辨認preedit chip。未辨明或selector不唯一則INCONCLUSIVE，不用hardwareEnter補PASS | NOT_TESTED |
| D14 | 真非空preedit＋SEND／NO_ENTER_ACTION | 第一次只完成組字、newline0/action0；第二次newline1/action0 | 同 D13；先baseline，再分兩次side-effect增量 | NOT_TESTED |
| D15 | explicit TYPE_CLASS_TEXT／MULTI_LINE(0x20001)；SEND／NO_ENTER_ACTION | newline1/action0，不派SEND | 同時保存platform與finalmetadata；multiline 的 flag由宣告case明示，不靠外觀推定 | NOT_TESTED |
| D16 | custom label換行／ID73，無NO_ENTER_ACTION | 必須顯示文字action「換行」，不是newline icon；派73一次、newline0 | label內容不等同Enter behavior；核對text/description/presentation與side effect | NOT_TESTED |
| D17 | advertised SEND；host performEditorAction return false | host收到ID4/count1、回false、newline0，無第二effect | observable hostfalse可測；oneway Binder不回editorboolean，IME同步false propagation scope INCONCLUSIVE；JVM nofallback另證 | NOT_TESTED |
| D18 | ZHUYIN／ENGLISH／SYMBOLS，emptycomposition | 三布局同 normalized policy；每次action或newline只有一次 | 三次真layout switching與各自metadata／label／delta；不可只一布局PASS推三布局 | NOT_TESTED |

## 執行入口與判讀

依 [08-device-runbook.md](08-device-runbook.md) 先固定 serial、四 APK、原 IME raw state；existing5tests後才hostsuite，finally restore。per-case emit 只代表當次 scope；JUnit skip/assumption／BLOCKED_SELECTOR 記 INCONCLUSIVE，沒有 emit 記 NOT_TESTED。不得將5個既有 instrumentation PASS、host suite 的完成 testcount、Gradle success或離線case定義數量轉換成30列 PASS。

所有 action cases 必須測 editor boundary ID/count，newlines 以 editor 實際文字數計 delta；host 記錄配置的boolean而不把 action delegate TextView。commit evidence 來自真IC callback與實際EditText，不自己拼接文字。C類 native learning值沒有product observer，physical gate維持OPEN；A04 fixture缺口維持OPEN。EMULATOR UI結果可保存，但physical欄仍NOT_TESTED。

## 每 case 觀察模板

以下另存 repo 外 evidence-dir；只填固定 synthetic 測試內容及必要 metadata，不記真實使用者輸入。

```text
CASE_ID:
RESULT: PASS / FAIL / INCONCLUSIVE / NOT_TESTED
OBSERVABLE_SCOPE:
FULL_ACCEPTANCE_SCOPE:
SCOPE_LIMITS:
DEVICE_KIND: PHYSICAL / EMULATOR
SERIAL:
MODEL:
API:
ABI:
PRODUCT_SOURCE_SHA: 75b93a3b68867fd6ec4c6adadd3e279d71e98104
FIXTURE_SOURCE_SHA:
ARTIFACT_MANIFEST_SHA256:
APP_APK_SHA256: b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9
APP_TEST_APK_SHA256: 3938b3e3c1fb711ed6141d491a3841839e098dfb2c2ae81924b94f0d6106b8bd
HOST_APK_SHA256:
HOST_TEST_APK_SHA256:
CASE_CONFIG:
PLATFORM_EDITORINFO_INPUTTYPE_IMEOPTIONS_LABEL_ID:
FINAL_EDITORINFO_INPUTTYPE_IMEOPTIONS_LABEL_ID:
INPUT_CONNECTION_PRESENT:
CASE_METADATA_GENERATION:
INITIAL_PREFERRED_MODE:
INITIAL_EFFECTIVE_MODE:
INITIAL_PREEDIT_NONEMPTY_AND_CHIP_EVIDENCE:
SOFT_KEY_SELECTOR_AND_MATCH_COUNT:
OBSERVED_ENTER_TEXT_DESCRIPTION_ICON:
ACTION_ID_DELTA:
ACTION_COUNT_DELTA:
HOST_ACTION_RETURN_BOOLEAN:
NEWLINE_COUNT_DELTA:
COMMIT_COUNT_DELTA:
FINAL_PREEDIT_EVIDENCE:
FINAL_MODE:
ACTUAL_DISPLAY_ROTATION_BEFORE_AFTER:
HOST_ORIENTATION_BEFORE_AFTER:
LEARNING_POLICY_EVIDENCE: OPEN（本輪無native observer）
CRASH:
EVIDENCE_PATH:
RESTORE_RESULT:
```

runner 產生的個別case JSON 必須與同目錄 run-summary／commands.jsonl／artifact context 一起判讀，不單獨抽出某個 PASS 宣稱完整physical驗收。A03未核定組字旋轉保存政策，只記 observation；D17與C04的不可觀察界線不能透過改測試或假connection抹除。
