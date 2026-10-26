# KU-Yin：從注音原型到可維護的Android Alpha

KU-Yin的目標是讓熟悉台灣大千注音的人，有一套離線、開源、來源可追溯的Android鍵盤。這份記錄整理可由程式與Git歷史支持的工程經歷，不替作者編造個人生活故事。

## 從原型辨認真正的產品來源

Google AI Studio與local donor提供早期設計／骨架；後續不是按檔名把舊APK當成果，而是核對Manifest、Dachen mapping、JNI、字庫與Git lineage。Compose成為產品介面，舊View shell保留為歷史參考。C4建立Native路徑，P0-R處理基本輸入與英文MVP；AIE-S1再加入候選瀏覽。

## 比按鍵畫面更難的邊界

組字、候選投影與Android Editor必須有明確owner。UI只呈現immutable snapshot並派命令；中文Session接libchewing，Service統一執行文字效果。scratch native context與live composition分開，候選以generation/selection identity限制過期點選。

Android回傳false不能推定對方完全沒寫字，因此不能盲目重送。Native ACK也不是Editor ACK，更不能由單元測試宣稱跨IPC exactly-once。這些限制與錯誤路徑都要留在測試及文件。

## 收束而不是一直加功能

R1–R3.8主要整理主線、工作樹、來源證據、Host observer與CI安全。獨立審查曾攔下不合原權限契約的修正；保留RED、拒絕方案與後續驗證，讓下一位維護者能知道什麼真的測過。

目前main已通過JVM／build／lint，但Emulator health／完整Runtime仍有缺口。公開的是Debug簽章Experimental Alpha，沒有把工程收束包裝成Stable。正式簽章、更新保留資料、學習清除、備份及完整Native授權材料還需要逐項驗收。

## 協作方式

不同AI Agent協助實作、來源追查、review與文件，採單一Writer與唯讀Reviewer，使用者保留產品、授權及發布決策。Agent不是法律作者或上游背書者。跨工具接續靠Git SHA、Task Ledger與原始收據，不靠讀完所有聊天記憶。

接下來進入Maintenance Mode：按可重現Issue處理穩定性與相容性。新排名、學習、觸控、符號或語音研究要另行批准，不能因上一輪編譯成功就自動開始。

[原始碼](https://github.com/juhjuhx/KU-Yin_keyboardforandroid) · [歷史來源](../HISTORY.md) · [架構](../ARCHITECTURE.md) · [目前門檻](../release/RELEASE_READINESS.md)
