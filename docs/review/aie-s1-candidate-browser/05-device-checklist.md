# AIE S0/S1 手動復驗

本輪 ADB inventory exit 0，沒有裝置。以下 NOT_EXECUTED；新 Debug APK 供使用者復驗，沒有 production-ready 或完整 P1 acceptance 主張。

1. 核對交接中的檔名、SHA256、版本 `0.2.0-aie.1 / 20003`；應與 P0 相同 applicationId/debug signer。正常更新安裝，保留設定/字典；若 signature mismatch/downgrade，停止，不解除安裝或清除資料。
2. 確認 Android 輸入法已選 KU-Yin。開普通文字 editor，按 ㄨ、ㄛ，無聲調可選「我」；加 ˇ 仍正確。
3. 橫捲候選，確認不因滑動選字；點「展開候選」，preedit 保留，主鍵區改候選網格。
4. 翻到下一頁，確認內容改變；上一頁返回，第一/最後頁相應按鈕停用。此頁不足10個可能是 projection 去重，頁碼是 plan pages。
5. 收合，確認原大千鍵盤／mode 回來且組字不消失。重開網格，點第二頁候選，確認 editor 收到相同文字一次。
6. 繼續輸入「我現在」（有調與無調）、Space/Enter/Backspace/標點；切換英文輸入 hel，選 hello、Space；切另一 editor 確認沒有舊候選誤提交。
7. 在窄螢幕、橫向、大字級下確認 preedit、展開/收合/頁控制與長候選可操作；TalkBack 讀完整候選與選取動作。每項記錄實際觀察，不把編譯當 PASS。
8. 設定中重設學習入口應停用，明示暫不支援及不會刪資料。不要刪除個人字典來測；完整原生＋fallback erase 尚未支援。

失敗記錄：版本/APK hash、editor 類型、按鍵/候選/頁、期待與實際 editor 文字、是否重複/丟失。僅用合成文字，避免提交私人訊息。由使用者回報裁定本片體驗，再決定 AIE-S2；本片不自動開始 S2。
