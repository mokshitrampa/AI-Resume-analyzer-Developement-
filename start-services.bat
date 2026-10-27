@echo off
set JAVA_HOME=C:\Users\moksh\.jdks\jdk-21.0.12.1+1
set PATH=%JAVA_HOME%\bin;%PATH%

echo 1. Launching Spring Boot Backend on port 8080...
start "ResumeAnalyzer-Backend" /min cmd /c "C:\Users\moksh\.jdks\apache-maven-3.9.6\bin\mvn.cmd -f "%~dp0backend\pom.xml" spring-boot:run"

echo 2. Launching React Vite Frontend on port 5173...
start "ResumeAnalyzer-Frontend" /min cmd /c "npm run --prefix "%~dp0frontend" dev -- --host 0.0.0.0 --port 5173"

echo Opening application in browser...
start http://localhost:5173
