# F3 主線維護交接

本文件只記錄 F3 已整合的來源及直接驗證；舊 FINAL_* 保留歷史，不能當成新的 Runtime 收據。查詢 actual main／PR／CI 後再續作，不自動重啟 Emulator 或發布。

|範圍|來源與證據|限制|
|---|---|---|
|Room 刪除 ACK|PR40，merge8015ead；DAO延遲 RED2失敗 → ACK7／UI4 PASS；exact PR CI40610484668 App278PASS/10nativeSKIP、Host57PASS；main CI40611061486 Build/Lint PASS|沒有schema更動；不證升級資料保留或完整學習清除|
|標準 QR|PR42，merge8a48902；RED6FAIL/2PASS → Asset2／UI6PASS，三種render設定×四機構12roundtrip；exact PR CI40611602943 App286PASS/10nativeSKIP、Host57PASS；main CI40612611342 Build/Lint PASS|main保留FSF/ASF/OSI/EFF原網址；手機掃碼NOT_TESTED；公開Alpha仍是舊圖樣|
|D2D|owner批准只sharedpref/kuyin_settings.xml；Draft PR41，XML3tests 2RED→3GREEN|未合併；不是OEM傳輸證據；Room／學習排除待裝置驗收|
|Host|目前main已有R37 bounded observer；PR27 geometry仍獨立Draft|歷史15PASS/9FAIL/8SKIP不改判；沒有新完整Runtime|

上述 main CI 的 Runtime job 均 SKIPPED，不能當輸入法驗收。

## 唯一新 bounded health 實驗

[CI40613937769](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/40613937769)，source `98cc3d05621b24d6c7e535ea2ffae17ff3072082`，API33/default x86_64、Pixel2、fresh no-snapshot，僅安裝合成測試 Host editor，沒有安裝 KU-Yin 產品。

H0=HEALTHY（僅該次有界觀察）；H1=INCONCLUSIVE。四份完整XML editor focused=false，程序自然exit0／EOF；最後一次只剩約64ms的capture逾時，不能稱所有capture完整。IC present不取代editor焦點。H2／App5／Host32未執行，保留原oracle，不再重試。Pixel7 RRO caller／freeze root仍UNRESOLVED。

原始ZIP SHA256：`1a9dfed01a13c8ad5bc12907d84ea0dbd735aab80f8f3c6f2115bfa79b88b880`。獨立review核對271檔、原oracle及disposition。下一個最小調查是 test-only View.hasFocus 與 Accessibility focused 的一致性，須先審查觀察規約。

## 發行與來源

公開 Alpha `v0.2.0-aie.1`／20003及八份資產不變。F2原簽章20005候選仍凍結；新的20006只作TEST_ONLY，狀態依實際候選PR／artifact，不保證保資料升級。簽章custody、實機、QR手機掃描、D2D、完整license/relink及Runtime仍阻擋新RC／Stable。

PR20只抽取QR；PR27／29／38保持隔離。沒有自動發布授權。canonical source在GitHub；本機 Workspace START_HERE／Task Ledger／F3 session保存私有原始證據，不整包上傳。
