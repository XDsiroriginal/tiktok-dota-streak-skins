@echo off
chcp 65001 > nul
title TikTok Dota 2 Streak Flame Skins Installer
echo ========================================================
echo       TikTok Dota 2 Streak Flame Skins Installer
echo ========================================================
echo.

where python >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Python не найден в системе! Установите Python 3.10+ с сайта python.org
    pause
    exit /b 1
)

python "%~dp0install.py"
if %errorlevel% neq 0 (
    echo.
    echo [!] Программа завершилась с ошибкой.
    pause
)
