# KU-Yin Keyboard for Android

KU-Yin 是一个开源、local-first 的 Android 注音 / Bopomofo 输入法。Android 层采用 Kotlin/View 与 `InputMethodService`，通过 JNI/C++ 调用 libchewing C API。

**上游：** libchewing 正式开发位于 https://codeberg.org/chewing/libchewing。当前 Android native 输入固定到 `fcitx5-android/prebuilt` commit `3587ba3355711f0aca50136e787719f6562676b8`，其对应的 libchewing source commit 为 `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`。

当前源码版本：**0.1.1-alpha**。CI 可以生成 Debug APK 与 unsigned Release APK。Android 13 emulator 已验证安装、IME 注册、启用、选择与 JNI 初始化；headless IME-window-visible assertion 仍为非阻塞实验项。

请从 GitHub Releases 下载测试版本。直接安装测试请优先使用 Debug APK；Release APK 当前未正式签章。

现有范围包括大千注音、`InputConnection` 组字/候选同步、ASCII/密码字段 session、editor action、selection reconciliation、禁止个性化学习控制与四种 Android ABI。

当前解码不需要云端服务，应用不申请 Android `INTERNET` permission。涉及敏感文字前请阅读 `SECURITY.md` 与 `docs/SECURITY_AUDIT.md`。

构建：

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest --stacktrace
gradle assembleDebug --stacktrace
gradle assembleRelease --stacktrace
```

完整使用指南、架构、Roadmap、致谢与 License 请阅读根目录 `README.md`。
