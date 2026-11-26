$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $PSScriptRoot
# Setup env vars
$env:DB_URL = if ($env:DB_URL) { $env:DB_URL } else { "jdbc:mysql://localhost:3306/StudyBuddy" }
$env:DB_USER = if ($env:DB_USER) { $env:DB_USER } else { "studybuddy" }
$env:DB_PASSWORD = if ($env:DB_PASSWORD) { $env:DB_PASSWORD } else { "studybuddy" }
$Model = if ($env:OLLAMA_MODEL) { $env:OLLAMA_MODEL } else { "qwen2.5:7b" }

function Refresh-EnvironmentPath {
    $env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [Environment]::GetEnvironmentVariable("Path", "User")
}

function Find-MySqlClient {
    return Get-ChildItem "C:\Program Files\MySQL" -Recurse -File -Filter "mysql.exe" -ErrorAction SilentlyContinue |
        Select-Object -First 1
}

# Install the Java, build, database, server, and AI dependencies
if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
    throw "Windows Package Manager (winget) is required. Install App Installer from the Microsoft Store."
}

winget install --id EclipseAdoptium.Temurin.21.JDK --exact --accept-package-agreements --accept-source-agreements
winget install --id Apache.Maven --exact --accept-package-agreements --accept-source-agreements
winget install --id Oracle.MySQL --exact --accept-package-agreements --accept-source-agreements
winget install --id Apache.Tomcat --exact --accept-package-agreements --accept-source-agreements
winget install --id Ollama.Ollama --exact --accept-package-agreements --accept-source-agreements

# Locate the installations added by winget
Refresh-EnvironmentPath
$java = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory -Filter "jdk-21*" | Select-Object -First 1
if (-not $java) { throw "Java 21 was not found after installation." }
$env:JAVA_HOME = $java.FullName

$mysql = Find-MySqlClient
if (-not $mysql) {
    throw "MySQL was not installed. The current winget MySQL package points to a broken download URL. Install MySQL Server manually, then rerun this script."
}

Get-Service | Where-Object { $_.Name -match "MySQL|MariaDB" } | Start-Service -ErrorAction SilentlyContinue

$tomcat = if ($env:CATALINA_HOME) {
    $env:CATALINA_HOME
} else {
    (Get-ChildItem "C:\Program Files\Apache Software Foundation" -Directory -Filter "Tomcat*" | Select-Object -First 1).FullName
}
if (-not $tomcat) { throw "Tomcat was not found after installation." }
$env:CATALINA_HOME = $tomcat

# Initialize the application database and account
$mysqlArgs = @("-u", "root")
if ($env:MYSQL_ROOT_PASSWORD) { $mysqlArgs += @("-p$env:MYSQL_ROOT_PASSWORD") }

$sql = @"
CREATE DATABASE IF NOT EXISTS StudyBuddy CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS '$env:DB_USER'@'localhost' IDENTIFIED BY '$env:DB_PASSWORD';
ALTER USER '$env:DB_USER'@'localhost' IDENTIFIED BY '$env:DB_PASSWORD';
GRANT ALL PRIVILEGES ON StudyBuddy.* TO '$env:DB_USER'@'localhost';
FLUSH PRIVILEGES;
"@
$sql | & $mysql.FullName @mysqlArgs
Get-Content "$ProjectRoot\backend\database\schema.sql" | & $mysql.FullName @mysqlArgs

# Start Ollama and download model
if (-not (Get-Process ollama -ErrorAction SilentlyContinue)) {
    Start-Process ollama -ArgumentList "serve"
    Start-Sleep -Seconds 3
}
ollama pull $Model

# Build the application and deploy it to Tomcat
Set-Location $ProjectRoot
mvn clean package -Dmaven.test.skip=true
Copy-Item "target\study-buddy-1.0-SNAPSHOT.war" "$env:CATALINA_HOME\webapps\studybuddy.war" -Force

Write-Host "Study Buddy is starting at http://localhost:8080/studybuddy/"
# Wait for Tomcat before opening the browser
Start-Process -FilePath "$env:CATALINA_HOME\bin\catalina.bat" -ArgumentList "run"
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    try {
        $response = Invoke-WebRequest "http://localhost:8080/studybuddy/" -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
            Start-Process "http://localhost:8080/studybuddy/"
            exit 0
        }
    } catch {
        Start-Sleep -Seconds 2
    }
}
throw "Tomcat started, but the application did not respond within 60 seconds."
