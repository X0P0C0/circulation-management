@echo off
chcp 936 >nul 2>&1
echo.
echo ========================================
echo   CM System - Build
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
echo 1/4 Building Backend...
cd /d "%ROOT%cm-backend"
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo Backend build failed.
    goto :fail
)
echo   Backend build OK

echo.
echo 2/4 Building PC Frontend...
set PATH=%APPDATA%\fnm\node-versions\v20.20.2\installation;%PATH%
cd /d "%ROOT%cm-pc"
echo   Cleaning node modules...
if exist node_modules rmdir /s /q node_modules >nul 2>&1
if exist package-lock.json del /q package-lock.json >nul 2>&1
echo   Installing dependencies...
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
echo   PC Frontend build OK

echo.
echo 3/4 Building H5 Frontend...
cd /d "%ROOT%cm-h5"
echo   Cleaning node modules...
if exist node_modules rmdir /s /q node_modules >nul 2>&1
if exist package-lock.json del /q package-lock.json >nul 2>&1
echo   Installing dependencies...
call npm install
if %errorlevel% neq 0 (
    echo H5 npm install failed.
    goto :fail
)
call npm run build
if %errorlevel% neq 0 (
    echo H5 build failed.
    goto :fail
)
echo   H5 Frontend build OK

echo.
echo 4/4 Copying to deploy...
cd /d "%ROOT%"
if not exist deploy\app mkdir deploy\app
copy /Y cm-backend\target\cm-backend-1.0.0.jar deploy\app\ >nul
copy /Y cm-pc\dist.zip deploy\dist.zip >nul
copy /Y cm-h5\dist.zip deploy\h5.zip >nul
copy /Y cm-backend\src\main\resources\application-prod.yml deploy\application-prod.yml >nul

echo.
echo ========================================
echo   Build Complete! Version: v1.0.%BUILD_NUM%
echo ========================================
echo.
echo   Files in deploy folder:
echo     deploy\app\cm-backend-1.0.0.jar
echo     deploy\dist.zip   (PC, zip contains dist/ prefix)
echo     deploy\h5.zip     (H5, zip contains root files)
echo     deploy\application-prod.yml
echo.
echo   Upload deploy folder to server:
echo     dist.zip -> /opt/cm/dist.zip
echo     h5.zip   -> /opt/cm/h5.zip
echo     jar      -> /opt/cm/app/cm-backend-1.0.0.jar
echo.
echo   Then run on server:
echo     cd /opt/cm/pc ^&^& rm -rf dist ^&^& unzip -o ../dist.zip
echo     cd /opt/cm/h5 ^&^& rm -rf * ^&^& unzip -o ../h5.zip
echo     systemctl restart cm
echo.
pause
exit /b 0

:fail
cd /d "%ROOT%"
echo Build failed.
pause
exit /b 1