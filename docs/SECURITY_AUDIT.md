# Security, Privacy, Performance and Data-Leak Audit

审计基线：2026-09-10 consolidation。范围为当前仓库静态代码、Manifest、JNI/native boundary、storage、CI/upstream flow 与已有 emulator evidence；不构成第三方安全认证。

## Findings summary

| Area | Current evidence | Risk / follow-up |
|---|---|---|
| Network exfiltration | Manifest 未申请 `INTERNET`; code search 未发现网络输入 endpoint | 低于一般云端 IME，但未来新增网络依赖必须重新审计 |
| Plaintext logging | 未发现按键/preedit/candidate 明文 logging；`AndroidChewingEngine` 仅有 generic JNI/init `Log.e` | 保持 error log 不含输入内容 |
| Clipboard | 当前未发现 `ClipboardManager` 实现 | 若未来新增必须 opt-in、生命周期/敏感字段隔离 |
| User dictionary | `noBackupFilesDir/libchewing/user/userdict.dat` | app-private/no-backup，但未额外加密；root/设备攻破不在保护范围 |
| Android backup | Manifest `allowBackup=false` | 已落实 |
| Sensitive editor | `EditorPolicy` 识别 text/web/visible/number password、FORCE_ASCII、NO_PERSONALIZED_LEARNING | 仍需扩大实体设备 runtime matrix |
| Personalized learning | Kotlin → JNI → `chewing_set_autoLearn` | 有静态与 emulator native evidence |
| JNI memory lifecycle | `GetStringUTFChars` 成对 release；native context 显式 `chewing_delete` | 继续用 instrumentation/ASAN 类方法做 future hardening |
| Native supply chain | prebuilt 固定 immutable commit | 尚缺逐文件 hash manifest与 source rebuild equivalence；中等供应链债务 |
| Release signing | 未建立 production signing | 不能把 unsigned Release APK 当正式安装包 |
| Platform age | target/compile API 33 | 2026 publishing/maintenance debt，应独立升级 |
| Performance | View/Canvas、本地 decoder、无网络 round-trip | 尚无正式 latency/memory benchmark，不能声称具体性能数字 |

## Data flow

```text
User touch
  → KeyboardView
  → session / editor policy
  → ChewingEngine
  → JNI
  → libchewing
  → preedit / candidates / commit
  → InputConnection
```

当前流程不需要云端服务。用户词典由 libchewing 在 app-private no-backup 路径维护。

## Sensitive editor controls

`EditorPolicy` 对密码变化型、`IME_FLAG_FORCE_ASCII` 和 `IME_FLAG_NO_PERSONALIZED_LEARNING` 进行统一决策。敏感字段不使用 Chewing candidates/composition；no-learning 会传到 native autoLearn control。此设计减少不必要的候选/学习暴露，但仍须在不同 OEM 输入框行为上持续验证。

## Supply-chain observations

`bootstrap_native_deps.sh` 固定 `fcitx5-android/prebuilt` commit `3587ba3355711f0aca50136e787719f6562676b8` 与其记录的 libchewing source `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`。固定 revision 比 floating branch 更可审计，但 static archive 本身尚未与本仓库维护的 SHA-256 manifest/可重复源码构建比较。

官方 libchewing 已迁移 Codeberg；watcher 只负责提醒变化，不会自动替换 native input。

## LGPL distribution note

当前 native flow 使用 static `libchewing_capi.a` 构成 JNI library。仓库公开源码、build script 与 exact upstream revision 有助于满足可审计与源码取得需求，但 LGPL v2.1 对静态链接/可重新链接的具体发行义务应由实际发行者确认。本审计不提供法律结论。

## Performance observations

输入路径完全本地且对象边界相对短；Canvas/View 适合高频键盘渲染。不过当前没有基准测试证据支持“低于 X ms”“低内存”等量化声明。后续可增加 key-to-commit latency、candidate generation、cold JNI init、memory/PSS 与 rotation/recreate benchmark。

## Release gate recommendation

在稳定版前至少完成 production signing、API modernize、实体设备/OEM matrix、native provenance hashes，以及对敏感 editor 的 runtime regression。alpha 可以继续发布，但 release notes 必须准确区分已验证范围与未验证范围。
