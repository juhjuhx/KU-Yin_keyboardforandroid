# APK 簽章與更新相容性

## 已核對的包身分

|物件|版本|APK SHA256|Signer SHA256|用途|
|---|---|---|---|---|
|公開 Alpha|20003 / 0.2.0-aie.1|`acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`|`46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94`|原有 Experimental Release|
|main 3aa90d5 CI APK|20003 / 0.2.0-aie.1|`c3080ade0f35bb81b3413df38605ced4d61cc2d3ed0d23c6f443ab7858500bd8`|`6d2d10a6042bd1f806efd6dcea94deee1800164e3a383de354ef96bc0272709a`|本機 TEST_ONLY，未發布|

兩包 applicationId 都是 `io.github.juhjuhx.kuyin`，minSdk24；已用 `apksigner verify --print-certs` 驗得單一 v2 signer。不同憑證的 CI APK不能當成公開 Alpha 的正常保資料更新包。不要卸載、清資料或刪字庫來避開更新錯誤。

## F1 簽章調查（2026-10-27）

維護者在本機以 `keytool -list` 核對既有 Debug keystore 的公開憑證，與公開 Alpha 的 `46488e…da94` 相同。因此原始憑證可在本機找到，不能再宣稱原簽章已遺失。本次未匯出私鑰、未將 keystore 放入 GitHub 或一般工程封存。

這只證明 keystore entry／certificate 身分。尚未證明私鑰簽署新 APK、受控備份還原、較高 versionCode 更新或 userdict／Room／偏好的完整保留。Debug key 仍是開發用途，不能把它改稱正式 Release key。

## 更新決策

1. 保留公開 Alpha、舊 tag 與八份資產；現有 CI 包維持 TEST_ONLY。
2. 下一個候選須從固定 Git source 建置，在可審查提交中設定大於20003且未佔用的 versionCode，以及可區別的 versionName；不可只改檔名或重簽舊包便宣稱新版本。
3. 維護者先完成 key custody／加密備份決策，再於本機使用原 key；不得將其上傳臨時 CI 或遠端 Reviewer。
4. 在一次性合成資料環境執行 Alpha→候選的正常 `adb install -r` 更新，驗證設定、測試學習資料與文字效果。出現 signature/downgrade 錯誤立即停止；不得用卸載處理。
5. 同憑證是必要條件，實際更新及 Runtime／授權等 Gate 尚需直接證據。未全部完成不發布新包。

F1 的既有 Emulator 在產品安裝前已有 SystemUI ANR 阻擋視窗，H0=UNHEALTHY，H1/H2及更新未執行；實體手機未連線。這是單次環境前置觀察，不能推論 KU-Yin 產品根因。

## API24與金鑰輪替

APK v3 proof-of-rotation 自 Android9/API28 提供。KU-Yin 支援 API24，不能以現代 Android 的輪替能力推定 API24–27可接受不同key。本次未驗到可用 rotation lineage；新正式key或新package身分屬後續產品／資料遷移決策。

官方來源：[Android app signing](https://developer.android.com/studio/publish/app-signing)、[AOSP v3](https://source.android.com/docs/security/features/apksigning/v3)、[apksigner](https://developer.android.com/tools/apksigner)。

下一步：[受控手機驗收手冊](DEVICE_ACCEPTANCE_F1.md)。新 Experimental／RC／Stable仍為 NO_GO。
