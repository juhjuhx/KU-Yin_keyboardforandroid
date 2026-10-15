# R2 UiAutomator 測試依賴修復

## 目的與界線

調查 Host observation 的 `QUERY_REGISTER` 失敗。這是 test infrastructure，不是 KU-Yin decoder／UI 修正；historical ea5 App visibility完整因果仍 `UNRESOLVED`；same3fc的新首次visibility失敗已重現且有獨立debug前置修正，見[R2 lifecycle](R2_ANDROID_IME_LIFECYCLE_TRACE.md)，PR10 HOLD、physical acceptance OPEN。

## 直接來源

- `ExternalUiObservation.captureOnce` 用 `LinkedHashMap<UiObject2, Int>` 登記 query／parent／child handle；hashCode 也是 live AccessibilityNodeInfo refresh。
- 38524582991／9f6d2e8：10案 BODY、CLEANUP 均 REGISTER stale，Host15PASS17SKIP。
- 38529968625／74193b2：真實 ZH06→ZH01 均 QUERY_REGISTER stale；132／130 是每案總量，並非單一 deadline 的 retry 次數。沒有證明 children traversal 是來源。
- UiAutomator2.3.0 的已解析 AAR bytecode、2.4.0 Google Maven source jar 與 POM 存於外部驗證目錄。查核來源而非抄入上游程式碼。

## 官方修正與授權

[AndroidX 官方 release notes](https://developer.android.com/jetpack/androidx/releases/test-uiautomator) 列出 I38ea1 修正 stale `UiObject2.hashCode/toString`、I3be25 週期性 accessibility cache invalidation；stable2.4.0於2026-07-01發布。官方 changelog 版本範圍終點 `ec9022d156846f24e22bec3e475d4206d6f321ea`，不冒稱其等於 Maven binary Git SHA。

精確 Maven `androidx.test.uiautomator:uiautomator:2.4.0`，POM Apache-2.0；source jar SHA256 `be710c5b295357f7e0deb652f1aa192b4fa6f2245af361743b402ce88ab8ec21`。不採用額外 GPL code 或 platform reflection。

2.4 hashCode 仍會嘗試 refresh，遇 stale 才使用 cached-node hash；不能稱為完全無 IPC。UiDevice 每2秒核對 service flags 並呼叫 setServiceInfo，使 accessibility cache 更新；不是增加本專案 read deadline。平台 UiAutomation.clearCache 是 API34 起，不能假裝 API33 有此公開 API。

POM direct dependencies：uiautomator-shell2.4.0、tracing1.1.0、annotation1.8.1、Kotlin stdlib2.1.20、jspecify1.0.0、test core1.7.0、JUnit4.13.2。實際 resolved graph：core原先已1.7.0、monitor仍1.8.0、Kotlin仍2.2.10；annotation變1.8.1，新增shell／shell-android2.4與jspecify1.0。

## RED → 最小修正 → GREEN

1. 真 ActivityScenario 的 editor 可見時建立 map，關閉 editor 後 getter 必須 stale；既有 map 查找不得拋出 stale，也不能把失效 getter 當 fresh UI。
2. 原2.3.0、API33 emulator run38561946857／source8c7d318：App5PASS，infra regression1FAIL；既存map查找實際StaleObjectException，kt33 assertion有效RED，getter仍stale的前置assertion已通過。
3. 僅 `acceptance-host` 的 androidTestImplementation 使用 UiAutomator alias。已只更新 catalog scalar，主程式依賴／native／signer 不變。
4. 先同一 API33 identity GREEN，再 ZH06→ZH01 實際 soft-key／editor，最後 full App／Host；不得以有限 probe 代表全套 acceptance。

新 infra regression 不發 P1 case envelope：當前 full Host33個JUnit identities = 原32 case methods＋1 regression，case envelopes仍32。frozen P1-E26 identity manifest／APK不變。

## CI 保護與本地證據

workflow guard 保護 main/Host production source、Gradle build scripts、全部 gradle 目錄、LICENSE、NOTICE；只從 catalog comparison 移除已核對的 versions.uiautomator scalar，實際bootstrap script亦保護；其餘 parsed data 必須相同，版本僅接受2.3.0／2.4.0。實際 isolated Git fixture 執行 workflow 程式：38有效1 assertion RED、39六測GREEN；原測試僅覆蓋gradle內檔案，reviewer再指出真native pin位於bootstrap script的guard缺口，46七測1 assertion RED後加入實際bootstrap路徑，47七測全部GREEN。拒絕其他dependency、product／Host source、未審查版本與真native bootstrap變更。

33b instrumentation compile PASS；35b有限模式13PASS；36b current inventory32!=33有效RED後明確加入精確新identity；37全量Python123PASS。36首次 import error不作RED；35 concurrent fake timeout保留，不延長timeout。

## 裁定

目前 `IDENTITY_RUNTIME_RED_GREEN_CONFIRMED`。只確認既存map identity不再拋stale；不得宣稱完整Host、historical visibility或physical acceptance已修復。完整Host與freeze-preservation independent receipt須另記。

## 更新後本地驗證

42 instrumentation真正重新compile PASS；Host unit當時UP-TO-DATE不當fresh證據。41b／42因重複Gradle task只取到App runtime＋Host AndroidTest graph，42c／42b單獨補足baseline／current Host runtime；App及Host production graph前後完全相同。首次空parser結果保留為invalid，不作證據。44 Python129PASS，43 contracts10PASS；45 --rerun-tasks真正執行App273與Host107，原始XML另存。GREEN runtime38563163760／3fc2c89：App5PASS、相同infra regression1PASS；181source hashes一致。getter仍stale前置assertion保留。有限ZH06→ZH01與完整Host尚待結果。

49最終Python130PASS（含7個真guard回歸）；45 App273／Host107無failure/error/skip；48四APK assemble PASS，signer46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94。全source gate不改產品／Host main／實際bootstrap pin；新檔是test infrastructure、read-only evidence與文檔。
