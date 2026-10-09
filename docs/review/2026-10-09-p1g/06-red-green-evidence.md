# Fresh RED/GREEN 與建置證據

精確command/exit/time/failed method/assertion evidence：red-green-ledger.json。原runner24測baseline exit0。F01/F02/F03/P01/P02均先有assertion regression RED，無syntax/import錯誤當RED。F03原IO02/04/05觸發位置修正見04，採真正IO03/06原unsafe失敗證據。

最新fake runner全量：`python3 -B -m unittest discover -s scripts/tests -p 'test_p*.py' -v`，97 tests / 0 failures / exit0，protocol-review-green。mutation executor皆FakeAndroid/isolated doubles；獨立review更以全域subprocess禁止器重跑，未實際呼叫ADB。

Python static：`python3 scripts/verify.py --static`，10 contracts PASS / exit0。
Native bootstrap：先受sandbox DNS限制 exit128，未當PASS；必要只讀固定SHA網路存取後 `KUYIN_DEPS_CACHE=/private/tmp/kuyin-p1g-native-20261009 bash scripts/bootstrap_native_deps.sh` fresh exit0。pin/ABI不改。

Fresh Gradle：JDK21/SDK36；`:acceptance-host:testDebugUnitTest :acceptance-host:assembleDebug :acceptance-host:assembleDebugAndroidTest :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:assembleDebug --no-build-cache --console=plain` exit0。app40類190測、host3類37測，0failure/error/skip。第一次受sandbox socket限制exit1，授權必要local socket後通過。`:app:assembleRelease` exit1；packageRelease缺既有my-upload-key.jks，compile/lint之前通過，沒有簽章/發布。

最終runner：payload-schema-green 101/101 exit0。獨立同101/101、另37個negative checks，0 external subprocess/ADB。四凍結APKs重新用SDK36 aapt/apksigner核package/signer，皆exit0；signer46488e99…99da94。完整gate metadata在build-evidence.json。

新build輸出有不同SHA／host test size，僅作build gate，不聲稱byte-reproducible，也未拿來替換frozen4。lsp_diagnostics工具不存在；以Python實際import/tests與Android compiler gates驗證。所有新build僅驗證，不替代包內四凍結APK。runtime NOT_TESTED；production release NOT_READY。

V3解壓fresh驗證：37 entries CRC與SHA256SUMS全通過；101測無skip/error/failure、global subprocess/Popen禁器外部calls=0、四原APK hash核對PASS。portable-verification.json保存確切結果；無任何device命令。
