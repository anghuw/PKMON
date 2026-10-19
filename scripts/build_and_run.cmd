@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build_and_run.ps1" %*
if errorlevel 1 (
  echo.
  echo Build failed. Read the messages above.
  pause
)
endlocal
