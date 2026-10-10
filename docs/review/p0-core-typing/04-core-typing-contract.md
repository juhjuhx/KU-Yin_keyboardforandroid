# P0-R 核心輸入契約

先修原生中文，再接 editor、英文；不換 pin、不新增decoder、不改鍵盤幾何。

1. 實際event code與可見注音一一對應。native owns syllable/phrase；UI不修正key，preview不改live context。
2. ㄨㄛ→我必須可選（不規定排名）；ㄨㄛˇ可選。我現在有聲調／無聲調連續輸入不能丟尾音。預覽可使用原生scratch context，但不得把猜測文字當decoder輸出。
3. Candidate identity是同snapshot的index及generation，不以顯示字串indexOf取代。native選取是explicit mutation；核對前不可盲用scratchindex。失敗保留輸入。
4. 候選點選完成原生replacement後送出一次；already-committed engine result不得再commit。Space/Enter/標點保持ordered effects；組字Enter不附帶editor action/newline。
5. Service owns InputConnection effects：正常setComposingText投影、commitText acknowledgment、finishComposingText。自身composing選取通知不得Reset；外部游標或editor change重置。commit false不可算成功，不自動重送造成double effect。
6. 英文獨立wordbuffer+離線prefix provider；選詞commit整詞，Space恰一分隔；Complete先完成word。敏感／forceASCII／非文字，以及 `TYPE_TEXT_FLAG_NO_SUGGESTIONS`／`TYPE_TEXT_FLAG_AUTO_COMPLETE` 禁用英文 dictionary suggestions，不學習、不連網。後兩個旗標不禁用必要的中文注音→漢字轉換；conversion 與英文預測分開正規化。
7. 真JNI gate、Robolectric真EditText gate、Android device gate分層記錄；前兩者不冒充實機。
8. 新APK versionCode/versionName可區分，同appID及debug signer，原v1/v2/v3 bytes不改。無装置提供手動腳本，保留產品驗收OPEN。
