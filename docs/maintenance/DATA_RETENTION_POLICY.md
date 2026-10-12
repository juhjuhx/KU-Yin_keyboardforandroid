# 資料保留政策

ACTIVE：本輪治理決策；不變產品既有儲存行為。

- 使用者字典、回饋、偏好與正式裝置資料永久KEEP於原位置，直到取得針對資料範圍的明確刪除／備份許可。不可用repo清理或app uninstall/pm clear替代。
- 既有產品沒有learning/feedback TTL或quota；此事實不等於推薦永久保留所有輸入。未來bounded counts/decay/private controls需ADR、migration/erase設計與使用者決策。
- 本輪rawprivate裝置證據/keys不copy、不上GitHub；需要保存時先確認owner及offline加密方法。synthetic userdict身份不明也保留，不以smallsize猜測安全。
- Code/docs/fixtures/native relink/release provenance保留；歷史review由INDEX導覽、Git追溯，不大量移動複製。
- 可重建build/cache列REGENERABLE但本輪不刪除；精確frozenAPK/ZIP不可用rebuild取代，不同signer不能更新。
- TAUI資料列REQUIRES_USER_DECISION；已保存exacttrackedbytes，無Androidconsumer；獨立專案位置和owner未確定前不遷移或刪除。
- 正常same-signature/versionCode升級保留appdata，不代表dictionaryassetrevision/migration已完备。nativeactivecontext禁止直接删userdict；Gitrevert不能復原私人DB資料。

執行任何未來刪除前需path/owner/hash/consumers/privacy/backup/restoreproof/approval；partialfailure不得顯示全成功。完整erase仍UNSUPPORTED。
