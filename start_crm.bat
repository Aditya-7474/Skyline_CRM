@echo off
setlocal
call "%~dp0start_backend.bat"
if errorlevel 1 exit /b 1
call "%~dp0start_frontend.bat"
if errorlevel 1 exit /b 1
echo Skyline CRM is ready at http://localhost:3001/login.html
endlocal
