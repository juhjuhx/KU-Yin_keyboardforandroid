# T24: Freeze — Final Security Audit, Permission Review, APK Size

> Wave 4 T24 | 2026-09-08 | In Progress
> Status: IN PROGRESS

---

## 1. Security Audit

### 1.1 Permission Review (AndroidManifest.xml)

| Permission | Declared? | Risk | Notes |
|---|---|---|---|
| INTERNET | NO | None | No network calls needed |
| VIBRATE | YES (maxSdk=30) | Low | Haptic feedback only |
| READ_CONTACTS | NO | None | Not needed |
| CAMERA | NO | None | Not needed |
| RECORD_AUDIO | NO | None | Not needed |
| POST_NOTIFICATIONS | NO | None | Not needed |

**Verdict**: PASS — Minimal permissions, no sensitive data access.

### 1.2 Dependency Audit

| Dependency | Version | Open Source? | License |
|---|---|---|---|
| androidx.core:core-ktx | 1.12.0 | Yes | Apache-2.0 |
| androidx.appcompat:appcompat | 1.6.1 | Yes | Apache-2.0 |
| com.google.android.material:material | 1.11.0 | Yes | Apache-2.0 |
| androidx.constraintlayout:constraintlayout | 2.1.4 | Yes | Apache-2.0 |
| androidx.lifecycle:* | 2.7.0 | Yes | Apache-2.0 |
| junit:junit | 4.13.2 | Yes | EPL-1.0 |

**Verdict**: PASS — All dependencies are open source with permissive licenses.

### 1.3 libchewing & OpenCC Licensing

| Component | License | Compatibility |
|---|---|---|
| libchewing | LGPL-2.1+ | Compatible (dynamic link or static with notice) |
| fcitx5-chewing | LGPL-2.1+ | Compatible |
| OpenCC | Apache-2.0 | Compatible |
| Original fork base (fcitx5-android) | LGPL-2.1 | Compatible |

**Verdict**: PASS — LGPL-2.1 + Apache-2.0 are compatible with distribution.

### 1.4 Code-level Security Checks

| Check | Status |
|---|---|
| No hardcoded secrets/keys | PASS |
| No reflection on private APIs | PASS |
| No exec()/Runtime.exec() | PASS |
| No WebView with JavaScript enabled | PASS |
| No SQLite with user-provided SQL (no SQLite used yet) | PASS |
| ProGuard enabled in release | PASS |
| No debug logging in release builds | TODO (verify after T21 JNI integration) |

---

## 2. APK Size Estimate

### Current (Wave 2-3, no native libs)

| Module | Estimated Size |
|---|---|
| app bytecode (Kotlin) | ~50 KB |
| resources (layouts, strings, colors) | ~10 KB |
| androidx dependencies | ~2 MB |
| material design | ~500 KB |
| **Total (debug, no proguard)** | **~3 MB** |
| **Total (release, proguard)** | **~2 MB** |

### With libchewing + OpenCC (future)

| Added | Size |
|---|---|
| libchewing_capi.a (all ABIs) | ~300 KB |
| OpenCC data (s2tw/tw2s JSON) | ~50 KB |
| **Additional APK size** | **~400 KB** |
| **Total estimated** | **~2.4 MB** |

**Target**: < 5 MB (well within F-Droid preference for lightweight apps)

---

## 3. Known Issues & Follow-ups

| # | Issue | Severity | Status |
|---|---|---|---|
| 1 | libchewing JNI not yet integrated | HIGH | T21 stub ready, JNI binding next |
| 2 | OpenCC not yet integrated | MEDIUM | T21 stub ready, C API binding next |
| 3 | DENY ACE blocks write to D:\666\opencode | MEDIUM | Requires admin PowerShell intervention |
| 4 | F-Droid/Play flavor split not yet in build.gradle | LOW | T23 spec complete, pending implementation |
| 5 | User dictionary persistence not implemented | MEDIUM | ChewingEngine.loadUserDict/saveUserDict stubs in place |

---

## 4. Wave 4 Completion Checklist

- [x] T21: engines/core split — ChewingEngine + ChineseConverter + IMEConfig interfaces
- [x] T21: AndroidChewingEngine stub (JNI placeholder)
- [x] T21: OpenCCConverter stub (C API placeholder)
- [x] T21: IMS wired to core interfaces
- [x] T21: CandidateView slotW bug fixed
- [x] T22: iOS notes documented (App Group, libchewing port, OpenCC)
- [x] T23: F-Droid/Play build matrix spec complete
- [ ] T24: Freeze security audit (this document — IN PROGRESS)
- [ ] T24: Build.gradle.kts flavor split implementation
- [ ] T24: Final git bundle for handoff

---

_T24 freeze audit in progress. Will finalize after build.gradle.kts updates._