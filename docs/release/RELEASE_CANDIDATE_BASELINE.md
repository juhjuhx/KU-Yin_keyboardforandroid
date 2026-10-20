# R3 Release Candidate Baseline

ACTIVE／2026-10-19；這是隔離候選來源，尚未成為 main 或公開 Release。

| Identity | Exact source / state |
|---|---|
| GitHub main（fresh API 27） | `63c9e4ab8ad7e277013acb5d7cce99228ec1f827` |
| QR component independently reviewed | `de2ac7d94e0a79f492d11ee165173e65b4562bd7` |
| R2.3.1 infrastructure | `e137e37594c04f8c0fd88c01ef911d1df9bcbc49` |
| Isolated local integration | `185a4e545bd2574ad773e255ea558e6be5f3ab0e`，兩個parent均保留 |
| Worktree / branch | `.worktrees/r3-release-candidate` / `release/rc-closure-r3` |
| Public Alpha tag | `v0.2.0-aie.1` → `2ce6810684fd65a65a3006a224dc786b1f3a5f62`，不動 |
| Public APK | 20003／0.2.0-aie.1；SHA256 `acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634` |
| Public signer | `46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94` |
| Candidate source version | 20004／0.2.0-rc.1；不是 RC_READY 或正式簽章證明 |
| PR10 / PR12 / PR18 | ea5c2b5 OPEN/HOLD；d6bf2a6 Draft；4190e0a Draft（README獨立審查），均未merge |

## Change and preservation boundary

唯一產品行為差異是 donation card 的正確離線 QR、官方 URL、可存取標籤及誠實掃描說明。Dachen、decoder/session、candidate identity、orderedCommits、InputConnection ACK、JNI、native pin、簽章設定、appID、ABI、Room/prefs schema 均不更改。QR decoder 只有 testImplementation，不進 runtime。

R2／R231 僅在候選分支正常整合，沒有重複套用 PR10／12，也未改舊工區。原 App visibility、Host strict preedit/candidate/commit assertions 保留；後續 CI 透過固定 product／infrastructure／composed test source groups 驗證。不得把 local merge 或 health-only PASS 當 Android Runtime 通過。

## Evidence and open gates

外部 session：`KU-Yin-R3-RC-Closure-2026-10-18-205053/`。00/01為起始24工區／remote baseline，27是新的remote核對；07與17真RED，18最後QR8/8GREEN及12張畫面，QR_FINAL_INDEPENDENT_REVIEW限定元件PASS。沒有執行手機安裝／IME切換；ADB inventory空。

後續fresh source `b98206267b3a65c452db1661bb3bae910ec61f6a` 已推送隔離branch；唯一CI39152444358 H0HEALTHY、H1INCONCLUSIVE，H2/App5/Host33/32-case/B01未執行。新本機Debug APK與source/relink材料已產生、四ABIrelink驗證完成；正式key/ownership、實機、QR手機scan、TalkBack、OEM D2D、完整native法律／runtime inventory仍OPEN。精確結果見[R3驗證矩陣](R3_RELEASE_VALIDATION_MATRIX.md)。NO_GO，不新增或覆寫Release/tag，不移植ASR／S2／M4。
