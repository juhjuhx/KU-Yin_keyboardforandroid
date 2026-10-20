# R3.1 獨立審查收束

更新：2026-10-20。基線 main63→PR20 de2d9df；本輪 source `97645c238dcecc43e677de625bf7049bd487d700`、workflow/runtime `feab29f53583c4bb8dfc4cffe13243a36a3e9d06`。兩位 read-only reviewer 分別從平台機制及整個 PR20 開始，沒有並行 source writers。

## 規格與工程品質

- **H1 source/helper：限定 PASS。** `scripts/emulator_health.py:332` 只增加控制終端，原 exit/timeout、XML/IC/focus/IME/user/boot oracle 不變。正常權限獨立37tests PASS；故障注入3.079s後 kill/reap，FD4→4。sandbox EPERM 的36/37結果保留，不當有意義產品 RED。
- **M1 MEDIUM 已補來源保護。** workflow RC guard 新增 b982 runtime pin及獨立97645c health pin，其餘 e137 paths 不放寬。最終13tests及 actual guard prefix 獨立 PASS；有 committed/dirty 反例。中間未批准health被拒的狀態與 RED 保留。
- **H1-only job：限定 PASS。** 最終 workflow SHA256 `fbca2662472939f49f9f000987bbb37414f0e07501469725c2a9dd1e55ddb77a`。四個真 Bash/fake process tests獨立PASS；scope明排除其他jobs，僅read權限下載固定原四APK，先H0，再host-only install/H1。這不是 Android PASS。
- **整 PR20：PARTIAL／Release NO_GO。** 實讀125files整合diff。Inherited M2手動publisher簽章／gate缺口仍 OPEN；privacy D2D、learning erase、runtime license inventory與正式key custody尚未完成。沒有執行publisher，未聲稱法律批准。

## 實際 CI 證據

獨立讀 [39426302308](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39426302308) 的176個raw檔，逐一驗SHA/size/集合、exact Git blob、log/receipt；212個Python測試名稱與summary一致，contracts10齊全，原四APK expected/observed及先前保存的reference bytes吻合。沒有把Python fixture的 INSTALLED 訊息當成產品在emulator安裝。

H0 raw重建 HEALTHY，僅initial/final bounded observation，非atomic/全程健康。H1 `ui-0`真有`-tt`，卻為59bytes null-root error、無XML、3006ms timeout；after WMS frozen=true。即使忽略timeout，原strict oracle仍INCONCLUSIVE，final恢復不能洗掉缺證。`collect_h1` capture failure分支正確停止。一次host安裝、exit3、emulator收束與artifact upload有log證據。

**High優先發布阻擋：H1恢復未達，深層因果UNRESOLVED。** 這不是已確認的產品decoder High漏洞。不得啟H2/App/Host/B01，更不能發行。

## 可追溯原報告

外部 `/Users/huang/Desktop/KU-Yin-R3.1-H1-Capture-20261019-211633/`：

- `R3_1_H1_PLATFORM_INDEPENDENT_RESEARCH.md`：固定AOSP來源及真正POSIX控制。
- `R3_1_ENTIRE_PR20_INDEPENDENT_REVIEW.md`：完整整合／privacy/license/signing範圍。
- `R3_1_FINAL_PATCH_INDEPENDENT_REVIEW.md`：分階段exact source hashes、實際測試、timeout cleanup及後加job獨立section。
- `R3_1_CI_RUNTIME_INDEPENDENT_REVIEW.md`：新176份raw重新裁定，未重跑Android或繼承主作者根因。

一位平台reviewer在後續複審遇額度限制，沒有盲retry或偽造新APPROVE；另一位已完成上述來源／runtime獨立核對。OpenCodeReview未向未批准provider送source/log，沒有freshOCR批准。整體 Release NO_GO。
