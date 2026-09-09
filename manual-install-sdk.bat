@echo off
REM Manual SDK installation helper
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.2
set ANDROID_HOME=C:\Users\USER\AppData\Local\Android\Sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%PATH%

echo ========================================
echo Android SDK Manual Installation
echo ========================================
echo.
echo Please watch for the license prompts and type 'y' to accept.
echo.

echo --- Step 1: Accept licenses ---
sdkmanager.bat --licenses
echo.

echo --- Step 2: Install packages ---
sdkmanager.bat "platforms;android-34" "build-tools;34.0.0" "platform-tools"
echo.

echo --- Step 3: Verify ---
if exist "%ANDROID_HOME%\platforms\android-34" echo [OK] Android 34 installed
if exist "%ANDROID_HOME%\build-tools\34.0.0" echo [OK] Build tools 34.0.0 installed
