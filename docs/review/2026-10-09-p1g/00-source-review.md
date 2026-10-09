# P1-G 來源與獨立初審

BASE：4e70acdb2a7ee9751ee295171cc7ef6fc65b1f2f；fixture6595b68403232187eba4d9ec10c7f0689c2b5b61、product75b93a3b68867fd6ec4c6adadd3e279d71e98104均存在。原P1-E工作樹乾淨，已建立隔離fix/p1g-acceptance-runner-safety；main不是P1基線。

必讀P1-F外部目錄：/Users/huang/Desktop/KU-Yin裝置驗收/2026-10-08-214315-P1F-盤點-abed8d1c/。六指定reports/JSON與evidence-manifest全部hash核對；P1-E07/08/09和runner553行、全部24既有tests已讀。原始裝置0、所有30case NOT_TESTED不更改。

獨立fresh reviewer p1g_initial_review重新證明F01 user欠缺、F02active subtype欠缺、F03log及select-success result OSError漏recovery、P01host None count、P02wrongowner/duplicate/no-start會PASS。已用純記憶體malformedprotocol與BrokenLog驗證，可開始限定TDD，不能操作裝置。source initial NEEDS_REVISION。

source-preservation.json保存protected來源逐byteSHA、四APK與v2ZIP大小/hash。baseline python3 -B -m unittest discover -s scripts/tests -p test_p1_acceptance_device.py：24/24 exit0；只fake subprocess。OCRv1.12.12本輪一次health exit1/HTTP401，BLOCKED_BY_PROVIDER，未重試或讀credentials。

路由實用：using-superpowers、deep-understanding、systematic-debugging、test-driven-development、writing-plans、code-work、code-verification、code-review-excellence、requesting-code-review、receiving-code-review、verification-before-completion、using-git-worktrees、source-driven-development、security-and-hardening、documentation-and-adrs。official-code-review已讀但本輪沒有PR，不聲稱完整PR流程；無視覺skills。

收束新增實用路由：finishing-a-development-branch（已讀，使用者已選local-only keep branch/worktree）；verification-before-completion再次以fresh101 portable／10contracts／frozenhash／independentreview核對。OpenCodeReview CLI health只一次失敗，未冒稱完整OCR skill scan。
