# Running Tests

## Install Prerequisites
Install on macOS with Homebrew:

```bash
brew install openjdk@21 maven
```

Install on Windows with WinGet:

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
winget install Apache.Maven
```

Verify:

```bash
java -version
mvn -version
```

## Commands to Run Tests
*Move to project root dir (`study-buddy/`*

Run all tests:
```bash
mvn test
```

Run one test class:
```bash
mvn -Dtest=LlmServiceTest test
```

Run one test method:
```bash
mvn -Dsurefire.useFile=false -Dtest=LlmServiceTest#generatesTrueFalseStudyMaterial test
```
