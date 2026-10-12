# 工作樹盤點

開始15個linked/root工作樹（另有獨立D2.5 repo一個）；零刪除。以下status來自本輪git status -uall，ignored逐檔meta在離線inventory。新增R1工區另記最終handoff。

| Path | HEAD | attached/detached | dirty | tracked / untracked / ignored |
|---|---|---|---|---|
| `/Users/huang/KU-Yin_keyboardforandroid` | `fe1e96fb29ba2555b176b8889380a3ce9b2e9fb9` | `refs/heads/feat/mainline-docs-i18n` | YES | 146/1/4765 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/aie-alpha-release` | `a6a4c654845338521700121ac06586887d1ec085` | `refs/heads/release/aie-alpha.1` | NO | 730/0/45 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/aie-s0-learning-reset-safety` | `ea5f1853b3d4aba9f6c0deb1fbcd0e092b1cdac8` | `refs/heads/fix/aie-s0-learning-reset-safety` | NO | 711/0/970 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/aie-s1-candidate-browser` | `65ec00bf1bd2b90c9821c3ca11e0951c0b9b8e22` | `refs/heads/feat/aie-s1-candidate-browser` | NO | 723/0/3691 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/aie-v1-engineering-review` | `84dcf9a85d43b079a053b5b78e6fdcaa90a5bd2f` | `refs/heads/docs/aie-v1-engineering-review` | NO | 706/0/0 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/c4-clean-baseline` | `3d86f7538c47dc929e25cdb0bd0755f8cc3bad60` | `DETACHED` | YES | 193/2/1362 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/c4-compose-libchewing-switch` | `e72acb3d2ba1f58c25821d40dfb64bc54cee8c87` | `refs/heads/c4-compose-libchewing-switch` | YES | 150/3/1217 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/fix-pre-c4-production-hardening` | `3f0297652aa427666220885af976eeba53f95efe` | `refs/heads/fix/pre-c4-production-hardening` | NO | 148/0/1372 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/m4-performance-baseline` | `3d86f7538c47dc929e25cdb0bd0755f8cc3bad60` | `DETACHED` | YES | 193/4/2541 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p0-core-typing-recovery` | `3072affee95fa7fa8b0016ad85aac322272e4dea` | `refs/heads/fix/p0-core-typing-recovery` | NO | 685/0/2750 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1-alpha-acceptance` | `5c6bcad34c4048e40ce4e871e70e176c10195bcf` | `refs/heads/test/p1-alpha-acceptance` | NO | 490/0/1290 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1-alpha-acceptance-fixture` | `4e70acdb2a7ee9751ee295171cc7ef6fc65b1f2f` | `refs/heads/test/p1-alpha-acceptance-fixture` | NO | 621/0/3756 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1a-native-startup-hardening` | `870fc03cbfe8e5107b8ff8dc13478ac7754eec9f` | `refs/heads/fix/p1-series-correctness` | NO | 225/0/2726 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1d-enter-policy` | `94e91916d481959fb73e0adf37edb76764b26bed` | `refs/heads/fix/p1d-enter-policy` | NO | 463/0/2841 |
| `/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1g-runner-safety` | `85711bc8c749d488923954e8f4c173a493bf27eb` | `refs/heads/fix/p1g-acceptance-runner-safety` | NO | 640/0/3429 |

所有4個dirty工作樹patch＋未追蹤來源均離線備份，已獨立clone演練；禁止reset/clean/prune。

## R1 新增工區

本輪實際18個main-repo工區：原15＋consolidation／fixture／canonical main 三個；另獨立D2.5 repo未併入此數。原15個 HEAD/status/dirty bytes仍匹配外部 baseline，4個dirty已隔離還原演練；沒有刪除／prune。`.worktrees/mainline` 是今後產品主線 checkout（branch main），原root仍為使用者dirty docs branch。Host fixture工區保留待阻擋解除；R1文件工區保留正常PR歷史。
