# KU-Yin Keyboard for Android

> 一个以台湾注音输入为核心、local-first 的开源 Android 输入法。Android 端使用 Kotlin/View 与 `InputMethodService`，通过 JNI/C++ 调用 libchewing C API。

[![Build](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/workflows/build.yml/badge.svg)](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/workflows/build.yml)
[![License: LGPL-2.1](https://img.shields.io/badge/License-LGPL--2.1-blue.svg)](LICENSE)

**繁體中文** · [简体中文](docs/i18n/README.zh-CN.md) · [English](docs/i18n/README.en.md)

## 上游与技术基础

KU-Yin 的核心不是自造中文解码器，而是在 Android IME 层把成熟上游能力以可测试、可追溯的方式接入：

| 上游 / 基础 | KU-Yin 中的角色 | 当前边界 |
|---|---|---|
| [libchewing](https://codeberg.org/chewing/libchewing) | 注音解码、候选与用户词典核心 | 通过 C API / JNI 使用；正式开发已迁移 Codeberg |
| [fcitx5-android/prebuilt](https://github.com/fcitx5-android/prebuilt) | Android 四 ABI 的 libchewing 预构建来源 | 固定 commit `3587ba3355711f0aca50136e787719f6562676b8` |
| libchewing source pin | 上述 prebuilt 对应的源码来源 | `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c` |
| Android `InputMethodService` / `InputConnection` | 系统 IME 生命周期与编辑器接口 | KU-Yin Android adapter 的平台契约 |
| Kotlin / Android View | 键盘、候选列、session 与 editor policy | KU-Yin 自有 Android 层 |
| C++ JNI bridge | Kotlin 与 libchewing C API 的边界 | KU-Yin 自有 native adapter |

精确 provenance、参考项目与授权边界见 [`docs/UPSTREAM.md`](docs/UPSTREAM.md) 与 [`NOTICE`](NOTICE)。KU-Yin **没有自有 Rust 层**；现代 libchewing 上游大量采用 Rust，并不意味着本项目需要为了技术栈外观额外引入 Rust。

## 当前状态

当前源码版本为 **`0.1.1-alpha`**。这是可建置、可安装测试的 alpha，不是稳定版。

已建立的验证链包括 JVM 单元测试、输入映射/架构 contracts、固定 native dependency bootstrap、Debug APK、unsigned Release APK，以及 Android 13 emulator 的安装、IME 注册、启用与选择。headless emulator 的 IME window-visible assertion 仍属于非阻塞实验项，实际 OEM / 实机兼容性仍需持续扩充。

下载请使用 [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases)。Debug APK 为 debug-signed，可直接测试；Release APK 目前仍未配置正式签章，因此只作为 release-build developer artifact。

## 核心能力

- 大千（Dachen）注音键位与 libchewing 解码
- 组字、候选与 Android `InputConnection` 同步
- 候选通过 native candidate index 提交
- ASCII / 密码字段输入表面
- Shift、数字、Space、Backspace、Enter 与 editor action
- 游标 / selection 改变后的 composition reconciliation
- `IME_FLAG_NO_PERSONALIZED_LEARNING` 对应的学习控制
- `armeabi-v7a`、`arm64-v8a`、`x86`、`x86_64` 四 ABI
- app-private、no-backup 的 libchewing 数据目录
- 上游 native 输入固定 revision，并在缺件时让 build 明确失败

## 安装与使用指南

1. 从 Releases 下载最新 **Debug APK**。
2. 安装 APK。Android 会提示输入法属于可读取输入内容的高敏感系统组件，这是 IME 的正常安全提示。
3. 到「设置 → 系统 / 一般管理 → 语言与输入 / 键盘 → 屏幕键盘 / 管理键盘」启用 **KU-Yin**。各 OEM 名称不同。
4. 打开任意文字输入框，通过系统输入法切换器选择 KU-Yin。
5. 普通文字字段使用大千注音；密码或强制 ASCII 字段会走 ASCII-oriented session。
6. 若遇到键盘不出现、候选异常或 OEM 特定问题，请在 Issue 附 Android 版本、设备型号、可复现步骤和 Logcat 中**不含敏感输入内容**的错误信息。

## 架构

```text
Android Framework
EditorInfo / InputConnection / IME lifecycle
                    │
                    ▼
        ChewingInputMethodService
                    │
        ┌───────────┼───────────────┐
        ▼           ▼               ▼
  EditorPolicy  ImeSession      KeyboardView /
                 Controller      CandidateView
        │           │               │
        └───────────┴───────┬───────┘
                            ▼
                    ChewingEngine contract
                            │
                            ▼
                  AndroidChewingEngine
                            │
                            ▼
                       JNI / C++
                            │
                            ▼
                    libchewing C API
```

Android 生命周期、隐私策略、session 状态、UI 与 native decoder 尽量分层，避免 View 直接依赖 JNI。详细说明见 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)。

## 隐私、安全与资料外泄边界

当前源码没有申请 Android `INTERNET` permission，也没有发现网络输入路径、Clipboard 功能或将按键/候选明文写入日志的代码。libchewing system/user data 位于 `noBackupFilesDir`，Manifest 关闭 Android backup。敏感 editor 会禁用候选/composition 并可关闭 personalized learning。

这些控制不等于“绝对安全”。用户词典本身可能包含敏感词汇，目前依赖 Android app-private storage 而非应用层额外加密；root / 已被攻破设备不在此威胁模型内。native prebuilt 虽固定 commit，但尚未建立源码重编与逐文件 hash 等价验证。完整审计见 [`docs/SECURITY_AUDIT.md`](docs/SECURITY_AUDIT.md) 与 [`SECURITY.md`](SECURITY.md)。

## 自行建置

参考 CI 工具链：JDK 17、Gradle 7.6.4、AGP 7.4.2、Kotlin 1.9.22、Android API 33、NDK `27.3.13750724`、CMake `3.22.1`。

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest --stacktrace
gradle assembleDebug --stacktrace
gradle assembleRelease --stacktrace
```

Debug APK：`app/build/outputs/apk/debug/app-debug.apk`

Unsigned Release APK：`app/build/outputs/apk/release/app-release-unsigned.apk`

环境、CI 与 release 说明见 [`docs/DEVELOPMENT.md`](docs/DEVELOPMENT.md)。

## 上游更新策略

`.github/workflows/upstream-watch.yml` 定期检查 `fcitx5-android/prebuilt` 与官方 libchewing release。发现变化时只创建 / 更新审查 Issue，不会直接改 `main`。任何 native pin 更新都必须重新通过 bootstrap、JVM tests、Debug/Release APK build 后才能合并。

## Roadmap

后续优先级见 [`docs/ROADMAP.md`](docs/ROADMAP.md)。近期重点是实机/OEM matrix、正式 release signing、API/target SDK 现代化、per-key accessibility、符号/Emoji 与后续布局；OpenCC、prediction、gesture typing 等不会在未真正接通前写成已完成能力。

## 项目迭代记录

KU-Yin 曾经历“可生成 APK 但实际不可用”的早期阶段，之后通过 P0–R4 recovery 把 Manifest、libchewing/JNI、输入语义、UI lifecycle、privacy policy、CI、runtime smoke 与 release flow 分阶段恢复。完整迭代报告与设计记忆见 [`docs/HISTORY.md`](docs/HISTORY.md)。

## 致谢与贡献记录

项目维护者：**juhjuhx**。

开发过程中使用过 OpenCode / Mules Spark 1.3、Hermes / Agnes 2.0 Dash Flash，以及 ChatGPT / GPT-5.6 Sol。这里的 AI attribution 代表它们的输出曾实质影响仓库，不把 AI 系统当作人类 GitHub 身份、法律作者或版权主体。早期实现即使后来被替换，仍保留参与记录。

更重要的技术基础来自 libchewing、fcitx5-android prebuilt、Android Open Source Project / Android Developers，以及社区键盘项目带来的架构与 UX 参考。生产依赖与 reference-only 项目会在 [`docs/UPSTREAM.md`](docs/UPSTREAM.md) 明确区分。

## 参与开发

请阅读 [`CONTRIBUTING.md`](CONTRIBUTING.md)。提交应优先附可复现问题、最小修改、对应测试和验证证据。输入法属于敏感组件，不接受未经说明的网络输入、按键内容遥测或把第三方实现直接复制进来的 PR。

## License

KU-Yin 仓库使用 [`GNU LGPL 2.1`](LICENSE)。第三方来源、固定 revision 与归属见 [`NOTICE`](NOTICE) / [`docs/UPSTREAM.md`](docs/UPSTREAM.md)。对于静态链接 libchewing 的再发布义务，发行者应自行确认 LGPL 对源码提供与可重新链接能力的具体要求；本项目文档不构成法律意见。
