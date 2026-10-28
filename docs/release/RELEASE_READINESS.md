# KU-Yin Release Readiness

**EXPERIMENTAL_ALPHA=AVAILABLE；RC_READY=false；STABLE_READY=false；NEW_RELEASE=NOT_PUBLISHED。**

工程收束不等於產品發行。權威機器狀態：[FINAL_RELEASE_GATE](FINAL_RELEASE_GATE.json)；最新工程驗證：[FINAL_TEST_REPORT](../maintenance/FINAL_TEST_REPORT.md)。舊Alpha tag/八資產原樣保留。

|Gate|目前裁定|必要證據|
|---|---|---|
|輸入產品來源|AIE-S1/P0-R已有；本輪無產品core變更|最終固定APK/source/signer identity|
|H0/H1/H2|CI40613937769：HEALTHY（bounded）／INCONCLUSIVE／NOT_EXECUTED|同boot/user/serial有效完成觀察；不能用synthetic F|
|Host|歷史15PASS9FAIL8SKIP；observer JVM另驗|原完整assertions直接Runtime重新執行|
|實機/候選/QR/可及性|NOT_TESTED／OPEN|固定APK、授權裝置、真editor text/ACK與手機掃碼|
|簽章與保資料更新|BLOCKED|key custody、certificate、正常update；不卸載清資料|
|Privacy/erase/D2D|OPEN|full erase、OEM傳輸政策、敏感欄位/復原；Room delete ACK 已由PR40修正並有故障注入測試|
|License/source/relink|PARTIAL／新二進位BLOCKED|標準文件修正之外，逐元件來源/資料/完整重鏈結與媒體權利|
|工程CI|獨立記錄unit/build/security結果|exact head及merge tree；不以SKIP當PASS|

prerelease.yml現在只能建TEST_ONLY artifact，不能寫Release或tag。未來新發布流程須另審核exact source/native/APK hashes、簽章、device/runtime、source/relink/NOTICE、維護者批准；不得只相信可手動填寫的PASS JSON。
