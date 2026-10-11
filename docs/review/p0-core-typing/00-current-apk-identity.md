# P0-R 現有 APK 身分與來源鑑識

本次只讀鑑識確認：凍結 v3 的產品 APK 是原 P1-D APK 的相同位元組，產品來源包含 P1-A／B／C／D。P1-E 提供獨立驗收工具，P1-G 修正驗收執行器；兩者不是凍結產品 APK 的新產品版本。新 P0-R 工作區以 `85711bc8c749d488923954e8f4c173a493bf27eb` 重用驗收基礎設施，產品基線則是 `75b93a3b68867fd6ec4c6adadd3e279d71e98104`。

此結論只涵蓋本機來源與 APK 靜態內容。本輪沒有讀取裝置已安裝 APK 的位元組，故 `INSTALLED_APK_MATCH=UNKNOWN`、`PHYSICAL_RUNTIME=NOT_TESTED`。版本號、檔名、工作區 HEAD、簽章相同均不能單獨證明已安裝版本。

## 本輪範圍與證據

- 日期：2026-10-10（每筆精確觀察時間見 JSON）。
- 工作區：`/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p0-core-typing-recovery`。
- 只讀取得 Git 身分／差異、實際 APK SHA256、ZIP entries、DEX class definitions、aapt 與 apksigner 結果、歷史封存內容。
- APK 鑑識子任務未執行 ADB、安裝、IME 切換、instrumentation、Gradle／native 建置、native bootstrap、push 或 Release 簽署。後續 P0-R 建置／測試另見 05、06、10；不能將本節只讀範圍解讀為整輪沒有建置。
- 舊工作區與歷史 exports 未改寫、清理或複製來源。新增內容限本工作區的鑑識文件與 `build/p0r-evidence/apk-forensics*.json`。

| 證據 | 內容 |
|---|---|
| [APK 鑑識原始 JSON](../../../build/p0r-evidence/apk-forensics.json) | 實際 17 個 APK 的路徑、大小、SHA256、aapt／apksigner argv／exit／stdout、每個 DEX／native／字典 entry SHA256、靜態差異 |
| [工作區清冊](../../../build/p0r-evidence/apk-forensics-worktrees.json) | 11 個 Git worktrees 的 HEAD、branch、git status、diff stat、既有 dirty files SHA256 |
| [來源與沿革核對](../../../build/p0r-evidence/apk-forensics-sources.json) | commit ancestry、88 個產品來源逐 byte 核對、P1-D 8 檔審查清冊、P1-G 99 檔保護清冊 |
| [exports／封存核對](../../../build/p0r-evidence/apk-forensics-exports.json) | AI Studio 歷史 export、歷史 source snapshots、C4 tar archive 193 檔與 Git blobs 比較 |

## 精確凍結 APK

```text
APK_PATH=/Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1g-runner-safety/build/p1-alpha-acceptance-v3-safety/app-debug.apk
APK_SIZE=29158832
APK_SHA256=b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9
PRODUCT_SOURCE_SHA=75b93a3b68867fd6ec4c6adadd3e279d71e98104
P0_WORKTREE_BASE_SHA=85711bc8c749d488923954e8f4c173a493bf27eb
APPLICATION_ID=io.github.juhjuhx.kuyin
VERSION_CODE=20001
VERSION_NAME=0.2.0-alpha.1
MIN_SDK=24
TARGET_SDK=36
COMPILE_SDK=36
DEBUGGABLE=true
ALLOW_BACKUP=false
SIGNATURE_VERIFY_EXIT=0
SIGNATURE_SCHEME_V2=true
DEBUG_CERTIFICATE_SHA256=46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94
INSTALLED_APK_MATCH=UNKNOWN
```

本輪使用 `/opt/homebrew/share/android-commandlinetools/build-tools/36.0.0/aapt`（`v0.2-13193326`）及同目錄 `apksigner`，JDK 為 `/opt/homebrew/opt/openjdk@21`。`aapt dump badging`、`dump permissions`、`dump xmltree … AndroidManifest.xml`、`apksigner verify --verbose --print-certs` 均 exit 0。

