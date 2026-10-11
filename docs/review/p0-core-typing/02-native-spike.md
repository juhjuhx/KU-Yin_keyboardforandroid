# P0-R：真 libchewing 原生探針與修正證據

本次在 macOS arm64 實際編譯 production 鎖定的 **libchewing 0.11.0 Rust + C API**，再用 production `chewing_jni.cpp` 與 `AndroidChewingEngine.kt` 跑 JVM。結果重現舊 adapter 丟失第二音節，修正後 `ㄨㄛ → 我`、有聲調與無聲調的 `我現在` 均可候選選取、完整提交一次。這是 host 原生邊界證據；**Android 實機／觸控／OEM 驗收仍 NOT_TESTED**。

## 固定來源與範圍

| 項目 | 值 |
|---|---|
| 工作樹起點 | `85711bc8c749d488923954e8f4c173a493bf27eb` |
| 被測既有 product lineage | `75b93a3` |
| libchewing source commit | `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c` |
| CAPI / Rust crate version | `0.11.0` |
| 字典與 Android archive 的 prebuilt commit | `3587ba3355711f0aca50136e787719f6562676b8` |
| 官方 immutable source archive SHA-256 | `29fdb2470a0a09133c1779c6d8058e394778a917c2c35da7f43a2e2869860816` |
| 本機 toolchain | cargo / rustc `1.97.1`、clang++、JDK 21、Kotlin compiler `2.2.10` |

