# KU-Yin 社群留言 FAQ｜2026-10-20

**Q：這是什麼鍵盤？**
A：KU-Yin 酷音是以台灣大千注音為核心的開源 Android 輸入法，使用 Kotlin／Jetpack Compose 和 libchewing JNI；目前公開 Experimental Alpha。

**Q：有正式安裝包嗎？**
A：有公開可下載的 **實驗性 Debug APK**，並非 Stable： https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1

**Q：一般手機可以直接當主力輸入法嗎？**
A：不能保證。公開 Alpha 尚缺完整 OEM/實機/跨 App 輸入欄位驗收，建議先在備用裝置或非敏感文字情境使用。若手機已有使用者字典，請勿因測試或簽章錯誤而盲目卸載、清資料。

**Q：它是完全離線嗎？**
A：核心中文／英文輸入不需網路，產品 Android Manifest 不要求 INTERNET。App 可由使用者主動開啟外部網站、分享回饋；本機字典與設定仍可能儲存個人資料，不能解讀為「零資料儲存」。

**Q：有 Gboard 類似的自動修正與個人化嗎？**
A：已有 libchewing 原生學習與基本候選，完整新 Ranker、自適應觸控和安全清除全部學習資料仍是後續工作，非已提供功能。

**Q：支援 Whisper、Rime 或 OpenCC 嗎？**
A：現行公開 Android 版本沒有 Whisper 語音、Rime Decoder 或完整 OpenCC 簡繁轉換；目前僅有一些研究與架構接口。

**Q：能直接掃 App 裡的捐款 QR 嗎？**
A：公開 Alpha 的舊圖樣不是標準可解碼 QR。R3 隔離分支已有標準 QR 資產與離線解碼測試，但尚未發布也沒有實際手機掃描驗證。建議直接使用 FSF/ASF/OSI/EFF 的官方連結。

**Q：和 libchewing 的關係？**
A：KU-Yin 使用固定版本 libchewing C API 作為中文 Decoder，透過自有 C++ JNI 整合；此專案是獨立的 Android 應用，不代表 libchewing 官方背書。

**Q：開源授權？**
A：KU-Yin 倉庫附 GNU LGPL-2.1 授權文字；各上游元件與詞庫依自己的授權條款。請查看根目錄 LICENSE、NOTICE、docs/UPSTREAM.md 和發布來源材料。

**Q：為什麼還沒有 Google Play／F-Droid？**
A：長期發行簽章、資料保留更新、Android Runtime、真機與第三方來源授權仍有待驗收。尚未提交可公開宣稱的正式商店版本。

**Q：怎麼幫忙測試？**
A：請在 GitHub Issues 提交系統版本、裝置型號、非敏感測試文字與最小重現步驟。不要上傳真實輸入、密碼、完整系統日誌或私人詞庫。

問題回報：https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues
