# KU-Yin Keyboard for Android

> Um IME Zhuyin (Bopomofo) de código aberto e local-first para Android, construído com `InputMethodService`, Kotlin/View, JNI e libchewing.

[繁體中文](README.md) · [简体中文](README.zh-CN.md) · [English](README.en.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [Español](README.es.md) · **Português** · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md)

## Estado atual

A versão atual é **`0.1.0-alpha`**. Testes JVM, builds Debug/Release APK e o bootstrap das dependências nativas funcionam no GitHub Actions. Em um emulador Android 13, instalação, registro como IME, ativação e seleção do KU-Yin já foram verificados.

O runtime smoke ainda falha na asserção de visibilidade da janela do IME em ambiente headless; portanto esta versão é uma **alpha preview**.

## Recursos principais

- Zhuyin Dachen (大千) com libchewing
- Sincronização de composição/candidatos via `InputConnection`
- Modos ASCII e campos de senha
- Shift, números, Space, Backspace, Enter e editor actions
- armv7, arm64, x86 e x86_64

## Download

Baixe a alpha atual em [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases) e veja [APK/README.md](APK/README.md).

O Debug APK é debug-signed e serve para testes. O Release unsigned APK é um artefato de desenvolvimento sem assinatura, não um pacote de produção.

## Privacidade e build

KU-Yin segue uma arquitetura local-first; a decodificação Zhuyin atual não depende de serviços em nuvem. Leia [SECURITY.md](SECURITY.md) antes de usar uma versão alpha com dados sensíveis.

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

Detalhes em [BUILD.md](BUILD.md) e [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md). Licença: [GNU LGPL 2.1](LICENSE). Atribuições: [NOTICE](NOTICE).
