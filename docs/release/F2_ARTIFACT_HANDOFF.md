# F2 原簽章 TEST_ONLY 產物交接

|項目|固定證據|
|---|---|
|來源|`41fd130ccdd4a505622c8292f9d16c250ce96b03`，base main57fbe7f|
|Package|`io.github.juhjuhx.kuyin`，min24／target36，四ABI|
|版本|20005／`0.2.0-f2.1-test`，避開Draft PR20的20004|
|APK SHA256|`8f67e381193c7284b04906e24ff14a736adb7c8cc90d0b8a8b0df2131bdbb10d`|
|Signer SHA256|`46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94`，同公開Alpha|
|散布|本機TEST_ONLY；不是RC／Stable／新Experimental Release|

本機新鮮驗證：Python129 PASS、12 contracts PASS；Gradle `--no-build-cache --rerun-tasks` 共177/177 tasks實際執行，App267 PASS／10 native SKIP、Host57 PASS，App/Host Debug及instrumentation編譯成功；Lint App0errors/76warnings、Host0/6。這些只證明所執行的層，不代替裝置驗收。獨立Reviewer直接核對APK／原簽章、XML與source tar837/837 Git blobs，裁定PASS_SCOPED。

版本metadata改動使舊Roborazzi來源雜湊失效；保留RED後實際重跑既有record task，PNG完全相同，再更新manifest來源資訊。它仍是JVM測試渲染，不是操作截圖；沒有弱化assertion。

## 本機重現與界線

先讀[DEVELOPMENT](../DEVELOPMENT.md)，使用隔離工區與固定來源；native pin仍為prebuilt3587ba3355711f0aca50136e787719f6562676b8／libchewing a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c。原key僅在owner本機使用；其他貢獻者自己的Debug key不能更新既有公開Alpha。不要把keystore、密碼或使用者資料傳給CI／Reviewer。

```sh
git checkout --detach 41fd130ccdd4a505622c8292f9d16c250ce96b03
python3 -m unittest discover -s scripts/tests -v
./gradlew :app:testDebugUnitTest :acceptance-host:testDebugUnitTest :app:assembleDebug
aapt dump badging app/build/outputs/apk/debug/app-debug.apk
apksigner verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk
shasum -a 256 app/build/outputs/apk/debug/app-debug.apk
```

上列checkout僅適用獨立乾淨工區，不能覆寫個人dirty checkout。工具鏈、native bootstrap、來源與簽章環境都影響產物bytes；固定source不是任意電腦都會產出同SHA的承諾。

## 仍需直接證據的門檻

較高code／同cert是更新必要條件，實際PackageManager正常更新與synthetic prefs/userdict/Room保留須另驗。Emulator結果必須標Emulator；實機、QR手機掃描、D2D/Room ACK、完整license/relink/SBOM及owner加密備份保管仍各自有Gate。新native `.so`由固定來源重建，摘要可能不同於舊CI包，不能因此推定完整relink等價或所有ABI Runtime通過。

只保留原[v0.2.0-aie.1公開Alpha](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1)下載入口與八份資產。後續動態結果見[Issue19](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/issues/19)，本文件不授權發布或安裝。

## F2 Runtime 處置

SDK image 佈署在最後一次有界重試解壓時觸及15 GiB保留門檻，被資源Guard中止；沒有建立AVD、安裝產品或執行H0/H1/H2、App/Host Runtime。這是環境佈署阻擋，不是新的產品失敗，也不是Runtime PASS。不再盲目重試。

候選[PR38](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/pull/38) exact source的[CI40520082438](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/40520082438) Build與blocking Lint成功，Runtime job明確SKIPPED。CI自有Debug key產物不能替代本機原簽章候選。完整資料保留更新及實機驗收仍未執行。
