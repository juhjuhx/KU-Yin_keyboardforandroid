# KU-YIN R2.3 EMULATOR HEALTH HANDOFF

## Source／執行身分

- main：`63c9e4ab8ad7e277013acb5d7cce99228ec1f827`；base：`1ef5ba4c141ee09d3e8496ae47ce5d4329f2815e`。
- runtime source：`5ef8a979850d1588d6be68aca103e12cb51f4a6d`；branch：`fix/r2-3-emulator-health`；worktree：`/Users/huang/KU-Yin_keyboardforandroid/.worktrees/r2-3-emulator-health`。
- 唯一CI：[39047679642](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39047679642)，attempt1／job117439598233；exact source如上，最終FAIL。文件完成HEAD另見外部 `R2_3_FINAL_RECEIPT.json`，不是新runtime source。
- 外部證據：`/Users/huang/Desktop/KU-Yin-R2.3-Verification-2026-10-17-233234/`。目錄為開始日，CI與報告分別使用UTC／Asia/Taipei時間，不混用clock。

## 已交付

1. 原runtime opt-in H0：任何KU/host APK install前有界唯讀採證、三態failclosed、serial/user/boot守護、完整current窗口／lastANR／服務／程序／PNG與環境檔。
2. H1既有exported受控host，真EditText／focus／InputConnection／原defaultIME不變；本輪未獲H0批准，因此沒有執行。
3. A health拒絕時B不能由continue-on-error放行；仍保留原App5、Host33／32case與B01 assertions、timeouts。
4. binding job只補回R2 KVM grant作emulator設定變更；新健康觀測也改時序，不宣稱與舊run形成單因果完整實驗。
5. raw command/stdout/stderr/exit/timeout與derived WMS observation分開，source manifest239/239、artifact103檔、共用四APK workspace SHA guards核對。

## Fresh evidence

| Gate | 結果 |
|---|---|
| Local Python | 181 PASS／exit0，27 log，101.817s |
| CI Python | 181 PASS／exit0，102.436s |
| Static contracts | 10 PASS，local及CI均執行 |
| Bootstrap／Gradle | build SUCCESS；App/Host JVM tasks、四APK assemble完成；91 tasks executed／63 cache；未封存JVM逐test XML，count UNKNOWN |
| Source independent | APPROVE，限定exact source／unit gate；7初審finding已修正；不代表runtime批准 |
| H0 | INCONCLUSIVE／exit3；第一次currentFocus=null，displayFrozen=true；result reason WINDOW_INVENTORY_UNVERIFIED |
| H1／H2 | NOT_EXECUTED；H0未通過即停 |
| App5／Host33／matrix32／B01 | NOT_EXECUTED；沒有JUnit執行或case emits，不能拿歷史24PASS／9SKIP替代 |
| B arm | skipped；A outcome=failure，action conclusion受continue-on-error顯示success，job最終FAIL |

原流程有效RED與review修正RED見[驗證矩陣](R2_3_VALIDATION_MATRIX.md)，syntax/import錯誤及早期host ps故障不冒充產品RED。最終health26 focused與獨立malformed tests通過；全181包含該26。

## First failure／ANR／根因界線

H0 `window-observation.txt:305` 顯示displayFrozen=true、`:318` focus=null；final觀測`:339` frozen=false、`:352` Launcher focus恢復。保守結果保留初次失敗，不以後次正常狀態升格PASS。

lastanr明示 `<no ANR has occurred since boot>`、current windows無ANR dialog、captured system log沒有相應ANR/FATAL/timeout訊號。PNG為Launcher選單，不是ANR。因此SYSTEMUI_ANR_REPRODUCED=NO（只限本次captured scope）。首次啟動焦點／display過渡有直接證據；其觸發因果及原R2.2 SystemUI ANR／原remote binding根因仍UNRESOLVED。

runner原有setup在probe前注入keyevent82、改動畫與spellchecker；與Launcher選單可能有關，未做因果對照。這些既有disposable配置操作不等於IME軟鍵輸入證據，probe本身不dismiss／點Wait／清資料／選KU。

## 本輪環境

Ubuntu24.04.5／ImageVersion20261012.334.1，host4vCPU／15989MiB；Emulator37.2.12/build16428233、platform-tools37.0.1；API33/default/x86_64 image revision2／TE1A.220922.034/10940250；pixel7、guest2cores／2048MB；SwiftShader4.0.0.1 GLES3.0；guest kernel5.15.41 android13-8。

KVM12 installed/usable且/dev/kvm可讀寫；resolved disable=false／actual qemu無-accel off，boot metric38428ms。這不是直接KVM backend fd證明，也不是ANR已修復的因果證據。image metadata/config hashes有保存，完整system image bytes hash沒有。

`emulator -version`診斷helper exit127缺libpulse，保留該錯誤；actual headless launch與source.properties證明版本，不能把helper錯誤寫成沒啟動或偷偷補套件。

## Review／未解決項目

來源完整報告與hash在[R2.3獨立審查](R2_3_INDEPENDENT_REVIEW.md)。runtime獨立review已完成，overall NEEDS_REVISION：F1／P2為探針未明確拒絕displayFrozen=true的可重現缺口。本輪因另一守護條件已阻擋，沒有實際假PASS。依STOP不再改probe／重跑CI；另有P3跨命令／單次snapshot與bounded觀測限制；詳runtime獨立報告及本輪收據。source-only APPROVE不能覆蓋新runtime findings。

## 保存與Git

所有Android／JNI／pins／applicationId／signing／schema／使用者字典／Room／preferences不變；沒有任何實體裝置操作。21原工區HEAD/status/tracked binarydiff/cached核對一致，公開AIEAPK與v2/v3三指定資產exacthash保留；不是全部archive重新全面盤點。新CI APK hash列shared/SHA256SUMS，不冒充凍結APK；artifact未包含APK bytes，signer未另封存。

main、PR12 Draft、PR10 HOLD、release v0.2.0-aie.1/source2ce6810不變。不merge／release／S2／M4。

## 下一最小工作／STOP

另開狹窄test-infrastructure follow-up：以本輪真raw加入displayFrozen guard與有界唯讀boot/window readiness判讀，先RED／GREEN，再決定新的health-only驗證。不得只加sleep或取消focus／原visibility assertion，不在本輪重啟binding A/B。H1／H2必須留待取得新健康門檻批准。

PHYSICAL_ACCEPTANCE=NOT_TESTED；PRODUCTION=NOT_READY；M4=NOT_VALIDATED；S2=NOT_STARTED。
