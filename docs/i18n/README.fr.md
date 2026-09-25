# Clavier KU-Yin Zhuyin pour Android

> **Statut**：alpha (`0.2.0-alpha.1`). Ligne produit Compose en développement. Tout comportement est UNVERIFIED avant validation sur appareil.

Clavier Zhuyin Dachen taïwanais pour Android. UI produit en Jetpack Compose. Le décodage chinois utilise pour l'instant un dictionnaire statique (`ZhuyinDictionary`, transitoire) ; le décodeur libchewing de référence (JNI, 4 ABI, dictionnaires — déjà dans l'APK) sera activé après validation (C4).

## Fonctionnalités

- Saisie Dachen Zhuyin, barre de candidats, sélection, Space／Backspace／Enter
- Pages anglaise QWERTY, symboles, emoji, mode sombre, thèmes multiples
- Classement：correspondance exacte ＞ continuation valide ＞ repli tonal. L'apprentissage ne réordonne qu'au sein d'un niveau
- La zone tactile couvre toute la cellule (capuchon visuel conservé à 44dp)

## Confidentialité et sécurité (vérifié dans le code)

- **Zéro permission**：ni `INTERNET`, ni `VIBRATE` ; `allowBackup=false`
- **Entièrement local**：aucune transmission, aucune lecture du presse-papiers
- Dépendances natives épinglées (prebuilt `3587ba33`, libchewing `a6a8fa4`)

## Build

Prérequis ：JDK 21, Android SDK (NDK 28.2, CMake 3.22.1). Application ID ：`io.github.juhjuhx.kuyin`.

```bash
bash scripts/bootstrap_native_deps.sh
./gradlew :app:testDebugUnitTest        # 61 tests
./gradlew :app:assembleDebug            # app-debug.apk (~29 Mo)
```

## Dons (l'auteur n'accepte aucun don personnel)

Carte intégrée vers FSF／ASF／Open Source Initiative／EFF. Le graphique façon QR est dessiné à la main et non vérifié au scan — utilisez le bouton copier l'URL.

## Licence

- Projet ：LGPL-2.1 (`LICENSE`)
- libchewing ：https://codeberg.org/chewing/libchewing. Détails dans `NOTICE`＋`docs/UPSTREAM.md`
