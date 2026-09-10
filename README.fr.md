# KU-Yin Keyboard for Android

> Un IME Zhuyin (Bopomofo) open source et local-first pour Android, basé sur `InputMethodService`, Kotlin/View, JNI et libchewing.

[繁體中文](README.md) · [简体中文](README.zh-CN.md) · [English](README.en.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [Español](README.es.md) · [Português](README.pt-BR.md) · **Français** · [Deutsch](README.de.md) · [Русский](README.ru.md)

## État

La version actuelle est **`0.1.0-alpha`**. Les tests JVM, les APK Debug/Release et le bootstrap des dépendances natives fonctionnent dans GitHub Actions. Sur un émulateur Android 13, l'installation, l'enregistrement IME, l'activation et la sélection de KU-Yin ont été vérifiés.

Le smoke test runtime échoue encore sur l'assertion de visibilité de la fenêtre IME en environnement headless. Cette version reste donc une **alpha preview**.

## Fonctions principales

- Saisie Zhuyin Dachen (大千) avec libchewing
- Synchronisation composition/candidats via `InputConnection`
- Modes ASCII et champs de mot de passe
- Shift, chiffres, Space, Backspace, Enter et actions d'éditeur
- armv7, arm64, x86 et x86_64

## Téléchargement

La version alpha est disponible dans [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases). Voir aussi [APK/README.md](APK/README.md).

Le Debug APK est signé en mode debug pour les tests. Le Release unsigned APK est un artefact développeur non signé et n'est pas un paquet de production.

## Confidentialité et compilation

KU-Yin suit une approche local-first ; le décodage Zhuyin actuel ne nécessite pas de service cloud. Consultez [SECURITY.md](SECURITY.md) avant d'utiliser une version alpha avec des données sensibles.

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

Voir [BUILD.md](BUILD.md) et [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md). Licence : [GNU LGPL 2.1](LICENSE). Attributions : [NOTICE](NOTICE).
