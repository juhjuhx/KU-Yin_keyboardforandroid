# KU-Yin 社群發布包｜2026-10-21

此資料夾是 **公開實驗性 Alpha 的分享工具包**，不是下一版正式 Release 的發行證明。

## 目前建議下載的最新公開版本

- 倉庫：https://github.com/juhjuhx/KU-Yin_keyboardforandroid
- 已公開 GitHub Pre-release：https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1
- APK：`KU-Yin-AIE-S1-candidate-browser-debug.apk`
- 由 GitHub Release API 核對的 SHA-256：`acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`
- 性質：Debug 簽章、Experimental Alpha；**不代表經過完整手機/OEM/無障礙/更新驗收**。

R3 的 `0.2.0-rc.1` APK 位於未合併的 [PR #20](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/pull/20) 工作鏈，**沒有 GitHub Release**。R3.1 的 [CI #39426302308](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39426302308) 顯示 H0 健康，但 H1 UI XML 擷取仍 INCONCLUSIVE；H2/App/Host/實機未執行。詳見 [Issue #19](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/19)。

## 包含內容

- [POSTS_READY.md](POSTS_READY.md)：Threads、Facebook、Instagram、Bilibili、LinkedIn、GitHub Discussion、X/Mastodon 等已填好真實連結的貼文。
- [ASSETS_AND_PRIVACY.md](ASSETS_AND_PRIVACY.md)：要實際拍攝的截圖分鏡、短影片腳本、比例、替代文字、匿名化檢查。
- [RELEASE_AND_SOCIAL_CHECKLIST.md](RELEASE_AND_SOCIAL_CHECKLIST.md)：安全下載與 SHA256、自查清單、發文時序、錯誤回報。
- [FAQ.md](FAQ.md)：可直接回覆留言的常見問題。

文案、素材規格及流程分別以 POSTS_READY、ASSETS_AND_PRIVACY、RELEASE_AND_SOCIAL_CHECKLIST 為唯一維護入口；舊 launch-kit 與 posting checklist 僅保留相容導航。另見 [作品集](PORTFOLIO_CASE_STUDY.md)、[圖片分類](media/README.md) 與 [工程門檻](../release/TODO_AND_GATES.md)。倉庫仍保留歷史 Pre-release，「最新」不代表「唯一」。

## 官方介紹句

**KU-Yin 酷音是一套以台灣大千注音為核心的離線 Android 開源輸入法，使用 Kotlin／Jetpack Compose 建構鍵盤介面，透過 JNI 整合固定版本的 libchewing，具備中文組字、候選瀏覽與離線英文補全。目前公開的是實驗性 Alpha，正式穩定版尚未推出。**

## 發文原則

1. 區分「已實作」「已由 JVM/CI 測試」「已由實機完整驗收」。不要將各分支結果拼成正式版 PASS。
2. 可說正式產品 Manifest 不請求 INTERNET；不能宣稱所有資料都不保存、也不能說已具備完整學習記錄清除。
3. 舊公開 Alpha 的基金會 QR 是裝飾／非標準碼；未合併 R3 分支才有已完成解碼測試的新 QR，手機實掃仍未驗。
4. 感謝 [libchewing](https://codeberg.org/chewing/libchewing) 及 [fcitx5-android/prebuilt](https://github.com/fcitx5-android/prebuilt)，不宣稱它們背書 KU-Yin。
5. 外部貼文使用官方 Release URL，不直接提供測試 worktree、CI 隨機簽章 APK、私人字典或資料庫檔案。
6. 截圖、動圖與影片素材**尚未在本包內產生**。取自實際 APK 運作或實際來源，不使用 AI 模擬畫面冒充真機結果。

此包是可發布文案與製作規格，不代表已代替使用者登入 Threads／FB／IG／Bilibili 發布。