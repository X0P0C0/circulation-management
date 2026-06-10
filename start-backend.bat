@echo off
chcp 936 >nul 2>&1
cd /d "%~dp0cm-backend"
echo Starting Backend...
mvn spring-boot:run