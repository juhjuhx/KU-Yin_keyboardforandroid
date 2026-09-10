# KU-Yin Keyboard for Android

> Android 向けのオープンソース／local-first 注音（Zhuyin・Bopomofo）IME。Android `InputMethodService`、Kotlin/View、JNI、libchewing を使用しています。

[繁體中文](README.md) · [简体中文](README.zh-CN.md) · [English](README.en.md) · **日本語** · [한국어](README.ko.md) · [Español](README.es.md) · [Português](README.pt-BR.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md)

## 現在の状態

現在のバージョンは **`0.1.0-alpha`** です。JVM テスト、Debug / Release APK のビルド、native dependency bootstrap は GitHub Actions で動作しています。Android 13 emulator では APK のインストール、IME 登録、有効化、選択まで確認済みです。

headless emulator で IME ウィンドウの可視性を確認する runtime smoke assertion はまだ失敗するため、現状は **alpha preview** です。

## 主な機能

- Dachen（大千）注音入力と libchewing
- `InputConnection` による composition / candidate 同期
- ASCII・パスワードフィールド用入力モード
- Shift、数字、Space、Backspace、Enter、editor action
- カーソル／selection 変更後の composition reconciliation
- 4 ABI: armv7、arm64、x86、x86_64

## ダウンロード

最新 alpha は [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases) から取得できます。パッケージ情報は [APK/README.md](APK/README.md) を参照してください。

Debug APK はテスト用に debug-sign 済みです。Release unsigned APK は release build 検証用の未署名 developer artifact で、正式配布版ではありません。

## プライバシー・ビルド

KU-Yin は local-first を基本方針とし、現在の注音デコードにクラウドサービスは必須ではありません。IME は機密文字列に触れられるため、[SECURITY.md](SECURITY.md) を確認してください。

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

詳細は [BUILD.md](BUILD.md)。既知の制限と開発状況は [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) にあります。ライセンスは [GNU LGPL 2.1](LICENSE)、第三者表記は [NOTICE](NOTICE) を参照してください。
