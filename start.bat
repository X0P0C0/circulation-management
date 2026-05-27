@echo off
chcp 936 >nul 2>&1

echo.
echo ========================================
echo   CM System - Starting
echo ========================================
echo.

echo [1/4] Cleaning old processes on ports 8080, 3100, 3101...
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":8080 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3100 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3101 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
timeout /t 2 /nobreak >nul
echo   Done.

echo [2/4] Starting Backend on port 8080...
start "CM-Backend" cmd /k call "%~dp0start-backend.bat"
timeout /t 15 /nobreak >nul

echo [3/4] Starting PC Frontend on port 3100...
start "CM-PC" cmd /k call "%~dp0start-pc.bat"
timeout /t 3 /nobreak >nul

echo [4/4] Starting H5 Frontend on port 3101...
start "CM-H5" cmd /k call "%~dp0start-h5.bat"

echo.
echo ========================================
echo   All Services Started!
echo ========================================
echo.
echo   Backend API : http://localhost:8080
echo   PC Frontend : http://localhost:3100
echo   H5 Frontend : http://localhost:3101
echo   Login       : admin / admin123
echo.
echo   Close service windows or run stop.bat
echo.
pause