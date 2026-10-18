# KU-YIN R2.3.1 HEALTH PROBE HANDOFF

## SOURCE

BASE_SHA：`9622586acaa7e0b1335acee6b35d3b857bde7c20`

RUNTIME_SOURCE_SHA：`d26fb1b76e1f448f6004910b1189a513a13fc1a9`

BRANCH：`fix/r2-3-1-health-probe`

WORKTREE：`/Users/huang/KU-Yin_keyboardforandroid/.worktrees/r2-3-1-health-probe`

MAIN：`63c9e4ab8ad7e277013acb5d7cce99228ec1f827`。文件完成HEAD以外部 `R2_3_1_FINAL_RECEIPT.json`／Git核對，不能替換 runtime source。

## 裁定與變更

**F1 = CLOSED_WITH_EVIDENCE**（限定 frozen parser 缺陷）；**H0 = HEALTHY**（唯一新run有界觀測）。不是 IME binding／產品／實機驗收通過。

最小修正 `scripts/emulator_health.py`：單一明確false guard；initial/final都驗；保留first assessment；ADB raw/UTC/monotonic/exit/timeout與non-atomic metadata。真raw fixtures、有意義RED/GREEN、保守unknown/empty/conflict tests、health-only workflow。沒有新增wait/retry/dismiss、沒有改原App/Host assertions。

## Fresh evidence

- [CI39080190653](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39080190653)：exact runtime source、attempt1、job117535398224 SUCCESS；Python192、contracts10，H0 exit0。
- initial UTC `2026-10-17T23:48:00.190249+00:00`、final `23:48:03.847835+00:00`，各唯一false／Launcher focus、same user0/boot；原始四WMS命令各exit0、no timeout。
- 作者從raw重建initial HEALTHY，核final/PNG/identity/source三hash，31個capture command沒有install/select/instrumentation。current lastanr none，PNG為Launcher選單；不將其當真editor輸入證明。
- Local最終Python192 PASS／131.431s、contracts10 PASS；兩位source reviewer限定PASS，完整規格/結果見[驗證矩陣](R2_3_1_VALIDATION_MATRIX.md)及[獨立source review](R2_3_1_INDEPENDENT_REVIEW.md)。
- **INDEPENDENT_RUNTIME_REVIEW = PASS（限定H0）**：第一次因額度中斷；使用者續作且已過provider提示恢復時間後，原reviewer完成raw獨立重建，100/100 artifact及31命令核實，Spec/Quality PASS。完整報告 `RUNTIME_EVIDENCE_INDEPENDENT_REVIEW.md`，SHA256 `6afe38f9642556700f2287498039bd4252efcab37e030b759f659e9964eedcc9`。這不是binding／physical／release批准；原中斷履歷保留，不換未批准provider或重跑CI。

## 保存／剩餘缺口

Android/JNI/native pins、applicationId/version/signing、字典/Room/preferences沒有修改／讀刪。原22工區HEAD/status/tracked diff/cached核對一致，另R3工區仍clean；舊公開APK及v2/v3三指定資產SHA完全匹配。沒有重build或覆寫凍結archive。這不是全磁碟資料重新盤點。

H1/H2、App5、Host33／32-case、B01與physical均NOT_EXECUTED／NOT_TESTED。原remote method/session根因UNRESOLVED；兩個WMS讀取非atomic，bounded健康不保證持續健康。hosthelper缺libpulse/time metadata限制照實保留。

PR10 HOLD、PR12 Draft不變；不建立新PR（既有PR triggers會執行runtime，超出本次scope）。只推送新branch，沒有merge/release/tag操作。Production NOT_READY、M4 NOT_VALIDATED、S2 NOT_STARTED。

## R3 checkpoint

R3 mainline工區 `.worktrees/r3-release-candidate`、branch `release/rc-closure-r3`仍在main63、clean。假QR／OSI-EFF URL與watcher錯報已獨立初審，**尚未修正**；companion bundle路徑尚未取得。外部 `/Users/huang/Desktop/KU-Yin-R3-Release-Closure-2026-10-18-073017/R3_CHECKPOINT.json`與兩份初審保留。沒有新APK／UI／About／signing更動，也沒有發布。

## NEXT／STOP

本次CI raw獨立審查已完成。下一步只能在另一個明確批准的階段規劃fresh H0→H1／H2，不能直接重跑完整Binding A/B。本次AVD已由action kill；新H1須與當次新H0同serial/user/boot_id，不能跨boot重用本次result。R3 QR切片可獨立續作但不能將其當已完成。只讀本交接、anchor、具體source/evidence；無需再載入數百份歷史。外部evidence同驗證矩陣列目錄，下一命令為唯讀核 `19-ci-final-metadata.json`、`21-ci-artifacts/h0/` 與runtime source，不安裝任何APK。
