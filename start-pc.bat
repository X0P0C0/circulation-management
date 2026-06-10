@echo off
chcp 936 >nul 2>&1
set "PATH=%APPDATA%\fnm\node-versions\v20.20.2\installation;%PATH%"
cd /d "%~dp0cm-pc"
echo Starting PC Frontend...
npm run dev