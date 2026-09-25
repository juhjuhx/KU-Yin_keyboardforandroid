# COMPOSE 主線檔案審計（2026-09-20，分支 `feat/compose-mainline-v0.3`，PR #5）

本文件收錄主線化過程中的保留／刪除／新增決策，避免上下文超載與幻覺。程式狀態以碼為準；此處只記決策。

## 必須留下來的檔案

- `app/src/main/java/com/example/ime/**`：Compose 產品管線（service、engine、ui、settings、session 遷移層）。其中 `ime/session/`（C1–C3：`ComposeSession`、`ZhuyinDictionarySession`、`ChewingEngineSession`＋3 份合約測試）是 decoder 遷移的比較 harness，不可刪。
- `app/src/main/cpp/{chewing_jni.cpp,CMakeLists.txt}`＋`scripts/bootstrap_native_deps.sh`：與 `94dc7b6` 逐字一致的 JNI／構建／pin 腳本；四 ABI `.so`＋四詞庫已驗入包。
- `app/src/main/java/com/example/androidkeyboard/engines/**`：`ChewingEngine` contract、`AndroidChewingEngine`（已補 `canPage`）、`LibChewingDataInstaller`、`EditorPolicy`、`ImeSessionController`。
- `app/src/test/**`：61 測試全綠；6 合約腳本；Roborazzi 基線 `greeting.png`。
- `LICENSE`（LGPL-2.1）、`NOTICE`（libchewing 出處＋pin，與 `PROVENANCE.txt` 一致）、`SECURITY.md`（與程式一致）、`CONTRIBUTING.md`。
- `OpenSourceDonationCard.kt`＋`UpstreamAndCommunityCards.kt`：開源社群面（捐贈卡／repo 連結，離線版，無網路權限）。

## 已刪除並收錄者

- `metadata.json`：AI Studio 鷹架殘留（含不實 `GEMINI_API` 能力聲明），無任何引用，本輪刪除。
- `app/.../ime/sync/UpstreamSyncManager.kt`：`HttpURLConnection` 直連 api.github.com，與零網路政策衝突；已改為靜態離線卡＋瀏覽器 Intent（前輪）。
- `mipmap-*/ic_launcher.xml` 佔位向量 5 檔：與 `.webp` 重名導致資源合併失敗，已刪（自適應圖標＋webp 保留）。
- Firebase／OkHttp／Retrofit／Moshi／AppCheck 實現＋catalog 條目：零引用確認後全清。
- 剪貼簿**讀取**路徑（`getText`／paste bar）：零網路暨無收割政策，已刪；寫入（複製）為用戶手勢保留。
- 英文 `16dp` 側 stagger 以外的鍵間／行間／外緣死區：改為鍵內留白（視覺維持 44dp／40dp）。
- `gradle.properties` 的 `googleServices.missing.passthrough` 死註解：插件已除，順手清。

## 新增給 GitHub 者（本輪）

- `app/src/main/res/values/dimens.xml`（建置必需）。
- `ime/session/` 3 生產＋3 測試（C1–C3）。
- `ZhuyinDictionaryRankingTest` 8 則（P1 分層排序：精確＞延續＞回退）。
- `KeyboardHitboxContractTest` 5 則（行權重／Space 主導／特殊鍵／唯一標籤／單擊單回調）。
- `README.md` 重寫（本文件）；`.gitignore` 補 `cpp/.deps/`、`assets/`、`.DS_Store`。
- PR #5（`feat/compose-mainline-v0.3` → `main`）：新主線候選。

## 開源社群 QR Code 內容（待實機驗）

`OpenSourceDonationCard.kt`：FSF／ASF／Open Source Initiative／EFF 四基金會（中英名＋簡介＋`donateUrl`＋複製／開啟按鈕＋testTag）＋ `FoundationQrCodeCanvas`（21×21 手繪模組圖）。**誠實註記：手繪圖未經掃碼驗證，上機 retest 時必須實際掃描確認，驗過前視為裝飾；**驗過後如需真 QR，引入發射端編碼庫（需授權審查）另案處理。

## 上游更新（upstream）

- `docs/UPSTREAM.md` 有效：libchewing（Codeberg，`a6a8fa4`）＋prebuilt（`3587ba33`）＋四 ABI 產物＋字典，皆與現包一致；fictx5／FlorisBoard／HeliBoard 列為僅研究參考（禁未審拷貝）。
- 舊 `UpstreamSyncManager` 的「檢查更新」語義已按零網路政策退役，不恢復。

## GPL／授權更新

- 根 `LICENSE` LGPL-2.1 維持；`NOTICE` 已載 libchewing 上游＋revision，與實際 linkage（靜態 `libchewing_capi.a`）一致。
- 無 donor-copy 引入（移植皆為自有重寫＋測試；研究參考未拷貝）。
- 缺口：尚無 `PRIVACY.md`（Play Data Safety 不得推斷）；`metadata.json` 已刪不影響授權。

## 未決（需人批）

1. 實機 retest（10 項對比清單，前輪報告）＋QR 掃碼驗證。
2. C4 生產切換 libchewing（CASE-4 回歸先行；`dispatch` 可能升級 Result 形）。
3. 英文第二排 16dp stagger 死區（等 visual baseline `rowInsetWeight` 決策）。
4. Release key／AAB／F-Droid source lane／`docs/ARCHITECTURE.md` 重寫（C12）。

## 合併後記（2026-09-22，PR #5 MERGED）

- `feat/compose-mainline-v0.3`（`696c9e8`＋C1–C3＋P1 hotfix＋CI 對齊＋docs）已合併至 `main`（merge `278bcc2`）；舊 `main`（`653b4a3`）僅留 View 基線歷史。
- CI 對齊：wrapper Gradle 9.3.1（jar 納版控）、JDK 21、NDK 28.2＋CMake 3.22.1；P1 合約改認 wrapper；smoke 改真 applicationId；release 相關步驟暫出（等簽章決策）。
- 全新審查（2026-09-22）：OCR CLI 可用但 LLM 憑證 401，`ocr review` 未跑；改以 high-signal 手動審查三适配器＋CI＋smoke——無新增 critical（已知缺口：C2 空預edit Backspace 吞 surrounding-delete 待 C4 補；`dispatch` 表達力待升級；`KeyboardLayout.Dachen` 數據暫借舊包）。
- PR #4 維持 DRAFT OPEN 作 donor；`feat/keyboard-shell-v0.2` 等舊分支未刪（需逐個授權）。
- `APK/README.md` 版本號已跟進 `0.2.0-alpha.1`（release 列改為待簽章）；`metadata.json` 已刪；`.DS_Store`＋生成物已忽略。
