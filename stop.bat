@echo off
echo.
echo Stopping all services...
taskkill /FI "WINDOWTITLE eq CM-Backend*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq CM-PC*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq CM-H5*" /F >nul 2>&1
echo Done.
pause