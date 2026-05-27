@echo off
chcp 65001 >nul
set PROJECT_ROOT=%~dp0

echo.
echo ========================================
echo   配件流转管理系统 - 一键启动
echo ========================================
echo.

call fnm use 2>nul

echo [1/3] 启动后端 (Spring Boot)...
start "CM-后端" cmd /k "cd /d "%PROJECT_ROOT%cm-backend" && mvn spring-boot:run"

echo [2/3] 启动 PC 前端...
timeout /t 3 /nobreak >nul
start "CM-PC前端" cmd /k "cd /d "%PROJECT_ROOT%cm-pc" && call fnm use 2>nul && npm run dev"

echo [3/3] 启动 H5 前端...
start "CM-H5前端" cmd /k "cd /d "%PROJECT_ROOT%cm-h5" && call fnm use 2>nul && npm run dev"

echo.
echo ========================================
echo   全部服务已启动！
echo ========================================
echo.
echo   后端 API:  http://localhost:8080
echo   PC 前端:   http://localhost:5173
echo   H5 前端:   http://localhost:5174
echo.
echo   默认账号:  admin / admin123
echo.
echo   关闭各服务窗口即可停止对应服务
echo   或双击 stop.bat 一键停止
echo.
pause