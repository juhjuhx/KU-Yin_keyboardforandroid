# R3.2 續作入口

main8369da25；兩隔離branch：fix/r3-2-image-integrity（十Launcher重新生成，manifest/雙decoder/112Python/11contracts/獨立review；Android與PR結果見最新R3_2_IMAGE_HANDOFF），fix/r3-2-h1-runtime（TEST_ONLY調查，非產品修復）。先讀CURRENT_STATE與各切片精確handoff，避免重做PR18/22文件。

CI39509158938：App5 PASS，Host15PASS9FAIL8SKIP。H1原始null-root／3006ms timeout因果仍UNRESOLVED；不增加timeout、不弱化oracle、不在H1未通過時執行H2。核實Runtime宣傳圖0；greeting PNG未修。Release NO_GO；不碰其他工區、資料、簽章、oldAlpha、S2/M4。

---

# KU-Yin 續作短入口

2026-10-21 publication closure。先讀 [CURRENT_STATE](../CURRENT_STATE.md)、[R3_FINAL_HANDOFF](../release/R3_FINAL_HANDOFF.md)、[PUBLICATION_VALIDATION](../release/PUBLICATION_VALIDATION.md) 與 [TODO_AND_GATES](../release/TODO_AND_GATES.md)，再讀必要 source；不用重載整段聊天或數百份 review。

main575cc883；PR18 已合併。產品仍是 AIE-S1／公開 Debug Alpha20003，沒有新 RC。PR20 Draft、PR12 Draft、PR10 HOLD。main CI39442957936 App5 PASS／Host15 PASS9 FAIL8 SKIP。隔離 R3.1 runtime feab29f 的 H1 仍 null root／無 XML／3006ms timeout，H2 未執行；十份文件保存6e4bcd7 已推原分支。

文件／社群切片 branch docs/publication-closure-20261021，不改 Kotlin/JNI、pin、簽章、資料、工區或 frozen assets。來源與長短文見 community/README；13 圖分類完成，核實 Runtime 宣傳截圖0，圖卡方向待答。無社群代發。

RELEASE=NO_GO。下一最小 runtime 工作是既有 raw 的單因子 H1 假說與 meaningful RED；不盲重跑、不增加 timeout、不弱化 assertion。先 fresh same-boot H0/H1，再單獨批准階段的 H2/App/Host。實機、QR scan、更新保資料、D2D、key custody、完整 license review 仍 OPEN。不要動其他工區或 AutoPilot 設定；無 init/compact/LSP 專用 API，不冒稱調用。
