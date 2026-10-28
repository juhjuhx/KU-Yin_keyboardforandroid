# ADR 0008：D2D 僅轉移鍵盤偏好設定

狀態：使用者已批准政策；F3 隔離切片實作，裝置／OEM 驗收待完成。

## 決定

Android 12+ device-transfer 使用單一 include：`sharedpref/kuyin_settings.xml`。該檔由 KeyboardSettings 寫入震動、聲音、高度、主題、候選字級、剪貼簿列開關、簡繁開關等偏好；不保存輸入文字。Room 回饋、fallback learning、其他 prefs/files/database 不在白名單。Native userdict 原本位於 noBackupFilesDir，位置不變。

維持 Manifest `allowBackup=false`。cloud-backup 及 legacy full-backup-content 明列九個 domain 的全排除；不承諾 Android 11 以下會轉移偏好。沒有搬資料、改 Room schema、刪除資料、加入權限或執行 migration。

## 理由與權衡

使用者選擇「只允許鍵盤偏好；排除回饋與學習」，以減少含回饋文字／email／個人學習的換機擴散，同時保留非內容偏好。新增 include 後，該模式只有明列項目可納入；不得日後以 root include 擴張。未來若在 kuyin_settings 加入輸入內容，必須重新審查此白名單。

[Android 官方 Auto Backup](https://developer.android.com/identity/data/autobackup#xml-syntax-android-12) 指出部分 OEM D2D 不完全受 allowBackup=false 控制。規則是平台契約表達，不是所有 OEM 均已驗證的保證，也不等於加密／完整 erase。

## 驗證與回退

`python3 -m unittest scripts.tests.test_backup_policy -v`：舊規則 3 tests／2 FAIL；新規則 3 PASS，包含偏好正例及回饋／字典／其他 domain 反例。這是 XML policy contract，沒有實際傳輸資料；compiled resources／Manifest 的 CI 與 synthetic OEM transfer 另列。

需要在授權裝置驗證實際備份／D2D、既有資料仍在、僅偏好恢復。缺少此證據，D2D_PRIVACY_READY=false、切片維持 Draft。正常 install-r 的資料保留與備份／換機、使用者刪除是不同契約。

回退只以審查過的 Git revert 恢復規則；不清除或回寫使用者資料。回退可能恢復舊 D2D 缺口，因此必須重新記錄風險，不能為追求 CI 綠燈撤銷排除。
