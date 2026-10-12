# 清理候選與保留清單

本輪不做刪除；無reset/clean/prune/branch-D。逐檔size/hash/Git owner參見ASSET_MANIFEST.json與離線inventory。所有原位置可回退（原檔保留），deletion authorization=NO。

| 候選 | 分類 | 引用／可再生／privacy | 決策與rollback |
|---|---|---|---|
| app/build、.cxx、.gradle | REGENERABLE | Gradle/CMake；synthetic builds | KEEP本輪；不清cache |
| frozen APK/ZIP、native/relink | ARCHIVE | 既有驗收與update身份，不可用fresh build替代 | 外部hash副本＋KEEP原件 |
| dirty來源／doc／M4bench | KEEP | 未提交user work，尚未validated | patch＋source副本、restore drill；不併入產品 |
| taui-workspace/兩檔 | REQUIRES_USER_DECISION | 無Androidsource consumer；其他專案owner待確認 | exact bundle/檔副本；不移除 |
| userdict/DB/prefs/裝置證據/keys | REQUIRES_USER_DECISION | 私人或不明敏感資料；非可再生 | 只inventory；無針對許可，不copy/刪除 |
| 歷史review/archive/分支 | KEEP | provenance／rollback；可由Git追溯 | 新INDEX導航，不搬大量文件 |
