@echo off
echo.
echo ========================================
echo   CM System - Environment Setup
echo ========================================
echo.

echo [1/4] Checking Java...
java -version 2>nul
if %errorlevel% neq 0 (
    echo   ERROR: Java not found. Please install JDK 17+
    goto :fail
)
echo   OK

echo.
echo [2/4] Checking Maven...
call mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo   ERROR: Maven not found.
    goto :fail
)
echo   OK

echo.
echo [3/4] Checking Node.js...
call fnm use >nul 2>&1
call node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo   ERROR: Node.js not found. Check fnm setup.
    goto :fail
)
echo   OK

echo.
echo [4/4] Installing frontend dependencies...

echo   -> cm-pc...
cd /d "%~dp0cm-pc"
call npm install
if %errorlevel% neq 0 (
    echo   ERROR: cm-pc npm install failed
    goto :fail
)
echo   -> cm-h5...
cd /d "%~dp0cm-h5"
call npm install
if %errorlevel% neq 0 (
    echo   ERROR: cm-h5 npm install failed
    goto :fail
)

cd /d "%~dp0"
echo.
echo ========================================
echo   Setup complete! Run start.bat to launch.
echo ========================================
pause
exit /b 0

:fail
cd /d "%~dp0"
echo.
echo Setup failed. Please check your environment.
pause
exit /b 1