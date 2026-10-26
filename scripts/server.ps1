# =====================================================================
# Script quan ly Website Ban Hang (Tomcat 10.1 + Java 17 + Antigravity)
# Tu dong nhan dien thu muc, khong hardcode duong dan tuyet doi
# =====================================================================

param (
    [string]$Action = "start"
)

$ErrorActionPreference = "Stop"

# Tu dong xac dinh thu muc du an
$scriptDir = $PSScriptRoot
if (-not $scriptDir) { $scriptDir = (Get-Location).Path }
$workspaceRoot = (Resolve-Path "$scriptDir\..").Path
$parentDir = (Resolve-Path "$workspaceRoot\..").Path

# 1. Tu dong tim thu muc Tomcat
$tomcatHome = ""
if (Test-Path "$parentDir\apache-tomcat-10.1.60") {
    $tomcatHome = "$parentDir\apache-tomcat-10.1.60"
} elseif (Test-Path "$workspaceRoot\apache-tomcat-10.1.60") {
    $tomcatHome = "$workspaceRoot\apache-tomcat-10.1.60"
} elseif ($env:CATALINA_HOME -and (Test-Path $env:CATALINA_HOME)) {
    $tomcatHome = $env:CATALINA_HOME
} elseif (Test-Path "C:\Program Files\Apache Software Foundation\Tomcat 10.1") {
    $tomcatHome = "C:\Program Files\Apache Software Foundation\Tomcat 10.1"
}

# 2. Tu dong tim thu muc Java JDK
$javaHome = ""
if (Test-Path "$parentDir\jdk17") {
    $javaHome = "$parentDir\jdk17"
} elseif (Test-Path "$workspaceRoot\jdk17") {
    $javaHome = "$workspaceRoot\jdk17"
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $javaHome = $env:JAVA_HOME
} elseif (Test-Path "C:\Program Files\Java\jdk-17") {
    $javaHome = "C:\Program Files\Java\jdk-17"
}

$javaBin = if ($javaHome) { "$javaHome\bin" } else { "javac" }

# 3. Tu dong xac dinh cong Tomcat (doc tu server.xml hoac mac dinh 8080)
$port = 8080
if ($tomcatHome -and (Test-Path "$tomcatHome\conf\server.xml")) {
    $xmlContent = Get-Content "$tomcatHome\conf\server.xml" -Raw
    if ($xmlContent -match '<Connector port="(\d+)" protocol="HTTP/1.1"') {
        $port = [int]$matches[1]
    }
}

$url        = "http://localhost:$port/ban_hang/home"
$webSrc     = "$workspaceRoot\web"
$buildWeb   = "$workspaceRoot\build\web"
$classesDir = "$buildWeb\WEB-INF\classes"
$libDir     = "$buildWeb\WEB-INF\lib"
$deployDir  = if ($tomcatHome) { "$tomcatHome\webapps\ban_hang" } else { "" }

function Stop-TomcatServer {
    Write-Host "[STOP] Dang kiem tra va tat Tomcat tren cong $port..." -ForegroundColor Yellow
    try {
        $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
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
    if ($deployDir) {
        if (-not (Test-Path $deployDir)) { New-Item -ItemType Directory -Path $deployDir -Force | Out-Null }
        Copy-Item -Path "$webSrc\*" -Destination $deployDir -Recurse -Force
    }

    Write-Host "[2/3] Dong bo thu vien JAR..." -ForegroundColor Cyan
    if (-not (Test-Path $libDir)) {
        New-Item -ItemType Directory -Path $libDir -Force | Out-Null
    }
    if (Test-Path "$workspaceRoot\lib\*.jar") {
        Copy-Item -Path "$workspaceRoot\lib\*.jar" -Destination $libDir -Force
    }
    if ($deployDir) {
        $deployLib = "$deployDir\WEB-INF\lib"
        if (-not (Test-Path $deployLib)) { New-Item -ItemType Directory -Path $deployLib -Force | Out-Null }
        if (Test-Path "$workspaceRoot\lib\*.jar") {
            Copy-Item -Path "$workspaceRoot\lib\*.jar" -Destination $deployLib -Force
        }
    }

    Write-Host "[3/3] Bien dich ma nguon Java (javac UTF-8)..." -ForegroundColor Cyan
    if (-not (Test-Path $classesDir)) {
        New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
    }

    $jarPaths = @()
    if (Test-Path "$workspaceRoot\lib\*.jar") {
        $jarPaths += (Get-ChildItem -Path "$workspaceRoot\lib\*.jar" | ForEach-Object { $_.FullName })
    }
    if ($tomcatHome -and (Test-Path "$tomcatHome\lib\*.jar")) {
        $jarPaths += (Get-ChildItem -Path "$tomcatHome\lib\*.jar" | ForEach-Object { $_.FullName })
    }
    $jars = $jarPaths -join ";"

    $srcFiles = (Get-ChildItem -Path "$workspaceRoot\src\java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })

    $javacExe = if (Test-Path "$javaBin\javac.exe") { "$javaBin\javac.exe" } else { "javac" }
    & $javacExe -encoding UTF-8 -cp "$jars" -d $classesDir $srcFiles
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Loi bien dich Java!" -ForegroundColor Red
        exit 1
    }

    if ($deployDir) {
        $deployClasses = "$deployDir\WEB-INF\classes"
        if (-not (Test-Path $deployClasses)) { New-Item -ItemType Directory -Path $deployClasses -Force | Out-Null }
        Copy-Item -Path "$classesDir\*" -Destination $deployClasses -Recurse -Force
    }

    Write-Host "[OK] Bien dich hoan tat khong loi!" -ForegroundColor Green
}

function Start-TomcatServer {
    Build-Project

    $conns = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
    if ($conns) {
        Write-Host "[INFO] Tomcat dang chay san tren cong $port." -ForegroundColor Green
        Open-Website
        return
    }

    Write-Host "[RUN] Khoi dong Tomcat tren cong $port..." -ForegroundColor Cyan
    if ($javaHome) { $env:JAVA_HOME = $javaHome }
    if ($tomcatHome) { $env:CATALINA_HOME = $tomcatHome }

    $catBat = "$tomcatHome\bin\catalina.bat"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c start `"Tomcat Server (HighFashion)`" `"$catBat`" run"

    Write-Host "[WAIT] Dang doi server khoi dong..." -ForegroundColor Yellow
    $ready = $false
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 1
        $testConn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
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
    if ($javaHome) { $env:JAVA_HOME = $javaHome }
    if ($tomcatHome) { $env:CATALINA_HOME = $tomcatHome }
    
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
