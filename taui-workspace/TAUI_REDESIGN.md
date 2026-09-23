# TAUI Workspace 重設計凍結版（2026-09-23，用戶已拍板）

> 對象：`/Users/huang/Desktop/taui-workspace-standalone-handoff.html`（86KB／1503行／零依賴）。
> 鐵律：風格動前先過人——本檔即凍結決策；改檔按 §3 切片執行。

## §0 凍結決策

- 美術：**延續 TAUI 微調**（守 contract antiGoal，不拼貼）。
- 外部庫：**零運行時**，只借 pattern 手寫原生 CSS/JS（MagicUI MIT／Aceternity 免費可商用／
  ReactBits MIT+CC 不可轉售／shadcn MIT／RAC Apache-2.0——整套採用＝框架遷移，違規則#5）。
- 後端：**定 JSON 契約＋fetch 層**（現全靜態 mock）。
- 動畫：**功能性微動效**（觸發式過渡，ambient 零）。
- Design Read：redesign-preserve 專業工具工作區，calm industrial-tactile，TAUI contract＋原生 CSS。
- 三旋鈕：`5 / 3 / 4`。

## §1 審查發現（改前基線）

- F1 eyebrow 氾濫：`.eyebrow`＋`.section-kicker` 共 6 處（ACTIVE PROJECT／下一步／TODAY／CONTEXT×2＋sheet），
  超 taste-skill 上限（每 3 節最多 1）。砍至 ≤3。
- F2 字形圖示：⌕⌂▱▶✦▤⚙◐◉ 全系 Unicode，跨字體不一致；contract 要 Fluent 系單一語法。
  解：內聯 SVG sprite（單一 1.5px 筆畫），零運行時。
- F3 動效缺口：motion tokens 就緒但僅 switch／press 在用；command dialog、progress、
  inspector 無進場過渡（contract §25 要求 edge-origin＋dialog .98→1）。
- F4 缺狀態範例：無 skeleton／empty／error（contract §36–38 要求）。
- F5 command 檢索：子字串 `includes`，contract 要 fuzzy intent→換 subsequence matcher（約15行）。

## §2 改動規格（按 contract §51 切片）

> Slice A 已落地（原檔備份 `taui-workspace-standalone-handoff.backup-20260923.html`）：
> eyebrow 6→3、10 符號 SVG sprite 置換 21 處字形、skip-link＋main id、死 CSS 清除；
> 驗證：殘留字形 0、標籤配平全 OK、+3.8KB。color-mix 基線 2023＋全現代瀏覽器，不做降級。
> Slice B 已落地（2026-09-23）：dialog 進場（.98→1＋4px 上浮，240ms token）＋
> progress width 過渡（密閉重繪，不擾兄弟佈局）＋destination／attention-row／三按鈕 hover
> transition；keyframes 僅 1 組、marquee 0；reduced-motion 全域 1ms kill 覆蓋全部。
> skip-link 的 top 位移僅 focus 瞬間且 absolute，不觸發內容重排。
> Slice C 已落地（2026-09-23）：`TAUI_DATA` 契約（progress／phases／attention／system）＋
> fetch 渲染層（`window.TAUI_DATA_URL`，無則 mock fallback＋「本機預覽」徽）＋
> skeleton（progress shimmer＋列表降透明度）／empty（沿用 command-empty 樣式）／error
> （fallback＋徽標，明文字非唯色傳達）；全 textContent 無 innerHTML；`node --check` 通過。
> 瀏覽器開檔目檢（渲染一致／三態／dark）待手邊確認。
> Slice D 已落地（2026-09-23）：command 檢索換 subsequence fuzzy（寬容子序列語義，
> 排序沿用既有首項高亮＋箭頭導航）＋direction-aware hover（事件委派，扛重繪；120px 品牌光暈，
> opacity 過渡）＋project-state shine-border（靜態 conic sheen，零動畫）；招牌限兩處。
> `node --check` 通過＋matcher 語義實測通過。

- Slice A（CSS/HTML 優化）：F1＋F2＋skip-link＋`color-mix` 降級註記；單檔維持（standalone 可攜優先於拆分）。
- Slice B（動效）：dialog scale／progress width transition／inspector edge slide；全 gated
  `prefers-reduced-motion`（既有全域 kill 保留）；marquee 零。
- Slice C（前後端鏈結）：`TAUI_DATA` JSON 契約
  `{project,progress,phases[],attention[],system{},inspector{}}`＋fetch 渲染層＋
  skeleton／empty／error 三態；無後端時 mock fallback（現寫死數據轉為預設值）。
- Slice D（fuzzy＋借鑑 pattern）：subsequence matcher；direction-aware-hover（Aceternity pattern，
  手寫約20行）僅用於 attention-row；shine-border（MagicUI pattern，純 CSS）僅 project-state pill。
- 驗證：contract §52 全項（320px 無橫溢／200% zoom／鍵盤全流程／dark／reduced-motion／
  無 card farm／單一圖示語言）。

## §3 Skills 矩陣（本軌道實際使用 9）

visual-atelier（總控）＋design-taste-frontend（三旋鈕＋反 AI-tell）＋ui-ux-pro-max
（a11y／觸擊／動效時長覆核）＋frontend-design（美學方向）＋brainstorming（設計門）＋
baseline-ui（基線）＋brand-guidelines（品牌一致）＋fixing-motion-performance
（transform/opacity＋reduced-motion）＋OpenCodeReview（改完終掃）。
排除：Three.js×9／agnes-vision／fb-marketplace／career-ops-search（域無關）；
pro-*／brooks-*／pr-review-toolkit／receiving／requesting（未安裝）；
baoyu-compress-image（檔內零點陣圖）；image-to-code（無參考圖輸入）。