憑證 DN 為 `C=US, O=Android, CN=Android Debug`，RSA 2048 bit。這是有效 Debug 簽章，不代表 production signing。Merged manifest 僅 requests `io.github.juhjuhx.kuyin.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`；INTERNET／VIBRATE 不在此 APK 的 permissions dump。IME service 是 `com.example.ime.KuYinInputMethodService`，受 `android.permission.BIND_INPUT_METHOD` 保護。

## Native 與字典實際內容

四個 ABI 均含 `libchewing-jni.so` 及 `libandroidx.graphics.path.so`，共 8 個 native entries。下列 SHA256 是解壓後 entry bytes，與 APK 整檔 SHA256 不同。

| ABI | `libchewing-jni.so` bytes | SHA256 |
|---|---:|---|
| arm64-v8a | 1,380,328 | `d6ab2f4d64d8e62f1ea6078ca2e19fc6a9cde65d094fc23c849156aeb36c4d3f` |
| armeabi-v7a | 966,116 | `fc4a2da4f576301b2f133152880217a6d90a6cd27e317b548f68dd8a1d40923c` |
| x86 | 1,480,740 | `72acf097d06beb857847642c40e8b253bdf6479bf36b8d950319bb96be0aa5a8` |
| x86_64 | 1,561,792 | `e6977e1c38377cc1494217611c1e1c5259fe2a117bffe84860cead2b2cbf3b54` |

AndroidX 四個 `.so` 的完整大小／SHA256 亦保存在 APK 鑑識 JSON。

| 字典 entry | bytes | SHA256 |
|---|---:|---|
| `assets/libchewing/swkb.dat` | 154 | `0c7368141ca3c7bc25f00a1c427bbcf0a3b4777696bea0a7d5b7c73e39423c82` |
| `assets/libchewing/symbols.dat` | 5,052 | `ad505e1e4c02b47cdae258f5708aacda2532ee56bf5db0c9dd37c45c24f3d89a` |
| `assets/libchewing/tsi.dat` | 4,501,904 | `394417675ec3e9a7ab6f8c1e06345bbe9b47e84d5df14c79d9bef75788463b48` |
| `assets/libchewing/word.dat` | 283,930 | `3b5fad6954801c1c60f5025561030b237586e060cb519f8f3fc77afdd6651242` |

上述 assets 在本輪比對的所有歷史 APK 中皆相同；這只證明字典 bytes 相同，不證明 decoder、JNI 或 editor dispatch 行為相同。來源的固定 dependency refs 可另查 [UPSTREAM](../../UPSTREAM.md)：prebuilt `3587ba3355711f0aca50136e787719f6562676b8`、libchewing `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`。本輪未重建 native 以證明 upstream source-to-binary 等價。

## 產品提交與驗收工具的界線

| 切片 | 精確 commit | 產品內容 |
|---|---|---|
| P1-A | `a7a036dffd29ec942fc98f78f83cf8ce59d1e6d8` | native startup gate／owner |
| P1-B | `6af9476337c911557530e02f10a2d21fc6ca3a3b` | canonical 大千映射與 Compose rows |
| P1-C | `b63fddc821ebb8dd28af309711d5da6dbb8a76c6` | 非文字 editor composition／effective mode policy |
| A／B／C checkpoint | `870fc03cbfe8e5107b8ff8dc13478ac7754eec9f` | 累積 A／B／C 加交接文件 |
| P1-D 產品 | `75b93a3b68867fd6ec4c6adadd3e279d71e98104` | immutable Enter policy、service 與 UI wiring |
| P1-D 文件 | `94e91916d481959fb73e0adf37edb76764b26bed` | 驗證交接，不增加產品行為 |
| Alpha 準備 | `5c6bcad34c4048e40ce4e871e70e176c10195bcf` | 凍結既有產品 APK |
| P1-E fixture | `6595b68403232187eba4d9ec10c7f0689c2b5b61` | 獨立 acceptance-host／驗收矩陣／執行器 |
| P1-E 文件 | `4e70acdb2a7ee9751ee295171cc7ef6fc65b1f2f` | v2 驗收工具交接 |
| P1-G／P0-R base | `85711bc8c749d488923954e8f4c173a493bf27eb` | 執行器安全與 v3 交接 |

