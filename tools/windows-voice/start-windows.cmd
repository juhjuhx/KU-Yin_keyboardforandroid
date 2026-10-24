@echo off
setlocal
cd /d "%~dp0"
if not exist .venv\Scripts\python.exe (
  py -3.11 -m venv .venv || exit /b 1
  .venv\Scripts\python.exe -m pip install -r requirements.txt || exit /b 1
)
if "%KUYIN_WHISPER_CLI%"=="" (
  echo Please set KUYIN_WHISPER_CLI to your whisper-cli.exe path.
  exit /b 1
)
if "%KUYIN_WHISPER_MODEL%"=="" (
  echo Please set KUYIN_WHISPER_MODEL to your ggml Whisper model path.
  exit /b 1
)
.venv\Scripts\python.exe winvoice.py --traditional
