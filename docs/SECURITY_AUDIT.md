# Security Audit

ACTIVE source audit，R1基線a6a4c65。不是第三方certification，也沒有新的physical/OEM安全證據。D2.5 static原稿留在Git歷史，當前dataflow以ARCHITECTURE/architecture為準。

| 邊界 | 來源核對 | 限制／處置 |
|---|---|---|
| Network | source Manifest無INTERNET；keyboard無cloudsuggestion | externalbrowser/share明確usergesture可傳feedback；不是零外部資料流 |
| Native userdict | noBackupFilesDir/system與user分離，personal appprivate | 未app加密／完整erase；不保護root compromiseddevice |
| Backup | allowBackup=false，native noBackup excluded | Android12+ OEM D2D語义不同；Room/prefs XML缺有效transfer excludes，OPEN |
| Sensitive input | EditorPolicy→NativeLearningPolicy；password/forceASCII/nontext/no-personalizedlearning | genericprivate mode/learning observer與physical矩陣OPEN |
| Content logging | native僅init/loaderror；syntheticprobe不編入product | R1不保存私人trace；敏感writefailure顯示遮蔽 |
| Composition/effect | snapshot stale guard、ordered effects、falseACK禁止重送 | nativeACK早於editorACK；無跨storetransaction/end-to-end exactlyonce保証 |
| Reset | disabled/unsupported、no deletion | S0已關誤導入口；fullerase仍OPEN，不再稱現行假成功HIGH |
| Room | feedback v1，email/description/metadata local | 無migration/TTL/quota；share JSON控制字元escape不足、UI無DBdeleteACK，待獨立產品修正 |
| System dict | installer只驗非空 | 無assetrevision/hash/update gate；未稱現有字典已損毀 |
| Supply chain | immutableprebuilt/source pins、source/relinkrelease材料 | bootstrap不是Rust重編等價/完整LGPL法律review |
| Release | 現有sameDebugsigner experimentalAlpha | productionkey未配置；manuallegacypublish須另hardening，禁止重簽替代 |

compile/target SDK 36（minor1）/min24；version20003/0.2.0-aie.1，實際build身份見DEVELOPMENT與release交接。

## 正常資料流

EditorInfo→service policy→Chinese session/native live與scratch候選→Compose snapshot→ordered effects→InputConnection。EnglishofflineWordSession獨立。個人learning在nativecommit可能早於editor回傳；fallbackprefs只在dictionary路徑（含試打）寫。Room只儲存手動feedback，不接輸入事件／ranking。

## 本輪已確認、未擴大修正的議題

來源：`app/src/main/res/xml/data_extraction_rules.xml`；`feedback/data/FeedbackEntity.kt` escapeJson；`feedback/ui/FeedbackDialogs.kt`版本1.0.0與Issue URL；`MainActivity.kt`feedback成功訊息；`LibChewingDataInstaller.kt`非空跳過。維持ledger於maintenance/INDEPENDENT_REVIEW，不在文檔治理中改product/schema或做data deletion。

官方語義參考：[Android Auto Backup](https://developer.android.com/identity/data/autobackup)。這是source/contract推論，不是已測OEM傳輸。歷史exactP1/AIE APK權限觀察見release/AIE_ALPHA_HANDOFF與review；app-definedsignatureDYNAMIC_RECEIVER permission不代表INTERNET。