所有上述 commits 均經 `git merge-base --is-ancestor` 確認屬 P0-R base 的祖先。在 P0-R 實作開始前的來源身分核對中，`app/src/main`、`app/src/debug`、`app/src/androidTest` 與 `app/build.gradle.kts` 共 88 個 inherited tracked files：`75b93a3 → 85711bc` Git diff 空，當時工作副本亦逐 byte 相同；此繼承基線範圍未歸屬變更 **0**。不以整個 checkout HEAD 誤稱凍結產品來自 P1-E 或 P1-G。

來源證據串聯如下：

1. [P1-D build-results](../2026-10-04-p1d/final-green/build-results.json) 既有紀錄將原 `p1d-enter-policy/app/build/outputs/apk/debug/app-debug.apk` 綁定 `b223620…e39c9`。
2. [P1-D review-manifest](../2026-10-04-p1d/final-green/review-manifest.json) 的 8 個來源／測試 SHA256 本輪重讀 `75b93a3` Git blobs 均相符。
3. [Alpha 產品 APK 核對](../2026-10-07-p1-alpha/產品APK核對.json) 明列 `source_code_sha=75b93a3…98104` 及同一 APK hash。
4. 本輪實際讀取原 P1-D APK、凍結 v1、v2、v3，四者整檔 SHA256／大小完全相同。
5. [P1-G source-preservation](../2026-10-09-p1g/source-preservation.json) 的 99 個產品與 host 檔案，本輪與 P0-R 工作副本 SHA256 均相符。

這是 Git／審查／建置帳本與實際 artifact 的交叉證據，不是 APK 內嵌 source SHA 的保證，也沒有宣稱 bit-reproducible build。

## DEX 靜態 feature 核對

直接讀取 APK ZIP 中 DEX 的 `string_ids`、`type_ids`、`class_defs` 與 ULEB128 `class_data` method indices；只接受實際 class definitions，沒有把單純字串或 reference 視為實作存在。沒有安裝 JADX 或建置新 class files。

| 切片 | 凍結 APK 可確認的靜態 surface |
|---|---|
| P1-A | `NativeStartupGate.start`、`NativeChewingEngineOwner.ensureStarted/resetSession/close` 的類別與 methods 存在 |
| P1-B | `DachenKeyboardRows` 在 `classes10.dex` 定義，含 `getRows` |
| P1-C | `EditorPolicy` 含 `getAllowComposition/getAllowPersonalizedLearning/getForceAscii`；`EffectiveModePolicy.effectiveMode`、`NativeLearningPolicy.shouldEnableLearning` 定義存在 |
| P1-D | `EditorEnterPolicy` 與 `Companion.from`、`Behavior.InsertNewline`、`PerformStandardAction.getActionCode`、`PerformCustomAction.getActionId` 在 `classes13.dex` 實際定義 |
| 產品 wiring | `KuYinInputMethodService.updateEnterPolicy/handleEditorAction`、`ChewingEngineSession.dispatch/completeComposition` definitions 存在 |

上述證明 feature 被打包，沒有宣稱它在裝置上正確執行。P1-A／B／C 類別本來可能已在其他版本存在，故精確改動沿革仍以 commit／source bytes 為準。相較之下，歷史 C4 與 M4 APK 均未定義 P1-D `EditorEnterPolicy`，可直接排除它們與凍結 APK 位元組相同。

## 實際 APK 比較

下表 hash 縮寫僅供閱讀；完整 SHA256、檔案路徑、簽章及 entry 比較保存於原始 JSON。所有列均讀取現存檔案，沒有依檔名推定。

