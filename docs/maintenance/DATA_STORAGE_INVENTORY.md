# 資料儲存清單

R1 source audit基線a6a4c65；沒有讀取裝置資料。DEVICE_LOCAL_DATABASE_INSPECTION=NOT_TESTED。本輪不改schema/prefs/native資料。

| Store / 實際位置（app-private相對） | Owner / 內容 | Schema / migration | 隱私 / export | 刪除、升級、rollback |
|---|---|---|---|---|
| no_backup/libchewing/user/userdict.dat | libchewing live context個人詞彙／learning | format交給pin；非app Room，不假定SQLite；無appmigration | PERSONAL；無完整export/import/erase | 禁刪activecontext檔；same-signatureupdate保留；需另外consent＋安全offlinebackup |
| shared_prefs/kuyin_dict_prefs.xml | ZhuyinDictionary `freq_<word>` Int；IMEfallback＋MainActivity試打 | 未versioned／無migration | PERSONAL；無wordexport | fullreset停用；native/fallback非atomic；不能只清它稱全部成功 |
| databases/kuyin_app_database＋可能WAL/SHM | FeedbackViewModel→Repository→DAO；feedback_entries | Room v1/exportSchema=false；無migration/destructivefallback | email/title/description/裝置資訊，PERSONAL；單筆JSON/report/user-share | 手動delete/allclear有API；无TTL/quota；same-signatureupdate保留；backup需一致DB snapshot、目前未執行 |
| shared_prefs/kuyin_settings.xml | KeyboardSettings；vibrate/sound/height/theme/clipboard/font/simplified | key/value未schema-versioned | USER_PREFERENCE；無完整export | resetlearning不清；upgrade保留；針對許可才能backup |
| shared_prefs default AndroidX prefs | IMEConfig舊View shellconversion/haptic/proximity | legacy keys | USER_PREFERENCE；現行service不用 | unregisteredlegacy不要當Compose設定；KEEP |
| APK assets/libchewing/*.dat→no_backup/libchewing/system | installer/capi system dictionaries | pinnedprebuilt；既存非空跳過，無assetrevision更新 | PUBLIC可重建 | 可由exactAPK重建，但不能混同userdict；hash/migrationgate OPEN |
| APK assets/english/word_frequency.tsv | EnglishLexicon 10k固定來源 | SOURCE.md/hash/Apache notice | PUBLIC唯讀；無learning | rebuild；prefix查詢，非autocorrect |
| fallback Kotlin字典 | ZhuyinDictionary bundled data | source Git版本 | PUBLIC唯讀＋另freqprefs | code可revert；freq不是code |
| app/host tests/fixtures | synthetic regression data | fixture source version | SYNTHETIC；不用真privatetext | 仍被test引用KEEP；不能以synthetic marker稱nativeerase |
| .gradle/build/.cxx/.deps | Gradle/CMake/bootstrap | pinnedinputs與可再生outputs | 大多REGENERABLE；按來源reviewlogs | 本輪KEEP；先保存重要evidence，不能git clean -fdx |
| Git/release/archive | refs、source/relink、exactAPK | GitSHA/version/hash | PUBLICSOURCE/EXTERNALOFFLINE | 原tag、APK與本地dirty均KEEP；正常revert/isolatedrestore |

## 實際source與呼叫點

- `app/src/main/java/com/example/feedback/data/{AppDatabase,FeedbackEntity,FeedbackDao,FeedbackRepository}.kt`：v1 DB、單筆與全部delete；ViewModel insert/delete/share，MainActivity feedback UI。沒有ranking/keyboardevents表。
- `ime/settings/KeyboardSettings.kt` seven setters用apply；MainActivity主題/聲音/震動/font writers，UI/FeedbackHelper讀取。height/simplifiedsetter存在不代表活躍feature。
- `ime/engine/ZhuyinDictionary.kt` freq讀寫與fallback-only clearAPI；fullreset無productioncaller。MainActivity內嵌trial亦用它。
- `androidkeyboard/engines/android/LibChewingDataInstaller.kt` system/user路徑，既存非空檢查；`AndroidChewingEngine.kt` nativelearningpolicy與scratch :memory:。

## Backup界線

allowBackup=false不是全部OEM device-transfer禁用保證。現行data_extraction_rules.xml只有commenteddevice-transfer sample；native noBackup excluded，Room/prefs D2D仍需實測/安全政策。此推論基於[Android官方Auto Backup](https://developer.android.com/identity/data/autobackup)，不是本輪裝置結果。設定／詞彙加密、完整erase/export/migration皆未提供；不用文件宣稱已實作。
