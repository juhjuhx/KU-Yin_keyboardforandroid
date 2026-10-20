# R3.2 圖片完整性

## 範圍與來源

基線 main `8369da25fa54ab762dfceed457e71a08767a642e`，Issue #23；分支 `fix/r3-2-image-integrity`。使用者明確表示無原件，授權自行編碼生成。十個 legacy launcher WebP 現從既有 adaptive JPG／XML 重新 rasterize，**不是復原歷史原件**，不改 adaptive XML、品牌原圖、Dachen、Kotlin、JNI、version、簽章或資料。

原始十 WebP 與 greeting PNG 在 `7960d858caa84ca7f447edae2683f58c1633db33` 即已損壞；公開 Alpha APK 的十個 launcher bytes 與來源相同。UTF-8→Latin-1 逆轉能往返，但只有四張通過兩個 decoder，視覺也不符合目前品牌；PNG inverse 仍無效。66個本機候選皆不可完整解碼，不能猜測修復。確切轉碼工具 UNKNOWN。

[歷史來源清單](../community/media/ASSET_MANIFEST.json) 不改寫；[鑑識清單](../community/media/R3_2_IMAGE_FORENSICS_MANIFEST.json) 保存原 SHA；[生成清單](../community/media/GENERATED_LAUNCHER_MANIFEST.json) 記錄原圖／XML／generator／十輸出 SHA、工具與 geometry。原件仍在 Git及外部鑑識保存位置。

## 修正與驗證

- 依原108dp背景、66dp中心前景及72dp viewport生成48/72/96/144/192px，每密度 square/round兩種lossless WebP；保留原圖，輸出不帶EXIF。
- 完整 RIFF/chunk framing、`dwebp`及 `ffmpeg -xerror -err_detect explode`獨立解碼十張；不是只檢查magic或寬高。
- 新 CI gate 在 Python tests 前安裝公開發行版 `webp`／`ffmpeg`；固定來源與輸出 allowlist、SHA 驗證防止manifest任意路徑讀取。
- meaningful RED：原十圖全部被拒絕；後續兩個 reviewer P2 的 regression 證明漸層 geometry 漂移及錯誤 provenance hash不可通過。GREEN：十圖完整解碼、三focused tests PASS。
- Fresh `python3 -m unittest discover -s scripts/tests -v`：112 PASS；全部11項 `check_*.py` PASS。Android build與最終CI結果另登記 handoff，未完成之前不算PASS。

## 獨立審查與限制

Reviewer `/root/r32_b_patch_review` 直接讀實際diff／source／negative tests，重跑checker及三focused tests；初次兩P2修正後 follow-up 的 specification／engineering 均PASS。該裁定不涵蓋Android／裝置／全部repository。

`app/src/test/screenshots/greeting.png` 尚未修復；不以繪圖程式生成假Roborazzi golden。現有JPG來源有生成式metadata，完整權利／C2PA鏈仍OPEN。核實手機Runtime宣傳截圖0；手機Launcher顯示NOT_TESTED。

回退僅需正常revert此圖片切片；不動舊Alpha／tag／APK。此修正不解除 H1、實機、簽章或license阻擋，RC／Stable仍 **NO_GO**。
