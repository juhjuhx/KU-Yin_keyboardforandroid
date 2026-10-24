# Final Security Audit — R3.8

基線main2a2181e023e0dee0ddff98681e374df10cd5fa8f。此報告區分工程範圍與產品散布；不是全裝置安全證明。沒有存取私人字典、Room記錄或keystore。

## 直接核對

- IME exported service由BIND_INPUT_METHOD保護；沒有新INTERNET/麥克風權限或鍵盤內容網路sink。服務仍唯一執行InputConnection effects，session/native context ownership不改。
- Password/sensitive與NO_PERSONALIZED_LEARNING政策已有；native live個人資料位於noBackupFilesDir，scratch preview為隔離context。這不是完整學習/erase實機驗收。
- S0 reset disabled且零刪除文案誠實。未添加新privileged helper、root需求、JNI observer或device secure settings操作。
- 本輪secret tripwire僅Git tracked／reachable blobs，輸出rule/path/blob/line，不輸出匹配值、不以網路試用憑證。二進位、大檔及symlink排除有清單，不宣稱secret-free。

## Findings / disposition

|Severity|來源／問題|本輪處置|
|---|---|---|
|High新散布|prerelease.yml可寫Release、無完整runtime/signing/source gate|改artifact-only/contentsread，無gh release/tag寫入；CI Debug不可宣稱升級相容|
|High新散布|根LICENSE及舊包KU-Yin license文字異於標準LGPL|root恢復標準；舊資產不覆寫，公開erratum與新二進位義務另列|
|Medium privacy|data_extraction_rules空cloud與註解device-transfer；allowBackup=false不等於所有OEM D2D排除|OPEN/RC blocker；本輪不改資料政策或schema|
|Medium privacy/UI|MainActivity:724–729先顯示刪除成功，FeedbackViewModel:109–118僅launch非同步DAO|OPEN，需fault-injection/ACK切片；不是native reset問題|
|Medium供應鏈|Actions可移動tag、wrapper無checksum|固定官方已解析commit／checksum；仍非完整dependency驗證或CVE認證|
|Open Runtime|Pixel7 overlays / Freeze / native觀察 / IPC與TYPE_NULL限制|UNRESOLVED，沒有新Emulator／實機證據|

Code review以AGENTS/Code Verification為合約，獨立source審查指出上述問題；正式Codex Security durable scan與OCR provider未執行，不冒稱其APPROVE。最後整合diff與新鮮測試必須另外對exact PR HEAD確認，見FINAL_TEST_REPORT。
