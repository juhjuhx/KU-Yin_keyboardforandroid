# KU-YIN AIE V1 ENGINEERING REVIEW HANDOFF

## Source of truth

BRANCH: docs/aie-v1-engineering-review
WORKTREE: /Users/huang/KU-Yin_keyboardforandroid/.worktrees/aie-v1-engineering-review
BASE_HEAD: 3072affee95fa7fa8b0016ad85aac322272e4dea
PRODUCT_SOURCE: 9f5db676a17b5770a738b5c20da937f9211e5a34
FINAL_DOCUMENTATION_HEAD: 用 git rev-parse HEAD 取得本交接之後的文件commit，不在檔案自填自引用hash。
P0_APK: /Users/huang/Desktop/KU-Yin-P0R-2026-10-10/KU-Yin-P0R-core-typing-debug.apk
APK_SHA256: 14082a60527ef57542d62cfb2bfaac1ed53a99080fb5af7f0de2792862fe260f
VERSION: 20002 / 0.2.0-p0r.1
BASIC_TYPING: WORKS_ON_USER_DEVICE（使用者回報，沒有核手機base.apk）
PRODUCT/TEST/GRADLE/RUNNER/AGENTS_FILES_CHANGED: 0
PUSHED/MERGED/PUBLISHED: NO

## 先讀這些文件

1. [深讀與實際流程](../research/AIE_V1_DEEP_UNDERSTANDING.md)
2. [問題台帳／隔離重現](../research/AIE_V1_ISSUE_LEDGER.md)
3. [候選 UX 三方案／待批准 ADR](../adr/0006-aie-candidate-ux.md)
4. [提案架構](AIE_V1_ARCHITECTURE.md)、[tree](ARCHITECTURE_TREE.md)、[flow](DATA_FLOW.md)、[state machine](STATE_MACHINE.md)
5. [Roadmap](../roadmap/AIE_V1_ROADMAP.md)、[TUI](../roadmap/ROADMAP_TUI.md)
6. [Upstream逐檔授權矩陣](../research/UPSTREAM_REUSE_MATRIX.md)、[隱私矩陣](../research/AIE_V1_PRIVACY_MATRIX.md)
7. [Test matrix](../testing/AIE_V1_TEST_MATRIX.md)、[效能測量計畫](../performance/AIE_V1_BASELINE.md)、[Release gates](../release/RELEASE_READINESS.md)
8. [實際技能路由](SKILL_ROUTING_MATRIX.md)、[狀態](AIE_V1_STATE.md)、[新鮮核對證據](AIE_V1_REVIEW_EVIDENCE.json)

## 重點裁定

- 候選pool與viewport分開：native預設page10、去重後顯示更少；UI未接頁面／展開，不能靠LazyRow抵達全pool。
- 無界preedit與工具共佔44dp列；長句/字級量化是待寫RED，不宣稱已實機重現。
- 空白personal preview與nativefrequency-sort預設false使顯示排名未直接利用nativelearning；不能簡單開live cand_open補洞。
- 已確認HIGH：設定reset只清fallbackprefs，native個人字典仍保留；沒有刪真資料，修正另片。
- 觸控是clickable，不是adaptive probabilities；既有semantics tests不能當真座標hitproof。
- symbols39/emoji40硬編碼；無productionsearch/kaomojicatalog。community/donation/credits全保留；QR hash圖不能聲稱可掃。
- 保留libchewing/nativepins；沒有GPLcopy、新decoder、SQLite/migration、touch資料或model。

## Verification / review

FRESH_STATIC: python3 scripts/verify.py --static，10/10 exit0。
FRESH_SCOPE: 產品／測試／infra／AGENTS diff空，11既有worktrees HEAD/status等於開始快照；P0APK hash一致。
FRESH_DOCS: JSON parse、相對Markdownlinks、code fences與git diff --check。
HISTORICAL_ONLY: app229/host37/Python104及native/Debug gate來自P0-R，未在本輪重跑。
GRADLE/NATIVE/DEVICE/PERFORMANCE: NOT_RUN（review-only，沒有新artifact）。
OCR: v1.12.13(fabbdb29)，網路可用的一次health401 invalid x-api-key，BLOCKED_BY_PROVIDER；request req_011Cftij3vNu1bC3An5JZVXj，session NONE。
INDEPENDENT_REVIEW: PARTIAL；三個GPT-6.1 SOL/max read-only reviewers交出具體定位findings後usage-limit中斷，無final approval。主代理逐項讀碼核對，沒有冒稱完整獨立簽核。

## Decision checkpoint

已送出單一候選UX問題：推薦單列＋展開當前頁網格（展開暫代主鍵區、可收合、前後頁、preedit保留）；替代常駐兩排或可配置區域。

USER_ANSWER: 尚未收到。FIRST_APPROVED_SLICE=NONE；ADR0006=PROPOSED。預選不等於批准；先收答案再做S1 accepted spec/executable plan。下一群才討論learning privacy／touch consent與correction；license／release／symbol也未決，不一口氣問表單。

## Overall status

ENGINEERING_REVIEW: REVIEW_READY_WITH_FINDINGS
IMPLEMENTATION: NOT_STARTED_PENDING_UX_DECISION
FULL_PHYSICAL_ACCEPTANCE: OPEN
PUBLIC_RELEASE: NOT_READY
M4: NOT_VALIDATED
NEXT: 回覆候選UX問題，review深讀報告與S1方向；批准後TDD、獨立review、新可辨Debug與實機驗證。
