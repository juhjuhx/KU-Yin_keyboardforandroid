# R3.1 實機驗收

更新：2026-10-20。`PHYSICAL_ACCEPTANCE=NOT_TESTED`。本輪未對實體手機安裝、選取 IME 或變更資料。

## 入口與更新安全

尚未取得 owner 對指定 serial、APK、四測試套件安裝、暫時 IME 切換及還原的授權；不得由 ADB 連線推定授權。本輪最終唯讀盤點只見一個其他既有 ARM64 emulator，未接管、未當實體手機；再次執行前重新盤點。

候選產品仍是原 R3 本機 Debug：applicationId `io.github.juhjuhx.kuyin`，`0.2.0-rc.1`／20004，SHA256 `c171a3455acb1cb4a3b16ca0eb65e483620c35a224a308f398ad65c1867f0416`，signer SHA256 `46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94`。這些是原 artifact 身分，不是新實機證據。R3 CI artifact 使用不同 Debug signer，不能用來聲稱保資料升級。

指定裝置與安裝檔後，先核 installed package/version/certificate、原 default/enabled/subtype/Android user 與 runner 支援的還原條件。簽章不符或 downgrade 必須停止，不 uninstall、不 pm clear、不刪字典、Room 或 preferences。

## 待執行

| scope | 結果 | 所需直接證據 |
| --- | --- | --- |
| P1 原 30-case matrix | NOT_TESTED | 逐案 observable/full result、真 editor text delta、action/commit count |
| 41 鍵／聲調／無調／我現在 | NOT_TESTED | 產品軟鍵、preedit/candidate identity、實際文字 |
| 候選橫捲／展開／翻頁／非首屏選字 | NOT_TESTED | 同 generation snapshot、接續組字、無重複 commit |
| 英文／Enter/Send／editor 切換 | NOT_TESTED | 跨 editor 實際文字及 action ID/count |
| 橫直／大字／TalkBack／輸入法切換 | NOT_TESTED | 裝置 configuration、focus、可繼續輸入 |
| 四基金會 QR 手機掃描 | NOT_TESTED | 每個實際畫面的完整 decoded URL；不依賴本機 PNG roundtrip |
| 原 Alpha 保資料更新 | NOT_TESTED | 同簽章與 monotonic version、更新後資料保留及啟動 |

只用合成文字，不擷取私人訊息或字典內容。個人 learning reset 測試需針對資料刪除範圍的額外授權；目前完整 erase 不支援，不假裝成功。Emulator 結果不能填入此表的 PASS。
