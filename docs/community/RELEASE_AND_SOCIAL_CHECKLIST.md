# KU-Yin Alpha 分享｜發布前檢查與時序

此清單只授權**宣傳已公開的 Experimental Alpha**，不等於允許建立新版 Release、合併 PR #20、發布 RC 或改標 Stable。

## A. 下載與來源確認

- [ ] 發文當天重新檢查官方 [GitHub Release](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1) 和下載檔名。
- [ ] 確認 APK：`KU-Yin-AIE-S1-candidate-browser-debug.apk`。
- [ ] 下載後核對 SHA-256：`acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`。
- [ ] macOS：`shasum -a 256 KU-Yin-AIE-S1-candidate-browser-debug.apk`。
- [ ] Linux：`sha256sum KU-Yin-AIE-S1-candidate-browser-debug.apk`。
- [ ] Windows PowerShell：`Get-FileHash .\KU-Yin-AIE-S1-candidate-browser-debug.apk -Algorithm SHA256`。
- [ ] 安裝前確認 Android 版本／備用鍵盤已可用。原版本 Debug 簽章可能與自編包不同；**簽章錯誤即停止，不自動卸載／清資料**。
- [ ] 未上傳的 R3 `0.2.0-rc.1` 候選不可與既有 Alpha 混淆；CI artifact 不得冒充官方 Release。

## B. 技術事實

- [ ] 說明 41 鍵大千注音／libchewing／Kotlin／Compose／JNI／離線英文補全。
- [ ] 說明原生候選已實作，但不同手機、各種 Editor 的完整操作與無障礙仍待驗。
- [ ] 不宣稱支援 Whisper／OpenCC 完整簡繁轉換／正式 Plugin／雲端 AI 候選。
- [ ] 可聲明產品不要求 INTERNET 權限，但不得宣稱不存任何本機個人學習資料。
- [ ] QR：公開 Alpha 尚不是新版 R3 標準 QR；如果分享其畫面，說明原版只是 QR 風格圖。
- [ ] 上游 [libchewing](https://codeberg.org/chewing/libchewing)、[fcitx5-android/prebuilt](https://github.com/fcitx5-android/prebuilt) 按直接依賴描述；FlorisBoard、HeliBoard 僅研究參考，不宣稱直接移植。

## C. 截圖、素材與版權

- [ ] 使用真實 UI 錄影／擷取，或清楚標示規劃示意圖；不得拿假的手機畫面當驗收證據。
- [ ] 使用合成文字，遮蔽通知、姓名、帳號、聯絡資訊、鍵盤輸入、私有字典與裝置識別。
- [ ] 圖卡／影片檢查大小、清晰度、字幕錯字及社群裁切。
- [ ] 資料來源、第三方商標、字型及音效授權核對。不得擅自拿上游 Logo 當官方聯名。
- [ ] IG 的貼文網址不一定直接可點；將 GitHub Release 放在 Bio 或置頂留言。

## D. 發布建議順序

1. 更新 GitHub README 與文件連結；檢查文件-only PR 的 diff、CI、正確連結，再合併。
2. GitHub README／Release 與 Issue 回報入口核對後，先發 Threads（短）與 Facebook（長）。
3. 具備真實、匿名化的畫面後，再發 IG 輪播、Reels、B 站影片及 LinkedIn。
4. 保留發文 URL 與日期在私人追蹤表，定期整理有價值的 Issue；避免無授權蒐集測試者個資。
5. R3／R3.1 直到 H1/H2/App/Host、實機、更新、正式簽章和隱私授權 gates 結束前，維持 `NO_GO`，不要以社群流量施壓發布未驗證版本。

## E. 社群留言／回饋範本

請回報：APK 版本、Android 版本與廠牌、輸入欄位類型、合成文字的最小重現步驟、期望／實際差異、是否能穩定重現。請勿貼密碼、私人聊天截圖、完整 logcat、真實字典或含敏感資訊的影片。

## F. 發布後自查

- [ ] 每篇分享都能從官方連結找到相同檔案與 SHA。
- [ ] 附有「Experimental Alpha／非 Stable」標示。
- [ ] 問題追蹤連至 GitHub Issues；回覆不將個別成功案例當成所有裝置驗收。
- [ ] PR #20、R3.1 與正式 RC 的狀態仍如實標記；公開 Alpha 未被覆寫。
