# AIE V1 隱私政策矩陣

狀態：現況＋待批准建議，不新增privacy defaults。來源 EditorPolicy:34–65、NativeLearningPolicy、EnglishWordSession與manifest。

| 情境 | 中文conversion | 英文completion | 目前native寫入 | AIE proposed learning/touch |
|---|---|---|---|---|
| 普通文字 | 允許 | 允許 | allowPersonalizedLearning=true | 明確動作＋成功ACK，使用者default決策待定 |
| text/web/visible/number password、PIN(numberpassword) | 禁 | 禁 | 禁 | 任何store/recents/context/calibration皆不寫 |
| NUMBER/PHONE/DATETIME/TYPE_NULL | 禁 | 禁 | 禁 | 不記學習；TYPE_NULL evidence可INCONCLUSIVE |
| FORCE_ASCII | 禁 | 禁 | 禁 | literal，不保留context |
| NO_PERSONALIZED_LEARNING | conversion仍可 | completion仍可 | 禁 | 禁新增個人紀錄；是否讀既有personaldata與private mode另定，不混旗標 |
| NO_SUGGESTIONS | 保留必要conversion | 禁 | 現在仍依普通learningpolicy | 不默認當成NO_PERSONALIZED_LEARNING；新增touch/ranker policy需ADR |
| AUTO_COMPLETE | 保留必要conversion | 禁IME英文completion | 現在仍依普通learningpolicy | editor completion與IME預測分開 |
| private/incognito | 無現有user toggle／不可靠通用偵測 | 同左 | 明示NO_PERSONALIZED_LEARNING時禁 | 提議可見private toggle、斷context、不讀寫個人rank、不存recents，待批准 |
| OTP | 非文字禁；普通文字無可靠通用OTPflag | 依actualmetadata | 依actualpolicy | 不用欄位名稱猜測；manual private control／appcontract需驗證 |

推薦touch calibration明確opt-in、預設不保存raw trajectories；只留bounded/decayed aggregate且有reset。個人輸入replay與export另有內容敏感性，不能用現有錯誤log或dictionaryhash充作consent／disabled proof。

native preview :memory:沒有personal read/write；live可以讀既有userdict但政策禁寫。native reset現有HIGH落差須先修，privacy控制不能只清fallback就通知成功。
