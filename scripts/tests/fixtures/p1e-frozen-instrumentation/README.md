# 凍結 P1-E instrumentation 來源

這八份 Kotlin 檔是 Git commit `6595b68403232187eba4d9ec10c7f0689c2b5b61` 的 exact bytes，對應原 `scripts/p1_instrumentation_manifest.json` 八個 SHA256。僅供 Python frozen provenance tests；不編譯、不複製到 APK。完整小型 fixture 讓 shallow CI 和 portable tests 可以驗證歷史契約，避免依賴網路或將 frozen 26-test runner 誤套 current 32-test suite。缺檔、bytes 不符均 FAIL；原 manifest、runner 與四 frozen APK 不變。來源授權沿用本專案 LICENSE／NOTICE。
