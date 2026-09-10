# KU-Yin Keyboard for Android

KU-Yin is an open-source, local-first Zhuyin/Bopomofo IME for Android. The Android layer is Kotlin/View + `InputMethodService`; JNI/C++ connects it to the libchewing C API.

**Upstream:** official libchewing is developed at https://codeberg.org/chewing/libchewing. Current Android native inputs are pinned to `fcitx5-android/prebuilt` commit `3587ba3355711f0aca50136e787719f6562676b8`, corresponding to libchewing source commit `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`.

Current source version: **0.1.1-alpha**. CI builds Debug and unsigned Release APKs. Android 13 emulator evidence covers install/register/enable/select and JNI initialization; the headless IME-window-visible assertion remains non-blocking.

Download alpha builds from GitHub Releases. Use the Debug APK for direct testing; the Release APK is currently unsigned.

Core scope: Dachen Zhuyin, composition/candidates through `InputConnection`, ASCII/password sessions, editor actions, selection reconciliation, no-personalized-learning control and four Android ABIs.

The project does not require a cloud service for current decoding and does not request Android `INTERNET` permission. See `SECURITY.md` and `docs/SECURITY_AUDIT.md` before using alpha builds with sensitive text.

Build:

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest --stacktrace
gradle assembleDebug --stacktrace
gradle assembleRelease --stacktrace
```

See the root `README.md` for the full guide, architecture, roadmap, credits and license information.