| 現存 artifact | bytes | SHA256 前 16 碼 | 與凍結 v3 的觀察 |
|---|---:|---|---|
| P1-D `app/build/outputs/apk/debug/app-debug.apk` | 29,158,832 | `b223620743831e03` | 整檔相同 |
| Alpha 凍結 v1 | 29,158,832 | `b223620743831e03` | 整檔相同 |
| P1-E 凍結 v2 | 29,158,832 | `b223620743831e03` | 整檔相同 |
| P1-G 凍結 v3 | 29,158,832 | `b223620743831e03` | 本輪指定 artifact |
| P1-E 現存重建輸出 | 29,158,832 | `b7b67be5da2e17f3` | 整檔不同；本輪涵蓋的 payload 只有四個 chewing JNI `.so` 不同 |
| P1-G 現存重建輸出 | 29,158,832 | `fad18178193ba866` | 整檔不同；本輪涵蓋的 payload 只有四個 chewing JNI `.so` 不同 |
| C4 clean baseline 輸出 | 29,158,832 | `87054013bde7d20d` | DEX／四個 chewing JNI `.so` 不同；無 P1-D policy definition |
| 2026-10-04 C4 source snapshot APK | 29,158,832 | `87054013bde7d20d` | 與現存 C4 clean APK 整檔相同；與凍結 v3 不同 |
| dirty C4 工作區輸出 | 29,142,136 | `cdf4a380e5737e08` | DEX／JNI 不同；來源不能僅以 HEAD 歸屬 |
| pre-C4 hardening 輸出 | 29,142,136 | `7aab1b348898e517` | DEX／JNI 不同 |
| M4 Debug 輸出 | 29,158,832 | `bad73f53a9714ee4` | DEX／JNI 不同；無 P1-D policy definition |
| M4 benchmark 輸出 | 28,338,753 | `68a4d8a58671aa16` | Manifest／DEX／JNI 不同 |
| P1-A 工作區累積 A／B／C 輸出 | 29,379,410 | `941dbce781f9ad84` | DEX／JNI 不同；無 P1-D policy definition |
| 主工作區輸出 | 30,537,915 | `a662be5e2384952a` | DEX／JNI 不同；無 P1-D policy definition |
| Desktop v0.2 `a91300a` 標籤 APK | 13,467,069 | `ce572fa897f9519b` | 舊 applicationId／簽章／DEX／JNI |
| Desktop M3 `96c7b8f` 標籤 APK | 13,479,357 | `5057bcd0017da14e` | 舊 applicationId／簽章／DEX／JNI |
| Desktop M3.1 `bbb7ffc` 標籤 APK | 13,479,357 | `433adefa67aa13ef` | 舊 applicationId／簽章／DEX／JNI |

現存 C4／M4／P1-D／P1-E／P1-G 皆標 `io.github.juhjuhx.kuyin`、`20001`、`0.2.0-alpha.1`；因此這三個欄位無法區別產品切片。Desktop 三個早期 APK 實際是 `com.example.androidkeyboard`、versionCode `3`、versionName `0.1.2-alpha`，其各自 Debug certificate SHA256 也不同；檔名中的 v0.2／commit 縮寫不是 Manifest 證據。

對 P1-E／P1-G 現存重建輸出，已比較所有 DEX、`AndroidManifest.xml`、`resources.arsc`、assets 與 native entries；DEX／Manifest／resources／四字典／四 AndroidX `.so` 與凍結 v3 均相同，四 chewing JNI `.so` 不同。未進一步證明該 native 差異僅是 debug paths，不能宣稱 machine-code／behavior 等價，也不能替換凍結 artifact。

## 工作區與未提交內容

下表為鑑識開始時的狀態。新 P0-R 文件與授權實作在本輪並行新增，該列不是完成後的 clean 宣稱。所有既有 dirty files 的精確路徑／SHA256 均列於工作區 JSON，未搬入 P0-R 產品來源。

| 工作區 | HEAD 前 7 碼 | 初始狀態／diff stat |
|---|---|---|
| 主工作區 | `fe1e96f` | `AGENTS.md` +85／-17；未追蹤 handoff |
| c4-clean-baseline | `3d86f75` | 3 文件 +5／-6；2 未追蹤文件，產品乾淨 |
| c4-compose-libchewing-switch | `e72acb3` | 5 產品檔 +20／-13；2 未追蹤 tests 與鑑識文件 |
| fix-pre-c4-production-hardening | `3f02976` | clean |
| m4-performance-baseline | `3d86f75` | `app/build.gradle.kts` +15；未追蹤 IME instrumentation 與 M4 文件 |
| p0-core-typing-recovery | `85711bc` | 未追蹤本輪 review docs；產品 diff 空 |
| p1-alpha-acceptance | `5c6bcad` | clean |
| p1-alpha-acceptance-fixture | `4e70acd` | clean |
| p1a-native-startup-hardening | `870fc03` | clean |
| p1d-enter-policy | `94e9191` | clean |
| p1g-runner-safety | `85711bc` | clean |

