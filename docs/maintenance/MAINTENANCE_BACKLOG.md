# KU-Yin Maintenance Backlog

本輪結束後只按Issue挑單一任務；這份清單不授權自動啟動新功能或實驗。

|工作|狀態／風險|驗收與範圍|
|---|---|---|
|RRO caller/Display Freeze/readiness|UNRESOLVED；Issue19|先核對exact image/source與可信初始化完成界面，再另批一次health-only；不固定sleep或隱藏frozen|
|Host Stale＋8缺口|JVM修正不等於Runtime；Issue26|健康環境下保留所有原assertions，九FAIL逐案，八SKIP另補|
|Room刪除ACK|Medium privacy/UI|fault-injection RED→DAO完成ACK→才顯示成功；不得清userdict/重設app|
|Android12+ D2D|Medium控制缺口|noBackup與prefs/Room分開；核准排除政策及OEM實測，backup不是加密|
|授權文件更正/舊包erratum|High新散布阻擋|root標準LGPL已校正；舊bytes不動，公開更正，下一版完整source/relink/媒體權利|
|Signer/保資料更新/實機QR|BLOCKED|授權手機、固定APK/cert/version；不能卸載或清資料掩蓋不相容|
|Main保護/供應鏈驗證|維護工作|記錄required checks；Actions/wrapper pins以外尚需dependency verification/SBOM/重建等價|

PR20、PR27各自Draft/HOLD。PR10/12不因本輪接近收尾就自動merge；Windows voice13/14獨立。未批准S2/S3/S4/S5、M4、Whisper或Rime移植。

本輪新增明確工程阻擋：App lint4 errors/77warnings。先按MissingPermission、MissingSuperCall、NewApi逐項設最小RED/處置，尤其onEvaluateInputViewShown不得為消除lint隨意改IME visibility。最後獨立review尚未完成，Draft不能merge。
