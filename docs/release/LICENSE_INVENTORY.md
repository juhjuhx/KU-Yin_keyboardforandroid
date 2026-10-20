# 發行授權與來源盤點

2026-10-21；**PARTIAL，不是完整法律／發行核准**。本輪不導入外部程式、資料或改 native pin。

| 材料 | exact 來源／責任 | 已確認與剩餘限制 |
|---|---|---|
| KU-Yin | [LICENSE](../../LICENSE)、[NOTICE](../../NOTICE)；LGPL-2.1 文字 | 倉庫聲明不取代各檔案適用授權 |
| libchewing | source `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`；Android prebuilt `3587ba3355711f0aca50136e787719f6562676b8` | 靜態 archive／JNI 分發需 source/relink 材料；本輪只驗檔案，不重新編譯或作完整 LGPL 合規裁定 |
| Native data | data `ea74f76dd2548b1d65d0ff70c3ae66057a6ad97d`；prebuilder `55e3c0877ba060bf6670120156eea61e4438797c` | 原來源與 notices 在既有 source/relink 包；完整 Rust dependency／runtime 逐檔 review 仍 OPEN |
| English 10k | AOSP LatinIME `127336e9f29d69607eab55982324b210279ae8c5` | [SOURCE](../../app/src/main/assets/english/SOURCE.md)／Apache license／原 NOTICE 保留；NOTICE 含 Lexiteria permission attribution，不能說全部字典均自行創作 |
| Android／Compose 等 | Gradle catalog 與平台依賴 | 原 notices 保留；本輪沒有新 dependency vulnerability／完整 SBOM 掃描 |
| 社群圖片 | [manifest](../community/media/ASSET_MANIFEST.json) | 兩品牌 JPG 的生成式來源標記與權利文件未完成驗證；其他不可解碼素材不作宣傳截圖 |
| 研究項目 | [UPSTREAM](../UPSTREAM.md) | HeliBoard／Qingjian 等僅參考，未把 GPL 程式複製進本輪；Rime／Whisper 不在產品中 |

## 本輪直接檔案證據

重新下載公開 source/relink tar，SHA256 `5a6d5ce96bc93f3385473c952912ca266958a51a9acb127a2ba6ab2363877ab8`，34,766,240 bytes。唯讀列出 5868 entries，逐一核對內部 SHA256SUMS 的 **4806 個檔案**；APK 的四 ABI native 與八個 dictionary/provenance 項目也吻合公開 provenance。

上述是 bytes／完整性驗證，**未執行包內程式、未做 fresh relink／重編等價證明**。歷史重鏈結操作與限制見 [Alpha 授權／建置](AIE_ALPHA_LICENSE_AND_BUILD.md)。公開新版本仍須核對最終 APK／source 一致性及逐檔 obligations；不能搬用舊 Alpha 證據當新 RC PASS。
