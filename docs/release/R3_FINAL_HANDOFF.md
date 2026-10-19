# KU-Yin R3 RC Closure Handoff

2026-10-19；**RELEASE_VERDICT=NO_GO／NOT_PUBLISHED**。本輪允許的QR修正、受控CI、材料與證據收束已執行；未滿足的runtime／實機／簽章／隱私／授權gate不是已完成驗收。不自動啟動新的修復、merge或Release。

## 來源與Git邊界

| Identity | Exact value |
|---|---|
| MAIN_BEFORE／MAIN_AFTER | `63c9e4ab8ad7e277013acb5d7cce99228ec1f827` |
| Branch／worktree | `release/rc-closure-r3`／`.worktrees/r3-release-candidate` |
| QR product slice | `de2ac7d94e0a79f492d11ee165173e65b4562bd7` |
| Reviewed test lineage | `e137e37594c04f8c0fd88c01ef911d1df9bcbc49`；正常merge parent185a4e5，沒有重複套PR10/12 |
| Build／runtime source | `b98206267b3a65c452db1661bb3bae910ec61f6a` |
| Documentation HEAD | 由 `git log -1 --format=%H -- docs/release/R3_FINAL_HANDOFF.md`／外部final receipt取得；不是新runtime驗證SHA |
| Frozen release | `v0.2.0-aie.1`／tag `2ce6810684fd65a65a3006a224dc786b1f3a5f62`，未修改 |
| Other PRs | PR10ea5c2b5 OPEN/HOLD；PR12d6bf2a6 Draft；PR18 4190e0a Draft，README獨立review；Windows13/14排除 |

只推隔離branch與準備Draft review，不merge main；本輪PR連結由branch的 `gh pr view`／external final receipt核對，不預填虛構PR號。24工區保留；原root dirty AGENTS/untracked與其他工區的HEAD/status/diff另對00/41保存，沒有reset/clean/prune。未讀／刪私人userdict、Room或prefs；舊Alpha APK／source tar、指定P1-G四APK／v3ZIP雜湊重驗一致。

## 最小產品修改與RED/GREEN

唯一產品Kotlin差異為 `OpenSourceDonationCard.kt`：假Canvas→標準離線QR、完整官方URL、整數module／quiet zone、foundation-specific semantics與誠實未實機掃描文字。沿用Compose視覺、copy/open、直接交組織／非背書說明；四PNG＋manifest、NOTICE、版本20004、test-only ZXing。沒有新INTERNET／camera／mic、沒有新typing功能。

07實際舊UI解碼ChecksumException／錯URL RED；17 corner quiet-zone真RED→18八測／十二rendered captures GREEN。19 boot counterexample兩RED→20兩GREEN；21 approved-source guard、23 duplicate route、25七個root-build/JVM反例RED→22/24/26 GREEN。初始化／下載／qualifier harness錯誤沒有充當產品RED。詳QR_PROVENANCE及external logs。

Dachen／candidate/session ownership、native JNI/pins、orderedCommits／InputConnection ACK及原App/Host assertions／timeout不變。CI gate固定reviewed source與同serial/user/boot/H0bytes；不提高timeout或放寬斷言。

## Fresh執行結果

- Python205 PASS、十contracts PASS、scripts AST／Bash／YAML PASS。
- App JVM290＝280PASS／10native-host SKIP；Host JVM123PASS／零SKIP；fresh `--rerun` Test tasks。四APK assemble exit0。不能稱十native-host integration已執行。
- 唯一CI [39152444358](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39152444358) exactb982、attempt1、FAILURE。
- H0=HEALTHY，限單次run的有界initial/final觀測、非atomic／非持續健康；H1=INCONCLUSIVE／EDITOR_CAPTURE_UNVERIFIED。Host helper實install1、launch1；UI dump3004ms timeout，只有33bytes提示而無XML。before WMS frozen=true、after/finalfalse；截圖ICtrue不補XML缺口。深層根因UNRESOLVED。
- H0 version helper127缺libpulse屬非gating；原log的Bluetooth E／Throwable與H1 ANRhistory缺口保留。不能說整次無ANR／平台error。
- **H2／產品install／IME select／App5／Host33／32-case／B01均NOT_EXECUTED**。沒有重跑CI／新AVD／hardware Enter代替soft key。
- 實機30-case／QR相機／TalkBack／保資料更新NOT_TESTED；沒有裝機或修改真裝置state。

