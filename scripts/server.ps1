# =====================================================================
# Script quan ly Website Ban Hang (Tomcat 10.1 + Java 17 + Antigravity)
# Khong can mo Apache NetBeans
# =====================================================================

param (
    [string]$Action = "start"
)

$ErrorActionPreference = "Stop"

$workspaceRoot = "d:\duan111\ban_hang"
$webSrc        = "$workspaceRoot\web"
$buildWeb      = "$workspaceRoot\build\web"
$classesDir    = "$buildWeb\WEB-INF\classes"
$libDir        = "$buildWeb\WEB-INF\lib"
$javaBin       = "C:\Program Files\Java\jdk-17\bin"
$tomcatHome    = "C:\Program Files\Apache Software Foundation\Tomcat 10.1"
$url           = "http://localhost:9999/ban_hang/"

function Stop-TomcatServer {
    Write-Host "[STOP] Dang kiem tra va tat Tomcat tren cong 9999..." -ForegroundColor Yellow
    try {
        $conns = Get-NetTCPConnection -LocalPort 9999 -ErrorAction SilentlyContinue
        if ($conns) {
            foreach ($conn in $conns) {
                $pidToKill = $conn.OwningProcess
                if ($pidToKill -gt 0) {
                    Write-Host "   Dung tien trinh PID: $pidToKill" -ForegroundColor Gray
                    Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
                }
            }
            Start-Sleep -Seconds 1
        }
        Get-CimInstance Win32_Process -Filter "CommandLine LIKE '%catalina%'" -ErrorAction SilentlyContinue | ForEach-Object {
            Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue
        }
        Write-Host "[OK] Da tat Tomcat thanh cong!" -ForegroundColor Green
    } catch {
        Write-Host "[INFO] Khong co tien trinh Tomcat nao dang chay." -ForegroundColor Gray
    }
}

function Build-Project {
    Write-Host "[1/3] Dong bo tai nguyen Web (JSP, CSS, JS, anh)..." -ForegroundColor Cyan
    if (-not (Test-Path $buildWeb)) {
        New-Item -ItemType Directory -Path $buildWeb -Force | Out-Null
    }
    Copy-Item -Path "$webSrc\*" -Destination $buildWeb -Recurse -Force

    Write-Host "[2/3] Dong bo thu vien JAR (mssql-jdbc, servlet-api)..." -ForegroundColor Cyan
    if (-not (Test-Path $libDir)) {
        New-Item -ItemType Directory -Path $libDir -Force | Out-Null
    }
    Copy-Item -Path "$workspaceRoot\lib\*.jar" -Destination $libDir -Force

    Write-Host "[3/3] Bien dich ma nguon Java (javac UTF-8)..." -ForegroundColor Cyan
    if (-not (Test-Path $classesDir)) {
        New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
    }
    $jars = (Get-ChildItem -Path "$workspaceRoot\lib\*.jar" | ForEach-Object { $_.FullName }) -join ";"
    $srcFiles = (Get-ChildItem -Path "$workspaceRoot\src\java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })

    & "$javaBin\javac.exe" -encoding UTF-8 -cp "$jars" -d $classesDir $srcFiles
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Loi bien dich Java!" -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Bien dich hoan tat khong loi!" -ForegroundColor Green
}

function Start-TomcatServer {
    Build-Project

    $conns = Get-NetTCPConnection -LocalPort 9999 -ErrorAction SilentlyContinue
    if ($conns) {
        Write-Host "[INFO] Tomcat dang chay san tren cong 9999." -ForegroundColor Green
        Open-Website
        return
    }

    Write-Host "[RUN] Khoi dong Tomcat 10.1..." -ForegroundColor Cyan
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
    $env:CATALINA_HOME = $tomcatHome

    $catBat = "$tomcatHome\bin\catalina.bat"
    Start-Process -FilePath $catBat -ArgumentList "run" -WindowStyle Minimized

    Write-Host "[WAIT] Dang doi server khoi dong..." -ForegroundColor Yellow
    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 1
        $testConn = Get-NetTCPConnection -LocalPort 9999 -ErrorAction SilentlyContinue
        if ($testConn) {
            $ready = $true
            break
        }
    }

    if ($ready) {
        Write-Host "[SUCCESS] Website da san sang tai: $url" -ForegroundColor Green
        Open-Website
    } else {
        Write-Host "[WARN] Dang hoan tat khoi dong. Ban co the truy cap $url sau vai giay." -ForegroundColor Yellow
    }
}

function Run-TomcatConsole {
    Build-Project
    Stop-TomcatServer

    Write-Host "[RUN] Khoi dong Tomcat truc tiep trong Terminal (Nhan Ctrl+C de dung)..." -ForegroundColor Cyan
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
    $env:CATALINA_HOME = $tomcatHome
    
    Start-Job -ScriptBlock {
        param($u)
        Start-Sleep -Seconds 6
        Start-Process $u
    } -ArgumentList $url | Out-Null

    $catBat = "$tomcatHome\bin\catalina.bat"
    & $catBat run
}

function Open-Website {
    Write-Host "[OPEN] Dang mo trinh duyet: $url" -ForegroundColor Cyan
    Start-Process $url
}

switch ($Action.ToLower()) {
    "start"   { Start-TomcatServer }
    "run"     { Run-TomcatConsole }
    "build"   { Build-Project }
    "stop"    { Stop-TomcatServer }
    "restart" { Stop-TomcatServer; Start-TomcatServer }
    "open"    { Open-Website }
    default   {
        Write-Host "Cach dung: .\server.ps1 [start|run|build|stop|restart|open]" -ForegroundColor Yellow
    }
}
