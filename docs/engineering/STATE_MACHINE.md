# AIE V1 State Machine

```text
IDLE --literal Zhuyin--> COMPOSING [live owner]
COMPOSING --key/tone/backspace--> COMPOSING [new snapshot; reset page]
COMPOSING --expand/collapse--> COMPOSING [UI only; no commit] (PROPOSED)
COMPOSING --page--> COMPOSING [same phonetics; new candidate snapshot]
COMPOSING --valid candidate/Space/Enter--> COMMIT_REQUESTED
COMPOSING --unavailable selection--> COMPOSING_WITH_ERROR [preserve input]
COMMIT_REQUESTED --editor ACK true--> IDLE/REMAINING_COMPOSITION
COMMIT_REQUESTED --false/null--> WRITE_UNCONFIRMED [block; never auto resend]
WRITE_UNCONFIRMED --explicit discard--> IDLE [no resend]
ANY --external editor focus/selection/span--> RESET_BOUNDARY [generation invalidation]
ANY --sensitive/nontext editor--> RESTRICTED_LITERAL [no learning/composition]
RESTRICTED_LITERAL --ordinary text--> PREFERRED_MODE_RESTORED
```

規則：active Enter只完成組字；第二次empty Enter才editor action。每press一個logicaleffect；scroll/expand不送出；stale candidate不派送。觸控adaptive尚未接線，raw resolvedevent未經批准不改。
