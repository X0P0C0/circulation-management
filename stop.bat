@echo off
chcp 65001 >nul 2>&1
echo.
echo 正在停止配件流转管理系统...
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":8080 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3100 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3101 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
echo 全部服务已停止。
echo.
pause