# Teclado KU-Yin Zhuyin para Android

> **Estado**：alpha (`0.2.0-alpha.1`). Línea de producto Compose en desarrollo. Todo comportamiento es UNVERIFIED hasta la validación en dispositivo.

Teclado Zhuyin Dachen taiwanés para Android. UI de producto en Jetpack Compose. La decodificación de chino usa hoy un diccionario estático (`ZhuyinDictionary`, transitorio); el decodificador libchewing de referencia (JNI, 4 ABI, diccionarios — ya en el APK) se activará tras la validación (C4).

## Funciones

- Entrada Dachen Zhuyin, tira de candidatos, selección, Space／Backspace／Enter
- Páginas inglesa QWERTY, símbolos, emoji, modo oscuro, múltiples temas
- Orden de candidatos：coincidencia exacta ＞ continuación válida ＞ repliegue tonal. El aprendizaje solo reordena dentro de un nivel
- El área táctil cubre toda la celda (tecla visual intacta a 44dp)

## Privacidad y seguridad (verificado en código)

- **Cero permisos**：sin `INTERNET`, sin `VIBRATE`; `allowBackup=false`
- **Totalmente local**：sin transmisiones, sin lectura del portapapeles
- Dependencias nativas fijadas (prebuilt `3587ba33`, libchewing `a6a8fa4`)

## Compilación

Requisitos ：JDK 21, Android SDK (NDK 28.2, CMake 3.22.1). Application ID ：`io.github.juhjuhx.kuyin`.

```bash
bash scripts/bootstrap_native_deps.sh
./gradlew :app:testDebugUnitTest        # 61 pruebas
./gradlew :app:assembleDebug            # app-debug.apk (~29 MB)
```

## Donaciones (el autor no acepta donaciones personales)

Tarjeta integrada hacia FSF／ASF／Open Source Initiative／EFF. El gráfico estilo QR está dibujado a mano y sin verificar — use el botón de copiar URL.

## Licencia

- Proyecto ：LGPL-2.1 (`LICENSE`)
- libchewing ：https://codeberg.org/chewing/libchewing. Detalles en `NOTICE`＋`docs/UPSTREAM.md`
