# R2.3 Emulator 環境基線與勘誤

範圍：main63c9e4a、R2.2 final1ef5ba4、runtime3115002；只改test infrastructure。此表從獨立審查的33個primary sources重核hash（33/33），不是fresh runtime。

| 項目 | R2成功 run38761725033 | R2.2失敗 run39005467151 | 解讀 |
|---|---|---|---|
| Emulator | 37.2.12.0 build16428233 | 同版 | 版本相同不代表完整環境相同 |
| Guest image | API33/default/x86_64；TE1A.220922.034/10940250 | 同fingerprint | 舊證據缺image package revision／image bytes hash |
| Profile／GPU | pixel7／swiftshader_indirect | 同設定 | 不是實測host GPU同一 |
| CPU／guest RAM | 2 cores／2048MB startup、MemTotal2013968kB | 同startup；B guest meminfo未知 | host資源壓力未知 |
| Acceleration | KVM grant；resolved disable=false；command無-accel off | binding job缺grant；permission denied；resolved disable=true；actual -accel off／TCG | 最強已觀測差異；R2 direct backend active未保存 |
| Boot metric | 38649ms | A527552ms／B505894ms | boot_completed不等於健康 |
| Snapshot | fresh create、no-snapshot | 兩個fresh AVD、no-snapshot | 不等於初始userdata hash已知 |

完整runner OS/ImageVersion、SDK及raw line references見外部 `R23_ENVIRONMENT_INDEPENDENT_REVIEW.json`；UNKNOWN不能補成預設。新run會保存allowlisted host OS/CPU/RAM、emulator/accel-check、AVD config、image source.properties/package.xml hashes、guest kernel/CPU/RAM/SurfaceFlinger、bootID與uptime。

## R2.2 勘誤

A before-install-window:195 的SystemUI ANR確實visible；B同檔:170的window當時 `isVisible=false`／shown=false。B仍有安裝前ANR事件，至before-app／失敗才可見。舊R2.2文件／CURRENT_STATE的「兩組安裝前均可見」不成立。本輪只校正當前入口，保留舊工區與raw歷史。離線新parser確認A visible、B hidden，見外部07-historical-health-classification.json；不將此當新H0執行。

## 有限假說與控制

缺KVM權限可能造成或放大boot／服務延遲；尚未證明它是ANR唯一或充分原因。新job唯一刻意變更的emulator環境設定為補回原R2 KVM grant，仍API33/default/x86_64/pixel7、相同CPU/RAM/GPU/snapshot設定。runner/image實際版本若漂移須明記，不稱exact old/new單因果證明。

新H0/H1觀測與共同host預裝亦會改變後續binding時序，所以H2是受控安裝路徑對照，不可隔空宣稱修復原remote問題。每arm只執行一次；A health不過則B不啟動。

## 官方契約

[Android acceleration](https://developer.android.com/studio/run/emulator-acceleration)：Linux KVM權限與accel-check用於確認可用性；不單靠workflow名稱推定。 [dumpsys](https://developer.android.com/tools/dumpsys)：指定service及timeout。 [AOSP API33 WMS](https://github.com/aosp-mirror/platform_frameworks_base/blob/android-13.0.0_r1/services/core/java/com/android/server/wm/WindowManagerService.java)：windows明確current dump、displays提供focus；default dump可包含lastANR。只參照介面，沒有複製平台實作。

## 本輪唯一次觀測

CI39047679642／source5ef8a979，Ubuntu24.04.5、ImageVersion20261012.334.1、host4vCPU/15989MiB；guest API33/default/x86_64 revision2（相同10940250fingerprint）、AVD pixel7/2cores/2048MB；Emulator37.2.12/build16428233、platform-tools37.0.1。KVM12 installed/usable、可讀寫/dev/kvm，actual qemu無-accel off；boot38428ms。GPU實測GLES Google SwiftShader4.0.0.1，guest kernel5.15.41 android13-8。raw environment.json有實際版本與source.properties/package.xml/config.ini hashes，沒有把它們說成完整system image檔案hash。

未觀測SystemUI ANR，lastanr無開機後ANR；H0卻因第一次currentFocus=null保留INCONCLUSIVE，final取樣Launcher恢復。snapshot有Launcher選單，既有runner startup keyevent82可能相關，但未做因果A/B，不能判定為唯一根因。新host OS／資源／probe也不同，不能稱KVM對ANR的單變量充分證明。

helper `emulator -version` exit127缺libpulse（非headless launcher不能啟動）：實際headless emulator已boot，版本由CI launch與source.properties核實。這個diagnostic缺口保留，不安裝額外host套件或重跑洗綠。
