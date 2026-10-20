# R3.2 H1 最終交接

## 來源與執行

Base main `8369da25fa54ab762dfceed457e71a08767a642e`。分支 `fix/r3-2-h1-runtime`；TEST_ONLY code/runtime checkpoint **`8acf4b24c63c8c8474e8ea95b50054977902ecaa`**。本文件為其後docs-only checkpoint；不冒稱最後文件HEAD重新跑Android。

一次manual [CI39514852906](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39514852906)，實際source-sha.txt為8acf4b2，job FAILURE／helper exit3。API33/default/x86_64/pixel7 fresh disposable emulator；固定reference source b98206267b3a65c452db1661bb3bae910ec61f6a、run39152444358、原四APK hashes。只安裝host，不安裝KU-Yin、不選IME；action已終止該disposable emulator。

## Fresh結果

| 層 | 結果 | 直接證據 |
|---|---|---|
| H0 | HEALTHY，單boot有界觀察 | h0/result.json、完整raw；不是持續健康保證 |
| H1 | INCONCLUSIVE／EDITOR_CAPTURE_UNVERIFIED | ui-0 exit null、timed_out=true、3007ms |
| XML | 完整但不滿足editor契約 | 27311-byte XML於2243ms收到、34-byte success marker於2246ms；case R23-H1、input_connection_present=true，但editor focused=false |
| 原oracle另行唯讀解析 | INCONCLUSIVE／EDITOR_EVIDENCE_UNVERIFIED:editor | 不改實際result、不忽略command timeout |
| H2／本分支App／Host／B01 | NOT_EXECUTED | H1未取得可信PASS，因此停止 |
| Physical acceptance | NOT_TESTED | 沒有安裝或操作實體手機 |

相同serial／Android user0／boot_id起迄一致。before/after/final均Window368a347/Binder2f13e86、host HAS_DRAWN、unfrozen、cutout136、config as.3 s.76；原null-root與dump bracket內window替換未重現。cutout0→136已在H0 final與H1 before之間被觀察。

## 根因裁定

歷史Hcutout仍 **UNRESOLVED**，不能以本次完整XML宣稱修復：本次沒有null事件，無受控因果對照。現有兩個獨立缺口是命令未自然終止，以及EditText實際XML focused=false。framework log僅一行VM shutdown；無足夠process/exit/焦點時間軸將timeout定性為UiAutomation disconnect、ADB或產品缺陷。

下一最小任務：只讀本次raw，設計「host焦點與uiautomator/ADB程序終止」的單一可反駁觀察；不得直接再跑全Binding A/B、延timeout或將完整XML當成功。未授權產品／JNI修補。

## 驗證與獨立審查

本機code層：原baseline37在允許PTY環境PASS；新diagnostic RED23/GREEN25、承襲pipe-tail契約補強RED32/GREEN33；full Python34 152PASS、10contracts27 PASS。原full26 fixture啟動失敗已保存並以29/30隔離修正，非Android根因。

`/root/r32_a_diagnostic_review` fresh context直接比對源碼／原raw，發現並複核關閉兩個diagnostic問題；source quality PASS限此切片、規格PARTIAL。收到本次CI後另獨立核查same identity／exact source3hash／CI四APK manifest、完整XML／focused=false／timeout，結論與表格一致；未把程式審查當產品驗收。raw artifact本身不含四APK，APK核對的審查層級是CI identities.json，不冒稱reviewer再次讀到APK bytes。

OCR BLOCKED_BY_PROVIDER；已知配置不可用，未重試、未上傳source。沒有改產品Kotlin/JNI/native pin、原assertions、字典、Room、preferences、簽章、version、Release、原工區。PR10 HOLD、PR12/20 Draft；無merge。

## 證據保存

外部：`/Users/huang/Desktop/KU-Yin-R3.2-20261021-k4xtcv1s`，CI raw在`CI39514852906-H0H1/`；完整獨立報告 `report-A-diagnostic-review.md`、`report-A-fresh-CI-review.md`，原始R3.1資料另保留。公開source hash／結果見 [R3_2_H1_RESULT.json](R3_2_H1_RESULT.json)。

Release **NO_GO**；不建立RC/Stable，不進H2。後續從本文件與必要raw續作，不重做PR18/22文件。
