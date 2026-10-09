# P1-G v3 裝置預檢與執行手冊

本輪 DEVICE_MUTATION=NONE、PHYSICAL_RUNTIME=NOT_TESTED、30 cases仍 NOT_TESTED。本手冊取代 v2 runner 的裝置執行入口；保留 v2 為歷史證據，不用它執行新的驗收。v3 source gate 為 READY_FOR_DEVICE_PRECHECK，個別裝置仍可能被安全預檢拒絕。

## 必要前提

由裝置擁有者明示授權安裝四 APK、暫時啟用／選取 KU-Yin、synthetic test text及還原原 IME。明示唯一 serial，不選第一台，不自動啟動 emulator；physical acceptance須實機。先核對ZIP外部sha256與包內SHA256SUMS.txt，再核四 APK manifest/signer identity。

原使用者必須在整個 session 維持相同 numeric Android user。prepare 固定 current user、serial/model/API/ABI/fingerprint、原 default/enabled/selected subtype。所有 settings、IME、install、instrumentation 均明示 --user ID；mutation 前後重查 current user，切換後停止後續操作與其他 user 的還原。

### Subtype fail-closed

僅 API35/36、已核對各命令 numeric --user 能力、原selected=-1且原IME唯一詳細block有mId與count=0，才能預檢READY。其他API、specific subtype hash、有任何subtype、不可讀／重複／OEM格式未知，均 BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED，不能install/select/run。這是restore可證明性限制，不是KU-Yin產品缺陷；不寫secure settings、不造set-subtype、不加root helper。若預檢拒絕，停止並另做有界能力評估，不能改安全條件取得PASS。

## 唯一執行入口

解壓v3後進入含四APK、artifact-manifest.json、scripts/的根目錄。下面是日後授權裝置範例，**本輪未執行**。serial與外部新目錄必須替換；不能重用有captured-state.json的目錄。每段都明示package-dir，勿使用runner歷史repo-layout預設路徑。

```bash
export KUYIN_SERIAL='實際已授權唯一serial'
export KUYIN_PACKAGE_DIR="$PWD"
export KUYIN_EVIDENCE_DIR='/Users/huang/Desktop/KU-Yin裝置驗收/實際日期-序號-新session'
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$KUYIN_PACKAGE_DIR" --evidence-dir "$KUYIN_EVIDENCE_DIR" prepare --device-kind PHYSICAL
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$KUYIN_PACKAGE_DIR" --evidence-dir "$KUYIN_EVIDENCE_DIR" install
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$KUYIN_PACKAGE_DIR" --evidence-dir "$KUYIN_EVIDENCE_DIR" select
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$KUYIN_PACKAGE_DIR" --evidence-dir "$KUYIN_EVIDENCE_DIR" run
```

逐段只在exit0後繼續。prepare只讀且exclusive保存schema2原狀態；BLOCKED capture不可拿來mutation，舊schema1亦拒絕。install四檔順序app/app-test/host/host-test，-r -t --user保留資料；signature mismatch STOP，無uninstall／pm clear。partial install保留attempted/confirmed證據，不自動移除APK。

select顯式改IME，成功會暫時保留KU-Yin。run先既有5測，只有完整5個canonical identity且無skip/failure才進host26測。host必須26個canonical identity、完整start/status2/finish/terminal與case owner；malformed／duplicate JSON keys／bundle key／emit／identity substitution均拒絕。儀表測試只點可見KU-Yin soft keys，沒有hardware Enter替代。

## 還原與故障

run finally及install/select錯誤嘗試有界restore。explicit restore可不依賴APK或manifest仍在原處，但必須使用同serial與captured user：

```bash
python3 scripts/p1_acceptance_device.py --serial "$KUYIN_SERIAL" \
  --package-dir "$KUYIN_PACKAGE_DIR" --evidence-dir "$KUYIN_EVIDENCE_DIR" restore
```

核對原default、enabled含subtype entries、selected subtype三欄與user；default相同而selected不同不是PASS。只set原IME、必要時disable本輪新增KU-Yin，不覆寫整串設定、不清字典。第三方狀態變更、identity/user不符、原IME缺失，拒絕覆蓋，保留manual_action_required。若user已切換，先由擁有者回到原使用者再重新預檢restore，不能直接還原別人的狀態。

命令正常路徑先write-ahead log+fsync；log／operation dir／結果寫入失敗不繞過cleanup，console提示也是best-effort。原始與restore失敗分開保存。結果不可寫時private mode0600 tempfile fallback；stderr印FALLBACK_EVIDENCE路徑，兩sink皆壞時需人工核對durable證據。狀態雖恢復但command evidence缺失仍FAIL，不宣稱clean PASS。不可因finally已進入就認定restore成功。

## 裁定與剩餘範圍

run：exit0僅完整acceptance+restore PASS；exit2 suite/protocol/case/restore FAIL；exit3完整sequence已跑但full scope INCONCLUSIVE/NOT_TESTED。其他子命令STOP exit1。case只用PASS/FAIL/INCONCLUSIVE/NOT_TESTED，observable/full分開。

完整30列與人工範圍見附帶docs/review/2026-10-07-p1e/09-device-matrix.md。A03真旋轉、A04安全fault fixture、native learning observer、B方向/第二寬度/semantic context、TYPE_NULL、D17 oneway Binder同步false缺口仍未豁免。正常fake或日後局部device PASS不能當完整Alpha PASS。人工新session也需select/restore邊界，只用synthetic內容，不蒐集個人通知、訊息或廣泛logcat。

離線回歸：`PYTHONPATH=scripts/tests python3 -B -m unittest discover -s scripts/tests -p 'test_p*.py' -v`。包內含完整8份instrumentation來源hash證據，沒有以compile成功充當runtime證據。M4 NOT_VALIDATED、production release NOT_READY；無push/release。
