@echo off
chcp 65001 >nul
echo.
echo ========================================
echo   配件流转管理系统 - 环境初始化
echo ========================================
echo.

set PROJECT_ROOT=%~dp0

echo [1/4] 检查 Java...
java -version 2>nul
if %errorlevel% neq 0 (
    echo   ERROR: 未检测到 Java，请安装 JDK 17+
    goto :error
)
echo   OK

echo.
echo [2/4] 检查 Maven...
mvn -version 2>nul
if %errorlevel% neq 0 (
    echo   ERROR: 未检测到 Maven
    goto :error
)
echo   OK

echo.
echo [3/4] 检查 Node.js...
call fnm use 2>nul
node -v 2>nul
if %errorlevel% neq 0 (
    echo   ERROR: 未检测到 Node.js
    goto :error
)
echo   OK

echo.
echo [4/4] 安装前端依赖...
echo   -> cm-pc...
cd /d "%PROJECT_ROOT%cm-pc"
call npm install --silent
if %errorlevel% neq 0 (
    echo   ERROR: cm-pc 依赖安装失败
    goto :error
)
echo   OK

echo   -> cm-h5...
cd /d "%PROJECT_ROOT%cm-h5"
call npm install --silent
if %errorlevel% neq 0 (
    echo   ERROR: cm-h5 依赖安装失败
    goto :error
)
echo   OK

cd /d "%PROJECT_ROOT%"
echo.
echo ========================================
echo   初始化完成！双击 start.bat 启动系统
echo ========================================
pause
exit /b 0

:error
echo.
echo 初始化失败，请检查环境后重试。
cd /d "%PROJECT_ROOT%"
pause
exit /b 1