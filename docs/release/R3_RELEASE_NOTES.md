# KU-Yin 0.2.0-rc.1 R3 — 未發布候選

**Debug測試產物，NO_GO；不是已發布RC或Stable。** 公開 `v0.2.0-aie.1`／tag／資產不變。

## 修改

- 移除捐贈卡SHA256偽QR，改為離線標準QR PNG；核對FSF／ASF／OSI／EFF完整官方HTTPS網址，保留複製／開啟與直接交基金會、無背書文字。
- QR整數module／完整quiet zone，明示手機相機尚未驗證；未新增產品網路、相機或麥克風權限。
- 整合既有R2.3.1 test lineage；CI固定reviewed source、同serial/user/boot與H0 bytes，失敗保留證據並停止。原App／Host visibility及typing assertions、native pins、候選owner與ACK不變。
- 版本20004／0.2.0-rc.1，沿用本機既有Debug certificate；source/relink與SHA材料分開標記本機／CI來源。

## 驗證與未完成

本輪Python205、contracts10、App JVM280PASS/10SKIP、Host JVM123PASS；四APKbuild與四ABIrelink完成。唯一CI39152444358 H0HEALTHY／H1INCONCLUSIVE（缺XML），H2／App5／Host33／32-case／B01未執行。實機、QR相機、TalkBack、保資料更新、長期signer、D2D政策與完整runtime license inventory仍OPEN。

不把Debug當正式簽章，不以CI不同certificate產物更新既有Alpha，不卸載或清字典。詳細 [readiness](R3_RELEASE_READINESS.md)、[validation](R3_RELEASE_VALIDATION_MATRIX.md)、[device plan](R3_DEVICE_ACCEPTANCE.md)、[license/source](R3_LICENSE_INVENTORY.md)。
