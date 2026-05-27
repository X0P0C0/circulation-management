@echo off
chcp 65001 >nul
echo.
echo 正在停止所有服务...

taskkill /FI "WINDOWTITLE eq CM-后端*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq CM-PC前端*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq CM-H5前端*" /F >nul 2>&1

:: 也杀掉 java 和 node 进程（兜底）
taskkill /IM java.exe /F >nul 2>&1
taskkill /IM node.exe /F >nul 2>&1

echo.
echo 全部服务已停止。
echo.
pause