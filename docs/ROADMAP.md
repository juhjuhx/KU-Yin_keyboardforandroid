# Roadmap (FUTURE work only; done items live in HISTORY/CURRENT_STATE)

## DONE (do not re-plan)

- applicationId migration → `io.github.juhjuhx.kuyin` (shipped in build file)
- SDK modernization → JDK21/Gradle9/AGP9/Kotlin2.2/compile-target36/NDK28.2 (shipped)
- C4-D2 decoder sessions + D2.5 state ownership (branch work, SDK verification pending)

## CURRENT

- D2.5 SDK gate: JVM + assembleDebug + runtime smoke on exact candidate SHA
- D3 production libchewing switch (needs D2.5 PASS + human authorization)

## NEXT

- Physical CASE-4/CASE-6 + long-sentence auto-selection acceptance
- M4 UX slices (candidate strip, geometry, touch, QWERTY, symbols, long-press, emoji, toolbar)
- Formal release signing / key management (Release APK stays unsigned until then)
- libchewing supply-chain: per-file hash manifest + source-rebuild equivalence

## LATER

- Device/OEM compatibility matrix, accessibility virtual nodes, insets/rotation/tablet
- Hsu/Eten26 only with layout+decoder+tests complete together
- OpenCC backend evaluation (current stub is not a feature), userdict import/export (privacy design first)
- Clipboard/gesture/prediction features each need threat model + opt-in first
- Upstream libchewing (Codeberg 0.12/0.13 APIs) via independent ABI-verified PR, never auto-drift
- Brand/visual identity after code baseline stabilizes
