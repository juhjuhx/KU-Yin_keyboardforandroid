# P0-R 計畫／路由

- [x] 基線：85711bc fullSHA已核。它與75b93a3 product/debug/androidTest diff為空；使用最新descendant僅為重用受控editor/runner，不把它當新的decoder設計。
- [x] APK/source/worktree/export盤點，00及01。
- [x] pinned libchewing真host spike：證明候選查詢clear音節、no-tone／fuzzy APIs邊界；02/03/08。
- [x] 固定state ownership與穩定candidate identity／editor effects contract，04。
- [x] 先native RED（ㄨㄛ、ㄨㄛˇ、我現在兩路），最小GREEN；選字/完成commit regression。
- [x] service/editor actualRobolectricEditText boundary及debug-only synthetic trace；06。
- [x] 中文MVP後明確versionCode/name新Debug APK＋provenance＋manual/device矩陣。
- [x] 英文offline lexical provider RED/GREEN，sensitive field禁suggestions；07/08。
- [x] nearby/full JVM/static/bootstrap/build、獨立actualdiffreview、舊包hash／新包signer核對；05/09/10/11。

A路由：using-superpowers/deep-understanding/systematic-debugging/code-review-excellence/addyosmani-source-driven-development/open-code-review/requesting-code-review/verification-before-completion。
B路由：test-driven-development/code-work/addyosmani-api-and-interface-design/addyosmani-test-driven-development/addyosmani-incremental-implementation/addyosmani-security-and-hardening/receiving-code-review/verification-before-completion。
official-code-review已載入但沒有PR，標NOT_APPLICABLE_NO_PR；不post GitHub。只載入實際有用技能，不湊UI/行銷quota。未實際載入的後續路由不宣稱已用。

Device驗收仍OPEN；計畫中的manual/device矩陣完成的是測試準備，不是runtime PASS。Source commit/最終review/APK provenance收束以11為準。
