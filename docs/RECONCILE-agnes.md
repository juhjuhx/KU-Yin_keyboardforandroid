# RECONCILE-agnes — 接管對賬書（先審後推的唯一真相源）

> 寫入：2026-09-09（Sisyphus 接管）。原則：**只收錄實字節驗證過的**；報告聲稱未驗證的一律標「待驗」；
> agnes 的 handoff/審查文檔降級為「線索」而非權威。本檔隨審計收斂持續增補。
> 狀態：§1–§2§4 已驗證；§3 待 app 代碼審計提煉回來後填寫。

## 0. 可信度分級

- ✅ 實字節：我方 `read`/`grep`/`git` 讀到的原始內容。
- 🔍 審計員聲稱：三條 audit lane 回報（已交叉抽查，未逐字全驗）。
- ⏳ 待驗：app 代碼審計全文提煉中；T11 自繪鍵盤規格還在跑。

## 1. 地面真相（倉庫與產物）

- 工作主體 = `D:\666\opencode\android-keyboard`（唯一權威副本；work/full 皆為舊快照，
  full 缺整棵 `cpp/`+`engines/`，所謂「完整備份」不成立）。
- Git：`master` 12 commits（`2a523c8` → `19f1d85`）；HANDOFF 的歷史快照停在 `89943f8`，
  落後 3 commits；Wave1→`ba2b4b2` 是 wave2-scope commit（錯配）；Wave4b「待提交」已過時
  （已提交 `27f5af0`）；「29 波次」無出處（最接近的是 29 項 TODO 計畫中的 5/29）。
- 應用是 **clean-room 重寫**（`ChewingInputMethodService` 等 12 個 Kt 檔），不是 fcitx5 fork ——
  與最初 plan（fork fcitx5-android）實質分叉，已接受為既定路線（回不去 fork，除非重來）。
- 衛生：`local.properties` 未被追蹤（僅路徑，無秘鑰）；`.gradle/`、`app/build/`、jniLibs、
  `*.apk` 均未追蹤；唯一追蹤異常是 `app/src/main/cpp/libchewing-src` 的**斷裂 gitlink**
  （mode 160000，無 `.gitmodules`，對象已消失；盤上另有一棵 Rust 樹被它遮住）。
- 真機證據（用戶截圖）：APK 可裝（19.39MB≈BUILD-SUCCESS 的 18.5MB），IME 以
  `KU-Yin (Chewing)` 註冊但**未勾選**；無桌面圖標；無可用設置頁。

## 2. 構建真相（審計已回，實字節交叉中）

- 本機環境：`java`=1.8（PATH）、`JAVA_HOME` 空；JDK17 存在（腳本硬編碼路徑）；
  NDK r27d 在 SDK 之外（`D:\666\android-ndk-r27d-windows`）；SDK 有 cmake/3.22.1、
  platforms 33/34，**無 ndk/ 子目錄**；`app/build.gradle.kts` 未聲明 `ndkVersion`。
- P0（必死）：裸調 `gradlew` 走 JDK8 → AGP 7.4.2 要求 JDK11+，配置期即死。
  只有經腳本設 `JAVA_HOME=JDK17` 才活。P0（次）：乾淨檢出無 `libchewing-src`、
  無 jniLibs → CMake FALSE 分支 → 缺 `chewing.h` 編譯死。
- CMakeLists（65 行）四宗罪：缺庫只 WARNING 不 FATAL（靜默產出運行時必崩的 `.so`，
  Android linker 允許未定義符號）；`LIBRARY_OUTPUT_DIRECTORY` 污染 `jniLibs`；
  cargo 註解 vs IMPORTED 現實矛盾；legacy 分支與 0.12.x CAPI JNI 永遠對不上。
- BUILD-SUCCESS 判定：**產物為真**（APK 18.49MB、四 `.a` 尺寸吻合），**路線成謎**
  （Rust 預編譯 vs HANDOFF 推 legacy C 0.10.x 對立；Gradle 7.6.4 實測 vs AGENTS 稱 8.5 誤記）。
- 腳本 sprawl：20 個安裝/構建腳本，6 套 sdkmanager 重複、盲目接受 license、
  `manual_install_sdk.py` 是 no-op、`libchewing_capi.a` 路徑名錯誤；`_run_elevated.vbs` 全盤不存在。

## 3. 代碼真相（app 代碼審計提煉 + 我方實字節複驗）

- JNI：REAL（32 符號對 header 全合；Kotlin externals 命名轉義正確）。
  但 header 是 Rust 0.11.0 而註解稱 0.12.x；`chewing_Init` 在 Rust CAPI 是 no-op。
- **H1（實錘）**：`KeyMapping.kt` 送 xkbcommon 碼（10–65）給只吃 ASCII 的
  `chewing_handle_Default`（`simple-select.c:38-42` 全是 `'x'` 形式；ChangeLog 明記
  Ignore non-printable）→ 注音鍵全被靜默丟棄。
