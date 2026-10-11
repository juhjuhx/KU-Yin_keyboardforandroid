# KU-YIN P1-G RUNNER SAFETY HANDOFF

## SOURCE

BASE_SHA: 4e70acdb2a7ee9751ee295171cc7ef6fc65b1f2f
FINAL_SHA: defac22fc72d78a314bd1d07c7468c9126a49d70（最終 runner/test source checkpoint；文件完成 commit 在此之後）
BRANCH: fix/p1g-acceptance-runner-safety
WORKTREE: /Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1g-runner-safety
PUSHED: NO
LOCAL_COMMITS: 7beb087856209467e02ac208f379d059eba68b19 → 3d5712a4c2f66b17b4e460d8aa79b81d0e917932 → defac22fc72d78a314bd1d07c7468c9126a49d70 → 文件完成 commit（以本分支 HEAD 核對）
PRODUCT_SOURCE_SHA: 75b93a3b68867fd6ec4c6adadd3e279d71e98104
FIXTURE_SOURCE_SHA: 6595b68403232187eba4d9ec10c7f0689c2b5b61

## REVIEW

F01: FIXED；numeric Android user + explicit command scope +前後guard
F02: FIXED；selected subtype capture + conservative fail-closed preflight +三欄還原核對
F03: FIXED；write-ahead proof +有界cleanup +雙錯誤／private fallback
P01: FIXED；5/26 exact count及canonical identities
P02: FIXED；start/status2/finish/terminal + source case owner + duplicate/malformed拒絕
PRODUCT_AND_HOST_FILES_CHANGED: 0（99受保護來源逐byte核對）

## ANDROID USER SAFETY

CAPTURE: current numeric android_user_id；prepare前後一致
PINNING: serial/device properties +同一numeric user
PRE_MUTATION_CHECK: 每次install/IME enable,set,disable/am instrument之前及之後重查current user
USER_SWITCH_BEHAVIOR: STOP；不對其他user還原，保留原capture/recovery evidence
MULTIUSER_LIMITATIONS: 每個命令必須具已核對numeric --user能力；無implicit fallback，secondary與user0均同樣fail-closed

## IME SUBTYPE SAFETY

CAPTURE: original_default_ime_raw / original_enabled_imes_raw / original_selected_input_method_subtype_raw / android_user_id；schema2 exclusive create
SUPPORTED_RESTORE_METHOD: 僅API35/36、原selected=-1、原IME唯一mId/detail block且唯一count=0；set原IME後重讀default/enabled/selected及user
UNSUPPORTED_PLATFORM_BEHAVIOR: BLOCKED_SUBTYPE_RESTORE_UNSUPPORTED；舊schema1亦不可mutation
POST_RESTORE_VERIFICATION: default/enabled（含subtype entries）/selected的實際值及identity吻合；原raw留存
FAIL_CLOSED_POLICY: specific/implicit subtype不可保證、未知API/OEM/duplicate/unreadable均拒絕prepare mutation；不寫secure、不造set-subtype、無root helper

## ERROR RECOVERY

PRIMARY_EXCEPTION: 獨立failure / exception_type，保留命令及attempted/confirmed install
RESTORE_EXCEPTION: restore.error / exception_type / manual_action_required，與primary分開
LOG_WRITE_FAILURE: 正常mutation前write-ahead fsync；cleanup不依賴log/operation directory/console可寫，仍受user/subtype guards
RECOVERY_RESULT: 可安全restore且狀態吻合才算state restored；proof缺失仍FAIL，private mode0600 fallback；原snapshot不覆寫

## PROTOCOL

EXPECTED_APP_TESTS: 5（4 classes；不可skip）
EXPECTED_HOST_TESTS: 26（3 classes；skip=INCONCLUSIVE）
EXPECTED_TEST_IDENTITIES: scripts/p1_instrumentation_manifest.json，actual Kotlin owner核對，8 source hashes
CASE_OWNER_MANIFEST: 26個source-owned B/C/D外層envelopes；D18內部觀察仍D18；明示多subcase契約才接受
CANONICAL_MANIFEST_SHA256: ccb2d32d62dd1aea5d84db2b7d53853c108b0f4dff99727dbe9d6c916fc9e8de
DUPLICATE_DETECTION: test start/finish/identity、case/subcase、bundle key、nested JSON key均拒絕；禁止last-key-wins抹FAIL
TERMINAL_VALIDATION: final code=-1唯一且完整、numtests/terminal/OK一致，無orphan/EOF/malformed；只消費active test的sendStatus2 stream
OBSERVABLE_VS_FULL: 保留B/C/D17及A03/A04缺口；fake正常sequence exit3，無full PASS

## RED EVIDENCE

