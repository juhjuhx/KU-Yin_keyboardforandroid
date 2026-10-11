# S1 來源、核對與實作設計

BASE_SHA/S0_FINAL_SHA: ea5f1853b3d4aba9f6c0deb1fbcd0e092b1cdac8
REVIEW_SHA: 84dcf9a85d43b079a053b5b78e6fdcaa90a5bd2f
P0_PRODUCT: 9f5db676a17b5770a738b5c20da937f9211e5a34
BRANCH: feat/aie-s1-candidate-browser
S0_GATE: 50類232測0failure/error/skip、nativeeditor5/5、獨立限定安全APPROVE；完整reset仍UNSUPPORTED。
UX: 使用者本prompt批准Option1，不重問。

## Fresh code audit
| Finding | Source | Behavior / Root / Gap | Severity |
|---|---|---|---|
| nativepage10 | pin editor/mod.rs:92–101；adapter:310–326 | default+identities每10plans分頁，投影/去重後可少於10，不是全部池僅10 | HIGH UI access |
| UI只當前頁 | UI:220–255,422–568 | LazyRow無前後頁/展開入口，session已有commands | HIGH |
| personalpreview | adapter:347–359 | :memory: + learningoff，不讀personal；S2另處理 | MEDIUM |
| nativesort | pin selection/phrase.rs:246 | candidates frequency sort預設false；不代表全部liveconversion不利用frequency | S2 |
| reset | MainActivity old699–701 | S0已停用局部clear及誤報 | MITIGATED |

獨立唯讀 /root/s1_initial_audit 核nativearchive及3檔bytes；native枚舉從page首到全部剩餘，choose index含page offset。保持nativepage0，adapter自己分頁；不加live PageUp/Down/cand_open。

## State/API
EngineUpdate追加純page metadata（pageIndex/pageSize/plan totalPages/selectionIds），原string候選/commit契約相容。ComposeImeState提供immutable CandidatePageSnapshot；provider、compositionGeneration、snapshotGeneration、page proof、opaque item identity。snapshot revision每次投影失效旧tap；composition generation只在composition action更新，presentation開合不query。
UI SelectCandidate帶完整ref；session驗ref後才engine選取，仍由adapter在live重找text/occurrence與fullbuffer核對。page callback綁同renderedstate；不引入第二commit或decoderowner。空projection有pageflags仍能翻頁；totalPages是plans頁數而非unique候選數，unknown totals顯示單一目前頁。

## UX
維持candidate chip/theme/fonts。正常單列可橫捲；preedit獨立有界header，避免擠走候選；expand48dp。Expanded grid bounded main-region高度，不擴至全螢幕；上一頁/頁碼/下一頁48dp、收合、preedit保留。只在中文候選頁啟用展開；英文prefix維持既有流程，不添新ranking。collapse零nativequery/commit，grid選字走原orderedCommits/ACK。

## Lifecycle
Reset/editorstart/finish/外部selection清expanded；完成composition清page。Hide request先收合/失效snapshot並暫關view inputgate，再requestHide；startinputview恢復gate。不要在paging/expand再次setComposingText觸碰editor。

## Gates / rollback
TDD session/UI/native；真pinJNI＋Robolectric editor（不冒實機）；全app/host/static/bootstrap/Debug/兩instrumentationcompile，串行Gradle。S1無排名/learning/touch/symbol/data migration，新version20003/0.2.0-aie.1，localcommits可回退，保存P0與frozen檔案。
