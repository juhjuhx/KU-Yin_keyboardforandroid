# 使用者回報與裁定

PRODUCT_TYPING_ACCEPTANCE=FAIL_BY_USER_REPORT。既有 JVM／合約／建置結果只保留其實際測量範圍；沒有實機證據不能解除此裁定。

| ID | 回報 | 必須證明 |
|---|---|---|
| P0-01 | ㄨㄛ沒有無聲調候選 | 真 pinned JNI 給出可選的我，聲調仍能精化 |
| P0-02 | 我現在無法連打 | 有／無聲調完整原生組字與選取，未完成尾音不丟失 |
| P0-03 | UO、C變Z | 分離可見注音、Dachen ASCII、原生 pending/preedit；不猜測口述ASCII |
| P0-04 | 候選沒有進入各App | 可見候選點選→正常 InputConnection→實際 EditText；失敗值可觀察 |
| P0-05 | 英文沒有字庫 | 真離線詞頻、prefix、選詞／Space／Backspace／大小寫／敏感欄位 |

已知鍵位：ㄨ=j、ㄛ=i、ㄒ=v、ㄧ=u、ㄢ=0、ㄗ=y、ㄞ=9。literal U/O/C/Z 與注音不是同一表示。沒有使用者畫面或裝置 byte proof 前，不把口述映射當已確認 defect。

實機仍 NOT_TESTED；新回報是 release blocker。M4 暫停，不推送、不發佈。
