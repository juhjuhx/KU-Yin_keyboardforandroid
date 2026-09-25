# KU-Yin APK Builds

Current source version: **`0.2.0-alpha.1**`.

Canonical downloads are GitHub Releases. APK binaries are intentionally not committed into Git history.

| Asset | Purpose | Status |
|---|---|---|
| `KU-Yin-v0.2.0-alpha.1-debug.apk` | direct alpha testing | debug-signed / installable |
| _(release artifact)_ | pending signing decision | not published; see root README.md |
| `SHA256SUMS.txt` | integrity verification | generated with release |

The prerelease workflow derives filenames from `app/build.gradle.kts` `versionName`. Always use the checksum attached to the specific GitHub Release; old run hashes are not authoritative for a newer build.

Installation and activation instructions are in the root `README.md`. Do not describe the unsigned Release APK as a production-signed package.
