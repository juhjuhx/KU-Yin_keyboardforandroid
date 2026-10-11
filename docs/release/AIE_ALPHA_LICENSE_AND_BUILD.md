# AIE Alpha 原生來源與建置材料

本輪只補散布材料，不改 native pins、decoder、資料格式或授權策略。

| 元件 | 固定來源 | 授權／實際證據 |
|---|---|---|
| KU-Yin | product `39acf96c6cfc058106315f3553e1c8b88438f4f7` | 完整 repo source + `LICENSE`（LGPL-2.1） |
| libchewing / capi | `a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c` | Cargo.toml：LGPL-2.1-or-later；完整 COPYING/source |
| Android archives | prebuilt `3587ba3355711f0aca50136e787719f6562676b8` | exact four ABI static archives + headers，不改 bytes |
| Build patch | prebuilder `55e3c0877ba060bf6670120156eea61e4438797c` | libchewing.patch SHA256 `8adab140b80f30f9e18593a9f3849ac7ec1c5635d83b00261b14554e47762346`，build-only，recipe以原檔引用、不複製Haskell實作 |
| Word/phrase data | libchewing-data `ea74f76dd2548b1d65d0ff70c3ae66057a6ad97d` | word.csv/tsi.csv 檔頭直接標註2025 libchewing Core Team、LGPL-2.1-or-later |
| misc data | 同 data pin | swkb.dat/symbols.dat 原檔與 prebuilt逐byte相同；沒有新增／重授權dataset |
| English | LatinIME `127336e9f29d69607eab55982324b210279ae8c5` | 原NOTICE、Apache-2.0、固定10k過濾來源已在assets |
| Rust dependencies | 原 libchewing Cargo.lock | 76 crates完整vendor及各自license/notice；inventory不代表全部進APK |
| Toolchain/runtime notices | prebuilt Rust1.92.0、KU-Yin NDK28.2 | 附Rust MIT/Apache/COPYRIGHT、NDK NOTICE及NOTICE.toolchain |

## 實際核對

上游 libchewing archive SHA256 `29fdb2470a0a09133c1779c6d8058e394778a917c2c35da7f43a2e2869860816`；data archive `767b935212f5a4046c0fc9b67b5429b8703af734597131559ecdd5e4af66e2c1`。來源與 patch 可從以下固定 revision 核查：

- https://github.com/chewing/libchewing/tree/a6a8fa4abd3f215e3ba89a7b61702eaf8ca68f5c
- https://github.com/chewing/libchewing-data/tree/ea74f76dd2548b1d65d0ff70c3ae66057a6ad97d
- https://github.com/fcitx5-android/prebuilder/blob/55e3c0877ba060bf6670120156eea61e4438797c/src/Rules/LibChewing.hs
- https://github.com/fcitx5-android/prebuilder/blob/55e3c0877ba060bf6670120156eea61e4438797c/patches/libchewing.patch

獨立材料包提供完整產品source、native source/data/vendor、可替換的static archives、KU-Yin JNI object與重鏈結腳本。四ABI實際link exit0；使用相同NDK/link flags後，產物的 `.text`／`.rodata` 與凍結APK完全一致。這不是完整APK bit-repro或Android runtime證據。

`cargo metadata --locked --offline --no-deps` 成功；`git apply --check` 驗patch成功。原生 prebuilt 重建需要另準備原 Rust/NDK/CMake與Corrosion helper；本輪沒有聲稱重新交叉編譯原Rust archives。

LGPL授權文字、可修改source與relink materials隨APK同時提供；沒有新增逆向工程限制、私鑰或 userdict。完整privacy erase、正式signing、商店規範與全面dependency vulnerability audit仍在production gates中，不能因Alpha建置就稱全部合規／正式就緒。
