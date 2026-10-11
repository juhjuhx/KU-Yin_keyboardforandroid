# P0-R 真機測試矩陣（尚未執行）

DEVICE=NONE；RUNTIME=NOT_TESTED。新 CoreTypingAcceptanceTest 六個方法使用現有 external host、EditorInfo/InputConnection、UIAutomator 可見軟鍵。沒有hardware keyevent或direct native替代裝置輸入。

| 測項 | 可執行證據 | 裝置結果 |
|---|---|---|
| ZH01/02/03 | 無tone/第三聲→點我→actual editor我、commit delta1 | NOT_TESTED |
| ZH04/05 | 無tone/有tone 我現在 →點整句→actual editor我現在 | NOT_TESTED |
| ZH06 | 點我後再打現在→actual editor我現在，兩次commit | NOT_TESTED |
| EN02/03 | hell→hello候選→actual editorhello、commit delta1 | NOT_TESTED |
| ZH07–09 | 真JNI unit gate +手動Backspace/repeat/tone | NOT_TESTED |
| ZH10–12 | 手動Space/Enter/標點，觀察commit/action delta | NOT_TESTED |
| ZH13/14 | 手動switch/editor/caret、自身selection不丟字 | NOT_TESTED |
| EN01/04–07 | 純session gate +手動大小寫、Space、刪字、password | NOT_TESTED |
| DEVICE01–04 | 全部以真editor文字/次數為準；另一editor人工 | NOT_TESTED |

不要用凍結v3 runner驗證新P0 APK：它固定舊hash與26-testmanifest，且舊B02/B03的candidate-select保留組字契約已被本次使用者『點候選要入editor』需求取代。舊APK／舊testAPK／舊manifest保留不改，不能把那些expectations改成PASS。

未來以明示serial/user、owner authorization、IME state備份／可復原preflight後，單獨執行 -e class io.github.juhjuhx.kuyin.acceptancehost.CoreTypingAcceptanceTest。此class不enable/select IME，沒有清資料；setup要求KU-Yin已選取。測試僅編譯完成≠六案PASS。
