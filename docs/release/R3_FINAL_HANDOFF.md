# Publication Closure — 短交接

2026-10-21；文件／發行材料切片；**RELEASE_VERDICT=NO_GO**。本文件是 main 上的新續作入口，不取代隔離分支的 R3／R3.1 原始證據。

## 來源

- main 基線 `575cc883288a920e7aba021fb8705f75f465e1c0`；README PR18 已合併，63→575 僅文件。
- 本輪分支 `docs/publication-closure-20261021`；只改文件與 JSON，最終提交 SHA 由 Git／PR／外部收據取得，不在同一 commit 中偽造 self-hash。
- PR20 `de2d9dfaa296a5cdeabde405d686720b615ce9e2` 仍 Draft；沒有新 RC Release。
- R3.1 runtime source `feab29f53583c4bb8dfc4cffe13243a36a3e9d06`；原十份文件保存 commit `6e4bcd772b800bf7ad98a2fbe02832a6517722ea` 已推原隔離分支，工區乾淨。
- [R3.1 exact handoff](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/blob/6e4bcd772b800bf7ad98a2fbe02832a6517722ea/docs/release/R3_1_FINAL_HANDOFF.md)；[root-cause raw 索引](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/blob/6e4bcd772b800bf7ad98a2fbe02832a6517722ea/docs/release/R3_1_ROOT_CAUSE.md)。

## 本輪已交付

十份原稿逐檔備份與獨立目錄還原演練；磁碟約 14 GiB free，刪除 0。社群文案收束、About 唯讀核對、13 圖分類、公開八資產／APK cert／source/relink bytes 核對、真實 main CI XML 及 Release gates 均有紀錄。原 worktrees、使用者資料、簽章、版本與發布 tag 不改。

原始命令／manifest／XML／review 保存於外部 `KU-Yin-發佈收束-20261021-h3peyl7s`。主入口是 [驗證表](PUBLICATION_VALIDATION.md)、[TODO](TODO_AND_GATES.md)、[機器門檻](RELEASE_GATE.json)、[社群包](../community/README.md)。原始備份含使用者工作資料，不作 GitHub artifact。

## Runtime 邊界

main CI39442957936：blocking build 成功，experimental Runtime 失敗；App5 PASS，Host15 PASS／9 FAIL／8 SKIP。九項 failure stack 均有 StaleObjectException，未在本輪修補或證明產品根因。R3.1 CI39426302308：H0 HEALTHY，H1 null root／無 XML／3006ms timeout，after WMS frozen=true；深層根因 UNRESOLVED，H2／App／Host 未執行。兩者來源不能拼成 RC 通過。

## 下一步與停止線

先以現有 raw 提出單一 H1 可驗證假說、meaningful RED 與新 disposable emulator 的 bounded health-only 計畫；不盲重跑、不加 timeout、不弱化 assertion。可信 fresh H0/H1 後，另一明確階段才 H2／App／Host。實機缺授權計畫、圖卡缺方向、簽章遷移／D2D／完整 license review 仍 OPEN。詳見 TODO。

Rollback：正常 revert 本輪文件 commit；十份原稿另有 bytes 備份，不 reset 其他工區。未新增 Release／社群發文／產品 patch。無全域 init/compact API，不改 Codex 設定；依本短入口續作。
