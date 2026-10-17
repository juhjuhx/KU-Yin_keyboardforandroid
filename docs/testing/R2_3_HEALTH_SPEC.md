# R2.3 Emulator Health 規格

基線1ef5ba4；main63c9e4a、release／PR狀態不變。使用者2026-10-17 R2.3 prompt為授權規格；只改test infrastructure，無product Kotlin／JNI／pins／資料／簽章變更。

H0在任何KU-Yin/host APK安裝前唯讀採證：唯一disposable emulator＋qemu1＋user0、boot_completed1、PM/AM/WM/IMMS正常、system_server/SystemUI存在、沒有阻擋性system ANR／crash／未處置關鍵boot error。三態HEALTHY／UNHEALTHY／INCONCLUSIVE，後兩者皆非零exit且不得install。每個ADB命令固定serial、每次採證有界timeout。visible ANR為UNHEALTHY；hidden window不能假可見，有未處置ANR歷史則INCONCLUSIVE。空／錯誤／不完整觀測不當PASS。

H1僅H0 HEALTHY：在該fresh CI安裝exact existing acceptance-host APK，以固定caseId=R23-H1/inputType1/imeOptions6啟動其exported Activity；不選KU IME、不寫secure settings。WindowManager currentFocus須為host、UI XML editor focused=true、caseId相符、INPUT_CONNECTION_PRESENT=true，兩側window無blocking overlay，原selected defaultIME不變。H1不等於軟鍵或KU binding通過。舊debug ImeHostActivity exported=false，不以shell繞過。

H2僅同arm H0/H1皆HEALTHY後續原R2.2 pipeline；A原App5，B原App5→Host33/32matrix→B01，原assertion/timeout不改，兩arm同job四APK hashes。A health不過則B不啟動；任何AppFAIL阻其Host。default不改其他historical CI/physical runner。禁止dismiss ANR／清資料／自動重試。

唯一預定環境變更：在binding job補回R2原本的KVM udev grant；仍API33/default/x86_64/pixel7/cold/no-snapshot/同RAM CPU GPU設定。真actual版本/flags/boot時間由raw採證，不把預設當量測。R2.2 B before-install ANR window當時hidden，原文件可見敘述需勘誤；失敗時兩組才均visible。

保存raw command/exit/timeout、window/activity/serverproto、bounded whitelist logcat、PNG、runner/AVD/image/kernel/CPU/RAM/GPU/KVM版本與source hashes，禁止private typing／字典／全env dump。

平台校正：API33 current windows使用明確`window windows`，focus使用`window displays`，raw command/exit各自保留，combined observation註記derived_from。要求current inventory及boot/display footer、focus owner相符；`activity lastanr`必須明示no ANR since boot，未知／timeout皆非HEALTHY。PNG驗chunk/CRC/IEND，不推斷畫面語意。H1新增10秒condition deadline，不更改任何既有visibility timeout。一般shell preflight的R23 opt-in ADB reads亦有5秒host timeout。
