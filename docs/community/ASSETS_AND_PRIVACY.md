# KU-Yin 社群素材與短片前處理

**狀態：SHOTLIST_READY／MEDIA_NOT_CAPTURED。** 這是攝製規格，不是已完成的手機畫面。沒有真實 APK 截圖時，不得使用設計圖、AI 示意圖當作已驗收的鍵盤錄影。

## 建議交付格式

| 目的 | 輸出尺寸／格式 | 備註 |
|---|---|---|
| IG／Threads／FB 圖卡 | 1080 × 1350 PNG／JPEG | 五張直式輪播；重要文字留在安全區 |
| 16:9 封面 | 1920 × 1080 PNG／WebP | 沿用同一視覺系統；方向待確認 |
| IG Story／Reels | 1080 × 1920 MP4 | 真實手機錄影與完整標籤 |
| Bilibili／YouTube 主片 | 1920 × 1080 MP4 | 30–60 秒、實際 APK 畫面 |
| GitHub Social Preview | 1200 × 630 PNG | 使用真實品牌名稱，不偽造商店徽章 |
| GitHub README 截圖 | PNG／WebP | 清晰且小容量；附替代文字 |

以上僅為製作規格。倉庫已有品牌 JPG、launcher WebP 與測試 PNG，沒有已核實的鍵盤 Runtime 宣傳截圖。見 [圖片清單](media/ASSET_MANIFEST.json)，不可把 Logo 或單元測試圖當作打字證據。

兩張品牌 JPG 含生成式 AI 來源文字；本輪未驗證 C2PA 簽章鏈或完整權利文件。十張 launcher WebP 與一張測試 PNG 無法由本輪 Pillow 解碼，Git blob 與本機位元組一致。保留原檔，不猜測修復，也不由此直接推論 APK 執行結果。

## 五張輪播分鏡與可貼上的文字

1. **封面**：KU-Yin 酷音｜Android 大千注音開源鍵盤。角標「Experimental Alpha／非正式版」。
2. **主要功能**：大千注音、中文組字與候選選字。畫面取自實際 Alpha；若尚未實測，就只放圖示／文字說明並標註「功能介紹，非實機驗證」。
3. **候選體驗**：候選橫向捲動、展開、翻頁。應拍攝真實操作，不要用 Compose Preview 當成 runtime 證據。
4. **技術架構**：Android IME → Kotlin Session → C++ JNI → libchewing；Compose UI 與 InputConnection。可用自製矢量示意圖。
5. **開源邀請**：GitHub URL、v0.2.0-aie.1、SHA256 驗證、感謝 libchewing；文字「歡迎回報 Issue」。

建議替代文字例：`KU-Yin Android 注音鍵盤的開源專案介紹：Experimental Alpha，使用 libchewing 與 Jetpack Compose。`

## 45 秒操作／技術介紹腳本

| 段落 | 畫面 | 旁白字幕 |
|---|---|---|
| 0–5 秒 | 專案標題 | 「這是 KU-Yin，一套正在開發中的 Android 大千注音鍵盤。」 |
| 5–12 秒 | 實際打字（若可驗） | 「中文解碼採用 libchewing，介面使用 Jetpack Compose。」 |
| 12–20 秒 | 真實候選瀏覽 | 「目前設計了單列候選、展開與翻頁。」 |
| 20–28 秒 | 自製架構圖 | 「另外處理輸入法的生命週期、候選身分和 Editor 寫入。」 |
| 28–37 秒 | GitHub 原始碼／文件 | 「原始碼、固定的 Native 依賴與測試文件都公開在 GitHub。」 |
| 37–45 秒 | GitHub Releases + 警語 | 「目前是實驗性 Alpha，還在實機驗收。歡迎一起測試與回報。」 |

**若真機未能錄到完整流程，將對應段落改為誠實的程式碼或架構講解，並標註 Demo 尚未驗證；不要剪接製造假成功。**

## 手機錄影匿名化檢查

- 錄製前使用合成測試句，如「你好，這是酷音測試」「今天測試輸入法」；不要打真姓名、地址、帳密或聊天內容。
- 啟用勿擾；遮蔽通知、狀態列中的個資、電話、電信商、位置、GPS、裝置序號、IME 私有資料路徑。
- Android 畫面有其他 App 時，確認 App 內容與錄影素材均有使用權；優先使用空白筆記 App。
- 不展示私人字典、Room 回饋、真實聯絡信箱、API 金鑰或未清理的 adb/logcat。
- 上傳前看完整 MP4，逐影格抽查前後 3 秒及切場；檢查 EXIF／GPS metadata。
- 若顯示 Debug 或系統安全警告，保留真實標籤，不剪掉警告冒充正式版。
- 內部來源紀錄必填：APK_SHA256、APK 版本、ANDROID_VERSION、DEVICE_MODEL、CAPTURE_TIME（含時區）、SCREENSHOT_SHA256、來源類型、匿名化方式及逐張 Reviewer。公開可用匿名裝置代號，內部保持可追溯；不得公開 serial 或私人輸入。
- 移除 EXIF/GPS 後重新計算公開檔 SHA256，保留原始檔與公開檔的對照。影片標示版本及 Experimental Alpha，不冒稱跨機型驗收。

## 編輯與字幕後處理

- 音訊／影片編碼：H.264 MP4、AAC，文字字幕另提供 UTF-8 SRT。
- 頁面錄影請先確定版本一致，後期不得以新版 RC Preview 拼成公開 Alpha 的操作示範。
- 圖片壓縮前後確認注音鍵帽與小字能辨讀；若縮小後不可辨，使用放大裁切但保留原始畫面備查。
- 顏色、圖示與開源組織名稱不得讓觀眾誤會 FSF/ASF/OSI/EFF 或 libchewing 官方支持 KU-Yin。

## 素材存放建議

公開且去識別後的圖像才放 `docs/community/media/`。原始手機錄影、ADB 證據、個人字典、可識別系統資訊應留在私有工作空間，不得直接提交至 public GitHub。