# PKMon one-click build and run script.
# This file is intentionally ASCII-only so it works on Windows PowerShell 5.1
# regardless of the console code page.
#
# Usage:
#   powershell -ExecutionPolicy Bypass -File .\scripts\build_and_run.ps1
#   powershell -ExecutionPolicy Bypass -File .\scripts\build_and_run.ps1 -NoRun
#   powershell -ExecutionPolicy Bypass -File .\scripts\build_and_run.ps1 -SkipTests

[CmdletBinding()]
param(
    [switch]$NoRun,
    [switch]$SkipTests,
    [string]$JavaHome = $env:JAVA_HOME
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$Client = Join-Path $Root "client"
$Server = Join-Path $Root "server"
$Static = Join-Path $Server "src\main\resources\static"
$Jar = Join-Path $Server "target\pkmon-server-0.1.0.jar"
$Url = "http://127.0.0.1:4180/"

function Fail([string]$Message) {
    Write-Host ""
    Write-Host "ERROR: $Message" -ForegroundColor Red
    exit 1
}

function Find-CommandOrFail([string]$Name, [string]$Hint) {
    $Command = Get-Command $Name -ErrorAction SilentlyContinue
    if (-not $Command) {
        Fail "$Name not found. $Hint"
    }
    return $Command.Source
}

Write-Host "== PKMon one-click build ==" -ForegroundColor Cyan
Write-Host "Repository: $Root"

# Flutter
$Flutter = Find-CommandOrFail "flutter" "Install Flutter and add its bin directory to PATH."
Write-Host "Flutter: $Flutter"

# Java: prefer JAVA_HOME, then PATH. Require Java 17 or newer.
$JavaExe = $null
if ($JavaHome -and (Test-Path (Join-Path $JavaHome "bin\java.exe"))) {
    $JavaExe = Join-Path $JavaHome "bin\java.exe"
} else {
    $JavaCommand = Get-Command java -ErrorAction SilentlyContinue
    if ($JavaCommand) {
        $JavaExe = $JavaCommand.Source
    }
}
if (-not $JavaExe) {
    Fail "Java not found. Install JDK 17+ and set JAVA_HOME."
}

# java -version writes to stderr; capture it without treating it as a fatal error.
$OldErrorAction = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$JavaVersionOutput = & $JavaExe -version 2>&1
$ErrorActionPreference = $OldErrorAction
$JavaVersion = ($JavaVersionOutput | Select-Object -First 1).ToString()
if ($JavaVersion -match 'version "(\d+)') {
    $JavaMajor = [int]$Matches[1]
    if ($JavaMajor -lt 17) {
        Fail "Java 17+ is required, found: $JavaVersion"
    }
} else {
    Fail "Could not determine Java version: $JavaVersion"
}
$env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $JavaExe)
Write-Host "Java: $JavaExe ($JavaVersion)"

# Maven: prefer mvn on PATH, otherwise use the Maven Wrapper in server/.
$MavenCommand = Get-Command mvn -ErrorAction SilentlyContinue
if ($MavenCommand) {
    $Maven = $MavenCommand.Source
} else {
    $Maven = Join-Path $Server "mvnw.cmd"
    if (-not (Test-Path $Maven)) {
        Fail "Maven not found and server\mvnw.cmd is missing."
    }
}
Write-Host "Maven: $Maven"

# Step 1: build the Flutter web client.
Write-Host ""
Write-Host "[1/3] Building Flutter web client..." -ForegroundColor Cyan
Push-Location $Client
try {
    & $Flutter pub get
    if ($LASTEXITCODE -ne 0) {
        Fail "flutter pub get failed."
    }
    & $Flutter build web --release
    if ($LASTEXITCODE -ne 0) {
        Fail "flutter build web failed."
    }
} finally {
    Pop-Location
}

# Step 2: copy the web build into Spring Boot static resources.
Write-Host ""
Write-Host "[2/3] Copying web build into server static resources..." -ForegroundColor Cyan
if (-not (Test-Path $Static)) {
    New-Item -ItemType Directory -Path $Static | Out-Null
}
Get-ChildItem -LiteralPath $Static -Force |
    Where-Object { $_.Name -ne ".gitkeep" } |
    Remove-Item -Recurse -Force
$WebBuild = Join-Path $Client "build\web"
Get-ChildItem -LiteralPath $WebBuild -Force | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination $Static -Recurse -Force
}

# Step 3: package the Spring Boot server.
Write-Host ""
Write-Host "[3/3] Packaging Spring Boot server..." -ForegroundColor Cyan
Push-Location $Server
try {
    if ($SkipTests) {
        & $Maven -B clean package "-DskipTests"
    } else {
        & $Maven -B clean package
    }
    if ($LASTEXITCODE -ne 0) {
        Fail "Maven package failed."
    }
} finally {
    Pop-Location
}

if (-not (Test-Path $Jar)) {
    Fail "Jar not found: $Jar"
}

Write-Host ""
Write-Host "Build complete: $Jar" -ForegroundColor Green

if ($NoRun) {
    Write-Host ""
    Write-Host "Build only. Start the server later with:"
    Write-Host "  `"$JavaExe`" -jar `"$Jar`""
    exit 0
}

# If a server is already running, do not start a second one.
$AlreadyRunning = $false
try {
    $Response = Invoke-WebRequest -Uri "http://127.0.0.1:4180/test/hello" -UseBasicParsing -TimeoutSec 2
    if ($Response.StatusCode -eq 200) {
        $AlreadyRunning = $true
    }
} catch {
}

if ($AlreadyRunning) {
    Write-Host "PKMon server is already running at $Url"
    Write-Host "Close its Java window first if you want to use the newly built jar."
} else {
    Write-Host "Starting PKMon server..." -ForegroundColor Cyan
    Start-Process -FilePath $JavaExe `
        -ArgumentList @("-jar", "`"$Jar`"") `
        -WorkingDirectory $Server | Out-Null

    $Ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        Start-Sleep -Seconds 1
        try {
            $Response = Invoke-WebRequest -Uri "http://127.0.0.1:4180/test/hello" -UseBasicParsing -TimeoutSec 2
            if ($Response.StatusCode -eq 200) {
                $Ready = $true
                break
            }
        } catch {
        }
    }
    if (-not $Ready) {
        Fail "Server did not become ready. Check the new Java window for errors."
    }
}

Write-Host ""
Write-Host "Ready: $Url" -ForegroundColor Green
try {
    Start-Process $Url
} catch {
    Write-Host "Open this URL in your browser: $Url"
}
