# R3.7 H0 Readiness 預備規格

狀態：PROPOSED／OFFLINE_SPECIFICATION_ONLY。未接入 ADB collector、H0 oracle 或 CI；不授權 Runtime。

## 證據與缺口
Android13 的 sys.boot_completed、overlay enable-exclusive exit0、兩份 [x] inventory 都不提供後續 framework configuration 傳播完成的公開 ACK。原始 R3.6 證據缺此邊界，因此仍 INCONCLUSIVE。F 是明確標示 SYNTHETIC 的有限初始化協定，不是 Android 已存在的介面。

## 不變條件
- 綁定同一 serial、user、boot_id；原 H0 bytes／SHA／第一份 FAIL 或 INCONCLUSIVE 不得被後續恢復洗掉。
- initial/final/post 是各自有 argv、exit、timeout、raw SHA、host monotonic 接收上下界的循序觀察，不是 atomic snapshot，也不等於 guest 事件時間。
- 所有 WMS 必須完整、明確 unfrozen；AM 有有效 task tree；無可見 blocking alert、無未處置 ANR／critical history。
- overlay 全清單與 configuration 必須一致；欠缺 completion observer、unknown、missing、錯誤命令、時間域矛盾一律不放行。
- 未來若提出真實 observer，須先釘住其來源／映像，證明有限 initializer generation、預期操作集合、逐項成功、terminal close、下游 propagation ACK 與同 boot 身分；不能只取樣等待穩定。
- 合成模型只接受明確提供的 fixture_only protocol，永不接受 ANDROID_RUNTIME。任何 HEALTHY 僅代表合成契約滿足，runtime_authorization=false。

## A–F Oracle
A boot complete 但 profile 未完成：INCONCLUSIVE。B overlay 過渡 frozen／首份失敗：INCONCLUSIVE且保存首份。C 原healthy但post frozen：INCONCLUSIVE。D post缺失：INCONCLUSIVE。E post可見ANR：UNHEALTHY。F 有限合成init完成、傳播ACK且全部循序觀察一致：僅模型HEALTHY。
另鎖住首份UNHEALTHY不得消失、ACK身分／generation／時間／hash／operation count／close後突變／配置改變等負例。

## 實作與回退
scripts/h0_readiness_contract.py + scripts/tests/test_h0_readiness_contract.py；既有 emulator_health.py 不變。本輪不 dispatch CI；未來單一 health-only 實驗需另行明確批准並先解決真實 completion 可觀測缺口。

## R3.8 Main-based Port
原始commit ccb281138e4939bbe58c5a35735b18978cb705f0保留於獨立分支。main沒有舊診斷collector；只抽出原AA的三個純WMS predicates與SERVICE_ERROR到h0_window_observation.py，函式原文不變。測試A直接鎖住合成boot=1前提，移除對整套legacy H0 fixture的載入；原本legacy H0可healthy的對照保存在原commit測試與R3.7 RED/GREEN。其他15項及原收據status/hash/identity反例不變。沒有ADB、JNI、runtime接線或新健康放行介面。
