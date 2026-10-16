# R2.1 逐案裁定

ACTIVE；R2 d6最終raw重新核對，不回填歷史03ac結果、不把SKIP改名。R2.1 source `b1b5bf6b3939fef7e62893d833b0853e0a08ae7d` 有限B01 runtime另列。

## R2 最終HEAD直接證據

Build [38761725033](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/38761725033) checkout actual merge `3b5e0845c0c83d145a26aa18803ee9e0dc98a04e`；parents main63c9＋d6bf2，tree04bd06fb2c13b012baeeceb0015e2ec577605da7與d6相同。App exact5＝5PASS；Host exact33＝24PASS／0FAIL／9SKIP。32case envelopes＝23PASS／9INCONCLUSIVE，另1infra PASS。start/end、current1..33、owner、每case唯一status2、terminal-1均核對；console41/33／Finished42不是identity計數來源。

| case | 已直接觀察 | 缺失／完整裁定 |
|---|---|---|
| B01 | 當前41鍵與ㄕ/ㄖstrictpreedit；clear後文字/commit/action=0 | 第二width/方向未執行；INCONCLUSIVE，R2.1新測試待驗 |
| B02 | 候選你commit1，續ㄅ後editor你ㄅ、commit仍1/action0 | live native context/no reset未知；INCONCLUSIVE |
| B03 | 兩音節不提前commit；候選你好commit1，續ㄅ後你好ㄅ／commit1 | context、修正/Enter與語義未證；INCONCLUSIVE |
| C01 | inputType2，temporary ENGLISH／注音阻擋；editor effects0 | native autoLearn/persistence未知；INCONCLUSIVE |
| C02 | inputType3，同上 | native autoLearn/persistence未知；INCONCLUSIVE |
| C03 | inputType4，同上 | native autoLearn/persistence未知；INCONCLUSIVE |
| C04 | platform/final inputType0、真nullIC，最後只editor-ready | softkey／mode／native均未證；BLOCKED_SELECTOR／INCONCLUSIVE |
| C05 | 兩preferred經nontext後返回text可見模式恢復 | native readback與C04中間狀態未證；INCONCLUSIVE |
| D17 | SEND4恰1、host真false、text/commit/newline delta0 | 額外同步IPC false契約需裁定；仍INCONCLUSIVE |

C04：82captures／81attempts／81completed、last_node_count=0、stale0。**沒有每份成功capture的內容，不可聲稱81份逐筆全空。** R2歷史文檔的較強用語應以本輪證據界線校正；未改歷史forensic檔。全Host stale總76是observation metric，不由recent ring推所有phase，也不當performance結果。

## R2.1 B01

有限run [38825146067](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/38825146067)：source b1b5bf6，App完整5前置→原B01 identity，scope FOCUSED_DIAGNOSTIC_NOT_ACCEPTANCE。attempt1 SDK包準備失敗未跑測試；唯一重跑attempt2 App4PASS1firstVisibilityFAIL，Host/B01未執行。已核App5完整protocol、185source hashes及五真failure dumps；B01=NOT_TESTED，完整gap仍INCONCLUSIVE，不把123unit/compile當runtime。

B01若通過，只閉合當次emulator的portrait/landscape兩width及輸入子契約；不宣稱physical rotation／OEM／所有尺寸。其他八案原assertions/skip均未修改。不能用一個新B01＋舊23案拼成一次fresh fullHost執行。

詳細12欄需求證據表：[gap ledger](R2_1_ACCEPTANCE_GAP_LEDGER.md)；[native ADR](R2_1_NATIVE_OBSERVABILITY_ADR.md)與[平台限制](R2_1_ANDROID_PLATFORM_CONSTRAINTS.md)均未批准product/JNI實作或scope reduction。

## 新阻擋

[App binding failure](R2_1_APP_BINDING_FAILURE.md)：此輪show前focus/token/真IC都具備，timeout後仍remote method/session null；不能沿用舊early-show原因。根因未定，原App assertion保留、Host正確未跑，停止本輪實作擴張。R2d6完整suite計數與R2.1前置FAIL分開。
