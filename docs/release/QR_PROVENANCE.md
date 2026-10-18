# Foundation QR — R3 provenance

ACTIVE，2026-10-18。修正 donation card 假 QR；不變更鍵盤、decoder、candidate 或 editor effects。

## Contract and source

四個 HTTPS 公開入口經官方網站核對（2026-10-18）：

| Foundation | Exact payload |
|---|---|
| FSF | https://my.fsf.org/donate |
| ASF | https://donate.apache.org/ |
| OSI | https://members.opensource.org/donate |
| EFF | https://supporters.eff.org/donate/donate |

官方來源：[FSF](https://my.fsf.org/donate)、[ASF](https://donate.apache.org/)、[OSI](https://members.opensource.org/donate)、[EFF](https://supporters.eff.org/donate/donate)。僅查公開頁面，沒有提交資料或捐款。QR 圖是本專案由 URL 自行產生，非基金會提供的商標、素材或背書；款項由官方網站直接收取。

UI/model 唯一產品接線：`app/src/main/java/com/example/ime/ui/OpenSourceDonationCard.kt`。每個 foundation 綁定明確 drawable、URL、名稱；UI 不做動態網路或 runtime QR 編碼。PNG 在 `app/src/main/res/drawable-nodpi/`，不染色、不裁切。150dp 既有框內保留8dp內縮、圖像上限134dp，以整數display pixels/module繪製，完整矩形quiet zone不受圓角框影響。原 SHA256 pseudo-QR Canvas 全數移除。

## Offline regeneration and licensing

```sh
# 在已有的離線工具環境執行；不於 App/CI 自動安裝套件
python3 scripts/generate_foundation_qr.py
python3 scripts/generate_foundation_qr.py --check
```

工具固定 `opencv-python-headless==4.13.0.92`（OpenCV4.13.0）、NumPy2.4.6。標準 BYTE、ECC M、自動最小版本；驗證 OpenCV 輸出兩模組邊界後換成四模組 quiet zone，8px/module、PNG compression9。FSF/ASF V2、OSI/EFF V3。四模組規則參考 [DENSO QR specification guide](https://www.qrcode.com/en/howto/code.html)。

- Encoder 上游 [OpenCV4.13.0 LICENSE](https://github.com/opencv/opencv/blob/4.13.0/LICENSE)：Apache-2.0。
- Python packaging wrapper [opencv-python LICENSE](https://github.com/opencv/opencv-python/blob/4.x/LICENSE.txt)：MIT；與 encoder license 分開。工具不打包進 APK。
- 獨立測試解碼器 [ZXing core3.5.3](https://github.com/zxing/zxing/tree/zxing-3.5.3/core)：Apache-2.0，僅 `testImplementation`；不新增產品 runtime dependency。
- PNG 是本專案工具的輸出；沒有複製上述專案程式碼或第三方鍵盤素材。此工具審查不代表所有 native／dictionary 授權已結案。

`app/src/main/assets/foundation_qr_manifest.json` 記錄 generator SHA、工具版本、URL/id/drawable、尺寸、模組、source PNG SHA256。Source hash 與 Android AAPT 處理後資源 bytes 分開，不以 source PNG hash 冒充最終 APK hash。無 timestamp 進生成內容；同工具鏈應 byte-for-byte 重現。

## Verification boundary

`DonationQrUiTest` 擷取真實 Compose node 像素交由獨立 ZXing 解碼，測試四組 URL、copy/open intent 接線，以及 320/360/600dp、density1/2/3、fontScale1/1.3/2、landscape 的有限組合。`DonationQrAssetTest` 檢查四個來源資源、ECC M、quiet zone、一個底部資料模組受損、manifest identity/hash。這是 JVM/Robolectric native graphics；不是手機相機或完整 TalkBack 實測。

RED：原產品在 `63c9e4a` 畫面 decode 回報 ChecksumException；OSI/EFF URL assertion 亦失敗。外部 evidence：`KU-Yin-R3-RC-Closure-2026-10-18-205053/07-red-results.xml`。後續完整 RED 有一個 landscape qualifier harness error，獨立保留、不算產品 RED；修正 qualifier 沒有更動 timeout。

手機相機掃描：NOT_TESTED。UI 明示「手機相機掃描尚未驗證；可複製或開啟官方網址。」沒有新增 INTERNET、camera、mic permission。完整 UI/accessibility/real-device release gate 仍 OPEN。

## Fresh correction evidence

2026-10-19 續作：原Green候選18前，reviewer逐像素發現外框污染quiet-zone四角。新增原像素斷言，`17-quiet-zone-red.xml` 在OSI座標(1,1)證實非白色；未以解碼成功替代規格。維持既有8dp內縮後，`18-quiet-zone-green.log` exit0，UI6/6、asset2/2（0failure/0skip）。四張QR × 三組畫面條件共12張圖，逐像素矩形白邊、整數module與literal URL獨立解碼。

此前EFF fractional nearest縮放也曾造成ZXing NotFound；`10-zxing-detector-comparison.txt` 比較TRY_HARDER仍失敗，不能靠改解碼選項放行。整數module大小修正後原decoder通過。landscape selector的raw tree（13）顯示LazyRow在viewport上方；測試先捲動外層再移動橫列，保留visible、real click、selected、decode全部斷言，未修改產品geometry或timeout。

全量早期回歸命令15：App273 tests（263PASS/10SKIP，host-native opt-in未提供），Host37測試輸出來自Gradle cache；不能把cached Host宣稱fresh runtime。Python109/109與10contracts fresh PASS（16）。這些先於最後quiet-zone內縮，最終整合後另執行fresh gates；不得把本段當最終RC驗收。
