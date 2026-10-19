# KU-Yin 酷音注音鍵盤 for Android

> 狀態：實驗性 Alpha（`0.2.0-aie.1`，versionCode `20003`）。Debug 簽章，S1 完整實機驗收尚未完成。

臺灣大千注音 Android 輸入法，使用 Jetpack Compose 與固定版本 libchewing。離線執行，英文提供本機 10,000 詞的字首補全；沒有雲端建議或大型語言模型。

## 下載與安裝

[GitHub 預發布與來源材料](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1)。安裝 `KU-Yin-AIE-S1-candidate-browser-debug.apk` 後，在 Android 輸入法設定啟用並選取 KU-Yin。

公開 APK 沿用 P0-R Debug certificate；正常更新應保留資料。若 Android 回報簽章不符或版本降級，先停止，不要解除安裝或清除資料。CI 重新產生的 Debug APK 不保證相同 signer，請使用 Release 所列的精確檔案並核對 SHA256SUMS。

## 已實作與驗證邊界

- 41 鍵大千注音、無聲調候選、連續組字、選字與 editor commit；P0-R 基本中文有使用者手機回報。
- S1 單列候選橫捲、展開網格、前後頁與 stale snapshot 防護；本地 JNI／Compose／editor 測試通過，實機 browsing 待驗。
- 英文 QWERTY、Shift／CapsLock、字首補全；基本符號與 emoji 頁。
- 深色模式、配色、候選字級、震動／音效設定、本機回饋及既有開源捐贈卡。
- S0 停用不完整「重設學習」，明示不會刪資料；完整 learning erase 尚未支援。

實機30-case、OEM／TalkBack、完整資料清除、正式簽章與商店發布仍OPEN。公開Alpha的歷史host結果為15PASS/9FAIL/8SKIP；R3隔離候選的最新CI在H1證據擷取停止，App／Host未執行，不能稱完整驗收。[公開Alpha交接](docs/release/AIE_ALPHA_HANDOFF.md)與[未發布R3狀態](docs/release/R3_RELEASE_READINESS.md)分開記錄。

## 隱私與安全（程式實測）

- 無網路／震動權限：無 `INTERNET`、無 `VIBRATE`；`allowBackup=false`
- 全本機：輸入不傳輸、不記錄按鍵明文；詞庫與 userdict 在 app-private `noBackupFilesDir`
- 原生依賴固定 revision（`scripts/bootstrap_native_deps.sh`：fcitx5-android/prebuilt `3587ba33`、libchewing `a6a8fa4`），非浮動分支

## 建置

需求：JDK 21、Android SDK（含 NDK 28.2、CMake 3.22.1）。

```bash
bash scripts/bootstrap_native_deps.sh   # 一次性抓取 pinned 原生依賴＋詞庫
./gradlew :app:testDebugUnitTest        # JVM 行為測試；數量以本次報告為準
./gradlew :app:assembleDebug            # app/build/outputs/apk/debug/app-debug.apk
```

合約檢查：`scripts/check_*.py`（dachen／P0／editor_sync／P1／P2／P3）。Application ID：`io.github.juhjuhx.kuyin`。

## 架構

```text
InputMethodService → ComposeDecoderSession → ChewingEngineSession → JNI → libchewing
                   ↘ EnglishWordSession → offline EnglishLexicon
CandidatePageSnapshot → Compose strip / expanded grid
orderedCommits → InputConnection → editor acknowledgment
```

Service 管 Android lifecycle／editor effects；session 是唯一組字 owner；UI 不直接呼叫 JNI。Native 啟動失敗才使用 `ZhuyinDictionarySession`。候選預覽在 scratch context，避免破壞 live composition。詳細責任見 [ARCHITECTURE](docs/ARCHITECTURE.md)。

## 開源捐贈

捐款直接交予 FSF／ASF／Open Source Initiative／EFF，本專案不收取款項，亦不代表組織背書。公開Alpha仍含舊裝飾圖，請使用官方網址的複製／開啟動作。未發布R3已改標準離線QR，四URL／畫面解碼與quiet zone測試完成，**手機相機尚未驗證**；來源與限制見[QR provenance](docs/release/QR_PROVENANCE.md)，不能將分支修正當成既有Release內容。

## 授權

- 本專案：LGPL-2.1（見 `LICENSE`）
- libchewing：上游 https://codeberg.org/chewing/libchewing（LGPL-2.1 家族），出處與 pin 見 `NOTICE`＋`docs/UPSTREAM.md`
- 第三方：AndroidX／Compose／Room（Apache-2.0）、JUnit（EPL，僅測試）

## 文件

- `docs/COMPOSE-MAINLINE.md`：主線檔案審計（保留／刪除收錄／新增）、決策與路線圖
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)：現行規範架構，10章來源、ownership、資料與故障圖；舊 View 僅歷史參考
- `SECURITY.md`：安全聲明（與程式一致）
- `docs/ROADMAP.md`、`docs/HISTORY.md`：歷史路線與沿革（部分條目已過時，見審計文檔）
