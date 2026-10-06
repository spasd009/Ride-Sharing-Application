@echo off
cd /d "%~dp0"
java -version >nul 2>&1
if errorlevel 1 (
  echo Install Java 17 or newer and reopen this terminal.
  pause
  exit /b 1
)
if exist "ride-sharing-1.0.0.jar" (
  java -jar ride-sharing-1.0.0.jar
) else (
  call mvn spring-boot:run
)
pause
