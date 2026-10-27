# F2 本機原簽章測試候選

此分支只將版本設為20005／0.2.0-f2.1-test，避開公開Alpha20003及Draft PR20的20004。沒有新增輸入功能、變更native pin或signing config。

使用現有本機Debug keystore建置；預期公開憑證為46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94，須以產物apksigner實驗證。keystore不得進GitHub、CI artifact或一般封存。

僅TEST_ONLY，不是RC或可公開新Release。較高code／同cert不證明資料更新保留；必須在可拋棄、合成資料環境經正常install-r實測，另完成Runtime、實機、license/relink/privacy/QR gates。原Alpha及八資產保留。
