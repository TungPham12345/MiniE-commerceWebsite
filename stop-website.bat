@echo off
chcp 65001 >nul
title Dung Server Website - HighFashion
echo ========================================================
echo  DỪNG SERVER WEBSITE BÁN HÀNG
echo ========================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\server.ps1" -Action stop
pause
