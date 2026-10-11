# F02：selected subtype 與 fail-closed 預檢

原 runner 只保存 default/enabled；enabled subtype 設定不代表目前 active subtype。新增 user-scoped selected_input_method_subtype raw、numeric user 與 capability evidence。prepare 專用 exclusive captured-state schema 2；BLOCKED 快照不可拿來 install/select/run/restore。

本輪未建立通用 subtype setter。AOSP shell ime set 使用 NOT_A_SUBTYPE_INDEX；-1 在有 subtype 的 IME 仍可能挑 implicit subtype，不能單看 sentinel 就承諾還原。安全路徑僅接受已研究 API 35/36、原 IME 唯一詳細 block、mId 一致、InputMethodSubtype array: count=0 且原 selected=-1；此時沒有可選 subtype，重新 set 原 IME 後仍逐一重讀 default/enabled/selected 與 user 驗證。specific hash、任何 subtype>0、不可讀、OEM 格式未知、其他 API 均 BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED，不寫 secure settings、不新增 root/helper。

AOSP 來源（REFERENCE_ONLY，未拷貝外部程式）：
- [Android 16 InputMethodInfo.dump](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/core/java/android/view/inputmethod/InputMethodInfo.java)：count 欄位。
- [Android 16 InputMethodBindingController](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/services/core/java/com/android/server/inputmethod/InputMethodBindingController.java)：zero subtype 回 null。
- [Android 16 InputMethodManagerService](https://android.googlesource.com/platform/frameworks/base/+/99b01a65cc4c104933788b3143285ab6bae65827/services/core/java/com/android/server/inputmethod/InputMethodManagerService.java)：shell set 不接受 subtype index。
- [Android 15 InputMethodInfo](https://android.googlesource.com/platform/frameworks/base/+/4e43ad12e1b211f15152e9c5b16b0fe88e6b93f3/core/java/android/view/inputmethod/InputMethodInfo.java)。API24/34 dump 沒有這個 count 欄位，因此拒絕，不推測。

READ-ONLY 研究由 p1g_android_contract reviewer 核對固定 revision。一般 shell help 只對 exact command synopsis 計算 --user；install-existing 不可代替 install。ime list 的 --user 在 API35/36 source profile 支援，實際 detailed block/identity/user 再核對；解析不明即拒絕。

TDD：f02-red 10 methods / 12 assertion failures（OEM format 含三個 subcases），exit1；f02-green 10/10 exit0；與原24+F01 合計44/44。全部 fake subprocess，沒有裝置相容性/實機PASS宣稱。

獨立反例補強：--user-profile 不算 --user、install-existing 不算 install、count=0 加 malformed duplicate 欄位及空 mId duplicate 全部先 RED 後拒絕。safety-review-final 60/60 exit0；只用 memory/fake ADB。
