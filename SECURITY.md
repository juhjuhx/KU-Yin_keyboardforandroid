# Security Policy

KU-Yin 是输入法，可能接触密码、私人通信与其他高敏感文字。安全声明只覆盖当前源码与已验证证据。

## 当前控制

- Manifest 不申请 Android `INTERNET` permission。
- Android backup 明确关闭。
- libchewing system/user data 位于 app-private `noBackupFilesDir`。
- `EditorPolicy` 识别 password、FORCE_ASCII 与 `IME_FLAG_NO_PERSONALIZED_LEARNING`。
- native adapter 可关闭 libchewing personalized learning。
- 当前没有 clipboard feature，也未发现按键/preedit/candidate 明文日志路径；现有 `Log.e` 仅记录 generic JNI/init 错误。
- native dependency 使用固定 revision，而非 floating branch。

## 这些不代表什么

不代表 root / 已攻破设备上的数据仍受保护；user dictionary 当前没有额外应用层加密；尚无完整 OEM / Android version runtime security matrix；static libchewing archive 尚未完成源码重编等价 / 每文件 hash gate；production release signing 尚未建立。

详细审计见 `docs/SECURITY_AUDIT.md`。

## 漏洞报告

若 GitHub Security tab 提供 private vulnerability reporting，请优先使用私密 Security Advisory。不要在公开 Issue 贴出可直接利用的敏感 exploit、真实输入内容、私钥或用户数据。

本项目目前不承诺固定响应 SLA。
