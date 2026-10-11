# P0-R 中文解碼器與英文建議方案比較

研究日期：2026-10-10。比較依固定原始碼與官方文件；沒有測得候選品質、解碼延遲、RAM 或 APK 增量的項目不填估算數值。本輪先修復既有 libchewing 邊界，再做英文離線補全；RIME 保留後續隔離 spike。

## 中文解碼器

| 選項 | 能力與本輪成本 | 授權／來源 | 判定 |
| --- | --- | --- | --- |
| 現有 libchewing C API | 已有 service owner、JNI、Compose session、ABI 與詞庫部署。修正候選／組字邊界可沿用既有 ownership；不需要換整個平台。現有 pin 的能力應由本輪 adapter 與裝置證據確認。 | source `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c`，prebuilt `3587ba3355711f0aca50136e787719f6562676b8`。官方 [固定 README](https://raw.githubusercontent.com/chewing/libchewing/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c/README.md) 宣告 LGPL-2.1-or-later，另列 SQLite public domain／CMake BSD 例外。 | **本輪採用**既有生產線。靜態 native 散布的 source/relink 條款仍沿用現有 NOTICE 的開放事項。 |
| librime + 注音 schema | 核心支援 schema、候選 menu、composition 和模組式 engine；需新增 C API/JNI ownership、初始化／部署、schema 編譯、user data／學習控制與多 ABI 驗證。官方 [依賴表](https://github.com/rime/librime/blob/7bc3fb0005a03aff1c086611e4d52bb7f6444fdd/README.md) 列 Boost、LevelDB、marisa、OpenCC、yaml-cpp 等。 | 核心 [BSD-3-Clause](https://raw.githubusercontent.com/rime/librime/7bc3fb0005a03aff1c086611e4d52bb7f6444fdd/LICENSE)；`rime-bopomofo` 與 terra-pinyin／stroke 資料各為 LGPLv3，不能統稱 BSD。 | **LICENSE_REVIEW_REQUIRED**；先維持 engine-agnostic seam，再獨立 spike，不能把換引擎當作已證實的候選修復。 |
| Fcitx5 framework / Android frontend | Fcitx 是 input-method framework，載入各語言 engine；Chewing 和 RIME 是其中不同 backend。整合整個 framework 會擴大 Android frontend、addon、native runtime、設定與生命週期範圍。現有 Android prebuilt 來源不代表 KU-Yin 已採用全 Fcitx。 | [Fcitx5 固定 README](https://github.com/fcitx/fcitx5/blob/a799d4df863d58a7b2e37f96106afb570a58ae4e/README.md)：LGPL-2.1+；[官方 engine 列表](https://fcitx-im.org/wiki/Input_method_engines) 明確區分 fcitx5-chewing、fcitx5-rime。 | **REFERENCE_ONLY**，不是本輪 decoder 替換。 |

`rime-bopomofo` 固定 schema `6085c9a38a4a728047862b33d67eee18aa86f3b9` 的 [YAML](https://raw.githubusercontent.com/rime/rime-bopomofo/6085c9a38a4a728047862b33d67eee18aa86f3b9/bopomofo.schema.yaml) 為 v3.1、要求 librime >=1.16；同 pin [README](https://github.com/rime/rime-bopomofo/blob/6085c9a38a4a728047862b33d67eee18aa86f3b9/README.md) 仍寫 >=1.3。若做 spike，以實際 schema 要求驗證，並釘選 terra-pinyin、stroke 和 preset 設定所有資料。schema 的空格／聲調／選字預設也不能直接覆蓋 KU-Yin 的行為契約。

## 英文建議

| 來源／方案 | 可參考部分 | 本輪決策 |
| --- | --- | --- |
| HeliBoard | 英文鍵盤、候選呈現、分隔符與 editor 行為。官方 [README](https://github.com/HeliBorg/HeliBoard/blob/65aa8f46fb227d4aad215d314a56f627220d2f92/README.md) 宣告 GPLv3；保留 Apache2 AOSP 起源不表示所有後續修改都是 Apache2。 | **REFERENCE_ONLY**。不複製 GPL 程式、UI 或資源；一般行為以自己的測試與程式獨立實作。 |
| FlorisBoard NLP | C++ dictionary manager、suggestion／spellcheck、dictionary preprocessing 邊界。官方 [NLP README](https://github.com/florisboard/nlp/blob/9e8d49649d6a3d32af85873043ba2c3c0a43d151/README.md) 自述 alpha、記憶體／詞庫大小和 ranking 尚待改善；那是上游自述，不是 KU-Yin 量測。 | Apache2 的程式不自動授權外部字典。完整 NLP/native 整合超出 bounded MVP；一般 provider seam **SAFE_TO_REIMPLEMENT**。 |
| AOSP LatinIME | 官方 combined unigram/frequency 格式與 Apache2 data。完整 LatinIME binary dictionary／JNI／gesture 管線不必引入。 | 本輪擷取固定 10k 資料，以純 Kotlin `EnglishLexicon` + `EnglishWordSession` 做離線字首補全；見 [07](07-english-dictionary-design.md) 與 [08](08-license-review.md)。 |
| SCOWL / wordfreq | SCOWL 可建立拼字清單與 commonness 分級；wordfreq 是多來源量化詞頻。 | 兩者列為未採用備選：SCOWL 分級不是精確 corpus frequency；wordfreq data 的 CC-BY-SA／來源 attribution 額外成本對本輪不必要。 |

## 共用邊界

中文與英文都讓自己的 session 擁有組字；UI 只讀 snapshot 並發命令，service 只套用有序 editor effects。候選 identity 應為 opaque source/entry token 加 snapshot revision；native index 留在 adapter，不能讓候選文字或顯示位置成為跨引擎選字 identity。過期 revision 必須拒絕，重複文字仍是不同 entry。

本輪英文 word state 是獨立元件，候選 snapshot identity 與 UI/service 接線由共用契約 owner 整合。這份比較沒有宣稱 RIME 已接線或任何裝置 runtime 已通過。
