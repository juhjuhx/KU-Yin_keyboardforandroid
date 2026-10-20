# KU-Yin 公開 Alpha：社群發布套件（2026-10-20）

> **內容定位：公開既有實驗性 Alpha 與開源工程紀錄，不宣告新 RC／Stable 發行。** 已核對 GitHub main `63c9e4ab8ad7e277013acb5d7cce99228ec1f827` 與公開 `v0.2.0-aie.1`。R3 PR #20／R3.1 分支仍在驗證；R3.1 H1 INCONCLUSIVE、H2／App／Host／實機未執行，不可宣稱新版已上線或全功能驗收通過。

## 單一官方連結（發布時重新開啟確認）

- Repository: https://github.com/juhjuhx/KU-Yin_keyboardforandroid
- **目前實際可下載**的 Experimental Alpha: https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1
- PR #20（未合併的 RC 工程候選）: https://github.com/juhjuhx/KU-Yin_keyboardforandroid/pull/20
- Release gate / outstanding blockers: https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/19
- Native upstream: https://codeberg.org/chewing/libchewing

**下載物限制**：`v0.2.0-aie.1` 為 Debug-signed experimental Alpha，不能保證所有手機／Editor 工作，安裝或更新若遇簽章不符，**不可為了測試而卸載或清除字典**。R3 `0.2.0-rc.1` 是尚未發布的 Debug candidate，不提供 RC 下載網址。任何界面擷圖／操作展示必須來自真正裝置或可標明的 Emulator，勿以 mockup 冒充實際運行畫面。

---

## Threads｜主文 A：公開專案（短版）

最近在做一個 Android 開源注音鍵盤：**KU-Yin 酷音**。

它使用台灣大千注音、Jetpack Compose 介面，中文解碼接 libchewing；也做了候選展開／翻頁、離線英文補全。目標是把注音輸入法做成可維護、可持續改進的開源專案。

目前開放的是**實驗性 Alpha**，完整真機相容性與正式發布驗收還在進行，歡迎看程式碼、提供回饋。

GitHub：https://github.com/juhjuhx/KU-Yin_keyboardforandroid

#Android #注音輸入法 #OpenSource

## Threads｜主文 B：開發者技術向

做 Android 輸入法最難的不只是畫出 41 鍵，而是輸入欄位的生命週期、選字後能否正確提交，以及候選頁面會不會選到過期的項目。

KU-Yin 用 Compose UI、Kotlin session、JNI/libchewing，並用候選快照身分和 Editor ACK 保護輸入狀態。Debug Alpha 已公開；目前仍在補 Emulator／實機驗收，還不算 Stable。

原始碼：https://github.com/juhjuhx/KU-Yin_keyboardforandroid

#Kotlin #JetpackCompose #libchewing #開源

## Facebook｜完整開發故事

這陣子我在做一個自己的 Android 開源鍵盤，叫做 **KU-Yin（酷音）**。

最初是想要一個更適合台灣大千注音操作、又可以自己持續修改的輸入法。做著做著發現，真正困難的不是把鍵盤畫在螢幕上，而是 Android 輸入法在不同 App 之間切換時，如何可靠地保留組字、候選與文字提交的狀態。

現在使用的是 Kotlin + Jetpack Compose 介面，中文核心採用開源的 libchewing，另外有離線英文詞表、候選列、展開候選與翻頁操作。我也建立了一套測試流程，試著把程式碼的正確性、Android 模擬器的健康狀態與真正手機的驗收分開處理，避免把「編譯成功」誤認為「可以安心天天用」。

**目前已公開 Experimental Alpha，尚不是正式穩定版。** 真機全面驗收、正式簽章及更新時的資料保護仍在收束。這也是我想公開記錄開發過程的原因：把哪些做好、哪些還沒做好，說清楚。

原始碼與問題回報：
https://github.com/juhjuhx/KU-Yin_keyboardforandroid

目前可下載的 Alpha：
https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1

也謝謝 libchewing、Android 開源社群及其他鍵盤專案提供的技術基礎。這是獨立的開源開發專案，沒有得到上述上游官方背書。

如果你有用 Android 注音鍵盤的習慣，也歡迎分享最在意哪些操作細節。

#注音輸入法 #Android開發 #Kotlin #JetpackCompose #開源軟體

## Instagram｜貼文文案（4:5 多圖）

**KU-Yin 酷音｜把台灣大千注音做成自己的開源 Android 鍵盤。**

🧩 Kotlin + Jetpack Compose
⌨️ 大千 41 鍵／候選展開與翻頁
📖 libchewing 中文解碼
🔒 本機輸入／離線英文補全

目前是實驗性 Alpha，不是正式穩定版；正在驗證不同 Android 裝置、輸入欄位與更新資料安全。專案原始碼及 Alpha 連結請見個人檔案連結，或搜尋「KU-Yin_keyboardforandroid」。

畫面請搭配**實際真機／標示來源的 Emulator 擷圖**，不要把設計稿標示為正式功能。

#注音 #大千注音 #AndroidKeyboard #開源 #JetpackCompose

## Bilibili／B站｜简体中文介绍文案

**标题：** 我做了一个开源 Android 注音输入法：KU-Yin 酷音｜Jetpack Compose + libchewing

