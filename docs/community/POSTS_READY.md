# KU-Yin 社群共用文案｜Experimental Alpha

這是可直接取用的文案，尚未代登入或發布私人社群。公開下載仍是[v0.2.0-aie.1](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1)，新main的CI產物不是新版Release。品牌圖與真實截圖各有來源標示。

## Threads／Facebook 共用短版

把一套 Android 大千注音鍵盤整理成開源專案：KU-Yin 酷音。

用 Kotlin／Compose 做介面，透過 JNI 整合 libchewing，已有注音組字、候選展開與翻頁、英文離線字首補全。現在可下載的是 Experimental Alpha，還不是穩定版，完整手機與不同 App 驗收仍在補。

這次主要整理來源、建置、文件和測試，沒有把新功能一次塞進去。想一起測試的話，請先用合成文字；有問題附機型、版本和重現步驟，別貼私人輸入。

原始碼／下載：
https://github.com/juhjuhx/KU-Yin_keyboardforandroid

## Threads／Facebook 共用長版

最近把 KU-Yin 酷音整理到可以持續公開維護的狀態。它是一套以台灣大千注音為核心的 Android 開源鍵盤，想處理的是日常組字、選字和跨 App 寫入的基本體驗。

中文採用 libchewing，介面是 Kotlin／Jetpack Compose，英文則用固定的本機詞表做字首補全。候選可橫向瀏覽、展開與翻頁；這些都有原始碼與分層測試，但不表示每一款手機都已驗收。

過程裡最花時間的，不只是把按鍵畫出來。候選翻頁後，舊的點選位置不能寫錯字；Native 組字與 Android 編輯器的確認也不是同一件事。後來把 Session、候選快照和 Editor effects 的責任分開，再用獨立審查、CI和來源收據逐項核對。

目前公開的是 Debug 簽章的 Experimental Alpha。新main完成了一輪平台契約、測試工具和CI安全維護，但完整Runtime、實機、正式簽章及保資料更新仍未閉合，所以沒有把工程綠燈當成新版穩定發行。

核心輸入在本機執行；這也不代表沒有個人學習資料，或已完成所有備份／清除政策。這些限制都放進文件，避免只把好看的測試數字放在首頁。

感謝 libchewing、Fcitx5 Android prebuilt、AOSP 和其他開源社群的累積。接下來先進入維護模式，依能重現的Issue處理問題。歡迎Android與注音使用者一起測試，分享機型、欄位類型、預期與實際結果。

專案／下載：https://github.com/juhjuhx/KU-Yin_keyboardforandroid
回報：https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues

## Instagram 圖說

KU-Yin 酷音，一套開源 Android 大千注音鍵盤。

注音組字與候選瀏覽使用 libchewing，介面是 Kotlin／Compose，英文提供離線字首補全。現在是 Experimental Alpha，完整手機驗收與正式更新流程仍待完成。

這組圖是品牌／技術介紹，不是操作截圖。目前尚無已核實操作畫面；取得核驗後才放入GitHub Gallery並逐張標明來源；不拿Preview冒充手機結果。

原始碼、下載、已知限制與Issues都在：
github.com/juhjuhx/KU-Yin_keyboardforandroid

#KUYin #開源 #大千注音

## 小紅書簡體版

把一个 Android 注音键盘整理成了开源项目：KU-Yin 酷音。

主要面向使用台湾大千注音的朋友。中文采用 libchewing，界面用 Kotlin / Jetpack Compose，支持候选展开、翻页和本地英文前缀补全。

目前公开的是 Experimental Alpha / Debug APK，不是稳定版，也没有宣称所有手机都能正常使用。最近的工作重点是来源、测试、CI和文档收束；完整真机、更新保留数据、备份与清除政策仍有待验证。

图卡是品牌和技术说明，不是手机操作截图。项目里的真实截图会单独标设备、APK和来源。

代码、下载、问题反馈：
https://github.com/juhjuhx/KU-Yin_keyboardforandroid

## Bilibili 開發紀錄

**标题：** 一个开源 Android 注音键盘怎样从原型走到可维护的 Alpha｜KU-Yin

**简介：**
KU-Yin 使用 Kotlin / Jetpack Compose、C++ JNI 和 libchewing，把中文组字、候选浏览与Android编辑器写入分层。英文是本机词表的前缀补全，不是云端AI。

这个开发记录会讲原型、Compose迁移、Native解码、候选快照，以及为什么“CI通过”不等于“手机验收通过”。当前公开的仍是 Experimental Alpha，完整Runtime和真机验收未完成。

如果展示真实操作，画面会标注设备、版本、APK SHA与来源。品牌图、代码图与测试渲染会另行标识。

项目与下载：https://github.com/juhjuhx/KU-Yin_keyboardforandroid

## 開源／Codex 社團技術介紹

分享一個 Android IME 工程案例：KU-Yin 酷音。Compose UI只派發命令與呈現候選快照，中文Session接JNI/libchewing，Service集中處理Editor effects與ACK。英文使用固定10,000詞離線prefix lookup。

AI Agent參與來源追查、實作、TDD與獨立審查；每個結論仍要對應Git/source、原始XML與測試層，不能把多個分支的歷史結果拼成一個PASS。現在main的JVM／build／lint已通過，Emulator health與完整Host Runtime仍有缺口；公開版是Experimental Alpha。

若對IME lifecycle、candidate identity、InputConnection、Native licensing或Agent接續有興趣，架構與維護記錄在repo。歡迎具體review和可重現Issue，不需要提供私人輸入內容。

https://github.com/juhjuhx/KU-Yin_keyboardforandroid

## 素材與使用方式

[五張圖卡／Story／16:9封面](media/social/README.md)共用同一視覺系統；主封面直接當第1張，不另複製。每張Alt／來源／SHA見social manifest。舊launch-kit和posting checklist只保留導航，不另維護第二套文案。

[FAQ](FAQ.md) · [素材與隱私](ASSETS_AND_PRIVACY.md) · [發文核對](RELEASE_AND_SOCIAL_CHECKLIST.md)
