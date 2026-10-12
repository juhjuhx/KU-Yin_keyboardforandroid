# Editor effects 與確認

狀態：NORMATIVE／IMPLEMENTED（文內 PROPOSED/OPEN 例外）。目的：說明此邊界的實際責任、資料與故障處理。來源基線 `a6a4c654845338521700121ac06586887d1ec085`；R1工具／test-adapter不改產品。

## 實際 source files / evidence

- [app/src/main/java/com/example/ime/session/ComposeSession.kt](../../app/src/main/java/com/example/ime/session/ComposeSession.kt)
- [app/src/main/java/com/example/ime/ui/KuYinKeyboardUi.kt](../../app/src/main/java/com/example/ime/ui/KuYinKeyboardUi.kt)
- [app/src/main/java/com/example/ime/KuYinInputMethodService.kt](../../app/src/main/java/com/example/ime/KuYinInputMethodService.kt)
- [app/src/main/java/com/example/ime/service/EditorEffectTracker.kt](../../app/src/main/java/com/example/ime/service/EditorEffectTracker.kt)
- [app/src/main/java/com/example/androidkeyboard/input/EditorEnterPolicy.kt](../../app/src/main/java/com/example/androidkeyboard/input/EditorEnterPolicy.kt)

## Contract / input-output

orderedCommits先commitText再additionalCommits，每個callback依序呼叫一次。service唯一操作currentInputConnection.commitText/setComposingText/finishComposingText/editorAction。這是effect產生／呼叫規約，**不是end-to-end exactly-once delivery**；IPC Boolean並不證實editor文字，必須外部EditText觀察。

EditorEnterPolicy.from snapshots EditorInfo primitives：NONE/UNSPECIFIED→換行；GO前往、SEARCH搜尋、SEND傳送、NEXT下一步、DONE完成、PREVIOUS上一步；custom label→actionId；NO_ENTER_ACTION優先換行。只有未被composition consumed時consult policy，performEditorAction(false)不加newline fallback，避免雙效果。

## ACK 分層與恢復

native adapter snapshot讀committedText後chewing_ack，防下一snapshot重讀；native replay亦ACK歷史commit。**它發生在Android editor ACK之前**。commitUpdate可能已learn，兩個store/editor沒有原子交易。

service的false/null write保存未確認字串及ordered tail、禁後續輸入，不自動retry（editor可能已寫卻回false）。敏感欄位遮蔽顯示；記憶體不log/寫檔；跨editor不重送。使用者明確discard才重置／解除guard，destroy清除。

EditorEffectTracker只保存connection/editor generation与最多32個selection/span tuple，普通欄位bounded extracted text只取offset；敏感/restricted不extract。自己的notification消費，外部span/cursor改變才reset；不保存實際文字作delivery proof。

| Failure | 行為 | 證據邊界 |
|---|---|---|
| null connection / commit false | retain tail、block | 不判定editor零寫入 |
| composing false | retain未確認preedit | 不blind retry |
| native commit buffer | snapshot讀取後native ACK | 非editor transaction |
| stale candidate | 拒絕ref，不送舊字 | 快照內guard |
| perform action false | 不追加換行 | host Binder full scope有限 |

Tests：EditorCommitRecoveryTest（含實際EditText已写却false）、EditorCommitRecoveryTest的tracker/selection案例、EditorEnterPolicyTest、NativeTypingEditorIntegrationTest。外部acceptance-host觀察action/commit/newline/text deltas；OEM/physical仍OPEN。
