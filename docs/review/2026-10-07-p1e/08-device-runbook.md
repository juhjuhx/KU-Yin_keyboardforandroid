# P1-E 裝置執行與還原手冊

本輪沒有 Android 裝置，`DEVICE_SEQUENCE=NOT_EXECUTED`、所有裝置 case `NOT_TESTED`。本手冊與 runner 是可供接續執行的入口，不是裝置驗收結果。v1 驗收包不變；v2 位於 `build/p1-alpha-acceptance-v2-fixture/`。產品／既有測試 APK 必須維持 frozen bytes，host／host test 是獨立 helper APK。

## 前提、權限與 ownership

由裝置擁有者開啟 USB debugging 並授權這部 Mac，再明示唯一 serial 與 PHYSICAL／EMULATOR。本輪使用者授權建立 harness；**本輪沒有執行 install／enable／set／instrument**。日後依本手冊執行的人負責確認裝置 ownership、可以安裝四 APK、暫時 enable/select KU-Yin，以及依 captured state 還原。instrumentation tests 不自行修改系統 IME。測試只用固定 synthetic 文字，不貼入真實訊息／密碼。

runner 是 Python stdlib CLI：[p1_acceptance_device.py](../../../scripts/p1_acceptance_device.py)。每個 ADB 呼叫固定 argv `['adb', '-s', serial, ...]`，帶 timeout，不採第一台裝置、不用無 serial 的 `adb shell`。提供 prepare／install／select／run／restore 五個明確子命令。沒有 uninstall、pm clear、user dictionary 清除、global setting hooks 或 logcat dump。`install -r -t` 保留 app 資料；signature mismatch 立即 STOP，不能為了通過而自動卸載。

## 四 APK 與資料檔

| 檔案 | application_id | 檢查 |
| --- | --- | --- |
| `app-debug.apk` | `io.github.juhjuhx.kuyin` | 29,158,832 bytes；SHA256 `b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9` |
| `app-debug-androidTest.apk` | `io.github.juhjuhx.kuyin.test` | 965,496 bytes；SHA256 `3938b3e3c1fb711ed6141d491a3841839e098dfb2c2ae81924b94f0d6106b8bd` |
| `acceptance-host-debug.apk` | `io.github.juhjuhx.kuyin.acceptancehost` | 依本次 `artifact-manifest.json` 的 bytes／sha256 |
| `acceptance-host-debug-androidTest.apk` | `io.github.juhjuhx.kuyin.acceptancehost.test` | 依本次 `artifact-manifest.json` 的 bytes／sha256 |

`artifact-manifest.json` 的 `artifacts` list 每個 APK 記 `file / bytes / sha256 / application_id`。runner 先驗四檔大小、SHA256、宣告 identity，另硬性核對兩個 frozen APK；prepare 後再核對 manifest digest／四檔／path 沒變。fixture signer／APK manifest 的靜態核對由 v2 package 證據提供；runner 不以 JSON 的 application_id 宣稱已重新解析 binary manifest 或簽章。

`captured-state.json` 與所有裝置狀態／runlogs 必須放在 **repo 外** 的使用者指定 evidence-dir；runner 強制拒絕 repo 內目錄。state 以 exclusive create 寫入，永不覆寫。不同裝置／artifact／新一輪原始狀態使用新的目錄，不提交 state 到 git，也不把它包入共用 APK ZIP。

## 指定 serial、artifact 與外部證據路徑

從 repo 根目錄執行。將範例替換為實際且已授權的 serial；目前沒有 serial，不能照抄範例當作已執行。

```bash
export KUYIN_SERIAL='請替換為實際唯一serial'
export KUYIN_PACKAGE_DIR="$PWD/build/p1-alpha-acceptance-v2-fixture"
export KUYIN_EVIDENCE_DIR='/Users/huang/Desktop/KU-Yin裝置驗收/2026-10-07-實際裝置序號-01'
python3 scripts/p1_acceptance_device.py --help
adb -s "$KUYIN_SERIAL" get-state
```

`get-state` 必須是 `device`；unauthorized／offline／not found 都停止。runner 的 prepare 會再固定讀取 model、manufacturer、API、ABI list／primary ABI、build fingerprint、qemu flag；後續每段重查相同 identity。API 必須 ≥24，device-kind 必須符合 qemu observation。EMULATOR 的結果不能代填 physical PASS。

若使用解壓後的可攜 v2 ZIP，先進入含四 APK、`artifact-manifest.json` 與 `scripts/` 的解壓根目錄。runner 不依賴原 repo；必須每段明示 `--package-dir "$PWD"`，避免採 repo-layout 的預設路徑。例如：

