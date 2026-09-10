# KU-Yin APK Builds

Current source version: **`0.1.1-alpha`**.

Canonical downloads are GitHub Releases. APK binaries are intentionally not committed into Git history.

| Asset | Purpose | Status |
|---|---|---|
| `KU-Yin-v0.1.1-alpha-debug.apk` | direct alpha testing | debug-signed / installable |
| `KU-Yin-v0.1.1-alpha-release-unsigned.apk` | verify release build | unsigned developer artifact |
| `SHA256SUMS.txt` | integrity verification | generated with release |

The prerelease workflow derives filenames from `app/build.gradle.kts` `versionName`. Always use the checksum attached to the specific GitHub Release; old run hashes are not authoritative for a newer build.

Installation and activation instructions are in the root `README.md`. Do not describe the unsigned Release APK as a production-signed package.
