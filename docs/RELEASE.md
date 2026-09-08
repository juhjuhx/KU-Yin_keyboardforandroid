# RELEASE.md — 發佈須知（zh-TW）

本文件說明本專案之發佈流程、簽署方式、權限設計與雙渠道發佈策略。

---

## 1. 雙渠道發佈策略

本專案支援以下兩個主要發佈渠道：

| 渠道       | 用途                         | 類型         | 簽署要求               |
|------------|------------------------------|--------------|------------------------|
| F-Droid    | 開源社群發佈                   | 自動化建置    | F-Droid 建置伺服器簽署  |
| Google Play| 商業渠道發佈                   | 上傳簽署      | 開發者自有簽署金鑰       |

### 1.1 F-Droid 渠道

- 透過 F-Droid 官方倉庫進行自動化建置與簽署。
- 建置伺服器將從公開原始碼倉庫拉取原始碼並自行編譯。
- 不需要（也不允許）上傳預建 APK 至 F-Droid 倉庫。
- 簽署由 F-Droid 建置基礎設施完成，開發者不需提供簽署金鑰。

### 1.2 Google Play 渠道

- 由開發者使用自有簽署金鑰進行簽署後上傳。
- 建議使用 Google Play App Signing（由 Google 管理簽署金鑰）。
- 每個發佈版本應使用 `versionCode` 遞增之版本號碼。

---

## 2. 簽署說明

| 項目                 | F-Droid           | Google Play              |
|----------------------|--------------------|--------------------------|
| 簽署方式              | F-Droid 建置伺服器  | 開發者金鑰 / Google Play AS |
| 簽署金鑰管理          | 由 F-Droid 負責     | 由開發者或 Google 管理      |
| APK 簽署格式          | APK Signature Scheme v2（或更新） | 同左     |
| 版本號碼格式          | versionCode 遞增    | 同左                       |

注意：由於兩種渠道使用不同之簽署金鑰，若使用者欲從 F-Droid 版本
切換至 Google Play 版本（或反之），將無法直接進行覆蓋安裝，必須
解除安裝後重新安裝。

---

## 3. 權限設計

### 3.1 極簡權限原則

本專案採用極簡權限原則，僅聲明最低限度必要權限。

| 權限名稱                              | 用途                           | 是否必要 |
|--------------------------------------|--------------------------------|----------|
| android.permission.VIBRATE           | 按鍵回饋震動                     | 否       |
| org.fcitx.fcitx5.android.permission.PLUGIN | 與 fcitx5 整合之插件概念權限 | 否       |

說明：
- `VIBRATE`：使用者可選擇啟用按鍵震動回饋，此為選用功能。
- `org.fcitx.fcitx5.android.permission.PLUGIN`：自訂權限，用於
  fcitx5 輸入法框架之插件概念整合。非必需，僅在與 fcitx5 整合
  時可能使用。

### 3.2 明確零網路聲明

本專案：

- 不要求 `INTERNET` 權限
- 不要求 `ACCESS_NETWORK_STATE` 權限
- 不要求任何網路相關權限
- 不會透過網路傳輸任何使用者資料
- 所有輸入法運算均於本機完成

此為本專案之核心設計原則，確保使用者隱私。

---

## 4. 獨立 applicationId 與 FileProvider

### 4.1 獨立 applicationId

本專案使用獨立之 applicationId，與其他輸入法應用程式區隔。

預設 applicationId：
```
com.example.androidkeyboard
```

注意：正式發佈前應更換為正式之 applicationId（如使用組織網域
作為前綴）。此處僅為開發階段之預設值。

### 4.2 FileProvider 設定

本專案使用 AndroidX FileProvider 進行安全之檔案共享。

FileProvider 配置要點：
- 應用程式資訊清單（AndroidManifest.xml）中須聲明 FileProvider
- 須提供 `file_paths.xml` 或等效之路徑配置
- 使用 `android.support.FILE_PROVIDER_PATHS` meta-data 指向路徑配置

FileProvider 配置範例：
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

路徑配置範例（`res/xml/file_paths.xml`）：
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="cache" path="/" />
    <files-path name="files" path="/" />
</paths>
```

---

## 5. 版本管理

### 5.1 版本號碼格式

| 欄位         | 說明                     | 範例          |
|--------------|--------------------------|---------------|
| versionCode  | 整數，每次發佈遞增          | 1, 2, 3, ... |
| versionName  | 語意化版本字串             | 0.1.0         |

### 5.2 版本命名規範

- 開發階段：0.x.y（x 為功能版本，y 為修正版本）
- 正式發佈：1.0.0 起
- 預先發佈版本：0.x.y-alpha.N 或 0.x.y-beta.N

---

## 6. 發佈流程

### 6.1 發佈前檢查

- [ ] 確認所有測試通過
- [ ] 確認無已知重大缺陷
- [ ] 確認版本號碼已更新
- [ ] 確認 CHANGELOG（如有）已更新
- [ ] 確認簽署金鑰可用（Google Play 渠道）

### 6.2 發佈步驟

1. 更新 `build.gradle` 中之 `versionCode` 與 `versionName`
2. 建置 Release 版本 APK/AAB
3. 使用簽署金鑰簽署（Google Play 渠道）
4. 上傳至 Google Play Console
5. 推送 tagged commit 至公開倉庫（供 F-Droid 自動建置）
6. 建立 GitHub Release（如適用）

### 6.3 F-Droid 自動建置

- F-Droid 將於新 tag 推送後自動觸發建置
- 建置結果將於數日至數週內出現在 F-Droid 官方倉庫
- 無需手動提交 APK 至 F-Droid

---

## 7. 注意事項

1. 雙渠道（F-Droid / Google Play）使用不同簽署金鑰，不可覆蓋安裝。
2. 權限已盡量精簡，未來新增權限應有充分理由並更新本文件。
3. 零網路原則為本專案核心價值，任何新增之網路相關功能應獨立
   為可選插件或另行評估。
4. FileProvider 為 Android 安全機制之必要配置，不可省略。
5. applicationId 決定應用程式之唯一識別碼，變更後視為全新應用程式。
