# KU-Yin 續作入口

更新：2026-10-12。先讀本檔與 `../release/AIE_ALPHA_PROGRESS.md`，再按未完成任務讀相關來源；不要以聊天記憶推定 SHA 或測試結果。

## 當前結果

GitHub PR #8 已正常merge（912b5cc），`v0.2.0-aie.1` 已公開experimental prerelease，tag來源2ce6810、APK產品來源39acf96。詳細hash／CI限制見 `../release/AIE_ALPHA_HANDOFF.md`。本輪未改產品；下一個最小任務是fixture adapter與手機復驗，不是AIE-S2。

## 已核對基線

- S1 產品來源：`39acf96c6cfc058106315f3553e1c8b88438f4f7`；文件／已推送分支：`65ec00bf1bd2b90c9821c3ca11e0951c0b9b8e22`。
- 產品分支：`feat/aie-s1-candidate-browser`；GitHub PR #8。發布整合工區：`.worktrees/aie-alpha-release`，分支 `release/aie-alpha.1`。
- 已驗證 APK：`0.2.0-aie.1 / 20003`，SHA256 `acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`；與 P0 同 Debug certificate。
- S0 停用不完整 learning reset，零資料刪除；S1 strip/grid/snapshot/paging 已實作。原生 paging 只使用 scratch preview，沒有 live-context refresh。
- S1 本地證據：App 265、Host 37、Python 104；contracts 10/10。來源及完整命令見 `../review/aie-s1-candidate-browser/06-handoff.md`。不要把歷史結果寫成本輪新執行。

## 狀態與边界

使用者已授權 push、正常合併及發布；只準備實驗性 Alpha prerelease，不宣稱 production ready。P0 基本中文由使用者回報可用；S1 實機尚未驗收。

保留既有 AGENTS、全部舊工區、個人字典、凍結 APK/ZIP、美術與開源捐贈。禁止 force push、清資料、重新簽不同 key 替代現有 APK。沒有 M4／AIE-S2／新 decoder／touch／symbol 工作授權。

目前沒有可呼叫的 `/init`、`/compact` 指令；採用本入口與短 TODO 保存事實，平台自動壓縮上下文，不修改全域 Codex 設定。

## 元件責任

```text
InputMethodService（lifecycle/editor effects/ACK）
  └─ ComposeDecoderSession（唯一組字 owner、generation）
      ├─ ChewingEngineSession → scratch candidate preview / live commit → JNI
      ├─ EnglishWordSession → offline 10k lexicon
      └─ CandidatePageSnapshot → Compose strip / expanded grid
```

UI 不直接呼叫 JNI；候選取回／呈現不新增 ranking 或 learning owner。Release scripts 與 acceptance-host 是基礎設施，不是產品 decoder。
