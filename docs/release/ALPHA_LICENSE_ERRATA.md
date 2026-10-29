# 公開 Alpha 授權文件勘誤與來源補充

此勘誤只校正文件與證據分類，不重授權、不改舊 Tag 或八份發行資產，也不宣稱新版本已完成全部散布驗收。

## 適用對象

- 公開版本：v0.2.0-aie.1／20003，APK SHA256 `acdbacdfebcb897074807bc0746d26ecefbbd8a5f28b25f46aab929afb865634`，產品 source `39acf96c6cfc058106315f3553e1c8b88438f4f7`。
- [既有 source/relink 包](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/download/v0.2.0-aie.1/KU-Yin-v0.2.0-aie.1-source-and-relink.tar.gz)：SHA256 `5a6d5ce96bc93f3385473c952912ca266958a51a9acb127a2ba6ab2363877ab8`。
- F2/F3 TEST_ONLY 與未來 APK 必須各自綁定來源；不能直接沿用這份舊 Alpha 證據。

## KU-Yin 授權文字校正

舊 source 包 `licenses/KU-Yin-LGPL-2.1.txt` 與歷史 root LICENSE 的 SHA256 是 `3edc8e0102b6cb4bfa9a5454533d060fbbcd8e0a438b3cd63a0ba72f1f85cbaa`，含不應改寫的條款異文。現行 [標準 LGPL-2.1 LICENSE](../../LICENSE) SHA256 為 `5749785c8bdefafcb5d798270ed0a967036fe2ca63dcedade1627565dfef81d2`；應以標準文字理解既有 LGPL 選擇。原件保留於 Git 與舊封包供追溯，不覆寫已下載資產。

同一舊包其實已有另一份完整 LGPL 副本 `licenses/libchewing-LGPL-2.1.txt`（26530 bytes，SHA256 `dc626520dcd53a22f727af3ee42c770e56c97a64fe3adb063799d8ab032fe551`）。其規範條款與標準文字一致，差異在末尾套用範例 placeholder，不能因整檔 hash 不同就稱它沒有提供 LGPL。

## 已有的 source／relink 證據

包內保留產品/libchewing/vendor/data 來源、實際 patch、四 ABI archives/JNI objects 與 relink helper：

|材料|SHA256|
|---|---|
|`libchewing-prebuilder.patch`|`8adab140b80f30f9e18593a9f3849ac7ec1c5635d83b00261b14554e47762346`|
|`relink-verification.json`|`640c93358baadb058db077e6a77fecb99df9dca6945b9990fa9684a4d67cd6fe`|

該收據記錄四 ABI link exit0 與 `.text`／`.rodata` 相同。這支持該固定 Alpha 的限定重鏈結能力，不等同所有 section 位元組再現或後續 APK 證明。參照 [LGPL-2.1 第4、6節](https://spdx.org/licenses/LGPL-2.1-only.html)：來源、通知及可修改/重新連結的義務要依實際散布核對；本文件不把 OEM 測試或 Rust 位元組重現列為授權文字本身要求。

## Notice 盤點勘誤

`r-efi` 原 source bundle 已有 [AUTHORS](../../third_party/notices/r-efi/AUTHORS)（SHA256 `ff92bed461f50338dd703a9ba9aee496a425957873df1d91192776e4bdf5dda7`），內含 MIT permission/disclaimer 與版權聲明。先前工具只比對 LICENSE/COPYING 等檔名，沒有列出 AUTHORS；`MISSING_STANDALONE_LICENSE_TEXT` 是索引假陰性，不能作為確定欠缺授權文字的結論。

`wasip2` 的固定上游 revision 通知補於 [third_party/notices/wasip2](../../third_party/notices/wasip2/)。每檔來源、revision、Git blob SHA 與 SHA256 見 [provenance](../../third_party/notices/SOURCE_NOTICE_PROVENANCE.json)。這是來源包 notice 補件；兩個離線 Android feature 模型未包含它們，不能由 vendor 清單推定 APK 已連結這些元件。

## 仍未完成

最終 APK 的 runtime notices 覆蓋與媒體權利仍需逐項核對；沒有特定 Rust 彙整檔名不自動等於違規，也不能直接推定全部義務完成。現有未確認品牌 JPG 權利鏈另行追蹤。完整二進位散布裁定維持 PARTIAL；Runtime、資料保存與簽章保管亦是獨立發行 Gate。
