# KU-Yin 當前狀態

權威：實際Git/source/當次CI優先；架構見[ARCHITECTURE](ARCHITECTURE.md)，未來工作見[維護清單](maintenance/MAINTENANCE_BACKLOG.md)。先前R1–R3.4逐輪狀態保存在Git歷史，不能當新證據。

## 工程來源與產品

R3.8從main `2a2181e023e0dee0ddff98681e374df10cd5fa8f`建立最小工程整合。R3.7原始readiness `ccb281138e4939bbe58c5a35735b18978cb705f0`、Host `22c2d66900547a9cd8a6ecfc1aa7410fe4faf73e`已保留並推至獨立分支。Host的strict preedit、candidate/editor/exactly-once assertions不變；新JVM observer測試不證Runtime。Readiness是另外審查的OFFLINE_SPECIFICATION_ONLY模型，不能放行裝置。

產品仍為Kotlin／Jetpack Compose／JNI C++／固定libchewing；P0-R基本打字有使用者回報，S1候選列/展開/頁面已實作。未實作AIE-S2排名、TouchResolver、完整learning erase、Whisper runtime或新Rime/Fcitx框架。S0完整reset仍停用。

## Runtime與發布

- H0：CI39957438988、source676f127，INCONCLUSIVE；Pixel7 RRO caller／Freeze root仍UNRESOLVED。
- H1/H2：NOT_EXECUTED。本輪不以收尾為由重跑Emulator。
- Host：歷史main CI39882220869為15 PASS／9 FAIL／8 SKIP；新observer JVM不能改判。
- 實機：NOT_TESTED；新RC／Stable：NO_GO。PR20、PR27各自Draft/HOLD，不整包自動合併。
- 唯一公開版本：[v0.2.0-aie.1 Experimental Alpha](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1)，Debug20003／0.2.0-aie.1，八資產及tag不變。

## 本輪安全處置

手動prerelease workflow改成只建CI測試artifact，contents:read，不能發布/改tag。Runtime只有明確manual opt-in才執行，執行失敗不再以continue-on-error掩蓋。Actions固定commit、wrapper加官方SHA。根LICENSE復原標準LGPL2.1，既有授權選擇不變；舊包異文及新二進位完整source/relink仍須處置。

已知產品缺口：Android12+ OEM D2D排除政策、Room刪除成功ACK、品牌JPG權利來源、實機QR掃描、正式簽章及保資料升級。均未被JVM/文件工作關閉。

最終命令、SHA、CI及工程裁定：[FINAL_AGENT_HANDOFF](maintenance/FINAL_AGENT_HANDOFF.md)、[測試報告](maintenance/FINAL_TEST_REPORT.md)、[機器Gate](release/FINAL_RELEASE_GATE.json)。

## R3.8 工程整合與證據權威

使用者已批准限定的PR28＋PR34工程整合：Test Infrastructure、Build/CI Security、Documentation及Issue31 Product Maintenance。原提交ancestry保留；PR29 readiness仍是獨立OFFLINE_SPECIFICATION_ONLY切片，PR20／27不納入。整合分支為`integrate/r38-issue31-engineering`，是否已合併及最終SHA須核對[Issue31](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/31)所連結的整合PR與exact-head checks，不從本文件推定。

Issue31移除沒有production caller的direct-Vibrator fallback並要求View，不新增權限；super呼叫後仍return true，保留always-show政策；night-v27保留完整等值style。不改Dachen、candidate／decoder、orderedCommits／Editor ACK、JNI、版本或簽章。回歸範圍見[平台契約](testing/ISSUE31_PLATFORM_CONTRACTS.md)。

證據分層：PR28@ea3fdb24的CI40103973292為Build PASS、blocking AppLint4 errors/80warnings；此歷史FAIL不改判。PR34@ea186d7的CI40148115742為Build／Lint PASS、127 Python、App267 PASS/10 SKIP、Host57 PASS，Runtime SKIPPED；它仍不能替代新整合HEAD的CI。本次隔離工區的Python127／12contracts通過，但冷建置exit124，不能列為全套本機PASS。整合只在新鮮必要CI與完整diff獨立審查皆通過後正常merge；編譯／JVM不證Runtime或實機。舊停止收據保存在FINAL_TEST_REPORT與Gate的historical_receipts。
