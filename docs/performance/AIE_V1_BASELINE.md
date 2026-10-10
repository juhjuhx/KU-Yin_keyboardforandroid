# AIE V1 效能／品質基線

狀態：MEASUREMENT_PLAN_READY / NEW_METRICS_NOT_MEASURED。沒有本輪latency/memory/mistap改善數字，尚未設定最終threshold。不開始未批准M4。

## 目前可確定的成本路徑

每UIcallback同步session.dispatch→adapter.snapshot→buildCandidates；一個pool preview，加每個非null plan一個derived projectioncontext。P=當頁plans、H=activehistory、C=枚舉池；成本近似受P×(H+C)與context初始化支配，實際Rust/CAPI/IO成本需量測。不能把程式路徑當掉幀benchmark。

## Synthetic corpus與記錄

- 固定source/nativepin/data hashes、裝置API/ABI、cold/warm分類、字級與width。
- ㄨ、ㄨㄛ、ㄨㄛˇ、我現在有調/無調；長句45字；100次inverse backspace；多頁同音pool；English hel；symbol ZWJ/VS/flags。golden期望字/identity分開，禁止record真訊息。
- timing層：retrieval／rank／touchresolve／UIframe／editorcommit。record原始synthetic durations、context/replay/allocation counts、p50/p95/max；Android UI用frame evidence，host不冒充手機。
- quality：pool recall@K與rank MRR、candidateposition、browsingsteps、correction precision/recall；區分invalidcandidate/錯commit/雙effect為硬correctnessfailure，不被平均值抵消。
- 同來源baseline與experiment pairedreplay；先correctness通過，才比較品質與延遲。閾值待真baseline和使用者review。
- 真用戶輸入replay需另consent，touch只合規boundedaggregate；release不loggingtypedcontent。

首片只追UI可達與正確選字；nativecache/ranker/adaptive-touch優化不得混入S1。體驗慢的根因可能含native同步成本與viewport，不宣稱唯一原因已定位。