鑑識完成時，新 P0-R 工作區已有並行實作新增的 `EnglishLexicon.kt`、`EnglishWordSession.kt` 與 5 個 tests；這些是本輪 P0-R 工作內容，不屬凍結 v3 產品 APK。88 檔核對是實作前的繼承來源身分，後續程式差異應以 P0-R 實作／驗證文件另行判定。本鑑識員未改動那些程式檔。

另外，歷史版本快照列出的獨立 `/Users/huang/KU-Yin-C4-D2.5-repo` 現存且 HEAD=`34a758b20c0246b5af09bf1c087208581884ad5b`，git status／diff stat 空；指定 Debug APK 路徑不存在。它不在主庫的 11 個 worktrees 清單，不混入 P0-R lineage。

dirty C4 與 M4 的現有變更是歷史材料，不是本輪新來源，也沒有重新審核成可合併變更。若未來要搬入，須另做逐 hunk 歸屬與行為驗證；本輪停止該引入路徑，避免污染精確產品基線。

## 歷史 AI Studio exports 與 source snapshots

只按已知 Desktop／專案 top-level 名稱縮小範圍查找，未對 home 進行遞迴 glob。

- `/Users/huang/Desktop/KU-Yin-v0.2-FINAL/ku-yin-酷音鍵盤.zip` 存在：1,123,973 bytes，SHA256 `1c72fec44fdbb2b347c2d7a0a404aa5ca115fbe1585229ba18051baa2813ae35`。含 Android source 與 AI Studio 格式 metadata，沒有用來證明來源 commit 的 Git ref manifest。
- 同目錄 `local/` 有 83 個本輪列冊的非 build／非 `.env` 檔案；README 保留 AI Studio app link，metadata 包含 `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`。這是歷史 export provenance，不能據此宣稱現行 Android IME 有網路 typing path，也不能作為精確產品基線。
- `完整審查_2026-10-04/程式快照/` 保留 C4 source tar、C4 APK、C4 原有文件 patch、舊工作區原有 patch／未提交 tests、M4 草稿。
- `C4_已提交來源_3d86f75.tar.gz` 去除固定 archive prefix `KU-Yin-C4-3d86f75/` 後，193 個檔案內容均與完整 Git ref `3d86f7538c47dc929e25cdb0bd0755f8cc3bad60` 的相應 blobs 相同。這項比較來自內容而非 tar 檔名。
- 完整審查 ZIP 現存：28,848,115 bytes，SHA256 `9e1e2331d74be2954752ddf0829aa8be6e1c39b4b0167e1a104b8f73d94a3609`。封存清冊／SHA256 見 exports JSON；本輪只讀封存，未解壓來源到新工作區。

上述資料僅做沿革辨識與保留，不取代來源 `75b93a3`，也沒有把 UI export 的 metadata 當作現行 Android 行為。

## 裝置與驗證限制

前輪提供的 2026-10-09 ADB 裝置數 0 屬歷史時點，不能推論今日裝置數或已安裝 APK。本輪讀到 Desktop `2026-10-09-231127-P1H-盤點-daab34da/adb-inventory.json` 自己記錄的是 sandbox daemon 啟動受阻、exit 1；該單一檔案亦不能獨立重證「0 台裝置」。本輪完全未呼叫 ADB。

靜態 artifact／來源核對結果為 PASS；installed identity、device typing 與 native runtime 仍為 INCONCLUSIVE／NOT_TESTED。此文件不能當作核心中文輸入通過、physical acceptance 通過、M4 完成或 production Release ready。

LSP diagnostics tool／CLI 在本輪介面未提供；沒有聲稱已執行。此輪只新增 Markdown 與 JSON，以 JSON parse、文件連結、內容／來源 hash 重新核對及舊工作區保存檢查驗證；未為文件重跑產品建置。

如後續 device evidence 指向另一個 APK，先取得實際已安裝 bytes／hash 與 signer 身分，重新歸屬症狀；不能從本輪本機 APK 結論直接將實機現象歸因於 P1-D。若指定凍結 hash、原本應保存的舊來源或舊工作區保留清單不符，應停止以該 artifact 進行後續實驗；授權 P0-R 新程式變更則另行驗證。
