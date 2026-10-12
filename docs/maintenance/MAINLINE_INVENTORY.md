# R1 來源深讀與盤點

本輪main= `a6a4c654845338521700121ac06586887d1ec085`；PR#8 merge `912b5cc4498415464cd637193258f15f3c153842`；tag `v0.2.0-aie.1` source `2ce6810684fd65a65a3006a224dc786b1f3a5f62`。release為public prerelease，不是production。root實際HEAD=fe1e96f（docs分支）、local main=653b4a3，未將舊local ref誤認GitHub main。

完整盤點來源：git rev-parse/status/remote/branch/worktree/log/tags/submodule/fsck；GitHub branches/releases/PR API。git fsck exit0，無unreachable commits（僅unreachable blobs/trees，未修改）。無submodules、限定來源樹掃描無nestedGit/symlink。另一D2.5 repo HEAD34a758b，clean，remote /tmp/ku-yin-c4；已另bundle，非主線。

## 深讀已確認

AGENTS仍寫fallback production，README仍寫ARCHITECTURE是舊View/C12待寫，SECURITY_AUDIT仍含D2.5 data flow；需校正當前入口、保留原文於Git/歷史。native production/startup fallback、scratch preview/ordered effects不可改。project_doctor確認錯查app/.deps；Host currentselector確定與S1兩列geometry不符，舊B02/B03契約不符P0 completion。timeout唯一根因未證，不先宣稱修好產品。

TAUI兩檔仍在main；無Android consumers，額外保存exact bytes，owner/獨立專案位置待決策，本輪KEEP。R1不移資料、不改DB、不刪archive。

## 來源及資料保護

完整metadata與dirty bytes僅保存在repo外工程archive。實機證據目錄/keys/不明userdict只定位且KEEP，未copy私人資料。沒有Android device-local DB讀取。
