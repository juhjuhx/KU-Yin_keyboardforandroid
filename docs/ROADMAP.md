# Roadmap

Roadmap 以“可用性与风险”排序，不承诺发布日期。

## P0 — release hardening

- 在实体 Android 设备完成大千输入、候选、password/ASCII、selection/app-switch smoke matrix。
- 建立正式 release signing / key management；在完成前 Release APK 继续标示 unsigned。
- 将 placeholder `com.example.androidkeyboard` applicationId/namespace 迁移为正式稳定命名，配套 upgrade strategy。
- 建立 native artifact hash manifest / source rebuild equivalence 检查，强化 libchewing supply-chain provenance。

## P1 — Android platform maintenance

- 独立升级 compileSdk/targetSdk 与 AGP/Gradle/Kotlin，不和输入行为修改混做。
- 建立 API 级别与主要 OEM compatibility matrix。
- per-key accessibility virtual nodes / TalkBack / Switch Access。
- IME switching、insets、横屏、平板和大屏 geometry 回归。

## P2 — input completeness

- 完整符号/Emoji surface。
- Hsu / Eten26：只有在 UI layout、decoder behavior、tests 同时完成后才公开。
- 评估真实 OpenCC backend；目前 pass-through/stub 不视为 feature。
- user dictionary 管理、导入/导出必须先完成隐私与格式设计。

## P3 — advanced features

- Clipboard、gesture typing、prediction/LM、更多语言模式。
- 任何会读取/持久化更多输入内容的功能都必须先有独立 threat model 与 opt-in 设计。

## Upstream modernization

官方 libchewing 已迁移 Codeberg，2026 年 0.12/0.13 系列加入 `chewing_new3()`、`chewing_handle_KeyboardEvent()` 等新接口。KU-Yin 目前仍使用固定 prebuilt 对应的 C API；升级 decoder 应通过独立 PR 验证 ABI、dictionary、learning、candidate 和四 ABI build，而非自动漂移。

## Project visual identity

代码/release 基线稳定后，将单独设计 KU-Yin 主视觉、repo banner 与 Android launcher icon。视觉资产不与功能 recovery 混在本轮 consolidation。
