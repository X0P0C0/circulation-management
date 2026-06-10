@echo off
chcp 936 >nul 2>&1
echo.
echo ========================================
echo   CM System - Build ^& Deploy
echo ========================================
echo.
set ROOT=%~dp0

:: Increment build version
echo Updating build version...
cd /d "%ROOT%cm-pc"
if not exist build-version.txt echo 0 > build-version.txt
set /p BUILD_NUM=<build-version.txt
set /a BUILD_NUM+=1
echo %BUILD_NUM% > build-version.txt
echo   Build version: v1.0.%BUILD_NUM%

:: Update version in code
python -c "import re,pathlib;p=pathlib.Path('src/layout/index.vue');p.write_text(re.sub(r'v1\.0\.\d+','v1.0.%BUILD_NUM%',p.read_text(encoding='utf-8')),encoding='utf-8')"
echo   Version updated in code

echo.
echo [1/3] Building backend...
cd /d "%ROOT%cm-backend"
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo Backend build failed.
    goto :fail
)
echo   Backend build OK

echo [2/3] Building frontend...
set PATH=%APPDATA%\fnm\node-versions\v20.20.2\installation;%PATH%
cd /d "%ROOT%cm-pc"
if exist node_modules rmdir /s /q node_modules >nul 2>&1
if exist package-lock.json del /q package-lock.json >nul 2>&1
call npm install
if %errorlevel% neq 0 (
    echo npm install failed.
    goto :fail
)
call npm run build
if %errorlevel% neq 0 (
    echo PC build failed.
    goto :fail
)
echo   Frontend build OK

echo [2/3] Compressing frontend...
cd /d "%ROOT%cm-pc"
if exist dist.zip del /q dist.zip >nul 2>&1
python make-zip.py
echo   Frontend compressed OK

echo [3/3] Uploading to server...
cd /d "%ROOT%"

scp cm-backend\target\cm-backend-1.0.0.jar root@101.133.175.15:/opt/cm/app/cm-backend-1.0.0.jar
if %errorlevel% neq 0 (
    echo Backend upload failed.
    goto :fail
)
echo   Backend uploaded

scp cm-pc\dist.zip root@101.133.175.15:/opt/cm/
if %errorlevel% neq 0 (
    echo Frontend upload failed.
    goto :fail
)
echo   Frontend uploaded

ssh root@101.133.175.15 "cd /opt/cm/pc && rm -rf * && python3 -c \"import zipfile; zipfile.ZipFile(\\\"../dist.zip\\\").extractall(\\\".\\\")\" && systemctl restart cm"
if %errorlevel% neq 0 (
    echo Server restart failed.
    goto :fail
)
echo   Server restarted

echo.
echo ========================================
echo   Deploy Complete! Version: v1.0.%BUILD_NUM%
echo ========================================
echo.
pause
exit /b 0

:fail
cd /d "%ROOT%"
echo Deploy failed.
pause
exit /b 1
