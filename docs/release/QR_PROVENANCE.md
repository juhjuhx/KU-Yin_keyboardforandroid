# 捐款 QR 來源與驗證（F3）

標準 QR 修正獨立抽自 PR20 的 QR 切片，沒有合併其候選／Runtime／版本變更。原 SHA256 模組圖不是標準 QR，已由專案離線生成的 PNG 取代；不經線上 QR 服務，也不加入網路／相機權限。

## 保留的既有官方網址

|ID|原主線網址，亦為實際 payload／複製／開啟網址|
|---|---|
|fsf|https://my.fsf.org/donate|
|asf|https://donate.apache.org/|
|osi|https://opensource.org/donate|
|eff|https://eff.org/donate|

OSI／EFF 可能由組織伺服器重新導向；本修正不擅自更改捐款目的地。圖片不是基金會提供的素材或背書。原組織介紹及作者不收款文案保留。

## 可重現來源

`python3 scripts/generate_foundation_qr.py --check` 使用已固定的 OpenCV4.13.0（opencv-python-headless4.13.0.92）與NumPy2.4.6，無網路、BYTE、M error correction、四模組純白quiet zone、每模組8 source pixels。manifest保存generator及每張PNG SHA；FSF／ASF／EFF25模組，OSI29模組。此工具為可選離線產圖依賴，不是APK runtime依賴。

[OpenCV4.13.0](https://github.com/opencv/opencv/blob/4.13.0/LICENSE) Apache-2.0；Python wrapper MIT，NumPy BSD-3-Clause。獨立decoder是[ZXing3.5.3](https://github.com/zxing/zxing/blob/zxing-3.5.3/LICENSE) Apache-2.0，僅testImplementation。沒有複製上游QR encoder實作進產品。

## 畫面與證據層級

Compose按整數 display pixels/module呈現，保留150dp框與8dp內縮，不以圓角裁切quiet-zone角落。三種畫面設定：360dp/mdpi/font1、320dp/xhdpi/font1.3、600dp landscape/xxhdpi/font2。測試直接擷取真實Compose測試render並以ZXing解碼，另驗四側白邊、單一module受損、精確copy/browser Intent及四foundation identity。

JVM render是測試畫面，不是手機截圖或相機掃描證據。手機掃描／TalkBack／實機仍NOT_TESTED；UI明示「手機相機掃描尚未驗證；可複製或開啟官方網址」。完整release gate不因此自動關閉。
