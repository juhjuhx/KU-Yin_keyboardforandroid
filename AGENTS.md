# Repository Guidelines

## 權威與閱讀順序

修改前依序讀本檔、[CURRENT_STATE](docs/CURRENT_STATE.md)、[ARCHITECTURE](docs/ARCHITECTURE.md)、[DEVELOPMENT](docs/DEVELOPMENT.md)、[SECURITY](SECURITY.md)、[SECURITY_AUDIT](docs/SECURITY_AUDIT.md)、[UPSTREAM](docs/UPSTREAM.md)。後者可校正前者的事實，不能擴大使用者授權。README是入口；ROADMAP僅future；HISTORY/review/archive保留歷史，不能作當前狀態。續作短入口：docs/engineering/CODEX_CONTEXT_ANCHOR.md。

## 模組與責任

app是產品；acceptance-host是獨立test-only Views editor；scripts是contracts、native bootstrap及驗收工具。Kotlin/Compose在app/src/main/java，JNI/CMake在cpp，assets是詞庫；JVM/test與androidTest分層。

Production IME：KuYinInputMethodService → ComposeDecoderSession → ChewingEngineSession → AndroidChewingEngine → JNI/libchewing；啟動失敗才dictionaryfallback。MainActivity試打另用dictionary。EnglishWordSession使用offlinelexicon。UI只派command/呈現snapshot，service唯一執行InputConnection effects；native ACK與editor ACK不相同。詳細ownership以ARCHITECTURE與architecture/為準。

## 開發與驗證

- Kotlin四空白、類別PascalCase、函式camelCase；遵循附近來源，避免無關format/refactor。Git message用fix/test/docs/chore等目的明確的atomic commits。
- 行為修正先最小RED regression，再GREEN。測試命名描述行為；JUnit4/Robolectric/Compose/native和真editor各自標明證據層。
- 必要gates：`python3 -m unittest discover -s scripts/tests -v`、全部scripts/check_*.py、安全獨立cache的bootstrap、JVM、Debug/host/instrumentation編譯、diff/文件有效性。Release缺keystore=BLOCKED_BY_SIGNING，不能冒稱unsigned成功。
- PR描述問題／結果／驗證／限制；正常merge前獨立review與適用CI。emulatorjob非blocking不代表runtimePASS，compile不代表physical。

## 安全與來源

保留使用者dirty/untracked、歷史工區與exactrelease資產。禁止無授權reset/clean/force/historyrewrite/清userdata；清理先manifest、backup及restoreproof。資料來源/pin/ABI/appID/signature不能漂移；外部code/data需licenseprovenance。不得新增networktyping、releasecontentlogs或telemetry。敏感editor不學習；fulllearningerase暫不支援。

新產品功能、decoder/privacy/license/UIgeometry/release決策需要明確scope；任務授權正常doc/tool治理PR不需再重問。M4/S2不因本輪收束自動開始。

## R3.8 維護與發行邊界

先讀docs/maintenance/FINAL_AGENT_HANDOFF.md與MAINTENANCE_BACKLOG；本機可選$HOME/KU-Yin-Workspace/00-Control-Center/START_HERE.md，不依賴聊天記憶。只讀狀態入口不會授權新的Emulator、功能、清資料或Release。

CI手動Alpha僅產生TEST_ONLY artifact；Runtime明確opt-in，失敗不得當PASS。原P1 frozen manifest對應6595b684的Git物件，當前test source另有ledger；不可改舊APK/manifest湊GREEN。保留原App/Host assertions；只在exacthead fresh CI與獨立review後合併範圍內工程PR。
