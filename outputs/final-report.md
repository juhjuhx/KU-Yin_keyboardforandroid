# android-keyboard 狀態報告

## 已完成

- Wave 0: 骨架文檔 (committed)
- Wave 1: 解碼規格 (untracked)
- Wave 2: Android 工程骨架 (untracked)

## 待處理

- git commit 所有變更
- submodule 接入 upstream
- libchewing JNI 綁定

## Git 限制

.git 目錄有 DENY ACE，需手動修復：
icacls D:\666\opencode\android-keyboard\.git /reset /T /C
