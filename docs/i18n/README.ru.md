# Клавиатура KU-Yin Zhuyin для Android

> **Статус**：alpha (`0.2.0-alpha.1`). Продуктовая ветка Compose в разработке. Все заявления о поведении — UNVERIFIED до проверки на устройстве.

Тайваньская клавиатура Dachen Zhuyin для Android. UI продукта — Jetpack Compose. Декодирование китайского сейчас выполняется статическим словарём (`ZhuyinDictionary`, переходный этап); авторитетный декодер libchewing (JNI, 4 ABI, словари — уже в APK) будет подключён после проверки на устройстве (C4).

## Возможности

- Ввод Dachen Zhuyin, полоса кандидатов, выбор кандидатов, Space／Backspace／Enter
- Английская QWERTY, страница символов, страница эмодзи, тёмная тема
- Ранжирование кандидатов：точное совпадение ＞ допустимое продолжение ＞ тональный fallback. Обучение меняет порядок только внутри слоя
- Сенсорная зона клавиш покрывает всю ячейку (визуальный колпачок — 44dp)

## Приватность и безопасность (проверено по коду)

- **Ноль разрешений**：нет `INTERNET`, нет `VIBRATE`; `allowBackup=false`
- **Полностью локально**：ввод никуда не отправляется, буфер обмена не читается
- Нативные зависимости зафиксированы (prebuilt `3587ba33`, libchewing `a6a8fa4`)

## Сборка

Требуется：JDK 21, Android SDK (NDK 28.2, CMake 3.22.1). Application ID：`io.github.juhjuhx.kuyin`.

```bash
bash scripts/bootstrap_native_deps.sh
./gradlew :app:testDebugUnitTest        # 61 тест
./gradlew :app:assembleDebug            # app-debug.apk (около 29 МБ)
```

## Пожертвования (автор личных пожертвований не принимает)

Карточка в приложении ведёт на FSF／ASF／Open Source Initiative／EFF. QR-графика нарисована вручную и не проверена сканированием — используйте кнопку копирования URL.

## Лицензия

- Проект：LGPL-2.1 (`LICENSE`)
- libchewing：https://codeberg.org/chewing/libchewing. Подробности — `NOTICE`＋`docs/UPSTREAM.md`
