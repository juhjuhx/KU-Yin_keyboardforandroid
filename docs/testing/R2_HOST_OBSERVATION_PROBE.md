# R2 有限 Host 讀取階段探測

TEST_ONLY；不是產品修正或完整 acceptance。main仍63c9e4ab，PR10仍HOLD。

## 調查依據

受控run38459179007（synthetic merge698d107c）原始XML：A／B／C App均5PASS；A／B Host15PASS9FAIL8SKIP，C為15PASS17SKIP。`terminal.txt` A/B original_exit=1，C=0；evidence_exit均0。Workflow總結A=failure B=failure C=success，因此整體FAIL正確，不以continue-on-error步驟的success覆蓋failure。

B/C App source keys及SHA相同；三组product source均相同。APK／native ELF雜湊不同，不能稱binary-identical控制。現有metadata不支持「IMM show回false必然沒IC／永久不會顯示」；失敗ea5尚未重現，root仍UNKNOWN。

目前9個原FAIL在C均因fresh UI capture deadline成為BLOCKED_SELECTOR；不是修復。既有總capture/attempt計數跨整個case且包含cleanup，不能當作單一5秒窗的retry數。UiAutomator2.3.0本機實際AAR bytecode證實hashCode也refresh AccessibilityNodeInfo，故診斷必須包含REGISTER，而不只顯式getter。尚未證明它就是失敗位置。

## 最小觀測切片

- UiReadTrace只在sharedTest/androidTest，記固定phase／BODY或CLEANUP／capture和attempt序號／node序號／elapsed／stale；不記節點文字、label或exception內容。
- recent failure ring最多8；每個owner另外保存最後failure（enum最多2），cleanup不能擠掉BODY證據。原Throwable重新拋出，不增加retry、不改deadline、不重複gesture。
- getter／query／register／idle／restore各自標記；未完成capture仍不作完整UI證據。
- 既有close雙failure覆寫缺陷另用runCaseCleanup與原mergeCaseFailure最小修正，保留primary/suppressed；real failure不能因另一inconclusive變SKIP。保持scenario=null先於ActivityScenario.close。

## 執行與限制

`KUYIN_R2_HOST_PROBE=true`是明確opt-in；其他不合法值在ADB前exit2。只執行source已核對的P0-ZH01、C04兩個Host method，仍先完整App5。預設完整Host32不變。固定fresh API33/default/x86_64/pixel7 CI emulator/user0，實機不受這個流程管理。

scope.txt必須為FOCUSED_DIAGNOSTIC_NOT_ACCEPTANCE；完整XML、case envelope、source/APK/selecteduser證據仍保存。兩案probe不能代表其餘30個Host method或30-case physical matrix。Probe的SKIP/INCONCLUSIVE仍不計PASS。

已執行一次的完整A/B/C改為workflow_dispatch，避免每個diagnostic提交重跑三台fresh AVD；原普通Build gate不改。需要新的隔離因果比較時再explicitly dispatch。新probe workflow只跑有限觀測，不替代normal CI。

## RED／GREEN

外部證據 `/Users/huang/Desktop/KU-Yin-R2-Verification-2026-10-13/`：

- 19b UiReadTrace：5測4失敗，其中3 AssertionError有效RED；1NoSuchElement oracle不作RED證明。
- 23 owner retention：6測1AssertionError RED；24 GREEN103測。
- 21 probe mode：2測2AssertionError RED；22 Python122PASS。
- 25 import位置compile錯誤不作RED；25b雙cleanup：4測3 assertion failures有效RED。
- 26最終Host107測GREEN；26b核對最後close-order source instrumentation編譯。
- 27全部contracts10PASS。

Runtime probe與其source checkpoint尚待CI。沒有宣稱root修復、physical PASS、PR10解除HOLD或release ready。
