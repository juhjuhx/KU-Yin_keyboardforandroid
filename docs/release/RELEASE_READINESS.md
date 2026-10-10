# KU-Yin Release Readiness

更新2026-10-11；PUBLIC_RELEASE=NOT_READY。使用者回報P0基本中文可用；沒有完整physical matrix證據，也沒有本輪新releaseartifact。

| Gate | 狀態／直接證據 | 關閉條件 |
|---|---|---|
| Source identity | 核3072aff/9f5db67，P0APK14082… | 統一approvedmainline、source/version/artifactmanifest |
| Basic Chinese | WORKS_ON_USER_DEVICE（report）；P0native/JVMhistoricalgreen | 每個coretyping/devicecase rawevidence；不把report扩成30PASS |
| Candidate browsing | HIGH AIE-01未接paging／UXapprovalpending | 真scroll/expand/page/select，longpreedit/大字級/OEM |
| English | 10k offline MVP，historicaltests | currentdevice完成completion/space/case/sensitive cases |
| Symbols | 基本inline/40emoji，search未有 | 合規catalog＋grapheme/realeditor＋approvedUX |
| Privacy/erase | sensitivepolicy有；HIGHreset只清fallback | native＋futurestores verifiederase、optout/privatecontrols、privacydocs |
| Critical data-loss/doubleeffect | P0ACKguard/historynativeproof；新UI未實作 | 失敗matrix、cursor/span/editor切換、沒有未關閉high/blocker |
| Device acceptance | FULL_PHYSICAL_ACCEPTANCE=OPEN | 正式case-by-case裁定＋未觀察scope disposition；恢復真證據 |
| Native/data licensing | LGPLsource/relink與ABI/repro義務OPEN；EnglishApache notices存在 | 逐檔notice/license、pin/source/relink文件包；build不是授權完成 |
| Unicode/new upstream | 研究固定rev，NO_IMPORT | approvedcopy策略、CLDRdata vsLDML區分，轉換器/pins/notices |
| CI/source consolidation | localP0未GitHub整合；無本輪freshCI | explicitpush/merge授權後requiredCI；nativehost缺席不可假PASS |
| Signing/update | Debugsamecertificate/version20002；releasekeystore未建立 | 簽章管理與updatecompat策略批准；不得用新key導致清資料 |
| Community/credits | donation/sourcecards保留；QR僅hash圖 | 功能links保留／歸屬正確；QR如宣稱可掃須真encoder+掃描 |
| Repro/build/contributing | 既有DEVELOPMENT與固定toolchain | 驗證freshcheckout/pins、sourcecompat、CONTRIBUTING與版本changelog |
| Channels/publication | 尚未批准 | GitHubalpha/F-Droid/Play逐群決策；明示publish才發布 |

下一階段不是自動publicrelease，而是候選UX批准與S1。Rime/model/新telemetry不屬release補丁。M4仍NOT_VALIDATED。
