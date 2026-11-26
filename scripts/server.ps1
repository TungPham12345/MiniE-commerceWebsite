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

# 3. Dung CATALINA_BASE rieng de khong nap cac ung dung khac trong Tomcat dung chung
$port         = 10000
$shutdownPort = $port + 5
$catalinaBase = "$workspaceRoot\build\tomcat-base"

$url        = "http://localhost:$port/ban_hang/home"
$webSrc     = "$workspaceRoot\web"
$buildWeb   = "$workspaceRoot\build\web"
$classesDir = "$buildWeb\WEB-INF\classes"
$libDir     = "$buildWeb\WEB-INF\lib"
$deployDir  = "$catalinaBase\webapps\ban_hang"

function Test-PortListening {
    param([int]$TargetPort)
    $listeners = [System.Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners()
    foreach ($endpoint in $listeners) {
        if ($endpoint.Port -eq $TargetPort) {
            return $true
        }
    }
    return $false
}

function Initialize-IsolatedTomcatBase {
    if (-not $tomcatHome -or -not (Test-Path "$tomcatHome\conf\server.xml")) {
        throw "Khong tim thay cau hinh Tomcat tai: $tomcatHome"
    }

    $baseConf = "$catalinaBase\conf"
    $baseServerXml = "$baseConf\server.xml"
    if (-not (Test-Path $baseServerXml)) {
        New-Item -ItemType Directory -Path $baseConf -Force | Out-Null
        Get-ChildItem -Path "$tomcatHome\conf" -Force |
            Where-Object { $_.Name -ne "Catalina" } |
            ForEach-Object {
                Copy-Item -LiteralPath $_.FullName -Destination $baseConf -Recurse -Force
            }
    }
    if (-not (Test-Path $baseServerXml)) {
        throw "Khong the tao server.xml cho Tomcat rieng tai: $baseServerXml"
    }

    @(
        "$catalinaBase\logs",
        "$catalinaBase\temp",
        "$catalinaBase\work",
        "$catalinaBase\webapps",
        "$baseConf\Catalina\localhost"
    ) | ForEach-Object {
        if (-not (Test-Path $_)) {
            New-Item -ItemType Directory -Path $_ -Force | Out-Null
        }
    }

    $serverConfig = New-Object System.Xml.XmlDocument
    $serverConfig.PreserveWhitespace = $true
    $serverConfig.Load($baseServerXml)
    $configChanged = $false

    $serverNode = $serverConfig.SelectSingleNode('/Server')
    if ($null -ne $serverNode -and $serverNode.GetAttribute("port") -ne [string]$shutdownPort) {
        $serverNode.SetAttribute("port", [string]$shutdownPort)
        $configChanged = $true
    }

    $connector = $serverConfig.SelectSingleNode('/Server/Service[@name="Catalina"]/Connector[@protocol="HTTP/1.1"]')
    if ($null -eq $connector) {
        throw "Khong tim thay HTTP Connector trong: $baseServerXml"
    }
    if ($connector.GetAttribute("port") -ne [string]$port) {
        $connector.SetAttribute("port", [string]$port)
        $configChanged = $true
    }
    if ($configChanged) {
        $serverConfig.Save($baseServerXml)
    }

    $contextConfig = New-Object System.Xml.XmlDocument
    $context = $contextConfig.CreateElement("Context")
    $context.SetAttribute("docBase", $buildWeb)
    [void]$contextConfig.AppendChild($context)
    $manager = $contextConfig.CreateElement("Manager")
    $manager.SetAttribute("pathname", "")
    [void]$context.AppendChild($manager)
    $contextConfig.Save("$baseConf\Catalina\localhost\ban_hang.xml")
}

function Import-SmtpEnvironment {
    foreach ($name in @("GMAIL_SMTP_USERNAME", "GMAIL_SMTP_APP_PASSWORD")) {
        $userValue = [Environment]::GetEnvironmentVariable($name, "User")
        if (-not [string]::IsNullOrWhiteSpace($userValue)) {
            [Environment]::SetEnvironmentVariable($name, $userValue, "Process")
        }
    }

    $configured = -not [string]::IsNullOrWhiteSpace($env:GMAIL_SMTP_USERNAME) -and
        -not [string]::IsNullOrWhiteSpace($env:GMAIL_SMTP_APP_PASSWORD)
    Write-Host "[INFO] SMTP environment available to Tomcat: $configured" -ForegroundColor Cyan
}

function Stop-TomcatServer {
    Write-Host "[STOP] Dang kiem tra va tat Tomcat tren cong $port..." -ForegroundColor Yellow
    $processes = Get-CimInstance Win32_Process -ErrorAction Stop | Where-Object {
        $_.ProcessId -ne $PID -and $_.Name -match '^(java|javaw)\.exe$' -and
        $_.CommandLine -and $_.CommandLine.IndexOf($catalinaBase, [StringComparison]::OrdinalIgnoreCase) -ge 0
    }
    if (-not $processes) {
        Write-Host "[INFO] Khong co Tomcat rieng nao dang chay." -ForegroundColor Gray
        return
    }

    foreach ($process in $processes) {
        Write-Host "   Dung Tomcat rieng PID: $($process.ProcessId)" -ForegroundColor Gray
        Stop-Process -Id $process.ProcessId -Force -ErrorAction SilentlyContinue
    }
    Start-Sleep -Milliseconds 500
    Write-Host "[OK] Da dung Tomcat rieng." -ForegroundColor Green
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
    if (Test-PortListening -TargetPort $port) {
        $conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        $processIds = $conns | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($processId in $processIds) {
            $process = Get-CimInstance Win32_Process -Filter "ProcessId=$processId" -ErrorAction SilentlyContinue
            if ($process -and $process.CommandLine -and
                    $process.CommandLine.IndexOf($catalinaBase, [StringComparison]::OrdinalIgnoreCase) -ge 0) {
                Write-Host "[INFO] Tomcat rieng dang chay san tren cong $port." -ForegroundColor Green
                Open-Website
                return
            }
        }
        throw "Cong $port dang duoc tien trinh khac su dung. Khong dung tien trinh do."
    }

    Import-SmtpEnvironment
    Build-Project
    Initialize-IsolatedTomcatBase

    if (Test-PortListening -TargetPort $port) {
        throw "Cong $port vua duoc tien trinh khac su dung. Khong khoi dong Tomcat."
    }

    Write-Host "[RUN] Khoi dong Tomcat tren cong $port..." -ForegroundColor Cyan
    if ($javaHome) { $env:JAVA_HOME = $javaHome }
    if ($tomcatHome) { $env:CATALINA_HOME = $tomcatHome }
    $env:CATALINA_BASE = $catalinaBase

    $catBat = "$tomcatHome\bin\catalina.bat"
    Start-Process -FilePath $catBat -ArgumentList "run" -WorkingDirectory "$tomcatHome\bin" | Out-Null

    Write-Host "[WAIT] Dang doi server khoi dong..." -ForegroundColor Yellow
    $ready = $false
    for ($i = 0; $i -lt 100; $i++) {
        Start-Sleep -Milliseconds 300
        if (Test-PortListening -TargetPort $port) {
            try {
                $req = [System.Net.HttpWebRequest]::Create("http://127.0.0.1:$port/")
                $req.Method = "HEAD"
                $req.Timeout = 1500
                $res = $req.GetResponse()
                $res.Close()
                $ready = $true
                break
            } catch [System.Net.WebException] {
                if ($null -ne $_.Exception.Response) {
                    $_.Exception.Response.Close()
                    $ready = $true
                    break
                }
            } catch {}
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
    Stop-TomcatServer
    Import-SmtpEnvironment
    Build-Project
    Initialize-IsolatedTomcatBase

    Write-Host "[RUN] Khoi dong Tomcat truc tiep trong Terminal (Nhan Ctrl+C de dung)..." -ForegroundColor Cyan
    if ($javaHome) { $env:JAVA_HOME = $javaHome }
    if ($tomcatHome) { $env:CATALINA_HOME = $tomcatHome }
    $env:CATALINA_BASE = $catalinaBase
    
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
