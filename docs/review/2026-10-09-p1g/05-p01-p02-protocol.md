# P01/P02：固定身分與有序協定

scripts/p1_instrumentation_manifest.json 是 fixture source 6595b684 的版本化合約；app 4 classes / 5 methods、host 3 classes / 26 methods。固定31個實際JUnit identity、26個外層case owner、8個原始Kotlin source SHA。runner硬定5/26及canonical manifest SHA256 ccb2d32d62dd1aea5d84db2b7d53853c108b0f4dff99727dbe9d6c916fc9e8de；不能以同count假suite替換。prepare後 artifact snapshot亦綁manifestSHA。restore刻意不被artifact變更阻擋。

Manifest建立時曾誤把NativeCase4Test檔第一個private delegate當test owner；獨立審查發現後先 ManifestIntegrityTests RED，再更正 NativeCase4Test。原fixture/app tests與APK未改。今後變更instrumentation需來源review、更新manifest與pin，不能自行放寬expected identity。

有序模型：唯一start → status2 observations → matching唯一finish → 唯一final -1 + count/OK summary一致。reject finish-without-start、overlap/orphan/duplicate start/finish、malformed code、duplicate bundle key、premature EOF／terminal後協定事件。只消費真正active owner的 sendStatus(2) stream bundle；任意stdout或未完成status2不算證據。

Canonical host每個owner恰好一個外層case emit；D18內部多layout/NO_ENTER_ACTION觀察仍屬D18，不拆成虛構JUnit。Parser也可依明示合約接收多case/subcase；undeclared或duplicate tuple拒絕。錯owner/outside/unknown case都FAIL；missing expected emit列missing_required_cases，suite INCONCLUSIVE且該case NOT_TESTED。

JUnit error/failure勝過case PASS；app5不得skip/assumption，hostskip INCONCLUSIVE。Observable PASS仍保留B/C/D17等full-scope limits，A04/learning不豁免；exact fake5+26正常流程仍exit3 PARTIAL並安全還原，非實機驗收PASS。

TDD：P01 8tests/1 assertion RED（替換identity）；P02 14tests/8 assertions RED（lifecycle/owner/duplicates/missing/subcase）。獨立負面審查再發現malformed code被忽略、case未綁status2、pre-prepare manifest可換；5methods/7 assertions RED後修正。protocol-review-green全97/97 exit0。此處僅記錄fake protocol，不是device RED/GREEN。

最後payload schema：duplicate JSON key可以last-key-wins抹FAIL／owner，已用object_pairs_hook拒絕任何nested duplicate keys；RESULT空/duplicatekey與status2 bundle自身identity錯配也拒絕並讓該case不可observable PASS。PayloadSchemaTests 3methods/4 assertion RED → 全101/101 GREEN，最終獨立review APPROVE。
