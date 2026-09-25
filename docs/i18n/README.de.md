# KU-Yin Zhuyin-Tastatur für Android

> **Status**：Alpha (`0.2.0-alpha.1`). Compose-Produktlinie in Entwicklung. Alle Verhaltensangaben bis zur Geräteprüfung UNVERIFIED.

Taiwanesische Dachen-Zhuyin-Tastatur für Android. Produkt-UI ist Jetpack Compose. Chinesisch-Dekodierung läuft derzeit über ein statisches Wörterbuch (`ZhuyinDictionary`, Übergangslösung); der libchewing-Decoder (JNI, 4 ABIs, Wörterbücher — bereits im APK) wird nach Geräteprüfung umgeschaltet (C4).

## Funktionen

- Dachen-Zhuyin-Eingabe, Kandidatenleiste, Kandidatenauswahl, Space／Backspace／Enter
- Englische QWERTY-, Symbol- und Emoji-Seiten, Dark Mode, mehrere Themes
- Kandidatenranking：exakte Treffer ＞ gültige Fortsetzung ＞ Ton-Fallback. Lernen sortiert nur innerhalb einer Ebene um
- Touch-Fläche deckt die volle Zelle ab (optische Kappe bleibt 44dp)

## Datenschutz und Sicherheit (am Code verifiziert)

- **Null Berechtigungen**：kein `INTERNET`, kein `VIBRATE`; `allowBackup=false`
- **Vollständig lokal**：keine Übertragung, kein Auslesen der Zwischenablage
- Native Abhängigkeiten gepinnt (prebuilt `3587ba33`, libchewing `a6a8fa4`)

## Build

Voraussetzungen：JDK 21, Android SDK (NDK 28.2, CMake 3.22.1). Application ID：`io.github.juhjuhx.kuyin`.

```bash
bash scripts/bootstrap_native_deps.sh
./gradlew :app:testDebugUnitTest        # 61 Tests
./gradlew :app:assembleDebug            # app-debug.apk (ca. 29 MB)
```

## Spenden (der Autor nimmt keine persönlichen Spenden an)

In-App-Karte für FSF／ASF／Open Source Initiative／EFF. Die QR-Grafik ist handgezeichnet und scan-ungeprüft — bitte die URL-Kopieren-Schaltfläche verwenden.

## Lizenz

- Projekt：LGPL-2.1 (`LICENSE`)
- libchewing：https://codeberg.org/chewing/libchewing. Details in `NOTICE`＋`docs/UPSTREAM.md`
