# Study Buddy Deployment Document
This document covers how to build and deploy the Study Buddy application on MacOS and Windows. On each OS, there are three paths covered in this document: deploying the application from scratch, deploying the application after the dependencies have already been installed, and deploying the application using the automated scripts.

---
### Table of Contents:
- Deployment From Scratch
  - [MacOS](#macos---deployment-from-scratch)
  - [Windows](#windows---deployment-from-scratch)
- Deployment Post Dependency Install
  - [MacOS](#macos---deployment-post-dependency-install)
  - [Windows](#windows---deployment-post-dependency-install)
- Deployment With Automated Scripts
  - [MacOS](#macos---deployment-automated-scripts)
  - [Windows](#windows---deployment-automated-scripts)
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
If your MySQL root account has a password, add `-p` to both MySQL commands.

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

1. Go to the project folder:
```powershell
cd study-buddy
```

2. Install project dependencies:
```powershell
winget install EclipseAdoptium.Temurin.21.JDK
winget install Apache.Maven
winget install Oracle.MySQL
winget install Ollama.Ollama
```

Install Tomcat 11 from the Apache Tomcat website, then set the Tomcat path:
```powershell
$env:CATALINA_HOME = "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
```

3. Configure Java and Tomcat:
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21"
$env:CATALINA_HOME = "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
```

4. Start MySQL:
```powershell
Start-Service MySQL80
```

5. Create the database and application user:
```powershell
@"
CREATE DATABASE IF NOT EXISTS StudyBuddy CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'studybuddy'@'localhost' IDENTIFIED BY 'studybuddy';
ALTER USER 'studybuddy'@'localhost' IDENTIFIED BY 'studybuddy';
GRANT ALL PRIVILEGES ON StudyBuddy.* TO 'studybuddy'@'localhost';
FLUSH PRIVILEGES;
"@ | mysql -u root
```
Load the schema:
```powershell
Get-Content backend\database\schema.sql | mysql -u root
```

6. Start Ollama:
```powershell
Start-Process ollama -ArgumentList "serve"
ollama pull qwen2.5:7b
```

7. Set database variables:
```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/StudyBuddy'
$env:DB_USER = 'studybuddy'
$env:DB_PASSWORD = 'studybuddy'
```

8. Build the application:
```powershell
mvn clean package -Dmaven.test.skip=true
```

9. Deploy the WAR:
```powershell
Copy-Item target\study-buddy-1.0-SNAPSHOT.war `
  "$env:CATALINA_HOME\webapps\studybuddy.war"
```

10. Start Tomcat:
```powershell
& "$env:CATALINA_HOME\bin\catalina.bat" start
```

11. Open Study Buddy:
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
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21"
$env:CATALINA_HOME = "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
```

3. Start MySQL:
```powershell
Start-Service MySQL80
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
mvn clean package -Dmaven.test.skip=true
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

# MacOS - Deployment With Automated Scripts

1. Go to the project folder:
```bash
cd study-buddy
```

2. Make the script executable:
```bash
chmod +x scripts/deploy-macos.sh
```

3. Run the deployment script:
```bash
./scripts/deploy-macos.sh
```

# Windows - Deployment With Automated Scripts

1. Go to the project folder:
```powershell
cd study-buddy
```

2. Set the Tomcat and MySQL paths:
```powershell
$env:CATALINA_HOME = "C:\Program Files\Apache Software Foundation\Tomcat 11.0"
$env:MYSQL_SERVICE = "MySQL80"
```

3. Run the deployment script:
```powershell
.\scripts\deploy-windows.ps1
```

# MacOS - Stopping Study Buddy
```bash
"$CATALINA_HOME/bin/catalina.sh" stop # stop Tomcat
brew services stop mysql # stop MySQL
pkill -x ollama # stop Ollama
```
# Windows - Stopping Study Buddy
```bash
& "$env:CATALINA_HOME\bin\catalina.bat" stop # stop Tomcat
Stop-Service MySQL80 # stop MySQL
Get-Process ollama -ErrorAction SilentlyContinue | Stop-Process -Force # stop Ollama
```
