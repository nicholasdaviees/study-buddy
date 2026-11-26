#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# Setup env vars
export DB_URL="${DB_URL:-jdbc:mysql://localhost:3306/StudyBuddy}"
export DB_USER="${DB_USER:-studybuddy}"
export DB_PASSWORD="${DB_PASSWORD:-studybuddy}"
export OLLAMA_MODEL="${OLLAMA_MODEL:-qwen2.5:7b}"

# Install HomeBrew
if ! command -v brew >/dev/null 2>&1; then
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    if [[ -x /opt/homebrew/bin/brew ]]; then
        eval "$(/opt/homebrew/bin/brew shellenv)"
    elif [[ -x /usr/local/bin/brew ]]; then
        eval "$(/usr/local/bin/brew shellenv)"
    fi
fi

# Install the Java, build, database, server, and AI dependencies
brew update
brew install openjdk@21 maven mysql tomcat ollama

export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
export PATH="$(brew --prefix openjdk@21)/bin:$PATH"
export CATALINA_HOME="${CATALINA_HOME:-$(brew --prefix tomcat)/libexec}"

# Start MySQL and initialize the application database and account
brew services start mysql
sleep 3

MYSQL_ARGS=(-u root)
if [[ -n "${MYSQL_ROOT_PASSWORD:-}" ]]; then
    MYSQL_ARGS+=("-p${MYSQL_ROOT_PASSWORD}")
fi

mysql "${MYSQL_ARGS[@]}" <<SQL
CREATE DATABASE IF NOT EXISTS StudyBuddy CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
ALTER USER '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON StudyBuddy.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL

mysql "${MYSQL_ARGS[@]}" < "$PROJECT_ROOT/backend/database/schema.sql"

# Start Ollama and download model
if ! pgrep -x ollama >/dev/null 2>&1; then
    ollama serve >/tmp/study-buddy-ollama.log 2>&1 &
    sleep 3
fi
ollama pull "$OLLAMA_MODEL"

# Build the application and deploy it to Tomcat
cd "$PROJECT_ROOT"
mvn clean package -Dmaven.test.skip=true
cp target/study-buddy-1.0-SNAPSHOT.war "$CATALINA_HOME/webapps/studybuddy.war"
echo "Study Buddy is starting at http://localhost:8080/studybuddy/"
# Wait for Tomcat before opening the browser
"$CATALINA_HOME/bin/catalina.sh" start
for _ in {1..30}; do
    if curl -fsS "http://localhost:8080/studybuddy/" >/dev/null 2>&1; then
        open "http://localhost:8080/studybuddy/"
        exit 0
    fi
    sleep 2
done
echo "Tomcat started, but the application did not respond within 60 seconds." >&2
exit 1
