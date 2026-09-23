# CURRENT_STATE (what is true NOW — no future architecture here)

- Release: `v0.2.0-alpha.1` = old commit `278bcc2` (pre-C3.5; NOT this branch).
- main: `73e810f` = old Android main + unrelated `taui-workspace/` web files.
- C4 branch: `c4-compose-libchewing-switch`; D2.5 candidate `34a758b`
  (local-only until SDK GREEN + human push approval).
- Production decoder: `ZhuyinDictionarySession`. Native decoder: built into APK,
  exercised by tests/harness only — NOT production until D3.
- Tests: 24 classes / 99 @Test (JVM run pending SDK box). Contracts: 7/7 exit 0.
- Runtime/device: NOT TESTED this round. QR donation art: decorative until scanned.
- Open gates: SDK JVM + assembleDebug + runtime smoke; D3 authorization.
- Known debt: main contains TAUI files (recorded, revert separately, no rewrite);
  dual learning (Zhuyin prefs + libchewing flag) pending B8; `ROW_*` vs canonical
  Dachen duplication pending M4; PR #4 = donor, PR #6 = docs-only.
