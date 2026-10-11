# AIE V1 Architecture Tree

```text
KU-Yin / :app
├─ service [EXISTS] lifecycle / native owner / EditorPolicy / EditorEnterPolicy
│  ├─ EditorEffectTracker [EXISTS] offsets + ACK + recovery
│  └─ CommitEffectObserver [PROPOSED] acknowledged supplemental learning
├─ ime/ui [EXISTS] Compose themes / canonical Dachen / candidate LazyRow
│  ├─ CandidatePanel [S1 EXISTS / strip + bounded grid]
│  ├─ expanded paging [S1 EXISTS / immutable scoped snapshot]
│  └─ TouchResolver [LATER / PRIVACY+CORRECTION_APPROVAL_REQUIRED]
├─ ime/session [EXISTS] Chinese sole owner + English token owner
├─ engines/android [EXISTS] libchewing adapter / derived scratch previews
│  └─ CandidateRanker seam [LATER / IDENTITY+REPLAY_GATES]
├─ userdict.dat [EXISTS] native Chinese learning
│  └─ reset controls [S0 UNSUPPORTED / disabled, no deletion]
├─ UserLearning [PROPOSED] supplemental stores / versioning / erase
├─ SymbolRepository [LATER / DATA_LICENSE+UX_APPROVAL_REQUIRED]
└─ community/donation/credits [PRESERVE]

:acceptance-host [TEST ONLY] real EditorInfo / InputConnection observer
├─ old app5/host26 [FROZEN package; NOT full physical acceptance]
└─ P0 core six methods [COMPILE_READY / DEVICE_EXECUTION_OPEN]

scripts / native host probe [EXISTS; synthetic, offline]
release [BLOCKED] hardware / licenses / privacy / signing / CI integration
```

只有EXISTS為目前產品；其餘是提案，不因畫出dependency就宣稱implemented。
