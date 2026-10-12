# R1 Implementation Plan

使用executing-plans按已批准prompt执行；root負責docs/doctor/Git，獨立workers唯讀稽核；Host adapter獨立工區與PR，Gradle序列執行。

- [x] 唯讀盤點main/release/PR/全部worktree/otherrepo，保存metadata。
- [x] 外部bundle、dirty patch/untracked、exact artifacts備份與clone還原演練。
- [ ] doctor: test_scripts_project_doctor 以temporary native stage RED（錯誤MISSING），修正路徑GREEN；獨立commit。
- [ ] 根據Kotlin/JNI/Gradle/Room建立10份架構source/owner/state/error/storage/test章節與Mermaid。
- [ ] canonical入口/權威/資料政策/歷史indexes一致；禁止把futureS2/M4画成實作。
- [ ] Host adapter獨立slice：geometry/stale/completion RED→GREEN，保留strict/editor asserts，編譯并CI emulator。
- [ ] 全Python/10contracts/bootstrap/JVM/Debug/host/instrumentation/Release signing gate、doclinks/JSON/YAML/Mermaid/diff checks。
- [ ] reviewer核actualdiff＋evidence，修正material findings。
- [ ] 正常PR merge，verifymain/tag/dirtyhash，寫finalhandoff/shortanchor。

Rollback是正常revert提交；archive復原不覆蓋原user工區，先isolatedclone驗證。production/physical gates保持獨立。
