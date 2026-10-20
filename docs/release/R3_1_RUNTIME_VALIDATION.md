# R3.1 驗證矩陣

更新：2026-10-20。基線 de2d9df；H1 code `97645c238dcecc43e677de625bf7049bd487d700`。唯一新 CI exact workflow HEAD `feab29f53583c4bb8dfc4cffe13243a36a3e9d06`；文件 HEAD 不能代替 runtime source。

| 驗證 | 本輪結果 | 證據／限制 |
| --- | --- | --- |
| 原來源＋最終 H1 regression | RED：1 failure | 15 號 log；`HIERARCHY_UNVERIFIED`，10.210s；不是 import error |
| H1 focused | 37 PASS | 12 號 log；本機真 PTY 正常權限，非 Android |
| RC guard 原 runtime 漏保 | RED：2 failures | 07；committed/dirty runtime 原本誤放行 |
| 新 health amendment pin | RED：1 failure | 20；舊 e137 分組拒絕已批准修正 |
| 最終 guard | 13 PASS／actual guard exit 0 | 21；保護其餘 e137 infra、固定 b982 runtime、獨立 97645c health |
| H1-only CI route | RED：4 failures→4 PASS | 22/23；執行真 workflow Bash 命令，fake 子行程驗 H0/安裝/H1 fail closed |
| static contracts | 10 PASS | 19；每個 script 的命令、exit 均保存 |
| 全 Python 原 baseline | 205 executed、1 ERROR | 04；fake shell watchdog 30s；單獨正常權限 05 PASS，不推定根因 |
| 全 Python 修改後本機 | BLOCKED_EVIDENCE_IO | 日誌寫入 ENOSPC，無可審核完整結果，不填 PASS；CI 已獨立執行完整 suite，見下一列 |
| 全 Python 新 CI | 212 PASS／89.463s | CI39426302308 exact feab29f，25/python.log；獨立執行，不追認本機丟失結果 |
| 2026-10-21 新本機 Python | 212 PASS／unittest128.724s | 命令總耗時128.912s；磁碟恢復後重新完整執行，新evidence04，僅unit，不新增Android結果 |
| 新 CI contracts | 10 PASS | 25/contracts.log；source/APK guards先通過 |
| YAML / diff | parse PASS／diff-check PASS | actionlint 未安裝，不冒稱執行 |
| 原始 H1 raw | hashes retained | 10；原檔未改，不用新解析覆寫原裁定 |
| Android H0 | HEALTHY／exit0 | 唯一 CI39426302308，initial/final unfrozen，same user0/boot；bounded/non-atomic |
| Android H1 | INCONCLUSIVE／exit3 | ui-0 `-tt`、null root／無 XML／3006ms timeout；after frozen=true，final false；不得算 PASS |
| H2／App5／Host33／32-case／B01 | NOT_EXECUTED | 沒有可信新 H1 PASS 前停止；不重跑完整 Binding A/B |
| Gradle/JVM/native/APK rebuild | NOT_RUN_R31 | 本輪 Android/native source 零變更，H1-only 固定使用原 R3 四 APK，先驗 source/hash |
| 實機 | NOT_TESTED | 見 DEVICE_ACCEPTANCE |

外部 evidence：`/Users/huang/Desktop/KU-Yin-R3.1-H1-Capture-20261019-211633/`。開發中誤 mock、PTY helper 等待、sandbox EPERM、暫存缺檔及 ENOSPC 的失敗均保留；只有 final meaningful RED 與具正常權限的 GREEN 支持來源修正。

健康 gate 與產品 gate 分開：H0 HEALTHY 不證明 H1；H1 HEALTHY 不證明 KU-Yin binding；JUnit 綠燈也不代表完整 physical matrix。Release 仍 NO_GO。

## 新 CI 環境與保存

Ubuntu ImageVersion `20261018.339.1`、runner Linux/X64/4 CPU、KVM可用；API33 default x86_64 revision2、Pixel7 fresh AVD、Android user0。source.properties 顯示 Emulator37.2.12／ADB37.0.1；額外 `emulator -version` probe 因缺 libpulse.so.0 exit127，**不宣稱該 binary version 命令成功**。原環境完整 raw 保留；這個診斷錯誤不是已證明的 H1 根因。

四個不適用 workflow jobs 為 intentional SKIPPED；不是 App/Host test skip，更不能將未執行 suite 說成零失敗。CI整體 FAILURE，176份 raw已逐檔hash；`26-ci-receipt.json` 對應 run/job/log。source-preservation原24工區不變，總25。
