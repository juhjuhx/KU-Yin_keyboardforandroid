# Contributing to KU-Yin

KU-Yin 是 Android Zhuyin/Bopomofo IME。贡献应保持可追溯、可测试，并尊重输入法属于高敏感软件这一事实。

## 开始前

请先阅读：`README.md`、`docs/ARCHITECTURE.md`、`docs/DEVELOPMENT.md`、`docs/UPSTREAM.md`、`SECURITY.md`。

历史 recovery 资料位于 Git history 与 `docs/archive/`，不再作为当前实现的 source of truth。

## 适合贡献的范围

优先欢迎可复现 Android runtime / OEM bug、大千输入正确性、editor/session policy、JNI/libchewing integration、accessibility、build reproducibility、安全/隐私 hardening 与文档修正。

不要在一个 bug-fix PR 里同时塞 SDK 大升级、UI 重写、decoder 更换和全新 feature family。

## Build 与测试

```bash
python3 scripts/check_dachen_contract.py
python3 scripts/check_p0_keyboard_contract.py
python3 scripts/check_editor_sync_contract.py
python3 scripts/check_p1_build_contract.py
python3 scripts/check_p2_ui_contract.py
python3 scripts/check_p3_architecture_security_contract.py
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest --stacktrace
gradle assembleDebug --stacktrace
gradle assembleRelease --stacktrace
```

Python contracts 是快速 preflight；Kotlin 行为以 JVM tests 为主要 truth。涉及 IME lifecycle / native 的修改应附 runtime evidence。

## 第三方代码

不要因为另一个键盘“看起来能用”就直接复制实现。任何 source transplantation 都要检查具体文件/模块 license 与 provenance。libchewing 是当前 production decoder；fcitx5-android、FlorisBoard、HeliBoard、AOSP 等大多作为 architecture/behavior research reference，除非另有明确复用记录。

## 安全要求

禁止未经明确设计与说明的网络传输输入内容、plaintext keystroke logging / telemetry，以及在 app-private 预期路径之外持久化输入内容。不要以“CI 绿灯”替代敏感 editor/runtime security evidence。

## Pull Request

PR 描述应包含问题/根因、修改边界、测试、build 证据、runtime 证据（若相关）、第三方来源（若有）与已知限制。

Conventional Commit 风格推荐：`fix(input): ...`、`test(session): ...`、`docs: ...`、`chore(ci): ...`。

## License

提交到本仓库的贡献按仓库 `LICENSE` 与适用上游义务处理。若引入第三方代码，请同时补齐 provenance / NOTICE 信息。
