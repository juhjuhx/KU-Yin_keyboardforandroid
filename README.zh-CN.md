# KU-Yin Keyboard for Android

> 一个开源、local-first 的 Android 注音输入法实验项目，基于 Android `InputMethodService`、Kotlin/View、JNI 与 libchewing。

[繁體中文](README.md) · **简体中文** · [English](README.en.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [Español](README.es.md) · [Português](README.pt-BR.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md)

## 当前状态

当前版本为 **`0.1.0-alpha`**。JVM 测试、Debug APK、Release APK 与 native dependency bootstrap 已能在 GitHub Actions 完成。Android 13 emulator 已实际验证 APK 安装、IME 注册、启用与切换成功。

自动 runtime smoke 目前仍停在 headless emulator 的“IME 窗口可见”断言，因此本版本属于 **alpha preview**，不是稳定发行版。

## 核心能力

- 大千（Dachen）注音输入与 libchewing 解码
- 组字、候选字与 `InputConnection` 同步
- ASCII / 密码字段输入模式
- Shift、数字、Space、Backspace、Enter 与 editor action
- 游标 / selection 变化后的 composition reconciliation
- `IME_FLAG_NO_PERSONALIZED_LEARNING` 对应的学习控制
- `armeabi-v7a`、`arm64-v8a`、`x86`、`x86_64`

## 下载与安装

请从 [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases) 下载最新 alpha 预览版，并查看 [APK/README.md](APK/README.md)。

- **Debug APK**：debug-signed，可直接安装测试。
- **Release unsigned APK**：未签名的开发者构建，用于验证 release build，并不是正式安装发行包。

安装后，在 Android 的键盘／语言与输入设置中启用 KU-Yin，再从输入法切换器选择 KU-Yin。不同厂商的设置路径会略有差异。

## 隐私与安全

KU-Yin 以 local-first 为基本原则，现有注音解码不依赖云端服务。输入法能够接触敏感文本，因此在用于重要资料前，请阅读 [SECURITY.md](SECURITY.md) 与源码，并留意项目仍处于 alpha 阶段。

## 自行构建

需要 JDK 17、Android SDK、Gradle 7.6.4、NDK `27.3.13750724` 与 CMake `3.22.1`。

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

更多说明见 [BUILD.md](BUILD.md)。

## 已知限制

- headless emulator 的 IME-window-visible smoke 尚未通过；install/register/enable/select 已验证。
- compileSdk / targetSdk 当前为 API 33。
- OpenCC、Hsu / Eten26、完整 accessibility、Emoji、clipboard、gesture typing 与 prediction 仍属于后续范围。
- 正式 release signing 尚未建立。

## 贡献与授权

开发说明见 [CONTRIBUTING.md](CONTRIBUTING.md)、[docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) 与 [docs/NEXT_STEPS.md](docs/NEXT_STEPS.md)。项目根授权为 [GNU LGPL 2.1](LICENSE)，第三方组件信息见 [NOTICE](NOTICE)。
