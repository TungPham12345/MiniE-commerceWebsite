@echo off
chcp 65001 >nul
title Website Ban Hang - HighFashion
echo ========================================================
echo  KHOI DONG WEBSITE BAN HANG (HIGHFASHION)
echo  Chay truc tiep bang Antigravity - Khong can NetBeans
echo ========================================================
echo  Server dang khoi dong va se tu dong mo trinh duyet...
echo  Nhan Ctrl + C khi ban muon dung server.
echo ========================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\server.ps1" -Action run
