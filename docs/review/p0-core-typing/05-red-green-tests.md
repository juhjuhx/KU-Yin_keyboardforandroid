# P0-R RED／GREEN 證據

2026-10-10，隔離工區 `fix/p0-core-typing-recovery`。相對 evidence path 均在本工區 `build/p0r-evidence/`；未提交二進位／原始建置輸出。歷史 P1 綠燈沒有取代本輪使用者失敗報告。

## 真正的 RED

| 問題 | 失敗／原因 | 保存的證據 |
|---|---|---|
| 原 native adapter | `ji` 無候選；連打尾音被候選查詢清掉；第二次 commit 重送「我」 | `production-adapter-red.json`、`production-adapter-red.log` |
| 選字→editor、identity | 3 測 3 敗：候選無 ordered commit、EditText 空白、重複 label 選錯 index | `editor-red.log`、`editor-red-xml/` |
| 英文候選 | `hell` 未出現可點選的 `hello` | `english-ui-red.log`、`english-red-xml/` |
| editor ACK／composing | 6 測 4 敗：composition、自身 selection、失敗送出尾段保存等 | `editor-recovery-red.log`、`recovery-red-xml/` |
| 審查：未完成輸入 | 4 測 3 敗：unavailable candidate 被 flush；非法尾音後標點／切語言丟輸入 | `review-regressions-red.log`、`review-red-xml/` |
| 外部移除 span | 8 測 1 敗：游標不變但 composing span 移除，下一鍵重複前綴 | `span-removal-red.log`、`span-red-xml/` |
| 反覆新增／退格 | 真 JNI 100 次 pending key/backspace 後 replay action 數 203，預期 3 | `history-red-command.log`、`history-red/` |
| editor 自有 suggestions | 3 測 2 敗：NO_SUGGESTIONS、AUTO_COMPLETE 仍顯示英文候選 | `english-policy-red.log`、`english-policy-red-xml/` |
| JNI supplementary 漢字 | 真 libchewing 加入固定 `𠮷/ㄐㄧˊ`，production candidate 回傳 `ð` 而非 `𠮷` | `unicode-red-command.log`、`unicode-red/production-adapter-unicode-red.log` |

英文真 asset 測試另有 17 測 1 敗：`hel` 的 `hello` 排第 7，原 limit 5 不足；最多 8 個後 17／17 通過。首次 class 尚不存在的編譯失敗只是 scaffold，沒有當成有效 RED。

## 最新 GREEN

串行 Gradle 命令，exit 0（`JAVA_HOME` 為本機 JDK21、`ANDROID_HOME` 為既有 SDK）：

```sh
KUYIN_NATIVE_HOST_DIR="$PWD/build/p0r-evidence/unicode-final-green" \
KUYIN_NATIVE_HOST_DATA="$PWD/app/src/main/assets/libchewing" \
./gradlew :app:testDebugUnitTest :acceptance-host:testDebugUnitTest \
  :acceptance-host:assembleDebug :acceptance-host:assembleDebugAndroidTest \
  :app:assembleDebug :app:assembleDebugAndroidTest :app:compileReleaseKotlin \
  --console=plain
```

App **49 類／229 測，0 failure、0 error、0 skipped**；Host **3 類／37 測，0／0／0**。其中 `NativeTypingEditorIntegrationTest` 5 案載入 pinned Rust libchewing＋production JNI，不是 mock decoder。一般未提供 host native 環境的 CI 會明確 skip 這 5 案，不能冒充此 opt-in gate。

最後交付輪（Unicode及設定頁版本修正後）保存 `final-delivery-build.log`，exit 0，48 秒；`final-test-summary.json`、`final-app-xml/`、`final-acceptance-host-xml/` 已更新。`compileReleaseKotlin` 僅驗證 release source 編譯，不是 signed release APK。

```sh
python3 -m unittest discover -s scripts/tests -v
python3 scripts/verify.py --static
```

各 exit 0：Python **104 測**（runner 101＋英文 converter 3）；contracts **10／10**。保存 `final-python-tests.log`、`final-contracts.log`。最初曾因修改既有 AcceptanceDriver 觸發 frozen-source hash guard；已還原該檔，P0 helper 移到新測試檔，沒有改弱 guard／manifest。

Native bootstrap 本輪 exit 0，見 `bootstrap.log`。C++ `-Wall -Wextra -Werror`、Kotlin host compiler、四 ABI Android Debug link 已通過；pins／ABIs 不變。

Native 最終命令：`python3 scripts/p0r_native_host_probe.py --source-dir /private/tmp/kuyin-p0r-native-20261010/libchewing --prebuilt-dir /private/tmp/kuyin-p1g-native-20261009/fcitx5-android-prebuilt --jdk-home /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home --output-dir build/p0r-evidence/unicode-final-green --phase unicode-final-green`，exit 0。Final manifest 對應 adapter `60cb45cd…086c18`、JNI `9e2760c1…819076`。固定 Unicode fixture 逐一核對 candidate enumeration／by-index／buffer／commit 為 `𠮷`，並重跑核心三序列、3-prefix replay equivalence、長句、invalid tail 與 persistent learning。Fixture 不編入產品 APK。

Device 新增 6 方法只編譯，**NOT_EXECUTED**。既有 app 5／host 26 也未實機執行。Fresh ADB 為 0 裝置；Robolectric EditText／macOS 真 JNI 不能稱為 physical-device acceptance。
