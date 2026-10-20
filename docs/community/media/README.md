# 圖片來源與待補畫面

2026-10-21：**已核實 Runtime 宣傳截圖 0 張，社群成品圖卡 0 張**。本目錄先保存來源分類，不放假手機畫面。

[ASSET_MANIFEST.json](ASSET_MANIFEST.json) 列出 main 基線 13 張圖片的來源、大小、SHA256 與解碼結果：兩張品牌 JPG、十張 launcher WebP、一張單元測試 PNG。品牌 JPG 不是鍵盤操作截圖；生成式來源標記及權利文件仍待確認。其餘 11 張無法由 Pillow 解碼，保留原檔與 Git 證據，不猜測修復或推論產品已損壞。

## R3.2 Launcher 更新

`ASSET_MANIFEST.json` 是保存的修正前證據，不代表本分支現有十個launcher。使用者授權重新編碼後，十個WebP改用現有adaptive原圖／XML生成，已完整解碼；來源見 [生成manifest](GENERATED_LAUNCHER_MANIFEST.json) 與 [鑑識manifest](R3_2_IMAGE_FORENSICS_MANIFEST.json)。greeting PNG仍損壞；這些資產都不是手機操作截圖。詳細 [工程驗證](../../testing/R3_2_IMAGE_INTEGRITY.md)。

## 待補的真實畫面

- [ ] 大千注音鍵盤／合成文字組字與候選列。
- [ ] 候選展開、翻頁及非首屏選字。
- [ ] 英文輸入與離線補全。
- [ ] 設定或關於頁。

先取得裝置與安裝授權，使用 exact 公開 Alpha APK。逐張保存 APK SHA、Android 版本、裝置型號、擷取時間、公開圖片 SHA、匿名化對照與獨立審查；完整規格見 [素材與隱私](../ASSETS_AND_PRIVACY.md)。未知來源不可補填猜測值。

4:5 五張圖卡與 16:9 封面共用現有識別；版式方向正在等待使用者確認。文字已在 [POSTS_READY](../POSTS_READY.md) 備妥。README 日後只引用通過來源／隱私審查的相對圖片路徑，附替代文字及版本。