原始碼取自 [官方固定 commit archive](https://codeberg.org/chewing/libchewing/archive/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c.tar.gz)，專用 cache 為 `/private/tmp/kuyin-p0r-native-20261010/`。字典只讀使用 `/private/tmp/kuyin-p1g-native-20261009/fcitx5-android-prebuilt/chewing-dict`，不重建或覆寫舊字典、APK、凍結 worktree；未操作 device、push 或 release。

本 agent 後續獲得 adapter／JNI 修正 ownership，僅改這兩個 production 檔案及新增原生測試 helper。Core／session／service／UI 的整合由 root 負責。

## 舊行為的直接重現

`native-state-traces.json` 同時記錄每鍵的 `buffer`、`bopomofo`、cursor、visible preedit、candidate list、commit，以及 candidate open/close 前後狀態。

| 序列 | 真原生、不刷新候選 | 舊 adapter 的 per-key open/close |
|---|---|---|
| `j,i` | `ㄨ → ㄨㄛ`；buffer 尚空 | 注音仍累積，但候選一直空 |
| `j,i,3,v,u,0,4,y,9,4` | `我 → 我ㄒ → 我ㄒㄧ → 我ㄒㄧㄢ → 我限 → 我限ㄗ → 我限ㄗㄞ → 我現在` | `v` 前刷新可見 `我ㄒ`，刷新後只剩 `我`；後續各注音被清掉，最後仍只有 `我` |
| `j,i,v,u,0,y,9`，預設 engine | 始終覆寫單一未完成音節，末尾為 `ㄗㄧㄞ` | 同樣沒有跨音節轉換 |
| 同無聲調序列，fuzzy engine 2 | `ㄨ → ㄨㄛ → 我ㄒ → 我ㄒㄧ → 我ㄒㄧㄢ → 我限ㄗ → 我限ㄗㄞ` | 完成第一音節後，候選刷新同樣清掉下一音節 |

根因是 `chewing_cand_open` 會呼叫 `editor.start_selecting()`；`EnteringSyllable::start_selecting` 先執行 `syl.clear()`。`buffer_Check != 0` 只保護第一音節，不能保護後續正在輸入的尾音。來源：[固定 commit C API io.rs](https://codeberg.org/chewing/libchewing/src/commit/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c/capi/src/io.rs)、[固定 commit editor/mod.rs](https://codeberg.org/chewing/libchewing/src/commit/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c/src/editor/mod.rs)；本機對應為 io.rs 2787–2794、editor/mod.rs 1212–1223。

此 pin 的 `all_candidates()` 只在 Selecting state 有效。C API 的 `chewing_userphrase_lookup` 只回報 user dictionary 是否存在指定詞，不提供系統字典或不改狀態的候選查詢。不能假設新版 0.12/0.13 的 API 存在於本 pin。

## 已驗證的無聲調預覽

`chewing_config_set_int(ctx, "chewing.conversion_engine", 2)` 會設定 `FuzzyChewingEngine` 與 `FuzzyPartialPrefix`。新初聲／介音／韻母撞到已輸入的位置時，`fuzzy_key_press` 原生完成前一音節、保留新音節；缺少聲調的 phonetic prefix 交由原生字典匹配。來源：[固定 commit zhuyin_layout/mod.rs](https://codeberg.org/chewing/libchewing/src/commit/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c/src/editor/zhuyin_layout/mod.rs)、[trie.rs](https://codeberg.org/chewing/libchewing/src/commit/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c/src/dictionary/trie.rs)、[fuzzy.rs](https://codeberg.org/chewing/libchewing/src/commit/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c/src/conversion/fuzzy.rs)。

補假設聲調可得到部分預覽，但不能只補 Space：`ji + Space` 的完整 conversion 是「我」，該次候選窗卻列出首聲「窩、倭、渦…」，其中沒有「我」。因此實作採原生 fuzzy boundary：

1. scratch `:memory:` context 關閉 auto learning，重播實際 native transitions。
2. 有未完成尾音時，僅 scratch 補 `s`（ㄋ）作 boundary，令原尾音以**未指定聲調**進入 native composition。
3. `chewing_clean_bopomofo_buf` 清掉 boundary 初聲；phone sequence 長度必須增加恰好一個，否則拒絕預覽，避免丟掉 invalid tail。
4. 此時才開候選窗，讀原生完整 conversion；live context 與實際 Key history 完全不變。

JSON 直接證明 `ji` 的原生完整 conversion 為「我」、候選 index 12 也可 choose「我」；`jivu0y9` 的完整 conversion 為「我現在」，最後 native「在」候選 index 9 choose 後仍完整「我現在」。scratch commit 與 reset/ack 亦記錄，live snapshot／raw events 前後相同。

候選第一項一律是原生完整 converted buffer，因此「我」自然出現在 `ji` 的可見第一頁，並未人工調整單字排名。其餘項目先在另一 replay context **真正 native choose**，再讀完整 buffer；例如尾候選「災」投影成「我限災」，不是拼接前綴。含注音或聲調佔位符的 display 不可選取／提交；`jiv` 的有效預測「我想」仍保留。

## 修正的選取、學習與一次性提交

即時 live context 只處理實際輸入、退格及明確的選取／提交。顯示與翻頁都只操作 scratch；synthetic boundary 不加入實際 Key history。

每個可見項目攜帶同 snapshot 的 native phrase identity 與原生選取後完整 display。明確選字時才 finalize live 尾音、fresh enumerate native identity，套用後核對完整 buffer 與 display 相等。scratch ordinal 不會盲用到帶個人字典的 live context。無法核對時關閉 learning、lossless replay 原 transitions（含既有 selection／backspace）恢復 composition，再還原 learning policy，返回 consumed 與 unavailableReason，無 commit text。

既有 CASE-4 保留：engine 的 select 只在原 live context 套用選字，下一鍵繼續 composition；session 的候選點選完成策略由 root 整合。沒有把 memory context 冒充持久化 context。

commit getter 不會消費 CAPI commit buffer，舊 adapter 第二次 Complete 會再送「我」。修正於取得 commit 後呼叫 `chewing_ack`，空 composition 的後續 Complete 不再送出文字。原生自動提交前綴按真正音節完成的 action boundary 從 replay history 移除；不重播已交給 editor 的前綴。

fresh 專用 userdict 先停用 learning：每鍵 preview 後 SHA-256 與初始檔相同；開啟 live learning 後提交「你好」，檔案 SHA 改變，關閉再開原生 context 執行 `chewing_userphrase_lookup("你好", "ㄋㄧˇ ㄏㄠˇ") == 1`，直接證明原持久化學習仍工作。沒有清理或修改既有使用者字典。

## 真 host 結果與重跑

正式 helper：`scripts/p0r_native_host_probe.py`；Kotlin host 測試：`scripts/native-tests/P0RNativeHostProbe.kt`。`HostLog.kt` 僅代替 Android logging；decoder、adapter、JNI、Rust library 均為真 production 邊界。完全未執行 Gradle。

```sh
python3 scripts/p0r_native_host_probe.py \
  --source-dir /private/tmp/kuyin-p0r-native-20261010/libchewing \
  --prebuilt-dir /private/tmp/kuyin-p1g-native-20261009/fcitx5-android-prebuilt \
  --jdk-home /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  --phase final
```

helper 使用 `cargo rustc --offline -p chewing_capi --release -- --crate-type cdylib`，再將真 host staticlib 與 production C++ 編成 `libchewing-jni.dylib`。Kotlin 以 cached compiler `2.2.10` 與 JDK 21 standalone 編譯。每步 command／exit／時間／來源 hash 存入 `host-probe-manifest.json`。

| 驗證 | 結果 |
|---|---|
| 真舊 adapter RED | 三核心案例失敗；continuous 丟尾音、重複 Complete 重送「我」 |
| `ji`、tone continuous、no-tone continuous | visible 第一候選含「我」／「我現在」，select+commit 完整一次；第二次 commit 空 |
| 尾音 alternatives | actual native choose 後完整「我限災」，提交與 display 相等 |
| 未完成尾音退格再輸入 | `我限ㄗㄞ → 我限ㄗ → 我限ㄗㄞ`，仍可轉「我現在」 |
| 重複 `jiji` | 與獨立 CAPI 相同為「喔喔」，不硬編成「我我」 |
| invalid tone `ji34` | 保留 `我ˋ`、consumed、commit 空；退格後可提交「我」 |
| invalid syllable `1j0` | 保留 `ㄅㄨㄢ`、consumed、commit 空、有 unavailableReason |
| partial `jiv` | 候選有「我想」，沒有「我ㄒ」佔位符 |
| 舊 CASE-4 | select 保持 active「你」；下一鍵續成「你ㄅ」；原 learning property 保留 |
| 150 有聲調 keys／45 字 | native overflow chunks + final commit 合計「我現在」×15，無遺失／重送；已提交前綴離開 replay history |
| 105 無聲調 keys／45 字 | 完整「我現在」×15，與獨立 CAPI 相等 |
| 個人字典 | preview 不寫；live learning 開啟後「你好」真正持久化 |
| JNI／Kotlin diagnostics | clang `-Wall -Wextra -Werror`、Kotlin compile PASS；環境沒有 `lsp_diagnostics` 工具 |
| pinned 4 ABI symbols | 新 wrapper 使用的 7 個 C API symbols 均在 4 個既有 archive；不改 pin／ABI archive |

最終 host suite exit 0，這組案例 JVM 執行時間 **30.329 秒**；是整套合成案例時間，不能當單鍵延遲 benchmark 或裝置效能上限。

實際 evidence 位於忽略的 `build/p0r-evidence/`：

- `source-provenance.json`、`host-probe-manifest.json`：archive／production source／字典 hash 與命令。
- `native-state-traces.json`、`native-extended-reference.json`：獨立 C API 逐鍵、hypothetical tones、boundary、clear 與長句 reference。
- `production-adapter-red.json`／`.log`、`production-adapter-final.json`：真舊 adapter RED 與最終修正後 host 結果。
- `native-persisted-learning.json`、`pinned-abi-symbols.json`：持久化學習與 4 ABI API availability。
- `libchewing_capi.dylib`、`libchewing-jni.dylib`：專用 macOS host artifacts，不能用作 Android APK。

## 仍需獨立審查／裝置驗證的限制

scratch `:memory:` 不帶 live 個人字頻，故有 personalized ranking 差異時候選可能安全拒絕。現行選取核對／恢復不能被 session 接續 flush 預設候選掩蓋；root 正以真 editor integration 驗證這個錯誤路徑。候選位置從完整原生選取結果取得，不以字串替換猜測。

候選每頁最多投影原生 page size（10），每鍵可能建立多個 scratch context。已以 enumeration 避免 by-index getter 每項重算全 fuzzy lookup 的二次複雜度；固定短 compose 反覆新增音／退格的歷史成長與手機延遲仍由 fresh reviewer 檢查。長句沒有代表手機效能完成驗收。

本文件證明鎖定 decoder 與 adapter 的 macOS 原生行為。Android Debug link／完整 JVM gates、候選點選 editor effect、Space／Enter single effect，以及新 APK 身分由 root 合併驗證；實機結果不可由本 spike 代替。

## 收束後的 native 再驗證

最終 adapter SHA256 `60cb45cd22799025f604f8a0e23a869aec27ebfaeae20537ab822d010f086c18`，JNI SHA256 `a7e5230786a4e3b5cde62ad10cd667b865e0e0bf72e74d60f8aa0c36a7e79899`。Fresh `build/p0r-evidence/replay-equivalence-green/host-probe-manifest.json` 與這兩個實際來源相符；`production-adapter-replay-equivalence-green.json` 的 failures 為空，command exit 0。

修正前 `ji3` 後反覆 v/Backspace 100 次使 active replay actions =203；修正後 =3。只在 pending key 的 buffer／Bopomofo／cursor／phone count 投影確實還原時消除 inverse pair。這個 tuple **不宣稱等於全部 Rust 內部狀態**：另用獨立 clean/noisy context 比較 `ji3`、`ji3v`、`ji3vu` 三個 prefix，各 100 次取消後的後續每鍵 update、退格、翻頁、候選選取、commit 和第二次空 commit，全部相等。Test-only synthetic trace 保留實際事件；derived active history 不把取消事件當新 syllable。

其餘 fresh probe：有／無聲調 45 字長句完整送出且無重送、alternative candidate 完整 buffer、invalid tail 不提交 raw 注音、preview 不寫 personal dictionary、開啟 live learning 後字典改變。全部是 macOS pinned native／adapter 證據，不是手機 latency 或 device acceptance。

## 最後的 Unicode 邊界修正

獨立審查發現 libchewing 的標準 UTF-8 不能直接餵給 JNI `NewStringUTF`（需要 Modified UTF-8；[Android 官方 JNI tips](https://developer.android.com/ndk/guides/jni-tips#utf-8-and-utf-16-strings)）。Host-only fixture 對 :memory: 真原生 context 加入固定 `𠮷/ㄐㄧˊ`，RED 直接讀 production candidate 得到 `ð`，未通過找字 assertion。不是 decoder fake。

五個 native output wrapper 統一使用 Java `String(byte[], "UTF-8")`；charset 名稱才使用 ASCII 的 NewStringUTF，錯誤保留 JVM exception 並釋放 local refs。GREEN 核對候選陣列、by-index、選字後 buffer、commit 都是 `𠮷`。補助 CAPI fixture 只在 host test 編譯，不在 Android CMake source list，不增加 product API。

**最終 JNI SHA256 改為 `9e2760c17fa7f6211e0b33e0d4f21d06ae108f0a946a476b8151d97633819076`**；adapter `60cb45cd…086c18` 未變。最後 `unicode-final-green/host-probe-manifest.json` 與結果 JSON 取代上一節 a7e5 intermediate checkpoint，完整 probe exit 0。Android 四 ABI build 與 App229測重新通過；ART 實機仍未驗證。
