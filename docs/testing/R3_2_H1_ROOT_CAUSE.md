# R3.2 H1：根因仍未定案

## 精確範圍

Base main `8369da25fa54ab762dfceed457e71a08767a642e`；Track A 分支 `fix/r3-2-h1-runtime`。只移入R3.1 `feab29f53583c4bb8dfc4cffe13243a36a3e9d06` 的TEST_ONLY health/protocol及原測試／fixtures；不整批套用PR10/12/20。不修改任何app／acceptance-host來源、產品assertion、native、版本、簽章或資料。

CI39509158938已完成：blocking build SUCCESS；experimental runtime FAILURE，App5PASS/Host15PASS9FAIL8SKIP。那是main當次產品證據，不能挪作本H1實驗成功。

## 原始分歧與唯一假說

feab29f 的原raw：H1 before Window466f05b、HAS_DRAWN、display unfrozen、cutout0、h866dp；第一次uiautomator輸出 `ERROR: null root node returned by UiTestAutomationBridge.`，3006ms timeout；after Window9e32380、DRAW_PENDING、frozen、cutout top136、h814dp，旋轉角度仍0。相同boot/user/defaultIME。final恢復unfrozen不能洗掉首次失敗。

**Hcutout**：display cutout/config收斂與首次active accessibility root查詢重疊，可能造成host window重建與null root。現有raw沒有null訊息逐行時間，故無法證明config先於null，亦無法排除工具斷線／kill後才改變。ROOT_CAUSE=UNRESOLVED，confidence低至中；不稱為已修復或flaky。

反證：若null明確先於config變化，該因果次序不成立；若穩定config/window下仍null，單靠config不能解釋失敗。若新一次未復現或缺共同時間軸，仍UNRESOLVED，不以偶發PASS定案。

Android13官方機制：[DumpCommand](https://github.com/aosp-mirror/platform_frameworks_base/blob/0d3ff311e6e80dee7fe88a2a2cfa272ce231c3c6/cmds/uiautomator/cmds/uiautomator/src/com/android/commands/uiautomator/DumpCommand.java#L76) 在root為null時已return，尚未走到FileWriter；[UiAutomationConnection](https://github.com/aosp-mirror/platform_frameworks_base/blob/0d3ff311e6e80dee7fe88a2a2cfa272ce231c3c6/core/java/android/app/UiAutomationConnection.java#L102) 的disconnect會還原rotation lock，但不足證明它造成cutout。平台ref未證明與image framework bytes完全相同。

## 本次最小可證偽實驗

先補診斷，不先引入等待remedy：一次新的disposable API33/default/x86_64/pixel7，原四APK SHA/reference run39152444358及b9820626source固定。只在H0 HEALTHY後安裝原host APK，執行原H1；不安裝KU-Yin產品、不選IME、不執行H2/App/Host。

opt-in `--output-timeline` 記錄UI dump stdout/stderr chunks的offset、bytes、host UTC/monotonic接收時間；保存原bytes、exit、timeout。guest epoch/uptime與overlay前後、有限allowlist framework log亦保存。這些是非原子、可能擾動時序的觀測，並非guest事件發生時間或持續健康保證；`drained_after_timeout`僅表示timeout-handler取得的bytes。

命令上限3s及H1擷取總預算10s、原bracket/XML/defaultIME/final identity/screenshot oracle均未放寬。診斷收集在原有擷取預算外的準備／事後階段，不用它替代原判定。沒有固定sleep修復、沒有新retry或experimental等待gate。既有INCONCLUSIVE polling保留原樣。

## RED/GREEN是診斷契約，不是Android根因修復

- RED23：真子程序輸出原null-root並逾時，舊collector有partial raw但沒有接收時間；新增測試因時間證據缺席而FAIL。
- GREEN25：保留raw與timestamp，逾時仍INCONCLUSIVE。原有PTY測試在macOS sandbox失敗；22在允許PTY的環境37PASS，不能冒稱Android RED。
- 完整26曾FAIL：400ms合成fixture因temporary shebang assessment未啟動。29量到direct Python17ms／shebang378ms；改成只在ADB process seam使用direct child，仍400ms；30PASS。不是延長Android期限。
- reviewer指出host排程停頓時，pipe尾端可能未保存；原subprocess.run也有相同診斷缺口，非新增Android regression。32 meaningful RED→nonblocking drain→33兩focused GREEN；drain不等待新輸出，逾時不改PASS。複審預啟動child120ms反例亦保存所有bytes。
- fresh全Python34：152PASS；全部10contracts27 PASS。無LSP工具，不冒稱執行；以Python編譯／實跑與獨立review覆核。

CI與freshH0/H1結果見本分支最新handoff；未取得可信H1 PASS就停止，H2/App/Host仍NOT_EXECUTED，Release NO_GO。

## Fresh有界觀察

CI39514852906@8acf4b2：H0 HEALTHY、H1 INCONCLUSIVE。完整XML在2243ms到達，但editor focused=false且命令3007ms timeout；本次沒有null-root。Window/cutout/config於dump前後穩定，原假說未重現，不能宣稱修復。見 [最終handoff](R3_2_H1_FINAL_HANDOFF.md)。
