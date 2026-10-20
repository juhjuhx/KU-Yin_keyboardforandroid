# Publication 文件與發行證據獨立審查

2026-10-21；目標 main575cc883 上的文件分支實際22檔 diff（含新增 JSON）。兩位 read-only reviewer 分別讀 source／Git blobs／原始證據；不是照抄作者結論，沒有對外上傳或執行 Android。

| Reviewer／範圍 | 規格 | 工程品質 | 直接核對 |
|---|---|---|---|
| publication_community_audit／README、社群、media | SCOPED_PASS | SCOPED_PASS | 13圖 bytes/hash/decode、AI標記、41相對連結、CI逐case、10份原稿及committed manifest |
| publication_release_audit／release、狀態、anchor | SCOPED_PASS | SCOPED_PASS | 八資產、四native／八詞庫、4806 tar hashes、57 JVM XML及兩 instrumentation XML、H1 raw、109／212 log分源 |

兩份是代理內部獨立審查，**不是 GitHub 官方 review approval、產品驗收或法律意見**。沒有新增 Critical／High 文件阻擋；產品發行仍 NO_GO。

## Findings 與處置

- **MEDIUM／繼承：** `.github/workflows/prerelease.yml` 的 manual publisher 未強制 H1／手機／approved signer／source-relink gates，仍可使用 CI Debug key 發版。新增 TODO 明列 OPEN；本輪不改／不執行該 workflow。RELEASE_GATE.json 只是狀態，不假稱已強制執行。
- **LOW／已修正：** 原 JSON `main_host_junit` 容易與 Host JVM37混淆，改為 `main_host_instrumentation`；15/9/8數值與原始 Android XML 相同，沒有換成 PASS。
- **證據時間邊界：** 07原收據是最後兩處時間措辭更新前的 staged snapshot，保留原樣；另07b直接比對commit6e4bcd7全部10檔一致，原00備份未改。JVM XML自身日期10月11日另明載，不冒稱本日重跑。
- **遠端覆核限制：** release reviewer 的額外 GitHub GET 因網路不可達未成功，遠端run/source映射依已保存metadata；本機XML與hash均直接重算。root另有實際 gh download／refs讀回；不能說 reviewer 獨立重跑了遠端CI。
- 圖片11項解碼失敗／品牌權利資料、H1 null root／timeout、手機／QR／更新／簽章／授權仍是公開 gaps，不由文件批准豁免。

## 收據與完成界線

完整報告保存在外部工程證據目錄，未將原始工程／裝置資料上傳。報告 SHA256：

- `實作複審-社群與圖片.md`：`25635fccb3ca8d6d77464b64dda670fbeb7b6c5eee3cbd64302c351e84d625ee`。
- `獨立審查-發佈文件最終版.md`：`144785d457b1183373d893adf514ad9f5b9f018826459359384a2ef311c97021`。

來源與命令見 [驗證表](PUBLICATION_VALIDATION.md)。提交前再次檢查 JSON、相對連結、diff 與文件路徑白名單；不含 Kotlin／JNI／scripts／workflow／二進位。Release、簽章遷移、實機與社群代發均未批准或執行。
