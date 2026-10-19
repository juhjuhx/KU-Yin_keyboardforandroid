# R3 RC 驗證矩陣

2026-10-19；來源 `b98206267b3a65c452db1661bb3bae910ec61f6a`。本表是此來源的本輪證據，不沿用 R2 偶發／分支結果。**RELEASE=NO_GO**。

外部證據 session：`KU-Yin-R3-RC-Closure-2026-10-18-205053`。原始 log／XML／下載 artifact 保留；文件後續 commit 不改寫 runtime source 身分。

| 層級／命令 | Fresh 結果 | 證據／限制 |
|---|---|---|
| `KUYIN_DEPS_CACHE=$PWD/build/r3-native-cache bash scripts/bootstrap_native_deps.sh` | exit0；四 ABI／四 dat staged | 03；固定 prebuilt，不是 Rust source rebuild |
| `python3 -m unittest discover -s scripts/tests -v` | exit0；205 PASS | 28；CI 同 source 205 PASS；deterministic fake ADB 不是裝置 |
| 全部十個 `scripts/check_*.py` | 10 PASS／exit0 | 28；static contracts，不是 Runtime |
| Bash syntax／scripts AST／workflow YAML | exit0 | 28；未使用不存在的 LSP API |
| `:app:testDebugUnitTest --rerun` | 290 tests：280 PASS／10 SKIP／0 FAIL | 31／32 XML；native-host opt-in 十測未執行，不能稱 native integration PASS |
| `:acceptance-host:testDebugUnitTest --rerun` | 123 PASS／0 SKIP／0 FAIL | 31／32 XML；JVM，不是 Host instrumentation |
| App／Host Debug 及兩份 AndroidTest assemble | 四 APK build exit0 | 31；未重建／覆寫舊 Alpha／P1 archives |
| QR focused resource＋Compose tests | 8 PASS；12 rendered captures roundtrip／white quiet zone | 17 真 RED→18 GREEN；列於 App suite，不重複加總 |
| Fresh 四 ABI relink | 四次 exit0；各 `.text`／`.rodata` 與本機新 APK 逐 byte 一致 | 37；完整 ELF 不同，不是整 APK reproducible 或 Android execution |
| 新 source/relink archive | 4,816 entries SHA 通過；共 4,817 regular files | 39；舊 source／verification 沒有冒充本輪結果 |

Gradle 實際命令：`./gradlew :app:testDebugUnitTest --rerun :acceptance-host:testDebugUnitTest --rerun :app:assembleDebug :app:assembleDebugAndroidTest :acceptance-host:assembleDebug :acceptance-host:assembleDebugAndroidTest --stacktrace`。JDK21／NDK28.2.13676358／CMake3.22.1，序列執行。

## 唯一 CI runtime：停止在 H1

[39152444358](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39152444358)，attempt1、exact b982062；整體 **FAILURE**。Build／source guard／四 APK hash step 成功，不抵銷 runtime step exit3。

| Gate | 結果 | 原始觀測 |
|---|---|---|
| H0 | HEALTHY，限單次run的有界initial/final觀測 | initial／final 明確 unfrozen／Launcher focus，同 serial/user/boot；31 captures，非 atomic snapshot |
| H1 | INCONCLUSIVE：`EDITOR_CAPTURE_UNVERIFIED` | Host target install 一次、editor launch 一次；`uiautomator dump /dev/tty` 3004ms host timeout，只33 bytes提示、沒有 XML |
| H1 不利觀測 | 保留 | before WMS `mDisplayFrozen=true`，after／final false；先後不證因果。截圖的 ICtrue 不替代完整 XML oracle |
| H2／產品 install／IME select | NOT_EXECUTED | `set -e` 在 H1 exit3 停止；沒有產品安裝／選取 |
| App instrumentation 5 | NOT_EXECUTED | 不寫 0 FAIL 當 PASS |
| Host instrumentation 33／32-case matrix | NOT_EXECUTED | 不拿歷史24PASS／9SKIP補本輪 |
| B01 geometry | NOT_EXECUTED | 同 gate 停止；未修改41鍵／原 assertion／timeout |
| 實機30-case／更新保資料／QR相機／TalkBack | NOT_TESTED | ADB inventory 沒有連線手機；沒有裝機、選 IME 或刪資料 |

H0 host `emulator -version` helper exit127（libpulse.so.0 缺失）；KVM check exit0。這是非 gating inventory 缺口，不推為 H1 根因。H0 raw保留 Bluetooth E／Throwable；H1沒有新 last-ANR／完整 logcat history，不能說全程無ANR。未重跑、未按 Wait／Close、未延長 timeout。**H1 capture 深層根因 UNRESOLVED**。

## CI 與本機產物分離

本機產品 APK SHA `c171a3455acb1cb4a3b16ca0eb65e483620c35a224a308f398ad65c1867f0416`，certificate `46488e99…99da94`；CI 產品 SHA `bb179c64a774ddab4728214e38926770e172c61ab75a8f0de03284be65d8d092`，certificate `02a1b82be234d9e9b6fb71e55f96d582b3534768fa845f7499740f16b8959b69`。

CI／本機四 APK 都不同，四個 libchewing JNI `.so` 完整 bytes 也不同；不能全歸簽章差異。267條 CI source hashes 對 Git blobs 全符，**不代表跨環境整 APK byte-identical**。本輪產品兩邊都沒有 Android runtime evidence。

獨立審查與 release gate 見 [review](R3_INDEPENDENT_REVIEW.md)、[readiness](R3_RELEASE_READINESS.md)。
