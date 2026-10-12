# 分支來源與保留決策

盤點基線 main `a6a4c654845338521700121ac06586887d1ec085`。Git ancestry＋實際dirty state分類，非名稱判定。遠端 refs 不刪除。唯一產品權威為 GitHub main；R1 分支只作收束。

| Ref | SHA | 分類 | main未包含commit數 | 決策 |
|---|---|---|---:|---|
| `refs/heads/c4-compose-libchewing-switch` | `e72acb3d2ba1f58c25821d40dfb64bc54cee8c87` | DIRTY_LOCAL | 0 | KEEP |
| `refs/heads/chore/mainline-consolidation-r1` | `a6a4c654845338521700121ac06586887d1ec085` | MERGED | 0 | KEEP |
| `refs/heads/docs/aie-v1-engineering-review` | `84dcf9a85d43b079a053b5b78e6fdcaa90a5bd2f` | MERGED | 0 | KEEP |
| `refs/heads/feat/aie-s1-candidate-browser` | `65ec00bf1bd2b90c9821c3ca11e0951c0b9b8e22` | MERGED | 0 | KEEP |
| `refs/heads/feat/compose-mainline-v0.3` | `696c9e8bf94439e85b2c733431f061835441cd6a` | MERGED | 0 | KEEP |
| `refs/heads/feat/keyboard-shell-v0.2` | `94dc7b6225e1b4c4da2d5854918512c7477b2916` | HISTORICAL_REFERENCE | 55 | KEEP |
| `refs/heads/feat/mainline-docs-i18n` | `fe1e96fb29ba2555b176b8889380a3ce9b2e9fb9` | DIRTY_LOCAL | 1 | KEEP |
| `refs/heads/fix/aie-s0-learning-reset-safety` | `ea5f1853b3d4aba9f6c0deb1fbcd0e092b1cdac8` | MERGED | 0 | KEEP |
| `refs/heads/fix/p0-core-typing-recovery` | `3072affee95fa7fa8b0016ad85aac322272e4dea` | MERGED | 0 | KEEP |
| `refs/heads/fix/p1-series-correctness` | `870fc03cbfe8e5107b8ff8dc13478ac7754eec9f` | MERGED | 0 | KEEP |
| `refs/heads/fix/p1d-enter-policy` | `94e91916d481959fb73e0adf37edb76764b26bed` | MERGED | 0 | KEEP |
| `refs/heads/fix/p1g-acceptance-runner-safety` | `85711bc8c749d488923954e8f4c173a493bf27eb` | MERGED | 0 | KEEP |
| `refs/heads/fix/pre-c4-production-hardening` | `3f0297652aa427666220885af976eeba53f95efe` | MERGED | 0 | KEEP |
| `refs/heads/fix/r1-host-fixture-adapter` | `a6a4c654845338521700121ac06586887d1ec085` | MERGED | 0 | KEEP |
| `refs/heads/main` | `653b4a3d9daaea94a2391a2e3175b7a02784845e` | MERGED | 0 | KEEP |
| `refs/heads/release/aie-alpha.1` | `a6a4c654845338521700121ac06586887d1ec085` | MERGED | 0 | KEEP |
| `refs/heads/test/p1-alpha-acceptance` | `5c6bcad34c4048e40ce4e871e70e176c10195bcf` | MERGED | 0 | KEEP |
| `refs/heads/test/p1-alpha-acceptance-fixture` | `4e70acdb2a7ee9751ee295171cc7ef6fc65b1f2f` | MERGED | 0 | KEEP |
| `refs/remotes/origin/HEAD` | `a6a4c654845338521700121ac06586887d1ec085` | MERGED | 0 | KEEP |
| `refs/remotes/origin/c4-compose-libchewing-switch` | `3d86f7538c47dc929e25cdb0bd0755f8cc3bad60` | MERGED | 0 | KEEP |
| `refs/remotes/origin/chore/repository-consolidation-v0.1.1` | `9e3ed359b7b8ce8f59e29d0551bbfcec1fc70b57` | HISTORICAL_REFERENCE | 2 | KEEP |
| `refs/remotes/origin/design/keyboard-shell-v0.2` | `accf58e8f081d84951939956d6aa0c24d05b5d6c` | HISTORICAL_REFERENCE | 2 | KEEP |
| `refs/remotes/origin/feat/aie-s1-candidate-browser` | `2ce6810684fd65a65a3006a224dc786b1f3a5f62` | MERGED | 0 | KEEP |
| `refs/remotes/origin/feat/compose-mainline-v0.3` | `696c9e8bf94439e85b2c733431f061835441cd6a` | MERGED | 0 | KEEP |
| `refs/remotes/origin/feat/keyboard-shell-v0.2` | `94dc7b6225e1b4c4da2d5854918512c7477b2916` | HISTORICAL_REFERENCE | 55 | KEEP |
| `refs/remotes/origin/feat/keyboard-shell-v0.2-red1` | `accf58e8f081d84951939956d6aa0c24d05b5d6c` | HISTORICAL_REFERENCE | 2 | KEEP |
| `refs/remotes/origin/feat/mainline-docs-i18n` | `fe1e96fb29ba2555b176b8889380a3ce9b2e9fb9` | HISTORICAL_REFERENCE | 1 | KEEP |
| `refs/remotes/origin/fix/p0-real-device-input` | `f0f3e20d1777ba6dcf7b1baac13ae704228e6967` | HISTORICAL_REFERENCE | 8 | KEEP |
| `refs/remotes/origin/fix/pre-c4-production-hardening` | `3f0297652aa427666220885af976eeba53f95efe` | MERGED | 0 | KEEP |
| `refs/remotes/origin/main` | `a6a4c654845338521700121ac06586887d1ec085` | MERGED | 0 | KEEP |

PR #4 的 View donor 是 HISTORICAL_REFERENCE，PR #6 的舊架構/i18n文件待按當前架構逐份移植，不能整支merge回舊產品。其餘已併入的P0/P1/AIE checkpoints保留回退路線。沒有必要重新合併 S0/S1。
