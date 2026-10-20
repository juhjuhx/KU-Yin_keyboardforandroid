# KU-Yin｜各平台可直接發布文案（Experimental Alpha）

**素材來源日期：2026-10-21。發文前確認 [Release](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1) 仍是公開最新版。** 以下文案全部指向已公開 Alpha；不可把 R3 Debug 候選改稱 Stable。

## Threads／Facebook 共用短版（繁體中文）

我在做一套 Android 大千注音鍵盤：**KU-Yin 酷音** ⌨️

開源、離線，以 Kotlin／Jetpack Compose 做介面，透過 JNI 整合 libchewing。現在有注音組字、選字、候選橫向瀏覽／展開翻頁，以及離線英文補全。

已公開 **Experimental Alpha**，還不是穩定版；完整實機與不同手機相容性正在補驗。想找願意一起測試、回報問題的 Android／注音使用者。

GitHub／下載：https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1

感謝 libchewing 和其他開源社群的成果。測試請先用非敏感文字；有問題歡迎開 Issue。

#KUYin #開源

## Threads｜技術補充回覆（串文第二則）

為什麼不直接用純 Kotlin 注音引擎？因為我希望保留成熟的 libchewing 解碼和詞庫，再把 Android 的鍵盤生命週期、候選快照、防止過期選字、InputConnection 寫入等問題處理清楚。架構與測試也全部放在 GitHub；JVM PASS 不等於手機 PASS，這部分仍在補齊。專案：https://github.com/juhjuhx/KU-Yin_keyboardforandroid

## Threads／Facebook 共用長版（繁體中文）

【開源專案分享：KU-Yin 酷音 Android 大千注音鍵盤】⌨️

最近把自己在做的 Android 輸入法整理到能公開邀請測試的階段。這套專案叫 **KU-Yin 酷音**，目標是以台灣大千注音為核心，做一個可以持續維護、能明確追溯上游來源的開源鍵盤。

目前已有的功能包括：大千注音組字與候選選字、候選橫向捲動與展開／翻頁、基本英文 QWERTY 和離線字首補全，以及主題、字級與基本符號／Emoji。

技術上使用 Kotlin、Jetpack Compose、JNI、libchewing。鍵盤核心輸入不依賴網路，專案也保留了固定 Native 上游版本、原始碼與建置文件。

這裡先說清楚：**目前公開的是 Debug 簽章的 Experimental Alpha，不是已完整驗收的穩定版。** 不同手機、輸入欄位、橫直螢幕與更新保留資料仍在測試，所以建議先用備用裝置或非敏感文字試用，並不要因簽章錯誤而直接卸載、清資料。

📦 專案、下載及來源材料：
https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1

🐛 問題回報：
https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues

特別感謝 libchewing、Fcitx5 for Android 等自由軟體社群長期累積的技術。未來會先完成實機驗收、穩定性與發行工程，再逐步研究個人化候選、觸控修正等功能。

歡迎分享你平常使用的注音輸入法、Android 機型，以及你最在意的鍵盤體驗。

#KUYin #Android

## Instagram｜貼文說明（繁體中文）

KU-Yin 酷音｜一套正在開發中的 Android 大千注音鍵盤 ⌨️

用 Jetpack Compose 做 UI，整合 libchewing 處理中文輸入，具備候選瀏覽、離線英文補全與基本符號。把輸入法當成值得長期維護的開源產品慢慢完成。

🧪 現在開放 **Experimental Alpha** 測試，不是 Stable；不同裝置與完整輸入情境仍需驗收。

📍GitHub：github.com/juhjuhx/KU-Yin_keyboardforandroid
📦 下載：個人檔案連結可放 GitHub Releases 的 v0.2.0-aie.1

圖片若為示意／設計稿必須另外註明；真機畫面才能標示「實際使用畫面」。

#KUYin #大千注音

## Bilibili／B 站｜影片標題與簡體介紹

**标题：** 我在做一个开源 Android 注音键盘：KU-Yin 酷音｜libchewing + Jetpack Compose｜实验性 Alpha

**简介：**

KU-Yin 是我正在开发的开源 Android 大千注音输入法，使用 Kotlin / Jetpack Compose 构建键盘界面，通过 JNI 集成 libchewing 作为中文解码器。已实现注音组字、候选选择、横向浏览与展开翻页，以及本地英文前缀补全。

