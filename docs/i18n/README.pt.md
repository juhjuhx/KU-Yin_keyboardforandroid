# Teclado KU-Yin Zhuyin para Android

> **Estado**：alpha (`0.2.0-alpha.1`). Linha de produto Compose em desenvolvimento. Todo comportamento é UNVERIFIED até validação em dispositivo.

Teclado Zhuyin Dachen taiwanês para Android. UI de produto em Jetpack Compose. A decodificação de chinês usa hoje um dicionário estático (`ZhuyinDictionary`, transitório); o decodificador libchewing de referência (JNI, 4 ABI, dicionários — já no APK) será ativado após validação (C4).

## Recursos

- Entrada Dachen Zhuyin, faixa de candidatos, seleção, Space／Backspace／Enter
- Páginas inglesa QWERTY, símbolos, emoji, modo escuro, múltiplos temas
- Ordem de candidatos：correspondência exata ＞ continuação válida ＞ recuo tonal. O aprendizado só reordena dentro de um nível
- A área de toque cobre toda a célula (tecla visual intacta em 44dp)

## Privacidade e segurança (verificado no código)

- **Zero permissões**：sem `INTERNET`, sem `VIBRATE`; `allowBackup=false`
- **Totalmente local**：sem transmissões, sem leitura da área de transferência
- Dependências nativas fixadas (prebuilt `3587ba33`, libchewing `a6a8fa4`)

## Compilação

Requisitos ：JDK 21, Android SDK (NDK 28.2, CMake 3.22.1). Application ID ：`io.github.juhjuhx.kuyin`.

```bash
bash scripts/bootstrap_native_deps.sh
./gradlew :app:testDebugUnitTest        # 61 testes
./gradlew :app:assembleDebug            # app-debug.apk (~29 MB)
```

## Doações (o autor não aceita doações pessoais)

Cartão integrado para FSF／ASF／Open Source Initiative／EFF. O gráfico estilo QR é desenhado à mão e não verificado — use o botão copiar URL.

## Licença

- Projeto ：LGPL-2.1 (`LICENSE`)
- libchewing ：https://codeberg.org/chewing/libchewing. Detalhes em `NOTICE`＋`docs/UPSTREAM.md`
