# P0-R 授權與來源逐項核對

核對日期：2026-10-10。程式碼、schema、詞庫與第三方依賴分開記錄。下列分類是本輪復用範圍，不表示閱覽一份 repo LICENSE 就完成整個 dependency tree 的法律審查。

- **REFERENCE_ONLY**：研究一般行為；本輪不直接複製程式／資源。
- **SAFE_TO_REIMPLEMENT**：自己寫一般演算法與邊界契約；不是允許移除所採用資料的原授權。
- **LICENSE_REVIEW_REQUIRED**：未來直接連結／複製前，需釘版本、逐檔與傳遞依賴核對；沒有因此引入本輪生產線。

## 官方來源與判定

| 來源與固定 revision | 授權／原始證據 | 本輪分類與義務 |
| --- | --- | --- |
| HeliBoard `65aa8f46fb227d4aad215d314a56f627220d2f92` | [README](https://github.com/HeliBorg/HeliBoard/blob/65aa8f46fb227d4aad215d314a56f627220d2f92/README.md)、[LICENSE](https://raw.githubusercontent.com/HeliBorg/HeliBoard/65aa8f46fb227d4aad215d314a56f627220d2f92/LICENSE)：GPLv3；AOSP ancestry 另有 Apache2，icon 另為 CC-BY-SA4。 | **REFERENCE_ONLY**。本輪沒有複製 GPL source、icon 或 layout；一般詞緩衝與分隔符行為獨立實作。不能把留下的 Apache license 檔當成整個 fork 的授權。 |
| FlorisBoard NLP `9e8d49649d6a3d32af85873043ba2c3c0a43d151` | [LICENSE](https://raw.githubusercontent.com/florisboard/nlp/9e8d49649d6a3d32af85873043ba2c3c0a43d151/LICENSE)：Apache2；[README](https://github.com/florisboard/nlp/blob/9e8d49649d6a3d32af85873043ba2c3c0a43d151/README.md) 列 ICU、MIT/BSD dependencies。 | provider 邊界 **SAFE_TO_REIMPLEMENT**；完整 source/native/data 復用 **LICENSE_REVIEW_REQUIRED**。程式授權不替外部 raw corpus 或產生的字典授權。 |
| AOSP LatinIME `127336e9f29d69607eab55982324b210279ae8c5` | [Android.bp](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/127336e9f29d69607eab55982324b210279ae8c5/Android.bp) 指定 `SPDX-license-identifier-Apache-2.0` 並適用 subpackages；[NOTICE](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/127336e9f29d69607eab55982324b210279ae8c5/NOTICE) 含 AOSP copyright + Apache2 完整文本。 | 純 Kotlin provider **SAFE_TO_REIMPLEMENT**；本輪真正復用是 10k 詞頻**資料**，可依已核對 Apache2 散布，必須保留 notice/license 和擷取修改說明。沒有複製 LatinIME source／native binary。 |
| librime core `7bc3fb0005a03aff1c086611e4d52bb7f6444fdd` | [LICENSE](https://raw.githubusercontent.com/rime/librime/7bc3fb0005a03aff1c086611e4d52bb7f6444fdd/LICENSE)：BSD-3-Clause。 | **LICENSE_REVIEW_REQUIRED**：未採用；若連結需保留 copyright、條款／disclaimer、不得借名稱背書，並核對 dependencies/plugins。 |
| rime-bopomofo `6085c9a38a4a728047862b33d67eee18aa86f3b9` | [LICENSE](https://raw.githubusercontent.com/rime/rime-bopomofo/6085c9a38a4a728047862b33d67eee18aa86f3b9/LICENSE)：LGPLv3；[schema](https://raw.githubusercontent.com/rime/rime-bopomofo/6085c9a38a4a728047862b33d67eee18aa86f3b9/bopomofo.schema.yaml) 載入 preset 並有 stroke dependency。 | **LICENSE_REVIEW_REQUIRED**；不是 BSD 資料。若改／散布 schema 或相關詞庫，需保留授權並依對應修改／來源條款處理。 |
| rime-terra-pinyin `723e51bc266cf9464530c1ddedb856aa18e3da34`；rime-stroke `1e8fff9b9494ddec23b0cbc526bcfd8171a6fd48` | [terra LICENSE](https://raw.githubusercontent.com/rime/rime-terra-pinyin/723e51bc266cf9464530c1ddedb856aa18e3da34/LICENSE)、[stroke LICENSE](https://raw.githubusercontent.com/rime/rime-stroke/1e8fff9b9494ddec23b0cbc526bcfd8171a6fd48/LICENSE)：各 LGPLv3。 | **LICENSE_REVIEW_REQUIRED**；未採用。bopomofo 的可運作 bundle 要另外包含這些來源及 preset／OpenCC 等實際用到的資料授權。 |
| Fcitx5 `a799d4df863d58a7b2e37f96106afb570a58ae4e`；Android frontend `e6199a2801b0c76e1baeff7a48f6b18e910e9dd7` | [Fcitx README](https://github.com/fcitx/fcitx5/blob/a799d4df863d58a7b2e37f96106afb570a58ae4e/README.md)：LGPL-2.1+；[framework license](https://raw.githubusercontent.com/fcitx/fcitx5/a799d4df863d58a7b2e37f96106afb570a58ae4e/LICENSES/LGPL-2.1-or-later.txt)、[Android license](https://raw.githubusercontent.com/fcitx5-android/fcitx5-android/e6199a2801b0c76e1baeff7a48f6b18e910e9dd7/LICENSE)。 | **REFERENCE_ONLY** framework；未新增 runtime。既有 prebuilt 的 libchewing provenance 沿用 KU-Yin pin，不能將其他 addon 全部當成同一授權。 |
| 現有 libchewing `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c` | [固定 README](https://raw.githubusercontent.com/chewing/libchewing/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c/README.md)、[COPYING](https://raw.githubusercontent.com/chewing/libchewing/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c/COPYING)：LGPL-2.1-or-later，列特定第三方例外。 | 既有 production dependency；本輪不浮動 native pin。維持 source/build/relink notice；APK build 不能證明已滿足所有 binary distribution 義務。 |

## 本輪採用資料的雜湊與散布內容

| 檔案 | SHA256 |
| --- | --- |
| 原始 `en_wordlist.combined.gz` | `07682388185c285d307e341d1733331af8699f735b4137e9f22571017fab69d2` |
| 上游 `NOTICE`（原封保留） | `d04394c729fc9fda050d5733d8ee11663fb54fe54d94532de968cf85c1cc42fb` |
| 上游 `Android.bp` 授權宣告 | `c971d67105a5b1f69077c1c8420fedd31ed14f531b98314f57731ab27112f630` |
| KU-Yin 10k `word_frequency.tsv` | `e44d3b87e830de6e57cc493f3cdb090404a477781299648464b2824e3b488e97` |

`app/src/main/assets/english/` 隨資料保留 `NOTICE`、`LICENSE-Apache-2.0.txt` 與 `SOURCE.md`，後者寫明固定 URL、資料 header、過濾／排序／截取修改；擷取器固定 source/output hash。Apache2 的 notice/copyright 與修改標示義務依 [官方 §4](https://www.apache.org/licenses/LICENSE-2.0#redistribution) 處理。這項復用沒有把詞頻分數換成手寫猜值，也沒有引入 HeliBoard GPL source。

## 未採用資料備選

SCOWL `rel-2020.12.07` 的 [官方 README](https://github.com/en-wl/wordlist/tree/rel-2020.12.07) 說明 combined work 為 MIT-like；[完整 Copyright](https://raw.githubusercontent.com/en-wl/wordlist/rel-2020.12.07/scowl/Copyright) 包含 Kevin Atkinson、VarCon、Ispell、WordNet 等各自條款與 public-domain 來源。可以依這些條款散布，但應保留整份 Copyright，不能只貼 MIT 標籤。size/commonness 級別不是每詞 corpus 次數。新主線已稱 ESDB，官方 [網站](https://wordlist.aspell.net/) 不提供穩定 database release；若採用要另釘 commit 與產物 hash。

wordfreq 當前官方 [LICENSE.txt](https://raw.githubusercontent.com/rspeer/wordfreq/master/LICENSE.txt) 是 **Apache2 code**，不應沿用舊「全部 LGPL」印象；[NOTICE.md](https://raw.githubusercontent.com/rspeer/wordfreq/master/NOTICE.md) 宣告 data 可依 **CC-BY-SA4** 散布，並列 Robyn Speer、Google Books、SUBTLEX、OpenSubtitles 等 attribution／來源條件。擷取頻率 data 仍應審查資料衍生作品與 attribution/share-alike，不能拿 code LICENSE 覆蓋 data。本輪未採用，因此沒有下載或包入其 data。

## 範圍結論

新增 Kotlin 邏輯為獨立實作；新增外部資產只有經固定來源核對的 AOSP 英文資料及其 notices。未複製 GPL keyboard source、未新增 RIME/Fcitx/NLP runtime；整合 owner 的最終 NOTICE／來源索引與實際 APK 授權檔核對以本輪總驗證結果為準。
