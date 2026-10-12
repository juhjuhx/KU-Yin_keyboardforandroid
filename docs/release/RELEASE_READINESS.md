# KU-Yin Release Readiness

更新2026-10-12；PRODUCTION_RELEASE=NOT_READY；GitHub v0.2.0-aie.1 experimental prerelease已公開；詳見 AIE_ALPHA_HANDOFF.md。使用者回報P0基本中文可用；沒有完整physical matrix證據，也本輪只有新 AIE Debug 測試 artifact，沒有 Release artifact。

| Gate | 狀態／直接證據 | 關閉條件 |
|---|---|---|
| Source identity | main a6a4c65含S1 source39acf96，公開APKacdbacd… | 統一approvedmainline、source/version/artifactmanifest |
| Basic Chinese | WORKS_ON_USER_DEVICE（report）；P0native/JVMhistoricalgreen | 每個coretyping/devicecase rawevidence；不把report扩成30PASS |
| Candidate browsing | S1 已接 strip/expand/page；host tests 綠，實機待驗 | 真scroll/expand/page/select，longpreedit/大字級/OEM |
| English | 10k offline MVP，historicaltests | currentdevice完成completion/space/case/sensitive cases |
| Symbols | 基本inline/40emoji，search未有 | 合規catalog＋grapheme/realeditor＋approvedUX |
| Privacy/erase | sensitivepolicy有；S0已停用不完整reset，fullerase仍OPEN | native＋futurestores verifiederase、optout/privatecontrols、privacydocs |
| Critical data-loss/doubleeffect | P0ACKguard＋S1 native/editor/gesture/lifecycle fresh green；實機OPEN | 失敗matrix、cursor/span/editor切換、沒有未關閉high/blocker |
| Device acceptance | FULL_PHYSICAL_ACCEPTANCE=OPEN | 正式case-by-case裁定＋未觀察scope disposition；恢復真證據 |
| Native/data licensing | Alpha已附source/relink與四ABI proof；完整Rust runtime/license review與重編等價OPEN；EnglishApache notices存在 | 逐檔notice/license、pin/source/relink文件包；build不是授權完成 |
| Unicode/new upstream | 研究固定rev，NO_IMPORT | approvedcopy策略、CLDRdata vsLDML區分，轉換器/pins/notices |
| CI/source consolidation | PR #8已merge；CI38164320915 blocking成功，app5PASS、host15PASS/9FAIL/8skip；fixture待適配 | explicitpush/merge授權後requiredCI；nativehost缺席不可假PASS |
| Signing/update | AIE Debug同certificate/version20003；releasekeystore未建立 | 簽章管理與updatecompat策略批准；不得用新key導致清資料 |
| Community/credits | donation/sourcecards保留；QR僅hash圖 | 功能links保留／歸屬正確；QR如宣稱可掃須真encoder+掃描 |
| Repro/build/contributing | 既有DEVELOPMENT與固定toolchain | 驗證freshcheckout/pins、sourcecompat、CONTRIBUTING與版本changelog |
| Channels/publication | GitHub experimental alpha已批准；F-Droid/Play未批准 | reviewed source/relink/notice、artifact與CI核對；正式簽章另決策 |

當前是已授权的 experimental alpha 發布；不是 production gate 豁免。新 AIE APK 實機復驗後才決定 S2；沒有自動 main-push publication。Rime/model/新telemetry不屬release補丁。M4仍NOT_VALIDATED。

## S0 安全修正
誤導reset HIGH以 unsupported/no-deletion mitigation 關閉；完整原生＋fallback學習清除仍未支援，因此privacy/erase release gate保持OPEN。不能將停用按鈕當erase PASS。
