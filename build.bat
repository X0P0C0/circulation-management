@echo off
echo.
echo ========================================
echo   CM System - Production Build
echo ========================================
echo.

set ROOT=%~dp0

echo [1/4] Building Backend...
cd /d "%ROOT%cm-backend"
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo   ERROR: Backend build failed
    goto :fail
)
echo   OK

echo.
echo [2/4] Building PC Frontend...
set PATH=%APPDATA%\fnm\node-versions\v20.20.2\installation;%PATH%
cd /d "%ROOT%cm-pc"
call npm run build
if %errorlevel% neq 0 (
    echo   ERROR: PC build failed
    goto :fail
)
echo   OK

echo.
echo [3/4] Building H5 Frontend...
cd /d "%ROOT%cm-h5"
call npm run build
if %errorlevel% neq 0 (
    echo   ERROR: H5 build failed
    goto :fail
)
echo   OK

echo.
echo [4/4] Copying deploy files...
cd /d "%ROOT%"
if not exist deploy\app mkdir deploy\app
if not exist deploy\pc mkdir deploy\pc
if not exist deploy\h5 mkdir deploy\h5
copy /Y cm-backend\target\cm-backend-1.0.0.jar deploy\app\ >nul
xcopy /Y /E /Q cm-pc\dist\* deploy\pc\ >nul
xcopy /Y /E /Q cm-h5\dist\* deploy\h5\ >nul

echo.
echo ========================================
echo   Build complete!
echo ========================================
echo.
echo   deploy\app\cm-backend-1.0.0.jar
echo   deploy\pc\   (PC static files)
echo   deploy\h5\   (H5 static files)
echo   deploy\nginx.conf
echo.
echo   Run: java -jar deploy\app\cm-backend-1.0.0.jar
echo.
pause
exit /b 0

:fail
cd /d "%ROOT%"
echo.
echo Build failed.
pause
exit /b 1