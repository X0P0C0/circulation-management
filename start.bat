@echo off
setlocal
set NODE_DIR=%APPDATA%\fnm\node-versions\v20.20.2\installation
set ROOT=%~dp0

echo.
echo ========================================
echo   CM System - Starting
echo ========================================
echo.

echo [1/3] Backend on :8080 ...
start "CM-Backend" cmd /k "cd /d "%ROOT%cm-backend" && mvn spring-boot:run"

echo [2/3] PC Frontend on :3100 ...
timeout /t 3 /nobreak >nul
start "CM-PC" cmd /k "set PATH=%NODE_DIR%;%%PATH%% && cd /d "%ROOT%cm-pc" && npm run dev"

echo [3/3] H5 Frontend on :3101 ...
start "CM-H5" cmd /k "set PATH=%NODE_DIR%;%%PATH%% && cd /d "%ROOT%cm-h5" && npm run dev"

echo.
echo   Backend: http://localhost:8080
echo   PC:      http://localhost:3100
echo   H5:      http://localhost:3101
echo   Login:   admin / admin123
echo.
echo   Close service windows or run stop.bat
echo.
pause