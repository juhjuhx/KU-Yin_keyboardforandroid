# Build libchewing as static library for Android
# Run this script to build libchewing for all Android ABIs

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$LibChewingSrc = Join-Path $ScriptDir "app\src\main\cpp\libchewing-src\capi"
$BuildDir = Join-Path $ScriptDir "build\cargo"
$NDKDir = "D:\666\android-ndk-r27d-windows"
$AndroidABIs = @("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

Write-Host "Building libchewing for Android..." -ForegroundColor Cyan

# Source Rust environment
$env:PATH = "$env:USERPROFILE\.cargo\bin;$env:PATH"
Push-Location $LibChewingSrc

foreach ($ABI in $AndroidABIs) {
    $RustTarget = switch ($ABI) {
        "armeabi-v7a" { "armv7-linux-androideabi" }
        "arm64-v8a" { "aarch64-linux-android" }
        "x86" { "i686-linux-android" }
        "x86_64" { "x86_64-linux-android" }
    }
    
    $ApiLevel = 24
    Write-Host "`nBuilding for ${ABI} (API ${ApiLevel})..." -ForegroundColor Yellow
    
    # Build with cargo
    $env:CARGO_TARGET_${RustTarget.Replace('-', '_')}_LINKER = Join-Path $NDKDir "toolchains\llvm\prebuilt\windows-x86_64\bin\aarch64-linux-android24-clang"
    if ($ABI -eq "armeabi-v7a") {
        $env:CARGO_TARGET_${RustTarget.Replace('-', '_')}_LINKER = Join-Path $NDKDir "toolchains\llvm\prebuilt\windows-x86_64\bin\armv7a-linux-androideabi24-clang"
    }
    if ($ABI -eq "x86") {
        $env:CARGO_TARGET_${RustTarget.Replace('-', '_')}_LINKER = Join-Path $NDKDir "toolchains\llvm\prebuilt\windows-x86_64\bin\i686-linux-android24-clang"
    }
    if ($ABI -eq "x86_64") {
        $env:CARGO_TARGET_${RustTarget.Replace('-', '_')}_LINKER = Join-Path $NDKDir "toolchains\llvm\prebuilt\windows-x86_64\bin\x86_64-linux-android24-clang"
    }
    
    cargo build --release --target ${RustTarget} --target-dir ${BuildDir} 2>&1
    
    $LibPath = Join-Path $BuildDir "${RustTarget}\release\libchewing_capi.a"
    if (Test-Path $LibPath) {
        $OutputDir = Join-Path $ScriptDir "app\src\main\jniLibs\${ABI}"
        New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null
        Copy-Item $LibPath $OutputDir
        Write-Host "Built: ${OutputDir}\libchewing.a" -ForegroundColor Green
    } else {
        Write-Host "Error: Failed to build for ${ABI}" -ForegroundColor Red
    }
}

Pop-Location
Write-Host "`nBuild complete! Check app/src/main/jniLibs/ for .a files." -ForegroundColor Green
