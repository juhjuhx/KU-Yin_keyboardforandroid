# KU-Yin Iteration Report

这份报告记录项目如何从早期实验代码收敛为当前可重复建置的 Android IME alpha。它是历史摘要，不取代 Git commit history。

## 0. Early scaffold

项目最初由多套 AI coding workflow 协助建立 Android/JNI/文档骨架，包括 OpenCode + Mules Spark 1.3 与 Hermes + Agnes 2.0 Dash Flash。早期产物提供了方向、文件与部分代码，但当时生成的 APK 无法作为可用输入法完成验证，文档也混合了已实现、规划中与参考架构。

## 1. Forensic reset

后续 recovery 不直接继续补功能，而是重新检查 Manifest、Gradle/NDK/JNI、`InputMethodService`、key mapping、候选状态、libchewing provenance 与 GitHub Actions。核心原则改为：每一个完成声明都必须对应静态或 runtime 证据。

## 2. P0 / P1: build and input semantics

修正 IME Manifest/resource、Gradle wrapper/native bootstrap，以及大千 key mapping、Space/Backspace、candidate index 与 composition/commit 流。native dependency 改成固定 `fcitx5-android/prebuilt` revision，不再依赖本机预建目录。

## 3. P2: UI and lifecycle

调整 touch commit timing、MOVE/CANCEL、键位 hitbox、dark/font-scale 行为、candidate layout 与 input-view lifecycle。UI 继续使用 Android View/Canvas，避免为了重写而重写。

## 4. P3: architecture and privacy

把 `EngineUpdate` 与 engine contract 收敛到 core boundary，移除失效 user-dictionary/settings API，停止 config 层重建 native decoder；加入 `EditorPolicy` 与 `ImeSessionController`，处理 password、`IME_FLAG_FORCE_ASCII`、`IME_FLAG_NO_PERSONALIZED_LEARNING` 与 editor action。Android backup 关闭，user dictionary 改到 app-private no-backup storage。

## 5. R1–R4 recovery v2

Fresh audit 发现 dangling icon、OpenCC stub 错报 ready、policy 尚未接 runtime、ASCII surface 不完整等问题。随后建立 RED→GREEN contracts/tests，加入 ASCII digits/Shift/Enter、selection reconciliation、libchewing auto-learning gate，并把 runtime harness 独立出来。

## 6. Android runtime evidence

Android 13 emulator 已实证 APK 可安装、系统能辨识 KU-Yin IME、能够 enable/select，JNI/libchewing 初始化与 personalized-learning gate 可执行。headless environment 的 IME-window-visible assertion 仍未成为 blocking guarantee，因此不把它包装成完整 OEM/runtime compatibility。

## 7. Mainline and release

Recovery branch 最终以 squash merge 进入 `main`，建立 `v0.1.0-alpha` prerelease、Debug/unsigned Release APK、checksums 与 multilingual landing。随后进入本次 v0.1.1 consolidation，把大量 point-in-time audit/spec 收敛为少数权威文件，并建立安全的 upstream watch。

## Attribution ledger

- **juhjuhx**：项目 owner、产品方向、测试、repo/release 决策与最终验收。
- **OpenCode / Mules Spark 1.3**：早期 scaffold、实现探索与文档。
- **Hermes / Agnes 2.0 Dash Flash**：早期 Android/JNI、架构与状态文档。
- **ChatGPT / GPT-5.6 Sol**：forensic review、P0–R4 recovery、TDD contracts、CI/native/security/docs/release consolidation。

AI 项目记录代表工具输出曾实质影响仓库，不把 AI 系统视作人类 GitHub identity、法律作者或版权主体。代码与 commit history 仍是最终事实来源。
