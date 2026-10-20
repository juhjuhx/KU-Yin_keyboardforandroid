# KU-Yin 社群發佈：素材、渠道與驗證清單

**基準**：2026-10-20。目標是在不誇大產品狀態的前提下，公開介紹現有 **Experimental Alpha `v0.2.0-aie.1`**。此文檔是操作準備，不代表已貼至 Threads、Facebook、Instagram、B站或 LinkedIn。

## 目前的官方產品身分

- GitHub 主線：`63c9e4ab8ad7e277013acb5d7cce99228ec1f827`。
- 現有可下載：https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases/tag/v0.2.0-aie.1
- Draft PR #20：R3 `0.2.0-rc.1` candidate 工作，未合併／未發布。
- R3.1 分支 `fix/r3-1-h1-capture@feab29f53583c4bb8dfc4cffe13243a36a3e9d06`：Python 212、contracts 10，H0 HEALTHY、H1 INCONCLUSIVE（未取得有效 XML）；H2、App、Host／實機皆未執行。
- 公開介紹可以寫「核心功能已實作，開放 Experimental Alpha」；**不可說 RC 或 Stable 已發布**。
- 詳細文字稿：`2026-10-public-alpha-launch-kit.md`。
- 履歷／作品集：`PORTFOLIO_CASE_STUDY.md`。

## 素材需求與檔名規格

| ID／建議檔名 | 比例／解析度 | 真實來源 | 不可缺的驗證 |
|---|---|---|---|
| `01-cover.png` | 4:5／1080×1350 | 視覺文字設計＋標示來源的真鍵盤截圖（若有） | 標註 Alpha |
| `02-dachen.png` | 4:5／1080×1350 | 真機／經說明的 Emulator 大千鍵盤 | 41 鍵及正確標籤；無私人輸入 |
| `03-candidates.png` | 4:5／1080×1350 | 真正展開候選／翻頁操作 | 實際被測版本清楚；非 UI Mock |
| `04-architecture.png` | 16:9／1920×1080 | 專案架構圖（可獨立繪製） | 與 docs/ARCHITECTURE.md 一致 |
| `05-engineering.png` | 16:9／1920×1080 | 真正 GitHub Actions／測試畫面 | 顯示 JVM PASS 不等於 Runtime PASS |
| `06-status.png` | 4:5／1080×1350 | 透明狀態卡：Alpha／限制／連結 | 不寫「正式版上線」 |
| `demo-vertical.mp4` | 9:16／1080×1920 | 45–60 秒真實操作或代碼導覽 | 沒有實機畫面就不要偽造 |
| `demo-horizontal.mp4` | 16:9／1920×1080 | 同素材橫向剪輯，適合 B站／FB | 所有畫面有可核對的版本來源 |
| `repo-preview.png` | 1200×630 | 可文字設計的 GitHub／社群分享卡 | 絕不可拿臆造的 UI 假裝實際截圖 |

**僅是建議規格**，不是本次已製作的影像素材。貼文若無操作素材，可先採純文字＋真實 GitHub Release／架構截圖。

## 建議 Carousel 排列與 Alt Text

- 封面 Alt：KU-Yin 酷音 Android 大千注音開源鍵盤的專案介紹，標示 Experimental Alpha。
- 鍵位 Alt：KU-Yin 大千注音 41 鍵佈局的實際測試版本畫面（須填寫來源手機或 Emulator）。
- 候選 Alt：KU-Yin 中文候選列／展開頁的實際畫面（須填寫所示版本）。
- 架構 Alt：Android IME Service 將 Editor 事件交由 ComposeDecoderSession，再由 JNI/libchewing 解碼並透過 InputConnection 提交文字的流程示意。
- 狀態 Alt：KU-Yin 目前已公開 Experimental Alpha，Release Candidate、實機驗收、正式簽章尚未完成的開發狀態。

## 上線前逐項簽核

- [ ] 已重新開啟 Repository／現有 Release URL，且資產確實存在。
- [ ] 如果提供 APK SHA256，必須抄自 **對應版本的 SHA256SUMS**，不要拿未發行 RC 的 `c171...` 取代公開 Alpha。
- [ ] 實際畫面記錄 Android 版本、APK identity 與來源；若沒有真機畫面就用工程圖／repo capture。
- [ ] 所有私人通知、相簿、帳號、Email、IME 使用者字典、GitHub token／簽署憑證都已遮蔽。
- [ ] 不拍攝真實密碼欄位、不使用私人聊天紀錄當 Demo。
- [ ] 參考 libchewing、AOSP、fcitx5、FlorisBoard 等來源時，區分正式依賴與研究參考，避免誤稱官方贊助。
- [ ] 標示「Experimental Alpha／Debug APK／尚未正式驗收」，貼文不暗示可取代日常主力輸入法。
- [ ] QR 若取自 PR #20，須說明尚未發布到現有 main／舊 Alpha；捐款網址僅指向各基金會官方網站，沒有個人收款。
- [ ] 各平台標題、短連結、第一則留言、縮圖與字幕不彼此矛盾。
- [ ] 貼文發出前，確認 GitHub 預設分支／PR 狀態沒有新變動；如更新則同步校正文字。

## 渠道順序（不是既定排程）

1. **GitHub**：先把對外文案與圖片來源證據留在這個 Draft PR，保留可追溯版本。
2. **Threads**：主文 A（簡短）＋一張真實來源的專案封面；若有人問能否當日常鍵盤，明確回覆仍為 Alpha。
3. **Facebook**：完整開發故事＋Repo 與 Alpha 連結。
4. **Instagram**：4:5 多圖加短 caption；Repo 連結放 Bio 或可點擊位置。
5. **Bilibili**：45–60 秒說明片或完整 16:9 工程導覽；附简体中文標題和字幕。
6. **LinkedIn／履歷**：引用作品集案例，強調架構、資料保護、Agent 驗收與 GitHub 證據。

## 發布後回饋管理

- GitHub Issues 集中管理 Bug／Feature；鼓勵使用者附 APK 版本、Android/OEM、輸入欄位類型、**合成文字**重現步驟與預期／實際結果。
- 負評或安全問題不要要求對方上傳使用者字典、真實輸入／密碼或完整原始私人 logcat。
- 隱私／安全報告遵循倉庫 `SECURITY.md`；不將私人輸入內容公開貼到 Issue。
- 真正開始新 RC 公開下載時，要另外產出 **新版本**的所有平台文案，不默默修改歷史 Alpha 日期／下載鏈結。

## GitHub About／Topics 提案（尚未直接更新 metadata）

**Description（繁中）**：

`KU-Yin 酷音｜離線大千注音 Android 開源鍵盤，使用 Jetpack Compose 與 libchewing；目前為實驗性 Alpha，正式實機驗收進行中。`

**English**：

`KU-Yin: an offline Android Zhuyin (Dachen) keyboard built with Jetpack Compose and libchewing. Experimental Alpha; device/release validation in progress.`

**Topics 候選**：`android`、`zhuyin`、`bopomofo`、`libchewing`、`jetpack-compose`、`kotlin`、`ime`、`offline-first`。

需要有 Repo administration 權限的維護者在 GitHub About 設定實際套用；本 PR 只新增文件。

## 資料保存界線

R3.1 本地十份未提交文件與 ENOSPC 必須依 `docs/engineering/R3_1_LOCAL_RECOVERY_CHECKLIST.md` 救援。這個社群 PR **不是**對那十份文件的備份，不應拿它證明本機已 clean 或推送完成。
