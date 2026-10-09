# P1-G 獨立審查

## 第一輪：原始 source

p1g_initial_review fresh-context 直接讀 P1-F reports、runner、原24測與 source，確認F01/F02/F03/P01/P02。不是產品 keyboard defect。

## F01/F02 Android contract 覆核

p1g_android_contract：固定官方 API24/34/35/36 source 研究；命令順序與 API35/36 zero-subtype strategy 成立。第一輪 NEEDS_REVISION：--user-profile substring、malformed duplicate count；另補 empty duplicate mId。已逐項 regression RED/GREEN。第二輪直接讀最新 source 並以 memory doubles 證偽，APPROVE，沒有新 finding。保留 API35/36、原selected=-1且count0限制；不等於任何裝置通過preflight。

## F03 安全覆核

p1g_initial_review 以 subprocess.run 全域 throw 保證不呼叫真 ADB，重跑 fake tests，發現 stderr+command-log IO 雙故障仍能打斷restore。ConsoleFailureTests RED證實，已修 best-effort diagnostic。最終安全閘門 APPROVE：獨立64/64，禁止真 subprocess；額外 stdout/mkdir/closed stderr 與API24/34/37反例通過，runner reviewed SHA256 4b72abfe6581317dac8de0487eb21370f9fe311e635422120d939ea148532cb2。無剩餘High/Medium。此裁定不含尚未實作P01/P02。

## Protocol 最終覆核

p1g_final_protocol_review fresh-context直接讀actual final diff、actual Kotlin test owners與8 source hashes；獨立找出NativeCase4 private delegate誤owner、malformed control／empty keys、未綁sendStatus2、manifest可替換、duplicate JSON抹FAIL。全部先assertion RED再修。最後runner SHA256 5d15c9d28007de5f9d3de3c7b91216190fde82d5b318388c85649b02ddde24a5，manifest ccb2d32d62dd1aea5d84db2b7d53853c108b0f4dff99727dbe9d6c916fc9e8de。

最終APPROVE：獨立101/101無skip，另37項protocol反例、5種manifest變更均拒絕；全域subprocess禁器記外部call0。5/26actual source identity吻合。fake整條sequence為INCONCLUSIVE且restore PASS，不混淆fullscope。沒有剩餘High/Medium；本批准只含runner source/fake安全契約，runtime仍NOT_TESTED。審查一度因provider usage limit中斷，使用者續行後已完成重驗，未把中斷當APPROVE。

## OCR

v1.12.12；/opt/homebrew/bin/ocr llm test exit1，Anthropic HTTP401 invalid x-api-key、request_id=null。OPEN_CODE_REVIEW_STATUS=BLOCKED_BY_PROVIDER。只試一次，不改 credentials、不宣稱OCR PASS。

## V3 package-only 獨立覆核

p1g_final_protocol_review直接讀ZIP bytes，APPROVE：4dffdeb1…03da816／37entries／CRC；SHA256SUMS完整覆蓋其餘36檔且全部相符；runner/manifest與approvedsource一致；四APK與原v2逐byte同、8Kotlin hash吻合；原P1E工區乾淨、99protectedsource與v2ZIP不變。無pycache/pyc/rawlog/credentials/device-state，全部30case仍NOT_TESTED。此pass只驗包完整性，不是physicalacceptance。
