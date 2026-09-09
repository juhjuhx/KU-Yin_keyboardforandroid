# 安装 Android SDK（PowerShell 版本）
$ErrorActionPreference = "Stop"

$NDK_DIR = "D:\666\android-ndk-r27d-windows"
$SDK_DIR = "$env:USERPROFILE\AppData\Local\Android\Sdk"
$CMDLINE_TOOLS_URL = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
$CMDLINE_TOOLS_DIR = Join-Path $SDK_DIR "cmdline-tools"

Write-Host "=== 安装 Android SDK ===" -ForegroundColor Cyan
Write-Host "SDK 目录: $SDK_DIR"

# 创建 SDK 目录
New-Item -ItemType Directory -Force -Path $CMDLINE_TOOLS_DIR | Out-Null

# 下载 command-line tools
Write-Host "`n下载 command-line tools..." -ForegroundColor Yellow
Invoke-WebRequest -Uri $CMDLINE_TOOLS_URL -OutFile "$env:TEMP\cmdline-tools.zip"

# 解压
Expand-Archive -Path "$env:TEMP\cmdline-tools.zip" -DestinationPath "$env:TEMP\cmdline-tools-extracted" -Force

# 移动到正确位置 (需要命名为 latest)
Move-Item -Path "$env:TEMP\cmdline-tools-extracted\cmdline-tools" -Destination "$CMDLINE_TOOLS_DIR\latest" -Force

Write-Host "`n=== 安装必要的 SDK 组件 ===" -ForegroundColor Cyan

# 设置环境变量
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.2"
$env:PATH = "$SDK_DIR\cmdline-tools\latest\bin;$env:PATH"

# 安装必需的包
$sdkmanager = Join-Path $SDK_DIR "cmdline-tools\latest\bin\sdkmanager.bat"
& $sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools" --accept_license

Write-Host "`n=== 安装完成 ===" -ForegroundColor Green
Write-Host "SDK 已安装到: $SDK_DIR"
Write-Host "`n下一步：运行 ./build_all.ps1"
