# R3 實機驗收與更新狀態

**NOT_TESTED；未安裝任何實體手機，沒有清除資料或切換使用者IME。** 本文是待owner授權後的計畫，不能當執行結果。仍須使用既有P1-G serial固定、state snapshot／restore及user/subtype fail-closed策略；若裝置不符合安全profile，停止，不弱化runner。

## 安裝前必核

- 只提供產品 `KU-Yin-0.2.0-rc.1-R3-debug.apk`；另外三份是test-only helper／instrumentation，不供一般使用者安裝。
- appID `io.github.juhjuhx.kuyin`；versionCode20004／0.2.0-rc.1；SHA256 `c171a3455acb1cb4a3b16ca0eb65e483620c35a224a308f398ad65c1867f0416`；Debug certificate `46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94`。
- 核對owner／serial／OS／OEM／user、已安裝包及signer、原輸入法／subtype與可恢復鍵盤。不從ADB連線推定同意，不執行uninstall／pm clear／真learning reset。
- 相同cert＋較高version只是必要靜態條件；若signature/downgrade錯誤，停止保存錯誤，不用刪資料繞過。

## 合成文字與觀測

依既有30-case矩陣逐案記expected／actual／text delta／ACK／action count／limitations，分開observable與full result。實機須包含：41鍵大千、有／無聲調「我」、連續「我現在」、候選展開／前後頁／非首屏選字、選字後續輸入、stale拒絕、英文hel→hello、Backspace／Space／Enter／SEND、password/nontext/TYPE_NULL、不同editor切換、橫直向／大字級／高度、TalkBack、四QR相機scan及更新後資料保留。

不得以畫面文字代替真editor commit delta，也不得為測試讀取或刪除私人詞彙。B nativecontext／C learning observer、C04真nullIC及D17 oneway Binder限制仍沿用原ADR；JVM／截圖不能補成PASS。

檢查完成需驗證same device/user與原IME／enabled/subtype恢復。無durable restore proof不報clean PASS。正式手機計畫與安裝需要owner明確授權；本輪未取得／未執行。
