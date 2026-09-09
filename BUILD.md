# 构建指南

## 环境配置状态

| 组件 | 状态 | 路径 |
|------|------|------|
| Java 17 | ✅ 已安装 | `C:\Program Files\Java\jdk-17.0.2` |
| Gradle 8.5 | ✅ 自动下载 | 项目根目录 |
| Android NDK r27d | ✅ 已提供 | `D:\666\android-ndk-r27d-windows` |
| CMake 4.4.3 | ✅ 已安装 | `D:\666\CMake\bin` |
| Rust 1.98.1 | ✅ 已安装 | `~/.cargo` |
| Android SDK | ❌ 需安装 | `~\AppData\Local\Android\Sdk` |
| libchewing | ⏳ 待构建 | `app/src/main/cpp/libchewing-src/` |

## 一键构建

### Windows PowerShell
```powershell
cd D:\666\opencode\android-keyboard
.\build_all.ps1
```

### Bash (Git Bash)
```bash
cd /d/666/opencode/android-keyboard
./build_all.sh
```

## 分步操作

### 1. 安装 Android SDK（如果尚未安装）
```powershell
.\install-android-sdk.ps1
```

### 2. 构建 libchewing native 库
```powershell
cd app/src/main/cpp/libchewing-src/capi
foreach ($abi in @('arm64-v8a', 'armeabi-v7a', 'x86', 'x86_64')) {
    $env:CARGO_TARGET_aarch64_linux_android_LINKER = 'D:\666\android-ndk-r27d-windows\toolchains\llvm\prebuilt\windows-x86_64\bin\aarch64-linux-android31-clang'
    cargo build --release --target aarch64-linux-android --target-dir ../../../build/cargo
}
```

### 3. 构建 APK
```powershell
cd ..\..\..\..\..
./gradlew :app:assembleDebug
```

## 输出位置
- **APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Native 库**: `app/src/main/jniLibs/<abi>/libchewing.a`

## 常见问题

### Q: JAVA_HOME 设置错误
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.2"
```

### Q: NDK 路径错误
```powershell
$env:ANDROID_NDK_HOME = "D:\666\android-ndk-r27d-windows"
```

### Q: Rust 目标未安装
```bash
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

### Q: Gradle 缓存问题
```powershell
Remove-Item -Recurse -Force .gradle
Remove-Item -Recurse -Force app/.gradle
./gradlew clean
```

## 架构说明

```
┌─────────────────────────────────────────┐
│         Android Application             │
│  (ChewingInputMethodService)            │
├─────────────────────────────────────────┤
│         Kotlin Layer                    │
│  (AndroidChewingEngine.kt)              │
│  - JNI 绑定                            │
│  - 状态管理                             │
│  - 候选词处理                           │
├─────────────────────────────────────────┤
│         JNI Bridge                      │
│  (chewing_jni.cpp)                      │
│  - C API 到 JNI 转换                   │
├─────────────────────────────────────────┤
│         Native Layer                    │
│  (libchewing_rust.a)                    │
│  - Rust 实现的 libchewing 0.12.x        │
│  - C API 兼容性层                       │
└─────────────────────────────────────────┘
```

## 下一步工作

1. ✅ 修复 AGP/Gradle 版本兼容性问题
2. ✅ 重写 chewing_jni.cpp 对齐新 C API
3. ✅ 更新 AndroidChewingEngine.kt JNI 声明
4. ⏳ 构建 libchewing native 库
5. ⏳ 安装 Android SDK
6. ⏳ 编译 APK 并测试
7. ⏳ 集成 OpenCC 简繁切换
8. ⏳ 实现 UserDict 持久化

---
最后更新：2026-09-09
