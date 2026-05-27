@echo off
chcp 65001 >nul 2>&1
echo.
echo ========================================
echo   配件流转管理系统 - 环境检查
echo ========================================
echo.

echo [1/4] 检查 Java...
java -version 2>nul
if %errorlevel% neq 0 (
    echo   错误：未找到 Java，请安装 JDK 17 及以上版本。
    goto :fail
)
echo   通过。

echo.
echo [2/4] 检查 Maven...
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo   错误：未找到 Maven，请安装 Maven。
    goto :fail
)
echo   通过。

echo.
echo [3/4] 检查 Node.js...
set PATH=%APPDATA%\fnm\node-versions\v20.20.2\installation;%PATH%
call node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo   错误：未找到 Node.js，请安装 Node.js 20。
    goto :fail
)
echo   通过。

echo.
echo [4/4] 安装前端依赖...

echo   -> 安装 cm-pc 依赖...
cd /d "%~dp0cm-pc"
call npm install
if %errorlevel% neq 0 (
    echo   错误：cm-pc 依赖安装失败。
    goto :fail
)

echo   -> 安装 cm-h5 依赖...
cd /d "%~dp0cm-h5"
call npm install
if %errorlevel% neq 0 (
    echo   错误：cm-h5 依赖安装失败。
    goto :fail
)

cd /d "%~dp0"
echo.
echo ========================================
echo   环境检查通过！请运行 start.bat 启动。
echo ========================================
pause
exit /b 0

:fail
cd /d "%~dp0"
echo.
echo 环境检查失败，请修复后重试。
pause
exit /b 1