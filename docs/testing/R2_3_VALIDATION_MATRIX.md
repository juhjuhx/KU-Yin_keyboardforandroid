# R2.3 驗證矩陣

基線1ef5ba4；本輪runtime source與最終文件SHA分列handoff。外部證據：`/Users/huang/Desktop/KU-Yin-R2.3-Verification-2026-10-17-233234/`。

| 層級 | 狀態 | 證據／範圍 |
|---|---|---|
| 舊原流程 RED | 確認失敗exit1 | 03；可見ANR仍安裝/執行/exit0被assertion抓到，非syntax error |
| H1安全 RED | 確認2 assertion FAIL | 04；診斷失敗與user切換原本仍HEALTHY |
| reviewer完整性 RED | 確認失敗 | 10／12／15／24；header-only、timeout、package error、finalwindow、missinginventory、rawexit |
| Health focused | 26 PASS／exit0 | 25；fake subprocess／純fixture，不是Android runtime |
| H0/H1 orchestration | 3 PASS／exit0 | 20，另在final全量及CI執行 |
| 全Python | 181 PASS／exit0 | 27-reviewed-final，101.817s；早期失敗log保留，180前次不冒充最後181 |
| 靜態contracts | 10 PASS／exit0 | 06；不代表runtime |
| Bash／YAML／diff | PASS | 22及本輪命令；無原Android source diff |
| 舊工區／指定資產 | 21/21／3/3 unchanged | 23；HEAD/status/binarydiff/cached與指定3資產hash |
| 環境review | 完成對照 | 33/33 primary hash重核；07 window離線分類，非fresh H0 |
| Source independent | APPROVE限定source | exact hashes；7 findings已修正，獨立26＋1 PASS，非runtime批准 |
| CI build/bootstrap/JVM | SUCCESS | run39047679642 exact5ef8a979；Python181/10contracts/兩JVM task與四APK build成功；91 tasks executed/63 cache；JVM逐test XML未封存，不報count |
| H0 | INCONCLUSIVE／exit3 | 首次mCurrentFocus=null，WINDOW_INVENTORY_UNVERIFIED；最後focus恢復未覆蓋初次結果，無ANR被觀測 |
| H1／H2 | NOT_EXECUTED | H0不過，在任何KU/host install前停止，B skipped |
| App5／Host33／matrix32／B01 | NOT_EXECUTED | 非FAIL轉SKIP：本輪根本未到instrumentation；A failure/B skipped |
| Physical／release | NOT_TESTED／NOT_READY | 不以emulator代替實機；PR12 Draft／PR10 HOLD |

不是所有新測試都有獨立RED：只有上表保留的assertion RED可如此宣稱。早期full suite曾因本機ps權限問題失敗，採證記UNKNOWN而不升權；之後重跑與CI結果分列。原App／Host assertions、timeouts與Kotlin來源不變。

## 本輪 CI 裁定

唯一run[39047679642](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39047679642)，attempt1／exact5ef8a979，job最終FAIL，不能稱CI GREEN。A action的step conclusion=success受continue-on-error影響；真outcome=failure，rawscript exit3。shared檢查明確拒B，B沒有建立第二AVD。source239/239 hashes與四APK workspace hashes核對成功、artifact103檔清單已存36；不代表下載到四個APK或核實本輪signer。

H0命令全部有界完成：boot1、bootanimstopped、PM/AM/IMMS與process可回應，last-anr明示無ANR；第一次WMcurrentFocus=null、Launcher當時isVisible=false，final WM已指向Launcher。這是觀測到的啟動焦點過渡，不是ANR重現／產品缺陷／舊remote根因修復證明。PNG顯示Launcher選單；action在probe前有既有keyevent82與動畫設定，不能把這些setup當IME軟鍵驗收。

新環境：Ubuntu24.04.5／ImageVersion20261012.334.1、API33/default/x86_64 image revision2、Emulator37.2.12 build16428233；KVM12 installed/usable且qemu cmd無-accel off，boot metric38428ms。原ANR因果仍UNRESOLVED；不能單次KVM變更就稱唯一原因。`emulator -version`helper exit127缺libpulse，已保留；實際headless launch/version與source.properties另有證據。

TEST_INFRASTRUCTURE：unit/source gate PASS；EMULATOR_HEALTH：未取得PASS；IME_BINDING/HOST_RUNTIME：NOT_EXECUTED。遵STOP，不再重跑、dismiss、清資料、改產品或改assertions。

## 最終獨立gate

Runtime／evidence review **NEEDS_REVISION**：安全STOP成立、source239／workspace四APKguard已核，但F1／P2 displayFrozen guard缺口OPEN，另P3 snapshot／持續健康限制。source-only APPROVE不可蓋掉新finding。39-frozen-counterexample.json是離線反例，不是裝置PASS。本輪遵STOP不修碼、不重跑CI；不建議merge。
