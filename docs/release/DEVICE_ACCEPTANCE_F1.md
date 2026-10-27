# F1 受控手機驗收與截圖

狀態：實機 NOT_TESTED；這份手冊不是測試收據。只使用合成文字。先取得指定 serial 與 APK 的裝置授權；保留可用的備援鍵盤。

## 1. 唯讀確認

在有 Android SDK 的電腦上，明確填入 serial 和待驗 APK；不要直接使用最新 CI 包更新公開 Alpha（目前憑證不同）。

```sh
adb devices -l
adb -s SERIAL shell getprop ro.build.version.release
adb -s SERIAL shell getprop ro.product.cpu.abi
adb -s SERIAL shell am get-current-user
adb -s SERIAL shell cat /proc/sys/kernel/random/boot_id
shasum -a 256 /absolute/path/candidate.apk
apksigner verify --verbose --print-certs /absolute/path/candidate.apk
aapt dump badging /absolute/path/candidate.apk
adb -s SERIAL shell pm path io.github.juhjuhx.kuyin
adb -s SERIAL shell dumpsys package io.github.juhjuhx.kuyin
adb -s SERIAL shell settings --user USER_ID get secure default_input_method
adb -s SERIAL shell settings --user USER_ID get secure selected_input_method_subtype
adb -s SERIAL shell ime list -s
```

將回覆保存在本機受保護 evidence 目錄。只分享必要包／版本／憑證、合成案例結果；不要公開完整系統日誌、通知、字典或私人內容。若可讀 installed base.apk，單獨核其SHA；只有package metadata時明確標示非bytes proof。

## 2. 安裝／更新人工 Gate

確認 package、舊/新 cert、較高versionCode、來源SHA、備援IME、資料保留計畫及可恢復範圍後，才由裝置操作者批准正常更新：

```sh
adb -s SERIAL install -r /absolute/path/candidate.apk
```

保留精確退出碼及PackageManager回覆。失敗立即停止，不加 `-d`、不uninstall、不clear、不root讀字庫。Android一般不能直接將較低版本重新裝回，原APK保留不代表可無損降版；需要獨立復原方案。

啟用/切換IME由使用者透過系統設定完成，先寫下原IME/subtype，不默默更動。測後恢復原IME與subtype並核對。

## 3. 最低案例

|ID|操作|直接證據|
|---|---|---|
|ZH1|ㄨㄛ、ㄨㄛˇ，各選「我」|preedit、候選、真editor精確文字與提交次數|
|ZH2|有/無聲調「我現在」連續組字|未完成輸入保留、最終我現在、無重複commit|
|CA1|展開/收合/上下頁、非首屏選字|同generation identity、正確文字、收合不掉組字|
|CA2|組字更新後舊候選tap|舊identity不提交；不以畫面相同推定|
|ED1|Space/Enter/Send、刪除、清組字|editor action ID/count與text delta，不用硬體Enter替代|
|EN1|中文↔英文、hel→hello、Space/Backspace|completion及分隔符恰好一次|
|PR1|NUMBER/PHONE/DATETIME/password|欄位/模式/可觀察學習限制分開；內部未觀測留INCONCLUSIVE|
|LC1|跨editor、切IME再回來、旋轉|focus/組字生命週期、無跨editor舊commit|
|AX1|大字級、TalkBack、觸控|可見標籤與實際操作；逐項PASS/FAIL/未測|
|UP1|Alpha→較高版本、原key更新|合成設定/測試資料保留，真PackageManager成功；無卸載|

TYPE_NULL必須保留真實null InputConnection；不可造假。只有截圖不能證明ACK或exactly-once；公開Alpha無觀察介面時，缺失子契約標INCONCLUSIVE。

舊版QR已知不是標準QR；勿宣稱可掃描。未合併QR修正不在此包中，用基金會官方HTTPS連結。

## 4. 真實Gallery與回報

在合成測試editor拍攝：大千41鍵、候選列、展開grid、英文補全、設定頁。先清除通知與私人內容，原始PNG保留；只裁切/去識別，不重繪鍵盤。每張記錄source commit、APK SHA、version、serial的本機代號、model/API、capture time/method、原圖SHA、privacy review。

```sh
adb -s SERIAL exec-out screencap -p > synthetic-keyboard.png
shasum -a 256 synthetic-keyboard.png
```

回報模板：case ID / APK SHA / source / Android / field type / steps / expected / actual text delta / action count / ACK evidence or missing / PASS-FAIL-INCONCLUSIVE / artifact path。不要附真實訊息、密碼或userdict。
