# R3.1 H1 擷取根因與證據界線

更新：2026-10-20。main `63c9e4ab8ad7e277013acb5d7cce99228ec1f827`；基線 PR20 `de2d9dfaa296a5cdeabde405d686720b615ce9e2`，原 runtime `b98206267b3a65c452db1661bb3bae910ec61f6a`。本輪只改測試基礎設施。

## 第一分歧

原 CI [39152444358](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39152444358) 的 `build/r22-preinstalled/h1/`：

| 界線 | 直接觀測 | 可支持的結論 |
| --- | --- | --- |
| launch | exit 0、2231ms | Activity 啟動命令完成，尚不證明 editor oracle |
| window-before-0 | `mDisplayFrozen=true` | 第一個 readiness 分歧，不能放行 |
| ui-0 | `adb -s emulator-5554 shell uiautomator dump /dev/tty`，3004ms timeout | 第一個擷取失敗，H1 正確 fail closed |
| ui-0 stdout | 33 bytes，只有 `UI hierchary dumped to: /dev/tty` 與換行 | 成功提示不等於 hierarchy XML |
| after / final WMS | unfrozen、host focus | 不能補造先前缺失的 XML 或追認 H1 PASS |

ui-0 stdout SHA256：`c2147cc54c0d6fa195fb43b9e2471e4f714db73de8a0d3771f9be22b921e58ed`。原 raw 未改。凍結是否造成命令 timeout **UNRESOLVED**；不把兩個分歧直接畫成因果。

## 平台機制與最小修正

固定 AOSP Android 13 來源中，[DumpCommand](https://android.googlesource.com/platform/frameworks/base/+/0d3ff311e6e80dee7fe88a2a2cfa272ce231c3c6/cmds/uiautomator/cmds/uiautomator/src/com/android/commands/uiautomator/DumpCommand.java) 交由 dumper 用 `FileWriter` 寫路徑，finally disconnect 後才印提示；[dumper](https://android.googlesource.com/platform/frameworks/base/+/0d3ff311e6e80dee7fe88a2a2cfa272ce231c3c6/cmds/uiautomator/library/core-src/com/android/uiautomator/core/AccessibilityNodeInfoDumper.java) 捕捉 IOException，只寫 error log。[ADB shell](https://android.googlesource.com/platform/packages/modules/adb/+/ef7818f751655cfa9aa36e824f13289b336079e0/client/commandline.cpp) 非互動命令預設 raw，沒有控制終端；`/dev/tty` 並非普通 stdout。

因此原擷取有不成立的傳輸假設。只將該命令改為 `shell -tt uiautomator dump /dev/tty`，為實際 FileWriter 配置控制終端；保留 command exit/timeout、完整 XML、兩側 WMS、editor focus、真 InputConnection、default IME、same user/boot 與 final diagnostics 的原斷言。

3 秒命令上限、10 秒 H1 預算不變。PTY 合併 stderr 且可能轉換換行；解析仍要求嚴格 XML 與原 editor 身分。此機制是固定上游版本的解釋；CI image 的實際二進位尚未逐檔對到該 revision，不能宣稱解釋了全部 3004ms timeout。

## RED / GREEN

真本機 child 以相同 `/dev/tty` 寫入合成 XML；raw 分支為獨立 session＋pipe，PTY 分支以 `pty.fork()` 建控制終端。不是為每個 argv 直接回傳假 XML。

- 同一最終 regression 對原 de2d9df 實作：10.210s，1 failure，`HIERARCHY_UNVERIFIED`，有效 RED。
- 目前來源：健康探針 37 tests PASS；source guard 13 tests PASS。仍不是 Android H1 PASS。
- 原 205-test baseline 有 fake-shell 30 秒 watchdog error；單獨改用正常子行程權限時同 test PASS。保留環境差異，沒有宣稱因果已解。
- 開發途中 helper 的全域 sleep mock、PTY 等待與 fixture SHA collision 已各別處理；失敗／中止記錄保留，不能當 GREEN。補跑 RED 最初缺 import 檔案亦為無效 RED，最終完整副本的 15 號證據取代其裁定。

另一個獨立審查 M1：RC source guard 原先未保護 `scripts/runtime_smoke.sh`。增加固定 b982 baseline 及 committed/dirty 反例，不改 runtime script 本身。health 新來源必須另外 pin 已審查 commit，不能將當前 HEAD 自動視為批准。

## 原始證據與裁定

外部：`/Users/huang/Desktop/KU-Yin-R3.1-H1-Capture-20261019-211633/`，含 `10-original-raw-provenance.json`、`15-original-source-meaningful-red.*`、`12-health-green.log`、`13-source-guard-green.log`、兩位 reviewer 原報告。原 R3 raw 位於 `KU-Yin-R3-RC-Closure-2026-10-18-205053/36-ci-runtime-raw/`。

## 唯一新 Android 實驗

[CI39426302308](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39426302308)，exact `feab29f53583c4bb8dfc4cffe13243a36a3e9d06`，最終 FAILURE。完整 Python 212 tests／89.463s 與 contracts10 PASS；原 b982 四 APK 的 source/hash 全吻合，沒有 rebuild。

H0 initial/final 均 HEALTHY，same serial/user0/boot；只安裝 host。H1 before window 已為 BASE_APPLICATION、HAS_DRAWN、focus host、unfrozen，不能歸因成只有 splash 尚未完成。`ui-0` 真正 argv 有 `-tt`，但 stdout 為59bytes `ERROR: null root node returned by UiTestAutomationBridge.`＋換行，**沒有 XML**，3006ms timeout／exit null。after WMS frozen=true、host window identity 改變且 DRAW_PENDING；final unfrozen/HAS_DRAWN。default IME 仍 LatinIME，user/boot 未變。

這次沒有進入成功 XML 輸出路徑，無法藉此驗證 Android 的修正傳輸已可用；也不能證明 `-tt` 導致 null root／window replacement。兩個不同 run 並非只改一變數的完整因果 A/B。已有 window 隨後恢復不能追認本次 capture PASS，原判定 `INCONCLUSIVE / EDITOR_CAPTURE_UNVERIFIED` 正確。

新 raw：外部 `25-ci-editor-health-raw/`，176檔 SHA manifest 與完整 CI log／run receipt 在26號證據。H2/App5/Host33/32-case/B01 **NOT_EXECUTED**，未重跑。下一最小調查為同一合成 host 的 UIAutomator accessibility root 與 window replacement／configuration lifecycle，先建立受控觀測與假說；不可先假定 early-show、產品 bug、flaky 或以延長 timeout／換 oracle 放行。

最終裁定：傳輸假設缺陷有 SOURCE_SUPPORTED_WITH_LOCAL_REGRESSION；Android H1 實際恢復 **NOT_PROVEN**；null root／timeout 深層根因 **UNRESOLVED**。Release NO_GO。
