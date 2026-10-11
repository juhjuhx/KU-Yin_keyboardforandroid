# P1-E 建置與 RED／GREEN 證據

## 環境與紀律

JDK：Homebrew OpenJDK 21.0.12.1；JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home。
Gradle 9.3.1／AGP 9.1.1／compileSdk 36.1／minSdk 24。所有 Gradle 指令序列執行，--no-build-cache --console=plain；未安裝、執行任何 APK。
本機未提供可驗收裝置，RUNTIME=NOT_TESTED。沒有 lsp_diagnostics 工具；使用 Kotlin／Android 編譯、JVM 及 Python 合約檢查，不冒稱 LSP 已執行。

## RED

指令：`JAVA_HOME=… ./gradlew :acceptance-host:testDebugUnitTest --no-build-cache --console=plain`。
初次 `host-initial-compile.log` 為 shared Kotlin source-set 配置錯誤，不算 RED。
依 [AGP 官方 built-in Kotlin 文件](https://developer.android.com/build/migrate-to-built-in-kotlin) 改用 AndroidSourceSet.kotlin.srcDir，未改測試斷言。

真正 RED：exit 1；2 類／28 測／25 failures／0 errors／0 skipped。
- FixtureLogicTest：22 測／19 失敗。
- MatrixDefinitionTest：6 測／6 失敗。
- falseActionStillCountsAndDoesNotInsertText：action count 預期 1，實際 0。
- newlineCountComesFromActualText：實際文字含 3 換行，counter 回 0。
- parsesAllPrimitiveExtras：預期 D11／確認／73／false，實際仍為 defaults。

證據：host-red.log、host-red.json、host-red-xml/；XML 於 GREEN 前複製保存。

## GREEN 與目前 gates

純邏輯 GREEN exit 0；完成真 EditorInfo／EditText 邊界後重新執行 host unit＋Debug build，exit 0。
Host JVM：2 類／28 測／0 failures／0 errors／0 skipped。證據 host-green.log、host-build.log、host-green.json、host-green-xml/。

Python repository contracts：`python3 scripts/verify.py --static`，10 個合約，exit 0；contracts.log。
Native bootstrap：`KUYIN_DEPS_CACHE=/private/tmp/kuyin-p1e-native-20261007 bash scripts/bootstrap_native_deps.sh`，exit 0；bootstrap.log。四 ABI 與字典仍固定既有 SHA，無 pin／ABI 變動。

產品 fresh gate：`:app:assembleDebugAndroidTest :app:testDebugUnitTest :app:assembleDebug`，exit 0；app-verification.log。
App JVM：40 類／190 測／0 failures／0 errors／0 skipped；app-green.json、app-green-xml/。
既有 instrumentation 仍為 4 類／5 個 @Test，只編譯與打包成功，未執行。

必須的 Release gate：`:app:assembleRelease`，exit 1，packageRelease 因既有 `my-upload-key.jks` 不存在被阻擋；app-release.log。沒有建立／讀取私鑰、簽署或發佈 Release；此限制與凍結 Alpha 的歷史結果一致。
Host instrumentation 與最終 artifact hashes 待整合完成後填入。

## 核准系統中斷

第一個 GREEN 指令未執行：自動核准審查因服務用量限制無法完成，不是判定操作不安全；使用者回覆「繼續」後重新提交同一 require_escalated 指令，核准後執行並取得 exit 0。未繞過核准。

## 新編譯產品與凍結證據的差異

新 app-debug.apk 仍 29,158,832 bytes，SHA256=b7b67be5da2e17f3ee815e254215f02ac5bf45944a26eeef22d467c21ca143c5。
它只用於 fresh build gate；v2 使用原 b2236207…e39c9 bytes，不取代 v1。
逐 ZIP entry 比較：DEX、resources、manifest、字典完全相同，只有四 ABI 的 libchewing-jni.so 不同。
以既有 NDK llvm-objcopy 對暫存副本移除 .note.gnu.build-id 後，四 ABI 都逐 byte 相等；原差異僅 19／20 bytes build-ID。
此檢查沒有修改 APK 或 native pin；證據 fresh-vs-frozen-apk.json、native-build-id-comparison.json。它不是其他版本 native rebuild equivalence 或 runtime 證明。

## 裝置執行器純邏輯與 scope 修正

Runner 起初 22 個 pure／fake-subprocess tests exit0；沒有執行真 ADB。
獨立 reviewer 發現 B 類 observable PASS 可誤升 full acceptance PASS，新增回歸後 RED：23 tests、B01／B02／B03 三個 subcase failures、exit1。
補 explicit 寬度／方向、native context/reset、整句語義 scope limits 後 GREEN：23 tests、0 failures、exit0。
證據 runner-unit.log（明示 OFFLINE）、runner-scope-red.log、runner-scope-green.log。
這是本輪測試基礎設施問題與修正，不是產品 runtime failure／PASS。

## 最終 host 整合

Cleanup priority 回歸：`:acceptance-host:testDebugUnitTest --tests '*CaseFailurePolicyTest'`，RED 9 測／2 failures／exit1；cleanup-policy-red.log／cleanup-policy-red-xml/。
修正 test-only mergeCaseFailure 與共享 classifier 後：host 全量 3 類／37 測／0 failures／0 errors／0 skipped，exit0；host-final-green.log／host-final-green.json／host-final-green-xml/。
Host androidTest：3 類／26 @Test，D18 同時涵蓋三布局 SEND 與 NO_ENTER_ACTION newline；最終 `:acceptance-host:assembleDebugAndroidTest` exit0，host-final-androidtest.log。這只是 COMPILE_READY，未安裝或執行。
非 poll Stale 例外不再額外降成 assumption；bounded poll 內仍重查 selector。

Runner select 同時 primary／restore 失敗的診斷回歸：RED 24 測／1 failure／exit1 → GREEN 24 測／0 failure／exit0，runner-select-red.log／runner-select-green.log。主因與還原錯誤存 select-result.json，不吞原錯誤。
Script compile／--help exit0；script-verification.json。最後 docs／repo contracts 仍10/10 exit0，contracts-final.log。

## 最終 artifact 與保存核對

四 APK 的 merged manifest、applicationId／instrumentation target 與 apksigner verify 均 exit0，詳 artifact-verification.json 與各 *.manifest.xml／*.signer.txt。
四者使用同一有效 Debug certificate SHA256=46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94。
Host main merged manifest 無 permission、testOnly=true、allowBackup=false、helper exported=true；APK 無 .so、產品 service／Compose／libchewing DEX 參照。兩個 test APK 的 REORDER_TASKS 不屬於產品功能。

核對 app main／debug／androidTest 的87個既有 tracked檔案：與base逐byte相同，product-source-protection.json。
原8工區 HEAD／status／既有dirty檔SHA完全相同；v1兩APK＋ZIP三binary hash不變，preservation-final.json。
本輪指定2026-10-07路徑不改名；最後封裝於2026-10-08（工具UTC時鐘），沒有回填或偽造Git作者／時間。
