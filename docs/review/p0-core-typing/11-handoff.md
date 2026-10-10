# KU-YIN P0-R CORE TYPING RECOVERY HANDOFF

## SOURCE

BASE_SHA: 85711bc8c749d488923954e8f4c173a493bf27eb
INHERITED_PRODUCT_SHA: 75b93a3b68867fd6ec4c6adadd3e279d71e98104
FINAL_PRODUCT_SOURCE_SHA: 9f5db676a17b5770a738b5c20da937f9211e5a34
BRANCH: fix/p0-core-typing-recovery
WORKTREE: /Users/huang/KU-Yin_keyboardforandroid/.worktrees/p0-core-typing-recovery
DOCUMENTATION_HEAD: 文件提交後的精確 HEAD 登錄於 Desktop「來源與雜湊.json」與工區 delivery-manifest.json；product source SHA 與 documentation HEAD 分開。
PUSHED: NO

## OLD_APK

SHA256: b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9
PRODUCT_SOURCE: 75b93a3b68867fd6ec4c6adadd3e279d71e98104
IDENTITY: 凍結 v3 bytes 等於原 P1-D 包，含 P1-A/B/C/D；不是 P1-E/G 的新產品版本。手機已裝 base.apk bytes UNKNOWN。
VERSION: 20001 / 0.2.0-alpha.1
FROZEN_ARTIFACTS: 四APK／v3ZIP及其他工區未變。

## ROOT_CAUSES

ZH_NO_TONE: 原 adapter 在尚無 converted buffer 時返回空候選；正常 native path 不使用 fallback no-tone query。
ZH_CONTINUOUS: live候選 cand_open 清除未完成尾音；重讀 CAPI commit 未 ack 會重送。
KEY_MAPPING: canonical大千 ㄨ=j、ㄛ=i；rawevents已驗證。口述UO／C→Z尚無精確手機glyph/event證據，不能宣稱literal C miswire；尾音遺失另已真重現。
EDITOR_COMMIT: native select原先只替換preedit、session/UI未完成送出；service缺composing projection且忽略ACK，容易掩蓋失敗。
ENGLISH_BACKEND: 原 onEnglishKey direct character commit，沒有 word buffer／lexical provider。
UNICODE: CAPI標準UTF-8誤餵NewStringUTF，真fixture把𠮷解為ð；五個output已修正並GREEN。

## DECODER / IMPLEMENTATION

CURRENT: 同 pin libchewing，原生FuzzyPartialPrefix；不學習的scratch context預覽完整buffer，fresh native text/occurrence identity選取、完整結果核對；錯配保留組字並顯示unavailableReason。
RIME_DECISION: 未整合；既有引擎已通過核心真native契約，先完成實機復驗。未來需要新能力再獨立spike／逐檔授權核對。
FCITX_DECISION: framework研究參考，沒有引入另一個runtime；與librime不是同一元件。
EDITOR: orderedCommits一次送出、setComposingText、自身callback識別／外部span重置、false ACK保存不自動重送；候選用同snapshot index/generation而非label indexOf。
ENGLISH: AOSP10k離線prefix provider、wordbuffer／completion／Space／Backspace／case；敏感、非文字、forceASCII、NO_SUGGESTIONS/AUTO_COMPLETE禁英文詞典。無autocorrect、learning、network或0.9B模型。
VERSION: 設定頁讀BuildConfig真版本，移除未證實詞庫完整性數字。
CHANGED_FILES: code commit共37檔；主要native/JNI/session/service/UI/editor/English/assets/test/probe，詳git show 9f5db676a17b5770a738b5c20da937f9211e5a34 --stat。沒有native pins／ABI／applicationId變更。

## RED / GREEN

RED: 原真JNI no-tone／尾音／commit；editor3/3；editorRecovery6/4；englishUI1；review regressions4/3；span8/1；history100cancel actions203；English flags3/2；Unicode exact candidate字元失敗。詳05及原log/XML，missing-class compile失敗未當有效RED。
GREEN_APP: 49 classes / 229 tests / 0 failures / 0 errors / 0 skipped；含5案真JNI→正式service/Compose→Robolectric真EditText。
GREEN_HOST_UNIT: 3 classes / 37 tests / 0/0/0。
PYTHON: 104 tests，exit0（runner101＋converter3，全部device mutation paths是fake）。
CONTRACTS: 10/10，exit0。
NATIVE: unicode-final-green完整production probe exit0/failures=[]；three-prefix cancel replay後續candidate/commit相等、兩種45字長句、invalidtail、preview不寫personaldata/livelearning仍可持久化。
BOOTSTRAP: exit0；原pin。
BUILD: final-delivery-build.log exit0，App/HostDebug與兩androidTest APK成功、四ABIlink、compileReleaseKotlin成功。未Release簽章。
DEVICE_TESTS: 新P0 CoreTypingAcceptanceTest六方法COMPILE_READY_NOT_EXECUTED；既有app5／host26未device執行。舊v3runner只認舊hash/manifest，不用於新P0。

## NEW_APK

PATH: /Users/huang/Desktop/KU-Yin-P0R-2026-10-10/KU-Yin-P0R-core-typing-debug.apk
SHA256: 14082a60527ef57542d62cfb2bfaac1ed53a99080fb5af7f0de2792862fe260f
BYTES: 32276066
VERSION_CODE: 20002
VERSION_NAME: 0.2.0-p0r.1
APPLICATION_ID: io.github.juhjuhx.kuyin
SIGNER: 46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94
SOURCE_SHA: 9f5db676a17b5770a738b5c20da937f9211e5a34
PROVENANCE: 10、Desktop來源與雜湊.json；95 build-source bytes等於codecommit，沒有宣稱SHA嵌入APK。
INSTALL: 靜態更新前提相同signer／較高version成立，手機尚未安裝；失敗停止，不uninstall/clear。

## DEVICE / REVIEW

DEVICE: NONE（fresh ADB0）
INSTALLED: NOT_EXECUTED
EDITOR_COMMIT_DEVICE: NOT_TESTED
EDITOR_COMMIT_HOST: 真JNI＋Robolectric EditText五案通過，不能冒充實機。
OCR: v1.12.13(fabbdb29)，一次health exit1/Anthropic401 invalid x-api-key，BLOCKED_BY_PROVIDER；request req_011CfskQf7f7gSGPApTUPDeT，session null。沒有外部OCR PASS。
INDEPENDENT: 實際finaldiff／XML／native manifests／APK身份核對，無未解決code blocker，見09。前一reviewer用量中斷未冒称已final approve。
LIMITS: 個人字典preview順位與live可能不同，guard會保留輸入；OEM/Binder/觸控尚未驗證；Adjacent非法tone組合顯示錯誤不silentrewrite；LGPL native散布source/relink義務未當成build已完成。

## STATUS / NEXT

CODE_GATE: VERIFIED_LOCALLY / READY_FOR_USER_RETEST
USER_TYPING_ACCEPTANCE: 先前FAIL_BY_USER_REPORT維持為未關閉問題，修復後復驗OPEN。
RUNTIME: NOT_TESTED
PHYSICAL_ACCEPTANCE: OPEN
PRODUCTION_RELEASE: NOT_READY
P1: 未宣告接受
M4: NOT_VALIDATED
NEXT: 用12手冊安裝新Debug、以可見注音測我／我現在／候選選字／真editor文字／英文字首補全。裝置可用且授權後再受控執行P0實機矩陣，保留每案證據；失敗用獨立targeted修復，不清使用者資料，不自動push/release/M4。
