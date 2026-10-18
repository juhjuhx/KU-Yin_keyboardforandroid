# R2.3.1 Validation Matrix

Runtime source：`d26fb1b76e1f448f6004910b1189a513a13fc1a9`。唯一 CI [39080190653](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39080190653)，workflow_dispatch／attempt1；job117535398224，3m16s、SUCCESS。不能借用歷史 App／Host 結果。

| Gate | Fresh 結果 | 證據與界線 |
| --- | --- | --- |
| F1 RED | PASS（成功抓到舊缺陷） | 真 raw 只切換 frozen bool；舊版 assertion exit1，新版非 HEALTHY。獨立 raw oracle 重建相同 SHA；不是舊 run 真假 PASS |
| 邊界 RED | PASS | empty duplicate subcases 先兩個 assertion FAIL，再 count guard GREEN |
| Focused health | 36 PASS／exit0 | final source hash `6e031b…72121`；單元／fake ADB，不是裝置 |
| All Python | 192 PASS／exit0 | local131.431s；CI98.355s；無 FAIL/SKIP |
| Static contracts | 10 PASS／exit0 | local及同次CI逐一執行 `scripts/check_*.py` |
| Protected source | PASS／exit0 | base9622586→HEAD：app、acceptance-host、gradle、runtime_smoke、bootstrap 不變 |
| Syntax／YAML／diff | PASS | AST、YAML parse、staged diff check；LSP工具未提供、actionlint未安裝，不冒稱執行 |
| Source review | PASS（限定 scope） | 兩位 fresh-context read-only reviewer；source及manualhealth路由。11 assessor＋7 collector 獨立 oracle；空欄位finding RESOLVED |
| H0 | **HEALTHY／exit0** | 初始與final各一個 false、有效Launcher focus、無 timeout；user0/boot ID一致；PM/AM/WMS/IMMS/程序/boot證據具備 |
| Current ANR | 未觀測到 | lastanr明示none、captured log無相應critical歷史、current window無blocking alert；PNG為Launcher選單。不是原ANR因果已修復 |
| Raw／source identity | PASS | CI source SHA吻合；SHA256SUMS三來源hash吻合；100個artifact files（其中H0 98）另建SHA manifest；31個ADB command files無mutation |
| Final runtime independent review | **PASS（限定H0）** | 首次額度中斷；續作於恢復時段後完成，獨立raw重建HEALTHY、100/100 artifact hashes及31命令核實。不是binding／release批准 |
| H1／H2 | NOT_EXECUTED | health-only scope；舊probe及binding jobs確實skipped |
| App5／Host33／32-case／B01 | NOT_EXECUTED | 無APK install／instrumentation／case emits，不拿歷史結果填入 |
| Bootstrap／Gradle／APK assemble | NOT_EXECUTED | 本切片未改Android或native，health-only CI不建APK；歷史R2.3建置不作fresh證據 |
| Physical30 | NOT_TESTED | 沒有實體裝置操作；沒有核准安裝／切IME／資料變更 |
| Release／merge | NOT_PERFORMED | main/tag/release保留；新branch-only，NO_PR |

外部 evidence：`/Users/huang/Desktop/KU-Yin-R2.3.1-Health-Correction-2026-10-18-073541/`：11全量unit、12/15contracts、19 metadata、20完整joblog、21 raw、22作者重建、23 artifact hashes、24審查中斷與完成履歷、25精確log行。最終收據／manifest由交接入口指向。

## 限制

WMS initial151ms、final134ms pair 是兩個獨立讀取，不是 atomic snapshot。觀測間與未來是否凍結未證明；host helper缺libpulse造成version診斷exit127仍保留，實際headless啟動及SDK metadata證明版本。新版 Emulator健康不能證明 R2.1 remote/session 或原 SystemUI ANR 根因解決；health-only沒有原build負載，非單因果 A/B。
