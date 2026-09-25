# KU-Yin 주음 키보드 for Android

> **상태**：alpha（`0.2.0-alpha.1`）。Compose 제품 라인 개발 중. 모든 동작 선언은 실기기 검증 전 UNVERIFIED.

대만 다첸 주음(Dachen Zhuyin) Android IME. 제품 UI는 Jetpack Compose. 중국어 디코딩은 현재 정적 사전(`ZhuyinDictionary`, 과도기)을 사용하며, libchewing 정식 디코딩(JNI·4 ABI·사전, APK 포함)은 실기기 검증 후 전환(C4).

## 기능

- 다첸 주음 입력, 후보 스트립, 후보 선택, Space／Backspace／Enter
- 영어 QWERTY, 기호 페이지, 이모지 페이지, 다크 모드, 다중 테마
- 후보 순위：정확히 일치 ＞ 유효한 이어짐 ＞ 성조 폴백. 학습은 같은 층 내에서만 재정렬
- 키 터치 영역은 전체 셀 커버(키캡 외관은 44dp 유지)

## 개인정보와 안전(코드 실측)

- **제로 권한**：`INTERNET` 없음, `VIBRATE` 없음, `allowBackup=false`
- **완전 로컬**：입력 전송·업로드 없음, 클립보드 읽기 없음
- 네이티브 의존성은 고정 리비전(prebuilt `3587ba33`、libchewing `a6a8fa4`)

## 빌드

요구 사항：JDK 21、Android SDK(NDK 28.2、CMake 3.22.1). Application ID：`io.github.juhjuhx.kuyin`.

```bash
bash scripts/bootstrap_native_deps.sh
./gradlew :app:testDebugUnitTest        # 61 테스트
./gradlew :app:assembleDebug            # app-debug.apk(약 29MB)
```

## 기부(作者는 개인 후원 받지 않음)

앱 내 카드에서 FSF／ASF／Open Source Initiative／EFF 기부 가능. QR 스타일 그래픽은 미검증이므로 장식으로 간주(URL 복사 버튼 사용).

## 라이선스

- 본 프로젝트：LGPL-2.1(`LICENSE`)
- libchewing：https://codeberg.org/chewing/libchewing. 자세한 내용은 `NOTICE`＋`docs/UPSTREAM.md`
