# KU-Yin Keyboard for Android

> Eine quelloffene, local-first Zhuyin-(Bopomofo)-IME für Android auf Basis von `InputMethodService`, Kotlin/View, JNI und libchewing.

[繁體中文](README.md) · [简体中文](README.zh-CN.md) · [English](README.en.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [Español](README.es.md) · [Português](README.pt-BR.md) · [Français](README.fr.md) · **Deutsch** · [Русский](README.ru.md)

## Status

Die aktuelle Version ist **`0.1.0-alpha`**. JVM-Tests, Debug/Release-APK-Builds und der Bootstrap der nativen Abhängigkeiten laufen in GitHub Actions. Auf einem Android-13-Emulator wurden Installation, IME-Registrierung, Aktivierung und Auswahl von KU-Yin verifiziert.

Der Runtime-Smoke-Test scheitert derzeit noch an der Sichtbarkeitsprüfung des IME-Fensters im headless Emulator. Diese Version ist daher eine **Alpha Preview**.

## Kernfunktionen

- Dachen-(大千)-Zhuyin-Eingabe mit libchewing
- Composition-/Kandidaten-Synchronisierung über `InputConnection`
- ASCII- und Passwortfeld-Modi
- Shift, Ziffern, Space, Backspace, Enter und Editor-Aktionen
- armv7, arm64, x86 und x86_64

## Download

Die aktuelle Alpha steht unter [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases) bereit. Paketdetails: [APK/README.md](APK/README.md).

Das Debug APK ist debug-signiert und zum Testen gedacht. Das Release unsigned APK ist ein unsigniertes Entwicklerartefakt und kein Produktionspaket.

## Datenschutz und Build

KU-Yin verfolgt einen local-first Ansatz; die aktuelle Zhuyin-Decodierung benötigt keinen Cloud-Dienst. Vor der Verwendung mit sensiblen Daten bitte [SECURITY.md](SECURITY.md) lesen.

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

Weitere Informationen: [BUILD.md](BUILD.md), [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md). Lizenz: [GNU LGPL 2.1](LICENSE). Drittanbieterhinweise: [NOTICE](NOTICE).
