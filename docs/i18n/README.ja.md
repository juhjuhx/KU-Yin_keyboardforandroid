# KU-Yin 注音キーボード for Android

> **ステータス**：alpha（`0.2.0-alpha.1`）。Compose 製品ライン開発中。動作の表明はすべて実機検証待ち。

台湾大千注音（Zhuyin/Bopomofo）Android IME。製品 UI は Jetpack Compose。中国語デコードは現在、静的辞書（`ZhuyinDictionary`、暫定）を使用し、libchewing 正式デコード（JNI・4 ABI・辞書、APK 同梱済み）への切替は実機検証待ち（C4）。

## 機能

- 大千注音入力、候補ストリップ、候補選択、Space／Backspace／Enter
- 英語 QWERTY、記号ページ、emoji ページ、ダークモード、複数テーマ
- 候補順位：完全一致 ＞ 有効な継続 ＞ 声調フォールバック。学習は同一層内でのみ並べ替える
- キータッチ領域は全セルカバー（見た目のキーキャップは 44dp 維持）

## プライバシーと安全（コード実測）

- **ゼロ権限**：`INTERNET` なし、`VIBRATE` なし、`allowBackup=false`
- **完全ローカル**：入力の送信・アップロードなし、クリップボード読取なし
- ネイティブ依存は固定リビジョン（prebuilt `3587ba33`、libchewing `a6a8fa4`）

## ビルド

要件：JDK 21、Android SDK（NDK 28.2、CMake 3.22.1）。Application ID：`io.github.juhjuhx.kuyin`。

```bash
bash scripts/bootstrap_native_deps.sh
./gradlew :app:testDebugUnitTest        # 61 テスト
./gradlew :app:assembleDebug            # app-debug.apk（約 29MB）
```

## 寄付（作者は個人寄付を受け取らない）

アプリ内カードから FSF／ASF／Open Source Initiative／EFF に寄付可能。QR 風グラフィックは未検証のため装飾扱い（URL コピーボタンを使用）。

## ライセンス

- 本プロジェクト：LGPL-2.1（`LICENSE`）
- libchewing：https://codeberg.org/chewing/libchewing。詳細は `NOTICE`＋`docs/UPSTREAM.md`
