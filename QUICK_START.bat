@echo off
setlocal
echo Start Java backend in terminal 1:
echo   cd backend-java
echo   mvn spring-boot:run
echo.
echo Start HTML frontend in terminal 2:
echo   python -m http.server 3001 --directory frontend-html
echo.
echo Open http://localhost:3001
pause
