# KU-Yin Keyboard for Android

> Un IME Zhuyin (Bopomofo) de código abierto y local-first para Android, construido con `InputMethodService`, Kotlin/View, JNI y libchewing.

[繁體中文](README.md) · [简体中文](README.zh-CN.md) · [English](README.en.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · **Español** · [Português](README.pt-BR.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md)

## Estado

La versión actual es **`0.1.0-alpha`**. Las pruebas JVM, los APK Debug/Release y el bootstrap de dependencias nativas funcionan en GitHub Actions. En un emulador Android 13 ya se verificaron la instalación, el registro como IME, la activación y la selección de KU-Yin.

La comprobación automatizada de visibilidad de la ventana IME aún falla en un emulador headless, por lo que esta versión es una **alpha preview**.

## Funciones principales

- Entrada Zhuyin Dachen (大千) con libchewing
- Sincronización de composición/candidatos mediante `InputConnection`
- Modos ASCII y campos de contraseña
- Shift, números, Space, Backspace, Enter y acciones del editor
- armv7, arm64, x86 y x86_64

## Descarga

Obtén la alpha actual en [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases). Consulta [APK/README.md](APK/README.md) para detalles.

El Debug APK está firmado para depuración y sirve para pruebas directas. El Release unsigned APK es un artefacto de desarrollo sin firma y no es un paquete de producción.

## Privacidad y compilación

KU-Yin sigue un enfoque local-first y el decodificador Zhuyin actual no requiere servicios en la nube. Revisa [SECURITY.md](SECURITY.md) antes de usar una versión alpha con datos sensibles.

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

Más información en [BUILD.md](BUILD.md) y [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md). Licencia: [GNU LGPL 2.1](LICENSE). Atribuciones: [NOTICE](NOTICE).
