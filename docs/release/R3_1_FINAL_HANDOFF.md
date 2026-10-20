# R3.1 Final Release Blocker Closure — Checkpoint

更新：2026-10-20。任務未完成，Release **NO_GO**。

## 2026-10-21 文件保存續作

遠端main已核為 `575cc883288a920e7aba021fb8705f75f465e1c0`（PR18正常合併，只有README／community文件），不能再把PR18標Draft。原10份未提交文件先原樣保存於 `/Users/huang/Desktop/KU-Yin-發佈收束-20261021-h3peyl7s/未提交文件原樣備份/`，manifest逐檔SHA吻合，另有隔離還原演練PASS。磁碟現約14GiB可用，不需要刪唯一資料；本Agent本輪刪除量0。

重新執行完整Python：212 tests PASS／unittest 128.724s／命令耗時128.912s／exit0，日誌 `04-R31-fresh-python.log`。這是新的本機unit證據，不代替舊失落結果或Android Runtime。以下2026-10-20來源／失敗紀錄保留；十份文件的本輪提交僅文件，不把其新HEAD當成新Runtime source。

## Source

- Runtime實驗當時main：`63c9e4ab8ad7e277013acb5d7cce99228ec1f827`；最新remote main見上方續作。
- PR20 baseline：`de2d9dfaa296a5cdeabde405d686720b615ce9e2`。
- worktree：`/Users/huang/KU-Yin_keyboardforandroid/.worktrees/r3-1-h1-capture`。
- branch：`fix/r3-1-h1-capture`。
- H1 code commit：`97645c238dcecc43e677de625bf7049bd487d700`。
- workflow/runtime commit：`feab29f53583c4bb8dfc4cffe13243a36a3e9d06`，已推獨立同名 branch。後續只文件，最新文件 HEAD/status 以 Git 及外部收據為準，不冒充新 runtime SHA。

## 已建立的證據

1. 原 H1 first readiness divergence 為 frozen；first capture divergence 為 marker-only／無 XML／3004ms timeout。凍結與 timeout 的深層因果 UNRESOLVED。
2. AOSP 支持 `/dev/tty` 非 raw ADB stdout 的傳輸缺陷；只加 `shell -tt`，原 3s/10s、strict editor/IC/focus/identity oracle 不變。
3. 最終 helper 對原 source meaningful RED；37 health／13 guard／4 finite-route tests 各自 GREEN，10 contracts PASS。
4. 新 runtime b982 與 health 97645c 分開 pin，其餘 infra e137 原封不動；actual guard 已直接執行 exit0。
5. 兩位獨立 reviewer 已研究平台與整個 PR20；最終 source 複審及其 exact hashes 位於外部原報告。新 editor-health job 另做限定 review，不沿用舊 workflow hash 的批准。
6. 唯一 [CI39426302308](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39426302308) exact feab29f：Python212／89.463s、contracts10、四APK source/hash PASS；H0 HEALTHY，H1 INCONCLUSIVE／EDITOR_CAPTURE_UNVERIFIED。`-tt` 已執行，但 null root／59bytes／無XML／3006ms timeout；before unfrozen/HAS_DRAWN、after frozen/DRAW_PENDING、final恢復。不同window identity不等於已知因果，深層root仍UNRESOLVED。
7. 新CI raw已獨立重建H0/H1、176檔manifest與exactGit source，停止／保存PASS；H1恢復未達。見[獨立審查](R3_1_INDEPENDENT_REVIEW.md)，不是整體產品APPROVE。

## 阻擋與下一步

- 本機磁碟曾僅約 350 MiB，兩次 ENOSPC；修改後 full Python 的結果無法落盤，**不列 PASS**，不刪其他資料換取綠燈。已請使用者釋出空間或指定外部位置。
- 有界 CI 已執行且拒絕 H1，**停止，不重跑**。產品未安裝、未選 KU-Yin、H2/App5/Host33/32-case/B01未執行。下一最小工作是用現有 raw 建立 UIAutomator root/window replacement 的假說與受控觀察計畫；不以加 timeout、隱藏 skip 或改 assertion 解決。
- 只有新 H1 真 PASS，才可在下一明確階段 fresh same-boot H0/H1/H2/App5/Host33/32-case/B01。H1 拒絕就保存 raw 並停止，不能 retry 至綠或加 timeout。
- 實機／手機 QR scan／signing custody／安全升級／privacy D2D／license release gate OPEN。不得發行或合併 PR10/12/18/20。

## Evidence / navigation

`/Users/huang/Desktop/KU-Yin-R3.1-H1-Capture-20261019-211633/`：00 原工區基線、02 remote refs、03 skill/tool route、10 原 raw hashes、15 meaningful RED、19 contracts、20/21 source guard、22/23 finite-route、24原24工區保存、25唯一CI raw176檔、26manifest/log/receipt、兩份獨立研究與 final patch/runtime reviews。

先讀本檔，再讀 [ROOT_CAUSE](R3_1_ROOT_CAUSE.md)、[RUNTIME_VALIDATION](R3_1_RUNTIME_VALIDATION.md)、[DEVICE_ACCEPTANCE](R3_1_DEVICE_ACCEPTANCE.md)、[RELEASE_READINESS](R3_1_RELEASE_READINESS.md) 及必要 raw。原 R1/R2/R3 handoff 保留，無須整庫重讀。

Rollback：正常 revert 本 branch 的新 commits；原 dirty root、24 個舊工區及 release 資產不得 reset/clean/覆寫。沒有 LSP/init/compact 專用 API；此 checkpoint 是持久上下文，不宣稱不存在的工具執行。
