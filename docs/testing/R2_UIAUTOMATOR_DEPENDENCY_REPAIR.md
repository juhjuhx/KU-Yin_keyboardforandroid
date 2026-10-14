# R2 UiAutomator 測試依賴修復

## 目的與界線

調查 Host observation 的 `QUERY_REGISTER` 失敗。這是 test infrastructure，不是 KU-Yin decoder／UI 修正；historical App 首次 visibility failure 仍 `UNRESOLVED_NOT_REPRODUCED`，PR10 HOLD、physical acceptance OPEN。

## 直接來源

- `ExternalUiObservation.captureOnce` 用 `LinkedHashMap<UiObject2, Int>` 登記 query／parent／child handle；hashCode 也是 live AccessibilityNodeInfo refresh。
- 38524582991／9f6d2e8：10案 BODY、CLEANUP 均 REGISTER stale，Host15PASS17SKIP。
- 38529968625／74193b2：真實 ZH06→ZH01 均 QUERY_REGISTER stale；132／130 是每案總量，並非單一 deadline 的 retry 次數。沒有證明 children traversal 是來源。
- UiAutomator2.3.0 的已解析 AAR bytecode、2.4.0 Google Maven source jar 與 POM 存於外部驗證目錄。查核來源而非抄入上游程式碼。

## 官方修正與授權

[AndroidX 官方 release notes](https://developer.android.com/jetpack/androidx/releases/test-uiautomator) 列出 I38ea1 修正 stale `UiObject2.hashCode/toString`、I3be25 週期性 accessibility cache invalidation；stable2.4.0於2026-07-01發布。官方 changelog 版本範圍終點 `ec9022d156846f24e22bec3e475d4206d6f321ea`，不冒稱其等於 Maven binary Git SHA。

精確 Maven `androidx.test.uiautomator:uiautomator:2.4.0`，POM Apache-2.0；source jar SHA256 `be710c5b295357f7e0deb652f1aa192b4fa6f2245af361743b402ce88ab8ec21`。不採用額外 GPL code 或 platform reflection。

2.4 hashCode 仍會嘗試 refresh，遇 stale 才使用 cached-node hash；不能稱為完全無 IPC。UiDevice 每2秒核對 service flags 並呼叫 setServiceInfo，使 accessibility cache 更新；不是增加本專案 read deadline。平台 UiAutomation.clearCache 是 API34 起，不能假裝 API33 有此公開 API。

POM direct dependencies：uiautomator-shell2.4.0、tracing1.1.0、annotation1.8.1、Kotlin stdlib2.1.20、jspecify1.0.0、test core1.7.0、JUnit4.13.2。monitor 等實際版本以 Gradle resolved graph 為準，不由此 POM 猜測。

## RED → 最小修正 → GREEN

1. 真 ActivityScenario 的 editor 可見時建立 map，關閉 editor 後 getter 必須 stale；既有 map 查找不得拋出 stale，也不能把失效 getter 當 fresh UI。
2. 原2.3.0、API33 emulator run38561946857／source8c7d318：App5PASS，infra regression1FAIL；既存map查找實際StaleObjectException，kt33 assertion有效RED，getter仍stale的前置assertion已通過。
3. 僅 `acceptance-host` 的 androidTestImplementation 使用 UiAutomator alias。已只更新 catalog scalar，主程式依賴／native／signer 不變。
4. 先同一 API33 identity GREEN，再 ZH06→ZH01 實際 soft-key／editor，最後 full App／Host；不得以有限 probe 代表全套 acceptance。

新 infra regression 不發 P1 case envelope：當前 full Host33個JUnit identities = 原32 case methods＋1 regression，case envelopes仍32。frozen P1-E26 identity manifest／APK不變。

## CI 保護與本地證據

workflow guard 保護 main/Host production source、Gradle build scripts、全部 gradle 目錄、LICENSE、NOTICE；只從 catalog comparison 移除已核對的 versions.uiautomator scalar，其餘 parsed data 必須相同，版本僅接受2.3.0／2.4.0。六項真 isolated Git fixture 執行 workflow 程式：38有效1 assertion RED、39全部GREEN；包含拒絕 native pin、其他 dependency、product／Host source及未審查版本。

33b instrumentation compile PASS；35b有限模式13PASS；36b current inventory32!=33有效RED後明確加入精確新identity；37全量Python123PASS。36首次 import error不作RED；35 concurrent fake timeout保留，不延長timeout。

## 裁定

目前 `RUNTIME_RED_CONFIRMED_GREEN_PENDING`。不得宣稱完整 Host、historical visibility 或 physical acceptance 已修復。依賴更新／resolved graph／native與凍結資產保存／獨立 review 均須另記實際結果。
