# KU-Yin APK Builds

Current preview: **`0.1.0-alpha`**

The canonical download location is [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.1.0-alpha). This directory intentionally does not commit APK binaries into Git history.

## Packages

| Package | Purpose | Install status |
|---|---|---|
| `KU-Yin-v0.1.0-alpha-debug.apk` | Debug-signed alpha preview | Installable for testing |
| `KU-Yin-v0.1.0-alpha-release-unsigned.apk` | Release-build verification artifact | Unsigned; developer artifact only |
| `SHA256SUMS.txt` | Checksums generated with the prerelease | Verify downloaded assets |

The prerelease workflow builds these files from the `main` branch and publishes them as GitHub Release assets. The checksum file attached to the release is authoritative for that release build.

## Run #85 reference evidence

Before the recovery branch was merged, GitHub Actions run `#85` verified that the Debug APK could be installed on Android 13, registered as an IME, enabled, and selected.

Reference SHA-256 for the APKs produced by run #85:

```text
1fdf5c86b0f040c4f2ab973d3bc912e493c147ab46bbe313751b786ef76f2121  app-debug.apk
0d5d7199688eb5a4a74b6248d0b74fa50d342d0a01ccb80c814e5b17c37e6027  app-release-unsigned.apk
```

These are reference hashes for run #85, not a substitute for the `SHA256SUMS.txt` attached to the prerelease built from `main`.

## Important

The release-named APK is currently **unsigned**. Do not present it as a production-signed release. For normal alpha testing, use the Debug APK until a dedicated release-signing flow is established.

See [BUILD.md](../BUILD.md), [SECURITY.md](../SECURITY.md), and [docs/PROJECT_STATUS.md](../docs/PROJECT_STATUS.md) for current verification scope and limitations.
