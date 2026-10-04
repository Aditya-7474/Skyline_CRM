@echo off
setlocal
set "ROOT=%~dp0"
powershell -NoProfile -Command "try {$r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 1 http://localhost:8001/api/; if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { exit 0 } else { exit 1 }} catch { exit 1 }" >nul 2>&1
if not errorlevel 1 goto backend_ready
if not exist "%ROOT%backend-java\target\skyline-crm-backend-1.0.0.jar" (
  echo Backend JAR not found. Building Spring Boot backend...
  call mvn -DskipTests package
  if errorlevel 1 (
    echo Backend build failed.
    pause
    exit /b 1
  )
)
start "Skyline CRM Backend" cmd /k "cd /d ""%ROOT%backend-java"" && java -jar target\skyline-crm-backend-1.0.0.jar --server.port=8001"
for /L %%I in (1,1,60) do (
  powershell -NoProfile -Command "try {$r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 1 http://localhost:8001/api/; if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) { exit 0 } else { exit 1 }} catch { exit 1 }" >nul 2>&1
  if not errorlevel 1 goto backend_ready
  timeout /t 1 /nobreak >nul
)
echo Backend did not become available on port 8001.
exit /b 1
:backend_ready
echo Backend ready on http://localhost:8001
endlocal
