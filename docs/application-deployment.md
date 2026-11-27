# Study Buddy Deployment Document
This document covers how to build and deploy the Study Buddy application on MacOS and Windows. On each OS, there are two paths covered in this document: deploying the application from scratch and deploying the application after the dependencies have already been installed.

---
### Table of Contents:
- Deployment From Scratch
  - [MacOS](#macos---deployment-from-scratch)
  - [Windows](#windows---deployment-from-scratch)
- Deployment Post Dependency Install
  - [MacOS](#macos---deployment-post-dependency-install)
  - [Windows](#windows---deployment-post-dependency-install)
- Stopping Study Buddy
  - [MacOS](#macos---stopping-study-buddy)
  - [Windows](#windows---stopping-study-buddy)
---

# MacOS - Deployment From Scratch
*Estimated time to complete: 15-20 minutes (mostly to download Ollama model)*
1. Go to the project folder:
  ```bash
  cd study-buddy
  ```

2. Install Homebrew:
  ```bash
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
  brew --version # confirm Homebrew installation
  ```

3. Install project dependencies:
  ```bash
  brew update
  brew install openjdk@21 maven mysql tomcat ollama
  ```

4. Configure Java and Tomcat:

If using Zsh shell use:
  ```bash
  export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
  export PATH="$(brew --prefix openjdk@21)/bin:$PATH"
  export CATALINA_HOME="$(brew --prefix tomcat)/libexec"
  ```

If using Fish shell use:
  ```bash
  set -gx JAVA_HOME (brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home
  set -gx PATH (brew --prefix openjdk@21)/bin $PATH
  set -gx CATALINA_HOME (brew --prefix tomcat)/libexec
  ```

5. Start MySQL:
  ```bash
  brew services start mysql
  ```

6. Create the database and application user:
  ```bash
  mysql -u root <<'SQL'
  CREATE DATABASE IF NOT EXISTS StudyBuddy
    CHARACTER SET utf8mb4;
  CREATE USER IF NOT EXISTS 'studybuddy'@'localhost'
    IDENTIFIED BY 'studybuddy';
  ALTER USER 'studybuddy'@'localhost'
    IDENTIFIED BY 'studybuddy';
  GRANT ALL PRIVILEGES ON StudyBuddy.*
    TO 'studybuddy'@'localhost';
  FLUSH PRIVILEGES;
  SQL
  ```
Load the schema:
  ```bash
  mysql -u root < backend/database/schema.sql
  ```

7. Start Ollama:
  ```bash
  ollama serve # start Ollama
  ollama pull qwen2.5:7b # do this in separate terminal from the ollama serve
  ```

8. Set database variables:
If using Zsh shell use:
  ```bash
  export DB_URL='jdbc:mysql://localhost:3306/StudyBuddy'
  export DB_USER='studybuddy'
  export DB_PASSWORD='studybuddy'
  ```

If using Fish shell use:
  ```bash
  set -gx DB_URL 'jdbc:mysql://localhost:3306/StudyBuddy'
  set -gx DB_USER 'studybuddy'
  set -gx DB_PASSWORD 'studybuddy'
  ```

9. Build the application:
  ```bash
  mvn clean package -Dmaven.test.skip=true
  ```

10. Deploy the WAR:
  ```bash
  cp target/study-buddy-1.0-SNAPSHOT.war \
    "$CATALINA_HOME/webapps/studybuddy.war"
  ```

11. Start Tomcat:
  ```bash
  "$CATALINA_HOME/bin/catalina.sh" start
  ```

12. Open Study Buddy:
  ```bash
  open "http://localhost:8080/studybuddy/login.html"
  ```
Or open browser and navigate to: http://localhost:8080/studybuddy/login.html

# Windows - Deployment From Scratch
*Estimated time to complete: 15-20 minutes (mostly to download Ollama model)*

**Open Powershell as Administrator**
1. Go to the project folder:
  ```powershell
  cd study-buddy
  ```

2. Install winget:
  ```powershell
  Add-AppxPackage -RegisterByFamilyName -MainPackage Microsoft.DesktopAppInstaller_8wekyb3d8bbwe
  ```
If the above installation command doesn't work, you may need to enter this one instead:
  ```powershell
  Install-PackageProvider -Name NuGet -Force | Out-Null
  Install-Module -Name Microsoft.WinGet.Client -Force -Repository PSGallery | Out-Null
  Repair-WinGetPackageManager -Force -Latest
  ```
Confirm installation:
  ```powershell
  winget --version
  ```

2. Install project dependencies:
  ```powershell
  winget install EclipseAdoptium.Temurin.21.JDK
  winget install Oracle.MySQL
  winget install Ollama.Ollama
  ```

3. Install Maven:

Can use Scoop or Chocolatey to install:
  ```powershell
  scoop install main/maven # scoop
  choco install maven # Chocolatey
  ```
If you don't have those package managers installed, you can download it directly from the website.
  ```powershell
  $mavenVersion = "3.9.11"
  $installRoot = "$env:USERPROFILE\Tools"
  $zipPath = "$env:TEMP\apache-maven.zip"
  $downloadUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip"

  New-Item -ItemType Directory -Force $installRoot | Out-Null

  Invoke-WebRequest `
    -Uri $downloadUrl `
    -OutFile $zipPath

  Expand-Archive `
    -Path $zipPath `
    -DestinationPath $installRoot `
    -Force

  $mavenHome = "$installRoot\apache-maven-$mavenVersion"

  [Environment]::SetEnvironmentVariable(
    "MAVEN_HOME",
    $mavenHome,
    "User"
  )

  $userPath = [Environment]::GetEnvironmentVariable("Path", "User")

  [Environment]::SetEnvironmentVariable(
    "Path",
    "$userPath;$mavenHome\bin",
    "User"
  )
  ```
- After installing, you may need to close and reopen the powershell window. Then verify the installation with:
  ```powershell
  mvn --version
  ```

3. Install Apache Tomcat:
- Navigate to and download the zip: https://tomcat.apache.org/download-11.cgi
- Go to downloads folder, right click, and choose **Extract All**
  - You can extract to any folder, but this documentation assumes you extract to the Downloads folder.
  - If you extract to a different folder, then update the path below to the appropriate folder.

4. Configure Java and Tomcat:

**NOTE:** ```REPLACE_WITH_YOUR_USERNAME``` in the ```CATALINA_HOME``` path.
Double check both paths below, they might need to be changed if your files are named differently.
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
  $env:CATALINA_HOME = "C:\Users\*********REPLACE_WITH_YOUR_USERNAME*********\Downloads\apache-tomcat-11.0.29-windows-x64\apache-tomcat-11.0.29" # your path to the extracted folder may be different. Change accordingly
  ```

5. Configure MySQL:
- Press Windows key and open up "MySQL Configurator"
- Go through setup screens:
  - Create a root password
  - Click **Add User**
    - Username: studybuddy
    - Host: localhost
    - Password: studybuddy
    - Leave role as **DB Admin**
  - Enable Configure MySQL Server as a Windows Service
  - Use the service name
  - Enable Start the MySQL Server at System Startup
  - In **Apply Configuration**, click **Execute**, then finish
- Start MySQL:
   ```powershell
   Get-Service MySQL84
   Start-Service MySQL84
   # may get an "error" on last command if MySQL is already started. This is fine verify it's running with below command
   ```
- Verify it's up and running:
   ```powershell
   $mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.4\bin"
   & "$mysqlBin\mysqladmin.exe" -u root -p ping
   # Expected result: mysqld is alive
   ```
- Lastly, load the database schema:
   ```powershell
    Get-Content backend\database\schema.sql -Raw | & "$mysqlBin\mysql.exe" -u root -p
    # enter root password
   ```
- You can verify the tables if you want:
   ```powershell
   & "$mysqlBin\mysql.exe" -u studybuddy -p StudyBuddy -e "SHOW TABLES;"
   # enter "studybuddy" as password
   ```

6. Create the database and application user:
  ```powershell
  $mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.4\bin"

  @"
  CREATE DATABASE IF NOT EXISTS StudyBuddy CHARACTER SET utf8mb4;
  CREATE USER IF NOT EXISTS 'studybuddy'@'localhost' IDENTIFIED BY 'studybuddy';
  ALTER USER 'studybuddy'@'localhost' IDENTIFIED BY 'studybuddy';
  GRANT ALL PRIVILEGES ON StudyBuddy.* TO 'studybuddy'@'localhost';
  FLUSH PRIVILEGES;
  "@ | & "$mysqlBin\mysql.exe" -u root -p

  # enter your root password
  ```
Load the schema:
  ```powershell
  Get-Content backend\database\schema.sql -Raw | & "$mysqlBin\mysql.exe" -u root -p
  # enter your root password
  ```

7. Start Ollama:
  ```powershell
  Start-Process ollama -ArgumentList "serve"
  ollama pull qwen2.5:7b
  ```

8. Set database variables:
  ```powershell
  $env:DB_URL = 'jdbc:mysql://localhost:3306/StudyBuddy'
  $env:DB_USER = 'studybuddy'
  $env:DB_PASSWORD = 'studybuddy'
  ```

9. Build the application:
  ```powershell
  mvn clean package "-Dmaven.test.skip=true"
  ```

10. Deploy the WAR:
  ```powershell
  Copy-Item target\study-buddy-1.0-SNAPSHOT.war `
    "$env:CATALINA_HOME\webapps\studybuddy.war"
  ```

11. Start Tomcat:
  ```powershell
  & "$env:CATALINA_HOME\bin\catalina.bat" start
  ```

12. Open Study Buddy:
  ```powershell
  Start-Process "http://localhost:8080/studybuddy/login.html"
  ```
Or open browser and navigate to: http://localhost:8080/studybuddy/login.html


# MacOS - Deployment Post Dependency Install
*Estimated time to complete: 1-2 minutes*
1. Go to the project folder:
  ```bash
  cd study-buddy
  ```

2. Configure Java and Tomcat:

If using Zsh shell use:
  ```bash
  export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
  export PATH="$(brew --prefix openjdk@21)/bin:$PATH"
  export CATALINA_HOME="$(brew --prefix tomcat)/libexec"
  ```

If using Fish shell use:
  ```bash
  set -gx JAVA_HOME (brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home
  set -gx PATH (brew --prefix openjdk@21)/bin $PATH
  set -gx CATALINA_HOME (brew --prefix tomcat)/libexec
  ```

3. Start MySQL:
  ```bash
  brew services start mysql
  ```

4. Start Ollama:
  ```bash
  ollama serve
  ```

5. Set database variables:

If using Zsh shell use:
  ```bash
  # do this in separate terminal from the ollama serve
  export DB_URL='jdbc:mysql://localhost:3306/StudyBuddy'
  export DB_USER='studybuddy'
  export DB_PASSWORD='studybuddy'
  ```

If using Fish shell use:
  ```bash
  # do this in separate terminal from the ollama serve
  set -gx DB_URL 'jdbc:mysql://localhost:3306/StudyBuddy'
  set -gx DB_USER 'studybuddy'
  set -gx DB_PASSWORD 'studybuddy'
  ```

6. Build the application:
  ```bash
  mvn clean package -Dmaven.test.skip=true
  ```

7. Deploy the WAR:
  ```bash
  cp target/study-buddy-1.0-SNAPSHOT.war \
    "$CATALINA_HOME/webapps/studybuddy.war"
  ```

8. Start Tomcat:
  ```bash
  "$CATALINA_HOME/bin/catalina.sh" start
  ```

12. Open Study Buddy:
  ```bash
  open "http://localhost:8080/studybuddy/login.html"
  ```
Or open browser and navigate to: http://localhost:8080/studybuddy/login.html

# Windows - Deployment Post Dependency Install

*Estimated time to complete: 1-2 minutes*
1. Go to the project folder:
  ```powershell
  cd study-buddy
  ```

2. Configure Java and Tomcat:
**NOTE:** ```REPLACE_WITH_YOUR_USERNAME``` in the ```CATALINA_HOME``` path.
Double check both paths below, they might need to be changed if your files are named differently.
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.10002-hotspot"
  $env:CATALINA_HOME = "C:\Users\*********REPLACE_WITH_YOUR_USERNAME*********\Downloads\apache-tomcat-11.0.29-windows-x64\apache-tomcat-11.0.29" # your path to the extracted folder may be different. Change accordingly
  ```

3. Start MySQL:
  ```powershell
  Get-Service MySQL84
  Start-Service MySQL84
  # may get an "error" on last command if MySQL is already started. This is fine verify it's running with below command
  ```

4. Start Ollama:
  ```powershell
  Start-Process ollama -ArgumentList "serve"
  ```

5. Set database variables:
  ```powershell
  $env:DB_URL = 'jdbc:mysql://localhost:3306/StudyBuddy'
  $env:DB_USER = 'studybuddy'
  $env:DB_PASSWORD = 'studybuddy'
  ```

6. Build the application:
  ```powershell
  mvn clean package "-Dmaven.test.skip=true"
  ```

7. Deploy the WAR:
  ```powershell
  Copy-Item target\study-buddy-1.0-SNAPSHOT.war `
    "$env:CATALINA_HOME\webapps\studybuddy.war"
  ```

8. Start Tomcat:
  ```powershell
  & "$env:CATALINA_HOME\bin\catalina.bat" start
  ```

12. Open Study Buddy:
  ```powershell
  Start-Process "http://localhost:8080/studybuddy/login.html"
  ```
Or open browser and navigate to: http://localhost:8080/studybuddy/login.html

# MacOS - Stopping Study Buddy
  ```bash
  # stop tomcat, stop mysql, stop ollama
  "$CATALINA_HOME/bin/catalina.sh" stop
  brew services stop mysql
  pkill -x ollama
  ```
# Windows - Stopping Study Buddy
  ```powershell
  # stop tomcat, stop mysql, stop ollama
  & "$env:CATALINA_HOME\bin\catalina.bat" stop
  Stop-Service MySQL84
  Get-Process ollama -ErrorAction SilentlyContinue | Stop-Process -Force
  ```
