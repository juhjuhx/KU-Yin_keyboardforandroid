# 發布 TODO 與門檻

更新 2026-10-21；**NO_GO：不建立新 Release**。社群文件可獨立交付，不能代替產品驗收。當前來源與驗證見 [交接](R3_FINAL_HANDOFF.md) 及 [驗證表](PUBLICATION_VALIDATION.md)。

## 已完成的保存與可公開材料

- [x] main／PR18 合併狀態、About／Topics 唯讀核對，沒有覆寫既有資料。
- [x] R3.1 原十份未提交文件逐檔備份、SHA256 與隔離還原演練；文件 commit `6e4bcd772b800bf7ad98a2fbe02832a6517722ea` 已推原隔離分支。
- [x] 可用空間約 14 GiB；本輪刪除 0 bytes，沒有刪 worktree／證據／字典。
- [x] 公開 Alpha 八件資產、七項外部 SHA256SUMS、四 ABI native／八詞庫項目與 source/relink 4806 項內部雜湊重新核對。
- [x] 社群共用長短文、IG 文字、B 站簡體、致謝、FAQ 與素材規格收束為單一入口。
- [x] 現有 13 張圖片完成分類；實際操作截圖 0 張，不以品牌圖／測試圖替代。

## 下一個有界工程切片

- [ ] H1：先用已保存 raw 定位 null root／window replacement；建立單一可推翻的假說與隔離 RED，不增加 timeout、不盲重跑 Binding A/B。
- [ ] 新 disposable emulator 同一 serial／user／boot：fresh H0 → 真 Editor H1 → H2；H1 缺 XML／逾時即停。原 R3.1 CI 的 H0 不能跨 boot 重用。
- [ ] exact 整合 source 的 App5、Host33／32-case、B01；目前 main 的 Host 九項 StaleObjectException 仍是失敗，不能換成 SKIP。
- [ ] 指定經授權手機與安裝計畫：確認備用輸入法、APK／簽章／版本，執行 30-case、候選、Editor 寫入、QR、TalkBack 與保資料更新。不自動卸載或清資料。
- [ ] Legacy manual publisher hardening／release gate enforcement：既有 prerelease.yml 不讀 RELEASE_GATE.json，仍可用 CI Debug key 直接建立新版本。本輪不執行；發行前另切片收束，JSON 本身不是強制控制。
- [ ] 正式簽章 key ownership／保管／更新政策；現有 Debug 憑證有效不等於 production signing PASS。
- [ ] Native 完整 Rust／資料授權與重編／relink 證據、English 詞表 NOTICE 義務；Android 12+ OEM D2D 與 full learning erase 限制。
- [ ] 原始品牌圖權利資料，以及 11 張不可解碼圖片的有來源復原方案；另切片，不猜測改二進位。
- [ ] 圖卡方向待使用者確認；準備一致 4:5 圖卡／16:9 封面。真實畫面擷取、匿名化、逐張來源審查後才加入 README。

## 整合與發布

PR20 Draft、PR12 Draft、PR10 HOLD；README PR18 已合併。不得全量重複套用舊測試分支。文件分支可推送供 review；不要為文件變更重跑沒有新假說的昂貴 Android Runtime，也不改 CI 來隱藏失敗。

新的 RC 需要 exact source、獨立 review、Runtime、手機與簽章 gates；Stable 另需明確核准。既有 Alpha tag／assets 不移動、不覆寫。Windows 語音、Whisper、S2–S5、M4 不在本輪。
