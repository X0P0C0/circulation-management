@echo off
chcp 65001 >nul 2>&1
echo.
echo ========================================
echo   配件流转管理系统 - 生产构建
echo ========================================
echo.

set ROOT=%~dp0

echo [1/4] 构建后端...
cd /d "%ROOT%cm-backend"
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo   错误：后端构建失败。
    goto :fail
)
echo   完成。

echo.
echo [2/4] 构建PC前端...
set PATH=%APPDATA%\fnm\node-versions\v20.20.2\installation;%PATH%
cd /d "%ROOT%cm-pc"
call npm run build
if %errorlevel% neq 0 (
    echo   错误：PC前端构建失败。
    goto :fail
)
echo   完成。

echo.
echo [3/4] 构建H5前端...
cd /d "%ROOT%cm-h5"
call npm run build
if %errorlevel% neq 0 (
    echo   错误：H5前端构建失败。
    goto :fail
)
echo   完成。

echo.
echo [4/4] 复制部署文件...
cd /d "%ROOT%"
if not exist deploy\app mkdir deploy\app
if not exist deploy\pc mkdir deploy\pc
if not exist deploy\h5 mkdir deploy\h5
copy /Y cm-backend\target\cm-backend-1.0.0.jar deploy\app\ >nul
xcopy /Y /E /Q cm-pc\dist\* deploy\pc\ >nul
xcopy /Y /E /Q cm-h5\dist\* deploy\h5\ >nul

echo.
echo ========================================
echo   构建完成！
echo ========================================
echo.
echo   产物位置：
echo     deploy\app\cm-backend-1.0.0.jar
echo     deploy\pc\   （PC前端静态文件）
echo     deploy\h5\   （H5前端静态文件）
echo     deploy\nginx.conf
echo.
echo   启动命令：java -jar deploy\app\cm-backend-1.0.0.jar
echo.
pause
exit /b 0

:fail
cd /d "%ROOT%"
echo.
echo 构建失败，请检查错误信息。
pause
exit /b 1