#Requires -Version 5.1
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$destination = Join-Path $env:LOCALAPPDATA 'Programs\KU-Yin Desktop'
$voice = Join-Path $root 'KU-Yin-Voice.exe'
$weasel = Join-Path $root 'weasel-0.17.4.0-installer.exe'
if (-not (Test-Path $voice)) { throw 'KU-Yin-Voice.exe is missing from this package.' }
if (-not (Test-Path (Join-Path $root 'whisper-cli.exe'))) { throw 'whisper-cli.exe is missing.' }
if (-not (Test-Path (Join-Path $root 'models\ggml-base.bin'))) { throw 'Offline Whisper base model is missing.' }
if (Get-Process -Name 'KU-Yin-Voice' -ErrorAction SilentlyContinue) {
    throw 'Close KU-Yin Voice in the system tray before updating.'
}
Write-Host 'KU-Yin Desktop: installing offline voice into user profile.' -ForegroundColor Green
New-Item -Path $destination -ItemType Directory -Force | Out-Null
Copy-Item -Path (Join-Path $root 'KU-Yin-Voice.exe') -Destination $destination -Force
Copy-Item -Path (Join-Path $root 'whisper-cli.exe') -Destination $destination -Force
Get-ChildItem -Path $root -Filter '*.dll' | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination $destination -Force
}
New-Item -Path (Join-Path $destination 'models') -ItemType Directory -Force | Out-Null
Copy-Item -Path (Join-Path $root 'models\ggml-base.bin') -Destination (Join-Path $destination 'models') -Force
$startMenu = Join-Path $env:APPDATA 'Microsoft\Windows\Start Menu\Programs'
$shortcut = (New-Object -ComObject WScript.Shell).CreateShortcut((Join-Path $startMenu 'KU-Yin Voice.lnk'))
$shortcut.TargetPath = Join-Path $destination 'KU-Yin-Voice.exe'
$shortcut.WorkingDirectory = $destination
$shortcut.Description = 'Local-first voice dictation; Ctrl+Alt+Space'
$shortcut.Save()
Write-Host "Voice installed at: $destination"
if (Test-Path $weasel) {
    $answer = Read-Host 'Install or update Weasel TSF Zhuyin keyboard from the bundled upstream installer? [Y/n]'
    if ($answer -eq '' -or $answer -match '^[Yy]') {
        $sha = (Get-FileHash -Path $weasel -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($sha -ne 'cf509534a8f5f8af9c98ed7cbb8f135439f145a8cbe7e50ede42bb5b5ab45c29') {
            throw 'Weasel installer checksum mismatch; refusing to run.'
        }
        Start-Process -FilePath $weasel -Wait
        Write-Host 'Choose Chinese (Taiwan) in the Weasel installer, then use Win+Space.'
    }
}
Start-Process -FilePath (Join-Path $destination 'KU-Yin-Voice.exe')
Write-Host 'Voice is launching. Use Ctrl+Alt+Space in your editor.' -ForegroundColor Green
