# KU-Yin AIE Alpha 發布交接

更新：2026-10-12。使用者已授權 push／正常合併／experimental prerelease；本輪沒有產品修改或資料刪除。

## 來源與發布

- 產品：`39acf96c6cfc058106315f3553e1c8b88438f4f7`。
- 整合／tag：`2ce6810684fd65a65a3006a224dc786b1f3a5f62`；`v0.2.0-aie.1`。
- PR #8 正常 merge：`912b5cc4498415464cd637193258f15f3c153842`；保留原 main 的兩個 TAUI 檔，沒有 force／rewrite／刪工區。
- [公開 experimental prerelease](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1)。正式版仍 NOT_READY，不宣稱 full P1 physical acceptance。

## 精確資產

`KU-Yin-AIE-S1-candidate-browser-debug.apk`：30,364,775 bytes；`20003 / 0.2.0-aie.1`；applicationId `io.github.juhjuhx.kuyin`。

- SHA256：`acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`。
- Signer：`46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94`（與P0相同Debug certificate）。未重新簽Release。
- source/relink tar：`5a6d5ce96bc93f3385473c952912ca266958a51a9acb127a2ba6ab2363877ab8`；4,806包內檔manifest核對，四ABI真link成功且 `.text`／`.rodata` 和APK相同。
- Release另附 `SHA256SUMS`、`release-provenance.json`、`CI-RUNTIME-STATUS.md`、`INDEPENDENT-REVIEW.md`、`MANUAL-DEVICE-CHECK.md` 與 notes。公開資產server digest／size已逐份核對。
- 未修改原P0／P1凍結APK／v1-v3 archives，未提交APK、ZIP、keys或private device state。

## 驗證與限制

[CI38164320915](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/38164320915)：blocking build PASS；API33/x86_64 app instrumentation 5/5 PASS。外部host HTML報告 **32 tests：15 PASS、9 FAIL、8 skipped**；stdout40是事件顯示差異，不能說40個不同測試。

9FAIL涉及stale UI node；已確認旧candidate selector與S1兩列header/strip不相容。部分preedit timeout原因未證明，EN02 error出在cleanup，不能據此宣稱英文decoder已失敗。獨立故障審查沒有已確認product High；所有未觀察typing/selector scope仍未通過，沒有篡改assertions。

本輪Python107 tests、10contracts PASS；S1之前App265／Host37與native/editor/Compose的來源驗證證據保持原時間點。Source-only reviewer APPROVE適用實驗性素材發布，非physical/production/legal全面簽核。OCR401：BLOCKED_BY_PROVIDER，未反覆重試。

完整native learning erase、S1 physical/OEM/TalkBack、正式key、完整Rust runtime inventory、舊manual CI publication流程改善仍OPEN。M4 NOT_VALIDATED，AIE-S2未開始。

## 最小下一步

1. 適配目前S1的fixture candidate geometry與P0即時選字completion contract。
2. 對read-only observation／cleanup做bounded fresh-query，不盲目重送點擊；保留strict preedit及exactly-once editor assertions。
3. 補當前32-test identity／XML evidence，重跑host suite後分類殘餘timeout。
4. 依manual check用此exactAPK做手機捲動／展開／翻頁／實際editor commit復驗。不得為測試清除個人字典。

本地交付：`/Users/huang/Desktop/KU-Yin-AIE-Alpha-發布材料-2026-10-12/`；公開下載不依賴此路徑。續作先讀 `../engineering/CODEX_CONTEXT_ANCHOR.md`，不重讀全部歷史。
