# T25: JNI Stub Mode — 說明與啟用步驟

> 2026-09-08 | 當前狀態：stub 模式運行
> 所有 ChewingEngine 方法目前為 no-op；IMS 行為等同普通英文鍵盤

---

## 當前行為

| 操作 | 當前行為 | 激活後行為 |
|------|---------|-----------|
| 按注音鍵 | 直接提交字元 | libchewing 解碼為注音符號 |
| 按空白鍵 | 提交空格 | 提交當前拼音組合 |
| 按刪除鍵 | 刪除一字元 | libchewing backspace |
| 點擊候選 | 提交候選字 | 提交 + OpenCC 轉換 |
| 簡繁切換 | 無效果 | s2tw/tw2s 轉換 |

## 激活 JNI 的三个步驟

1. **安裝 NDK**：Android Studio > SDK Manager > SDK Tools > 勾選 NDK (Side by side) r25+
2. **克隆 libchewing 源碼**：見 docs/JNI-INTEGRATION.md §2
3. **取消 CMakeLists.txt 註解**：見 docs/JNI-INTEGRATION.md §3

## Stub 模式的優點

- 可正常編譯和安裝 APK（無 native 依賴）
- UI 層級功能完整測試（佈局、動畫、滑動切頁）
- 便於 CI/CD（無需 NDK 環境即可 build）
- 作為回歸測試基準（激活 JNI 前後對比）

---