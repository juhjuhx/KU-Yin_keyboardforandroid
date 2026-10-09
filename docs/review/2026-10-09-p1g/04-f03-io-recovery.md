# F03：I/O 故障與有界復原

正常執行每個命令前先 durable intent log（flush/fsync）；寫入失敗不能啟動下一個 mutation。命令返回後 log I/O 失敗、select success-result 寫入失敗、instrumentation output 寫入失敗、timeout、KeyboardInterrupt 均進入 transaction cleanup；install 也保留 attempted/confirmed 清單與 partial installation 可能性，無 uninstall/clear。

cleanup 可在 log sink 已壞時繼續保存 memory recovery records，numeric user / identity / subtype 邊界不放寬。恢復失敗與原始失敗分別記錄；user 已切換時停止，不操作另一個 user。restore 只做一次有界 sequence／condition polling，不無限 retry。

結果寫入失敗：stderr 保留原因，mkstemp mode0600 fallback json + fsync。沒有 durable command proof 時 result=FAIL，即使狀態已復原也不聲稱 clean PASS。原始 captured-state 永不覆寫。

TDD：f03-red 10 tests，IO03（日誌壞於 instrumentation）與 IO06（select-result sink 壞）證明原實作 cleanup 缺口；IO02/04/05 故障觸發條件初次誤在 enable 後啟動，已改為實際 set KU-Yin 後啟動，避免把只需 disable 的合法 recovery 當錯誤。corrected focused 10/10 exit0。另 install-io-red 1 assertion fail，已補 install transaction。完整 safety-all-green 57/57 exit0。副本報告改用 select-failure.json，保留原24測的兩錯誤驗證意圖。

獨立安全反例：stderr.write 與 commands log 同時 OSError，原 cleanup 仍會被 console 阻斷。ConsoleFailureTests 先 assertion RED，再將診斷改 best-effort；durable failure report 與 identity-safe restore 不依賴 stderr。safety-final-green 61/61 exit0。

後續獨立證偽：run／explicit restore 建立 operation directory 失敗也需 cleanup，已先兩 subcase RED再提供讀取既有 capture + identity-safe recovery + private fallback；closed stderr 的 ValueError 同樣先 RED再best-effort。safety-gate-final 63/63 exit0。復原不可讀 user/state 時保留 manual_action_required，不猜測、不繞過capability。

stdout progress 入口同樣可能 BrokenPipeError；已用 ProgressConsoleFailureTests run/restore 兩 subcase RED，再改 progress 為best-effort。Console 輸出屬提示，primary/restore error 的 durable json/fallback 才是證據。safety-approved-green 64/64 exit0。