逐命令／exit／test count見[validation](R3_RELEASE_VALIDATION_MATRIX.md)；runtime未完成的cases不能從舊R2 PASS拼接。

## 本機APK與材料

外部根目錄：`/Users/huang/Desktop/KU-Yin-R3-RC-Closure-2026-10-18-205053/`。

- `candidate-artifacts/KU-Yin-0.2.0-rc.1-R3-debug.apk`：30,371,415bytes；appID `io.github.juhjuhx.kuyin`；20004／0.2.0-rc.1；SHA256 `c171a3455acb1cb4a3b16ca0eb65e483620c35a224a308f398ad65c1867f0416`。
- Debug certificate SHA256 `46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94`；apksigner v2 verify0，與公開Alpha同cert。是必要靜態更新條件，實際手機更新／資料保留未測，正式長期key custody未證。
- 四ABI與四dict／English notices保留；產品無INTERNET／CAMERA／RECORD_AUDIO。HostTest另含INTERNET，三helper/instrumentation不是一般使用者產品APK。四APK DEX未含ZXing/OpenCV。
- source/relink tar43,017,050bytes；SHA256 `00c43969da91acf24fd32874b6c5c00eaf2cdd46dc5ffaf775040b47cdebee09`；4816 manifest entries重驗。當次JNI object copies strip-debug、原archives與舊pin一致；JNI code未變。四ABI新link的.text/.rodata與本機APK相同，fullELF不同，未重編Rust。
- `R3_release-provenance.json`、材料內source-provenance/relink-verification、外層SHA256SUMS／LICENSE／NOTICE／RELEASE_NOTES／DEVICE_ACCEPTANCE均分明；**cryptographically signed provenance NOT_PROVIDED**，不假造Release key。

CI APK與本機四檔都不同，CI cert `02a1b82be234d9e9b6fb71e55f96d582b3534768fa845f7499740f16b8959b69`；四JNI完整bytes亦不同，不能全歸簽章，也未宣稱跨環境整APK可重現。CI source267/267 hashes對Git相符，與artifact byte等價是不同gate。

## 獨立review與OPEN gate

[Review index](R3_INDEPENDENT_REVIEW.md)分開spec／quality／實際執行scope。QR source與CI source窄審查PASS；runtime source/stop契約PASS但觀測能力PARTIAL；artifact靜態核對PASS。新材料獨立SPEC／QUALITY SCOPED PASS，不代表full legal approval。最後文件review因服務額度中斷未產完整報告，REVIEW_INCOMPLETE；保留已完成source／runtime／artifact／material報告，不冒稱整體APPROVE。OCR未上傳到未批准provider，NOT_RUN，歷史401不充當fresh結果；沒有其APPROVE。

完整Rust runtime subset／COPYRIGHT-library.html與個別notice仍OPEN；vendor r-efi5.3.0、wasip2 1.0.1+wasi-0.2.4有license expression但notice_files空，繼承原包，不宣稱76 crates每份notice完整或已確認這兩者進Android runtime。D2D／OEM、fullerase unsupported、feedback刪除ACK、正式signer與實機仍有獨立gate。[Readiness](R3_RELEASE_READINESS.md)保持NO_GO。

## 續作：只做下一個最小任務

1. 先讀本檔／CURRENT_STATE／ARCHITECTURE，核 `git status --porcelain=v1 -uall`、`git rev-parse HEAD`及remote refs；不得覆寫他人dirty。
2. 從external `36-ci-runtime-raw/build/r22-preinstalled/h1/result.json`、`commands.json`及 before/after windows、ui-0 stdout／stderr／command JSON查H1 capture首敗。保留原3000ms bounded gate、frozen不利觀測與無XML；先寫可反證假說／最小RED，再決定test-only修正。不要直接重啟Binding A/B。
3. H1有效PASS後才可新明確階段恢復H2/App/Host/B01；每次新boot重新H0，不能用本次結果授權下次裝機。
4. 其他blockers各自解：owner signer custody／data-safe update決策、授權手機plan、D2D政策、完整native/license inventory／phone QR／TalkBack。沒有Critical gate全關就不建新Release。

回退只正常revert branch提交；不改已發布tag、不要求卸載／降級／清字典。S2／M4／ASR與Windowsvoice不在本輪，亦不因本輪build通過自動開始。
