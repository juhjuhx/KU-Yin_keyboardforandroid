# S1 有限效能基線

環境：macOS 15.8.1 arm64，JDK21，pinned host JNI；不是 Android 裝置或 M4 measurement。無延遲目標、沒有宣稱體感改善。

同 JNI/dictionary，以 P0 舊 adapter 與本輪新 adapter 各自獨立 JVM；`:memory:`，每次 reset 後輸入 Dachen j+i，量測 `nextPageUpdate`。5 次 warmup、20 samples，原始命令／samples／platform：`build/aie-s1-evidence/page-latency.json`。

| adapter | median | p95 |
|---|---:|---:|
| P0-R | 21.829 ms | 27.715 ms |
| S1 | 22.375 ms | 26.113 ms |

單批 host 樣本的差異不足以推論效能改善或回歸。這包含 scratch context/replay/projection，是候選整頁取得時間，不是純 snapshot creation；grid rendering、expand/collapse、selection→實際 Android editor 的 latency 尚未量測。現版依然同步取得候選；若真裝置感到卡頓，另立 retrieval/profile 工作，不在本片偷偷增加 asynchronous owner 或排名。
