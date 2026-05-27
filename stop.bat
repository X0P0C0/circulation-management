@echo off
chcp 936 >nul 2>&1
echo.
echo Stopping CM System...
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":8080 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3100 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3101 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
echo All services stopped.
echo.
pause