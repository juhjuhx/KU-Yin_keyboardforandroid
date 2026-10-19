# R3 來源、授權與重鏈結清單

本輪只修 QR／CI，不改 native pin／English data／授權策略。這份清單與新材料可核對，**不是全面法律核准**。

| 元件／位置 | 固定身分／授權 | 本輪直接證據與限制 |
|---|---|---|
| KU-Yin tracked source | b98206267b3a65c452db1661bb3bae910ec61f6a；LICENSE LGPL-2.1 | exact `git archive`，含本輪QR/JVM/CI來源；未含私鑰／userdict／build |
| libchewing source | a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c；LGPL-2.1-or-later | 完整source／COPYING／Cargo.lock/vendor；沿用已驗immutable材料，不冒稱本輪Rust重編 |
| 四 ABI static archives | prebuilt3587ba3355711f0aca50136e787719f6562676b8 | 與 staged及舊材料逐byte相同；本輪JNI objects重新取自當次build，僅copy strip-debug |
| prebuilder patch | 55e3c0877ba060bf6670120156eea61e4438797c；patch SHA8adab140…47762346 | build-only patch／recipe來源保留；原source未暗改 |
| native data | ea74f76dd2548b1d65d0ff70c3ae66057a6ad97d；詞頻資料標LGPL-2.1-or-later | 完整data source＋四dat；system與personal store分開，沒有複製userdict |
| English assets | LatinIME127336e9f29d69607eab55982324b210279ae8c5；Apache-2.0／原Lexiteria NOTICE | TSV SHAe44d3b87…88e97、SOURCE.md、NOTICE／license皆與原Alpha相同；沒有新GPL鍵盤code |
| Cargo vendor／toolchain | 76 crates逐項license inventory；Rust1.92／NDK notices | 原source/vendor inventory保留；r-efi5.3.0、wasip2 1.0.1+wasi-0.2.4無獨立LICENSE/COPYING且notice_files空。build/dev/platform不等於APK runtime subset，COPYRIGHT-library.html仍缺；完整notice review OPEN |
| AndroidX／Compose／Room | Apache-2.0及各自notices | 既有dependency；四ABI亦含AndroidX graphics path，全面transitive license/vulnerability review仍OPEN |
| QR assets | 專案由公開官方URL離線生成 | PNG／manifest／generator source；無基金會logo／背書授權聲明 |
| OpenCV tool／Python wrapper | 4.13.0 Apache-2.0／wrapper MIT | 僅離線asset tool，不在APK；精確生成版本見QR provenance |
| ZXing | core3.5.3 Apache-2.0 | 僅testImplementation；四APK DEX雙路核對與ZIP均無ZXing/OpenCV |

## 本輪新材料

`KU-Yin-v0.2.0-rc.1-R3-source-and-relink.tar.gz`：43,017,050 bytes，SHA256 `00c43969da91acf24fd32874b6c5c00eaf2cdd46dc5ffaf775040b47cdebee09`。4,816份manifest entries／4,817 regular files（包括SHA256SUMS本身）；完整archive串流重驗hash通過。不是舊Alpha tar改名。

四 ABI 本輪真實 link exit0，`.text`／`.rodata` 與本機新APK相同；完整ELF不同、Rust source重建未執行、Android runtime未測。資料夾含exact app source、upstream/data/vendor、licenses、可替換archive、fresh stripped JNI objects、script、工具鏈／來源manifest及本輪relink proof。原Alpha tar SHA `5a6d5ce96bc93f3385473c952912ca266958a51a9acb127a2ba6ab2363877ab8` 保持不變。

材料的 `README-來源與重鏈結.md` 說明修改／重鏈結；允許為修改除錯逆向工程，沒有額外禁止條款。新tar與APK尚未公開發布；包內來源／notices不代表Release授權gate已完成。
