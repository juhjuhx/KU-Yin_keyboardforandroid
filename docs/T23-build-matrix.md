# T23: F-Droid / Play Store Dual-Channel Build Matrix

> Wave 4 T23 | 2026-09-08 | Spec — build.gradle.kts changes pending
> Status: IN PROGRESS (spec written, gradle changes pending)

---

## 1. Dual-Channel Difference Matrix

| Item | Play Store | F-Droid |
|---|---|---|
| applicationId | com.example.androidkeyboard | com.example.androidkeyboard.fdroid |
| versionCode | Independent counter | Independent counter (usually < Play version) |
| minSdk | 26 | 26 (same) |
| ProGuard | enabled (release) | enabled (release) |
| Google Play Services | Not used | Not used (no dependency) |
| Signing keystore | Upload key | Different keystore |
| Icon/Banner | Customizable | Must meet F-Droid review guidelines |
| Analytics | Not collected | Strictly prohibited |
| LICENSE | LGPL-2.1 | LGPL-2.1 (must retain) |

## 2. flavorDimensions Configuration (pending)

`kotlin
android {
    flavorDimensions += \ channel\
    productFlavors {
        create(\play\) {
            dimension = \channel\
            applicationId = \com.example.androidkeyboard\
            versionCode = 1
            versionName = \0.1.0-alpha\
        }
        create(\fdroid\) {
            dimension = \channel\
            applicationId = \com.example.androidkeyboard.fdroid\
            versionCode = 1
            versionName = \0.1.0-alpha\
            buildConfigField(\boolean\, \IS_FDROID\, \true\)
        }
    }
}
`

## 3. CI/CD (GitHub Actions) — pending

Add build-play and build-fdroid jobs to .github/workflows/build.yml.

## 4. F-Droid Review Checklist

1. No INTERNET permission — already satisfied
2. No Google services dependency — already satisfied
3. All dependencies from public Maven — verify on each update
4. Version number convention — F-Droid requires matching major.minor
5. Re-signing by F-Droid bot — does not affect functionality

## 5. TODO

- [ ] build.gradle.kts: add flavorDimensions + productFlavors
- [ ] .github/workflows/build.yml: add dual-channel jobs
- [ ] Check compilelibrarysources for non-open-source deps
- [ ] Conditional compilation via BuildConfig.IS_FDROID

_T23 spec complete. Gradle changes pending T24 freeze._