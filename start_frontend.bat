@echo off
setlocal
set "ROOT=%~dp0"
powershell -NoProfile -Command "try {$r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 1 http://localhost:3001/login.html; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 }} catch { exit 1 }" >nul 2>&1
if errorlevel 1 start "Skyline CRM Frontend" cmd /k "cd /d ""%ROOT%"" && python -m http.server 3001 --directory frontend-html"
for /L %%I in (1,1,30) do (
  powershell -NoProfile -Command "try {$r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 1 http://localhost:3001/login.html; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 }} catch { exit 1 }" >nul 2>&1
  if not errorlevel 1 goto frontend_ready
  timeout /t 1 /nobreak >nul
)
echo Frontend did not become available on port 3001.
exit /b 1
:frontend_ready
echo Frontend ready. Opening login page...
start "" "http://localhost:3001/login.html"
endlocal
