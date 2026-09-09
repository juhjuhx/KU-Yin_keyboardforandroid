@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-17.0.2"
set "ANDROID_HOME=C:\Users\USER\AppData\Local\Android\Sdk"
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\cmdline-tools\latest\bin;%ANDROID_HOME%\platform-tools;%PATH%"

echo JAVA_HOME: %JAVA_HOME%
echo ANDROID_HOME: %ANDROID_HOME%
echo.

java -version

echo.
echo === Building APK ===
cd /d D:\666\opencode\android-keyboard
gradlew.bat :app:assembleDebug