```bash
export KUYIN_SERIAL='請替換為實際唯一serial'
export KUYIN_EVIDENCE_DIR='/Users/huang/Desktop/KU-Yin裝置驗收/實際裝置-新一輪'
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$PWD" --evidence-dir "$KUYIN_EVIDENCE_DIR" prepare --device-kind PHYSICAL
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$PWD" --evidence-dir "$KUYIN_EVIDENCE_DIR" install
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$PWD" --evidence-dir "$KUYIN_EVIDENCE_DIR" select
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$PWD" --evidence-dir "$KUYIN_EVIDENCE_DIR" run
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$PWD" --evidence-dir "$KUYIN_EVIDENCE_DIR" restore
```

## 分段執行

先 prepare；它驗 APK，再只讀保存原 default IME **原字串**與 `enabled_input_methods` **原字串（含 subtype）**，不選取／啟用 KU-Yin。原 default 是空／null、已不在 enabled list，或格式不明，立即 STOP，不猜測還原。

```bash
python3 scripts/p1_acceptance_device.py \
  --serial "$KUYIN_SERIAL" --package-dir "$KUYIN_PACKAGE_DIR" \
  --evidence-dir "$KUYIN_EVIDENCE_DIR" prepare --device-kind PHYSICAL
```

接著安裝四 APK。acceptance-host 整個 APK 是 testOnly，Activity exported 僅供 helper；使用 `-t`。產品安裝升級採 `-r`，不清除資料。任一 install 失敗就 STOP；不要 uninstall／pm clear 換取成功。已成功安裝的前幾個 APK 可能仍留在裝置，這種 partial install 要記錄，不能宣稱四檔全部安裝完成。

```bash
python3 scripts/p1_acceptance_device.py \
  --serial "$KUYIN_SERIAL" --package-dir "$KUYIN_PACKAGE_DIR" \
  --evidence-dir "$KUYIN_EVIDENCE_DIR" install
```

顯式 select 會先核對原始其他 IME／subtype 未被外部操作改變，核對 canonical IME ID 恰好為 `io.github.juhjuhx.kuyin/com.example.ime.KuYinInputMethodService`，再 enable／set，並 bounded polling 確認。select 成功會暫時保留 KU-Yin 為選取狀態；若 select 本身失敗，嘗試安全 restore。

```bash
python3 scripts/p1_acceptance_device.py \
  --serial "$KUYIN_SERIAL" --package-dir "$KUYIN_PACKAGE_DIR" \
  --evidence-dir "$KUYIN_EVIDENCE_DIR" select
python3 scripts/p1_acceptance_device.py \
  --serial "$KUYIN_SERIAL" --package-dir "$KUYIN_PACKAGE_DIR" \
  --evidence-dir "$KUYIN_EVIDENCE_DIR" run
```

run 前提是 KU-Yin 已 enabled／selected；它不幫 tests 偷改前提。執行順序為 frozen app instrumentation 既有 4 類 5 tests，再 acceptance-host suite；任何第一層 protocol／test failure 停止第二層。run 正常、test failure、timeout 或 Ctrl-C 都走 finally restore。一般 ADB timeout 30 秒、install 120 秒、每 suite 600 秒，可用 global `--adb-timeout / --install-timeout / --instrumentation-timeout` 明示更改。

實際 instrumentation argv 對應下列命令；**通常使用 runner，這裡只供核對 protocol**，直接執行也必須以 restore 收尾：

```bash
adb -s "$KUYIN_SERIAL" shell am instrument -w -r \
  -e class com.example.androidkeyboard.runtime.ImeRuntimeSmokeTest,com.example.androidkeyboard.runtime.NativeCase4Test,com.example.androidkeyboard.runtime.ContinuousSentenceTest,com.example.ExampleInstrumentedTest \
  io.github.juhjuhx.kuyin.test/androidx.test.runner.AndroidJUnitRunner
adb -s "$KUYIN_SERIAL" shell am instrument -w -r \
  io.github.juhjuhx.kuyin.acceptancehost.test/androidx.test.runner.AndroidJUnitRunner
```

## 結果解析與證據

每次子命令建立新的 timestamp＋random operation 目錄，不覆寫先前 logs。`commands.jsonl` 保存 exact argv、timeout、時間、exit、stdout/stderr；run 另保存兩 suite 文本、30 份 `<CASE_ID>.json` 與 `run-summary.json`。檔案只含裝置／IME metadata 與固定 synthetic 測試 observation；不用 logcat 收集其他 app 內容。

ADB `am instrument` exit=0 本身不代表 PASS。runner 要看到 final `INSTRUMENTATION_CODE=-1`（正常成功的 Android 慣例）、帶 class/test 的 terminal status、numtests、OK banner，且沒有 FAILURES!!!／failure status／shortMsg／process crash／protocol error。**final code -1 與單測 status code -1 不同**，後者是 error。既有層還要求恰好完成 5 tests／reported5／OK(5 tests)，不能 skip／assumption。host skip／assumption 記 INCONCLUSIVE；自訂 stream status 不算 JUnit test count。

