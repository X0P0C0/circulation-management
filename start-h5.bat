@echo off
set "PATH=%APPDATA%\fnm\node-versions\v20.20.2\installation;%PATH%"
cd /d "%~dp0cm-h5"
npm run dev