# KU-Yin Keyboard for Android

> Android용 오픈소스·local-first 주음(Zhuyin/Bopomofo) IME입니다. Android `InputMethodService`, Kotlin/View, JNI, libchewing을 사용합니다.

[繁體中文](README.md) · [简体中文](README.zh-CN.md) · [English](README.en.md) · [日本語](README.ja.md) · **한국어** · [Español](README.es.md) · [Português](README.pt-BR.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md)

## 현재 상태

현재 버전은 **`0.1.0-alpha`** 입니다. JVM 테스트, Debug/Release APK 빌드, native dependency bootstrap이 GitHub Actions에서 동작합니다. Android 13 emulator에서 APK 설치, IME 등록, 활성화 및 선택까지 확인되었습니다.

headless emulator의 IME 창 표시 여부를 확인하는 runtime smoke assertion은 아직 실패하므로 현재 빌드는 **alpha preview** 입니다.

## 주요 기능

- Dachen(大千) 주음 입력 및 libchewing 디코딩
- `InputConnection` composition/candidate 동기화
- ASCII 및 비밀번호 입력 모드
- Shift, 숫자, Space, Backspace, Enter, editor action
- 커서/selection 변경 후 composition reconciliation
- armv7, arm64, x86, x86_64 지원

## 다운로드

최신 alpha는 [GitHub Releases](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/releases)에서 받을 수 있습니다. 패키지 설명은 [APK/README.md](APK/README.md)를 확인하세요.

Debug APK는 테스트용 debug-signed 빌드입니다. Release unsigned APK는 release build 검증용 미서명 developer artifact이며 정식 배포 패키지가 아닙니다.

## 개인정보 및 빌드

KU-Yin은 local-first를 기본 원칙으로 하며 현재 주음 디코딩은 클라우드 서비스를 필요로 하지 않습니다. IME는 민감한 텍스트에 접근할 수 있으므로 [SECURITY.md](SECURITY.md)를 확인하세요.

```bash
bash scripts/bootstrap_native_deps.sh
gradle testDebugUnitTest
gradle assembleDebug
gradle assembleRelease
```

상세 빌드 정보는 [BUILD.md](BUILD.md), 현재 제한 사항은 [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md)를 참고하세요. 라이선스는 [GNU LGPL 2.1](LICENSE)이며 제3자 표기는 [NOTICE](NOTICE)에 있습니다.