host 發出 `P1_ACCEPTANCE_CASE={JSON}`，欄位 `case_id / result / evidence / scope`；result 只有 PASS／FAIL／INCONCLUSIVE／NOT_TESTED。runner 保存每個原始 emit，另連結當前 JUnit test identity／terminal status；skip／assumption 的 PASS emit 也不能成為 observable PASS。缺 emit 的 case 保持 NOT_TESTED。`observable_result` 是當次 scope 的有效結果；`full_acceptance_result` 額外列出尚未滿足的完整契約缺口，兩者不混用。C 類 UI mode／composition observation 可 PASS，但 `native_learning_observer=OPEN`，不能據此宣稱 native learning physical 已證。D17 host 收到 action 並回 false 可觀察，IME 收到同步 false 在 oneway Binder 無法證明；full scope INCONCLUSIVE。

run exit code：`0` 僅在完整四值結果可判 PASS 且 restore 通過；`2` 是 suite/protocol/case/restore FAIL；`3` 是序列完成但 full acceptance INCONCLUSIVE／仍有 NOT_TESTED。其他子命令成功為0、STOP為1。**目前 A03 manual、A04 fault fixture 與 native learning 仍 OPEN，正常裝置執行後也可能合理回3**；不能改 parser 把它變成全30 PASS。run-summary 的 restore result 與 sequence result 獨立看。

B01 的第二寬度／方向、B02 的 native context／reset 身分與 B03 的整句語義／context 範圍亦保持 OPEN；執行器明示這些 scope limits，局部 UI PASS 不自動成為 full acceptance PASS。後續人工證據另行審查。

## 還原與異常

```bash
python3 scripts/p1_acceptance_device.py \
  --serial "$KUYIN_SERIAL" --evidence-dir "$KUYIN_EVIDENCE_DIR" restore
```

restore 不需要 APK 還在原 path。它只 `ime set` captured 原 default，且僅當 KU-Yin 原未 enabled 而現在 enabled 時 disable KU-Yin；KU-Yin 原本 enabled 時保留其原 subtype 記錄。最後核對 default／enabled **含 subtype 原字串**。已還原時可重複執行，但不覆寫 captured state。

default 被第三者改成原值／KU-Yin 以外的 IME、其他 enabled/subtypes 有變、原 KU-Yin subtype 有變、裝置 identity 不同、原 IME 不見、或最後 raw 字串差異都 STOP，要求人工核對，不能覆寫整串 `settings put secure enabled_input_methods`。斷線、host process crash／timeout、Ctrl-C、terminal 關閉等可能阻止 restore 完成；重新接上同 serial 後執行上面的 restore，並保存其 result。不可把「finally 有呼叫」當作已成功還原。

## 人工 actual rotation 與獨立觀察

自動 run 已 restore。要進行人工 A03／B layout observation，重新明示 select，開啟 host 的固定 editor case，完成後 restore。例：

```bash
python3 scripts/p1_acceptance_device.py \
  --serial "$KUYIN_SERIAL" --package-dir "$KUYIN_PACKAGE_DIR" \
  --evidence-dir "$KUYIN_EVIDENCE_DIR" select
adb -s "$KUYIN_SERIAL" shell am start -W \
  -n io.github.juhjuhx.kuyin.acceptancehost/.AcceptanceEditorActivity \
  --es caseId A03 --ei inputType 1 --ei imeOptions 4 \
  --es initialText KUYinP1 --ez performEditorActionReturnValue true
```

真正將實機從 portrait 轉到 landscape，再轉回；記 host_orientation／display_rotation、旋轉前是否有真 preedit、完成後再輸入、action/newline/commit deltas、crash。recreate 不能代填 actual rotation；composition 保留／清除只記 observation，沒有 frozen preservation contract。preedit 必須辨認「清除」chip 與暫存 Text，不能以鍵面 ㄅ／ㄕ 本來存在當作 composition。selector 不唯一／preedit 無法辨識／TYPE_NULL 前提不成立記 INCONCLUSIVE，不使用硬編座標／adb KEYCODE_ENTER 補 PASS。

每人工 case 使用 [09-device-matrix.md](09-device-matrix.md) 的模板另存 repo 外 evidence-dir。人工 evidence 不自動改本文件的初始 NOT_TESTED，也不讓自動 runner 猜測它已完成。

本 runner 的驗證僅為 pure／fake-subprocess 單元測試；真正安裝、binder／UI selector、原 IME 還原與 OEM 行為仍需裝置執行。[官方 ADB 文件](https://developer.android.com/tools/adb) 支持明示 `-s`、install 與 shell instrumentation 使用方式；[AndroidJUnitRunner](https://developer.android.com/reference/androidx/test/runner/AndroidJUnitRunner) 是兩層 instrumentation runner 的官方入口。
