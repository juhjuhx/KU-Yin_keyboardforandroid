# AIE V1 Data Flow

```text
visible Dachen glyph → canonical ASCII → session command → live decoder
                                                 │          │
                                                 │          └─ state/commit
                                                 └─ active action history
                                                            ↓
                                               scratch :memory: / no learning
                                                            ↓
                                              raw candidates + default conversion
                                                            ↓
                                               current page / full-buffer projection
                                                            ↓
                                             CandidatePageSnapshot [composition + snapshot generation / provider / identity]
                                                            ↓
                                              strip / expanded-page [S1 implemented]
                                                            ↓ explicit current-snapshot tap
                                              live identity resolve + full-buffer check
                                                            ↓
                                               commitUpdate + native ACK (chewing_ack)
                                                            ↓
                                                orderedCommits → InputConnection
                                                            ↓
                                      false → retain/block; true → record own editor offsets
```

注意兩種ACK：`chewing_ack` 清native已讀commit；`InputConnection.commitText` Boolean是Android呼叫結果。不是同一確認。

future ranker位於候選projection與UI之間，必須保留selection identity；future supplemental learning接service effect事件，policy先判、唯一ID去重、false不記。native既有auto_learn較早，不宣稱已延後。

symbol UTF-8／UTF-16完整String → direct orderededitor effect；不拆ZWJ/VS/RI sequences，也不繞composing flush護欄。
