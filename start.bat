@echo off
echo.
echo ========================================
echo   CM System - Starting All Services
echo ========================================
echo.

set ROOT=%~dp0

echo [1/3] Starting Backend...
start "CM-Backend" "%ROOT%start-backend.bat"

echo [2/3] Starting PC Frontend...
timeout /t 3 /nobreak >nul
start "CM-PC" "%ROOT%start-pc.bat"

echo [3/3] Starting H5 Frontend...
start "CM-H5" "%ROOT%start-h5.bat"

echo.
echo ========================================
echo   All services started!
echo ========================================
echo.
echo   Backend API:  http://localhost:8080
echo   PC Frontend:  http://localhost:3100
echo   H5 Frontend:  http://localhost:3101
echo.
echo   Login:  admin / admin123
echo.
echo   Close each window to stop, or run stop.bat
echo.
pause