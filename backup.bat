@echo off
chcp 936 >nul 2>&1
echo.
echo ========================================
echo   CM Database Backup
echo ========================================
echo.

set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_DIR=%~dp0backups
set BACKUP_FILE=%BACKUP_DIR%\cm_db_%TIMESTAMP%.sql

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

echo [1/2] Exporting database...
ssh root@101.133.175.15 "mysqldump -u root -p123456 cm_db" > "%BACKUP_FILE%"
if %errorlevel% neq 0 (
    echo Backup failed.
    pause
    exit /b 1
)
echo   Database exported to: %BACKUP_FILE%

echo [2/2] Compressing backup...
powershell -Command "Compress-Archive -Path '%BACKUP_FILE%' -DestinationPath '%BACKUP_FILE%.zip' -Force"
del /q "%BACKUP_FILE%"
echo   Backup compressed: %BACKUP_FILE%.zip

echo.
echo ========================================
echo   Backup Complete!
echo ========================================
echo.
echo   Backup location: %BACKUP_DIR%
echo.
pause
