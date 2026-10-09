# F01 Android user identity

Current user由am get-current-user讀精確非負整數，prepare前後一致；durable state保存android_user_id與device_identity。同一serial／props但user不同拒絕。

每段identity重查、shell help逐命令user capability；settings／IME／install／instrumentation都數字--user，每個mutation前後checkcurrentuser；unsupported包括user0亦failclosed，不採implicitfallback。

RED：10tests/10 assertion failures，exit1（captureNone、wronguser仍install/select/run/restore、unscopedargv）。GREEN10/10 exit0，整合既有24共34/34exit0。log/json build/p1g-evidence/f01-*；既有fixture更新合法numericuser與scopedargv，原24保護意圖保留。無device。
