# KU-Yin C4 long-run ledger (authoritative after compaction)

- Branch: `c4-compose-libchewing-switch` @ `f959b4e` (worktree `/tmp/ku-yin-c4`, clean)
- Remote branch: still `e72acb3` (nothing pushed)
- Baseline: `e72acb3` → D2.5 `e3b3097`/`34a758b` → RC2 docs/tooling → `f959b4e`
- Toolchain root: `$HOME/.local/share/kuyin-toolchain/` (user-local, session env only)
- Gate states: STATIC PASS (8/8) / OCR delegation PASS, managed BLOCKED / JVM NOT RUN / BUILD NOT RUN / RUNTIME NOT RUN / DEVICE NOT TESTED
- D3 ENGINEERING: HOLD (needs JVM+build green)
- Toolchain self-provision (2026-09-23 ~22:48+): Temurin 21.0.12.1 OK,
  cmdline-tools OK, platform-tools/platform-36/build-tools-36/cmake OK,
  NDK 28.2 downloading (~14% at 22:5x, PID 54487, log /tmp/ndk-install.log)
- Next: finish toolchain provision → bootstrap → `verify --static` → `clean testDebugUnitTest` → `assembleDebug`
- Push: NOT PERFORMED until local gate green + human approval (prepared cmd on record)
- Forbidden: main merge/push, force-push, tags, releases, signing, Rime, INTERNET permission, telemetry