目前公开的是 **Experimental Alpha / Debug APK，不是稳定版**。R3 候选版本还在 Android IME 运行环境和真机验收阶段，暂不对外发布。视频如果展示实际操作，会注明设备、Android 版本和 APK 版本；如果只是代码/Compose Preview，也会清楚标示。

项目源码、安装包、校验值与上游致谢：
https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1

欢迎交流 Android IME、中文输入法、JNI/libchewing、Jetpack Compose 和开源维护。请勿在公开 Issue 上传真实输入内容或私人日志。

**标签：** Android、开源、输入法、注音、Kotlin、Jetpack Compose、libchewing、软件开发

## LinkedIn｜English technical portfolio

**KU-Yin: building an open-source Android Zhuyin keyboard.**

I have been developing KU-Yin, an offline-first Taiwanese Dachen Zhuyin input method for Android. It combines a Kotlin/Jetpack Compose interface with the libchewing native decoder through a C++ JNI layer.

The engineering work goes beyond the visual keyboard: IME lifecycle management, candidate pagination and snapshot identity, editor commit/acknowledgment handling, pinned native dependencies, source provenance, and a multi-layer test harness.

An **experimental debug-signed Alpha** is available on GitHub for early feedback. Full physical-device, accessibility, upgrade, and release-signing validation remain open, so this is not a production-stable launch announcement.

Source and Alpha release:
https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1

Thanks to the libchewing and broader open-source communities for the upstream foundations.

#AndroidDevelopment #Kotlin #JetpackCompose #OpenSource #JNI #InputMethod

## GitHub Discussion｜英文技術公告

**Title:** KU-Yin Android keyboard — experimental Alpha feedback welcome

KU-Yin is an open-source, offline-first Dachen Zhuyin input method for Android, using Jetpack Compose and the pinned libchewing decoder via JNI.

The current public build is [v0.2.0-aie.1 Experimental Alpha](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1). It includes native Zhuyin composition, paginated candidate browsing, offline English prefix completion, and basic symbols.

This build is debug-signed and **not production-stable**. Full device/OEM/accessibility and data-preserving upgrade checks are ongoing. Please test with synthetic, non-sensitive text only.

To report an issue, include: the exact APK version, device/OEM, Android version, type of editor, steps to reproduce, expected result, and actual result. Exclude personal typing content, private dictionaries, passwords, and unredacted logs.

Issues: https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues

## X／Mastodon｜短版

KU-Yin 酷音 ⌨️ 開源 Android 大千注音鍵盤。Kotlin + Jetpack Compose UI、JNI + libchewing 中文解碼；具候選翻頁與離線英文補全。已公開實驗性 Alpha，完整真機驗收仍在進行。源碼／測試版：https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1 #OpenSource #Android #Zhuyin

## 履歷／作品集一句話

KU-Yin 酷音｜獨立開發與維護中的離線 Android 大千注音開源輸入法；整合 Kotlin、Jetpack Compose、C++ JNI／libchewing、候選快照與 Editor ACK 管理，建立 Native 依賴固定、CI 與測試追溯流程，已公開 Experimental Alpha。

## IG 五張圖卡文字與 16:9 封面

1. KU-Yin 酷音｜Android 大千注音開源鍵盤。Experimental Alpha，非穩定版。
2. 注音組字與候選選字｜中文解碼使用 libchewing。無核實截圖時標示「功能介紹，非操作畫面」。
3. 候選瀏覽｜單列、展開與翻頁已實作；完整手機驗收仍待補齊。
4. 離線輸入｜Kotlin／Compose + JNI；英文使用本地字首補全，沒有雲端建議。
5. 一起測試｜GitHub 原始碼、下載與 Issues；只用合成文字回報，感謝上游社群。

16:9 封面共用第一張標題，副標為「實驗性 Alpha 開發紀錄」。版式方向待使用者確認，尚未製成圖片。比例及來源規格見 [素材規格](ASSETS_AND_PRIVACY.md)。

常見留言統一使用 [FAQ](FAQ.md)。短／長版可跨 Threads 與 Facebook 共用，避免不同人設與重複維護。本包未代登入任何社群帳號或發文。
