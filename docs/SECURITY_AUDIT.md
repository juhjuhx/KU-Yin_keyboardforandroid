# Security, Privacy, Performance Audit (rebaselined 2026-09-23, static)

Baseline: full-tree grep + manifest + build files + JNI boundary read at D2.5
candidate. Not a third-party certification. Native-production claims are
withheld until D3 switches the decoder.

## Findings summary

| Area | Current evidence | Risk / follow-up |
|---|---|---|
| Network exfiltration | Source manifest requests no permissions; verified P1-D merged Debug APK requests only the app-defined signature `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` from AndroidX Core, no INTERNET/VIBRATE; no HTTP/WS/socket libs or URL fetch paths | Artifact clarification 2026-10-07; re-audit on any network dependency |
| Plaintext logging | Only 3 native-error `Log.e` in AndroidChewingEngine (load/init paths, no keystroke/preedit/candidate content) | Keep input content out of logs |
| Clipboard | WRITE-only (`FeedbackViewModel.copyToClipboard`, user share gesture); Compose donation/clipboard cards use write/paste-into-field paths; no programmatic READ harvesting found | Re-audit if any read path appears |
| User dictionary | `noBackupFilesDir/libchewing/user/userdict.dat` via installer | app-private/no-backup; no extra encryption (device-compromise out of scope) |
| Android backup | `allowBackup=false` | Done |
| Sensitive editor | `EditorPolicy` (text/web/visible/number password, FORCE_ASCII, NO_PERSONALIZED_LEARNING) + `EffectiveModePolicy` preferred/effective split; restricted editors cannot reach Zhuyin (policy guard + engine guard) | Device runtime matrix still required |
| Personalized learning | Gated on policy in both adapters; dual Zhuyin-prefs + libchewing-learn coexists | B8 must select single production ranking |
| JNI memory lifecycle | New/delete paired; GetStringUTFChars usage per audit doc; strings released after call | ASAN/instrumentation hardening later |
| Native supply chain | Pinned immutable commits (prebuilt 3587ba33, libchewing a6a8fa4) + bootstrap verification | Per-file hash manifest + rebuild equivalence still missing |
| Release signing | No production signing | Unsigned artifacts must never present as releases |
| Platform age | target/compile 36, Compose product UI | Current; CI pins toolchain |
| Performance | Local decoder, no network round-trip | No latency/memory benchmarks claimed |

## Data flow

```text
Touch → Compose UI → session → ZhuyinDictionary (production) → InputConnection
Touch → Compose UI → session → ChewingEngine → JNI → libchewing (D3 target)
```

No cloud service in either path. User dictionary maintained locally only.

## Artifact clarification (2026-10-07, exact local P1-D APK)

This adds a bounded artifact observation to the historical static audit, not a new
full security certification. Verified Debug APK SHA256:
`b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9`.

Its merged manifest declares and requests the app-defined signature permission
`io.github.juhjuhx.kuyin.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`; merger provenance
is `androidx.core:core:1.18.0`. Thus "zero permissions" applies to the source manifest,
not literally to the merged APK. INTERNET/VIBRATE remain absent; allowBackup=false.
Debug signature is valid and is not production signing.

The normative current decoder is ChewingEngineSession with startup-only dictionary
fallback (CURRENT_STATE/ARCHITECTURE); the data-flow sketch above is the historical
D2.5 audit context. Physical P1 runtime/security coverage remains NOT_TESTED.
Evidence: `review/2026-10-07-p1-alpha/產品APK核對.json` and `合併權限來源.txt`.
