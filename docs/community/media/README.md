# 圖片來源與 Gallery

## 當前公開素材

- **Runtime 鍵盤／設定截圖：0 張，BLOCKED。** 沒有完成 APK、裝置與隱私來源核驗的操作畫面。
- **品牌／技術介紹：7 組 SVG＋PNG**，包含五張 1080×1350 圖卡、1080×1920 Story 及 1920×1080 封面。封面兼作圖卡第一張，不重複保存。
- [原創 Hero](kuyin-readme-hero.svg)／[來源說明](kuyin-readme-hero.SOURCE.md)；[社群包與 Alt Text](social/README.md)／[資產權利與 SHA manifest](social/ASSET_LICENSE_MANIFEST.json)。這些是品牌排版，不是手機畫面。

品牌包沿用既有原創矢量幾何，以深青綠、暖白、淡紫編排；使用系統字型呈現，沒有另打包字型檔。原有兩張品牌 JPG 的生成式來源標記、C2PA 鏈及完整權利仍待核對，這次不重用作新封面。

## Launcher 與歷史鑑識

[ASSET_MANIFEST.json](ASSET_MANIFEST.json) 保存**修正前**十三張圖片的 SHA／來源與解碼證據，不代表目前圖片仍損壞，也不應覆寫成新的鑑識結果。

十張 Launcher WebP 已於 PR24 修復並合併；來源是已有 adaptive 圖形／XML 經程式生成，見 [生成 manifest](GENERATED_LAUNCHER_MANIFEST.json)、[鑑識 manifest](R3_2_IMAGE_FORENSICS_MANIFEST.json) 及 [整合驗證](../../testing/R3_4_IMAGE_INTEGRATION.md)。[greeting PNG](ROBORAZZI_RENDER_MANIFEST.json) 由 Roborazzi 重建，是 JVM 測試渲染。以上均非 Runtime 截圖，亦未改動既有 Alpha APK。

## 待補的操作畫面

|畫面|狀態／必要來源|
|---|---|
|大千41鍵、組字、候選列|BLOCKED；exact APK＋真實 IME 視窗與軟鍵操作|
|展開、上下頁、非首屏選字|BLOCKED；候選身分與實際 editor 文字證據|
|英文補全|BLOCKED；實際 English session|
|設定／啟用頁|BLOCKED；健康且經授權的裝置／Emulator|

本輪僅作 read-only ADB inventory：未發現實體手機；既有 API36 Emulator 的有界觀察未能證實完整健康條件及可拋棄性，故未安裝 KU-Yin、未切換 IME、未取圖。這不是新的 H0／H1／H2 驗收。

後續須逐張保存 APK SHA、source commit、Android 版本／裝置、擷取時間、公開圖片 SHA、匿名化方式和 Reviewer；規格見 [素材與隱私](../ASSETS_AND_PRIVACY.md)。Preview、Roborazzi、品牌圖和技術示意必須各自標明，不能補位冒充 Gallery。
