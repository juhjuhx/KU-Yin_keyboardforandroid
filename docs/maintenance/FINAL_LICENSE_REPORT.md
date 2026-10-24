# Final License Report — R3.8

## 標準文字校正

既有KU-Yin LGPL2.1授權選擇不變。根LICENSE復原自[SPDX固定revision31ba1a50](https://raw.githubusercontent.com/spdx/license-list-data/31ba1a50e5397e00a304dbadc76531740e89ee48/text/LGPL-2.1-only.txt)，26001 bytes，SHA256 `5749785c8bdefafcb5d798270ed0a967036fe2ca63dcedade1627565dfef81d2`。原main2a2181e的section5及其他文字有實質改寫，非單純排版；原bytes由Git保留。

舊Alpha source-and-relink.tar.gz內licenses/KU-Yin-LGPL-2.1.txt與舊root完全相同（SHA3edc8e0102b6cb4bfa9a5454533d060fbbcd8e0a438b3cd63a0ba72f1f85cbaa）。八資產與舊tag保留，不修改封包；這是需公開更正的歷史文件問題，不宣稱本輪已修正已下載的舊包。其他上游license不能只因與SPDX排版不同就斷言異文，需逐檔核對。

## 實際依賴與材料

|元件|來源／義務／狀態|
|---|---|
|libchewing production decoder|source a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c；LGPL2.1 family；經JNI靜態連結，需要對應source/relink與元件notices|
|Android prebuilt|fcitx5-android/prebuilt3587ba3355711f0aca50136e787719f6562676b8；bootstrap確認commit/四ABI/字典；source association不是重編等價證明|
|English10k|AOSP LatinIME127336e9f29d69607eab55982324b210279ae8c5衍生詞表；Apache2.0/來源NOTICE/Lexiteria attribution保留，無GPL鍵盤code拷入|
|Compose/AndroidX/Material Icons|Gradle production dependencies；未發現bundled font binaries；需對最終binary彙整notices|
|Launcher/品牌JPG/SVG|生成manifest與原創SVG分開；JPG原件權利鏈仍OPEN；Roborazzi不是手機截圖|
|Pillow/PyYAML/Gradle/Actions|僅驗證/建置；不列Android runtime功能；固定來源見BUILD_INPUT_PINS|
|Rime/Fcitx5 framework/HeliBoard/Whisper|研究或未來，並非新增runtime dependencies|

舊Alpha有source/vendor/relink材料，不自動適用future APK。尚未完成全新二進位的完整SBOM、全部crate/data/media逐檔審核及重鏈結重建等價；**LICENSE_VERDICT=PARTIAL；NEW_BINARY_DISTRIBUTION=BLOCKED**。根文字checksum通過不是全部LGPL義務通過。