这是我正在开发的 Android 注音键盘 **KU-Yin（酷音）**。它以台湾大千注音布局为基础，使用 Kotlin / Jetpack Compose 构建键盘界面，中文解码整合开源 libchewing，同时包含候选词横向浏览、展开分页和离线英文补全。

输入法开发不仅要解决布局，更要考虑 Android InputMethodService 的生命周期、输入框切换、候选状态与文字提交。项目采用 Session 状态管理、候选快照标识和 Editor ACK 相关防护。

当前 GitHub 上公开的是 **实验性 Alpha（Debug APK）**；完整真机兼容性、正式签名与更新验证仍未完成，不要把它理解为稳定正式版。欢迎查看代码、参与讨论或提交可复现的问题报告。

源码：https://github.com/juhjuhx/KU-Yin_keyboardforandroid
当前 Alpha：https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1

标签：Android开发、输入法、开源、Jetpack Compose、Kotlin、注音、libchewing

## LinkedIn／求職作品集公開短文

我正在開發 **KU-Yin 酷音**，一套以台灣大千注音為核心的離線 Android 開源輸入法。

技術組合為 Kotlin、Jetpack Compose、JNI／libchewing、Android InputMethodService，以及 JVM、Android Instrumentation 與 GitHub Actions。除了鍵盤畫面，更著重於 session ownership、candidate snapshot identity、InputConnection 寫入確認、原生依賴來源追溯及分層測試證據。

目前已公開實驗性 Alpha，正式 Release Candidate 尚在進行 Android Runtime、實機、簽章與資料安全驗收。我持續記錄程式碼、測試結果及未解風險，避免將 CI Build PASS 誤認為產品驗收通過。

GitHub：https://github.com/juhjuhx/KU-Yin_keyboardforandroid

#AndroidDevelopment #Kotlin #OpenSource #SoftwareEngineering

## X／Mastodon 簡訊

KU-Yin 酷音：我正在做一套離線 Android 大千注音開源鍵盤，採 Kotlin／Compose + JNI/libchewing，支援候選翻頁與英文補全。Experimental Alpha 已公開，完整真機驗收還在做。原始碼：https://github.com/juhjuhx/KU-Yin_keyboardforandroid #OpenSource

---

## 短影片 45–60 秒腳本（只有取得實際操作影像才能發布）

| 時段 | 畫面素材（不得偽造實機） | 旁白／字幕 |
|---|---|---|
| 00–05s | 真實鍵盤畫面；顯示 Alpha 小字 | 「我正在做一套 Android 開源注音鍵盤，KU-Yin。」 |
| 05–15s | 實測大千鍵盤與合成測試字詞 | 「鍵位用台灣大千注音，畫面用 Jetpack Compose。」 |
| 15–25s | 真實候選列／展開候選操作；若未錄到則略過 | 「中文解碼接 libchewing，候選可以瀏覽與翻頁。」 |
| 25–37s | IDE 中的架構程式碼、README、單元測試 | 「最花時間的是 Session、選字身分與 Android Editor 的文字提交。」 |
| 37–48s | GitHub Actions、Release 頁；顯示 NO-GO/Alpha | 「目前仍在完整驗收，公開下載的是實驗性 Alpha。」 |
| 48–60s | Repo URL 與 CTA | 「程式碼公開，歡迎追蹤、審查和回報問題。」 |

**畫面注意**：若目前沒有可供公開的真實手機示範，可改成純程式碼／架構＋Release 頁介紹影片；絕不可拿 Compose Preview 或捏造聊天介面冒充功能演示。

## 多圖內容建議（建立圖片前的素材來源要求）

1. **封面**：「KU-Yin 酷音｜開源 Android 大千注音鍵盤」＋真實截圖或純文字設計。
2. **核心**：大千 41 鍵（必須是當前實際版本截圖；不曝真實輸入內容）。
3. **候選**：候選列／展開／翻頁（沒有真實驗證影片時註明功能畫面為開發中）。
4. **架構**：Android IME → ComposeDecoderSession → JNI/libchewing → InputConnection（可用圖表，不能當 App 畫面）。
5. **開源致謝**：libchewing、AOSP、AndroidX、fcitx5-android/prebuilt；分清正式依賴與研究參考。
6. **最後一頁**：Experimental Alpha、GitHub URL、已知限制與參與方式。

## 每個渠道最後檢查

- Threads／Facebook：貼上 Repo 及目前 Alpha release 的完整 URL；先檢查連結實際可開啟。
- Instagram：Caption 連結通常不可直接點擊；使用 Profile Link，附 Repo QR 必須可解碼後再用。
- B站：簡中字幕、實際操作出處、源碼與下載條件；避免「国产」「全平台」「AI 鍵盤」等不實宣稱。
- LinkedIn／履歷：個人完成的程式碼／審查工作與 Agent 協助要如實區分；不要聲稱全數程式均為自己手寫。
- 任何渠道：禁止「正式版已上線」「全機型支援」「Whisper 已支援」「已經通過完整實機驗收」「從不處理使用者任何資料」。
- 發布前應再核對 GitHub SHA、Release 文件與照片，避免在貼文完成後產品狀態已改變。

> 本文件是可複製發布的文字與腳本；沒有聲稱任何社群平台已經貼文。
