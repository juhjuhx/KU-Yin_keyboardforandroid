# R2 IME visibility 根因調查

## 來源／邊界

main `63c9e4ab8ad7e277013acb5d7cce99228ec1f827`；PR10 remote `ea5c2b5b3404508c8ab6b0a69642b3a54a8cb565`、local `4fc43ffe29a361ea7916abf0e319bdf42dea8eb6`。R2 從 main 隔離分支，正常 merge 已審查的 local Host source，未將 PR10 合併 main。產品 main Kotlin/JNI／pins／signer 不改。R1 的1185個baseline＋supplemental checksum全一致；18原工區HEAD/status/diff雜湊保存在外部 R2 evidence。

## 已觀察／尚未判因

- 原 Alpha、PR9、PR11 emulator App5PASS；Host32＝15PASS／9FAIL／8SKIP。9個failure包含UiAutomator stale讀取，不是已證實9個產品缺陷。
- 087ff39 App5PASS／Host15PASS17SKIP，不算修好。
- ea5c2b5 run38254806233，actual merge `42fb9afb59b54e8e0e11859f508ba554d939379a`：App4PASS1FAIL，`ImeRuntimeSmokeTest.kt:72`首次window visibility，recreate未執行；Host未跑。
- passing traces有Insets `fromIme=true`，失敗trace只有show `fromIme=false`；現有trace缺focus／connection／service回應，第一個內部失去狀態的邊界仍未知。
- main與PR10的App smoke、debug host、service、main/debug Manifest、runtime script相同。原registered/enabled/selected assertions通過不代表editor已連接或window可見。

**ROOT_CAUSE=UNRESOLVED。** 沒有把一次success當flake證明。假說：editor/window connection race、IME installation/default reset、binding或window/control未完成、observer問題；需同image/user/install順序的trace支持／反證。產品兩個show policy回true，反證簡單「硬體鍵盤policy拒絕」說法。

## 官方語義／診斷影響

[Keyboard visibility](https://developer.android.com/develop/ui/views/touch-and-input/keyboard-input/visibility)說IMM show需window/view connection；[ActivityScenario](https://developer.android.com/reference/androidx/test/core/app/ActivityScenario)只保證lifecycle transition，不保證window focus。[WindowInsetsController](https://developer.android.com/reference/android/view/WindowInsetsController)可defer control，但不代表任何early show必成功。AOSP Android13 IMM的isActive/isAcceptingText會checkFocus，不能加入passive poll，以免修飾正在調查的race。

## 最小調查計畫

> 執行方式：主要agent逐切片實作，獨立read-only reviewer核證；使用者已授權本prompt的診斷／比較／TDD，不另要求重選流程。

- [x] exact source／18 worktrees／frozen資料保存／歷史raw XML覆核。
- [x] TEST_ONLY observer regression RED：debug host final EditorInfo／IC presence、focus/attachment、既有show返回；不記text，不增加show呼叫。
- [x] 保留40×250ms poll與原assert；failure現場在ActivityScenario.use內、close前採證。診斷schema與CI failure evidence自身也做RED/GREEN。
- [x] API33 default x86_64 pixel7、user0、相同runner/install/IME setup，A原main/App test、B原main＋observer、C PR10 local＋同observer；source/hash/provenance逐組保存。一次多變數比較不作因果證據。
- [ ] 真runtime找第一個分歧，支持root後才最小repair RED/GREEN；原App5過才跑Host32。
- [x] fresh Python120／contracts10／App273／Host97／bootstrap／四APK編譯，診斷source獨立審查；emulator證據審查仍待執行。

本機沒有emulator或system image；runtime需CI disposable emulator。原實機與P1安全runner不修改。PR10 HOLD，physical OPEN，release NOT_READY。

## 2026-10-14 受控執行結果

[CI38459179007](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/38459179007) actual merge698d107c／PR12 head7f474c41：A/B/C原始App XML均5PASS0FAIL0SKIP；A/B原Host各15PASS9FAIL8SKIP，C為15PASS0FAIL17SKIP。完整source manifest逐blob核對；產品不改，並非binary-identical APK實驗。三個fresh emulator/user0皆未重現ea5首次visibility失敗，不能推定flaky或已修復。

B：attached/windowfocus/token及真IC已有，但原IMM show回false；其後平台仍完成show且visibility=true。C：初次show request前已visible，不能把該次PASS歸因posted IMM。recreate在三組真執行且可見。參見外部「受控比較生命週期與根因獨立覆核.md」與[新階段探測](R2_HOST_OBSERVATION_PROBE.md)。

Host9個FAIL→SKIP不是repaired；完整capture仍阻擋strict preedit證據。新增有界getter/REGISTER階段trace＋獨立cleanup failure保留，準備兩案有限probe；尚無產品根因或產品修正。PR10保持HOLD。

## 已定位的另一個Host基礎設施缺陷

38529968625將失敗邊界定位QUERY_REGISTER的live UiObject2 hash；old2.3.0在真destroyed editor的existing-map lookup確實StaleObjectException（38561946857）。同一source regression、同API33／user0，僅test dependency2.4後GREEN（38563163760），getter仍stale，沒有使用失效UI作fresh observation。這是已驗證primitive修復，不等於原9個Host cases全部修復，更不等於historical App visibility根因。官方source／版本／license與精確RED/GREEN見[依賴修復](R2_UIAUTOMATOR_DEPENDENCY_REPAIR.md)。有限真軟鍵sequence執行中，通過後才完整Host。

## 2026-10-15 最小修正狀態

已確認的local前置缺陷有debug-only after-focus one-shot修正33b3ac8，8測GREEN與獨立source批准；觀測缺陷另有3895625 shell stdin修正，5個JVM測試GREEN、compile通過。未更動production service、native或EditorPolicy。新實驗38660489452仍待結果；不從unit GREEN推導runtime恢復，也不把local not-served推廣成舊ea5 served失敗的完整因果。
