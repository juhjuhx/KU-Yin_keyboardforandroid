# KU-Yin 社群發布包

此包介紹目前已公開的Experimental Alpha，沒有代替使用者發文，也不是新RC／Stable的發行證據。

- 公開下載：[v0.2.0-aie.1](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1)。APK為`KU-Yin-AIE-S1-candidate-browser-debug.apk`，SHA256 `acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`。
- main包含PR35工程維護；CI artifact與公開Alpha分開，沒有自動更新公開APK。
- 新RC／Stable仍NO_GO。Runtime、實機、QR手機掃描、正式簽章與保資料更新仍有缺口。[Issue19](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/19)

## 可直接取用

|內容|唯一入口|
|---|---|
|繁中長／短版、IG、小紅書簡體、Bilibili、開源／Codex社團|[POSTS_READY](POSTS_READY.md)|
|五張1080×1350圖卡、1080×1920 Story、1920×1080封面|[social](media/social/README.md)|
|圖像來源、SHA、Alt、授權分類|[social manifest](media/social/ASSET_LICENSE_MANIFEST.json)|
|真實畫面與歷史素材分類|[media](media/README.md)|
|隱私／拍攝規格與發文核對|[ASSETS_AND_PRIVACY](ASSETS_AND_PRIVACY.md)／[Checklist](RELEASE_AND_SOCIAL_CHECKLIST.md)|
|開發經歷與留言|[作品集記錄](PORTFOLIO_CASE_STUDY.md)／[FAQ](FAQ.md)|

品牌封面採原創SVG幾何排版，不是App截圖；真實畫面必須有裝置、APK SHA、版本與隱私審查。沒有核實鍵盤畫面時保留BLOCKED，不拿Roborazzi、Preview或品牌圖補位。兩張來源權利仍待完整核實的舊JPG未用於新社群圖卡。

## 對外說明原則

可說：離線大千注音、libchewing組字、候選瀏覽、英文prefix completion，以及Experimental Alpha。

不可說：已完整實機驗收、所有手機相容、AI雲端候選、完整學習清除、Rime／Whisper已整合，或新main CI包可無損更新舊Alpha。敏感欄位限制學習不等於完全不儲存資料，noBackup也不是加密。

基金會及上游只作致謝，不代表背書。App現有舊QR不是標準碼，請使用README的官方URL；未發布PR20的修正不能說已上線。

舊`2026-10-public-alpha-launch-kit`及`POSTING_AND_ASSET_CHECKLIST`是SUPERSEDED相容入口，不再產生平行文案。
