# 完整构建脚本
# 1. 构建 libchewing native 库
# 2. 构建 Android APK

$ErrorActionPreference = "Stop"

$PROJECT_DIR = "D:\666\opencode\android-keyboard"
$JAVA_HOME = "C:\Program Files\Java\jdk-17.0.2"
$NDK_DIR = "D:\666\android-ndk-r27d-windows"
$SDK_DIR = "$env:USERPROFILE\AppData\Local\Android\Sdk"
$CMAKE_DIR = "D:\666\CMake\bin"
$RUSTUP_HOME = "$env:USERPROFILE\.cargo"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Android 键盘应用 - 完整构建流程" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# 设置环境变量
$env:JAVA_HOME = $JAVA_HOME
$env:PATH = "$JAVA_HOME\bin;$NDK_DIR\toolchains\llvm\prebuilt\windows-x86_64\bin;$CMAKE_DIR;$SDK_DIR\platform-tools;$SDK_DIR\emulator;$env:PATH"
$env:ANDROID_NDK_HOME = $NDK_DIR
$env:ANDROID_HOME = $SDK_DIR
$env:ANDROID_SDK_ROOT = $SDK_DIR

# 检查必要工具
Write-Host "[检查] 验证开发环境..." -ForegroundColor Yellow
$tools = @("java", "gradlew", "cmake", "cargo", "rustc")
foreach ($tool in $tools) {
    $cmd = Get-Command $tool -ErrorAction SilentlyContinue
    if ($cmd) {
        Write-Host "  ✓ $tool 已找到" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $tool 未找到，请安装" -ForegroundColor Red
    }
}

# 检查 SDK
if (-not (Test-Path "$SDK_DIR\platforms\android-34")) {
    Write-Host "`n[提示] Android SDK 未安装，请先运行: .\install-android-sdk.ps1" -ForegroundColor Yellow
}

# 步骤 1: 构建 libchewing
Write-Host "`n[步骤 1] 构建 libchewing native 库..." -ForegroundColor Cyan
Set-Location "$PROJECT_DIR\app\src\main\cpp\libchewing-src\capi"

$abis = @(
    @{name="arm64-v8a"; target="aarch64-linux-android"},
    @{name="armeabi-v7a"; target="armv7-linux-androideabi"},
    @{name="x86"; target="i686-linux-android"},
    @{name="x86_64"; target="x86_64-linux-android"}
)

foreach ($abi in $abis) {
    Write-Host "  构建 ${abi.name}..." -NoNewline
    $envVarName = "CARGO_TARGET_" + $abi.target.Replace('-', '_').ToUpper() + "_LINKER"
    $linker = "$NDK_DIR\toolchains\llvm\prebuilt\windows-x86_64\bin\"
    if ($abi.name -eq 'arm64-v8a') {
        $linker += "aarch64-linux-android31-clang"
    } elseif ($abi.name -eq 'armeabi-v7a') {
        $linker += "armv7a-linux-androideabi31-clang"
    } elseif ($abi.name -eq 'x86_64') {
        $linker += "x86_64-linux-android31-clang"
    } else {
        $linker += "i686-linux-android31-clang"
    }
    Set-Item -Path "Env:$envVarName" -Value $linker

    cargo build --release --target $abi.target --target-dir "$PROJECT_DIR\build\cargo" 2>&1 | Out-Null
    
    $libPath = "$PROJECT_DIR\build\cargo\$($abi.target)\release\libchewing_capi.a"
    if (Test-Path $libPath) {
        $outputDir = "$PROJECT_DIR\app\src\main\jniLibs\$($abi.name)"
        New-Item -ItemType Directory -Force -Path $outputDir | Out-Null
        Copy-Item $libPath $outputDir
        Write-Host "  ✓ 完成" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 失败" -ForegroundColor Red
    }
}

# 步骤 2: 构建 APK
Write-Host "`n[步骤 2] 构建 Android APK..." -ForegroundColor Cyan
Set-Location $PROJECT_DIR

./gradlew :app:assembleDebug --no-daemon 2>&1 | Tee-Object -FilePath "$PROJECT_DIR\build\gradle.log"

if ($LASTEXITCODE -eq 0) {
    $apkPath = "$PROJECT_DIR\app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        Write-Host "`n========================================" -ForegroundColor Green
        Write-Host "  构建成功！APK 位置:" -ForegroundColor Green
        Write-Host "  $apkPath" -ForegroundColor Cyan
        Write-Host "========================================`n" -ForegroundColor Green
    }
} else {
    Write-Host "`n构建失败，请查看日志: $PROJECT_DIR\build\gradle.log" -ForegroundColor Red
}
