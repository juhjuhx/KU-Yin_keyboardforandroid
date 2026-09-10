# KU-Yin Keyboard for Android

> An open-source, local-first Zhuyin (Bopomofo) input method for Android, built with Android `InputMethodService`, Kotlin/View, JNI, and libchewing.

[繁體中文](README.md) · [简体中文](README.zh-CN.md) · **English** · [日本語](README.ja.md) · [한국어](README.ko.md) · [Español](README.es.md) · [Português](README.pt-BR.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md)

## Status

The current version is **`0.1.0-alpha`**. JVM tests, Debug APK, Release APK, and pinned native dependency bootstrap are working in GitHub Actions. On an Android 13 emulator, the APK has been verified to install, register as an IME, become enabled, and become the selected input method.

The automated runtime smoke test still fails the IME-window-visible assertion on a headless emulator. Treat this build as an **alpha preview**, not a stable release.

## Core capabilities

- Dachen (大千) Zhuyin input backed by libchewing
- Composition/candidate synchronization through `InputConnection`
- ASCII and password-field input surfaces
- Shift, digits, Space, Backspace, Enter, and editor actions
- Composition reconciliation after cursor/selection changes
- Personalized-learning policy for `IME_FLAG_NO_PERSONALIZED_LEARNING`
- `armeabi-v7a`, `arm64-v8a`, `x86`, and `x86_64` builds

## Download and install

Get the current alpha preview from [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases) and see [APK/README.md](APK/README.md) for package details.

- **Debug APK**: debug-signed and intended for direct testing.
- **Release unsigned APK**: an unsigned developer artifact used to verify the release build; it is not a production install package.

After installation, enable KU-Yin in Android's keyboard/language & input settings, then select KU-Yin using the input-method switcher. Menu names vary by Android vendor.

## Privacy and security

KU-Yin follows a local-first design. The current Zhuyin decoding path does not require a cloud service. Because an IME can access sensitive text, review [SECURITY.md](SECURITY.md), the source code, and the current alpha limitations before using it for sensitive data.

## Build from source

The CI baseline uses JDK 17, Android SDK, Gradle 7.6.4, NDK `27.3.13750724`, and CMake `3.22.1`.

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

See [BUILD.md](BUILD.md) for details.

## Known limitations

- The headless-emulator IME-window-visible smoke assertion is still failing; install/register/enable/select are verified.
- compileSdk / targetSdk are currently API 33.
- OpenCC conversion, Hsu/Eten26 user layouts, full per-key accessibility, emoji, clipboard, gesture typing, and prediction are not complete production features.
- Production release signing is not configured yet.

## Contributing and license

See [CONTRIBUTING.md](CONTRIBUTING.md), [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md), and [docs/NEXT_STEPS.md](docs/NEXT_STEPS.md). The repository root license is [GNU LGPL 2.1](LICENSE); third-party attribution is documented in [NOTICE](NOTICE).
