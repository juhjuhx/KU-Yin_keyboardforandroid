# 英文離線詞頻資產來源

- 上游：Android Open Source Project，`platform/packages/inputmethods/LatinIME`。
- 固定 commit：`127336e9f29d69607eab55982324b210279ae8c5`。
- [來源 gzip](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/127336e9f29d69607eab55982324b210279ae8c5/dictionaries/en_wordlist.combined.gz?format=TEXT) 回傳 base64，必須先解碼再核對 gzip。
- gzip：914,050 bytes；SHA256 `07682388185c285d307e341d1733331af8699f735b4137e9f22571017fab69d2`。
- 原始 header：`dictionary=main:en,locale=en,description=English,date=1414726273,version=54`。這是固定舊資料，不宣稱最新語料。
- 本專案修改：只取第一層 unigram、小寫 ASCII 單字、`f > 0`、空白 `flags`、非 `not_a_word=true`；同字取最大 `f`，依 `(-f,word)` 排序，再取前 10,000 詞。未加入自造詞頻、bigram、縮寫、專名或學習資料。
- `word_frequency.tsv`：123,878 bytes；SHA256 `e44d3b87e830de6e57cc493f3cdb090404a477781299648464b2824e3b488e97`。
- `f` 是 AOSP 量化 logarithmic frequency score，不是原始 corpus 次數；[格式說明](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/127336e9f29d69607eab55982324b210279ae8c5/dictionaries/sample.combined) 特別規定 `f=0` 不應主動建議。
- 授權：Apache-2.0。[固定 Android.bp](https://android.googlesource.com/platform/packages/inputmethods/LatinIME/+/127336e9f29d69607eab55982324b210279ae8c5/Android.bp) 宣告適用子目錄；同目錄附完整上游 `NOTICE` 與 `LICENSE-Apache-2.0.txt`。
- `NOTICE` 原檔 SHA256：`d04394c729fc9fda050d5733d8ee11663fb54fe54d94532de968cf85c1cc42fb`。

重建（在專案根目錄，來源為已解碼的官方 gzip）：

```sh
python3 scripts/generate_english_lexicon.py --source /path/to/en_wordlist.combined.gz
python3 -m unittest scripts/tests/test_p0_english_lexicon.py -v
```

擷取器先核對固定 gzip 雜湊，再核對 10k TSV 雜湊，才寫入輸出。授權與來源檔一起重散布。此資產只供離線字首補全；輸入內容沒有持久化、查網路或個人化學習路徑。