- **H2（修正版實錘）**：原生是 bitmask（`IGNORE=1/COMMIT=2/BELL=4`，
  `public.rs:67-69`、`chewing.h:139-143`；用法見 `genkeystroke.c:259` 的 `&`），
  Kotlin 用 `!=` 比對且只定義了 IGNORE。注意：審計原文「vs native OK=0」不準確，
  此樹根本沒有 OK=0（已用實字節糾正審計本身）。修法：照抄
  `chewing_keystroke_CheckIgnore`／`chewing_commit_Check` 語義。
- **H3（實錘）**：`KeyMapping.kt` 號稱 standard.rs 實為許氏混大千：
  3/4 聲調互換、`ㄌ` 在 D(40) 與 X(53) 重複、`ㄫ`≠`ㄤ`、D/F/J/X/C/V 七鍵錯位。
  真大千：`3=ˇ 4=ˋ D=ㄎ F=ㄑ J=ㄨ ;=ㄤ X=ㄉ C=ㄊ V=ㄋ B=ㄌ`。
- **H4**：`chewing_cand_open` 從未被調用；service 在普通按鍵分支從不 `updateCandidates()`。
- OpenCC：STUB（直通）。測試：零（`src/test`、`androidTest` 皆不存在）。
- 修正合併策略：H1+H3 合為一張「注音→ASCII 真大千表」重寫；H2 照抄原生謂詞語義；
  H4 按需 open＋service 補刷新；同一 worker 一次做完（同文件，禁併發）。

## 4. R2a 已落地（接管後直改）

1. `AndroidManifest.xml`：`exported="true"`（圖標與設置入口）。
2. `input_method.xml`：單一 `zh_TW`/`keyboard` subtype。
3. `strings.xml`：Hsu「嘸蝦米」→「許氏」。
4. Service 空安全重構**因 live 併發編輯暫緩**（文件當時每分鐘在變；已保留守衛調用，
   文件可編譯；待審計全回後由 worker 持 fresh-read 重做）。
5. 協作規則新增：同一文件同時只能一 lane 持有（認領制），違者回滾。

## 6. R2b＋R2c 收官（2026-09-09 續）

- R2b：KeyMapping 真大千 ASCII 表、bitmask 謂詞、cand_open、service 刷新 —— 實字節驗收。
- BOM：6 檔全清（engines×5＋CandidateView），kotlinc 通過即最強證明。
- 重建：`assembleDebug` exit 0，APK 19,424,999B（`app/build/outputs/apk/debug/app-debug.apk`）。
  殘留 warning 僅 `AndroidChewingEngine.kt:50` 冗餘 else（順帶證明 R2b 碼已編入）。
- 待真機三驗：圖標出現、KU-Yin 可勾選、鍵盤面彈出。未過之前不算完。

## 7. R2e 重建（2026-09-09 續）

- `assembleDebug` exit 0（46 tasks，21 executed），APK 20,415,633B。
  `compileDebugKotlin` 跑過＝真設置頁＋R2b 已編入；唯一 warning 是我寫的
  `getPackageInfo` deprecated（非致命，後續遷 API33 flag 版）。
- 本 APK 含：圖標入口、單 subtype、H1–H4、真設置頁。待用戶裝機三驗。

## 8. 駁回存檔：REVIEW-2026-09-09 §2.5（2026-09-09 續）

- 該節以 xkbcommon 碼為尺去量 KeyMapping（`KEY_X(53)→ㄌ`、`KEY_C(54)→ㄏ`），
  並自稱此即 standard.rs —— 兩頭皆錯：R2b 之後本倉鍵碼語義已是 **ASCII**（xkbcommon
  典範作廢）；且其表 stream 本身 neither Dachen（`X→ㄉ C→ㄊ V→ㄋ`）nor Hsu，
  與 `simple-select.c` 及物理 QWERTY 位置皆對不上。
- 結論：§2.5 整節作廢，不作為修復依據；鍵位唯一準繩是 R2b ASCII 表＋物理鍵位置
  （`KeyMapping.kt` 頭註＋`KeyboardLayout.kt` 顯示行一致即算對）。
- 另：該報告多處為過期快照（BOM 表是 R2c 前狀態；MB/MiB 混寫），引用時先看日期。

## 5. 待決策（需用戶拍板，不代判）

- 包名 `com.example.*`＋通用名「Android Keyboard」→ 定稿（改包名＝重裝）。
- libchewing 路線：Rust 預編譯 `.a`（現狀）vs legacy C 0.10.x（HANDOFF 建議）vs Rust 0.13.x 自編。
  建議維持現狀預編譯（已驗證產物），除非審計翻出 link 缺口。
- OpenCC： stub 現狀 → HashMap 過渡（HANDOFF Q3）還是完整 C API（後期）。
- 構建統一入口：砍掉 20 腳本，留一個 `build.ps1`（定 JDK17＋NDK＋SDK 校驗前置）。
