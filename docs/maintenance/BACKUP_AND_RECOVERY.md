# 工程備份與恢復

本輪外部保存：`/Users/huang/Desktop/KU-Yin-Engineering-Archive-2026-10-12/`。

- repository.bundle 保存目前repo全部refs與其reachable objects（不包含unreachable blobs/trees）（含worktreeHEAD，detachedC4亦在可達history）；historical-c4-d25.bundle另存獨立D2.5repo。fsck無unreachablecommit，未對unreachableblob/tree做清理。
- inventory各worktree tracked/untracked/ignored metadata、HEAD/status、binarydiff與cachedpatch；dirty/另copy未追蹤來源。Gitbundle不含未提交bytes，因此不能單獨宣稱完整備份。
- materials以SHA內容去重保留536來源references/470exactcopies，包括old/currentAPK/ZIP、review/test evidence、source/relink材料、TAUIbytes；完整離線ARCHIVE_MANIFEST.json和SHA256SUMS。
- restore-drill/在獨立clone重建4個dirtyworktree：checkout原SHA→applycachedpatch --index→applyworkingpatch→copyuntracked；比較status、binarypatch與sourcebytes。RESTORE_PROOF.json PASS。未修改原來源。
- 私人userdict/DB/prefs/device evidence/keys不備份。這不是完整使用者appdata備份；不宣稱能恢復手機詞庫。若需此資料，先針對許可＋offline加密，再另做consistentDB/nativecontext關閉規約。

## 安全還原演練指令

```sh
git bundle verify /external/repository.bundle
git clone --no-checkout /external/repository.bundle /new/isolated-restore
git -C /new/isolated-restore checkout --detach ORIGINAL_SHA
git -C /new/isolated-restore apply --index /external/cached.patch
git -C /new/isolated-restore apply /external/working.patch
```

只用新空目的地。再次比對SHA/status後才考慮人工搬回；不可reset/clean原工區，也不可覆蓋已有snapshot。任何校驗失敗禁止irreversiblecleanup。工程archive維持offline，不包含GitHubcredentials或privateIMEstate。
