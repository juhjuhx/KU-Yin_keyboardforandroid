# S1 原生分頁與選取契約

來源：S0 `ea5f1853b3d4aba9f6c0deb1fbcd0e092b1cdac8`；libchewing source `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`，prebuilt `3587ba3355711f0aca50136e787719f6562676b8`。沒有複製外部程式碼、改 native pin／ABI／字典。

## 原生實際行為

同 pin 的 `chewing_cand_TotalChoice/ChoicePerPage/CurrentPage/TotalPage` 描述原生 selection 狀態。`Enumerate/hasNext/String` 從目前頁起枚舉後續 choices；選取位置須包含原生頁 offset。Left/Right 等事件會操作 decoder，不是唯讀 getter。

P0-R 已有安全邊界：以 `:memory:`、停用學習的 disposable context 重播，補齊尾音並取得 native identities；live context 保留未完成音節。adapter 將 default plan 加 native plans，每 10 plans 分頁，於獨立 scratch context 投影文字、過濾與去重。原生選取頁固定於 0；adapter 的下一頁不等於向 live context 發送原生方向鍵。

S1 沿用此流程，只補 `EngineUpdate.candidatePage` 純 metadata 及 UI/session 接線。`totalPages` 是 plan pages，不是 unique visible candidate count；頁內去重可能使可見項目少於 10，空投影頁仍可翻頁。沒有新 `cand_open`／page mutation／native ownership／排名／個人字典預覽策略。

## 狀態與身份

```text
EngineUpdate + EngineCandidatePage
    → ChewingEngineSession（唯一中文 owner）
    → CandidatePageSnapshot
       compositionGeneration / snapshotGeneration / provider / page / candidates
    → Compose strip / grid（唯讀）
    → SelectCandidate(index, CandidateRef)
    → session 校驗同 snapshot 的 provider、page、opaque identity
    → 原 adapter native identity + 完整 buffer 校驗
    → 原 orderedCommits → service → InputConnection ACK
```

- Native identity 是 default plan 或原生文字＋occurrence，限定於當前 snapshot；不是以可見文字 `indexOf` 選字。
- snapshot revision 隨投影、翻頁與 close 改變；composition generation 隨組字 action 改變，翻頁不變。回到同頁也不復活舊 tap。
- stale generation／page／provider／identity／page request 在 native select 前拒絕。
- fallback 沒有真正分頁，pageIndex/totalPages 保持 null；ordinal identity 僅在同 snapshot 有效。
- expand/collapse 不 query／commit；close 使舊 callback 失效。UI 不呼叫 JNI，不新增 commit path。
- editor restart/finish/reset 清 expanded/page；hide request 先關 input gate。symbol Enter/delete 也在 service boundary 關閉後拒絕，避免 hide callback 與 finish callback 間的舊輸入。

## 原生證據與限制

`NativeCandidatePagingTest` 使用本輪重建的實際 pinned host JNI：4 案驗證不同頁、邊界、第二頁 identity 選取、live action history 與下一音節不變。`NativeTypingEditorIntegrationTest` 的新案例走正式 service/Compose callback，向真 Robolectric EditText 提交第二頁候選一次，再繼續輸入「我」。這是 macOS JNI／Android framework 模擬證據，沒有宣稱 physical-device PASS。
