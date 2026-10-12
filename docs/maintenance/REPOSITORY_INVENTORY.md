# 倉庫檔案與模組責任

main基線730 tracked files；詳細逐檔meta是工程archive inventory/01-tracked.json。

| 路徑 | Owner / 用途 |
|---|---|
| app/src/main/java | Android service、Compose、session、Kotlin adapter、Room/設定 |
| app/src/main/cpp | 自有 JNI/CMake；.deps是可重建pinned輸入 |
| app/src/main/assets | 英文離線lexicon、bootstrap system字庫（fallback字典在Kotlin ZhuyinDictionary） |
| app/src/test / androidTest / debug | JVM、Compose、native boundary、裝置smoke/debug host |
| acceptance-host | 獨立test-only Views editor oracle，不依賴product/native |
| scripts | native bootstrap、10 contracts、runner、doctor、emulator orchestration及Python測試 |
| .github/workflows | blocking build、experimental emulator、manual publish、upstream watcher |
| docs/adr / review / archive | 決策與歷史證據；不競爭當前權威 |
| taui-workspace | 無Android consumers的web prototype；KEEP待owner決策 |

所有來源檔、native重建/重鏈結輸入、fixtures、credits與捐贈保留。資料治理見DATA_STORAGE_INVENTORY.md；archive來源見ARCHIVE_MANIFEST.json。
