# KU-Yin Keyboard for Android

> Открытый local-first IME Zhuyin (Bopomofo) для Android на базе `InputMethodService`, Kotlin/View, JNI и libchewing.

[繁體中文](README.md) · [简体中文](README.zh-CN.md) · [English](README.en.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [Español](README.es.md) · [Português](README.pt-BR.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · **Русский**

## Состояние

Текущая версия — **`0.1.0-alpha`**. JVM-тесты, сборки Debug/Release APK и bootstrap нативных зависимостей работают в GitHub Actions. На эмуляторе Android 13 подтверждены установка APK, регистрация KU-Yin как IME, включение и выбор ввода.

Runtime smoke test всё ещё падает на проверке видимости окна IME в headless-эмуляторе, поэтому это **alpha preview**, а не стабильный релиз.

## Основные возможности

- Zhuyin Dachen (大千) на базе libchewing
- Синхронизация composition/candidates через `InputConnection`
- ASCII-режим и поля паролей
- Shift, цифры, Space, Backspace, Enter и editor actions
- armv7, arm64, x86 и x86_64

## Загрузка

Текущая alpha доступна в [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases). Подробности о пакетах: [APK/README.md](APK/README.md).

Debug APK подписан debug-ключом и предназначен для тестирования. Release unsigned APK — неподписанный developer artifact, а не производственный пакет.

## Конфиденциальность и сборка

KU-Yin следует local-first подходу; текущий Zhuyin-декодер не требует облачного сервиса. Перед использованием с чувствительными данными ознакомьтесь с [SECURITY.md](SECURITY.md).

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

Подробнее: [BUILD.md](BUILD.md), [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md). Лицензия: [GNU LGPL 2.1](LICENSE). Сторонние компоненты: [NOTICE](NOTICE).
