# P0-R 收束狀態（2026-10-10）

基線 `85711bc8c749d488923954e8f4c173a493bf27eb`；繼承產品 `75b93a3b68867fd6ec4c6adadd3e279d71e98104`。本輪只改新隔離 `fix/p0-core-typing-recovery`；其他 10 工區 HEAD/status/既有 dirty bytes 最後核對未變。沒有 push、M4、release signing、安裝或資料清理。

- b223 凍結 APK 等於原 P1-D 包，version20001/0.2.0-alpha.1；不是裝錯檔名就能解釋全部問題。手機已裝 bytes 仍 UNKNOWN。
- 真原生 RED 已確認 no-tone 候選缺失、live cand_open 清尾音、CAPI commit 未 ack 重送；修同 pin FuzzyPartialPrefix、disposable preview、fresh native identity 選字、ACK 和 bounded active history。
- Candidate tap 完成中文組字送 editor；InputConnection composing/self-selection/外部 span 更新、false ACK 保存不盲目重送均有 regression。
- 獨立審查新增 unavailable candidate／標點／模式切換、外部 span、cancel history、英文 NO_SUGGESTIONS/AUTO_COMPLETE regression，全部先有 assertion RED 再修正。
- 額外 Unicode RED 證實 𠮷 被 JNI Modified UTF-8 誤解為ð；五個 native output 統一標準 UTF-8→Java String，真 CAPI fixture candidate/buffer/commit GREEN。
- EnglishWordSession＋AOSP10k離線詞頻提供 prefix completion；最大8候選，password/forceASCII/nontext/no-suggestions/editor-completion 禁英文查詞，無 network/learning/autocorrect/模型。
- 最後真 native probe `unicode-final-green` exit0、failures=[]，匹配 adapter60cb45cd…086c18 / JNI9e2760c1…819076。三種 cancellation prefix 後續 decoding/paging/select/commit 與 clean context 相等；兩種45字長句送出一次。
- fresh App49類229測、Host3類37測、Python104測、static10contracts 全綠。App真JNI→service/Compose→Robolectric EditText五案沒有skip；兩個androidTest APK及四ABI Debug build、releaseKotlin compile成功。
- 最後Debug APK v20002/0.2.0-p0r.1；最後來源、完整hash與交付路徑見10/11。原 frozen四APK/v3 ZIP hashes與CRC不變。
- OCR1.12.13一次provider health401，BLOCKED_BY_PROVIDER。獨立review結論以09為準，前一位reviewer使用量中斷不冒稱APPROVE。
- 新CoreTypingAcceptanceTest6方法已編譯，未device執行。旧P1 runner固定旧APK/manifest，不套用新P0包。

`DEVICE=NONE`、`RUNTIME=NOT_TESTED`、`PHYSICAL_ACCEPTANCE=OPEN`、`PRODUCT_TYPING_ACCEPTANCE=OPEN_AFTER_FIX`、`PRODUCTION_RELEASE=NOT_READY`、`M4=NOT_VALIDATED`。下一步是使用者安裝新Debug包，以合成文字在真Android editor確認「我／我現在」及英文完成；失敗保留glyph/版本/editor資訊，不清資料。
