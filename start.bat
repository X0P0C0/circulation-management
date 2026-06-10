@echo off
chcp 936 >nul 2>&1
setlocal enabledelayedexpansion

echo.
echo ========================================
echo   CM System - Starting
echo ========================================
echo.

echo [1/5] Cleaning old processes...
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":8080 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3100 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3101 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3102 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
ping -n 3 127.0.0.1 >nul
echo   Done.

echo [2/5] Starting Backend on port 8080...
cd /d "%~dp0cm-backend"
start "CM-Backend" cmd /k "mvn spring-boot:run"
cd /d "%~dp0"

echo [3/5] Starting PC Frontend on port 3100...
cd /d "%~dp0cm-pc"
set "PATH=%APPDATA%\fnm\node-versions\v20.20.2\installation;%PATH%"
start "CM-PC" cmd /k "npm run dev"
cd /d "%~dp0"

echo [4/5] Starting H5 Frontend on port 3101...
cd /d "%~dp0cm-h5"
start "CM-H5" cmd /k "npm run dev"
cd /d "%~dp0"

echo [5/5] Starting App Frontend on port 3102...
cd /d "%~dp0cm-app"
start "CM-App" cmd /k "npx uni"
cd /d "%~dp0"

echo.
echo ========================================
echo   All Services Starting...
echo ========================================
echo.
echo   Backend API : http://localhost:8080
echo   PC Frontend : http://localhost:3100
echo   H5 Frontend : http://localhost:3101
echo   App Frontend: http://localhost:3102
echo   Login       : admin / admin123
echo.
echo   Wait 10-15 seconds for services to be ready.
echo   Close service windows or run stop.bat to stop.
echo.
pause