COMMANDS: red-green-ledger.json（exact argv/exit/time/failed method/assertions）
FAILED_TESTS: USER_01–10；SUBTYPE_01–10（含OEM subcases）；IO03/IO06、install IO、console/mkdir；COUNT_04；PROTO_02/03/04/05/06/07/10、outside emit；後續manifest/wire/payload反例
EXPECTED_FAILURES: 原unsafe缺capture/錯user仍mutate、缺selected/無fail-closed、cleanup跳過、替換identity/錯owner仍PASS；全assertion RED，非syntax/import假RED。F03觸發條件修正詳04

## GREEN EVIDENCE

RUNNER_TESTS: 101/101，0failure/error/skip；repo與v3解壓後均exit0，portable global subprocess禁器外部calls=0
F01: USER_01–10 PASS + exact help負面cases
F02: SUBTYPE_01–10 PASS + duplicate/malformed mId/count PASS
F03: IO_01–10 PASS + install/mkdir/closed stdout/stderr recovery PASS
P01: COUNT_01–08 PASS + source/manifest integrity及actual run wiring
P02: PROTO_01–12 PASS + malformed control/status2/duplicate JSON/key/owner反例
CONTRACTS: 10/10，python3 scripts/verify.py --static exit0
BUILD_GATES: native bootstrap exit0；app40類190測+host3類37測0failed/error/skip；Debug/兩instrumentation APK編譯exit0；Release exit1（既有my-upload-key.jks缺失），未簽章/發布
BUILD_SOURCE: 新build hash與frozen可能不同，僅gate證據；包中固定四原APKs，不聲稱bit-reproducible
LSP_DIAGNOSTICS: TOOL_NOT_AVAILABLE；以實際Python import/tests、Android compiler及static contracts驗證

## ARTIFACTS

FOUR_APK_HASHES_UNCHANGED: YES
APP: b223620743831e0370a31a65dcc998e6a74dfe70d45e020036c4f8b4468e39c9
APP_TEST: 3938b3e3c1fb711ed6141d491a3841839e098dfb2c2ae81924b94f0d6106b8bd
HOST: 8e53e8933e1b436b3192dab4319a7da1fe58e98afba038447924f51e1481d829
HOST_TEST: 5db907bd2e131742ce817139785d2f4a9a5cb7254ccd13839bfcbe6eb6277bd2
DEBUG_SIGNER_SHA256: 46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94（四檔fresh apksigner/aapt exit0）
V2_ZIP_UNCHANGED: YES；787d44f788b172dad942087fcffb2e34e1bdad8bfcdeebb7a02b086ab5a3e65d
V3_ZIP_PATH: /Users/huang/KU-Yin_keyboardforandroid/.worktrees/p1g-runner-safety/build/p1-alpha-acceptance-v3-safety/KU-Yin-P1-Alpha驗收包-v3-safety.zip
V3_ZIP_SHA256: 4dffdeb1aeaa9d0e24000f75f54f45090c7bf33a2d1a3dde90e95d33e03da816
V3_ZIP_BYTES: 27688836；37 entries；CRC/全部SHA256SUMS PASS
V3_PORTABLE: extracted 101/101無skip、0 external subprocess，four frozen identities PASS；本handoff在ZIP之外避免self-hash循環
BINARY_COMMITTED: NO

## OPENCODEREVIEW

VERSION: v1.12.12
STATUS: BLOCKED_BY_PROVIDER
SESSION: NONE（request_id=null）
FINDINGS: 本輪一次ocr llm test exit1，Anthropic HTTP401 invalid x-api-key；未重試、未讀/改credential、未聲稱OCR PASS

## INDEPENDENT_REVIEW

STATUS: APPROVE（source/fake safety及v3 package-only獨立核對，詳07）
REMAINING_FINDINGS: 無未修復High/Medium。review source SHA256 5d15c9d28007de5f9d3de3c7b91216190fde82d5b318388c85649b02ddde24a5；獨立101測+37protocol checks，0 external calls

## RUNTIME

DEVICE: NOT_USED（本輪沒有inventory/install/select/run實機命令）
PHYSICAL_RUNTIME: NOT_TESTED
PHYSICAL_ACCEPTANCE: OPEN；30 cases未取得本輪裝置證據
M4: NOT_VALIDATED
PRODUCTION_RELEASE: NOT_READY

## FINAL RULING

READY_FOR_DEVICE_PRECHECK（僅runner source gate；不保證任何具體裝置通過user/subtype capability）

## NEXT

P1 ALPHA PHYSICAL-DEVICE EXECUTION。先核v3 bytes/身份、明示授權serial、通過保守preflight，再install/select/app5/host26/restore與剩餘scope evidence審查。拒絕預檢時另做有界平台評估，不放寬安全條件。STOP，無push/release/M4。
