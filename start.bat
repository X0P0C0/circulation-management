@echo off
chcp 65001 >nul 2>&1
setlocal

echo.
echo ========================================
echo   配件流转管理系统 - 启动
echo ========================================
echo.

echo [1/4] 正在清理旧进程（端口 8080、3100、3101）...
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":8080 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3100 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":3101 " ^| findstr LISTENING') do taskkill /PID %%a /F >nul 2>&1
timeout /t 2 /nobreak >nul
echo   清理完成。

echo [2/4] 正在启动后端服务（端口 8080）...
start "CM-后端" cmd /k call "%~dp0start-backend.bat"
timeout /t 15 /nobreak >nul

echo [3/4] 正在启动PC前端（端口 3100）...
start "CM-PC前端" cmd /k call "%~dp0start-pc.bat"
timeout /t 3 /nobreak >nul

echo [4/4] 正在启动H5前端（端口 3101）...
start "CM-H5前端" cmd /k call "%~dp0start-h5.bat"

echo.
echo ========================================
echo   全部服务已启动！
echo ========================================
echo.
echo   后端接口 ：http://localhost:8080
echo   PC网页端 ：http://localhost:3100
echo   H5手机端 ：http://localhost:3101
echo   登录账号 ：admin
echo   登录密码 ：admin123
echo.
echo   关闭对应的服务窗口即可停止服务。
echo   也可运行 stop.bat 一键停止所有服务。
echo.
pause